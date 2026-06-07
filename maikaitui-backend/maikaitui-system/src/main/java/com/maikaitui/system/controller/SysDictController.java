package com.maikaitui.system.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysDictData;
import com.maikaitui.system.entity.SysDictType;
import com.maikaitui.system.service.SysDictService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统字典控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/dict")
@AllArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    /**
     * 分页查询字典类型列表
     */
    @GetMapping("/type/list")
    public Result getDictTypeList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return sysDictService.getDictTypeList(page, size);
    }

    /**
     * 根据字典类型查询字典数据
     */
    @GetMapping("/data/{dictType}")
    public Result getDictDataByType(@PathVariable String dictType) {
        return sysDictService.getDictDataByType(dictType);
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/type")
    public Result addDictType(@Valid @RequestBody SysDictType dictType) {
        return sysDictService.addDictType(dictType);
    }

    /**
     * 修改字典类型
     */
    @PutMapping("/type")
    public Result updateDictType(@Valid @RequestBody SysDictType dictType) {
        return sysDictService.updateDictType(dictType);
    }

    /**
     * 新增字典数据
     */
    @PostMapping("/data")
    public Result addDictData(@Valid @RequestBody SysDictData dictData) {
        return sysDictService.addDictData(dictData);
    }

    /**
     * 修改字典数据
     */
    @PutMapping("/data")
    public Result updateDictData(@Valid @RequestBody SysDictData dictData) {
        return sysDictService.updateDictData(dictData);
    }
}
