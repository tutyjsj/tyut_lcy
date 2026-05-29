package com.ruoyi.tuyt.business.checktemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检查模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_template")
public class CheckTemplate extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 录入类型（RADIO/CHECKBOX/TEXT） */
    private String inputType;

    /** 是否正常（1正常 0异常） */
    private Integer isNormal;
}
