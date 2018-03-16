package com.mmall.controller.backen;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yangf on 2018/3/15.
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IOrderService iOrderService;

    //1.所有订单列表

    /**
     * 查询所有订单
     * @param session session
     * @param pageNum 页数
     * @param pageSize 每页个数
     * @return 结果
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        if (!iUserService.validateUserRole(user).isSuccess()) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
        }
        return iOrderService.manageOrderList(pageNum,pageSize);
    }

    //2.订单详情
    /**
     * 获取订单详情
     * @param session session
     * @param orderNo 订单号
     * @return 结果
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session,Long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        if (!iUserService.validateUserRole(user).isSuccess()) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
        }
        return iOrderService.manageOrderDetail(orderNo);
    }
    //3.搜索订单

    /**
     * 搜索订单
     * @param session session
     * @param orderNo 订单号
     * @param pageNum 页数
     * @param pageSize 每页个数
     * @return 结果
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse searchOrder(HttpSession session,Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1")int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        if (!iUserService.validateUserRole(user).isSuccess()) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
        }
        return iOrderService.manageSearchOrder(orderNo,pageNum,pageSize);
    }

    //4.发货

    /**
     * 发货
     * @param session session
     * @param orderNo 订单号
     * @return 发货是否成功
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> sendGoods(HttpSession session,Long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        if (!iUserService.validateUserRole(user).isSuccess()) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
        }
        return iOrderService.manageSendGoods(orderNo);
    }
}
