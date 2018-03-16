package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yangf on 2018/2/26.
 */
@Controller
@RequestMapping(value = "/product/")
public class ProductController {
    @Autowired
    private IProductService iProductService;


    /**
     * 获取产品详情
     * @param productId 产品id
     * @return 产品详情vo
     */
    @RequestMapping(value = "details.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> details(Integer productId) {
        return iProductService.details(productId);
    }

    /**
     * 搜索商品
     * @param pageNum 页数
     * @param pageSize 每页个数
     * @param keyword 关键字
     * @param categoryId 分类id
     * @param orderBy 排序方式
     * @return 返回商品集合 分页
     */
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse<PageInfo> searchList(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                               @RequestParam(value = "PageSize",defaultValue = "10") int pageSize,
                                               @RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                               @RequestParam(value = "orderBy",defaultValue = "")String orderBy) {
        return iProductService.searchList(pageNum,pageSize,keyword,categoryId,orderBy);

    }
}
