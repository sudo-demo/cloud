package com.example.common.enums;

import com.example.common.model.Result;
import lombok.Getter;

@Getter
public enum ResultEnum {

    SUCCESS(200, "操作成功"),

    EMPTY_RESULT(300, "查询结果为空"),

    PARAMETER_ERR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权,无权访问"),
    FORBIDDEN(403, "无权限访问资源"),
    NOT_FOUND(404, "资源，服务未找到"),

    ERROR(500, "操作失败,请稍后再试!"),

    DATA_REPEAT_ERR(600, "文件上传失败,数据重复"),
    ID_EMPTY(601, "编辑时主键ID不能为空!"),

    USER_LOGIN_SUCCESS(200,"登录成功！"),
    CAPTCHA_NOT_FOUND(1001, "验证码不存在！"),
    CAPTCHA_NO_MATCH(1002, "验证码不正确！"),
    CAPTCHA_TIMEOUT(1003, "验证码已过期！"),
    USER_NAME_ERR(1004, "登录账号错误！"),
    PASSWORD_ERR(1005, "登录密码错误！"),
    CLIENT_ERR(1006, "client证书有误！"),
    USER_NOT_FOUND_ERR(1007, "未找到系统用户信息！"),
    CLIENT_SCOPE_ERR(1008, "scope不合法"),
    USER_DISABLED_ERR(1009, "账户已被停用，请联系管理员启用！"),
    ROLE_NOT_FOUND_ERR(1016,"未找到登录账号角色信息，请联系管理员！"),
    ROLE_DISABLED_ERR(1017,"登录账号角色已被停用，请联系管理员！"),

    // feign
    REQUEST_TIMEOUT(1101,"服务超时,降级处理!"),

    // 文件相关
    FILE_TYPE_ERROR(1202,"文件类型有误"),

    ;

    /**
     * 操作返回值编号
     */
    private final int code;

    /**
     *操作返回值描述
     */
    private final String message;

    ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 转换成返回结果类
     */
    public <T> Result<T> toResp() {
        return Result.of(this);
    }
}
