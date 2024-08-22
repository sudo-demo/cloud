package com.example.common.enums;

import lombok.Getter;

@Getter
public enum FileType {

    ELECTRONIC_INVOICE(1, "电子发票", new String[]{"application/pdf", "image/png"}),
    BUSINESS_CLAIM_FORM(2,"业务报销单", new String[]{"application/pdf"})
    ;

    private final Integer code;
    private final String message;
    private final String[] contentType;

    FileType(Integer code, String message, String[] contentType) {
        this.code = code;
        this.message = message;
        this.contentType = contentType;
    }

}
