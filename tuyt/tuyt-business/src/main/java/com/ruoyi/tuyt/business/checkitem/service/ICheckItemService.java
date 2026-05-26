package com.ruoyi.tuyt.business.checkitem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.checkitem.entity.CheckItem;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ICheckItemService extends IService<CheckItem> {
    PageResult<CheckItem> queryPage(String keyword, String itemType, Integer pageNum, Integer pageSize);
    CheckItem getById(Long id);
    void add(CheckItem item);
    void update(CheckItem item);
    void delete(List<Long> ids);
}
