package com.xuecheng.api.filesystem;


import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value="图片地址管理",description="提供图片地址管理、查询功能")
public interface FileSystemControllerApi {

    /**
     *上传文件
     *@parammultipartFile文件
     *@paramfiletag文件标签
     *@parambusinesskey业务key
     *@parammetedata元信息,json格式
     *@return
     */
    @ApiOperation(value="上传图片")
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata);

}
