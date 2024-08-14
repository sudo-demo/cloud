package com.example.common.config;

import com.example.common.enums.ResultEnum;
import com.example.common.exception.TokenException;
import com.example.common.exception.validateException;
import com.example.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@ResponseBody
public class ExceptionHandle {


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleBadRequestException(HttpMessageNotReadableException e, HttpServletRequest request) {
        e.printStackTrace();
//        return Result.paramError(e.getMessage() == null ? e.toString() : e.getMessage());
        return Result.paramError("请求缺少必需的请求体数据");

    }

    @ExceptionHandler(validateException.class)
    public Result<Void> validateException(Exception e) {
        e.printStackTrace();
        return Result.paramError(e.getMessage() == null ? e.toString() : e.getMessage());
    }

    @ExceptionHandler(TokenException.class)
    public Result tokenException(TokenException e) {
        e.printStackTrace();
        return Result.error(ResultEnum.FORBIDDEN.getCode(),e.getMessage()==null?ResultEnum.FORBIDDEN.getMessage():e.getMessage());
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
        return Result.error(ResultEnum.PARAMETER_ERR.getCode(), message);
    }
}
