package com.hjh.bibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjh.bibackend.common.constant.UserConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.UserDto;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.model.query.PageQuery;
import com.hjh.bibackend.model.query.userQuery.UserSelectQuery;
import com.hjh.bibackend.model.vo.user.UserAddVo;
import com.hjh.bibackend.model.vo.user.UserVo;
import com.hjh.bibackend.service.JwtService;
import com.hjh.bibackend.service.UserService;
import com.hjh.bibackend.mapper.UserMapper;
import com.hjh.bibackend.utils.FieldUtils;
import com.hjh.bibackend.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author HIKI
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-04-25 12:04:01
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Resource
    UserMapper userMapper;

    @Resource
    JwtService jwtService;

    /**
     * 更新用户状态
     * */
    @Override
    public boolean updateStatus(Long userId, Integer status) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        User user = getById(userId);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户不存在");
        }
        updateWrapper.set("status",status);
        boolean res = update(updateWrapper);
        if(!res)throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return res;
    }

    /**
     *通过Id获取用户
     * */
    @Override
    public UserVo getUserById(Long id) {
        User user = userMapper.selectById(id);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户不存在");
        }
        return BeanUtil.copyProperties(user,UserVo.class);
    }

    /**
     * 获取当前登录的用户
     * */
    @Override
    public UserVo getCurrentUser() {
        UserDto userDto = UserHolder.getUser();
        User user = userMapper.selectById(userDto.getId());
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户不存在");
        }
        return BeanUtil.copyProperties(user,UserVo.class);
    }

    /**
     * 退出登录
     * 1.验证该用户的UserID与参数UserId一致
     * 2.执行删除令牌
     * */
    @Override
    public void logOut() {
        UserDto userDto = UserHolder.getUser();
        if(userDto==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        jwtService.revokeTokenById(userDto.getId());
    }

    /**
     * 重置用户密码
     * */
    @Override
    public boolean updateUserPwdById(Long id,String password) {
        User user = getById(id);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户不存在");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id).set("password", SecureUtil.md5(password+UserConstant.PASSWORD_SALT));
        boolean res = update(updateWrapper);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return res;
    }

    /**
     * 修改用户
     * 返回影响行数
     * */
    @Override
    public UserVo updateUser(UserVo userVo) {
        User user = getById(userVo.getId());
        if(user==null){
            throw new BusinessException(ErrorCode.ERROR_ROLE,"用户不存在");
        }
        String[] fields = {"account","username","phone","email","userRole","avatarUrl"};
        FieldUtils.updatePartOfFields(userVo,user,fields);
        boolean res = updateById(user);
        if(!res)throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        return getUserById(user.getId());
    }

    /**
     * 删除用户
     * @param id
     * @return
     */
    @Override
    public boolean deleteUser(Long id){
        User user = getById(id);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        return removeById(id);
    }


    /**
     * 批量删除
     * @param ids
     * @return
     */
    @Override
    public boolean deleteUsers(List<Long> ids){
        return removeByIds(ids);
    }

    /**
     * 添加用户
     * @param userAddVo
     * @return
     */
    @Override
    public boolean addUser(UserAddVo userAddVo){
        User user = new User();
        user.setPassword(SecureUtil.md5(userAddVo.getPassword()+UserConstant.PASSWORD_SALT));
        boolean res = save(BeanUtil.copyProperties(userAddVo,User.class));
        return res;
    }

    /**
     * 统计当前条件下的记录数目
     * @param userSelectQuery
     * @return
     */
    @Override
    public long countTotalUsers(UserSelectQuery userSelectQuery){
        PageQuery pageQuery = userSelectQuery.getPageQuery();
        String account = userSelectQuery.getAccount();
        String username = userSelectQuery.getUsername();
        String userRole = userSelectQuery.getUserRole();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("account",account).like("username",username).like("user_Role",userRole);
        return count(queryWrapper);
    }

    /**
     * 查询用户,无条件
     * */
    @Override
    public List<UserVo> getUserList(PageQuery pageQuery) {
        Long currentPage = pageQuery.getCurrentPage();
        Long pageSize = pageQuery.getPageSize();
        Page<User> page = new Page<>(currentPage,pageSize);
        IPage<User> iPage = userMapper.selectPage(page,null);
        List<User> userList = iPage.getRecords();
        List<UserVo> userVoList = userList.stream().map(item ->{
            return BeanUtil.copyProperties(item,UserVo.class);
        }).collect(Collectors.toList());
        return userVoList;
    }

    /**
     * 查询用户，有条件
     * @param userSelectQuery
     * @return
     */
    @Override
    public List<UserVo> getUserListByKeyWord(UserSelectQuery userSelectQuery){
        PageQuery pageQuery = userSelectQuery.getPageQuery();
        String account = userSelectQuery.getAccount();
        String username = userSelectQuery.getUsername();
        String userRole = userSelectQuery.getUserRole();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> page = new Page<>(pageQuery.getCurrentPage(),pageQuery.getPageSize());
        queryWrapper.like("account",account).like("username",username).like("user_Role",userRole);
        IPage<User> iPage = userMapper.selectPage(page,queryWrapper);
        List<User> userList = iPage.getRecords();
        List<UserVo> userVoList = userList.stream().map(item ->{
            return BeanUtil.copyProperties(item,UserVo.class);
        }).collect(Collectors.toList());
        return userVoList;
    }

    /**
     * 修改密码
     * */
    @Override
    public void updateUserPwd(String password) {
        UserDto userDto = UserHolder.getUser();
        String handlerPassword = SecureUtil.md5(password + UserConstant.PASSWORD_SALT);
        boolean update = update().set("password", handlerPassword).eq("id", userDto.getId()).update();
        if(!update)throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新密码失败");
    }

    @Override
    public boolean isAdmin(UserDto userDto){
        String userRole = userDto.getUserRole();
        if(userRole.equals(UserConstant.ADMIN)){
            return true;
        }
        return false;
    }
}
