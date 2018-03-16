package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * Created by yangf on 2018/3/12.
 */
public interface IOrderService {

    /**
     * 支付
     * @param userId 用户id
     * @param orderNo 订单号
     * @param path 二维码路径
     * @return 包含二维码信息的map
     */
    ServerResponse pay(Integer userId, long orderNo, String path);

    /**
     * 验证信息
     * @param params 参数
     * @return 返回结果
     */
    ServerResponse alipayCallback(Map<String,String> params);

    /**
     * 查询订单信息
     * @param userId 用户id
     * @param orderNo 订单号
     * @return 结果
     */
    ServerResponse queryOrderStatus(Integer userId, long orderNo);


    /**
     * 创建订单
     * @param userId 用户id
     * @param shippingId 地址id
     * @return 结果
     */
    ServerResponse createOrder(Integer userId, Integer shippingId);

    /**
     * 取消订单
     * @param userId 用户id
     * @param orderNo 订单号
     * @return 结果
     */
    ServerResponse<String> cancelOrder(Integer userId, long orderNo);

    /**
     * 获取所有订单
     * @param userId 用户id
     * @return 订单vo
     */
    ServerResponse getCartOrderProduct(Integer userId);

    /**
     * 获取订单详情
     * @param userId 用户id
     * @param orderNo 订单号
     * @return orderVo
     */
    ServerResponse orderDetail(Integer userId, long orderNo);

    /**
     * 获取用户订单列表
     * @param userId 用户id
     * @return 列表
     */
    ServerResponse<PageInfo> orderList(Integer userId,int pageNum, int pageSize);

    //backend 后台管理

    /**
     * 获取所有订单列表
     * @param pageNum 页数
     * @param pageSize 每页个数
     * @return 分页数据
     */
    ServerResponse manageOrderList(int pageNum, int pageSize);

    /**
     * 查询订单详情
     * @param orderNo 订单号
     * @return orderVo
     */
    ServerResponse manageOrderDetail(Long orderNo);

    /**
     * 搜索订单 分页是为了扩展使用
     * @param orderNo 订单号
     * @param pageNum 页数
     * @param pageSize 每页个数
     * @return 分页数据
     */
    ServerResponse manageSearchOrder(Long orderNo,int pageNum, int pageSize);

    /**
     * 发货
     * @param orderNo 订单号
     * @return 发货是否成功
     */
    ServerResponse<String> manageSendGoods(Long orderNo);
}
