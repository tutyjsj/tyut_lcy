package com.ruoyi.tuyt.business.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.system.entity.SysUser;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ISysUserService extends IService<SysUser> {
    PageResult<SysUser> queryPage(String keyword, Integer pageNum, Integer pageSize);
    SysUser getById(Long id);
    SysUser getByUsername(String username);
    void add(SysUser user);
    void update(SysUser user);
    void delete(List<Long> ids);
    void resetPassword(Long id, String newPassword);
}
