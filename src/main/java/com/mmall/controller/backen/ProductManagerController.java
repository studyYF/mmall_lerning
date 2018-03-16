package com.mmall.controller.backen;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import com.sun.deploy.net.HttpResponse;
import javafx.geometry.Pos;
import org.apache.ibatis.annotations.ResultMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yangfan on 2017/9/25.
 */
@Controller
@RequestMapping(value = "/manage/product")
public class ProductManagerController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 更新或新增产品
     *
     * @param session session
     * @param product 产品对象
     * @return 是否成功
     */
    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse saveProduct(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.validateUserRole(user).isSuccess()) {
                return iProductService.saveOrUpdateProduct(product);
            }
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
        }
        return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 修改商品的销售状态
     *
     * @param session   session
     * @param productId 产品id
     * @param status    产品状态
     * @return 是否成功
     */
    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.validateUserRole(user).isSuccess()) {
            return iProductService.setProductStatus(productId, status);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
    }

    /**
     * 获得产品详情
     *
     * @param session   session
     * @param productId 产品id
     * @return 产品对象
     */
    @RequestMapping(value = "get_product_detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProduct(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.validateUserRole(user).isSuccess()) {
            return iProductService.manageProductDetail(productId);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
    }


    /**
     * 获取产品列表
     *
     * @param pageNum  页数
     * @param pageSize 每页数据
     * @return 产品列表
     */
    @RequestMapping(value = "get_product_list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<ProductListVo>> getProducts(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.validateUserRole(user).isSuccess()) {
            return iProductService.getList(pageNum, pageSize);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
    }

    /**
     * 搜索商品列表
     *
     * @param session     session
     * @param pageNum     页数
     * @param pageSize    每页个数
     * @param productName 产品名称
     * @param productId   产品id
     * @return 产品列表
     *
     */
    @RequestMapping(value = "product_search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, String productName, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.validateUserRole(user).isSuccess()) {
            return iProductService.searchProduct(pageNum,pageSize,productId,productName);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
    }


    /**
     *
     * @param session session
     * @param file springMVC 文件上传
     * @param request http请求
     * @return 请求结果map
     */
    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse uploadFile(HttpSession session,@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {

        //管理员的权限，必须要管理员的权限
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        //判断用户是否存在
        if (user == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.validateUserRole(user).isSuccess()){
            //获取项目的绝对路径
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map resultMap = Maps.newHashMap();
            resultMap.put("uri", targetFileName);
            resultMap.put("url", url);
            return ServerResponse.createBySuccess(resultMap);
        }
        return ServerResponse.createErrorByErrorMessage(ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());

    }

    /**
     * 富文本上传，使用simiditor插件(格式要按照插件要求返回)
     * @param session session
     * @param request 请求
     * @param response 返回
     * @param file springmvc 文件
     * @return map simiditor插件
     */
    @RequestMapping(value = "rich_text_upload", method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImageUpload(HttpSession session, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "upload_file", required = false)MultipartFile file) {
        Map resultMap = Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg",ResponseCode.NEED_LOGIN.getDesc());
            return  resultMap;
        }

        if (iUserService.validateUserRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg",ResponseCode.SUCCESS.getDesc());
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }
        resultMap.put("success", false);
        resultMap.put("msg",ResponseCode.NEED_MANAGER_AUTHORITY.getDesc());
        return resultMap;
    }

}
