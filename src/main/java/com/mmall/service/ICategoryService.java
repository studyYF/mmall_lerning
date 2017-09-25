package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by yangfan on 2017/9/21.
 */
public interface ICategoryService {

    public ServerResponse addCategory(String categoryName, Integer parentId);

    public ServerResponse updateCategoryName(String categoryName, Integer categoryId);

    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId);

    public ServerResponse<List<Integer>> getDeepChildrenCategory(Integer categoryId);
}
