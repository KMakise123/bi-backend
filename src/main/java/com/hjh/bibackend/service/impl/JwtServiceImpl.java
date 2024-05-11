package com.hjh.bibackend.service.impl;



import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.JwtTokenDto;
import com.hjh.bibackend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.hjh.bibackend.common.constant.JwtConstant.*;


@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public String generateAccessToken(User user) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId()));
        claims.put("avatarUrl", user.getAvatarUrl());
        claims.put("userRole", user.getUserRole());
        claims.put("userStatus",user.getStatus());
        Date now = new Date();
        //注意这里的now.getTime()是以毫秒为单位的
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME*1000);
        String token = Jwts.builder()
                //设置载荷
                .setClaims(claims)
                //设置签发时间
                .setIssuedAt(now)
                //设置过期时间
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        String key = ACCESS_TOKEN_PREFIX + user.getId();
        System.out.println(key);
        stringRedisTemplate.opsForValue().set(key, token, EXPIRATION_TIME, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public String generateRefreshToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getId().toString());

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME*1000);//超时时间设置为24个小时

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        String key = REFRESH_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(key, token, EXPIRATION_TIME, TimeUnit.SECONDS);
        return token;
    }


    @Override
    public User validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            String avatarUrl = (String) claims.get("avatarUrl") ;
            String userRole = (String) claims.get("userRole") ;
            Integer userStatus = (int) claims.get("userStatus");
            String key = ACCESS_TOKEN_PREFIX + userId;
            String storedToken = stringRedisTemplate.opsForValue().get(key);

            if (storedToken != null && storedToken.equals(token)) {
                // 如果Redis中存储的令牌与传入的令牌匹配，则验证通过
                return new User(Long.parseLong(userId),avatarUrl,userStatus,userRole);
            }
        } catch (Exception e) {
            // 解析过程中发生异常，验证失败
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * 通过token获取UserId?
     * */
    @Override
    public String getIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    //撤销令牌
    @Override
    public void revokeToken(User user) {
        String accessKey = ACCESS_TOKEN_PREFIX + user.getId();
        String refreshKey = REFRESH_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.delete(refreshKey);
    }

    @Override
    public void revokeTokenById(Long userId) {
        String accessKey = ACCESS_TOKEN_PREFIX + userId;
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.delete(refreshKey);
    }

    //清理过期的令牌
    @Override
    public void cleanExpiredTokens() {
        stringRedisTemplate.keys("*").forEach(key -> {
            //之所以在key值前加上前缀是为了判定那些是关于token的热点信息
            if (key.startsWith(ACCESS_TOKEN_PREFIX) || key.startsWith(REFRESH_TOKEN_PREFIX)) {
                String token = stringRedisTemplate.opsForValue().get(key);
                if (token != null && isTokenExpired(token)) {
                    stringRedisTemplate.delete(key);
                }
            }
        });
    }

    //令牌是否过期
    @Override
    public boolean isTokenExpired(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (ExpiredJwtException e){
            claims = e.getClaims();
            log.error(e.getMessage());
        }
        Date expirationDate = claims.getExpiration();
        return expirationDate.before(new Date());
    }


    //刷新令牌
    @Override
    public void save2Redis(JwtTokenDto jwtToken, User user) {
        String token = jwtToken.getAccessToken();
        String refreshToken =  jwtToken.getRefreshToken();
        String accessKey = ACCESS_TOKEN_PREFIX + user.getId();
        String refreshKey = REFRESH_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(accessKey,token,EXPIRATION_TIME, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(refreshKey,refreshToken,EXPIRATION_TIME, TimeUnit.SECONDS);
    }
}
