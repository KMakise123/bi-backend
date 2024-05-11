package com.hjh.bibackend.common.constant;

public class JwtConstant {
    public final static String ACCESS_TOKEN_PREFIX = "access_token:";
    public final static String REFRESH_TOKEN_PREFIX = "refresh_token:";
    public final static String SECRET_KEY = "20889c5462e8dc4ca24ddfe6b35b41d0203010001";
    //如果单位是秒，那么86400就是一天的时间。如果单位是毫秒，那么就要乘上1000
    public final static long EXPIRATION_TIME = 60*60*24;
}
