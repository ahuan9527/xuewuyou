package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }

        //设置条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //设置对象
        CmsPage cmsPage = new CmsPage();
        //站点id
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //模板id
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        //分页测试
        if (page<=0){
            page=1;
        }
        page = page -1;
        if (size<=0){
            size =10;
        }
        Pageable pageable = PageRequest.of(page,size);
        //创建example
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    //添加页面
    @Override
    public CmsPageResult add(CmsPage cmsPage) {
        //校验页面是否存在
        CmsPage byPageNameAndsAndSiteIdAndPageWebPath = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (byPageNameAndsAndSiteIdAndPageWebPath != null){
            //抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }

    @Override
    public CmsPage findPageById(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }


    @Override
    public CmsPageResult editPage(String id, CmsPage cmsPage) {

        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()){
            CmsPage one = byId.get();
            if (one!=null){
                //跟新id
                one.setTemplateId(cmsPage.getTemplateId());
                //更新所属站点
                one.setSiteId(cmsPage.getSiteId());
                //更新页面别名
                one.setPageAliase(cmsPage.getPageAliase());
                //更新页面名称
                one.setPageName(cmsPage.getPageName());
                //更新访问路径
                one.setPageWebPath(cmsPage.getPageWebPath());
                //更新物理路径
                one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
                //dataurl
                one.setDataUrl(cmsPage.getDataUrl());
                //执行更新
                CmsPage save=cmsPageRepository.save(one);
                if (save!=null){
                    return new CmsPageResult(CommonCode.SUCCESS,save);
                }
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    @Override
    public ResponseResult delPage(String id) {
        //查询一下
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 静态化程序获取页面的DataUrl
     * 静态化程序远程请求DataUrl获取数据模型。
     * 静态化程序获取页面的模板信息
     * 执行页面静态化
     * @param pageId
     * @return
     */
    @Override
    public String getPageHtml(String pageId) {

        //获取页面模型数据
        Map model = this.getModelByPageId(pageId);
        if (model == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String templateContent= getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(templateContent)){
            //模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html= generateHtml(templateContent,model);
        if (StringUtils.isEmpty(html)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //页面发布
    @Override
    public ResponseResult postPage(String pageId) {
        //执行页面的静态化
        String pageHtml=this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //保存静态化文件
        CmsPage cmsPage=saveHtml(pageId,pageHtml);
        //发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }



    //发送页面发布消息
    private void sendPostPage(String pageId) {
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        Map<String,String>msgMap=new HashMap<>();
        msgMap.put("pageId",pageId);
        //消息内容
        String msg=JSON.toJSONString(msgMap);
        //获取站点的id作为routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息
        this.rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,msg);
    }

    //保存静态内容
    private CmsPage saveHtml(String pageId, String content) {
        //查询页面
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(htmlFileId)){
            //执行删除
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html文件到GridFS
        InputStream inputStream= null;
        try {
            inputStream = IOUtils.toInputStream(content,"utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectId objectId=gridFsTemplate.store(inputStream,cmsPage.getPageName());
        //文件id
        String fileId = objectId.toString();
        //将文件id存储到cmspage中
        cmsPage.setHtmlFileId(fileId);
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    /*-------------------------------------------------------*/
    //静态化
    private String generateHtml(String template, Map model) {

        //生成配置类
        Configuration configuration=new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader=new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",template);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板
        try {
            Template  template1 = configuration.getTemplate("template");
            String html=FreeMarkerTemplateUtils.processTemplateIntoString(template1,model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面模板
    private String getTemplateByPageId(String pageId) {
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (optional.isPresent()){

            CmsPage cmsPage = optional.get();
            if (cmsPage==null){
                ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
            }
            //获取页面模板Id
            String templateId = cmsPage.getTemplateId();
            if (StringUtils.isEmpty(templateId)){
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
            }
            Optional<CmsTemplate> optional1 = cmsTemplateRepository.findById(templateId);
            if (optional1.isPresent()){
                CmsTemplate cmsTemplate = optional1.get();
                //获取模板id
                String templateFileId = cmsTemplate.getTemplateFileId();
                //取出模板文件的内容
                GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
                //打开下载流对象
                GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
                //创建GridFsResource
                GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
                try {
                    String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                    return content;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return null;
    }

    //获取页面模型数据
    private Map getModelByPageId(String pageId) {
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            if (cmsPage==null){
                ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
            }
            //取出dataurl
            String dataUrl = cmsPage.getDataUrl();
            if (StringUtils.isEmpty(dataUrl)){
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
            }
            ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
            Map body = forEntity.getBody();
            return body;
        }

        return null;
    }

    //保存页面
    @Override
    public CmsPageResult save(CmsPage cmsPage) {
        //校验页面是否存在，根据页面名称，站点id,页面webpath查询 pageId
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1!=null){
            //跟新
            return this.editPage(cmsPage1.getPageId(),cmsPage);
        }else {
            return this.add(cmsPage);
        }
    }

    @Override
    //一键发布页面
    @Transactional
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //添加页面
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage cmsPage1 = save.getCmsPage();
        //要发布的页面id
        String pageId = cmsPage1.getPageId();
        //发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if (!responseResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面的url
        //页面url=站点域名+站点webpath+页面webpath+页面名称
        //站点id
        String siteId = cmsPage1.getSiteId();
        //查询站点信息
        Optional<CmsSite> optionalSite = cmsSiteRepository.findById(siteId);
        if (!optionalSite.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsSite cmsSite = optionalSite.get();
        //站点域名
        String siteDomain = cmsSite.getSiteDomain();
        //站点webpath
        String siteWebPath = cmsSite.getSiteWebPath();
        //页面web路径
        String pageWebPath = cmsPage1.getPageWebPath();
        //页面名称
        String pageName = cmsPage1.getPageName();
        //页面的web访问地址
        String pageUrl = siteDomain+siteWebPath+pageWebPath+pageName;
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }
}
