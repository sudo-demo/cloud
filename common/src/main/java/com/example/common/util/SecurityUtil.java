package com.example.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.domain.LoginUser;
import com.example.common.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public class SecurityUtil {

    public static User getLoginUser(HttpServletRequest request)
    {
        Object user = RedisUtil.get(JwtUtil.getTokenKey(request)); // 从Redis获取用户信息
        if (ObjectUtil.isNull(user)) {
            return null; // 如果用户信息为空，返回null
        }
        return BeanUtil.copyProperties(user, User.class);
//        if (ObjectUtil.isNull(user)) {
//            return null; // 如果用户信息为空，返回null
//        }
//        return JSON.parseObject(user.toString(), User.class); // 解析并返回用户对象
    }

    /**
     * 获取当前用户
     */
    public static User getUser() {
        // 先从上下文获取用户
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            LoginUser loginUser = (LoginUser) authentication.getPrincipal(); // 获取当前登录用户
            return loginUser.getUser(); // 返回用户对象
        } catch (Exception exception) {
            // 不做处理
        }
        HttpServletRequest request = HttpUtils.getRequest();
        return ObjectUtil.isNull(getLoginUser(request)) ? new User() : getLoginUser(request); // 如果获取失败，返回一个新的User对象
    }


    /**
     * 获取用户所有的角色ID集合
     */
    public static Set<Long> getRoleIds() {
        User user = getUser(); // 获取当前用户
        return user.getRoleIds(); // 返回用户的角色ID集合
    }

    /**
     * 获取用户当前的角色ID
     */
    public static Long getRoleId(){
        User user = getUser(); // 获取当前用户
        return user.getRoleId(); // 返回用户的角色ID
    }

    /**
     * 获取当前的用户ID
     */
    public static Long getUserId(){
        return getUser().getUserId();
    }

    /**
     * 获取当前的用户名称
     */
    public static String getUserName(){
        return getUser().getUserName();
    }
}
