package com.xuecheng.manage_cms;


import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class PageHtmlTest {

    @Autowired
    PageService pageService;

    @Test
    public void pageHtmlTest(){

        String pageHtml = pageService.getPageHtml("5c570b2f9caab121f0e2f933");
        System.out.println(pageHtml);

    }

}
