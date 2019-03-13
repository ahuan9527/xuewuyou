package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryService {
    @Autowired
    SysDictionaryRepository dictionaryRepository;

    public SysDictionary getByType(String type){

        SysDictionary sysDictionaryByDType = dictionaryRepository.findSysDictionaryByDType(type);
        if (sysDictionaryByDType == null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return sysDictionaryByDType;
    }
}
