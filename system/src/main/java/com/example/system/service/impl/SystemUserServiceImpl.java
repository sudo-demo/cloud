package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.annotation.DataScope;
import com.example.common.model.BaseEntity;
import com.example.common.util.JwtUtil;
import com.example.system.domain.SystemUser;
import com.example.system.domain.dto.LoginBody;
import com.example.system.domain.dto.SystemUserDto;
import com.example.system.service.SystemUserService;
import com.example.system.mapper.SystemUserMapper;
import com.example.common.domain.LoginUser;
import com.example.common.exception.validateException;
import com.example.common.model.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @description 针对表【system_user(用户信息表)】的数据库操作Service实现
 * @createDate 2023-09-01 10:40:37
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @DataScope(clazz = SystemUserServiceImpl.class, callMethod = "setUserDataScope")
//    @DataScope()
    @Override
    public IPage<SystemUser> getUserPage(PageDTO pageDTO) {
        Page<SystemUser> page = pageDTO.toPage();
//        pageDTO.setSql("@DataScope");
        BaseEntity baseEntity = new BaseEntity();
        IPage<SystemUser> systemUserPage = baseMapper.getUserPage(page,baseEntity);
//        LambdaQueryWrapper<SystemUser> wrapper = new LambdaQueryWrapper<>();
//
//        IPage<SystemUser> systemUserPage = systemUserMapper.selectPage(page, wrapper);
        return systemUserPage;
    }

    public String setUserDataScope(){
        return "2 = 1";
    }

    @Override
    public SystemUser getInfo(Integer userId) {
        return systemUserMapper.selectById(userId);
    }

    @Override
    public void created(SystemUserDto systemUserDto) {
        boolean exists = lambdaQuery()
                .select(SystemUser::getUserName)
                .eq(SystemUser::getLoginId, systemUserDto.getLoginId())
                .exists();
        if (exists) {
            throw new validateException("登录账号已存在");
        }
        systemUserDto.setUserId(null);
        SystemUser systemUser = BeanUtil.copyProperties(systemUserDto, SystemUser.class);
        systemUserMapper.insert(systemUser);
    }

    @Override
    public void updated(SystemUserDto systemUserDto) {
        systemUserDto.setLoginId(null);
        SystemUser systemUser = BeanUtil.copyProperties(systemUserDto, SystemUser.class);
        systemUserMapper.updateById(systemUser);
    }

    @Override
    public void remove(Integer userId) {
        systemUserMapper.deleteById(userId);
    }

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public String login(LoginBody loginBody) {
        // 创建Authentication对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginBody.getLoginId(), loginBody.getPassword());
        // 调用AuthenticationManager的authenticate方法进行认证
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if(authentication == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        return jwtUtil.createToken(loginUser.getUser());
    }

    public void getAct(String data){
        System.out.printf("到了："+data);
    }
}




