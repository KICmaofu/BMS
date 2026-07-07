package com.example.libsys.module.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.libsys.common.exception.BusinessException;
import com.example.libsys.mapper.BookMapper;
import com.example.libsys.mapper.CategoryMapper;
import com.example.libsys.module.book.entity.BookEntity;
import com.example.libsys.module.book.entity.CategoryEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class CategoryService extends ServiceImpl<CategoryMapper, CategoryEntity> {

    private final BookMapper bookMapper;

    public CategoryService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<CategoryEntity> getAllCategories() {
        return list(new LambdaQueryWrapper<CategoryEntity>().orderByAsc(CategoryEntity::getSortOrder));
    }

    public List<CategoryEntity> getCategoriesByParentId(Long parentId) {
        return list(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getParentId, parentId)
                .orderByAsc(CategoryEntity::getSortOrder));
    }

    @Transactional(rollbackFor = Exception.class)
    public void addCategory(CategoryEntity category) {
        CategoryEntity exist = getOne(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getCode, category.getCode()));
        if (exist != null) {
            throw new BusinessException("分类编码已存在");
        }
        save(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, CategoryEntity category) {
        CategoryEntity exist = getById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }
        category.setId(id);
        updateById(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        CategoryEntity exist = getById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }

        Long count = bookMapper.selectCount(new LambdaQueryWrapper<BookEntity>().eq(BookEntity::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException("该分类下有图书，无法删除");
        }

        List<CategoryEntity> children = list(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getParentId, id));
        if (!children.isEmpty()) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }

        removeById(id);
    }
}
