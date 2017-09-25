package com.mmall.controller.backen;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yangfan on 2017/9/7.
 */
@Controller
@RequestMapping(value = "/managerBack/user")
public class UserManagerController {

    @Autowired
    private IUserService iUserService;

    /**
     * 后台登录
     * @param username 用户名
     * @param password 密码
     * @return 登录是否成功
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password) {
        ServerResponse<User> serverResponse = iUserService.login(username, password);
        if (serverResponse.isSuccess()) {
            User user = serverResponse.getData();
            if (user.getRole().equals(Const.Role.ROLE_ADMIN)) {
                return serverResponse;
            } else {
                return ServerResponse.createErrorByErrorMessage("不是管理员,无法登录");
            }
        } else {
            return serverResponse;
        }
    }
}
