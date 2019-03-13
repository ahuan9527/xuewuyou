package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MangeRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void test01(){
        //分页测试
        Pageable pageable = PageRequest.of(0,10);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);

        System.out.println("_____________________________"+all.getTotalPages());
    }

    //修改
    @Test
    public void test02(){
        Optional<CmsPage> byId = cmsPageRepository.findById("5a92141cb00ffc5a448ff1a0");
        if (byId.isPresent()){
            CmsPage cmsPage = byId.get();
            cmsPage.setPageAliase("测试修改！");
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }
    //自定义
    @Test
    public void test03(){
//        CmsPage byPageName = cmsPageRepository.findByPageName("10101.html");
//        System.out.println(byPageName);
    }

    //自定义条件查询
    @Test
    public void test04(){
        //分页测试
        Pageable pageable = PageRequest.of(0,10);
        CmsPage cmsPage = new CmsPage();

        cmsPage.setPageAliase("轮播");
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains() );

        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        System.out.println(all);
    }

    @Test
    public void test05(){
        ResponseEntity<java.util.Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/297e7c7c62b888f00162b8a7dec20000", java.util.Map.class);

        Map body = forEntity.getBody();
        System.out.println(body);
    }
}
