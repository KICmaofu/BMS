package com.example.libsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.libsys.module.book.entity.BookEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<BookEntity> {
}
