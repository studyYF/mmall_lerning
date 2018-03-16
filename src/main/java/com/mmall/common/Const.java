package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by yangfan on 2017/9/6.
 */
public class Const {

    public static final String CURRENT_USER = "current_user";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";


    // 用户角色
    public interface Role{
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    //产品是否在线销售的枚举
    public enum ProductStatusEnum{

        ON_SALE("在线",1);
        private String value;
        private int code;

        ProductStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    // 产品列表排序方式
    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    // 购物车中是否选中的状态
    public interface Cart {
        Integer CHECK = 1;//选中
        Integer UN_CHECK = 0;// 未选中

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";// 表示库存不可以满足购买需求，调整个数到最大库存
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";//表示库存可以满足购买需求
    }

    /**
     * 订单状态
     */
    public enum OrderStatusEnum {

        //取消
        CANCELED(0,"已取消"),
        //未支付
        NO_PAY(10,"未支付"),
        //已支付
        PAIED(20,"已支付"),
        //已发货
        SHIPPED(40,"已发货"),
        //订单完成
        ORDER_SUCCESS(50,"订单完成"),
        //订单关闭
        ORDER_CLOSE(60,"订单关闭");
        OrderStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }


        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum orderStatusEnum : values()) {
                if (code == orderStatusEnum.getCode()) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }


    /**
     * 支付宝状态返回常量
     */
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }


    /**
     * 支付平台枚举
     */
    public enum PayPlatformEnum{

        ALIPAY(1,"支付宝");

        private int code;
        private String value;

        PayPlatformEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {

            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }


    }

    // 支付方式
    public enum PayTypeEnum {

        ONLINE(1,"在线支付");
        private int code;
        private String value;

        PayTypeEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {

            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static PayTypeEnum codeOf(int code) {
            for (PayTypeEnum payTypeEnumItem : values()) {
                if (code == payTypeEnumItem.getCode()) {
                    return payTypeEnumItem;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }




}
