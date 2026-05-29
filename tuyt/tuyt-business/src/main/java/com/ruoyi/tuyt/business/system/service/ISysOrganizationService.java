package com.ruoyi.tuyt.business.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.system.entity.SysOrganization;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;
import java.util.Map;

public interface ISysOrganizationService extends IService<SysOrganization> {
    PageResult<SysOrganization> queryPage(String keyword, Integer pageNum, Integer pageSize);
    List<SysOrganization> queryTree();
    /** 获取机构树形列表（用于报表筛选） */
    List<Map<String, Object>> getOrgTree(Long parentId);
    SysOrganization getById(Long id);
    void add(SysOrganization org);
    void update(SysOrganization org);
    void delete(List<Long> ids);
}
