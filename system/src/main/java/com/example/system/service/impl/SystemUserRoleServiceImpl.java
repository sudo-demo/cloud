package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.exception.validateException;
import com.example.system.domain.SystemUserRole;
import com.example.common.domain.VRoleApi;
import com.example.system.domain.dto.RoleListDto;
import com.example.system.domain.dto.SystemUserRoleDto;
import com.example.system.domain.dto.SystemUserRoleScope;
import com.example.system.mapper.SystemUserRoleMapper;
import com.example.system.mapper.SystemUserRoleScopeMapper;
import com.example.system.service.SystemRoleApiService;
import com.example.system.service.SystemUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.common.util.RedisUtil;
import static java.util.stream.Collectors.groupingBy;

/**
 * 
 * @description 针对表【system_user_role(用户授权角色表)】的数据库操作Service实现
 * @createDate 2023-09-03 16:25:51
 */
@Service
public class SystemUserRoleServiceImpl extends ServiceImpl<SystemUserRoleMapper, SystemUserRole>
        implements SystemUserRoleService {

    @Resource
    private SystemUserRoleMapper mapper;

    @Resource
    private SystemUserRoleScopeMapper systemUserRoleScopeMapper;

    @Resource
    private SystemRoleApiService systemRoleApiService;

    @Override
    @Transactional()
    public void authorized(SystemUserRoleDto dto) {

        List<SystemUserRole> list = lambdaQuery()
                .eq(SystemUserRole::getUserId, dto.getUserId())
                .select(SystemUserRole::getUserRoleId)
                .list();
        Set<Long> userRoleIdSet = list.stream().map(SystemUserRole::getUserRoleId).collect(Collectors.toSet());
        LambdaQueryWrapper<SystemUserRoleScope> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SystemUserRoleScope::getUserRoleId, userRoleIdSet);
        systemUserRoleScopeMapper.delete(lambdaQueryWrapper);

        LambdaQueryWrapper<SystemUserRole> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(SystemUserRole::getUserId, dto.getUserId());
        mapper.delete(lambdaQueryWrapper2);

        Map<Long, List<RoleListDto>> collect = dto.getRoleList().stream().collect(groupingBy(RoleListDto::getRoleId));
        collect.forEach((roleId, roleListDtoList) -> {
            SystemUserRole systemUserRole1 = new SystemUserRole();
            systemUserRole1.setUserId(dto.getUserId());
            systemUserRole1.setRoleId(roleId);
            mapper.insert(systemUserRole1);
            System.out.println(systemUserRole1.getUserRoleId());
            SystemUserRoleScope systemUserRoleScope1 = new SystemUserRoleScope();
            roleListDtoList.forEach(college -> {
                systemUserRoleScope1.setCollegeId(college.getCollegeId());
                systemUserRoleScope1.setUserRoleId(systemUserRole1.getUserRoleId());
                systemUserRoleScopeMapper.insert(systemUserRoleScope1);
            });
        });

    }

    @Override
    public List<VRoleApi> cacheRole(Set<Long> roleId) {
        roleId.forEach(id-> {
            List<VRoleApi> roleApi = systemRoleApiService.getRoleApi(id);
            RedisUtil.del("role_"+id);
            if(roleApi.isEmpty()){
                return;
            }
            List<VRoleApi> vRoleApis = buildTree(roleApi, 0L);
            vRoleApis.forEach(item->{
                if(!RedisUtil.hset("role_"+id,item.getAppModel(),item)){
                    throw new validateException("操作redis失败");
                }
            });
        });
        return null;
    }


    public List<VRoleApi> buildTree(List<VRoleApi> roleApi, Long pid) {
        List<VRoleApi> rootNode = new ArrayList<>();
        for (VRoleApi vRoleApi : roleApi) {
            if (vRoleApi.getParentApiId() == pid) {
                List<VRoleApi> vRoleApis = buildTree(roleApi, vRoleApi.getApiId());
                for (VRoleApi v : vRoleApis) {
                    vRoleApi.getChildren().put(v.getAction(), v);
                }
                rootNode.add(vRoleApi);
            }
        }
        return rootNode;
    }
}




