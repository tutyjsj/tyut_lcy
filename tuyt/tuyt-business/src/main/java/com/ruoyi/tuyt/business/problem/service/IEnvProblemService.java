package com.ruoyi.tuyt.business.problem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.entity.ProblemExportVO;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;
import java.util.Map;

public interface IEnvProblemService extends IService<EnvProblem> {
    PageResult<EnvProblem> queryPage(String problemNo, String enterpriseName, Long enterpriseId, String areaName, String problemLevel, String pollutionType, String problemType, String problemSource, String handleStatus, Long gridId, Integer pageNum, Integer pageSize);
    EnvProblem getById(Long id);
    void add(EnvProblem problem);
    void update(EnvProblem problem);
    void delete(List<Long> ids);
    void close(Long id, String reason);
    void batchClose(List<Long> ids, String reason);
    void changeLevel(Long id, String level);
    void merge(List<Long> ids, Long targetId);
    Map<String, Object> statistics();
    /** 问题预警专用统计（支持筛选参数，基于全部数据，不依赖分页） */
    Map<String, Object> warningStatistics(String problemLevel, String pollutionType, Long gridId);
    PageResult<Map<String, Object>> ranking(String keyword, String sort, Long parentId, String timeRange, Integer pageNum, Integer pageSize);
    /** 导出问题台账（支持全部导出或按条件导出） */
    List<ProblemExportVO> exportProblems(String problemNo, String enterpriseName, Long enterpriseId, String areaName, String problemLevel, String pollutionType, String problemType, String problemSource, String handleStatus, Long gridId);
}
