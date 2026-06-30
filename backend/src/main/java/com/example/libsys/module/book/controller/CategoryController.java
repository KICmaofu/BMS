package com.example.libsys.module.book.controller;

import com.example.libsys.common.result.R;
import com.example.libsys.module.book.entity.CategoryEntity;
import com.example.libsys.module.book.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

@GetMapping("/list")


    public R<List<CategoryEntity>> getAllCategories() {
        return R.ok(categoryService.getAllCategories());
    }

    @GetMapping("/parent/{parentId}")
    public R<List<CategoryEntity>> getCategoriesByParentId(@PathVariable Long parentId) {
        return R.ok(categoryService.getCategoriesByParentId(parentId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> addCategory(@RequestBody CategoryEntity category) {
        categoryService.addCategory(category);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> updateCategory(@PathVariable Long id, @RequestBody CategoryEntity category) {
        categoryService.updateCategory(id, category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public R<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return R.ok();
    }
}
