package com.ruoyi.tuyt.business.problem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.business.grid.entity.GridInfo;
import com.ruoyi.tuyt.business.grid.service.IGridInfoService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.entity.ProblemExportVO;
import com.ruoyi.tuyt.business.problem.mapper.EnvProblemMapper;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemLogService;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.common.enums.HandleStatusEnum;
import com.ruoyi.tuyt.common.enums.ProblemLevelEnum;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnvProblemServiceImpl extends ServiceImpl<EnvProblemMapper, EnvProblem> implements IEnvProblemService {

    private final IEnterpriseService enterpriseService;
    private final IGridInfoService gridInfoService;
    private final IEnvProblemLogService envProblemLogService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PageResult<EnvProblem> queryPage(String problemNo, String enterpriseName, Long enterpriseId, String areaName, String problemLevel, String pollutionType, String problemType, String problemSource, String handleStatus, Long gridId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<EnvProblem> wrapper = new LambdaQueryWrapper<>();
        // 按问题编号模糊搜索
        if (StringUtils.hasText(problemNo)) {
            wrapper.like(EnvProblem::getProblemNo, problemNo);
        }
        // 按企业ID精确匹配（一企一档历史问题使用）
        if (enterpriseId != null) {
            wrapper.eq(EnvProblem::getEnterpriseId, enterpriseId);
        }
        // 按企业名称模糊搜索
        if (StringUtils.hasText(enterpriseName)) {
            LambdaQueryWrapper<Enterprise> ew = new LambdaQueryWrapper<>();
            ew.like(Enterprise::getEnterpriseName, enterpriseName);
            List<Long> enterpriseIds = enterpriseService.list(ew).stream()
                    .map(Enterprise::getId)
                    .collect(Collectors.toList());
            if (enterpriseIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, pageNum.longValue(), pageSize.longValue());
            }
            wrapper.in(EnvProblem::getEnterpriseId, enterpriseIds);
        }
        // 地级市网格筛选：自动展开下级区/街道，合并区级问题到市
        if (gridId != null) {
            List<String> cityAreaNames = resolveCityAreaNames(gridId);
            if (cityAreaNames.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, pageNum.longValue(), pageSize.longValue());
            }
            wrapper.in(EnvProblem::getAreaName, cityAreaNames);
        } else if (StringUtils.hasText(areaName)) {
            wrapper.like(EnvProblem::getAreaName, areaName);
        }
        if (StringUtils.hasText(problemLevel)) {
            wrapper.eq(EnvProblem::getProblemLevel, problemLevel);
        }
        if (StringUtils.hasText(pollutionType)) {
            wrapper.eq(EnvProblem::getProblemType, pollutionType);
        }
        if (StringUtils.hasText(problemType)) {
            wrapper.eq(EnvProblem::getProblemType, problemType);
        }
        if (StringUtils.hasText(problemSource)) {
            wrapper.eq(EnvProblem::getProblemSource, problemSource);
        }
        if (StringUtils.hasText(handleStatus)) {
            wrapper.eq(EnvProblem::getHandleStatus, handleStatus);
        }
        wrapper.orderByDesc(EnvProblem::getCreateTime);
        Page<EnvProblem> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillEnterpriseName(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 根据城市网格ID，递归收集市+区+街道所有网格名，实现区级数据合并到地级市 */
    private List<String> resolveCityAreaNames(Long gridId) {
        List<GridInfo> allGrids = gridInfoService.list();
        Map<Long, List<GridInfo>> childrenMap = new HashMap<>();
        for (GridInfo g : allGrids) {
            childrenMap.computeIfAbsent(g.getParentId(), k -> new ArrayList<>()).add(g);
        }
        Set<Long> allIds = collectDescendantIds(gridId, childrenMap);
        allIds.add(gridId);
        return allGrids.stream()
                .filter(g -> allIds.contains(g.getId()) && g.getGridName() != null)
                .map(GridInfo::getGridName)
                .distinct()
                .collect(Collectors.toList());
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
        EnvProblem old = getById(problem.getId());
        if (!updateById(problem)) throw new BusinessException("问题不存在");

        // 记录修改日志
        StringBuilder sb = new StringBuilder();
        if (problem.getEnterpriseId() != null && !problem.getEnterpriseId().equals(old.getEnterpriseId())) {
            sb.append("事发企业变更; ");
        }
        if (StringUtils.hasText(problem.getAddress()) && !problem.getAddress().equals(old.getAddress())) {
            sb.append("事发地点: ").append(old.getAddress()).append(" → ").append(problem.getAddress()).append("; ");
        }
        if (StringUtils.hasText(problem.getPollutionType()) && !problem.getPollutionType().equals(old.getPollutionType())) {
            sb.append("污染类型: ").append(old.getPollutionType()).append(" → ").append(problem.getPollutionType()).append("; ");
        }
        if (StringUtils.hasText(problem.getProblemDesc()) && !problem.getProblemDesc().equals(old.getProblemDesc())) {
            sb.append("问题详情已修改; ");
        }
        if (StringUtils.hasText(problem.getAreaName()) && !problem.getAreaName().equals(old.getAreaName())) {
            sb.append("事发区域: ").append(old.getAreaName()).append(" → ").append(problem.getAreaName()).append("; ");
        }
        if (sb.length() > 0) {
            envProblemLogService.recordLog(problem.getId(), "update", "修改内容: " + sb.toString(), problem.getCreateUserId());
        }
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }

    @Override
    @Transactional
    public void close(Long id, String reason) {
        EnvProblem problem = getById(id);
        String status = problem.getHandleStatus();
        // 只能关闭待处理、已处理、处理完成的问题
        if (HandleStatusEnum.CLOSED.name().equals(status)) {
            throw new BusinessException("问题" + problem.getProblemNo() + "已是关闭状态，无法再次关闭");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("关闭原因不能为空");
        }
        problem.setHandleStatus(HandleStatusEnum.CLOSED.name());
        problem.setCloseReason(reason);
        updateById(problem);
        envProblemLogService.recordLog(id, "close", "问题关闭，原因: " + reason, null);
    }

    @Override
    @Transactional
    public void batchClose(List<Long> ids, String reason) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要关闭的问题");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("关闭原因不能为空");
        }
        for (Long id : ids) {
            close(id, reason);
        }
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

    /**
     * 问题预警专用统计（支持筛选参数，基于全部数据，不依赖分页）
     * 返回：各级别数量、超期数量、今日新增、近30天趋势、污染类型分布
     */
    @Override
    public Map<String, Object> warningStatistics(String problemLevel, String pollutionType, Long gridId) {
        // 构建基础查询条件：全部待处理问题
        LambdaQueryWrapper<EnvProblem> wrapper = new LambdaQueryWrapper<EnvProblem>()
                .eq(EnvProblem::getHandleStatus, "PENDING");

        // 可选筛选条件
        if (StringUtils.hasText(problemLevel)) {
            wrapper.eq(EnvProblem::getProblemLevel, problemLevel);
        }
        if (StringUtils.hasText(pollutionType)) {
            wrapper.eq(EnvProblem::getProblemType, pollutionType);
        }
        if (gridId != null) {
            List<String> cityAreaNames = resolveCityAreaNames(gridId);
            if (cityAreaNames.isEmpty()) {
                wrapper.eq(EnvProblem::getId, -1L); // 无匹配数据
            } else {
                wrapper.in(EnvProblem::getAreaName, cityAreaNames);
            }
        }

        List<EnvProblem> pendingList = list(wrapper);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 各级别数量
        long levelI = pendingList.stream().filter(p -> "I".equals(p.getProblemLevel())).count();
        long levelII = pendingList.stream().filter(p -> "II".equals(p.getProblemLevel())).count();
        long levelIII = pendingList.stream().filter(p -> "III".equals(p.getProblemLevel())).count();

        // 超期待处理（报警超过7天未处理）
        long overdue = pendingList.stream()
                .filter(p -> p.getAlarmTime() != null && p.getAlarmTime().isBefore(sevenDaysAgo))
                .count();

        // 今日新增
        long todayNew = pendingList.stream()
                .filter(p -> p.getAlarmTime() != null && !p.getAlarmTime().isBefore(todayStart))
                .count();

        // 近30天趋势（每天新增数量）
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MAX);
            long count = pendingList.stream()
                    .filter(p -> p.getAlarmTime() != null
                            && !p.getAlarmTime().isBefore(dayStart)
                            && !p.getAlarmTime().isAfter(dayEnd))
                    .count();
            Map<String, Object> dayItem = new HashMap<>();
            dayItem.put("date", LocalDate.now().minusDays(i).toString());
            dayItem.put("count", count);
            trendData.add(dayItem);
        }

        // 污染类型分布（待处理问题）
        Map<String, Long> pollutionTypeMap = pendingList.stream()
                .filter(p -> p.getProblemType() != null)
                .collect(Collectors.groupingBy(EnvProblem::getProblemType, Collectors.counting()));
        List<Map<String, Object>> pollutionTypes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : pollutionTypeMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            pollutionTypes.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("levelI", levelI);
        result.put("levelII", levelII);
        result.put("levelIII", levelIII);
        result.put("overdue", overdue);
        result.put("todayNew", todayNew);
        result.put("totalPending", (long) pendingList.size());
        result.put("trendData", trendData);
        result.put("pollutionTypes", pollutionTypes);
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> ranking(String keyword, String sort, Long parentId, String timeRange, Integer pageNum, Integer pageSize) {
        // 1. 获取所有网格并构建层级树
        List<GridInfo> allGrids = gridInfoService.list();
        Map<Long, List<GridInfo>> childrenMap = new HashMap<>();
        for (GridInfo g : allGrids) {
            childrenMap.computeIfAbsent(g.getParentId(), k -> new ArrayList<>()).add(g);
        }

        // 2. 确定查询的层级范围：parentId为null默认查顶级(level=1)，否则查指定父grid的子级
        List<GridInfo> targetGrids;
        if (parentId != null) {
            targetGrids = childrenMap.getOrDefault(parentId, Collections.emptyList());
        } else {
            targetGrids = allGrids.stream().filter(g -> g.getParentId() == null || g.getGridLevel() == 1).collect(Collectors.toList());
        }
        // 如果顶级没有，查level=2
        if (targetGrids.isEmpty() && parentId == null) {
            targetGrids = allGrids.stream().filter(g -> g.getGridLevel() == 2).collect(Collectors.toList());
        }

        // 3. 递归获取每个网格的所有子网格ID
        Map<Long, Set<Long>> descendantMap = new HashMap<>();
        for (GridInfo g : allGrids) {
            descendantMap.put(g.getId(), collectDescendantIds(g.getId(), childrenMap));
        }

        // 4. 时间过滤条件
        LocalDateTime timeStart = null;
        if ("today".equals(timeRange)) {
            timeStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        } else {
            timeStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        }

        // 5. 获取所有问题
        List<EnvProblem> allProblems = list(new LambdaQueryWrapper<EnvProblem>().ge(EnvProblem::getAlarmTime, timeStart));

        // 6. 构建区域→网格ID的映射
        Map<String, List<Long>> areaGridMap = new HashMap<>();
        for (GridInfo g : allGrids) {
            if (g.getGridName() != null) {
                areaGridMap.computeIfAbsent(g.getGridName(), k -> new ArrayList<>()).add(g.getId());
            }
        }

        // 7. 对每个目标网格计算统计数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (GridInfo grid : targetGrids) {
            // 关键词过滤
            if (StringUtils.hasText(keyword)) {
                String kw = keyword.toLowerCase();
                boolean nameMatch = grid.getGridName() != null && grid.getGridName().toLowerCase().contains(kw);
                boolean leaderMatch = grid.getResponsiblePerson() != null && grid.getResponsiblePerson().toLowerCase().contains(kw);
                if (!nameMatch && !leaderMatch) continue;
            }

            Set<Long> allGridIds = descendantMap.getOrDefault(grid.getId(), new HashSet<>());
            allGridIds.add(grid.getId());

            // 收集所有关联的areaName
            Set<String> areaNames = new HashSet<>();
            for (GridInfo g : allGrids) {
                if (allGridIds.contains(g.getId()) && g.getGridName() != null) {
                    areaNames.add(g.getGridName());
                }
            }

            // 问题统计
            long problemTotal = allProblems.stream().filter(p -> areaNames.contains(p.getAreaName())).count();
            long pendingCount = allProblems.stream().filter(p -> areaNames.contains(p.getAreaName()) && "PENDING".equals(p.getHandleStatus())).count();
            long processedCount = allProblems.stream().filter(p -> areaNames.contains(p.getAreaName()) && "PROCESSED".equals(p.getHandleStatus())).count();
            long closedCount = allProblems.stream().filter(p -> areaNames.contains(p.getAreaName()) && "CLOSED".equals(p.getHandleStatus())).count();

            // 企业数量（包括子网格）
            Long enterpriseCount = 0L;
            if (!allGridIds.isEmpty()) {
                String inClause = allGridIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                enterpriseCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(DISTINCT enterprise_id) FROM grid_enterprise WHERE grid_id IN (" + inClause + ")", Long.class);
            }

            // 下级网格数
            int subGridCount = childrenMap.getOrDefault(grid.getId(), Collections.emptyList()).size();

            Map<String, Object> item = new HashMap<>();
            item.put("gridId", grid.getId());
            item.put("gridName", grid.getGridName());
            item.put("gridLeader", grid.getResponsiblePerson() != null ? grid.getResponsiblePerson() : "-");
            item.put("subGridCount", subGridCount);
            item.put("enterpriseCount", (int) (enterpriseCount == null ? 0L : enterpriseCount));
            item.put("problemTotal", (int) problemTotal);
            item.put("pendingCount", (int) pendingCount);
            item.put("processedCount", (int) processedCount);
            item.put("closedCount", (int) closedCount);
            result.add(item);
        }

        // 8. 排序：按问题总数降序，相同时按待处理数降序
        result.sort((a, b) -> {
            int totalCmp = Integer.compare((int) b.get("problemTotal"), (int) a.get("problemTotal"));
            if (totalCmp != 0) return totalCmp;
            return Integer.compare((int) b.get("pendingCount"), (int) a.get("pendingCount"));
        });

        // 9. 快速排序模式：前五名/后五名
        if ("bottom5".equals(sort)) {
            Collections.reverse(result);
            result = result.size() > 5 ? result.subList(0, 5) : result;
        } else if ("top5".equals(sort)) {
            result = result.size() > 5 ? result.subList(0, 5) : result;
        }

        // 10. 分配排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).put("rank", i + 1);
        }

        // 11. 分页
        int total = result.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<Map<String, Object>> pageList = from < total ? result.subList(from, to) : Collections.emptyList();

        return PageResult.of(pageList, (long) total, (long) pageNum, (long) pageSize);
    }

    /** 递归收集子网格ID */
    private Set<Long> collectDescendantIds(Long gridId, Map<Long, List<GridInfo>> childrenMap) {
        Set<Long> result = new HashSet<>();
        List<GridInfo> children = childrenMap.getOrDefault(gridId, Collections.emptyList());
        for (GridInfo child : children) {
            result.add(child.getId());
            result.addAll(collectDescendantIds(child.getId(), childrenMap));
        }
        return result;
    }

    @Override
    public List<ProblemExportVO> exportProblems(String problemNo, String enterpriseName, Long enterpriseId, String areaName,
                                                 String problemLevel, String pollutionType, String problemType, String problemSource,
                                                 String handleStatus, Long gridId) {
        LambdaQueryWrapper<EnvProblem> wrapper = new LambdaQueryWrapper<>();
        // 按问题编号模糊搜索
        if (StringUtils.hasText(problemNo)) {
            wrapper.like(EnvProblem::getProblemNo, problemNo);
        }
        if (enterpriseId != null) {
            wrapper.eq(EnvProblem::getEnterpriseId, enterpriseId);
        }
        if (StringUtils.hasText(enterpriseName)) {
            LambdaQueryWrapper<Enterprise> ew = new LambdaQueryWrapper<>();
            ew.like(Enterprise::getEnterpriseName, enterpriseName);
            List<Long> eids = enterpriseService.list(ew).stream()
                    .map(Enterprise::getId).collect(Collectors.toList());
            wrapper.in(!eids.isEmpty(), EnvProblem::getEnterpriseId, eids);
            if (eids.isEmpty()) return Collections.emptyList();
        }
        if (gridId != null) {
            List<String> cityAreaNames = resolveCityAreaNames(gridId);
            wrapper.in(!cityAreaNames.isEmpty(), EnvProblem::getAreaName, cityAreaNames);
            if (cityAreaNames.isEmpty()) return Collections.emptyList();
        } else if (StringUtils.hasText(areaName)) {
            wrapper.like(EnvProblem::getAreaName, areaName);
        }
        if (StringUtils.hasText(problemLevel)) {
            wrapper.eq(EnvProblem::getProblemLevel, problemLevel);
        }
        if (StringUtils.hasText(pollutionType)) {
            wrapper.eq(EnvProblem::getProblemType, pollutionType);
        }
        if (StringUtils.hasText(problemType)) {
            wrapper.eq(EnvProblem::getProblemType, problemType);
        }
        if (StringUtils.hasText(problemSource)) {
            wrapper.eq(EnvProblem::getProblemSource, problemSource);
        }
        if (StringUtils.hasText(handleStatus)) {
            wrapper.eq(EnvProblem::getHandleStatus, handleStatus);
        }
        wrapper.orderByDesc(EnvProblem::getCreateTime);
        List<EnvProblem> list = list(wrapper);
        fillEnterpriseName(list);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return list.stream().map(p -> {
            ProblemExportVO vo = new ProblemExportVO();
            vo.setProblemNo(p.getProblemNo());
            vo.setProblemLevel(p.getProblemLevel() != null ? ProblemLevelEnum.fromCode(p.getProblemLevel()) != null
                    ? ProblemLevelEnum.fromCode(p.getProblemLevel()).getName() : p.getProblemLevel() : "");
            vo.setAlarmTime(p.getAlarmTime() != null ? p.getAlarmTime().format(dtf) : "");
            vo.setProblemSource(p.getProblemSource());
            vo.setProblemType(p.getProblemType());
            vo.setPollutionType(p.getPollutionType());
            vo.setProblemDesc(p.getProblemDesc());
            vo.setAddress(p.getAddress());
            vo.setEnterpriseName(p.getEnterpriseName());
            vo.setAreaName(p.getAreaName());
            vo.setHandleStatus(HandleStatusEnum.fromCode(p.getHandleStatus()) != null
                    ? HandleStatusEnum.fromCode(p.getHandleStatus()).getName() : p.getHandleStatus());
            vo.setPenaltyStatus(p.getPenaltyStatus());
            vo.setCloseReason(p.getCloseReason());
            vo.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().format(dtf) : "");
            return vo;
        }).collect(Collectors.toList());
    }
}
