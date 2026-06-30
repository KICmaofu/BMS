package com.example.libsys.module.auth.vo;


import java.util.List;

public class LoginVo {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private List<String> roles;

    public LoginVo() {
    }

    public LoginVo(String token, Long userId, String username, String nickname, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
