package com.example.libsys.module.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.libsys.common.exception.BusinessException;
import com.example.libsys.common.result.PageResult;
import com.example.libsys.mapper.BookMapper;
import com.example.libsys.mapper.CategoryMapper;
import com.example.libsys.module.book.dto.BookDto;
import com.example.libsys.module.book.entity.BookEntity;
import com.example.libsys.module.book.entity.CategoryEntity;
import com.example.libsys.module.book.vo.BookVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService extends ServiceImpl<BookMapper, BookEntity> {

    private final CategoryMapper categoryMapper;

    public BookService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public PageResult<BookVo> getBookPage(Long pageNum, Long pageSize, String keyword, Long categoryId) {
        Page<BookEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<BookEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BookEntity::getTitle, keyword)
                    .or().like(BookEntity::getAuthor, keyword)
                    .or().like(BookEntity::getIsbn, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(BookEntity::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(BookEntity::getCreatedAt);

        Page<BookEntity> result = page(page, wrapper);

        List<Long> categoryIds = result.getRecords().stream()
                .map(BookEntity::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryMap = Map.of();
        if (!categoryIds.isEmpty()) {
            List<CategoryEntity> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
        }

        Map<Long, String> finalCategoryMap = categoryMap;
        List<BookVo> vos = result.getRecords().stream()
                .map(book -> {
                    BookVo vo = new BookVo();
                    BeanUtils.copyProperties(book, vo);
                    if (book.getCategoryId() != null) {
                        vo.setCategoryName(finalCategoryMap.get(book.getCategoryId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(vos, result.getTotal(), pageNum, pageSize);
    }

    public BookVo getBookById(Long id) {
        BookEntity book = getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        BookVo vo = new BookVo();
        BeanUtils.copyProperties(book, vo);
        if (book.getCategoryId() != null) {
            CategoryEntity category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBook(BookDto dto) {
        if (StringUtils.hasText(dto.getIsbn())) {
            BookEntity existBook = getOne(new LambdaQueryWrapper<BookEntity>().eq(BookEntity::getIsbn, dto.getIsbn()));
            if (existBook != null) {
                throw new BusinessException("ISBN已存在");
            }
        }

        BookEntity book = new BookEntity();
        BeanUtils.copyProperties(dto, book);
        if (book.getTotalCount() == null) {
            book.setTotalCount(0);
        }
        book.setAvailableCount(book.getTotalCount());
        save(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBook(Long id, BookDto dto) {
        BookEntity book = getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        if (StringUtils.hasText(dto.getIsbn())) {
            BookEntity existBook = getOne(new LambdaQueryWrapper<BookEntity>()
                    .eq(BookEntity::getIsbn, dto.getIsbn())
                    .ne(BookEntity::getId, id));
            if (existBook != null) {
                throw new BusinessException("ISBN已存在");
            }
        }

        BookEntity updateEntity = new BookEntity();
        updateEntity.setId(id);
        BeanUtils.copyProperties(dto, updateEntity);

        if (dto.getTotalCount() != null && !dto.getTotalCount().equals(book.getTotalCount())) {
            int diff = dto.getTotalCount() - book.getTotalCount();
            int newAvailable = book.getAvailableCount() + diff;
            if (newAvailable < 0) {
                throw new BusinessException("总馆藏数不能小于已借出数量");
            }
            updateEntity.setAvailableCount(newAvailable);
        }

        updateById(updateEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        BookEntity book = getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getTotalCount() > book.getAvailableCount()) {
            throw new BusinessException("存在未归还的图书，无法删除");
        }
        removeById(id);
    }

    public List<BookEntity> getAllBooks() {
        return list(new LambdaQueryWrapper<BookEntity>().orderByDesc(BookEntity::getCreatedAt));
    }

    public List<BookVo> getHotBooks(Integer limit) {
        List<BookEntity> books = list(new LambdaQueryWrapper<BookEntity>()
                .orderByDesc(BookEntity::getTotalCount)
                .last("LIMIT " + (limit != null ? limit : 10)));

        return books.stream().map(book -> {
            BookVo vo = new BookVo();
            BeanUtils.copyProperties(book, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
