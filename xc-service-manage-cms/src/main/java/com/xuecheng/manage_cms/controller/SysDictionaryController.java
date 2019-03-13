package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.course.SysDicthinaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.impl.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SysDictionaryController implements SysDicthinaryControllerApi {
    @Autowired
    SysDictionaryService sysDictionaryService;


    @Override
    @GetMapping("/sys/dictionary/get/{type}")
    public SysDictionary getByType(@PathVariable("type") String type) {
        return sysDictionaryService.getByType(type);
    }
}
