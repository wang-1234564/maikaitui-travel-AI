package com.maikaitui.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysDictData;
import com.maikaitui.system.entity.SysDictType;
import com.maikaitui.system.mapper.SysDictDataMapper;
import com.maikaitui.system.mapper.SysDictTypeMapper;
import com.maikaitui.system.service.SysDictService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统字典服务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;

    @Override
    public Result getDictTypeList(int page, int size) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysDictType::getCreateTime);
        IPage<SysDictType> result = sysDictTypeMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @Override
    public Result getDictDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSortOrder);
        List<SysDictData> dictDataList = sysDictDataMapper.selectList(wrapper);
        return Result.success(dictDataList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addDictType(SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType.getDictType());
        if (sysDictTypeMapper.selectCount(wrapper) > 0) {
            return Result.error("字典类型编码已存在");
        }
        sysDictTypeMapper.insert(dictType);
        return Result.success("新增字典类型成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateDictType(SysDictType dictType) {
        if (sysDictTypeMapper.selectById(dictType.getId()) == null) {
            return Result.error("字典类型不存在");
        }
        sysDictTypeMapper.updateById(dictType);
        return Result.success("修改字典类型成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addDictData(SysDictData dictData) {
        sysDictDataMapper.insert(dictData);
        return Result.success("新增字典数据成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateDictData(SysDictData dictData) {
        if (sysDictDataMapper.selectById(dictData.getId()) == null) {
            return Result.error("字典数据不存在");
        }
        sysDictDataMapper.updateById(dictData);
        return Result.success("修改字典数据成功");
    }
}
