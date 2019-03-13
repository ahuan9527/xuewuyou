package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Value("${course-publish.siteId}")
    private String publish_siteId;

    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String publish_previewUrl;

    @Value("${course-publish.pageWebPath}")
    private String publish_pageWebPath;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_pagePhysicalPath;
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;

    @Autowired
    CmsPageClient feignClient;

    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode=teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    //添加课程
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        //校验课程id和课程计划名称
        if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //取出课程id
        String courseid = teachplan.getCourseid();
        //取出父节点id
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            //如果父节点为空则获取根节点
            parentid=getTeachplanRoot(courseid);
        }
        //取出父节点信息
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //父节点
        Teachplan teachplanParent = optional.get();
        //父节点级别
        String grade = teachplanParent.getGrade();
        //设置父节点
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");//未发布
        //子节点级别,根据父节点来判断
        if (grade.equals("1")){
            teachplan.setGrade("2");
        }else if(grade.equals("2")){
            teachplan.setGrade("3");
        }
        //设置课程id
        teachplan.setCourseid(teachplanParent.getCourseid());
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //获取课程根节点没有则添加
    private String getTeachplanRoot(String courseid) {
        //校验课程id
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseid);
        if (!baseOptional.isPresent()){
            return  null;
        }
        CourseBase courseBase = baseOptional.get();
        //取出课程 计划根节点
        List<Teachplan> byCourseidAndParentid = teachplanRepository.findByCourseidAndParentid(courseid, "0");
        if (byCourseidAndParentid==null || byCourseidAndParentid.size() == 0){
            //新增一个根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseid);
            teachplan.setPname(courseBase.getName());
            teachplan.setParentid("0");
            teachplan.setGrade("1");//1级
            teachplan.setStatus("0");//未发布
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        Teachplan teachplan1=byCourseidAndParentid.get(0);
        return teachplan1.getId();
    }

    //查询我的课程
    public QueryResponseResult findCourseList(  int page,int size,CourseListRequest courseListRequest){

        //分页测试 从第一页开始
        if (page<=0){
            page=1;
        }

        if (size<=0){
            size =7;
        }
        //使用pagehelper 插件
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        if (courseListPage == null){
            ExceptionCast.cast(CourseCode.COURSE_LIST_NULL);
        }
        List<CourseInfo> result = courseListPage.getResult();
        //返回结果集
        QueryResult queryResult = new QueryResult();
        queryResult.setList(result);
        queryResult.setTotal(courseListPage.getTotal());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    //添加课程
    public ResponseResult addCourseBase(CourseBase courseBase){
        courseBase.setStatus("202001");
        CourseBase save = courseBaseRepository.save(courseBase);
        if (save == null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //根据id获取课程
    public CourseBase getCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if (!baseOptional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_LIST_NULL);
        }
        CourseBase courseBase = baseOptional.get();
        return courseBase;
    }
    //跟新课程信息
    public ResponseResult updateCourseBase(String id,CourseBase courseBase){
        if (courseBase==null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
       courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //添加课程图片
    @Transactional
    public  ResponseResult saveCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()){
            coursePic = optional.get();
        }
        //没有课程图片则新建对象
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);

    }
    //根据id查询课程图片
    public CoursePic findCoursepic(String courseId){
        return coursePicRepository.findById(courseId).get();
    }
    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result=coursePicRepository.deleteCoursePicByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CourseView getCourseView(String id){
        CourseView courseView = new CourseView();

        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_LIST_NULL);
        }
        CourseBase courseBase = optional.get();
        courseView.setCourseBase(courseBase);
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(picOptional.get());
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }
    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> byId = courseBaseRepository.findById(courseId);
        if (byId.isPresent()){
            CourseBase courseBase = byId.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        return null;
    }

    //课程预览
    public CoursePublishResult preview(String id){
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);
        //模板id
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id+".html");
        //页面别名 == 课程名称
        cmsPage.setPageAliase(courseBaseById.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_pageWebPath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_pagePhysicalPath);

        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+id);

        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = feignClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String pageUrl = publish_previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String courseId){
        //课程信息
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        //发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(courseId);

        if (cmsPostPageResult==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //更新课程状态
        Optional<CourseBase> byId = courseBaseRepository.findById(courseId);
        if (!byId.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CourseBase courseBase = byId.get();
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        //创建课程索引信息
        CoursePub coursePub = createCoursePub(courseId);
        //向数据库保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(courseId, coursePub);
        if (newCoursePub==null){
            ExceptionCast.cast(CourseCode.COURSE_PUB_NULL);
        }
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }
    //保存CoursePub
    private CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
        if (coursePubOptional.isPresent()){
            BeanUtils.copyProperties(coursePubOptional.get(),coursePubNew);
        }else{
            coursePubNew = new CoursePub();
            BeanUtils.copyProperties(coursePub,coursePubNew);
        }
        //设置主键
        coursePubNew.setId(courseId);
        //设置logstach时间戳
        coursePub.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePub.setPubTime(date);
        CoursePub save = coursePubRepository.save(coursePubNew);
        return save;

    }

    //创建CoursePub
    private CoursePub createCoursePub(String courseId) {
        CoursePub coursePub = new CoursePub();

        coursePub.setId(courseId);

        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        if (coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(courseId);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);

        return coursePub;

    }

    //发布课程详情页面
    private CmsPostPageResult publish_page(String courseId) {
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);
        //模板id
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名 == 课程名称
        cmsPage.setPageAliase(courseBaseById.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_pageWebPath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_pagePhysicalPath);

        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        CmsPostPageResult cmsPostPageResult = feignClient.postPageQuick(cmsPage);
        return  cmsPostPageResult;
    }
}
