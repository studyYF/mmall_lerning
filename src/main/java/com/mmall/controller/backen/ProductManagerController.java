package com.mmall.controller.backen;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yangfan on 2017/9/25.
 */
@Controller
@RequestMapping(value = "/manage/product")
public class ProductManagerController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;

    /**
     * 更新或新增产品
     * @param session session
     * @param product 产品对象
     * @return 是否成功
     */
    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse saveProduct(HttpSession session, Product product) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.validateUserRole(user).isSuccess()) {
                return iProductService.saveOrUpdateProduct(product);
            }
            return ServerResponse.createErrorByErrorMessage("没有权限，需要管理员权限");
        }
        return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"需要登录");
    }
}
