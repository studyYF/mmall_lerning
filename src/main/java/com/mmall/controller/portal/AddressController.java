package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yangf on 2018/2/28.
 */
@Controller
@RequestMapping("/shipping/")
public class AddressController {

    @Autowired
    private IAddressService iAddressService;

    /**
     * 获取地址列表
     * @param session session
     * @return 该用户的地址列表
     */
    @RequestMapping(value = "list_address.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> getAddressList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "10")int pageNum,@RequestParam(value = "pageSize",defaultValue = "1")int pageSize) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iAddressService.list(user.getId(),pageNum,pageSize);
    }

    /**
     * 添加地址
     * @param session session
     * @param shipping 地址pojo
     * @return 该用户的地址列表
     */
    @RequestMapping(value = "add_address.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addAddress(HttpSession session, Shipping shipping) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iAddressService.addAddress(user.getId(), shipping);
    }

    /**
     * 删除地址
     * @param session session
     * @param shippingId 地址id
     * @return 该用户的地址列表
     */
    @RequestMapping(value = "delete_address.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addAddress(HttpSession session, Integer shippingId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iAddressService.deleteAddressById(user.getId(),shippingId);
    }

    /**
     * 更新地址
     * @param session session
     * @param shipping 地址id
     * @return 该用户的地址列表
     */
    @RequestMapping(value = "update_address.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse updateAddress(HttpSession session, Shipping shipping) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iAddressService.updateAddress(user.getId(),shipping);
    }

    /**
     * 获取地址详情
     * @param session session
     * @param shippingId 地址id
     * @return 地址shipping
     */
    @RequestMapping(value = "detail_address.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getShipping(HttpSession session, Integer shippingId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iAddressService.getAddressById(user.getId(),shippingId);
    }

}

