package com.maikaitui.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Category;
import com.maikaitui.tourism.mapper.CategoryMapper;
import com.maikaitui.tourism.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Result getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(null);

        // 获取根节点（parentId = 0）
        List<Category> tree = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        // 递归构建树
        for (Category root : tree) {
            buildChildren(root, allCategories);
        }

        return Result.success(tree);
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(Category parent, List<Category> allCategories) {
        List<Category> children = allCategories.stream()
                .filter(c -> parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (Category child : children) {
                buildChildren(child, allCategories);
            }
        } else {
            parent.setChildren(new ArrayList<>());
        }
    }

    @Override
    public Result getCategoryList(int page, int size) {
        Page<Category> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        IPage<Category> result = categoryMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @Override
    @Transactional
    public Result addCategory(Category category) {
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        int rows = categoryMapper.insert(category);
        if (rows > 0) {
            log.info("新增分类成功: {}", category.getName());
            return Result.success("新增分类成功");
        }
        return Result.error("新增分类失败");
    }

    @Override
    @Transactional
    public Result updateCategory(Category category) {
        if (category.getId() == null) {
            return Result.error("分类ID不能为空");
        }
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            return Result.error("分类不存在");
        }
        int rows = categoryMapper.updateById(category);
        if (rows > 0) {
            log.info("更新分类成功: {}", category.getId());
            return Result.success("更新分类成功");
        }
        return Result.error("更新分类失败");
    }

    @Override
    @Transactional
    public Result deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        Long childCount = categoryMapper.selectCount(wrapper);
        if (childCount > 0) {
            return Result.error("该分类下存在子分类，无法删除");
        }
        int rows = categoryMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除分类成功: {}", id);
            return Result.success("删除分类成功");
        }
        return Result.error("删除分类失败");
    }
}
