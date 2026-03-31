package com.hackathon.backend.dto.response;

public class AuthResponse {

    private String token;
    private UserInfoResponse user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserInfoResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoResponse getUser() {
        return user;
    }

    public void setUser(UserInfoResponse user) {
        this.user = user;
    }
}
