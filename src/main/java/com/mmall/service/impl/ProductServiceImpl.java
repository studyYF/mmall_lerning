package com.mmall.service.impl;

import ch.qos.logback.classic.gaffer.PropertyUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yangfan on 2017/9/25.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

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
                return ServerResponse.createErrorByErrorMessage("更新失败");
            } else {
                //新增
                int result = productMapper.insert(product);
                if (result > 0) {
                    return ServerResponse.createBySuccess("新增成功");
                }
                return ServerResponse.createErrorByErrorMessage("新增失败");
            }
        }
        return ServerResponse.createErrorByErrorMessage("参数错误");
    }

    @Override
    public ServerResponse setProductStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("更新产品状态成功");
        }
        return ServerResponse.createBySuccessMessage("更新产品状态失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createErrorByErrorMessage("产品已经下架或者删除");
        }

        return ServerResponse.createBySuccess(assembleProductDetailVo(product));
    }




    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        List<Product> list = productMapper.selectProducts();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product: list) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> searchProduct(int pageNum, int pageSize, Integer productId, String productName) {
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> list = productMapper.selectProductByProductIdAndProductName(productId,productName);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product: list) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<ProductDetailVo> details(Integer productId) {
        if (productId == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createErrorByErrorMessage("产品已经下架或者删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createErrorByErrorMessage("产品不是销售状态");
        }
        return ServerResponse.createBySuccess(assembleProductDetailVo(product));
    }

    @Override
    public ServerResponse<PageInfo> searchList(int pageNum, int pageSize, String keyword, Integer
            categoryId, String orderBy) {
        //判断关键字和分类id是否为空
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();

        if (categoryId != null) {//分类id不为空,搜索该分类id 得到分类category
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                // 分类为空并且没有关键字的话,返回一个空的集合,不报错,只是没有数据
                PageHelper.startPage(pageNum,pageSize);
                //创建一个空的productVo集合
                List<ProductListVo> list = new ArrayList<ProductListVo>();
                PageInfo pageInfo = new PageInfo(list);
                return ServerResponse.createBySuccess(pageInfo);
            }
            if (category != null) {
                categoryIdList = iCategoryService.getDeepChildrenCategory(category.getId()).getData();
            }
        }

        // 如果有挂件次,拼接关键字 为 % keyword % (在数据库中模糊搜索)
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum,pageSize);
        // 排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            // 如果包含orderBy
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                //拼接排序方式 PageHelper 排序格式为 "price asc"
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectProductByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword,categoryIdList.size() == 0 ? null : categoryIdList);
        List<ProductListVo> productListVoList = new ArrayList<ProductListVo>();
        // 把list中的product替换成productVo
        for (Product product: productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 将product转成实际业务对象
     * @param product product实例
     * @return productDetailVo
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("",""));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null){
            productDetailVo.setCategoryId(0);
        } else {
            productDetailVo.setCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToString(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToString(product.getUpdateTime()));
        return productDetailVo;
    }

    /**
     * 将product转成实际需要的productListVo对象
     * @param product product对象
     * @return productListVo对象
     */
    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setName(product.getName());
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setMainPage(product.getMainImage());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        return productListVo;
    }
}
