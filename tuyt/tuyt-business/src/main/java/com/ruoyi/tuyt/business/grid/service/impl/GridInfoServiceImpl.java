package com.ruoyi.tuyt.business.grid.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.grid.entity.GridInfo;
import com.ruoyi.tuyt.business.grid.mapper.GridInfoMapper;
import com.ruoyi.tuyt.business.grid.service.IGridInfoService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GridInfoServiceImpl extends ServiceImpl<GridInfoMapper, GridInfo> implements IGridInfoService {

    @Override
    public PageResult<GridInfo> queryPage(String keyword, Integer gridLevel, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<GridInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(GridInfo::getGridName, keyword).or()
                   .like(GridInfo::getLeader, keyword).or()
                   .like(GridInfo::getResponsiblePerson, keyword);
        }
        if (gridLevel != null) {
            wrapper.eq(GridInfo::getGridLevel, gridLevel);
        }
        wrapper.orderByAsc(GridInfo::getGridLevel, GridInfo::getCreateTime);
        Page<GridInfo> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<GridInfo> queryTree() {
        return list(new LambdaQueryWrapper<GridInfo>()
                .orderByAsc(GridInfo::getGridLevel, GridInfo::getCreateTime));
    }

    @Override
    public GridInfo getById(Long id) {
        GridInfo grid = super.getById(id);
        if (grid == null) throw new BusinessException("网格不存在");
        return grid;
    }

    @Override
    @Transactional
    public void add(GridInfo grid) { save(grid); }

    @Override
    @Transactional
    public void update(GridInfo grid) {
        if (!updateById(grid)) throw new BusinessException("网格不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        // 检查是否有子网格
        for (Long id : ids) {
            long childCount = count(new LambdaQueryWrapper<GridInfo>().eq(GridInfo::getParentId, id));
            if (childCount > 0) throw new BusinessException("存在子网格，无法删除");
        }
        removeByIds(ids);
    }
}
