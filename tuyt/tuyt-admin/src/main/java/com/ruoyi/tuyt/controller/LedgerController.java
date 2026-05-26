package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "台账报表")
@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final IEnvProblemService envProblemService;
    private final ITaskInfoService taskInfoService;

    @Operation(summary = "导出问题台账")
    @GetMapping("/problem/export")
    public R<Map<String, Object>> exportProblem() {
        List<EnvProblem> all = envProblemService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("records", all);
        result.put("total", all.size());
        return R.ok("问题台账导出成功", result);
    }

    @Operation(summary = "导出任务台账")
    @GetMapping("/task/export")
    public R<Map<String, Object>> exportTask() {
        List<TaskInfo> all = taskInfoService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("records", all);
        result.put("total", all.size());
        return R.ok("任务台账导出成功", result);
    }

    @Operation(summary = "获取报表")
    @GetMapping("/report")
    public R<Map<String, Object>> report() {
        Map<String, Object> report = new HashMap<>();
        report.put("problemCount", envProblemService.count());
        report.put("taskCount", taskInfoService.count());
        report.put("problemStatistics", envProblemService.statistics());
        return R.ok(report);
    }
}
