package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yangf on 2018/2/27.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;


    /**
     * 获取购物车商品列表
     * @param session session
     * @return 商品列表
     */
    @RequestMapping(value = "cart_list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> cartList(HttpSession session) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.cartList(user.getId());
    }

    /**
     * 添加到购物车
     * @param session session
     * @param count 数量
     * @param productId 产品id
     * @return 返回
     */
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session, Integer count, Integer productId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }

    /**
     * 更新购物车
     * @param session session
     * @param productId 产品id
     * @param count 数量
     * @return 返回购物车list
     */
    @RequestMapping(value = "update_cart.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> updateCart(HttpSession session, Integer productId, Integer count) {
        //1.判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.updateCart(user.getId(),productId,count);
    }

    /**
     * 删除购物车中的商品
     * @param session session
     * @param productIds 产品id 用 "," 拼接
     * @return 返回购物车list
     */
    @RequestMapping(value = "delete_cart.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> deleteCart(HttpSession session, String productIds) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteCartByProductIds(user.getId(),productIds);
    }

    /**
     * 全选购物车
     * @param session session
     * @return 列表
     */
    @RequestMapping(value = "select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectAllProduct(HttpSession session) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectProduct(user.getId(),null,Const.Cart.CHECK);
    }

    /**
     * 全不选购物车
     * @param session session
     * @return 列表
     */
    @RequestMapping(value = "un_select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectAllProduct(HttpSession session) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectProduct(user.getId(),null,Const.Cart.UN_CHECK);
    }

    /**
     * 单选购物车某个产品
     * @param session session
     * @return 列表
     */
    @RequestMapping(value = "select_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectProduct(HttpSession session, Integer productId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectProduct(user.getId(),productId,Const.Cart.CHECK);
    }

    /**
     * 单选购物车某个产品
     * @param session session
     * @return 列表
     */
    @RequestMapping(value = "un_select_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectProduct(HttpSession session, Integer productId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectProduct(user.getId(),productId,Const.Cart.UN_CHECK);
    }


    /**
     * 获取购物车中所有商品的总数
     * @param session session
     * @return list
     */
    @RequestMapping(value = "get_allProduct_count.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> getAllProductCount(HttpSession session) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getAllProductCount(user.getId());
    }




}
