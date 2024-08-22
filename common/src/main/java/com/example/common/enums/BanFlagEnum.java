package com.example.common.enums;

import lombok.Getter;

@Getter
public enum BanFlagEnum {

    NORMAL(1,"正常"),
    BAN(2,"禁用");

    private final int code;
    private final String message;

    BanFlagEnum(int code, String message)
    {
        this.code = code;
        this.message = message;
    }

}
