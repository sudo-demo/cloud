package com.example.system.service;

import com.example.system.domain.SystemRoleApi;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.domain.VRoleApi;

import java.util.List;

/**
* @author huanghongjia
* @description 针对表【system_role_api(角色与接口关联表(权限))】的数据库操作Service
* @createDate 2023-09-03 16:22:04
*/
public interface SystemRoleApiService extends IService<SystemRoleApi> {

    List<VRoleApi> getRoleApi(Long roleId);

}
