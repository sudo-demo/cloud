package com.example.common.model;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.util.SqlUtil;
import com.example.common.util.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 
 */
@Data
public class PageDTO implements Serializable {

    private static final long serialVersionUID = -4587741144199718611L;

    @ApiModelProperty(value = "当前页", example = "1")
    private Long pageNum;

    @ApiModelProperty(value = "每页的数量", example = "10")
    private Long pageSize;

    @ApiModelProperty(value = "正排序字段")
    private String asc;

    @ApiModelProperty(value = "倒排序字段")
    private String desc;

    @ApiModelProperty(hidden = true)
    public <T> Page<T> toPage() {
        Optional<Long> current = Optional.ofNullable(getPageNum());
        Optional<Long> size = Optional.ofNullable(getPageSize());
        Page<T> page = new Page<>(current.orElse(1L), size.orElse(10L));
        if (getDesc() != null) {
            page.setOrders(StringUtil.toStrList(
                            SqlUtil.filter(getDesc())).stream()
                    .map((col) -> OrderItem.desc(StringUtil.humpToUnderline(col)))
                    .collect(Collectors.toList()));
        }
        if (getAsc() != null) {
            page.setOrders(StringUtil.toStrList(
                            SqlUtil.filter(getAsc())).stream()
                    .map((col) -> OrderItem.asc(StringUtil.humpToUnderline(col)))
                    .collect(Collectors.toList()));
        }
        return page;

    }

}
