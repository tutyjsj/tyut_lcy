package com.ruoyi.tuyt.business.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.system.entity.SysOrganization;
import com.ruoyi.tuyt.business.system.mapper.SysOrganizationMapper;
import com.ruoyi.tuyt.business.system.service.ISysOrganizationService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysOrganizationServiceImpl extends ServiceImpl<SysOrganizationMapper, SysOrganization> implements ISysOrganizationService {

    @Override
    public PageResult<SysOrganization> queryPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysOrganization> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysOrganization::getOrgName, keyword);
        }
        wrapper.orderByAsc(SysOrganization::getSort);
        Page<SysOrganization> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<SysOrganization> queryTree() {
        List<SysOrganization> all = list(new LambdaQueryWrapper<SysOrganization>().orderByAsc(SysOrganization::getSort));
        Map<Long, List<SysOrganization>> parentMap = all.stream()
                .filter(o -> o.getParentId() != null && o.getParentId() > 0)
                .collect(Collectors.groupingBy(SysOrganization::getParentId));
        List<SysOrganization> roots = new ArrayList<>();
        for (SysOrganization org : all) {
            if (org.getParentId() == null || org.getParentId() == 0) {
                roots.add(org);
            }
            // 这里不设置 children 字段，由 Controller 控制返回格式
        }
        return all;
    }

    @Override
    public SysOrganization getById(Long id) {
        SysOrganization org = super.getById(id);
        if (org == null) throw new BusinessException("组织不存在");
        return org;
    }

    @Override
    @Transactional
    public void add(SysOrganization org) { save(org); }

    @Override
    @Transactional
    public void update(SysOrganization org) {
        if (!updateById(org)) throw new BusinessException("组织不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        // 检查是否有子组织
        for (Long id : ids) {
            long childCount = count(new LambdaQueryWrapper<SysOrganization>().eq(SysOrganization::getParentId, id));
            if (childCount > 0) throw new BusinessException("存在子组织，无法删除");
        }
        removeByIds(ids);
    }
}
