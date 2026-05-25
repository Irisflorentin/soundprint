package com.soundprint.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {
    private List<T> records;       // 当前页数据
    private Long total;            // 总条数
    private Long current;          // 当前页码
    private Long size;             // 每页大小
    private Long pages;            // 总页数

    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        return result;
    }
}
