package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.entity.EnvProblemLog;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemLogService;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "环境问题管理")
@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
public class ProblemController {

    private final IEnvProblemService envProblemService;
    private final IEnvProblemLogService envProblemLogService;
    private final ITaskInfoService taskInfoService;

    @Operation(summary = "分页查询问题列表")
    @GetMapping("/list")
    public R<PageResult<EnvProblem>> list(
            @RequestParam(required = false) String problemNo,
            @RequestParam(required = false) String enterpriseName,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) String areaName,
            @RequestParam(required = false) String problemLevel,
            @RequestParam(required = false) String pollutionType,
            @RequestParam(required = false) String problemType,
            @RequestParam(required = false) String problemSource,
            @RequestParam(required = false) String handleStatus,
            @RequestParam(required = false) Long gridId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(envProblemService.queryPage(problemNo, enterpriseName, enterpriseId, areaName, problemLevel, pollutionType, problemType, problemSource, handleStatus, gridId, pageNum, pageSize));
    }

    @Operation(summary = "获取问题详情")
    @GetMapping("/{id}")
    public R<EnvProblem> getById(@PathVariable Long id) {
        return R.ok(envProblemService.getById(id));
    }

    @Operation(summary = "新增问题")
    @PostMapping
    public R<Void> add(@RequestBody EnvProblem problem) {
        envProblemService.add(problem);
        R<Void> r = R.ok();
        r.setMessage("新增问题成功");
        return r;
    }

    @Operation(summary = "更新问题")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody EnvProblem problem) {
        problem.setId(id);
        envProblemService.update(problem);
        R<Void> r = R.ok();
        r.setMessage("更新问题成功");
        return r;
    }

    @Operation(summary = "合并问题")
    @PutMapping("/merge")
    public R<Void> merge(@RequestBody Map<String, Object> params) {
        List<Long> ids = convertToLongList(params.get("ids"));
        Long targetId = Long.valueOf(params.get("targetId").toString());
        envProblemService.merge(ids, targetId);
        R<Void> r = R.ok();
        r.setMessage("合并问题成功");
        return r;
    }

    @Operation(summary = "关闭问题")
    @PutMapping("/close")
    public R<Void> close(@RequestBody Map<String, Object> params) {
        List<Long> ids = convertToLongList(params.get("ids"));
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        for (Long id : ids) {
            envProblemService.close(id, reason);
        }
        R<Void> r = R.ok();
        r.setMessage("关闭问题成功");
        return r;
    }

    @Operation(summary = "修改问题等级")
    @PutMapping("/{id}/level")
    public R<Void> changeLevel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String level = params.get("level") != null ? params.get("level").toString() : "";
        envProblemService.changeLevel(id, level);
        R<Void> r = R.ok();
        r.setMessage("修改等级成功");
        return r;
    }

    /** 安全转换 JSON 数字列表为 Long 列表（JSON反序列化默认将数字解析为Integer） */
    @SuppressWarnings("unchecked")
    private List<Long> convertToLongList(Object obj) {
        if (obj == null) return List.of();
        return ((List<?>) obj).stream()
                .map(e -> Long.valueOf(e.toString()))
                .collect(Collectors.toList());
    }

    @Operation(summary = "问题统计")
    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics() {
        return R.ok(envProblemService.statistics());
    }

    @Operation(summary = "问题预警专用统计（支持筛选参数，基于全部数据，不依赖分页）")
    @GetMapping("/warning-stats")
    public R<Map<String, Object>> warningStatistics(
            @RequestParam(required = false) String problemLevel,
            @RequestParam(required = false) String pollutionType,
            @RequestParam(required = false) Long gridId) {
        return R.ok(envProblemService.warningStatistics(problemLevel, pollutionType, gridId));
    }

    @Operation(summary = "网格排名")
    @GetMapping("/ranking")
    public R<PageResult<Map<String, Object>>> ranking(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "month") String timeRange,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "3") Integer pageSize) {
        return R.ok(envProblemService.ranking(keyword, sort, parentId, timeRange, pageNum, pageSize));
    }

    @Operation(summary = "批量派发问题（为选中问题一键创建调度任务）")
    @PostMapping("/batch-dispatch")
    public R<Map<String, Object>> batchDispatch(@RequestBody Map<String, Object> params) {
        List<Long> problemIds = convertToLongList(params.get("problemIds"));
        int success = 0, fail = 0;
        for (Long problemId : problemIds) {
            try {
                EnvProblem problem = envProblemService.getById(problemId);
                if (problem == null) { fail++; continue; }
                TaskInfo task = new TaskInfo();
                task.setTaskTitle("处理" + (problem.getEnterpriseName() != null ? problem.getEnterpriseName() : "") + " - "
                        + (problem.getProblemDesc() != null ? problem.getProblemDesc() : "环境问题"));
                // 截断标题（数据库限制255字）
                if (task.getTaskTitle() != null && task.getTaskTitle().length() > 255) {
                    task.setTaskTitle(task.getTaskTitle().substring(0, 252) + "...");
                }
                task.setTaskType("RECTIFY");
                task.setUrgency(params.get("urgency") != null ? params.get("urgency").toString() : "NORMAL");
                task.setDeadline(params.get("deadline") != null
                        ? LocalDateTime.parse(params.get("deadline").toString().replace("T", " ").substring(0, 19),
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : LocalDateTime.now().plusDays(7));
                Object gridIdVal = params.get("gridId");
                if (gridIdVal instanceof Number) task.setGridId(((Number) gridIdVal).longValue());
                task.setTaskContent("现场核查处理：" + (problem.getProblemDesc() != null ? problem.getProblemDesc() : "") +
                        "\n污染类型：" + (problem.getPollutionType() != null ? problem.getPollutionType() : "") +
                        "\n事发企业：" + (problem.getEnterpriseName() != null ? problem.getEnterpriseName() : "") +
                        "\n处理单位：" + (problem.getAreaName() != null ? problem.getAreaName() : ""));
                task.setProblemId(problemId);
                task.setStatus("DISPATCHED");
                taskInfoService.dispatch(task);
                // 更新问题状态为处理中
                problem.setHandleStatus("PROCESSING");
                envProblemService.updateById(problem);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("total", problemIds.size());
        return R.ok(result);
    }

    @Operation(summary = "获取问题动态日志")
    @GetMapping("/{id}/logs")
    public R<List<EnvProblemLog>> getLogs(@PathVariable Long id) {
        return R.ok(envProblemLogService.getLogsByProblemId(id));
    }
}
