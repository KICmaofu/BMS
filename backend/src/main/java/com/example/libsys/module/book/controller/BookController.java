package com.example.libsys.module.book.controller;

import com.example.libsys.common.result.R;
import com.example.libsys.common.result.PageResult;
import com.example.libsys.module.book.dto.BookDto;
import com.example.libsys.module.book.service.BookService;
import com.example.libsys.module.book.vo.BookVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

@GetMapping("/page")


    public R<PageResult<BookVo>> getBookPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return R.ok(bookService.getBookPage(pageNum, pageSize, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public R<BookVo> getBookById(@PathVariable Long id) {
        return R.ok(bookService.getBookById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> addBook(@Valid @RequestBody BookDto dto) {
        bookService.addBook(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> updateBook(@PathVariable Long id, @RequestBody BookDto dto) {
        bookService.updateBook(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return R.ok();
    }

    @GetMapping("/hot")
    public R<List<BookVo>> getHotBooks(@RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(bookService.getHotBooks(limit));
    }
}
