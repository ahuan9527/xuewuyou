package com.xuecheng.api.cms;

import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;

@Api(value="cms站点管理接口",description="cms站点管理接口，提供页面的查")
public interface SiteControllerApi {
    public QueryResponseResult findList();
}
