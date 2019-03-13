package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseMarketService;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {
    @Autowired
    CourseService courseService;

    @Autowired
    CourseMarketService courseMarketService;

    //查询课程计划
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId){
        return courseService.findTeachplanList(courseId);
    }

    //添加课程计划
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan){
        return courseService.addTeachplan(teachplan);
    }

    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult findCourseList(@PathVariable("page") int page,@PathVariable("size") int size, CourseListRequest courseListRequest) {
        System.out.println("page:"+page+",size:"+size);
        return courseService.findCourseList(page,size,courseListRequest);
    }

    //添加我的课程
    @Override
    @PostMapping("/coursebase/add")
    public ResponseResult addCourseBase(@RequestBody  CourseBase courseBase) {

        return courseService.addCourseBase(courseBase);
    }

    @Override
    @GetMapping("/coursbaseview/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseBaseById(courseId);
    }

    @Override
    @PutMapping("/coursbaseEdit/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id,@RequestBody CourseBase courseBase) {

        return courseService.updateCourseBase(id,courseBase);
    }

    @Override
    @GetMapping("/findcoursemarket/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseMarketService.getCourseMarketById(courseId);
    }

    @Override
    @PutMapping("/coursmarketedit/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id,@RequestBody CourseMarket courseMarket) {
        return courseMarketService.updateCourseMarket(id,courseMarket);
    }

    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId,@RequestParam("pic") String pic) {
        return courseService.saveCoursePic(courseId,pic);
    }

    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursepic(courseId);
    }

    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCourseView(id);
    }

    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable String id) {
        return courseService.publish(id);
    }
}
