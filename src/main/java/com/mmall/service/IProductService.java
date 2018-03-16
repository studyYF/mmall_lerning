package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

import java.util.List;

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

    /**
     * 修改产品销售状态
     * @param productId 产品id
     * @return 是否成功
     */
    public ServerResponse setProductStatus(Integer productId, Integer status);

    /**
     * 根据产品id返回产品详情
     * @param productId 产品id
     * @return 产品对象
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    /**
     * 获取分类下的产品列表
     * @param pageNum 页数
     * @param pageSize 每页条数
     * @return 产品列表
     */
    public ServerResponse getList(int pageNum, int pageSize);

    /**
     * 搜索产品
     * @param  pageNum 页数
     * @param pageSize 每页个数
     * @param productId 产品id
     * @param productName 产品名称
     * @return 产品列表
     */
    public ServerResponse searchProduct(int pageNum, int pageSize,Integer
            productId,String productName);

    /**
     * 获取商品详情
     * @param productId 产品id
     * @return 返回商品详情
     */
    public ServerResponse<ProductDetailVo> details(Integer productId);

    /**
     * 搜索商品
     * @param pageNum 页数
     * @param pageSize 每页个数
     * @param keyword 关键字
     * @param categoryId 分类id
     * @param orderBy 排序方式
     * @return 分页数据
     */
    public ServerResponse<PageInfo> searchList(int pageNum, int pageSize, String keyword, Integer categoryId, String orderBy);
}
