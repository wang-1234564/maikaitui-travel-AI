package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Attraction;
import com.maikaitui.tourism.service.AttractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 景点控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/attraction")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    /**
     * 分页查询景点列表
     */
    @GetMapping("/list")
    public Result getAttractionList(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) Long regionId,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String sortBy) {
        return attractionService.getAttractionList(page, size, regionId, categoryId, keyword, sortBy);
    }

    /**
     * 查询热门景点
     */
    @GetMapping("/hot")
    public Result getHotAttractions(@RequestParam(defaultValue = "10") int limit) {
        return attractionService.getHotAttractions(limit);
    }

    /**
     * 根据ID查询景点详情
     */
    @GetMapping("/{id}")
    public Result getAttractionById(@PathVariable Long id) {
        return attractionService.getAttractionById(id);
    }

    /**
     * 新增景点
     */
    @PostMapping
    public Result addAttraction(@Valid @RequestBody Attraction attraction) {
        return attractionService.addAttraction(attraction);
    }

    /**
     * 修改景点
     */
    @PutMapping
    public Result updateAttraction(@Valid @RequestBody Attraction attraction) {
        return attractionService.updateAttraction(attraction);
    }

    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    public Result deleteAttraction(@PathVariable Long id) {
        return attractionService.deleteAttraction(id);
    }

    /**
     * 获取推荐景点
     */
    @GetMapping("/{id}/recommendations")
    public Result getRecommendations(@PathVariable Long id,
                                      @RequestParam(defaultValue = "5") int limit) {
        return attractionService.getRecommendations(id, limit);
    }
}
