package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Created by yangfan on 2017/9/6.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createErrorByErrorMessage("用户名不存在");
        }

        // 密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if (user == null){
            return ServerResponse.createErrorByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //校验用户名是否已经存在
        ServerResponse response = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!response.isSuccess()) {
            return ServerResponse.createErrorByErrorMessage("用户名已经存在");
        }
        //校验email是否已经存在
        response = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!response.isSuccess()) {
            return ServerResponse.createErrorByErrorMessage("email已存在");
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {//该result表示插入的行数,如果为0,说明没有插入成功
            return ServerResponse.createErrorByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {//判断type是否为空,当他是" "时,也返回true
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0){
                    return ServerResponse.createErrorByErrorMessage("email已存在");
                }
            }
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createErrorByErrorMessage("用户名存在");
                }
            }
        } else {
            return ServerResponse.createErrorByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        //判断用户名是否存在
        ServerResponse response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createErrorByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestion(username);
        if (question != null) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createErrorByErrorMessage("该用户的密保问题为空");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createBySuccessMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String token) {
        //1.判断token是否为空
        if (!StringUtils.isNotBlank(token)) {
            return ServerResponse.createErrorByErrorMessage("token为空,需要传递");
        }
        //2.判断用户名是否存在
        ServerResponse response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createErrorByErrorMessage("用户名不存在");
        }
        //3.获取缓存的token
        String forgetToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (forgetToken == null) {
            return ServerResponse.createErrorByErrorMessage("token已经过期");
        }
        //4.判断两个token是否相等
        if (StringUtils.equals(token, forgetToken)) {
            String passMd5 = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultRow = userMapper.updatePassword(username, passMd5);
            if (resultRow > 0) {
                return ServerResponse.createBySuccess("更新密码成功");
            }
        } else {
            return ServerResponse.createErrorByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createErrorByErrorMessage("更新密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String username, String passwordOld, String passwordNew) {
        //1.判断旧密码是否正确
        String password = userMapper.selectPassword(username);
        if (!StringUtils.equals(password, MD5Util.MD5EncodeUtf8(passwordOld))) {
            return ServerResponse.createErrorByErrorMessage("旧密码错误");
        }
        //2.判断新密码是否为空
        if (!StringUtils.isNotBlank(passwordNew)) {
            return ServerResponse.createBySuccessMessage("密码不能为空");
        }
        //3.重置密码
        int resultRow = userMapper.updatePassword(username, MD5Util.MD5EncodeUtf8(passwordNew));
        if (resultRow > 0) {
            return ServerResponse.createBySuccess("更新密码成功");
        }
        return ServerResponse.createErrorByErrorMessage("更新密码错误");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        //1.判断用户email是否存在
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount > 0) {
            return ServerResponse.createErrorByErrorMessage("email已经存在,请重新输入");
        }
        //2.创建新
        // 的user对象,将需要修改的字段赋值
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPhone(user.getPhone());
        newUser.setId(user.getId());
        newUser.setQuestion(user.getQuestion());
        newUser.setAnswer(user.getAnswer());
        resultCount = userMapper.updateByPrimaryKeySelective(newUser);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("更新用户信息成功",newUser);
        }
        return ServerResponse.createErrorByErrorMessage("更新用户信息失败");
    }

    @Override
    public ServerResponse<User> getUserInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user != null) {
            user.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccess("获取用户信息成功", user);
        }
        return ServerResponse.createErrorByErrorMessage("获取用户信息失败");
    }

    @Override
    public ServerResponse<String> validateUserRole(User user) {
        if (user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createError();
    }
}
