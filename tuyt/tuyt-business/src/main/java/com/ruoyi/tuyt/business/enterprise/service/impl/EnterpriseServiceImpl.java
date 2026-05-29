package com.ruoyi.tuyt.business.enterprise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.mapper.EnterpriseMapper;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.business.grid.entity.GridEnterprise;
import com.ruoyi.tuyt.business.grid.entity.GridInfo;
import com.ruoyi.tuyt.business.grid.mapper.GridEnterpriseMapper;
import com.ruoyi.tuyt.business.grid.service.IGridInfoService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements IEnterpriseService {

    private final IGridInfoService gridInfoService;
    private final GridEnterpriseMapper gridEnterpriseMapper;

    @Override
    public PageResult<Enterprise> queryPage(String keyword, String superviseType, String enterpriseType, Long gridId, String status, String excludeStatus, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Enterprise::getEnterpriseName, keyword).or()
                              .like(Enterprise::getEnterpriseCode, keyword).or()
                              .like(Enterprise::getLegalPerson, keyword));
        }
        if (StringUtils.hasText(superviseType)) {
            wrapper.eq(Enterprise::getSuperviseType, superviseType);
        }
        if (StringUtils.hasText(enterpriseType)) {
            wrapper.eq(Enterprise::getEnterpriseType, enterpriseType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Enterprise::getStatus, status);
        }
        if (StringUtils.hasText(excludeStatus)) {
            String[] excludes = excludeStatus.split(",");
            wrapper.notIn(Enterprise::getStatus, Arrays.asList(excludes));
        }
        if (gridId != null) {
            Set<Long> childGridIds = collectDescendantGridIds(gridId);
            if (childGridIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, pageNum.longValue(), pageSize.longValue());
            }
            String inClause = childGridIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            wrapper.inSql(Enterprise::getId,
                    "SELECT enterprise_id FROM grid_enterprise WHERE grid_id IN (" + inClause + ")");
        }
        wrapper.orderByAsc(Enterprise::getCreateTime);
        Page<Enterprise> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillGridName(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 递归收集网格及其所有子孙网格ID */
    private Set<Long> collectDescendantGridIds(Long gridId) {
        List<GridInfo> allGrids = gridInfoService.list();
        Map<Long, List<GridInfo>> childrenMap = new HashMap<>();
        for (GridInfo g : allGrids) {
            if (g.getParentId() != null) {
                childrenMap.computeIfAbsent(g.getParentId(), k -> new ArrayList<>()).add(g);
            }
        }
        Set<Long> result = new HashSet<>();
        result.add(gridId);
        collectDescendants(gridId, childrenMap, result);
        return result;
    }

    private void collectDescendants(Long gridId, Map<Long, List<GridInfo>> childrenMap, Set<Long> result) {
        List<GridInfo> children = childrenMap.getOrDefault(gridId, Collections.emptyList());
        for (GridInfo child : children) {
            result.add(child.getId());
            collectDescendants(child.getId(), childrenMap, result);
        }
    }

    @Override
    public Enterprise getById(Long id) {
        Enterprise enterprise = super.getById(id);
        if (enterprise == null) throw new BusinessException("企业不存在");
        fillSingleGridName(enterprise);
        return enterprise;
    }

    /** 批量填充网格名称（用于列表查询） */
    private void fillGridName(List<Enterprise> records) {
        if (records.isEmpty()) return;
        Set<Long> enterpriseIds = records.stream().map(Enterprise::getId).collect(Collectors.toSet());
        Map<Long, Long> gridMap = gridEnterpriseMapper.selectList(
                        new LambdaQueryWrapper<GridEnterprise>().in(GridEnterprise::getEnterpriseId, enterpriseIds))
                .stream().collect(Collectors.toMap(GridEnterprise::getEnterpriseId, GridEnterprise::getGridId, (a, b) -> a));
        if (gridMap.isEmpty()) return;
        Set<Long> gridIds = new HashSet<>(gridMap.values());
        Map<Long, String> nameMap = gridInfoService.listByIds(gridIds).stream()
                .collect(Collectors.toMap(GridInfo::getId, GridInfo::getGridName));
        for (Enterprise e : records) {
            Long gridId = gridMap.get(e.getId());
            if (gridId != null) e.setGridName(nameMap.getOrDefault(gridId, "-"));
        }
    }

    /** 单个填充网格名称 */
    private void fillSingleGridName(Enterprise enterprise) {
        GridEnterprise ge = gridEnterpriseMapper.selectOne(
                new LambdaQueryWrapper<GridEnterprise>().eq(GridEnterprise::getEnterpriseId, enterprise.getId()));
        if (ge != null && ge.getGridId() != null) {
            GridInfo grid = gridInfoService.getById(ge.getGridId());
            if (grid != null) enterprise.setGridName(grid.getGridName());
        }
    }

    @Override
    @Transactional
    public void add(Enterprise enterprise) { save(enterprise); }

    @Override
    @Transactional
    public void update(Enterprise enterprise) {
        if (!updateById(enterprise)) throw new BusinessException("企业不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
