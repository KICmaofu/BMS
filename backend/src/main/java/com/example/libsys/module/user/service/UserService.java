package com.example.libsys.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.libsys.common.exception.BusinessException;
import com.example.libsys.common.result.PageResult;
import com.example.libsys.mapper.RoleMapper;
import com.example.libsys.mapper.UserMapper;
import com.example.libsys.mapper.UserRoleMapper;
import com.example.libsys.module.user.dto.UserDto;
import com.example.libsys.module.user.entity.RoleEntity;
import com.example.libsys.module.user.entity.UserEntity;
import com.example.libsys.module.user.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class UserService extends ServiceImpl<UserMapper, UserEntity> {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(RoleMapper roleMapper, UserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<UserVo> getUserPage(Long pageNum, Long pageSize, String keyword) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword)
                    .or().like(UserEntity::getNickname, keyword)
                    .or().like(UserEntity::getEmail, keyword));
        }
        wrapper.orderByDesc(UserEntity::getCreatedAt);

        Page<UserEntity> result = page(page, wrapper);

        List<UserVo> vos = result.getRecords().stream().map(this::convertToVo).collect(Collectors.toList());

        return PageResult.of(vos, result.getTotal(), pageNum, pageSize);
    }

    private UserVo convertToVo(UserEntity user) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        List<RoleEntity> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(RoleEntity::getCode).collect(Collectors.toList()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDto dto) {
        UserEntity existUser = getOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, dto.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(dto, user);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        save(user);

        if (dto.getRoleIds() != null && dto.getRoleIds().length > 0) {
            userRoleMapper.batchInsert(user.getId(), dto.getRoleIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserDto dto) {
        UserEntity user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserEntity updateEntity = new UserEntity();
        updateEntity.setId(id);
        updateEntity.setNickname(dto.getNickname());
        updateEntity.setEmail(dto.getEmail());
        updateEntity.setPhone(dto.getPhone());
        updateEntity.setStatus(dto.getStatus());

        if (StringUtils.hasText(dto.getPassword())) {
            updateEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        updateById(updateEntity);

        userRoleMapper.deleteByUserId(id);
        if (dto.getRoleIds() != null && dto.getRoleIds().length > 0) {
            userRoleMapper.batchInsert(id, dto.getRoleIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        UserEntity user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("超级管理员不能删除");
        }
        removeById(id);
        userRoleMapper.deleteByUserId(id);
    }

    public UserVo getUserById(Long id) {
        UserEntity user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVo(user);
    }

    public List<RoleEntity> getAllRoles() {
        return roleMapper.selectList(null);
    }

    public List<UserEntity> getSimpleUserList() {
        return list(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, 1)
                .select(UserEntity::getId, UserEntity::getUsername, UserEntity::getNickname)
                .orderByAsc(UserEntity::getUsername));
    }
}
