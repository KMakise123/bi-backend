package com.hjh.bibackend.service;


import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.vo.UserAuthVo;

public interface LoginService {
    public UserAuthVo accountLogin(String userAccount, String password);

    public User doRegister(String userAccount, String password, String checkPassword, String phone);

    public String createPhoneCode(String phone);

    public UserAuthVo phoneLogin(String phone,String code);
}
