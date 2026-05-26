package com.ruoyi.tuyt.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int SUCCESS = 200;
    private static final int FAIL = 500;

    private int code;
    private String message;
    private T data;
    private long timestamp;

    private R() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = SUCCESS;
        r.message = "操作成功";
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.data = data;
        return r;
    }

    public static <T> R<T> ok(String message, T data) {
        R<T> r = ok(data);
        r.message = message;
        return r;
    }

    public static <T> R<T> fail() {
        R<T> r = new R<>();
        r.code = FAIL;
        r.message = "操作失败";
        return r;
    }

    public static <T> R<T> fail(String message) {
        R<T> r = fail();
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = fail(message);
        r.code = code;
        return r;
    }

    public static <T> R<T> fail(int code, String message, T data) {
        R<T> r = fail(code, message);
        r.data = data;
        return r;
    }
}
