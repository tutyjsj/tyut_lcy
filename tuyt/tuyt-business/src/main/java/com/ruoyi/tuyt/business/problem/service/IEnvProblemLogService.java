package com.ruoyi.tuyt.business.problem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblemLog;

import java.util.List;

public interface IEnvProblemLogService extends IService<EnvProblemLog> {

    /** 获取指定问题的所有动态日志 */
    List<EnvProblemLog> getLogsByProblemId(Long problemId);

    /** 记录操作日志 */
    void recordLog(Long problemId, String operationType, String content, Long operatorId);
}
