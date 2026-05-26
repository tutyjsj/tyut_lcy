package com.ruoyi.tuyt.business.problem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;
import java.util.Map;

public interface IEnvProblemService extends IService<EnvProblem> {
    PageResult<EnvProblem> queryPage(String keyword, String problemLevel, String handleStatus, Integer pageNum, Integer pageSize);
    EnvProblem getById(Long id);
    void add(EnvProblem problem);
    void update(EnvProblem problem);
    void delete(List<Long> ids);
    void close(Long id, String reason);
    void changeLevel(Long id, String level);
    void merge(List<Long> ids, Long targetId);
    Map<String, Object> statistics();
    List<Map<String, Object>> ranking(Integer top);
}
