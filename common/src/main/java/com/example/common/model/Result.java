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
 * 
 */
@ApiModel("统一返回结果类")
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("状态码")
    private int code;

    @ApiModelProperty("提示信息")
    private String message;

    @ApiModelProperty("返回结果")
    private T data;

    /**
     * 是否成功
     *
     * @return
     */
    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isOk() {
        return code == ResultEnum.SUCCESS.getCode();
    }

    /**
     * 是否失败
     *
     * @return
     */
    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isErr() {
        return code != ResultEnum.SUCCESS.getCode();
    }

//    /**
//     * 转换对象
//     *
//     * @return
//     */
//    public <E> Result<E> convertT(Class<E> clazz) {
//        E e = Func.copyProperties(data, clazz);
//        return Result.success(e);
//    }

    private static <T> Result<T> of(ResultEnum resultEnum, T data) {
        return new Result()
                .setCode(resultEnum.getCode())
                .setMessage(resultEnum.getMessage())
                .setData(data);
    }

    /**
     * 返回成功消息
     *
     * @param resultEnum 返回结果枚举
     * @return 成功消息
     */
    public static <T> Result<T> of(ResultEnum resultEnum) {
        return of(resultEnum, null);
    }


    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static <T> Result<T> success() {
        return of(ResultEnum.SUCCESS);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回消息
     * @return 成功消息
     */
    public static <T> Result<T> msg(String msg) {
        return new Result()
                .setCode(ResultEnum.SUCCESS.getCode())
                .setMessage(msg);
    }



    /**
     * 返回成功消息
     *
     * @param data 返回内容
     * @return 成功消息
     */
    public static <T> Result<T> success(T data) {
        return of(ResultEnum.SUCCESS, data);
    }

    /**
     * 返回成功消息
     *
     * @param data 返回内容
     * @return 成功消息
     */
    public static <T> Result<T> success(T data, int code) {
        return new Result()
                .setCode(code)
                .setMessage(ResultEnum.SUCCESS.getMessage())
                .setData(data);
    }

    /**
     * 返回错误消息
     *
     * @return
     */
    public static <T> Result<T> error() {
        return of(ResultEnum.ERROR);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> Result<T> error(String msg) {
        return new Result()
                .setCode(ResultEnum.ERROR.getCode())
                .setMessage(msg);
    }

    /**
     * 返回错误消息
     *
     * @param code 返回状态码
     * @param msg  返回内容
     * @return 警告消息
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<T>()
                .setCode(code)
                .setMessage(msg);
    }

    /**
     * 返回错误消息：参数错误异常
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> Result<T> paramError(String msg) {
        return new Result<T>()
                .setCode(ResultEnum.PARAMETER_ERR.getCode())
                .setMessage(msg);
    }

    /**
     * 返回错误消息
     *
     * @param resultEnum 返回结果枚举
     * @return 警告消息
     */
    public static <T> Result<T> error(ResultEnum resultEnum) {
        return of(resultEnum);
    }

//    /**
//     * 返回错误消息
//     *
//     * @param ex 异常
//     * @return 警告消息
//     */
//    public static <T> Result<T> error(BizException ex) {
//        return error(ex.getCode(), ex.getMessage());
//    }

    /**
     * 默认降级结果
     *
     * @return
     */
    public static <T> Result<T> defaultFB() {
        return Result.error(ResultEnum.REQUEST_TIMEOUT);
    }
}
