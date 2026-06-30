package com.example.libsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.libsys.module.record.entity.BorrowRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecordEntity> {
}
