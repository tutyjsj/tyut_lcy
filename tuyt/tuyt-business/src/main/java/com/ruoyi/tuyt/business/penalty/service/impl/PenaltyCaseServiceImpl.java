package com.ruoyi.tuyt.business.penalty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.business.penalty.entity.PenaltyCase;
import com.ruoyi.tuyt.business.penalty.mapper.PenaltyCaseMapper;
import com.ruoyi.tuyt.business.penalty.service.IPenaltyCaseService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.framework.config.LoginUserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PenaltyCaseServiceImpl extends ServiceImpl<PenaltyCaseMapper, PenaltyCase> implements IPenaltyCaseService {

    private final ITaskInfoService taskInfoService;
    private final IEnvProblemService envProblemService;
    private final IEnterpriseService enterpriseService;

    @Override
    public PageResult<PenaltyCase> queryPage(String caseNo, String status, String penaltyType,
                                              Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PenaltyCase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(caseNo)) {
            wrapper.like(PenaltyCase::getCaseNo, caseNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PenaltyCase::getStatus, status);
        }
        if (StringUtils.hasText(penaltyType)) {
            wrapper.eq(PenaltyCase::getPenaltyType, penaltyType);
        }
        wrapper.orderByDesc(PenaltyCase::getCreateTime);
        Page<PenaltyCase> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillRefInfo(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PenaltyCase getDetail(Long id) {
        PenaltyCase pc = getById(id);
        if (pc == null) throw new BusinessException("处罚案件不存在");
        fillRefInfo(List.of(pc));
        return pc;
    }

    @Override
    @Transactional
    public void file(PenaltyCase penaltyCase) {
        penaltyCase.setStatus("FILED");
        Long userId = LoginUserHolder.getUserId();
        penaltyCase.setApplicantId(userId);
        penaltyCase.setCaseNo("PC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000)));

        // 关联任务信息
        if (penaltyCase.getTaskId() != null) {
            try {
                TaskInfo task = taskInfoService.getById(penaltyCase.getTaskId());
                if (task != null) {
                    if (penaltyCase.getProblemId() == null) penaltyCase.setProblemId(task.getProblemId());
                    if (penaltyCase.getEnterpriseId() == null) penaltyCase.setEnterpriseId(task.getEnterpriseId());
                }
            } catch (Exception ignored) {}
        }

        // 更新关联问题的处罚状态
        if (penaltyCase.getProblemId() != null) {
            try {
                EnvProblem problem = envProblemService.getById(penaltyCase.getProblemId());
                if (problem != null) {
                    problem.setPenaltyStatus("FILED");
                    envProblemService.updateById(problem);
                }
            } catch (Exception ignored) {}
        }

        save(penaltyCase);
    }

    @Override
    @Transactional
    public void updateCase(PenaltyCase penaltyCase) {
        PenaltyCase exist = getById(penaltyCase.getId());
        if (exist == null) throw new BusinessException("处罚案件不存在");
        if ("CLOSED".equals(exist.getStatus())) throw new BusinessException("已结案的案件不可修改");

        if (StringUtils.hasText(penaltyCase.getPenaltyType())) exist.setPenaltyType(penaltyCase.getPenaltyType());
        if (penaltyCase.getPenaltyAmount() != null) exist.setPenaltyAmount(penaltyCase.getPenaltyAmount());
        if (StringUtils.hasText(penaltyCase.getPenaltyContent())) exist.setPenaltyContent(penaltyCase.getPenaltyContent());
        if (StringUtils.hasText(penaltyCase.getLegalBasis())) exist.setLegalBasis(penaltyCase.getLegalBasis());
        if (StringUtils.hasText(penaltyCase.getCaseDesc())) exist.setCaseDesc(penaltyCase.getCaseDesc());
        if (penaltyCase.getHearingDate() != null) exist.setHearingDate(penaltyCase.getHearingDate());
        if (StringUtils.hasText(penaltyCase.getStatus()) && !"CLOSED".equals(penaltyCase.getStatus())) {
            exist.setStatus(penaltyCase.getStatus());
        }
        updateById(exist);
    }

    @Override
    @Transactional
    public void rule(Long id, String rulingResult) {
        PenaltyCase pc = getById(id);
        if (pc == null) throw new BusinessException("处罚案件不存在");
        pc.setStatus("PENALIZED");
        pc.setRulingDate(LocalDateTime.now());
        pc.setRulingResult(rulingResult);
        updateById(pc);

        syncProblemPenaltyStatus(pc.getProblemId(), "PENALIZED");
    }

    @Override
    @Transactional
    public void close(Long id) {
        PenaltyCase pc = getById(id);
        if (pc == null) throw new BusinessException("处罚案件不存在");
        pc.setStatus("CLOSED");
        updateById(pc);
        syncProblemPenaltyStatus(pc.getProblemId(), "CLOSED");
    }

    @Override
    @Transactional
    public void deleteCases(List<Long> ids) {
        removeByIds(ids);
    }

    private void syncProblemPenaltyStatus(Long problemId, String status) {
        if (problemId == null) return;
        try {
            EnvProblem problem = envProblemService.getById(problemId);
            if (problem != null) {
                problem.setPenaltyStatus(status);
                envProblemService.updateById(problem);
            }
        } catch (Exception ignored) {}
    }

    private void fillRefInfo(List<PenaltyCase> list) {
        if (list == null || list.isEmpty()) return;

        Set<Long> taskIds = list.stream().map(PenaltyCase::getTaskId).filter(id -> id != null).collect(Collectors.toSet());
        Set<Long> entIds = list.stream().map(PenaltyCase::getEnterpriseId).filter(id -> id != null).collect(Collectors.toSet());

        Map<Long, TaskInfo> taskMap = taskIds.isEmpty() ? Map.of() :
                taskInfoService.listByIds(taskIds).stream().collect(Collectors.toMap(TaskInfo::getId, t -> t, (a, b) -> a));
        Map<Long, Enterprise> entMap = entIds.isEmpty() ? Map.of() :
                enterpriseService.listByIds(entIds).stream().collect(Collectors.toMap(Enterprise::getId, e -> e, (a, b) -> a));

        for (PenaltyCase pc : list) {
            TaskInfo t = taskMap.get(pc.getTaskId());
            if (t != null) {
                pc.setTaskTitle(t.getTaskTitle());
                pc.setTaskNo(t.getTaskNo());
            }
            Enterprise ent = entMap.get(pc.getEnterpriseId());
            if (ent != null) {
                pc.setEnterpriseName(ent.getEnterpriseName());
            }
        }
    }
}
