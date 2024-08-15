package com.example.system.service;

import com.example.system.domain.SystemUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.domain.VRoleApi;
import com.example.system.domain.dto.SystemUserRoleDto;

import java.util.List;
import java.util.Set;

/**
* 
* {@code @description} 针对表【system_user_role(用户授权角色表)】的数据库操作Service
* @createDate 2023-09-03 16:25:51
*/
public interface SystemUserRoleService extends IService<SystemUserRole> {
    void authorized(SystemUserRoleDto dto);

    List<VRoleApi> cacheRole(Set<Long> roleId);
}
