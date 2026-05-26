package com.ruoyi.tuyt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问题处理状态
 */
@Getter
@AllArgsConstructor
public enum HandleStatusEnum {

    PENDING("PENDING", "待处理"),
    PROCESSED("PROCESSED", "已处理"),
    DONE("DONE", "处理完成"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String name;

    public static HandleStatusEnum fromCode(String code) {
        for (HandleStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
