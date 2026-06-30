package com.example.libsys.module.auth.service;

import com.example.libsys.common.exception.BusinessException;
import com.example.libsys.common.utils.JwtUtil;
import com.example.libsys.module.auth.dto.LoginDto;
import com.example.libsys.module.auth.vo.LoginVo;
import com.example.libsys.security.LoginUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public LoginVo login(LoginDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            String token = jwtUtil.generateToken(loginUser.getUserId(), loginUser.getUsername());

            return new LoginVo(
                    token,
                    loginUser.getUserId(),
                    loginUser.getUsername(),
                    loginUser.getNickname(),
                    loginUser.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toList())
            );
        } catch (AuthenticationException e) {
            throw new BusinessException(401, "用户名或密码错误");
        }
    }
}
