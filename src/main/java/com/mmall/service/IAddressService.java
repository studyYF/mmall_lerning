package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by yangf on 2018/2/28.
 */
public interface IAddressService {



    ServerResponse addAddress(Integer userId, Shipping shipping);

    ServerResponse<PageInfo> list(Integer userId,int pageNum, int pageSize);

    ServerResponse deleteAddressById(Integer userId,Integer shippingId);

    ServerResponse updateAddress(Integer userId, Shipping shipping);

    ServerResponse getAddressById(Integer userId, Integer shippingId);
}
