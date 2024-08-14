package com.example.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.BaseEntity;
import com.example.common.model.PageDTO;
import com.example.system.domain.SystemUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author huanghongjia
* @description 针对表【system_user(用户信息表)】的数据库操作Mapper
* @createDate 2023-09-01 10:40:37
* @Entity com.example.base.domain.SystemUser
*/
public interface SystemUserMapper extends BaseMapper<SystemUser> {

    IPage<SystemUser> getUserPage(@Param("page") Page<SystemUser> page, @Param("baseEntity") BaseEntity baseEntity);


}




