package com.yang.usercenter.service;

import com.yang.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-07-28 16:56:26
*/
public interface UserService extends IService<User> {


    /**
     * 账号注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 第二次输入密码
     * @return 注册结果
     */
    long userRegister(String userAccount, String userPassword,String checkPassword,String planetCode);

    /**
     * 账号登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用戶脫敏
     * @param user 脫敏用戶
     * @return 返回類型為User
     */
    User getSafetyUser(User user);

    /**
     * 用户注销
     * @return 返回类型为整型
     */
    Integer userLogout(HttpServletRequest request);


    /**
     * 根据标签查询用户
     * @param tagNameLists 用户传递的标签
     * @return
     */
     List<User> searchUserByTags(List<String> tagNameLists);


    /**
     * 获取当前登录的用户信息
     * @param request
     * @return
     */
     User getCurrentLoginUser(HttpServletRequest request);


    /**
     * 更新用户信息
     * @param user
     * @param userLogin
     * @return
     */
     int updateUser(User user,User userLogin);

    /**
     * 根据请求判断是否是管理员请求的
     * @param request
     * @return
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 判断该用户是否管理员
     * @param user
     * @return
     */
     boolean isAdmin(User user);

    /**
     * 推荐用户查询
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    List<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);

    List<User> matchUsers(int num, User loginUser);
}
