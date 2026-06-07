package com.maikaitui.system.service;

import com.maikaitui.common.core.Result;
import com.maikaitui.system.entity.SysDictData;
import com.maikaitui.system.entity.SysDictType;

/**
 * 系统字典服务接口
 */
public interface SysDictService {

    /**
     * 分页查询字典类型列表
     */
    Result getDictTypeList(int page, int size);

    /**
     * 根据字典类型查询字典数据
     */
    Result getDictDataByType(String dictType);

    /**
     * 新增字典类型
     */
    Result addDictType(SysDictType dictType);

    /**
     * 修改字典类型
     */
    Result updateDictType(SysDictType dictType);

    /**
     * 新增字典数据
     */
    Result addDictData(SysDictData dictData);

    /**
     * 修改字典数据
     */
    Result updateDictData(SysDictData dictData);
}
