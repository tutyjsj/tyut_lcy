package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "企业管理")
@RestController
@RequestMapping("/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final IEnterpriseService enterpriseService;

    @Operation(summary = "分页查询企业列表")
    @GetMapping("/list")
    public R<PageResult<Enterprise>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String superviseType,
            @RequestParam(required = false) String enterpriseType,
            @RequestParam(required = false) Long gridId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String excludeStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(enterpriseService.queryPage(name, superviseType, enterpriseType, gridId, status, excludeStatus, pageNum, pageSize));
    }

    @Operation(summary = "获取企业详情")
    @GetMapping("/{id}")
    public R<Enterprise> getById(@PathVariable Long id) {
        return R.ok(enterpriseService.getById(id));
    }

    @Operation(summary = "新增企业")
    @PostMapping
    public R<Void> add(@RequestBody Enterprise enterprise) {
        enterpriseService.add(enterprise);
        R<Void> r = R.ok();
        r.setMessage("新增企业成功");
        return r;
    }

    @Operation(summary = "更新企业")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Enterprise enterprise) {
        enterprise.setId(id);
        enterpriseService.update(enterprise);
        R<Void> r = R.ok();
        r.setMessage("更新企业成功");
        return r;
    }

    @Operation(summary = "删除企业")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除企业成功");
        return r;
    }
}
