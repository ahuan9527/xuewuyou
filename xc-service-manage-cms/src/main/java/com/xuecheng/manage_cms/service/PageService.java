package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface PageService {
    //分页查询
    public QueryResponseResult findList(int page,int size,QueryPageRequest queryPageRequest);
    //添加
    public CmsPageResult add(CmsPage cmsPage);

    //根据id查询页面
    public CmsPage findPageById(String id);
    //修改页面
    public CmsPageResult editPage(String id,CmsPage cmsPage);
    //删除页面
    public ResponseResult delPage(String id);

    /**
     * 静态化程序获取页面的DataUrl
     * 静态化程序远程请求DataUrl获取数据模型。
     * 静态化程序获取页面的模板信息
     * 页面静态化
     */
    public String getPageHtml(String pageId);

    //页面发布
    public ResponseResult postPage(String pageId);

    //保存页面
    public CmsPageResult save(CmsPage cmsPage);

    //一键发布页面
    CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
