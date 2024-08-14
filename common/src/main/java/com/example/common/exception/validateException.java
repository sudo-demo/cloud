package com.example.common.exception;

import org.apache.commons.lang3.StringUtils;

public class validateException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public validateException(String message) {
        super(message);
    }

    public validateException(String[] message) {
        super(StringUtils.join(message, ","));
    }
}
