package com.example.common.exception;

/**
 * 动态代理相关异常
 */
public class ProxyException extends RuntimeException {

    private static final long serialVersionUID = -4315821076066641243L;

    public ProxyException(String message) {
        super(message);
    }

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}