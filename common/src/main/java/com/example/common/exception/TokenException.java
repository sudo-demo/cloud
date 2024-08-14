package com.example.common.exception;

import org.apache.commons.lang3.StringUtils;

public class TokenException extends RuntimeException {

    private static final long serialVersionUID = -8387394280899935259L;

    public TokenException(){

    }

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String[] message) {
        super(StringUtils.join(message, ","));
    }
}
