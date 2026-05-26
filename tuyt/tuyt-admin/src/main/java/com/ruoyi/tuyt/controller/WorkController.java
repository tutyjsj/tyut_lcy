package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "我的工作")
@RestController
@RequestMapping("/work")
@RequiredArgsConstructor
public class WorkController {

    private final ITaskInfoService taskInfoService;

    @Operation(summary = "我的待办")
    @GetMapping("/todo")
    public R<PageResult<TaskInfo>> todo(
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String taskType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String keyword = (taskNo != null ? taskNo : "")
                + (title != null ? " " + title : "")
                + (taskType != null ? " " + taskType : "");
        return R.ok(taskInfoService.queryPage(keyword.trim().isEmpty() ? null : keyword.trim(), "DISPATCHED", pageNum, pageSize));
    }

    @Operation(summary = "处理待办任务")
    @PutMapping("/todo/{id}")
    public R<Void> processTask(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        taskInfoService.processTask(id, data);
        R<Void> r = R.ok();
        r.setMessage("处理成功");
        return r;
    }

    @Operation(summary = "我的转办")
    @GetMapping("/transfer")
    public R<PageResult<TaskInfo>> transfer(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskInfoService.queryPage(null, "RETURNED", pageNum, pageSize));
    }

    @Operation(summary = "我的已办")
    @GetMapping("/done")
    public R<PageResult<TaskInfo>> done(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskInfoService.queryPage(null, "DONE", pageNum, pageSize));
    }
}
