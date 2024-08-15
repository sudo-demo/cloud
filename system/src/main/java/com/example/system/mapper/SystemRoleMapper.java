package com.example.system.mapper;

import com.example.system.domain.SystemRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* 
* @description 针对表【system_role(角色信息表)】的数据库操作Mapper
* @createDate 2023-09-02 22:29:40
* @Entity com.example.base.domain.SystemRole
*/
public interface SystemRoleMapper extends BaseMapper<SystemRole> {

    @Select("select * from system_role where role_id=#{roleId}")
    SystemRole getRoleInfo(Long roleId);

}




