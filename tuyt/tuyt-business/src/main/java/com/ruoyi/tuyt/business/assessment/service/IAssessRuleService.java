package com.ruoyi.tuyt.business.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.assessment.entity.AssessRule;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IAssessRuleService extends IService<AssessRule> {
    PageResult<AssessRule> queryPage(String keyword, String category, Integer pageNum, Integer pageSize);
    AssessRule getById(Long id);
    void add(AssessRule rule);
    void update(AssessRule rule);
    void delete(List<Long> ids);
}
