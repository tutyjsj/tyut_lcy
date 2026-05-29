package com.ruoyi.tuyt.business.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.assessment.entity.AssessResult;
import com.ruoyi.tuyt.common.result.PageResult;

public interface IAssessResultService extends IService<AssessResult> {
    PageResult<AssessResult> queryPage(String assessPeriod, String month, String gridName, Integer pageNum, Integer pageSize);
    void generateResults(String type);
}
