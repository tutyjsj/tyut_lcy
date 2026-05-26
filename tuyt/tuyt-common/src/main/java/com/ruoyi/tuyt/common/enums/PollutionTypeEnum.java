package com.ruoyi.tuyt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 污染类型
 */
@Getter
@AllArgsConstructor
public enum PollutionTypeEnum {

    WASTE_WATER("WASTE_WATER", "废水污染"),
    WASTE_GAS("WASTE_GAS", "废气污染"),
    NOISE("NOISE", "噪声污染"),
    SOLID_HAZARDOUS_WASTE("SOLID_HAZARDOUS_WASTE", "固危废污染"),
    RADIATION("RADIATION", "放辐射污染"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String name;

    public static PollutionTypeEnum fromCode(String code) {
        for (PollutionTypeEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
