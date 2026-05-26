package com.ruoyi.tuyt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问题严重程度等级
 */
@Getter
@AllArgsConstructor
public enum ProblemLevelEnum {

    I("I", "严重", "#FF0000"),
    II("II", "较严重", "#FFA500"),
    III("III", "一般", "#0066FF");

    private final String code;
    private final String name;
    private final String color;

    public static ProblemLevelEnum fromCode(String code) {
        for (ProblemLevelEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
