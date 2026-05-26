package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.service.IEnvProblemService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "环境问题管理")
@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
public class ProblemController {

    private final IEnvProblemService envProblemService;

    @Operation(summary = "分页查询问题列表")
    @GetMapping("/list")
    public R<PageResult<EnvProblem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String problemLevel,
            @RequestParam(required = false) String handleStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(envProblemService.queryPage(keyword, problemLevel, handleStatus, pageNum, pageSize));
    }

    @Operation(summary = "获取问题详情")
    @GetMapping("/{id}")
    public R<EnvProblem> getById(@PathVariable Long id) {
        return R.ok(envProblemService.getById(id));
    }

    @Operation(summary = "新增问题")
    @PostMapping("/")
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

    @Operation(summary = "删除问题")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        envProblemService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除问题成功");
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

    @Operation(summary = "合并问题")
    @PostMapping("/merge")
    public R<Void> merge(@RequestBody Map<String, Object> params) {
        List<Long> ids = convertToLongList(params.get("ids"));
        Long targetId = Long.valueOf(params.get("targetId").toString());
        envProblemService.merge(ids, targetId);
        R<Void> r = R.ok();
        r.setMessage("合并问题成功");
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

    @Operation(summary = "网格排名")
    @GetMapping("/ranking")
    public R<List<Map<String, Object>>> ranking(@RequestParam(defaultValue = "10") Integer top) {
        return R.ok(envProblemService.ranking(top));
    }
}
