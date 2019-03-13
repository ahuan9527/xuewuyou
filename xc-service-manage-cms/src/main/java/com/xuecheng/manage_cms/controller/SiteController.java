package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.SiteControllerApi;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_cms.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteController implements SiteControllerApi {
    @Autowired
    private SiteService siteService;

    //查询所有站点
    @GetMapping("/cms/site/list")
    public QueryResponseResult findList(){
        return siteService.findList();
    }
}
