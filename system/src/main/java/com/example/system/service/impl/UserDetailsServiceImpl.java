package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.system.domain.SystemUserRole;
import com.example.system.service.SystemUserRoleService;
import com.example.system.service.SystemUserService;
import com.example.common.domain.LoginUser;
import com.example.common.domain.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.system.domain.SystemUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在Spring Security的整个认证流程中会调用会调用UserDetailsService中的loadUserByUsername方法根据用户名称查询用户数据。默认情况下调用的是
 * InMemoryUserDetailsManager中的方法，该UserDetailsService是从内存中获取用户的数据。现在我们需要从数据库中获取用户的数据，那么此时就需要自定义一个
 * UserDetailsService来覆盖默认的配置。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    @Lazy
    private SystemUserService systemUserService;

    @Resource
    private SystemUserRoleService systemUserRoleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUser systemUser = systemUserService.lambdaQuery()
                .eq(SystemUser::getLoginId, username).one();
        if(ObjectUtil.isNull(systemUser)){
            throw new UsernameNotFoundException("用户不存在");
        }
        List<SystemUserRole> list = systemUserRoleService.lambdaQuery().eq(SystemUserRole::getUserId, systemUser.getUserId()).list();
        Set<Long> roleIds = list.stream().map(SystemUserRole::getRoleId).collect(Collectors.toSet());
        Long firstRoleId = roleIds.stream().findFirst().orElseThrow(() -> new NoSuchElementException("没有角色 ID"));
        User user = BeanUtil.copyProperties(systemUser, User.class);
        user.setRoleIds(roleIds);
        user.setRoleId(firstRoleId);
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);

        //        //手动设置了权限及角色，也可以通过数据库查询获取
//        List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList("addUser,findAll,ADMIN,USER");  //配置权限及角色
        return loginUser;
    }
}
