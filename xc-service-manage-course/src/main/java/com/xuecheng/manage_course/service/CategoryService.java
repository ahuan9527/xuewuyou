package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import com.xuecheng.manage_course.dao.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    CategoryRepository categoryRepository;

    //查询课程分类
    public CategoryNode findList(){
        CategoryNode categoryNode = categoryMapper.selectList();
        return categoryNode;
    }
}
