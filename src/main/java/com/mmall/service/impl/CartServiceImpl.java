package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by yangf on 2018/2/27.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;


    @Override
    public ServerResponse<CartVo> cartList(Integer userId) {
        CartVo cartVo = this.getLimitCartByUserId(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        //判断参数是否符合要求
        if (productId == null || count == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //需要判断产品id是否存在
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createErrorByErrorMessage("没有该产品");
        }
        // 去数据库中搜索userid 和 productid 符合的cart
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if (cart == null) {//如果购物车中没有该商品，则创建这个商品并且添加到购物车中，
            cart = new Cart();
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            cart.setChecked(Const.Cart.CHECK);
            cartMapper.insert(cart);
        } else {//如果购物车有该商品，则把购物车中的数量相加
            Integer originalCount = cart.getQuantity();
            cart.setQuantity(originalCount + count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.cartList(userId);
    }


    @Override
    public ServerResponse<CartVo> updateCart(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        cart.setQuantity(count);
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.cartList(userId);
    }

    @Override
    public ServerResponse<CartVo> deleteCartByProductIds(Integer userId, String productIds) {

        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productIdList)) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteCartProductByProductIds(userId,productIdList);
        return this.cartList(userId);
    }

    @Override
    public ServerResponse<CartVo> selectProduct(Integer userId, Integer productId, Integer
            checked) {
        cartMapper.cartProductCheck(userId,productId,checked);
        return this.cartList(userId);
    }


    @Override
    public ServerResponse<Integer> getAllProductCount(Integer userId) {
         int count = cartMapper.getAllProductCount(userId);
        return ServerResponse.createBySuccess(count);
    }

    //根据用户id 返回购物车vo，这里面需要判断库存和购买数量是否超出范围
    private CartVo getLimitCartByUserId(Integer userId) {
        // 创建cartVo实例、cart产品list、产品总价
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<CartProductVo>();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        // 根据用户id获取所有的购物车列表
        List<Cart> cartList = cartMapper.selectCartsByUserId(userId);
        if (!CollectionUtils.isEmpty(cartList)) {//如果集合不为空，遍历list
            for (Cart cartItem: cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                cartProductVo.setProductChecked(cartItem.getChecked());
                // 根据产品id从数据库中查询到该产品
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setStoke(product.getStatus());
                    // 判断库存是否超过添加到购物车的个数
                    int limitStokeCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        //库存大于购买数量
                        limitStokeCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        limitStokeCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    }
                    cartProductVo.setProductQuantity(limitStokeCount);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(cartItem.getQuantity().doubleValue(),product.getPrice().doubleValue()));
                    cartProductVoList.add(cartProductVo);
                    //计算总价(选中状态才相加)
                    if (cartItem.getChecked() == Const.Cart.CHECK) {
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                    }
                }
            }
            //组装cartVo
            cartVo.setCartProductVoList(cartProductVoList);
            cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            cartVo.setTotalPrice(cartTotalPrice);
            cartVo.setAllChecked(this.allChecked(userId));
        }
        return cartVo;
    }

    /**
     * 判断购物车是否全选
     * @param userId 用户id
     * @return 是否全选
     */
    private boolean allChecked(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.cartAllChecked(userId) == 0;
    }




}
