package com.ruoyi.tuyt.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.ruoyi.tuyt.business.system.entity.SysUser;
import com.ruoyi.tuyt.business.system.service.ISysUserService;
import com.ruoyi.tuyt.common.result.R;
import com.ruoyi.tuyt.framework.config.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "登录管理")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final StringRedisTemplate stringRedisTemplate;
    private final ISysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // 从数据库查询用户
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return R.fail(401, "用户名或密码错误");
        }
        // 密码 MD5 校验
        if (!DigestUtil.md5Hex(password).equals(user.getPassword())) {
            return R.fail(401, "用户名或密码错误");
        }

        String token = JwtTokenUtil.createToken(user.getId(), username);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName() != null ? user.getRealName() : user.getUsername());
        userInfo.put("phone", user.getPhone());
        userInfo.put("orgId", user.getOrgId());
        result.put("userInfo", userInfo);
        return R.ok("登录成功", result);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user/info")
    public R<Map<String, Object>> userInfo(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> info = new HashMap<>();
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Long userId = JwtTokenUtil.getUserId(token);
        SysUser user = sysUserService.getById(userId);
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        info.put("phone", user.getPhone());
        info.put("roles", new String[]{"admin"});
        return R.ok(info);
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public R<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && JwtTokenUtil.verify(token)) {
            long remainingMillis = JwtTokenUtil.getRemainingMillis(token);
            if (remainingMillis > 0) {
                String blacklistKey = JwtTokenUtil.BLACKLIST_PREFIX + token;
                try {
                    stringRedisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofMillis(remainingMillis));
                } catch (Exception e) {
                    log.warn("Redis 不可用，无法写入 Token 黑名单: {}", e.getMessage());
                }
            }
        }

        return R.ok("退出成功");
    }
}
