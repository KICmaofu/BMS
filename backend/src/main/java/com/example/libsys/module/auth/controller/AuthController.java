package com.example.libsys.module.auth.controller;

import com.example.libsys.common.result.R;
import com.example.libsys.module.auth.dto.LoginDto;
import com.example.libsys.module.auth.service.AuthService;
import com.example.libsys.module.auth.vo.LoginVo;
import com.example.libsys.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginVo> login(@Valid @RequestBody LoginDto dto) {
        return R.ok(authService.login(dto));
    }

    @GetMapping("/info")
    public R<LoginVo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            LoginVo vo = new LoginVo();
            vo.setUserId(loginUser.getUserId());
            vo.setUsername(loginUser.getUsername());
            vo.setNickname(loginUser.getNickname());
            vo.setRoles(loginUser.getRoles().stream().map(r -> r.getCode()).toList());
            return R.ok(vo);
        }
        return R.fail(401, "未登录");
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        SecurityContextHolder.clearContext();
        return R.ok();
    }
}
