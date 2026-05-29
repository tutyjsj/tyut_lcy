package com.ruoyi.tuyt.controller;

import com.alibaba.excel.EasyExcel;
import com.ruoyi.tuyt.business.problem.entity.ProblemExportVO;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.business.task.entity.TaskExportVO;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.business.system.service.ISysOrganizationService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ISysOrganizationService organizationService;

    @Operation(summary = "导出问题台账（生成 Excel 文件并下载）")
    @GetMapping("/problem/export")
    public void exportProblem(
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
            HttpServletResponse response) throws IOException {

        // 查询导出数据
        List<ProblemExportVO> exportList = envProblemService.exportProblems(
                problemNo, enterpriseName, enterpriseId, areaName, problemLevel,
                pollutionType, problemType, problemSource, handleStatus, gridId);

        // 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "问题台账_" + timestamp;
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        // 写入 Excel
        EasyExcel.write(response.getOutputStream(), ProblemExportVO.class)
                .sheet("问题台账")
                .doWrite(exportList);
    }

    @Operation(summary = "导出任务台账（生成 Excel 文件并下载）")
    @GetMapping("/task/export")
    public void exportTask(
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String overdueType,
            HttpServletResponse response) throws IOException {

        // 查询导出数据
        List<TaskExportVO> exportList = taskInfoService.exportTasks(taskNo, title, taskType, status, urgency, overdueType);

        // 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "任务台账_" + timestamp;
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        // 写入 Excel
        EasyExcel.write(response.getOutputStream(), TaskExportVO.class)
                .sheet("任务台账")
                .doWrite(exportList);
    }

    @Operation(summary = "任务报表统计")
    @GetMapping("/report")
    public R<Map<String, Object>> report(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Long unitId) {
        return R.ok(taskInfoService.taskReport(month, unitId));
    }

    @Operation(summary = "按机构查看任务列表")
    @GetMapping("/report/tasks")
    public R<PageResult<TaskInfo>> reportTasks(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskInfoService.queryTasksByOrg(orgId, status, month, pageNum, pageSize));
    }

    @Operation(summary = "获取机构树（报表筛选用）")
    @GetMapping("/report/org-tree")
    public R<List<Map<String, Object>>> orgTree(@RequestParam(required = false) Long parentId) {
        return R.ok(organizationService.getOrgTree(parentId));
    }
}
