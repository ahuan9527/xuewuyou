package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator.
 */
@Mapper
@Repository
public interface CourseMapper {

   CourseBase findCourseBaseById(String id);

   //分页查询 我的课程,使用pagehelper插件
   Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}