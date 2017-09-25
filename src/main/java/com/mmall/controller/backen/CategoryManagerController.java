package com.mmall.controller.backen;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by yangfan on 2017/9/21.
 */
@Controller
@RequestMapping("/manage/category")
//RequestParam表示默认值
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;


    /**
     * 添加品类
     * @param session session
     * @param categoryName 分类名称
     * @param parentId 父节点id 默认是0
     * @return 是否添加成功
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"需要登陆");
        }
        //校验用户是否是管理员
        if (iUserService.validateUserRole(user).isSuccess()) {
            return iCategoryService.addCategory(categoryName,parentId);
        } else {
            return ServerResponse.createBySuccessMessage("不是管理员，需要管理员权限");
        }
    }

    /**
     * 更新品类名称
     * @param session session
     * @param categoryId 分类id
     * @param categoryName 分类名称
     * @return 是否更新成功
     */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"需要登陆");
        }
        //校验用户是否是管理员
        if (iUserService.validateUserRole(user).isSuccess()) {
            //更新categoryName
            return iCategoryService.updateCategoryName(categoryName,categoryId);
        } else {
            return ServerResponse.createBySuccessMessage("不是管理员，需要管理员权限");
        }
    }

    /**
     * 查询当前分类下的子分类
     * @param session session
     * @param categoryId 分类id
     * @return 子节点category列表
     */
    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage("用户未登录，需要登录");
        }

        if (iUserService.validateUserRole(user).isSuccess()) {
            return iCategoryService.getChildParallelCategory(categoryId);
        } else {
            return ServerResponse.createErrorByErrorMessage("不是管理员，需要管理员权限");
        }
    }

    /**
     * 查询分类下所有节点以及子节点
     * @param session session
     * @param categoryId 分类id
     * @return 子节点id列表
     */
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Integer>> getDeepChildrenCategory(HttpSession session, Integer categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErrorByErrorMessage("用户未登录，需要登录");
        }
        if (iUserService.validateUserRole(user).isSuccess()) {
            return iCategoryService.getDeepChildrenCategory(categoryId);
        } else {
            return ServerResponse.createErrorByErrorMessage("不是管理员，需要管理员权限");
        }
    }
}
