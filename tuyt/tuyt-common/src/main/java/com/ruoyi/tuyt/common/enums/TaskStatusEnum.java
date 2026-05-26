package com.ruoyi.tuyt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {

    DRAFT("DRAFT", "已拟定"),
    DISPATCHED("DISPATCHED", "已派发"),
    SIGNED("SIGNED", "已签收"),
    DONE("DONE", "已完成"),
    REVOKED("REVOKED", "已撤销"),
    RETURNED("RETURNED", "已退回");

    private final String code;
    private final String name;

    public static TaskStatusEnum fromCode(String code) {
        for (TaskStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
