package com.example.libsys.module.record.dto;

import jakarta.validation.constraints.NotNull;

public class BorrowDto {

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Integer borrowDays;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }
}
