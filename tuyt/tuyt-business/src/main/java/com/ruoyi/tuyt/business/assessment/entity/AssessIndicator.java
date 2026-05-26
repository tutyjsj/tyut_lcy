package com.ruoyi.tuyt.business.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_indicator")
public class AssessIndicator extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String indicatorName;
    private String assessType;
    private Integer isValid;
}
