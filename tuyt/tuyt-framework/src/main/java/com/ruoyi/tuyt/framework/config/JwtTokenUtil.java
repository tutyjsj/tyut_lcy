package com.ruoyi.tuyt.framework.config;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;

import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtTokenUtil {

    private static final String SECRET = "tuyt-env-grid-secret-key-2024";

    public static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 7天

    private static final JWTSigner SIGNER = JWTSignerUtil.hs256(SECRET.getBytes());

    /** Token 黑名单 key 前缀 */
    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * 生成 Token
     */
    public static String createToken(Long userId, String username) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + EXPIRE_TIME);
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("username", username)
                .setIssuedAt(now)
                .setExpiresAt(expire)
                .setSigner(SIGNER)
                .sign();
    }

    /**
     * 验证 Token 签名和有效期
     */
    public static boolean verify(String token) {
        try {
            return JWTUtil.verify(token, SIGNER);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Token 剩余有效时间（毫秒），如果已过期返回 0
     */
    public static long getRemainingMillis(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object exp = jwt.getPayload("exp");
            if (exp == null) return 0;
            long expireAtMs = Long.parseLong(exp.toString()) * 1000L;
            long remaining = expireAtMs - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 解析 Token 获取 userId
     */
    public static Long getUserId(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            return Long.valueOf(jwt.getPayload("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 Token 获取 username
     */
    public static String getUsername(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            return jwt.getPayload("username").toString();
        } catch (Exception e) {
            return null;
        }
    }
}
