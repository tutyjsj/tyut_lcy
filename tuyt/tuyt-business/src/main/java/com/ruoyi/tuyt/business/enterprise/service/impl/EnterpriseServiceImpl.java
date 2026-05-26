package com.ruoyi.tuyt.business.enterprise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.mapper.EnterpriseMapper;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements IEnterpriseService {

    @Override
    public PageResult<Enterprise> queryPage(String keyword, String pollutionType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Enterprise::getEnterpriseName, keyword).or()
                   .like(Enterprise::getEnterpriseCode, keyword).or()
                   .like(Enterprise::getLegalPerson, keyword);
        }
        if (StringUtils.hasText(pollutionType)) {
            wrapper.eq(Enterprise::getPollutionType, pollutionType);
        }
        wrapper.orderByAsc(Enterprise::getCreateTime);
        Page<Enterprise> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public Enterprise getById(Long id) {
        Enterprise enterprise = super.getById(id);
        if (enterprise == null) throw new BusinessException("企业不存在");
        return enterprise;
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
