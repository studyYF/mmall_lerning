package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yangf on 2018/2/28.
 */
@Service("iAddressService")
public class AddressServiceImpl implements IAddressService {
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse addAddress(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int result = shippingMapper.insert(shipping);
        if (result <= 0) {//result 表示插入数据库中的位置，0表示插入失败
            return ServerResponse.createErrorByErrorMessage("添加地址失败");
        }
        return ServerResponse.createBySuccess("添加成功");
    }

    @Override
    public ServerResponse<PageInfo> list( Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.getAllAddress(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse deleteAddressById(Integer userId, Integer shippingId) {
        int result = shippingMapper.deleteAddressByUserIdAndShippingId(userId, shippingId);
        if (result <= 0) {
            return ServerResponse.createErrorByErrorMessage("删除失败");
        }
        return ServerResponse.createBySuccess("删除成功");
    }

    @Override
    public ServerResponse updateAddress(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        // 为了防止横向越权，这里更新地址的方法需要重写，根据shippingId 和 userId更新
        int result = shippingMapper.updateByPrimaryKeySelective(shipping);
        if (result <= 0) {
            return ServerResponse.createErrorByErrorMessage("修改地址失败");
        }
        return ServerResponse.createBySuccess("修改地址成功");
    }

    @Override
    public ServerResponse getAddressById(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByUserIdAndShippingId(userId,shippingId);
        if (shipping == null) {
            return ServerResponse.createErrorByErrorMessage("没有该地址信息");
        }
        return ServerResponse.createBySuccess(shipping);
    }
}
