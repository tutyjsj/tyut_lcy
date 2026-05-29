package com.ruoyi.tuyt.business.checktemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.checktemplate.entity.CheckTemplate;
import com.ruoyi.tuyt.business.checktemplate.mapper.CheckTemplateMapper;
import com.ruoyi.tuyt.business.checktemplate.service.ICheckTemplateService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 检查模板 Service 实现
 */
@Service
public class CheckTemplateServiceImpl extends ServiceImpl<CheckTemplateMapper, CheckTemplate>
        implements ICheckTemplateService {

    @Override
    public PageResult<CheckTemplate> queryPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CheckTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CheckTemplate::getTemplateName, keyword);
        }
        wrapper.orderByAsc(CheckTemplate::getCreateTime);
        Page<CheckTemplate> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public CheckTemplate getById(Long id) {
        CheckTemplate template = super.getById(id);
        if (template == null) {
            throw new BusinessException("检查模板不存在");
        }
        return template;
    }

    @Override
    @Transactional
    public void add(CheckTemplate template) {
        save(template);
    }

    @Override
    @Transactional
    public void update(CheckTemplate template) {
        if (!updateById(template)) {
            throw new BusinessException("检查模板不存在");
        }
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        removeByIds(ids);
    }
}
