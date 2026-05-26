package com.ruoyi.tuyt.business.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.assessment.entity.AssessIndicator;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IAssessIndicatorService extends IService<AssessIndicator> {
    PageResult<AssessIndicator> queryPage(String keyword, String assessType, Integer pageNum, Integer pageSize);
    AssessIndicator getById(Long id);
    void add(AssessIndicator indicator);
    void update(AssessIndicator indicator);
    void delete(List<Long> ids);
}
