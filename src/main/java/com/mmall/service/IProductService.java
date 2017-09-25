package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

/**
 * Created by yangfan on 2017/9/25.
 */
public interface IProductService {

    /**
     * 保存或更新产品
     * @param product 产品对象
     * @return 是否更新成功
     */
    public ServerResponse saveOrUpdateProduct(Product product);
}
