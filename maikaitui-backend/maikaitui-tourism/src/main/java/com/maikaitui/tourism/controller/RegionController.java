package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Region;
import com.maikaitui.tourism.service.RegionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 地区控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/region")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /**
     * 获取地区树
     */
    @GetMapping("/tree")
    public Result getRegionTree() {
        return regionService.getRegionTree();
    }

    /**
     * 分页查询地区列表
     */
    @GetMapping("/list")
    public Result getRegionList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return regionService.getRegionList(page, size);
    }

    /**
     * 根据ID查询地区
     */
    @GetMapping("/{id}")
    public Result getRegionById(@PathVariable Long id) {
        return regionService.getRegionById(id);
    }

    /**
     * 新增地区
     */
    @PostMapping
    public Result addRegion(@Valid @RequestBody Region region) {
        return regionService.addRegion(region);
    }

    /**
     * 修改地区
     */
    @PutMapping
    public Result updateRegion(@Valid @RequestBody Region region) {
        return regionService.updateRegion(region);
    }

    /**
     * 删除地区
     */
    @DeleteMapping("/{id}")
    public Result deleteRegion(@PathVariable Long id) {
        return regionService.deleteRegion(id);
    }
}
