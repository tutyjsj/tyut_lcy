package com.ruoyi.tuyt.business.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.system.entity.SysUser;
import com.ruoyi.tuyt.business.system.mapper.SysUserMapper;
import com.ruoyi.tuyt.business.system.service.ISysUserService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public PageResult<SysUser> queryPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword).or()
                   .like(SysUser::getRealName, keyword).or()
                   .like(SysUser::getPhone, keyword);
        }
        wrapper.orderByAsc(SysUser::getCreateTime);
        Page<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        // 清除密码字段，不返回给前端
        page.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SysUser getById(Long id) {
        SysUser user = super.getById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional
    public void add(SysUser user) {
        // 检查用户名重复
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        } else {
            user.setPassword(DigestUtil.md5Hex("123456"));
        }
        save(user);
    }

    @Override
    @Transactional
    public void update(SysUser user) {
        SysUser exist = super.getById(user.getId());
        if (exist == null) throw new BusinessException("用户不存在");
        // 不更新密码（密码单独重置）
        user.setPassword(null);
        if (!updateById(user)) throw new BusinessException("更新失败");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        removeByIds(ids);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser user = super.getById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(DigestUtil.md5Hex(newPassword));
        updateById(user);
    }
}
