package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    //将Html文件保存到物理路径
    public void  savePageToServerPath(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        CmsPage cmsPage=optional.get();
        //页面所属站点
        CmsSite cmsSite=this.getCmsSiteById(cmsPage.getSiteId());
        //页面物理路径
        String pagePath= (cmsSite.getSitePhysicalPath()==null?"":cmsSite.getSitePhysicalPath())+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();
        //查询页面静态文件
        String htmlFileId = cmsPage.getHtmlFileId();
        InputStream inputStream=this.getFileById(htmlFileId);
        if (inputStream == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        FileOutputStream fileOutputStream=null;
        try{
            fileOutputStream=new FileOutputStream(new File(pagePath));
        //将文件内容保存到服务物理路径
            IOUtils.copy(inputStream,fileOutputStream);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //获取页面
    private InputStream getFileById(String htmlFileId) {
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据站点id得到站点
    private CmsSite getCmsSiteById(String siteId) {
        Optional<CmsSite>optional=cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            CmsSite cmsSite=optional.get();
            return cmsSite;
        }
        return null;
    }
}
