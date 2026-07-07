package com.example.libsys.module.user.controller;

import com.example.libsys.common.result.R;
import com.example.libsys.common.result.PageResult;
import com.example.libsys.module.user.dto.UserDto;
import com.example.libsys.module.user.entity.RoleEntity;
import com.example.libsys.module.user.entity.UserEntity;
import com.example.libsys.module.user.service.UserService;
import com.example.libsys.module.user.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<UserVo>> getUserPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(userService.getUserPage(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<UserVo> getUserById(@PathVariable Long id) {
        return R.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> addUser(@Valid @RequestBody UserDto dto) {
        userService.addUser(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
        userService.updateUser(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @GetMapping("/roles")
    public R<List<RoleEntity>> getAllRoles() {
        return R.ok(userService.getAllRoles());
    }

    @GetMapping("/simple-list")
    public R<List<UserEntity>> getSimpleUserList() {
        return R.ok(userService.getSimpleUserList());
    }
}
