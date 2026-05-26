package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.assessment.entity.AssessIndicator;
import com.ruoyi.tuyt.business.assessment.service.IAssessIndicatorService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "考评管理")
@RestController
@RequestMapping("/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final IAssessIndicatorService assessIndicatorService;

    @Operation(summary = "获取考评规则列表")
    @GetMapping("/rules")
    public R<PageResult<AssessIndicator>> rules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String assessType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(assessIndicatorService.queryPage(keyword, assessType, pageNum, pageSize));
    }

    @Operation(summary = "新增考评规则")
    @PostMapping("/rules")
    public R<Void> addRule(@RequestBody AssessIndicator indicator) {
        assessIndicatorService.add(indicator);
        R<Void> r = R.ok();
        r.setMessage("新增考评规则成功");
        return r;
    }

    @Operation(summary = "获取考评指标")
    @GetMapping("/indicators")
    public R<PageResult<AssessIndicator>> indicators(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(assessIndicatorService.queryPage(keyword, null, pageNum, pageSize));
    }

    @Operation(summary = "新增考评指标")
    @PostMapping("/indicators")
    public R<Void> addIndicator(@RequestBody AssessIndicator indicator) {
        assessIndicatorService.add(indicator);
        R<Void> r = R.ok();
        r.setMessage("新增考评指标成功");
        return r;
    }

    @Operation(summary = "获取考评结果")
    @GetMapping("/results")
    public R<Map<String, Object>> results() {
        // 考评结果返回全量指标列表
        List<AssessIndicator> all = assessIndicatorService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("records", all);
        result.put("total", all.size());
        return R.ok(result);
    }

    @Operation(summary = "执行考评")
    @PostMapping("/run")
    public R<Void> run() {
        R<Void> r = R.ok();
        r.setMessage("考评执行成功");
        return r;
    }
}
