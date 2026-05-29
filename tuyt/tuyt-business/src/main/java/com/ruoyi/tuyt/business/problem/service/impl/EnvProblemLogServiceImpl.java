package com.ruoyi.tuyt.business.problem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.problem.entity.EnvProblemLog;
import com.ruoyi.tuyt.business.problem.mapper.EnvProblemLogMapper;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvProblemLogServiceImpl extends ServiceImpl<EnvProblemLogMapper, EnvProblemLog> implements IEnvProblemLogService {

    @Override
    public List<EnvProblemLog> getLogsByProblemId(Long problemId) {
        LambdaQueryWrapper<EnvProblemLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvProblemLog::getProblemId, problemId)
               .orderByDesc(EnvProblemLog::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void recordLog(Long problemId, String operationType, String content, Long operatorId) {
        EnvProblemLog log = new EnvProblemLog();
        log.setProblemId(problemId);
        log.setOperationType(operationType);
        log.setContent(content);
        log.setOperatorId(operatorId != null ? operatorId : 0L);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}
