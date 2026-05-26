package com.ruoyi.tuyt.business.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.system.entity.SysOrganization;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ISysOrganizationService extends IService<SysOrganization> {
    PageResult<SysOrganization> queryPage(String keyword, Integer pageNum, Integer pageSize);
    List<SysOrganization> queryTree();
    SysOrganization getById(Long id);
    void add(SysOrganization org);
    void update(SysOrganization org);
    void delete(List<Long> ids);
}
