package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Set;

/**
 * Created by yang on 2017/9/21.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {


    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加品类
     * @param categoryName 分类名称
     * @param parentId 父节点
     * @return  是否添加成功
     */
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return  ServerResponse.createBySuccessMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//表示分类可用
        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0){
            return  ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return  ServerResponse.createErrorByErrorMessage("添加品类失败");
    }

    /**
     * 更新品类名
     * @param categoryName 分类名称
     * @param categoryId 分类id
     * @return 是否更新成功
     */
    @Override
    public ServerResponse updateCategoryName(String categoryName, Integer categoryId) {
        // 校验参数
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createErrorByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int returnCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (returnCount > 0) {
            return ServerResponse.createBySuccessMessage("更新品类成功");
        }
        return ServerResponse.createErrorByErrorMessage("更新品类失败");

    }

    /**
     * 查询同级目录下的所有品类
     * @param categoryId 分类id
     * @return 分类列表
     */
    @Override
    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId) {

        List<Category> list = categoryMapper.selectParallelCategoryByParentId(categoryId);
        if (CollectionUtils.isEmpty(list)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess("查询成功",list);
    }


    /**
     * 查询分类下的所有子节点
     * @param categoryId 分类id
     * @return 所有子节点
     */
    @Override
    public ServerResponse<List<Integer>> getDeepChildrenCategory(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        categorySet = findChildrenCategory(categorySet,categoryId);
        List<Integer> list = Lists.newArrayList();
        for (Category categoryItem: categorySet) {
            list.add(categoryItem.getId());
        }

        return ServerResponse.createBySuccess(list);
    }

    //递归遍历所有的子节点
    private Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        // list中包含所有categoryId对应的子节点，一级
        List<Category> categoryList = categoryMapper.selectParallelCategoryByParentId(categoryId);
        for (Category categoryItem: categoryList) {
            findChildrenCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
