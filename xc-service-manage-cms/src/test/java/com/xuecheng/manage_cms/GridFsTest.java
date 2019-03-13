package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {
    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    //存文件
    @Test
    public void test01() throws FileNotFoundException {
        //要存储的文件
        File file=new File("f:/course.ftl");
            //定义输入流
        FileInputStream inputStram= new FileInputStream(file);
            //向GridFS存储文件
        ObjectId objectId = gridFsTemplate.store(inputStram,"课程详情模板","");
            //得到文件ID
        String fileId=objectId.toString();
        System.out.println(fileId);
    }
    //取文件
    @Test
    public void test02() throws IOException {
        String fileId="5c5703b39caab114dcfd45cc";
        //根据id查询文件
        GridFSFile gridFSFile=
                gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream=
                gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
//创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource=new GridFsResource(gridFSFile,gridFSDownloadStream);
//获取流中的数据
        String s =IOUtils.toString(gridFsResource.getInputStream(),"UTF‐8");
        System.out.println(s);

    }
}
