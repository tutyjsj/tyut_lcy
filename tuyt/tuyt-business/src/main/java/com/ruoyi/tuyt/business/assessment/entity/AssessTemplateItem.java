package com.ruoyi.tuyt.business.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 考评模板项（模板包含的考评指标及权重）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_template_item")
public class AssessTemplateItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 模板ID */
    private Long templateId;
    /** 考评项名称 */
    private String itemName;
    /** 考评项描述 */
    private String itemDesc;
    /** 权重（%） */
    private BigDecimal weight;
    /** 排序 */
    private Integer sortOrder;
}
