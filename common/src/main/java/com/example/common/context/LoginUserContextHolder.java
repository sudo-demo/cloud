package com.example.common.context;

import com.example.common.domain.LoginUser;

/**
 * 登录用户上下文持有类
 * 基于ThreadLocal实现，用于在当前请求线程中存储和获取登录用户信息
 * 解决多线程环境下用户信息传递问题，保证线程隔离性
 */
public class LoginUserContextHolder {

    /**
     * ThreadLocal存储容器，每个线程独立拥有一份数据副本
     * 泛型指定为LoginUser，明确存储类型
     */
    private static final ThreadLocal<LoginUser> LOGIN_USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的登录用户信息
     * @param loginUser 登录用户对象，包含用户基本信息和权限等
     */
    public static void setLoginUser(LoginUser loginUser) {
        LOGIN_USER_HOLDER.set(loginUser);
    }

    /**
     * 获取当前线程的登录用户信息
     * @return 登录用户对象，未登录时返回null
     */
    public static LoginUser getLoginUser() {
        return LOGIN_USER_HOLDER.get();
    }

    /**
     * 清除当前线程的登录用户信息
     * 必须在请求处理完成后调用，防止ThreadLocal内存泄漏
     * 特别是在使用线程池的环境下，线程会被复用，不清除会导致信息残留
     */
    public static void clear() {
        LOGIN_USER_HOLDER.remove();
    }

    /**
     * 判断当前线程是否存在登录用户信息
     * @return true：已登录；false：未登录
     */
    public static boolean hasLoginUser() {
        return LOGIN_USER_HOLDER.get() != null;
    }


}