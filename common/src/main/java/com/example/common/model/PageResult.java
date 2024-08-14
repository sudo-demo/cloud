package com.example.common.model;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.enums.ResultEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author meng
 */
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 7249399839674095037L;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> records;


    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public PageResult(List<T> list, long total) {
        this.records = list;
        this.total =  total;
    }

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public static <T> PageResult<T> of(List<T> list, long total) {
        ResultEnum success = ResultEnum.SUCCESS;
        return new PageResult<T>()
                .setCode(success.getCode())
                .setMessage(success.getMessage())
                .setRecords(list)
                .setTotal(total);
    }

    /**
     * 分页
     *
     * @param page 分页数据
     */
    public static <T> PageResult<T> of(IPage page) {
        ResultEnum success = ResultEnum.SUCCESS;
        return new PageResult<T>()
                .setCode(success.getCode())
                .setMessage(success.getMessage())
                .setRecords(page.getRecords())
                .setTotal(page.getTotal());
    }

    /**
     * 参数异常
     *
     * @param msg 提示信息
     */
    public static <T> PageResult<T> paramError(String msg) {
        return new PageResult<T>()
                .setCode(ResultEnum.PARAMETER_ERR.getCode())
                .setMessage(msg);
    }
}
