package com.maikaitui.tourism.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Category;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取分类树
     */
    Result getCategoryTree();

    /**
     * 分页查询分类列表
     */
    Result getCategoryList(int page, int size);

    /**
     * 根据ID查询分类
     */
    Result getCategoryById(Long id);

    /**
     * 新增分类
     */
    Result addCategory(Category category);

    /**
     * 修改分类
     */
    Result updateCategory(Category category);

    /**
     * 删除分类
     */
    Result deleteCategory(Long id);
}
