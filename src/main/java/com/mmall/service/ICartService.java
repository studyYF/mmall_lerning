package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * Created by yangf on 2018/2/27.
 */
public interface ICartService {


    /**
     * 获取购物车商品列表
     * @param userId 用户id
     * @return 列表
     */
    ServerResponse<CartVo> cartList(Integer userId);

    /**
     *  添加商品到购物车
     * @param userId 用户id
     * @param productId 产品id
     * @param count 产品数量
     * @return 返回结果
     */
    ServerResponse add(Integer userId, Integer productId, Integer count);


    /**
     * 更新购物车商品数量
     * @param userId 用户id
     * @param productId 产品id
     * @param count 产品数量
     * @return cartVo
     */
    ServerResponse<CartVo> updateCart(Integer userId, Integer productId, Integer count);


    /**
     * 删除购物车
     * @param userId 用户id
     * @param productIds 产品id 用 ","拼接
     * @return cartVo
     */
    ServerResponse<CartVo> deleteCartByProductIds(Integer userId, String productIds);

    /**
     * 选中商品
     * @param userId 用户id
     * @param productId 产品id，如果不传的话，就是全选或者全不选
     * @param checked 是否选中
     * @return list
     */
    ServerResponse<CartVo> selectProduct(Integer userId, Integer productId, Integer checked);

    /**
     * 获取购物车商品总数
     * @param userId 用户id
     * @return list
     */
    ServerResponse<Integer> getAllProductCount(Integer userId);

}
