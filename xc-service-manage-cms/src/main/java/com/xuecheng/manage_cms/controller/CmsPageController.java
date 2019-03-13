package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    PageService pageService;


    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page")int page, @PathVariable("size")int size, QueryPageRequest queryPageRequest){
      /*  //暂时采用测试数据，测试接口是否可以正常运行
        QueryResult queryResult=new QueryResult();
        queryResult.setTotal(2);
//静态数据列表
        List list=new ArrayList();
        CmsPage cmsPage=new CmsPage();
        cmsPage.setPageName("测试页面");
        list.add(cmsPage);
        queryResult.setList(list);
        QueryResponseResult queryResponseResult=new
                QueryResponseResult(CommonCode.SUCCESS,queryResult);*/
        return pageService.findList(page,size,queryPageRequest);
    }

    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return pageService.add(cmsPage);
    }

    @Override
    @GetMapping("/get/{id}")
    public CmsPage findPageById(@PathVariable("id") String id) {
        return pageService.findPageById(id);
    }

    @Override
    @PutMapping("/edit/{id}")
    public CmsPageResult editPage(@PathVariable("id") String id,@RequestBody CmsPage cmsPage) {
        return pageService.editPage(id,cmsPage);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delPage(@PathVariable("id") String id) {
        return pageService.delPage(id);
    }

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId")String pageId){
        return pageService.postPage(pageId);
    }

    @Override
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return pageService.save(cmsPage);
    }

    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return pageService.postPageQuick(cmsPage);
    }


}
