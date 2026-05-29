package com.ruoyi.tuyt.framework.config;

import com.ruoyi.tuyt.common.result.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器 - 校验 JWT Token + Redis 黑名单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String[] WHITE_LIST = {
            "/api/login",
            "/api/doc.html",
            "/api/v3/api-docs",
            "/api/swagger-ui",
            "/api/webjars",
            "/api/websocket"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 白名单放行
        for (String white : WHITE_LIST) {
            if (uri.startsWith(white)) {
                return true;
            }
        }

        // 获取 Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        // 验证 Token 签名和有效期
        if (!JwtTokenUtil.verify(token)) {
            writeUnauthorized(response, "Token已过期或无效，请重新登录");
            return false;
        }

        // 检查 Redis 黑名单（用户已退出登录的 Token）
        // 如果 Redis 不可用，跳过黑名单检查，不阻断请求
        try {
            String blacklistKey = JwtTokenUtil.BLACKLIST_PREFIX + token;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
                writeUnauthorized(response, "Token已失效，请重新登录");
                return false;
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过 Token 黑名单检查: {}", e.getMessage());
        }

        // 存入 ThreadLocal（后续可以在 Controller 中获取）
        LoginUserHolder.set(JwtTokenUtil.getUserId(token), JwtTokenUtil.getUsername(token));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        R<Void> result = R.fail(401, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
