package com.ruoyi.tuyt.business.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.system.entity.SysRole;
import com.ruoyi.tuyt.business.system.mapper.SysRoleMapper;
import com.ruoyi.tuyt.business.system.service.ISysRoleService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public PageResult<SysRole> queryPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysRole::getRoleName, keyword).or().like(SysRole::getRoleCode, keyword);
        }
        wrapper.orderByAsc(SysRole::getCreateTime);
        Page<SysRole> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SysRole getById(Long id) {
        SysRole role = super.getById(id);
        if (role == null) throw new BusinessException("角色不存在");
        return role;
    }

    @Override
    @Transactional
    public void add(SysRole role) {
        save(role);
    }

    @Override
    @Transactional
    public void update(SysRole role) {
        if (!updateById(role)) throw new BusinessException("角色不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        removeByIds(ids);
    }
}
