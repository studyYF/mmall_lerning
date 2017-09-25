package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

/**
 * Created by yangfan on 2017/9/6.
 */
public interface IUserService {
    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    ServerResponse<User> login(String username, String password);

    /**
     * 注册
     * @param user
     * @return
     */
    ServerResponse<String> register(User user);

    /**
     * 校验用户名和密码
     * @param str
     * @param type
     * @return
     */
    ServerResponse<String> checkValid(String str, String type);

    /**
     * 获取用户密保问题
     * @param username
     * @return
     */
    ServerResponse<String> selectQuestion(String username);

    /**
     * 校验用户密保问题答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    ServerResponse<String> checkAnswer(String username, String question, String answer);

    /**
     * 重置密码
     * @param username
     * @param passwordNew
     * @param token
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String token);

    /**
     * 重置密码,登录状态下
     * @param username
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    ServerResponse<String> resetPassword(String username, String passwordOld, String passwordNew);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    ServerResponse<User> updateUserInfo(User user);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    ServerResponse<User> getUserInformation(Integer userId);

    /**
     * 校验用户是否为管理员
     * @param user
     * @return
     */
    ServerResponse<String> validateUserRole(User user);
}


