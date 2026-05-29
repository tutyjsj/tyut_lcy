package com.ruoyi.tuyt.business.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise")
public class Enterprise extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String enterpriseCode;
    private String enterpriseName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String pollutionType;
    private String superviseType;
    private String enterpriseType;
    private String legalPerson;
    private String legalPhone;
    private String creditLevel;
    private Integer productionStatus;
    private String sourceType;
    private Integer hasVideo;
    private Integer hasMonitor;
    private Integer hasWorking;
    private Integer status;

    /** 所属网格名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String gridName;
}
