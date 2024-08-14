package com.example.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.annotation.DataScope;
import com.example.system.domain.SystemUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.dto.LoginBody;
import com.example.system.domain.dto.SystemUserDto;
import com.example.common.model.PageDTO;


/**
* @author huanghongjia
* @description 针对表【system_user(用户信息表)】的数据库操作Service
* @createDate 2023-09-01 10:40:37
*/
public interface SystemUserService extends IService<SystemUser> {

    IPage<SystemUser> getUserPage(PageDTO pageDTO);

    SystemUser getInfo(Integer userId);

    void created(SystemUserDto systemUserDto);

    void updated(SystemUserDto systemUserDto);

    void remove(Integer userId);

    String login(LoginBody loginBody);

    void getAct(String data);

}
