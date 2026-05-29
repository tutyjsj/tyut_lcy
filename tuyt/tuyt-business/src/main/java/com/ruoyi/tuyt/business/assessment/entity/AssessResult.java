package com.ruoyi.tuyt.business.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_result")
public class AssessResult extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long gridId;
    private String gridName;
    private Long ruleId;
    private BigDecimal score;
    private String level;
    private String assessPeriod;
    /** 响应率 % */
    private BigDecimal responseRate;
    /** 处置及时率 % */
    private BigDecimal disposalRate;
    /** 任务完成率 % */
    private BigDecimal completeRate;
}
