package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "系统健康检查")
@RestController
@RequestMapping("/health")
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping
    public R<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("app", "环境网格化综合管理系统");
        info.put("version", "1.0.0");
        info.put("java", System.getProperty("java.version"));
        return R.ok(info);
    }
}
