package com.ruoyi.tuyt.business.problem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.mapper.EnvProblemMapper;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.common.enums.HandleStatusEnum;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnvProblemServiceImpl extends ServiceImpl<EnvProblemMapper, EnvProblem> implements IEnvProblemService {

    private final IEnterpriseService enterpriseService;

    @Override
    public PageResult<EnvProblem> queryPage(String keyword, String problemLevel, String handleStatus, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<EnvProblem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(EnvProblem::getProblemNo, keyword).or()
                   .like(EnvProblem::getProblemDesc, keyword).or()
                   .like(EnvProblem::getAddress, keyword);
        }
        if (StringUtils.hasText(problemLevel)) {
            wrapper.eq(EnvProblem::getProblemLevel, problemLevel);
        }
        if (StringUtils.hasText(handleStatus)) {
            wrapper.eq(EnvProblem::getHandleStatus, handleStatus);
        }
        wrapper.orderByDesc(EnvProblem::getCreateTime);
        Page<EnvProblem> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillEnterpriseName(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    private void fillEnterpriseName(List<EnvProblem> records) {
        Set<Long> enterpriseIds = records.stream()
                .map(EnvProblem::getEnterpriseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (enterpriseIds.isEmpty()) return;
        Map<Long, String> nameMap = enterpriseService.listByIds(enterpriseIds).stream()
                .collect(Collectors.toMap(Enterprise::getId, Enterprise::getEnterpriseName));
        for (EnvProblem p : records) {
            if (p.getEnterpriseId() != null) {
                p.setEnterpriseName(nameMap.getOrDefault(p.getEnterpriseId(), "-"));
            }
        }
    }

    @Override
    public EnvProblem getById(Long id) {
        EnvProblem problem = super.getById(id);
        if (problem == null) throw new BusinessException("问题不存在");
        if (problem.getEnterpriseId() != null) {
            Enterprise e = enterpriseService.getById(problem.getEnterpriseId());
            problem.setEnterpriseName(e != null ? e.getEnterpriseName() : "-");
        }
        return problem;
    }

    @Override
    @Transactional
    public void add(EnvProblem problem) { save(problem); }

    @Override
    @Transactional
    public void update(EnvProblem problem) {
        if (!updateById(problem)) throw new BusinessException("问题不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }

    @Override
    @Transactional
    public void close(Long id, String reason) {
        EnvProblem problem = getById(id);
        problem.setHandleStatus(HandleStatusEnum.CLOSED.name());
        problem.setCloseReason(reason);
        updateById(problem);
    }

    @Override
    @Transactional
    public void changeLevel(Long id, String level) {
        EnvProblem problem = getById(id);
        problem.setProblemLevel(level);
        updateById(problem);
    }

    @Override
    @Transactional
    public void merge(List<Long> ids, Long targetId) {
        EnvProblem target = getById(targetId);
        for (Long id : ids) {
            if (id.equals(targetId)) continue;
            EnvProblem problem = getById(id);
            problem.setMergeId(targetId);
            problem.setHandleStatus(HandleStatusEnum.CLOSED.name());
            problem.setCloseReason("合并至问题" + target.getProblemNo());
            updateById(problem);
        }
    }

    @Override
    public Map<String, Object> statistics() {
        List<EnvProblem> all = list();

        long total = all.size();
        long pending = all.stream().filter(p -> "PENDING".equals(p.getHandleStatus())).count();
        long done = all.stream().filter(p -> "PROCESSED".equals(p.getHandleStatus()) || "DONE".equals(p.getHandleStatus())).count();

        Map<String, Long> pollutionTypeMap = all.stream()
                .filter(p -> p.getPollutionType() != null)
                .collect(Collectors.groupingBy(EnvProblem::getPollutionType, Collectors.counting()));
        List<Map<String, Object>> pollutionTypes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : pollutionTypeMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            pollutionTypes.add(item);
        }

        Map<String, Long> sourceMap = all.stream()
                .filter(p -> p.getProblemSource() != null)
                .collect(Collectors.groupingBy(EnvProblem::getProblemSource, Collectors.counting()));
        List<Map<String, Object>> sources = new ArrayList<>();
        for (Map.Entry<String, Long> entry : sourceMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            sources.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pending", pending);
        result.put("done", done);
        result.put("pollutionTypes", pollutionTypes);
        result.put("sources", sources);
        return result;
    }

    @Override
    public List<Map<String, Object>> ranking(Integer top) {
        List<EnvProblem> all = list();
        // 按区域排名（areaName 分组计数）
        Map<String, Long> areaCount = all.stream().collect(
                Collectors.groupingBy(p -> p.getAreaName() != null ? p.getAreaName() : "未知", Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        areaCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(top)
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    result.add(item);
                });
        return result;
    }
}
