package com.ruoyi.tuyt.business.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.assessment.entity.AssessIndicator;
import com.ruoyi.tuyt.business.assessment.mapper.AssessIndicatorMapper;
import com.ruoyi.tuyt.business.assessment.service.IAssessIndicatorService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AssessIndicatorServiceImpl extends ServiceImpl<AssessIndicatorMapper, AssessIndicator> implements IAssessIndicatorService {

    @Override
    public PageResult<AssessIndicator> queryPage(String keyword, String assessType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AssessIndicator> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(AssessIndicator::getIndicatorName, keyword);
        }
        if (StringUtils.hasText(assessType)) {
            wrapper.eq(AssessIndicator::getAssessType, assessType);
        }
        wrapper.orderByAsc(AssessIndicator::getCreateTime);
        Page<AssessIndicator> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public AssessIndicator getById(Long id) {
        AssessIndicator indicator = super.getById(id);
        if (indicator == null) throw new BusinessException("考评指标不存在");
        return indicator;
    }

    @Override
    @Transactional
    public void add(AssessIndicator indicator) { save(indicator); }

    @Override
    @Transactional
    public void update(AssessIndicator indicator) {
        if (!updateById(indicator)) throw new BusinessException("考评指标不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
