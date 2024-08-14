package com.example.system.service.impl;

import com.example.system.service.PermissionService;
import com.example.common.domain.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service("PermissionService")
public class PermissionServiceImpl implements PermissionService {
    @Override
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        System.out.println("主体principal："+principal);
        return true;
    }
}
