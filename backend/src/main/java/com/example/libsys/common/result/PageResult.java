package com.example.libsys.common.result;

import java.util.List;

public class PageResult<T> {
    private List<T> rows;
    private Long total;
    private Long pageNum;
    private Long pageSize;

    public PageResult() {}

    public PageResult(List<T> rows, Long total, Long pageNum, Long pageSize) {
        this.rows = rows;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(List<T> rows, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(rows, total, pageNum, pageSize);
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
