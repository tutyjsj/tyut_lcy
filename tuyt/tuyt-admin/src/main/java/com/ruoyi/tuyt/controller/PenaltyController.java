package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.penalty.entity.PenaltyCase;
import com.ruoyi.tuyt.business.penalty.service.IPenaltyCaseService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "行政处罚")
@RestController
@RequestMapping("/penalty")
@RequiredArgsConstructor
public class PenaltyController {

    private final IPenaltyCaseService penaltyCaseService;

    @Operation(summary = "分页查询处罚案件")
    @GetMapping("/list")
    public R<PageResult<PenaltyCase>> list(
            @RequestParam(required = false) String caseNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String penaltyType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(penaltyCaseService.queryPage(caseNo, status, penaltyType, pageNum, pageSize));
    }

    @Operation(summary = "获取案件详情")
    @GetMapping("/{id}")
    public R<PenaltyCase> getById(@PathVariable Long id) {
        return R.ok(penaltyCaseService.getDetail(id));
    }

    @Operation(summary = "立案")
    @PostMapping("/file")
    public R<Void> file(@RequestBody PenaltyCase penaltyCase) {
        penaltyCaseService.file(penaltyCase);
        R<Void> r = R.ok();
        r.setMessage("立案成功");
        return r;
    }

    @Operation(summary = "编辑案件")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody PenaltyCase penaltyCase) {
        penaltyCase.setId(id);
        penaltyCaseService.updateCase(penaltyCase);
        R<Void> r = R.ok();
        r.setMessage("更新成功");
        return r;
    }

    @Operation(summary = "作出处罚裁决")
    @PostMapping("/{id}/rule")
    public R<Void> rule(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String rulingResult = body.getOrDefault("rulingResult", "");
        penaltyCaseService.rule(id, rulingResult);
        R<Void> r = R.ok();
        r.setMessage("裁决已作出");
        return r;
    }

    @Operation(summary = "结案")
    @PutMapping("/{id}/close")
    public R<Void> close(@PathVariable Long id) {
        penaltyCaseService.close(id);
        R<Void> r = R.ok();
        r.setMessage("已结案");
        return r;
    }

    @Operation(summary = "删除案件（支持批量）")
    @DeleteMapping("/batch")
    public R<Void> delete(@RequestBody List<Long> ids) {
        penaltyCaseService.deleteCases(ids);
        R<Void> r = R.ok();
        r.setMessage("删除成功");
        return r;
    }
}
