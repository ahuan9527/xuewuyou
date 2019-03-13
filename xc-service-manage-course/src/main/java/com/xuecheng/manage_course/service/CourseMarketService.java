package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.CourseMarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CourseMarketService {
    @Autowired
    CourseMarketRepository courseMarketRepository;

    //查询课程营销信息
    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> byId = courseMarketRepository.findById(courseId);
        if (!byId.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_LIST_NULL);
        }
        CourseMarket courseMarket = byId.get();
        return courseMarket;
    }
    //更新课程营销信息
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket){
        if (courseMarket == null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        courseMarketRepository.save(courseMarket);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
