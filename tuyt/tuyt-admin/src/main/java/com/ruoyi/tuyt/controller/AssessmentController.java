package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.assessment.entity.*;
import com.ruoyi.tuyt.business.assessment.service.*;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "考评管理")
@RestController
@RequestMapping("/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final IAssessRuleService assessRuleService;
    private final IAssessIndicatorService assessIndicatorService;
    private final IAssessResultService assessResultService;
    private final IAssessTemplateService assessTemplateService;
    private final IAssessTemplateItemService assessTemplateItemService;

    // ==================== 考评规则 ====================

    @Operation(summary = "获取考评规则列表")
    @GetMapping("/rules")
    public R<PageResult<AssessRule>> rules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(assessRuleService.queryPage(keyword, category, pageNum, pageSize));
    }

    @Operation(summary = "保存考评规则（新增/编辑/删除）")
    @PostMapping("/rules")
    public R<Void> saveRule(@RequestBody AssessRule rule) {
        if (rule.getDeleted() != null && rule.getDeleted() == 1) {
            assessRuleService.delete(List.of(rule.getId()));
        } else if (rule.getId() != null) {
            assessRuleService.update(rule);
        } else {
            assessRuleService.add(rule);
        }
        return R.ok();
    }

    // ==================== 考评指标 ====================

    @Operation(summary = "获取考评指标列表")
    @GetMapping("/indicators")
    public R<PageResult<AssessIndicator>> indicators(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String assessType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(assessIndicatorService.queryPage(keyword, assessType, pageNum, pageSize));
    }

    @Operation(summary = "保存考评指标（新增/编辑/删除）")
    @PostMapping("/indicators")
    public R<Void> saveIndicator(@RequestBody AssessIndicator indicator) {
        if (indicator.getDeleted() != null && indicator.getDeleted() == 1) {
            assessIndicatorService.delete(List.of(indicator.getId()));
        } else if (indicator.getId() != null) {
            assessIndicatorService.update(indicator);
        } else {
            assessIndicatorService.add(indicator);
        }
        return R.ok();
    }

    // ==================== 考评结果 ====================

    @Operation(summary = "获取考评结果")
    @GetMapping("/results")
    public R<PageResult<AssessResult>> results(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String gridName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(assessResultService.queryPage(type, month, gridName, pageNum, pageSize));
    }

    @Operation(summary = "执行考评")
    @PostMapping("/run")
    public R<Void> run(@RequestBody Map<String, String> params) {
        String type = params.getOrDefault("type", "月度");
        assessResultService.generateResults(type);
        return R.ok();
    }

    // ==================== 考评模板 ====================

    @Operation(summary = "获取考评模板列表")
    @GetMapping("/templates")
    public R<PageResult<AssessTemplate>> templates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String templateType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(assessTemplateService.queryPage(keyword, templateType, pageNum, pageSize));
    }

    @Operation(summary = "获取所有启用的模板（下拉选项）")
    @GetMapping("/templates/enabled")
    public R<List<AssessTemplate>> templatesEnabled() {
        return R.ok(assessTemplateService.listEnabled());
    }

    @Operation(summary = "保存考评模板（新增/编辑）")
    @PostMapping("/templates")
    public R<AssessTemplate> saveTemplate(@RequestBody AssessTemplate template) {
        if (template.getId() != null) {
            assessTemplateService.update(template);
        } else {
            assessTemplateService.add(template);
        }
        return R.ok(template);
    }

    @Operation(summary = "删除考评模板")
    @DeleteMapping("/templates/{id}")
    public R<Void> deleteTemplate(@PathVariable Long id) {
        assessTemplateService.delete(List.of(id));
        assessTemplateItemService.deleteByTemplateId(id);
        return R.ok();
    }

    @Operation(summary = "获取模板下的考评项")
    @GetMapping("/templates/{templateId}/items")
    public R<List<AssessTemplateItem>> templateItems(@PathVariable Long templateId) {
        return R.ok(assessTemplateItemService.listByTemplateId(templateId));
    }

    @Operation(summary = "保存模板下的考评项")
    @PostMapping("/templates/{templateId}/items")
    public R<Void> saveTemplateItems(@PathVariable Long templateId, @RequestBody List<AssessTemplateItem> items) {
        assessTemplateItemService.saveItems(templateId, items);
        return R.ok();
    }
}
