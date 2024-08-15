package com.example.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.system.domain.SystemRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.dto.SystemRoleDto;
import com.example.common.model.PageDTO;

/**
* 
* @description 针对表【system_role(角色信息表)】的数据库操作Service
* @createDate 2023-09-02 22:22:34
*/
public interface SystemRoleService extends IService<SystemRole> {

    IPage<SystemRole> getRolePage(PageDTO pageDTO);

    SystemRole getInfo(Integer roleId);

    void created(SystemRoleDto dto);

    void updated(SystemRoleDto dto);

    void remove(Integer roleId);

}
