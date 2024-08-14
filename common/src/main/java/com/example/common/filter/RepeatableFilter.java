package com.example.common.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * Repeatable 过滤器
 * 
 * @author ruoyi
 */
@Component
public class RepeatableFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        System.out.println("filterConfig："+filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            // 使用自定义的包装器包装 HttpServletRequest
            HttpServletRequest wrappedRequest = new RepeatedlyRequestWrapper((HttpServletRequest) request);
            // 将包装后的请求传递给过滤器链的下一个组件
            chain.doFilter(wrappedRequest, response);
        } else {
            // 对非 HTTP 请求执行过滤链
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy()
    {

    }
}
