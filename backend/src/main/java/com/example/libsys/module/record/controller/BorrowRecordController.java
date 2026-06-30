package com.example.libsys.module.record.controller;

import com.example.libsys.common.result.R;
import com.example.libsys.common.result.PageResult;
import com.example.libsys.module.record.dto.BorrowDto;
import com.example.libsys.module.record.service.BorrowRecordService;
import com.example.libsys.module.record.vo.BorrowRecordVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/record")
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    public BorrowRecordController(BorrowRecordService borrowRecordService) {
        this.borrowRecordService = borrowRecordService;
    }

@GetMapping("/page")


    public R<PageResult<BorrowRecordVo>> getRecordPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return R.ok(borrowRecordService.getRecordPage(pageNum, pageSize, userId, bookId, status, keyword));
    }

    @GetMapping("/{id}")
    public R<BorrowRecordVo> getRecordById(@PathVariable Long id) {
        return R.ok(borrowRecordService.getRecordById(id));
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> borrowBook(@Valid @RequestBody BorrowDto dto) {
        borrowRecordService.borrowBook(dto);
        return R.ok();
    }

    @PutMapping("/return/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> returnBook(@PathVariable Long id) {
        borrowRecordService.returnBook(id);
        return R.ok();
    }

    @PutMapping("/renew/{id}")
    public R<Void> renewBook(@PathVariable Long id, @RequestParam(required = false, defaultValue = "30") Integer days) {
        borrowRecordService.renewBook(id, days);
        return R.ok();
    }

    @PutMapping("/lost/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> markLost(@PathVariable Long id) {
        borrowRecordService.markLost(id);
        return R.ok();
    }

    @PutMapping("/pay-fine/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> payFine(@PathVariable Long id) {
        borrowRecordService.payFine(id);
        return R.ok();
    }

    @GetMapping("/my/count")
    public R<Long> getMyBorrowingCount(@RequestParam Long userId) {
        return R.ok(borrowRecordService.getBorrowingCountByUserId(userId));
    }
}
