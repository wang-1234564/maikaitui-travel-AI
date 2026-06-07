package com.maikaitui.tourism.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.tourism.entity.Guide;
import com.maikaitui.tourism.service.GuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 攻略控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tourism/guide")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    /**
     * 分页查询攻略列表
     */
    @GetMapping("/list")
    public Result getGuideList(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String destination,
                               @RequestParam(required = false) Integer durationDays,
                               @RequestParam(required = false) String season,
                               @RequestParam(required = false) String travelStyle,
                               @RequestParam(required = false) String sortBy) {
        return guideService.getGuideList(page, size, destination, durationDays, season, travelStyle, sortBy);
    }

    /**
     * 查询热门攻略
     */
    @GetMapping("/hot")
    public Result getHotGuides(@RequestParam(defaultValue = "6") int limit) {
        return guideService.getHotGuides(limit);
    }

    /**
     * 根据ID查询攻略详情
     */
    @GetMapping("/{id}")
    public Result getGuideById(@PathVariable Long id) {
        return guideService.getGuideById(id);
    }

    /**
     * 查询某景区的相关攻略
     */
    @GetMapping("/attraction/{attractionId}")
    public Result getGuidesByAttraction(@PathVariable Long attractionId,
                                        @RequestParam(defaultValue = "4") int limit) {
        return guideService.getGuidesByAttraction(attractionId, limit);
    }

    /**
     * 新增攻略
     */
    @PostMapping
    public Result addGuide(@Valid @RequestBody Guide guide) {
        return guideService.addGuide(guide);
    }

    /**
     * 修改攻略
     */
    @PutMapping
    public Result updateGuide(@Valid @RequestBody Guide guide) {
        return guideService.updateGuide(guide);
    }

    /**
     * 删除攻略
     */
    @DeleteMapping("/{id}")
    public Result deleteGuide(@PathVariable Long id) {
        return guideService.deleteGuide(id);
    }
}
