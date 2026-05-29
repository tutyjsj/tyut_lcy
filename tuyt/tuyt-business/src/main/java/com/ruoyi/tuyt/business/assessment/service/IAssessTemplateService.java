package com.ruoyi.tuyt.business.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.assessment.entity.AssessTemplate;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IAssessTemplateService extends IService<AssessTemplate> {

    PageResult<AssessTemplate> queryPage(String keyword, String templateType, Integer pageNum, Integer pageSize);

    void add(AssessTemplate template);

    void update(AssessTemplate template);

    void delete(List<Long> ids);

    /** 获取所有启用的模板（下拉选择用） */
    List<AssessTemplate> listEnabled();
}
