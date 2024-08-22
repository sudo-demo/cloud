package com.example.common.util;


import com.example.common.filter.RepeatedlyRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 通用http工具封装
 *
 * @author ruoyi
 */
@Component
public class HttpUtils {

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new RuntimeException("request is null");
//            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取request参数 json
     */
    public static String getRequestParam() {
        HttpServletRequest request = getRequest();
        if (request instanceof RepeatedlyRequestWrapper) {
            return ((RepeatedlyRequestWrapper) request).getRequestParam();
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     *
     * @return 返回客户端IP地址
     */
    public static String getIpAddress() {
        // 获取HttpServletRequest对象
        HttpServletRequest request = getRequest();

        // 从请求头中获取"X-Forwarded-For"的值作为IP地址
        String ipAddress = request.getHeader("X-Forwarded-For");

        // 如果IP地址为空或者为"unknown"，则继续判断其他头部信息
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            // 从请求头中获取"Proxy-Client-IP"的值作为IP地址
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        // 如果IP地址仍然为空或者为"unknown"，则继续判断其他头部信息
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            // 从请求头中获取"WL-Proxy-Client-IP"的值作为IP地址
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        // 如果IP地址仍然为空或者为"unknown"，则使用请求对象的getRemoteAddr()方法获取IP地址
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 返回IP地址
        return ipAddress;
    }

}
