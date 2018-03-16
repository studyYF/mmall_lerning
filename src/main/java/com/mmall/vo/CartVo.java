package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by yangf on 2018/2/27.
 */
public class CartVo {

    private List<CartProductVo> cartProductVoList;//产品列表
    private BigDecimal totalPrice;//总价
    private Boolean allChecked;//是否全选
    private String imageHost;//图片地址

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
