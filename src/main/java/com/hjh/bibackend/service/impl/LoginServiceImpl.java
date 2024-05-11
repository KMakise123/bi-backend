package com.hjh.bibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.gson.Gson;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.common.constant.UserConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.JwtTokenDto;
import com.hjh.bibackend.model.vo.UserAuthVo;
import com.hjh.bibackend.model.vo.user.UserVo;
import com.hjh.bibackend.service.LoginService;
import com.hjh.bibackend.service.UserService;
import com.hjh.bibackend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Resource
    private JwtServiceImpl jwtService;


    /**
     *校验手机登录
     * 1.判断验证码是否过期
     * 2.验证码没有过期，再用验证码查询手机号，判断是否一致
     * 3.不一致则 抛出异常
     * 4.一致则返回token
     * 5.并清除这个KEY,防止用户重复调用
     * */
    @Override
    public UserAuthVo phoneLogin(String phone, String code){
        if(!isPhoneCodeExpired(phone,code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码不正确或过期");
        }
        LambdaUpdateWrapper<User> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(User::getPhone,phone);
        List<User> userList = userService.list(queryWrapper);
        if(userList==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该手机号未注册");
        }
        User user = userList.get(0);

        //更新登录时间
        user.setLoginTime(DateUtil.date());
        boolean res = userService.updateById(user);
        ThrowUtils.throwIf(!res,new BusinessException(ErrorCode.SYSTEM_ERROR,"更新登录时间失败"));

        String token = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        JwtTokenDto jwtToken = new JwtTokenDto(token, refreshToken);
        jwtService.save2Redis(jwtToken, user);
        stringRedisTemplate.delete(UserConstant.PHONE_CODE_PREFIX + code);
        UserAuthVo userAuthVo = new UserAuthVo();
        UserVo userVo = BeanUtil.copyProperties(user,UserVo.class);
        userAuthVo.setUserInfo(gson.toJson(userVo));
        userAuthVo.setToken(token);
        return userAuthVo;
    }

    /**
     * 校验验证码是否通过
     * 1.先判断验证码是否存在
     * 2.判断是否过期
     * 3.若过期就返回一个false
     * 4.若没有过期，则再次检查手机号码是否匹配
     * 5.成功则返回true
     * */
    public boolean isPhoneCodeExpired(String phone ,String code){
        String key = UserConstant.PHONE_CODE_PREFIX + code;
        boolean exit = stringRedisTemplate.hasKey(key);
        if(!exit)return false;
        long expired = stringRedisTemplate.getExpire(key,TimeUnit.SECONDS);
        if(expired<0)return false;
        String phoneStore = stringRedisTemplate.opsForValue().get(key);
        if(!phoneStore.equals(phone))return false;
        return true;
    }

    /**
     * 发送验证码
     * 要先查明存不存在该号码
     * 1.生成验证码
     * 2.将(key:验证码，value:手机号)放入redis当中
     * 3.设置好过期时间1分钟
     * */
    @Override
    public String createPhoneCode(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone",phone);
        if(userService.list(queryWrapper)==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该手机号未注册");
        }
        String code = RandomUtil.randomNumbers(6);
        String key = UserConstant.PHONE_CODE_PREFIX + code;
        stringRedisTemplate.opsForValue().set(key,phone,60, TimeUnit.SECONDS);
        return code;
    }

    /**
     * 账号登录
     * */
    //@Transactional(isolation = Isolation.SERIALIZABLE) //解决幻读
    @Override
    public UserAuthVo accountLogin(String userAccount, String userPassword) {
        String entryPassword = SecureUtil.md5(userPassword + UserConstant.PASSWORD_SALT);
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",userAccount);
        queryWrapper.eq("password",entryPassword);
        User user = userService.getOne(queryWrapper);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该账号不存在");
        }

        //更新登录时间
        user.setLoginTime(DateUtil.date());
        boolean res = userService.updateById(user);
        ThrowUtils.throwIf(!res,new BusinessException(ErrorCode.SYSTEM_ERROR,"更新登录时间失败"));


        String token = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        JwtTokenDto jwtToken = new JwtTokenDto(token, refreshToken);
        jwtService.save2Redis(jwtToken, user);
        UserAuthVo userAuthVo = new UserAuthVo();
        UserVo userVo = BeanUtil.copyProperties(user,UserVo.class);
        userAuthVo.setUserInfo(gson.toJson(userVo));
        userAuthVo.setToken(token);
        return userAuthVo;
    }

    /**
     * 注册
     * */
    @Override
    public User doRegister(String userAccount, String userPassword, String checkPassword,String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",userAccount);
        //若不唯一就注册失败
        if(userService.count(queryWrapper) >= 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不唯一");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不一致");
        }
        String entryPassword = SecureUtil.md5(userPassword + UserConstant.PASSWORD_SALT);
        User user = new User();
        user.setAccount(userAccount);
        user.setPassword(entryPassword);
        user.setUsername("user-" + UUID.randomUUID().toString().substring(0, 10));
        user.setPhone(phone);
        boolean res = userService.save(user);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
        }
        return user;
    }
}
