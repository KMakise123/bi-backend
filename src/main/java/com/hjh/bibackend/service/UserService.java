package com.hjh.bibackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.UserDto;
import com.hjh.bibackend.model.query.PageQuery;
import com.hjh.bibackend.model.query.userQuery.UserSelectQuery;
import com.hjh.bibackend.model.vo.user.UserAddVo;
import com.hjh.bibackend.model.vo.user.UserVo;

import java.util.List;

/**
* @author HIKI
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-04-25 12:04:01
*/
public interface UserService extends IService<User> {
    /**
     * 更新用户状态
     * */
    public boolean updateStatus(Long userId, Integer status);

    /**
     *通过Id获取用户
     * */
    public UserVo getUserById(Long id);

    /**
     * 获取当前登录的用户
     * */
    public UserVo getCurrentUser();

    /**
     * 退出登录
     * 1.验证该用户的UserID与参数UserId一致
     * 2.执行删除令牌
     * */
    public void logOut();

    /**
     * 修改用户
     * 返回影响行数
     * */
    public UserVo updateUser(UserVo userVo);

    /**
     * 删除用户
     * @param id
     * @return
     */
    public boolean deleteUser(Long id);

    /**
     * 批量删除
     * @param ids
     * @return
     */
    public boolean deleteUsers(List<Long> ids);

    /**
     * 添加用户
     * @param userAddVo
     * @return
     */
    public boolean addUser(UserAddVo userAddVo);

    /**
     * 统计当前条件下的记录数目
     * @param userSelectQuery
     * @return
     */
    public long countTotalUsers(UserSelectQuery userSelectQuery);

    /**
     * 查询用户,无条件
     * */
    public List<UserVo> getUserList(PageQuery pageQuery);

    /**
     * 查询用户，有条件
     * @param userSelectQuery
     * @return
     */
    public List<UserVo> getUserListByKeyWord(UserSelectQuery userSelectQuery);

    /**
     * 修改密码
     * */
    public void updateUserPwd(String password);

    /**
     * 重置用户密码
     * */
    public boolean updateUserPwdById(Long id,String password);

    /**
     * 判断是否为管理员
     * @param userDto
     * @return
     */
    public boolean isAdmin(UserDto userDto);
}
