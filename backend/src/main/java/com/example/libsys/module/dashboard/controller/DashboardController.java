package com.example.libsys.module.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.libsys.common.result.R;
import com.example.libsys.mapper.BookMapper;
import com.example.libsys.mapper.BorrowRecordMapper;
import com.example.libsys.mapper.UserMapper;
import com.example.libsys.module.dashboard.vo.DashboardVo;
import com.example.libsys.module.record.entity.BorrowRecordEntity;
import com.example.libsys.module.user.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public DashboardController(BookMapper bookMapper, UserMapper userMapper, BorrowRecordMapper borrowRecordMapper) {
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

@GetMapping("/stats")


    public R<DashboardVo> getDashboardStats() {
        DashboardVo vo = new DashboardVo();

        Long totalBooks = bookMapper.selectCount(null);
        vo.setTotalBooks(totalBooks);

        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getStatus, 1));
        vo.setTotalUsers(totalUsers);

        Long totalBorrowing = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecordEntity>().eq(BorrowRecordEntity::getStatus, 0)
        );
        vo.setTotalBorrowing(totalBorrowing);

        Long totalOverdue = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecordEntity>().eq(BorrowRecordEntity::getStatus, 2)
        );
        vo.setTotalOverdue(totalOverdue);

        return R.ok(vo);
    }
}
