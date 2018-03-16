package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yangf on 2018/3/12.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {


    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;


    /**
     * 创建订单
     * @param session session
     * @param shippingId 送货地址id
     * @return 结果
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session,Integer shippingId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.createOrder(user.getId(),shippingId);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
    }


    /**
     * 取消订单
     * @param session session
     * @param orderNo 订单号
     * @return 结果
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.cancelOrder(user.getId(),orderNo);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
    }


    /**
     * 获取用户购物车中所有选中的商品
     * @param session session
     * @return 订单vo
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.getCartOrderProduct(user.getId());
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 获取用户某个订单详情
     * @param session session
     * @param orderNo 订单号
     * @return 订单vo
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session,long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.orderDetail(user.getId(),orderNo);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 获取用户所有的订单列表
     * @param session session
     * @return 列表
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.orderList(user.getId(),pageNum,pageSize);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
    }








    /**
     * 支付
     * @param session session
     * @param orderNo 订单号
     * @param request 请求,获取项目路径
     * @return 结果
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, long orderNo, HttpServletRequest request) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            String path = request.getServletContext().getRealPath("upload");
            return iOrderService.pay(user.getId(),orderNo,path);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());

    }

    /**
     * 支付宝回调接口
     * @param request 回调信息map
     * @return 结果
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map params = Maps.newHashMap();
        //1.获取回调参数
        Map<String,String[]> resultMap = request.getParameterMap();
        //2.遍历map,填充到params中
        for (Iterator iterator = resultMap.keySet().iterator();iterator.hasNext();){
            String key = (String)iterator.next();
            String values = "";
            String value[] = resultMap.get(key);
            for (int i = 0; i < value.length; i++) {
                values = (i == value.length - 1) ? values + value[i] : values + value[i] + ",";
            }
            params.put(key,values);
        }
        //3.打印信息
        logger.info("支付宝回调信息, sign:#{}, trade_status:#{},params:#{}",params.get("sign"),params.get("trade_status"),params.toString());
        //4.验证支付宝回调的正确性
        params.remove("sign_type");
        try {
            boolean alipaySignure = AlipaySignature.rsaCheckV2(params,Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if (!alipaySignure) {//验证失败
                return ServerResponse.createErrorByErrorMessage("非法请求");
            }
        } catch (AlipayApiException e) {
            logger.error("验证支付宝回调失败:",e);
        }
        //todo 验证各种数据

        ServerResponse response = iOrderService.alipayCallback(params);
        if (response.isSuccess()) {
            return Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;

    }

    /**
     * 查询订单信息
     * @param session session
     * @param orderNo 订单号
     * @return 结果
     */
    @RequestMapping("query_order_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderStatus(HttpSession session, long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            ServerResponse response = iOrderService.queryOrderStatus(user.getId(), orderNo);
            if (response.isSuccess()) {
                return ServerResponse.createBySuccess(true);
            }
            return ServerResponse.createBySuccess(false);
        }
        return  ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
    }
}
