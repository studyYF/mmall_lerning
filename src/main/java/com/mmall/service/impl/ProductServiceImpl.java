package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yangfan on 2017/9/25.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            //判断上传的图片是否为空，给mainPage赋值
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] imagesArray = product.getSubImages().split(",");
                if (imagesArray.length > 0) {
                    product.setMainImage(imagesArray[0]);
                }
            }
            // 更新
            if (product.getId() != null) {
                int result = productMapper.updateByPrimaryKey(product);
                if (result > 0) {
                    return ServerResponse.createBySuccess("更新成功");
                }
                return ServerResponse.createBySuccess("更新失败");
            } else {//新增
                int result = productMapper.insert(product);
                if (result > 0) {
                    return ServerResponse.createBySuccess("新增成功");
                }
                return ServerResponse.createBySuccess("新增失败");
            }
        }
        return ServerResponse.createErrorByErrorMessage("参数错误");
    }
}
