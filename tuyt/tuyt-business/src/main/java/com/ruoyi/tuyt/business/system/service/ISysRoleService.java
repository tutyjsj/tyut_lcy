package com.ruoyi.tuyt.business.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.system.entity.SysRole;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    PageResult<SysRole> queryPage(String keyword, Integer pageNum, Integer pageSize);
    SysRole getById(Long id);
    void add(SysRole role);
    void update(SysRole role);
    void delete(List<Long> ids);
}
