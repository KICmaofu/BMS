package com.example.libsys.module.dashboard.vo;


public class DashboardVo {

    private Long totalBooks;

    private Long totalUsers;

    private Long totalBorrowing;

    private Long totalOverdue;

    public Long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(Long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalBorrowing() {
        return totalBorrowing;
    }

    public void setTotalBorrowing(Long totalBorrowing) {
        this.totalBorrowing = totalBorrowing;
    }

    public Long getTotalOverdue() {
        return totalOverdue;
    }

    public void setTotalOverdue(Long totalOverdue) {
        this.totalOverdue = totalOverdue;
    }
}
