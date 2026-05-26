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

@Tag(name = "巡查计划")
@RestController
@RequestMapping("/patrol-plan")
@RequiredArgsConstructor
public class PatrolPlanController {

    private final ITaskInfoService taskInfoService;

    @Operation(summary = "获取巡查计划列表")
    @GetMapping("/list")
    public R<PageResult<TaskInfo>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 巡查计划即已派发的任务
        return R.ok(taskInfoService.queryPage(null, "DISPATCHED", pageNum, pageSize));
    }

    @Operation(summary = "新增巡查计划")
    @PostMapping("/")
    public R<Void> add(@RequestBody TaskInfo task) {
        taskInfoService.dispatch(task);
        R<Void> r = R.ok();
        r.setMessage("新增巡查计划成功");
        return r;
    }

    @Operation(summary = "删除巡查计划")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        taskInfoService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除巡查计划成功");
        return r;
    }
}
