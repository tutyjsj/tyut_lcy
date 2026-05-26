package com.ruoyi.tuyt.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        long pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        return new PageResult<>(records, total, pageNum, pageSize, pages);
    }
}
