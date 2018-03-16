package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by yangf on 2018/3/12.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private final static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    @Override
    public ServerResponse pay(Integer userId, long orderNo, String path) {
        Map<String, String> resultMap = new HashMap<>();
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
        if (order != null) {
            resultMap.put("orderNo", String.valueOf(order.getOrderNo()));
            // 需保证商户系统端不能重复，建议通过数据库sequence生成，
            String outTradeNo = order.getOrderNo().toString();

            // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
            String subject = new StringBuilder().append("happymmall订单号").append(outTradeNo).toString();

            // (必填) 订单总金额，单位为元，不能超过1亿元
            // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
            String totalAmount = order.getPayment().toString();

            // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
            // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
            String undiscountableAmount = "0";

            // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
            // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
            String sellerId = "";

            // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
            String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).toString();

            // 商户操作员编号，添加此参数可以为商户操作员做销售统计
            String operatorId = "test_operator_id";

            // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
            String storeId = "test_store_id";

            // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
            ExtendParams extendParams = new ExtendParams();
            extendParams.setSysServiceProviderId("2088100200300400500");

            // 支付超时，定义为120分钟
            String timeoutExpress = "120m";

            // 商品明细列表，需填写购买商品详细信息，
            List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId,orderNo);

            for (OrderItem orderItem : orderItemList) {
                GoodsDetail good = GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(), BigDecimalUtil.multiply(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),orderItem.getQuantity());
                goodsDetailList.add(good);
            }

            /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
             *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
             */
            Configs.init("zfbinfo.properties");

            /** 使用Configs提供的默认参数
             *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
             */
            AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

            // 创建扫码支付请求builder，设置请求参数
            AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                    .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                    .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                    .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                    .setTimeoutExpress(timeoutExpress)
                    .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                    .setGoodsDetailList(goodsDetailList);

            AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
            switch (result.getTradeStatus()) {
                case SUCCESS:
                    logger.info("支付宝预下单成功: )");

                    AlipayTradePrecreateResponse response = result.getResponse();
                    dumpResponse(response);

                    File folder = new File(path);
                    if (!folder.exists()) {
                        folder.setWritable(true);
                        folder.mkdirs();
                    }
                    // 需要修改为运行机器上的路径

                    //1.创建二维码生成的路径
                    String qrPath = String.format(path + "/qr-%s.png",response.getOutTradeNo());
                    //2.创建二维码图片名称
                    String qrName = String.format("/qr-%s.png",response.getOutTradeNo());
                    //3.生成二维码
                    ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                    //4.上传二维码到FTP服务器
                    File targetFile = new File(qrPath);
                    try {
                        FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
                    } catch (IOException e) {
                        logger.error("上传二维码异常:",e);
                    }
                    //5.拼接二维码地址返回
                    String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + qrName;
                    resultMap.put("qrUrl",qrUrl);
                    logger.info("qrPath:" + qrPath);

                    return ServerResponse.createBySuccess(resultMap);

                case FAILED:
                    logger.error("支付宝预下单失败!!!");
                    return ServerResponse.createErrorByErrorMessage("支付宝预下单失败!!!");
                case UNKNOWN:
                    logger.error("系统异常，预下单状态未知!!!");
                    return ServerResponse.createErrorByErrorMessage("系统异常，预下单状态未知!!!");


                default:
                    logger.error("不支持的交易状态，交易返回异常!!!");
                    return ServerResponse.createErrorByErrorMessage("不支持的交易状态，交易返回异常!!!");
            }
        }
        return ServerResponse.createErrorByErrorMessage("用户没有该订单");
    }

    @Override
    public ServerResponse alipayCallback(Map<String,String> params) {
        //1.获取订单号
        long orderNo = Long.parseLong(params.get("out_order_no"));
        //2.获取支付宝订单号
        String tradeNo = params.get("trade_no");
        //3.获取支付宝订单状态
        String tradeStatus = params.get("trade_status");
        //4.查询数据库中的订单是否存在
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("不是快乐慕商城订单");
        }
        //5.判断数据库中订单状态 a.支付成功,发货,完成,取消,不做处理
        if (order.getStatus() >= Const.OrderStatusEnum.PAIED.getCode()) {
            return ServerResponse.createBySuccessMessage("重复订单");
        }
        //6.判断回调状态,success,更新当前订单状态
        if (tradeStatus.equals(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS)){
            order.setSendTime(DateTimeUtil.stringToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAIED.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //7.添加支付信息数据
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setUserId(order.getUserId());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse queryOrderStatus(Integer userId, long orderNo) {
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("没有该订单");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAIED.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createError();
    }

    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {

        //1.获取购物车中所有选中的商品
        List<Cart> cartList = cartMapper.selectCheckByUserId(userId);

        //2.计算总价
        //2.1获取orderItem数组
        ServerResponse response = this.getCartOrderItem(userId,cartList);
        if (!response.isSuccess()) {
            return response;
        }
        //2.2获取总价
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //3.生成订单
        Order order = this.assembleOrder(payment,userId,shippingId);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("生成订单失败");
        }
        //4.设置orderItemList列表中orderItem的订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //5.将orderItem批量插入到orderitem表中
        orderItemMapper.batchInsert(orderItemList);
        //6.减少库存
        this.reduceStock(orderItemList);
        //7.清空购物车
        this.cleanCart(cartList);
        //8.返回前端购物车列表 组装orderVo
        OrderVo orderVo = assembleOrderVo(orderItemList,order);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<String> cancelOrder(Integer userId, long orderNo) {
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("该用户没有该订单");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createErrorByErrorMessage("订单已经付款,不能取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        orderMapper.updateByPrimaryKeySelective(updateOrder);
        return ServerResponse.createBySuccess("取消成功");
    }

    @Override
    public ServerResponse getCartOrderProduct(Integer userId) {
        //1.获取购物车中的所有选中的商品
        List<Cart> cartList = cartMapper.selectCheckByUserId(userId);
        //2.根据购物车中的商品获取orderItem详情
        ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        //3.组装OrderProductVo
        BigDecimal payment = new BigDecimal("0");
        OrderProductVo orderProductVo = new OrderProductVo();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        return ServerResponse.createBySuccess(orderProductVo);
    }


    @Override
    public ServerResponse orderDetail(Integer userId, long orderNo) {
        //1.获取订单order
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("用户该订单不存在");
        }
        //2.获取订单item
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId,orderNo);
        //3.组装orderVo
        OrderVo orderVo = this.assembleOrderVo(orderItemList,order);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> orderList(Integer userId,int pageNum, int pageSize) {
        //1.根据用户id查询所有订单
        List<Order> orderList = orderMapper.selectOrderByUserId(userId);
        if (CollectionUtils.isEmpty(orderList)) {
            return ServerResponse.createErrorByErrorMessage("没有订单");
        }
        //2.组装ordervo的list
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageHelper.startPage(pageNum,pageSize);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /// 后台


    @Override
    public ServerResponse<PageInfo> manageOrderList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse manageOrderDetail(Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("没有此订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(orderItemList,order);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> manageSearchOrder(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("订单不存在");
        }
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(orderItemList,order);
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createErrorByErrorMessage("没有该订单");
        }
        if (order.getStatus() == Const.OrderStatusEnum.PAIED.getCode()) {
            order.setSendTime(new Date());
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createErrorByErrorMessage("订单未付款,不能发货");
    }


    /**
     * 组装OrderVolist
     * @param orderList orderList
     * @param userId 用户id
     * @return orderVo list
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList;
            //根据是否传入userId,判断是获取所有订单还是获取某用户的所有订单
            //1 获取orderItemList
            if (userId == null) {
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectByUserIdOrderNo(userId,order.getOrderNo());
            }
            //2 拼装orderVo
            OrderVo orderVo = this.assembleOrderVo(orderItemList,order);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 拼装orderVo
     * @param orderItemList orderItmeList
     * @param order order
     * @return orderVo
     */
    private OrderVo assembleOrderVo(List<OrderItem> orderItemList,Order order) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPayment(order.getPayment());
        orderVo.setShippingId(order.getShippingId());
        orderVo.setPaymentTime(DateTimeUtil.dateToString(order.getPaymentTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToString(order.getCloseTime()));
        orderVo.setEndTime(DateTimeUtil.dateToString(order.getEndTime()));
        orderVo.setSendTime(DateTimeUtil.dateToString(order.getSendTime()));
        orderVo.setPaymentTypeDes(Const.PayTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDes(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setPostage(order.getPostage());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        orderVo.setReviceName(shipping.getReceiverName());
        orderVo.setShippingVo(assembleShippingVo(shipping));
        List<OrderItemVo> orderItemVoList = assembleOrderItemList(orderItemList);
        orderVo.setOrderItemVoList(orderItemVoList);
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return orderVo;
    }

    /**
     * 组装orderitemVoList
     * @param orderItemList orderitemVoList
     * @return 组装orderitemVoList
     */
    private List<OrderItemVo> assembleOrderItemList(List<OrderItem> orderItemList) {
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }

    /**
     * 组装orderItemVo
     * @param orderItem orderItemVo
     * @return orderItemVo
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setCreateTime(DateTimeUtil.dateToString(orderItem.getCreateTime()));
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        return orderItemVo;
    }


    /**
     * 组装shippingVo
     * @param shipping shipping
     * @return shippingVo
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }


    /**
     * 清空购物车
     * @param cartList 购物车列表
     */
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 更新产品库存
     * @param orderItems orderitem
     */
    private void reduceStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }

    }

    /**
     * 生成订单
     * @param payment 总价
     * @param userId 用户id
     * @param shippingId 地址id
     * @return 订单
     */
    private Order assembleOrder(BigDecimal payment,Integer userId,Integer shippingId) {
        //1.生成订单号
        long orderNo = this.generateOrderNo();
        //2.组装order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPayment(payment);
        order.setOrderNo(orderNo);
        order.setPostage(0);
        order.setShippingId(shippingId);
        order.setPaymentType(Const.PayTypeEnum.ONLINE.getCode());
        int countRow = orderMapper.insert(order);
        if (countRow > 0) {
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     * @return 订单号
     */
    private long generateOrderNo() {
        //获取当前时间 + 取余10
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }







    /**
     * 根据cartlist创建orderItem list
     * @param userId 用户id
     * @param cartList cartlist
     * @return 结果
     */
    private ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        //1.判断购物车是否为空
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createErrorByErrorMessage("购物车为空");
        }
        //2.遍历cartlist 创建orderitem
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            //2.1 根据id获取product
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            //2.2 判断产品是否在销售
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createErrorByErrorMessage("产品"+ product.getName() + "不在销售状态");
            }
            //2.3 判断库存是否充足
            if (product.getStock() < cart.getQuantity()) {
                return ServerResponse.createErrorByErrorMessage("产品" + product.getName() + "库存不足");
            }
            //2.4 组装orderitem
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setProductName(product.getName());
            orderItem.setUserId(userId);
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(),cart.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }


    /**
     * 计算订单总价
     * @param orderItems 订单item列表
     * @return 订单总价
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItems) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItems) {
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
}
