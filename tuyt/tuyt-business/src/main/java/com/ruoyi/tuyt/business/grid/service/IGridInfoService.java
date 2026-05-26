package com.ruoyi.tuyt.business.grid.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.grid.entity.GridInfo;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IGridInfoService extends IService<GridInfo> {
    PageResult<GridInfo> queryPage(String keyword, Integer gridLevel, Integer pageNum, Integer pageSize);
    List<GridInfo> queryTree();
    GridInfo getById(Long id);
    void add(GridInfo grid);
    void update(GridInfo grid);
    void delete(List<Long> ids);
}
