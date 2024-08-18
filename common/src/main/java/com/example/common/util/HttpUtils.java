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
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取request参数 json
     */
    public static String getRequestParam() {
        RepeatedlyRequestWrapper request = (RepeatedlyRequestWrapper) getRequest();
        if (request == null) {
            return null;
        }
        return request.getRequestParam();
    }
}
