package com.hjh.bibackend.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@Slf4j
public class KeyUtils {

    private static final int length = 256;

    /**
     * 生成AK，用于标识用户身份，也就是说必须唯一
     * */
    public static String generateAK(){
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 生成sk,使用Base64进行编码
     * */
    public static String generateSK(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(length);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.encode(secretKey.getEncoded());
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }
}
