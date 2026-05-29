package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "巡查计划")
@RestController
@RequestMapping("/patrol-plan")
@RequiredArgsConstructor
public class PatrolPlanController {

    private final ITaskInfoService taskInfoService;

    @Operation(summary = "获取巡查计划列表（支持按配置类型、标题、启用时间、周期、状态筛选）")
    @GetMapping("/list")
    public R<PageResult<TaskInfo>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String cycle,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(taskInfoService.queryPatrolPlans(type, title, startTime, cycle, status, pageNum, pageSize));
    }

    @Operation(summary = "新增巡查计划")
    @PostMapping
    public R<Void> add(@RequestBody TaskInfo task) {
        taskInfoService.dispatchPatrolPlan(task);
        R<Void> r = R.ok();
        r.setMessage("新增巡查计划成功，系统将按计划自动派发巡查任务");
        return r;
    }

    @Operation(summary = "编辑巡查计划")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody TaskInfo task) {
        task.setId(id);
        taskInfoService.updatePatrolPlan(task);
        R<Void> r = R.ok();
        r.setMessage("修改巡查计划成功");
        return r;
    }

    @Operation(summary = "删除巡查计划")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 巡查计划状态为 ENABLED/DISABLED，不能走 taskInfoService.delete()（那个只允许DRAFT）
        taskInfoService.removeById(id);
        R<Void> r = R.ok();
        r.setMessage("删除巡查计划成功，已派发的任务不受影响");
        return r;
    }
}
