package com.example.system.service;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * RBAC权限模型示例，Service类
 */
public interface PermissionService {

    /**
     * 定义一个方法，根据请求对象，判断其是否有权限访问某些资源
     *
     * @param request        请求信息
     * @param authentication 登录用户信息
     * @return
     */
    boolean hasPermission(HttpServletRequest request, Authentication authentication);

}
