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

@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskInfoService taskInfoService;

    @Operation(summary = "分页查询任务列表")
    @GetMapping("/list")
    public R<PageResult<TaskInfo>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskInfoService.queryPage(keyword, status, pageNum, pageSize));
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

    @Operation(summary = "催办任务")
    @PostMapping("/{id}/urge")
    public R<Void> urge(@PathVariable Long id) {
        taskInfoService.urge(id);
        R<Void> r = R.ok();
        r.setMessage("催办成功");
        return r;
    }

    @Operation(summary = "督办任务")
    @PostMapping("/{id}/supervise")
    public R<Void> supervise(@PathVariable Long id) {
        taskInfoService.supervise(id);
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

    @Operation(summary = "退回任务")
    @PostMapping("/{id}/return")
    public R<Void> returnTask(@PathVariable Long id, @RequestParam String reason) {
        taskInfoService.returnTask(id, reason);
        R<Void> r = R.ok();
        r.setMessage("退回任务成功");
        return r;
    }

    @Operation(summary = "审核退回")
    @PostMapping("/{id}/audit-return")
    public R<Void> auditReturn(@PathVariable Long id) {
        taskInfoService.auditReturn(id, "approved");
        R<Void> r = R.ok();
        r.setMessage("审核退回成功");
        return r;
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        taskInfoService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除任务成功");
        return r;
    }
}
