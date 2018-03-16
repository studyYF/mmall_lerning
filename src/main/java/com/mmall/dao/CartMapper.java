package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdAndProductId(@Param("userId")Integer userId, @Param("productId") Integer productId);

    List<Cart> selectCartsByUserId(Integer userId);

    int cartAllChecked(Integer userId);

    int deleteCartProductByProductIds(@Param("userId")Integer userId,@Param("productIds") List<String> productIds);

    int cartProductCheck(@Param("userId")Integer userId,@Param("productId")Integer productId,@Param("checked") Integer checked);

    int getAllProductCount(Integer userId);

    List<Cart> selectCheckByUserId(Integer userId);
}