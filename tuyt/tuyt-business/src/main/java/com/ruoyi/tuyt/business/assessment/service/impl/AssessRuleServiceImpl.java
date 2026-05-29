package com.ruoyi.tuyt.business.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.assessment.entity.AssessRule;
import com.ruoyi.tuyt.business.assessment.mapper.AssessRuleMapper;
import com.ruoyi.tuyt.business.assessment.service.IAssessRuleService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AssessRuleServiceImpl extends ServiceImpl<AssessRuleMapper, AssessRule> implements IAssessRuleService {

    @Override
    public PageResult<AssessRule> queryPage(String keyword, String category, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AssessRule> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(AssessRule::getRuleName, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(AssessRule::getCategory, category);
        }
        wrapper.orderByAsc(AssessRule::getCreateTime);
        Page<AssessRule> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public AssessRule getById(Long id) {
        AssessRule rule = super.getById(id);
        if (rule == null) throw new BusinessException("考评规则不存在");
        return rule;
    }

    @Override
    @Transactional
    public void add(AssessRule rule) { save(rule); }

    @Override
    @Transactional
    public void update(AssessRule rule) {
        if (!updateById(rule)) throw new BusinessException("考评规则不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
