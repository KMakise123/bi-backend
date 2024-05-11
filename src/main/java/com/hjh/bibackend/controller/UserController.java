package com.hjh.bibackend.controller;

import com.hjh.bibackend.common.BaseResponse;
import com.hjh.bibackend.model.query.StatusQuery;
import com.hjh.bibackend.model.query.userQuery.PwdQuery;
import com.hjh.bibackend.model.query.userQuery.UserSelectQuery;
import com.hjh.bibackend.model.vo.user.UserAddVo;
import com.hjh.bibackend.model.vo.user.UserVo;
import com.hjh.bibackend.service.UserService;
import com.hjh.bibackend.utils.ResponseUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;


@Slf4j
@RequestMapping("/user")
@RestController
@ResponseBody
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/update/status")
    public BaseResponse<Boolean> updateUserStatus(@RequestBody @Validated StatusQuery statusQuery){
        Long userId = statusQuery.getId();
        Integer userStatus = statusQuery.getStatus();
        boolean result = userService.updateStatus(userId,userStatus);
        return ResponseUtils.success(result);
    }

    //查询用户
    @PostMapping("/list")
    public BaseResponse<List<UserVo>> getUsersList(@RequestBody @Validated UserSelectQuery userSelectQuery){
        List<UserVo> userList = userService.getUserListByKeyWord(userSelectQuery);
        return ResponseUtils.success(userList);
    }

    //统计总共有多少个用户
    @PostMapping("/total")
    public BaseResponse<Long> UserNumber(@RequestBody @Validated UserSelectQuery userSelectQuery){
        Long totalUsers = userService.countTotalUsers(userSelectQuery);
        return ResponseUtils.success(totalUsers);
    }

    //更新密码
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody @Validated PwdQuery pwdQuery){
        Long userId = pwdQuery.getId();
        String userPassword = pwdQuery.getPassword();
        boolean result = userService.updateUserPwdById(userId,userPassword);
        return ResponseUtils.success(result);
    }


    //修改
    //@RoleCheck(mustRole = UserConstant.ADMIN)
    @PostMapping("/update")
    public BaseResponse<UserVo> updateUser(@RequestBody @Validated UserVo newUserVo){
        UserVo userVo = userService.updateUser(newUserVo);
        return ResponseUtils.success(userVo);
    }

    //删除
    //@RoleCheck(mustRole = UserConstant.ADMIN)
    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteUserById(@RequestParam(required = true) @Validated @NotNull(message = "userId为空") Long userId){
        boolean result =  userService.deleteUser(userId);
        return ResponseUtils.success(result);
    }

    @PostMapping("/delete/list")
    public BaseResponse<Boolean> deleteUsersById(@RequestBody @Validated @NotNull(message = "ids为空") List<Long> ids){
        boolean result = userService.deleteUsers(ids);
        return ResponseUtils.success(result);
    }

    //增加
    //@RoleCheck(mustRole = UserConstant.ADMIN)
    @PostMapping("/add")
    public BaseResponse<Boolean> addUser(@RequestBody @Validated UserAddVo userAddVo){
        System.out.println(userAddVo);
        boolean result = userService.addUser(userAddVo);
        return ResponseUtils.success(result);
    }

    //退出
    @GetMapping("/logout")
    public BaseResponse<Boolean> logOut(){
        userService.logOut();
        return ResponseUtils.success(true);
    }

}
