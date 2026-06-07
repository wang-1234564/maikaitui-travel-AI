package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Category;
import com.maikaitui.tourism.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public Result getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 分页查询分类列表
     */
    @GetMapping("/list")
    public Result getCategoryList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return categoryService.getCategoryList(page, size);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Result getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result addCategory(@Valid @RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    /**
     * 修改分类
     */
    @PutMapping
    public Result updateCategory(@Valid @RequestBody Category category) {
        return categoryService.updateCategory(category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
