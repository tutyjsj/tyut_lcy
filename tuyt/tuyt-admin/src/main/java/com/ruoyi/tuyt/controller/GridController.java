package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.grid.entity.GridInfo;
import com.ruoyi.tuyt.business.grid.service.IGridInfoService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "网格管理")
@RestController
@RequestMapping("/grid")
@RequiredArgsConstructor
public class GridController {

    private final IGridInfoService gridInfoService;

    @Operation(summary = "分页查询网格列表")
    @GetMapping("/list")
    public R<PageResult<GridInfo>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer gridLevel,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(gridInfoService.queryPage(keyword, gridLevel, pageNum, pageSize));
    }

    @Operation(summary = "获取网格树")
    @GetMapping("/tree")
    public R<List<GridInfo>> tree() {
        return R.ok(gridInfoService.queryTree());
    }

    @Operation(summary = "获取网格详情")
    @GetMapping("/{id}")
    public R<GridInfo> getById(@PathVariable Long id) {
        return R.ok(gridInfoService.getById(id));
    }

    @Operation(summary = "新增网格")
    @PostMapping
    public R<Void> add(@RequestBody GridInfo gridInfo) {
        gridInfoService.add(gridInfo);
        R<Void> r = R.ok();
        r.setMessage("新增网格成功");
        return r;
    }

    @Operation(summary = "更新网格")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody GridInfo gridInfo) {
        gridInfo.setId(id);
        gridInfoService.update(gridInfo);
        R<Void> r = R.ok();
        r.setMessage("更新网格成功");
        return r;
    }

    @Operation(summary = "删除网格")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        gridInfoService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除网格成功");
        return r;
    }
}
