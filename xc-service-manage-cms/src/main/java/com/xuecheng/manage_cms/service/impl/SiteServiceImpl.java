package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteServiceImpl implements SiteService {
    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Override
    public QueryResponseResult findList() {

        List<CmsSite> all = cmsSiteRepository.findAll();
        QueryResult<CmsSite> siteQueryResult = new QueryResult<>();
        siteQueryResult.setList(all);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,siteQueryResult);
        return queryResponseResult;
    }
}
