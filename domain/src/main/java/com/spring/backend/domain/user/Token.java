package com.spring.backend.domain.user;

public class Token {

    private Long id;
    private String accessToken;
    private String refreshToken;

    public Token(Long id, String accessToken, String refreshToken) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getId()            { return id; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken(){ return refreshToken; }
}
