package com.ruoyi.tuyt.business.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.assessment.entity.AssessTemplate;
import com.ruoyi.tuyt.business.assessment.mapper.AssessTemplateMapper;
import com.ruoyi.tuyt.business.assessment.service.IAssessTemplateService;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessTemplateServiceImpl extends ServiceImpl<AssessTemplateMapper, AssessTemplate> implements IAssessTemplateService {

    @Override
    public PageResult<AssessTemplate> queryPage(String keyword, String templateType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AssessTemplate> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AssessTemplate::getTemplateName, keyword);
        }
        if (templateType != null && !templateType.isEmpty()) {
            wrapper.eq(AssessTemplate::getTemplateType, templateType);
        }
        wrapper.orderByAsc(AssessTemplate::getCreateTime);
        Page<AssessTemplate> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public void add(AssessTemplate template) {
        save(template);
    }

    @Override
    public void update(AssessTemplate template) {
        updateById(template);
    }

    @Override
    public void delete(List<Long> ids) {
        removeByIds(ids);
    }

    @Override
    public List<AssessTemplate> listEnabled() {
        return list(new LambdaQueryWrapper<AssessTemplate>().eq(AssessTemplate::getStatus, 1));
    }
}
