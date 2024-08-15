package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.system.domain.SystemRoleApi;
import com.example.common.domain.VRoleApi;
import com.example.system.mapper.VRoleApiMapper;
import com.example.system.service.SystemRoleApiService;
import com.example.system.mapper.SystemRoleApiMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 
 * @description 针对表【system_role_api(角色与接口关联表(权限))】的数据库操作Service实现
 * @createDate 2023-09-03 16:22:04
 */
@Service
public class SystemRoleApiServiceImpl extends ServiceImpl<SystemRoleApiMapper, SystemRoleApi>
        implements SystemRoleApiService {

    @Resource
    private SystemRoleApiMapper systemRoleApiMapper;

    @Resource
    private VRoleApiMapper vRoleApiMapper;

    @Override
    public List<VRoleApi> getRoleApi(Long roleId) {
        LambdaQueryWrapper<VRoleApi> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(VRoleApi::getRoleId, roleId);
        return vRoleApiMapper.selectList(objectLambdaQueryWrapper);
    }
}




