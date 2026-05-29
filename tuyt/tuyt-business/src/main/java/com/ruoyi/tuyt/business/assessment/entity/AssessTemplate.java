package com.ruoyi.tuyt.business.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考评模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_template")
public class AssessTemplate extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 模板名称 */
    private String templateName;
    /** 模板类型：月度/半年/年度 */
    private String templateType;
    /** 模板描述 */
    private String templateDesc;
    /** 状态：1启用 0停用 */
    private Integer status;
}
