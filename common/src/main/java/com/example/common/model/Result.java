package com.example.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.example.common.enums.ResultEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 统一返回结果类
 */
@ApiModel("统一返回结果类")
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("状态码")
    private int code; // 状态码

    @ApiModelProperty("提示信息")
    private String message; // 提示信息

    @ApiModelProperty("返回结果")
    private T data; // 返回结果

    /**
     * 是否成功
     *
     * @return true 如果状态码为成功
     */
    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isOk() {
        return code == ResultEnum.SUCCESS.getCode();
    }

    /**
     * 是否失败
     *
     * @return true 如果状态码不为成功
     */
    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isErr() {
        return code != ResultEnum.SUCCESS.getCode();
    }

    /**
     * 私有构造函数
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    返回结果
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 根据结果枚举和数据创建结果对象
     *
     * @param resultEnum 结果枚举
     * @param data       返回数据
     * @return Result<T> 结果对象
     */
    private static <T> Result<T> of(ResultEnum resultEnum, T data) {
        return new Result<>(resultEnum.getCode(), resultEnum.getMessage(), data);
    }

    /**
     * 根据结果枚举创建结果对象
     *
     * @param resultEnum 结果枚举
     * @return Result<T> 结果对象
     */
    public static <T> Result<T> of(ResultEnum resultEnum) {
        return of(resultEnum, null);
    }

    /**
     * 返回成功消息
     *
     * @return Result<T> 成功消息
     */
    public static <T> Result<T> success() {
        return of(ResultEnum.SUCCESS);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回消息
     * @return Result<T> 成功消息
     */
    public static <T> Result<T> msg(String msg) {
        return new Result<>(ResultEnum.SUCCESS.getCode(), msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param data 返回内容
     * @return Result<T> 成功消息
     */
    public static <T> Result<T> success(T data) {
        return of(ResultEnum.SUCCESS, data);
    }

    /**
     * 返回成功消息
     *
     * @param data 返回内容
     * @param code 返回状态码
     * @return Result<T> 成功消息
     */
    public static <T> Result<T> success(T data, int code) {
        return new Result<>(code, ResultEnum.SUCCESS.getMessage(), data);
    }

    /**
     * 返回错误消息
     *
     * @return Result<T> 错误消息
     */
    public static <T> Result<T> error() {
        return of(ResultEnum.ERROR);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return Result<T> 警告消息
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultEnum.ERROR.getCode(), msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param code 返回状态码
     * @param msg  返回内容
     * @return Result<T> 警告消息
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 返回错误消息：参数错误异常
     *
     * @param msg 返回内容
     * @return Result<T> 警告消息
     */
    public static <T> Result<T> paramError(String msg) {
        return new Result<>(ResultEnum.PARAMETER_ERR.getCode(), msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param resultEnum 返回结果枚举
     * @return Result<T> 警告消息
     */
    public static <T> Result<T> error(ResultEnum resultEnum) {
        return of(resultEnum);
    }

    /**
     * 默认降级结果
     *
     * @return Result<T> 默认错误消息
     */
    public static <T> Result<T> defaultFb() {
        return Result.error(ResultEnum.REQUEST_TIMEOUT);
    }
}
