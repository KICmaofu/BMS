package com.example.libsys.module.record.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.libsys.common.exception.BusinessException;
import com.example.libsys.common.result.PageResult;
import com.example.libsys.mapper.BookMapper;
import com.example.libsys.mapper.BorrowRecordMapper;
import com.example.libsys.mapper.UserMapper;
import com.example.libsys.module.book.entity.BookEntity;
import com.example.libsys.module.record.dto.BorrowDto;
import com.example.libsys.module.record.entity.BorrowRecordEntity;
import com.example.libsys.module.record.vo.BorrowRecordVo;
import com.example.libsys.module.user.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class BorrowRecordService extends ServiceImpl<BorrowRecordMapper, BorrowRecordEntity> {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    private static final int DEFAULT_BORROW_DAYS = 30;
    private static final int MAX_RENEW_COUNT = 2;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    public BorrowRecordService(BookMapper bookMapper, UserMapper userMapper) {
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    public PageResult<BorrowRecordVo> getRecordPage(Long pageNum, Long pageSize, Long userId, Long bookId, Integer status, String keyword) {
        Page<BorrowRecordEntity> page = new Page<>(pageNum, pageSize);

        List<Long> finalMatchedBookIds = null;
        List<Long> finalMatchedUserIds = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Long> matchedBookIds = bookMapper.selectList(
                    new LambdaQueryWrapper<BookEntity>()
                            .like(BookEntity::getTitle, keyword)
                            .or().like(BookEntity::getIsbn, keyword)
            ).stream().map(BookEntity::getId).collect(Collectors.toList());

            List<Long> matchedUserIds = userMapper.selectList(
                    new LambdaQueryWrapper<UserEntity>()
                            .like(UserEntity::getUsername, keyword)
                            .or().like(UserEntity::getNickname, keyword)
            ).stream().map(UserEntity::getId).collect(Collectors.toList());

            if (matchedBookIds.isEmpty() && matchedUserIds.isEmpty()) {
                return PageResult.of(List.of(), 0L, pageNum, pageSize);
            }

            finalMatchedBookIds = matchedBookIds;
            finalMatchedUserIds = matchedUserIds;
        }

        final List<Long> bookIdsForLambda = finalMatchedBookIds;
        final List<Long> userIdsForLambda = finalMatchedUserIds;

        LambdaQueryWrapper<BorrowRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(BorrowRecordEntity::getUserId, userId);
        }
        if (bookId != null) {
            wrapper.eq(BorrowRecordEntity::getBookId, bookId);
        }
        if (status != null) {
            wrapper.eq(BorrowRecordEntity::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> {
                boolean hasCondition = false;
                if (bookIdsForLambda != null && !bookIdsForLambda.isEmpty()) {
                    w.in(BorrowRecordEntity::getBookId, bookIdsForLambda);
                    hasCondition = true;
                }
                if (userIdsForLambda != null && !userIdsForLambda.isEmpty()) {
                    if (hasCondition) {
                        w.or().in(BorrowRecordEntity::getUserId, userIdsForLambda);
                    } else {
                        w.in(BorrowRecordEntity::getUserId, userIdsForLambda);
                    }
                }
                if (!hasCondition && (userIdsForLambda == null || userIdsForLambda.isEmpty())) {
                    w.eq(BorrowRecordEntity::getId, -1L);
                }
            });
        }
        wrapper.orderByDesc(BorrowRecordEntity::getBorrowDate);

        Page<BorrowRecordEntity> result = page(page, wrapper);

        List<Long> bookIds = result.getRecords().stream()
                .map(BorrowRecordEntity::getBookId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> userIds = result.getRecords().stream()
                .map(BorrowRecordEntity::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, BookEntity> bookMap = Map.of();
        if (!bookIds.isEmpty()) {
            bookMap = bookMapper.selectBatchIds(bookIds).stream()
                    .collect(Collectors.toMap(BookEntity::getId, b -> b));
        }

        Map<Long, UserEntity> userMap = Map.of();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(UserEntity::getId, u -> u));
        }

        Map<Long, BookEntity> finalBookMap = bookMap;
        Map<Long, UserEntity> finalUserMap = userMap;
        List<BorrowRecordVo> vos = result.getRecords().stream()
                .map(record -> convertToVo(record, finalBookMap, finalUserMap))
                .collect(Collectors.toList());

        return PageResult.of(vos, result.getTotal(), pageNum, pageSize);
    }

    private BorrowRecordVo convertToVo(BorrowRecordEntity record, Map<Long, BookEntity> bookMap, Map<Long, UserEntity> userMap) {
        BorrowRecordVo vo = new BorrowRecordVo();
        vo.setId(record.getId());
        vo.setBookId(record.getBookId());
        vo.setUserId(record.getUserId());
        vo.setBorrowDate(record.getBorrowDate());
        vo.setDueDate(record.getDueDate());
        vo.setReturnDate(record.getReturnDate());
        vo.setStatus(record.getStatus());
        vo.setStatusText(getStatusText(record.getStatus()));
        vo.setFineAmount(record.getFineAmount());
        vo.setFinePaid(record.getFinePaid());
        vo.setRenewCount(record.getRenewCount());
        vo.setRemark(record.getRemark());

        BookEntity book = bookMap.get(record.getBookId());
        if (book != null) {
            vo.setBookTitle(book.getTitle());
            vo.setBookIsbn(book.getIsbn());
        }

        UserEntity user = userMap.get(record.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserNickname(user.getNickname());
        }

        return vo;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "借阅中";
            case 1 -> "已归还";
            case 2 -> "已逾期";
            case 3 -> "已丢失";
            default -> "未知";
        };
    }

    @Transactional(rollbackFor = Exception.class)
    public void borrowBook(BorrowDto dto) {
        BookEntity book = bookMapper.selectById(dto.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        if (book.getAvailableCount() <= 0) {
            throw new BusinessException("图书库存不足");
        }

        UserEntity user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        Long borrowingCount = count(new LambdaQueryWrapper<BorrowRecordEntity>()
                .eq(BorrowRecordEntity::getUserId, dto.getUserId())
                .in(BorrowRecordEntity::getStatus, 0, 2));
        if (borrowingCount >= 5) {
            throw new BusinessException("该用户已达最大借阅数量(5本)");
        }

        int borrowDays = dto.getBorrowDays() != null ? dto.getBorrowDays() : DEFAULT_BORROW_DAYS;
        if (borrowDays <= 0 || borrowDays > 90) {
            throw new BusinessException("借阅天数必须在1-90天之间");
        }

        BorrowRecordEntity record = new BorrowRecordEntity();
        record.setBookId(dto.getBookId());
        record.setUserId(dto.getUserId());
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(borrowDays));
        record.setStatus(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFinePaid(0);
        record.setRenewCount(0);
        save(record);

        book.setAvailableCount(book.getAvailableCount() - 1);
        bookMapper.updateById(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnBook(Long recordId) {
        BorrowRecordEntity record = getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        if (record.getStatus() == 1) {
            throw new BusinessException("该图书已归还");
        }

        BookEntity book = bookMapper.selectById(record.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setReturnDate(now);
        record.setStatus(1);

        if (now.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate().toLocalDate(), now.toLocalDate());
            BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
            record.setFineAmount(fine);
        }

        updateById(record);

        book.setAvailableCount(book.getAvailableCount() + 1);
        bookMapper.updateById(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void renewBook(Long recordId, Integer days) {
        BorrowRecordEntity record = getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        if (record.getStatus() != 0 && record.getStatus() != 2) {
            throw new BusinessException("该记录状态不支持续借");
        }

        if (record.getRenewCount() >= MAX_RENEW_COUNT) {
            throw new BusinessException("已达最大续借次数(" + MAX_RENEW_COUNT + "次)");
        }

        int renewDays = days != null ? days : DEFAULT_BORROW_DAYS;
        if (renewDays <= 0 || renewDays > 30) {
            throw new BusinessException("续借天数必须在1-30天之间");
        }

        LocalDateTime newDueDate = record.getDueDate().plusDays(renewDays);
        record.setDueDate(newDueDate);
        record.setRenewCount(record.getRenewCount() + 1);

        if (record.getStatus() == 2) {
            record.setStatus(0);
        }

        updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markLost(Long recordId) {
        BorrowRecordEntity record = getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        if (record.getStatus() != 0 && record.getStatus() != 2) {
            throw new BusinessException("该记录状态不支持标记丢失");
        }

        BookEntity book = bookMapper.selectById(record.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        record.setStatus(3);
        record.setFineAmount(book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO);
        updateById(record);

        book.setTotalCount(book.getTotalCount() - 1);
        bookMapper.updateById(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void payFine(Long recordId) {
        BorrowRecordEntity record = getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        if (record.getFineAmount() == null || record.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("没有需要缴纳的罚款");
        }

        if (record.getFinePaid() == 1) {
            throw new BusinessException("罚款已缴纳");
        }

        record.setFinePaid(1);
        updateById(record);
    }

    public void updateOverdueStatus() {
        List<BorrowRecordEntity> records = list(new LambdaQueryWrapper<BorrowRecordEntity>()
                .eq(BorrowRecordEntity::getStatus, 0)
                .lt(BorrowRecordEntity::getDueDate, LocalDateTime.now()));

        for (BorrowRecordEntity record : records) {
            record.setStatus(2);
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate().toLocalDate(), LocalDate.now());
            BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
            record.setFineAmount(fine);
        }

        if (!records.isEmpty()) {
            updateBatchById(records);
        }
    }

    public BorrowRecordVo getRecordById(Long id) {
        BorrowRecordEntity record = getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        Map<Long, BookEntity> bookMap = Map.of();
        BookEntity book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            bookMap = Map.of(book.getId(), book);
        }

        Map<Long, UserEntity> userMap = Map.of();
        UserEntity user = userMapper.selectById(record.getUserId());
        if (user != null) {
            userMap = Map.of(user.getId(), user);
        }

        return convertToVo(record, bookMap, userMap);
    }

    public long getBorrowingCountByUserId(Long userId) {
        return count(new LambdaQueryWrapper<BorrowRecordEntity>()
                .eq(BorrowRecordEntity::getUserId, userId)
                .in(BorrowRecordEntity::getStatus, 0, 2));
    }
}
