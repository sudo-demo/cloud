package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.system.domain.SystemRole;
import com.example.system.domain.dto.SystemRoleDto;
import com.example.system.service.SystemRoleService;
import com.example.system.mapper.SystemRoleMapper;
import com.example.common.exception.validateException;
import com.example.common.model.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author huanghongjia
 * @description 针对表【system_role(角色信息表)】的数据库操作Service实现
 * @createDate 2023-09-02 22:22:34
 */
@Service
public class SystemRoleServiceImpl extends ServiceImpl<SystemRoleMapper, SystemRole>
        implements SystemRoleService {

    @Autowired
    private SystemRoleMapper mapper;

    @Override
    public IPage<SystemRole> getRolePage(PageDTO pageDTO) {
        Page<SystemRole> page = pageDTO.toPage();
        LambdaQueryWrapper<SystemRole> wrapper = new LambdaQueryWrapper<>();

        IPage<SystemRole> systemRolePage = mapper.selectPage(page, wrapper);
        return systemRolePage;
    }

    @Override
    public SystemRole getInfo(Integer roleId) {
        return mapper.selectById(roleId);
    }

    @Override
    public void created(SystemRoleDto dto) {
        SystemRole roleInfo = mapper.getRoleInfo(dto.getRoleId());
        QueryWrapper<SystemRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("deleted_at");
        List<SystemRole> systemRoles = mapper.selectList(queryWrapper);
        System.out.println(systemRoles);
        SystemRole data = BeanUtil.copyProperties(dto, SystemRole.class);
        if (ObjectUtil.isNull(roleInfo)) {
            mapper.insert(data);
        } else {
            if(roleInfo.getDeletedAt() == null){
                throw new validateException("角色ID存在");
            }else{
                UpdateWrapper<SystemRole> updateWrapper = new UpdateWrapper<>();

                updateWrapper.eq("role_id",data.getRoleId())
                                .set("deleted_at",null);
                mapper.update(data,updateWrapper);
            }
        }

    }

    @Override
    public void updated(SystemRoleDto dto) {
        SystemRole data = BeanUtil.copyProperties(dto, SystemRole.class);
        mapper.updateById(data);
    }

    @Override
    public void remove(Integer roleId) {
        boolean update = lambdaUpdate()
                .eq(SystemRole::getRoleId, roleId)
                .set(SystemRole::getDeletedAt, new Date())
                .update();
    }
}




