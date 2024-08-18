package com.example.common.config;

import com.example.common.enums.ResultEnum;
import com.example.common.exception.TokenException;
import com.example.common.exception.validateException;
import com.example.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@ResponseBody
public class ExceptionHandle {


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleBadRequestException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("请求参数错误", e);
        e.printStackTrace();
//        return Result.paramError(e.getMessage() == null ? e.toString() : e.getMessage());
        return Result.paramError("请求缺少必需的请求体数据");

    }

    @ExceptionHandler(validateException.class)
    public Result<Void> validateException(Exception e) {
        log.error("参数校验异常", e);
        e.printStackTrace();
        return Result.paramError(e.getMessage() == null ? e.toString() : e.getMessage());
    }

    @ExceptionHandler(TokenException.class)
    public Result tokenException(TokenException e) {
        log.error("token异常", e);
        e.printStackTrace();
        return Result.error(ResultEnum.FORBIDDEN.getCode(), e.getMessage() == null ? ResultEnum.FORBIDDEN.getMessage() : e.getMessage());
    }


    /**
     * 通用处理
     */
    @ExceptionHandler(value = Exception.class)
    public Result<Void> errorHandle(Exception e) {
        e.printStackTrace();
        log.error("执行代码异常", e);
        return Result.error(e.getMessage() == null ? e.toString() : e.getMessage());
    }


    /**
     * 校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
//        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(o -> o.getDefaultMessage())
                .collect(Collectors.joining(","));
        log.error("参数校验异常", e);
        return Result.error(ResultEnum.PARAMETER_ERR.getCode(), message);
    }
}
