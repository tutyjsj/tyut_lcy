package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskInfoService taskInfoService;

    @Operation(summary = "分页查询任务列表")
    @GetMapping("/list")
    public R<PageResult<TaskInfo>> list(
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String overdueType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskInfoService.queryPage(taskNo, title, taskType, status, urgency, overdueType, pageNum, pageSize));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public R<TaskInfo> getById(@PathVariable Long id) {
        return R.ok(taskInfoService.getById(id));
    }

    @Operation(summary = "派发任务")
    @PostMapping("/dispatch")
    public R<Void> dispatch(@RequestBody TaskInfo task) {
        taskInfoService.dispatch(task);
        R<Void> r = R.ok();
        r.setMessage("派发任务成功");
        return r;
    }

    @Operation(summary = "编辑任务")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody TaskInfo task) {
        task.setId(id);
        taskInfoService.updateTask(task);
        return R.ok();
    }

    @Operation(summary = "催办任务")
    @PostMapping("/{id}/urge")
    public R<Void> urge(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = (String) body.getOrDefault("reason", "");
        taskInfoService.urge(id, reason);
        R<Void> r = R.ok();
        r.setMessage("催办成功");
        return r;
    }

    @Operation(summary = "督办任务")
    @PostMapping("/{id}/supervise")
    public R<Void> supervise(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = (String) body.getOrDefault("reason", "");
        taskInfoService.supervise(id, reason);
        R<Void> r = R.ok();
        r.setMessage("督办成功");
        return r;
    }

    @Operation(summary = "撤销任务")
    @PutMapping("/{id}/revoke")
    public R<Void> revoke(@PathVariable Long id) {
        taskInfoService.revoke(id);
        R<Void> r = R.ok();
        r.setMessage("撤销任务成功");
        return r;
    }

    @Operation(summary = "发布任务（拟定→派发）")
    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        taskInfoService.publish(id);
        R<Void> r = R.ok();
        r.setMessage("发布任务成功");
        return r;
    }

    @Operation(summary = "退回任务")
    @PostMapping("/{id}/return")
    public R<Void> returnTask(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = (String) body.getOrDefault("reason", "");
        String suggestHandler = (String) body.getOrDefault("suggestHandler", "");
        String suggestUnit = (String) body.getOrDefault("suggestUnit", "");
        taskInfoService.returnTask(id, reason, suggestHandler, suggestUnit);
        R<Void> r = R.ok();
        r.setMessage("任务已退回，调度人员将收到通知");
        return r;
    }

    @Operation(summary = "审核退回")
    @PostMapping("/{id}/audit-return")
    public R<Void> auditReturn(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String auditResult = (String) body.getOrDefault("auditResult", "APPROVED");
        String auditComment = (String) body.getOrDefault("auditComment", "");
        taskInfoService.auditReturn(id, auditResult, auditComment);
        R<Void> r = R.ok();
        r.setMessage("审核完成，退回人员已收到通知");
        return r;
    }

    @Operation(summary = "删除任务（支持批量，仅已拟定状态可删）")
    @DeleteMapping("/batch")
    public R<Void> delete(@RequestBody List<Long> ids) {
        taskInfoService.delete(ids);
        R<Void> r = R.ok();
        r.setMessage("删除任务成功");
        return r;
    }
}
