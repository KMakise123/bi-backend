package com.hjh.bibackend.service;

import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.JwtTokenDto;

/**
 * JWT服务接口
 * */
public interface JwtService {

    public String generateAccessToken(User user);

    public String generateRefreshToken(User user);

    public User validateToken(String token);

    public String getIdFromToken(String token);

    public void revokeToken(User user);

    public void revokeTokenById(Long userId);

    public void cleanExpiredTokens();

    public boolean isTokenExpired(String token);

    public void save2Redis(JwtTokenDto jwtToken, User user);
}
