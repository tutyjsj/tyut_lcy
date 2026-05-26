package com.ruoyi.tuyt.business.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("env_problem")
public class EnvProblem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String problemNo;
    private String problemLevel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;
    private String problemSource;
    private String problemSourceDetail;
    private String problemType;
    private String pollutionType;
    private String problemDesc;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Long enterpriseId;
    @TableField(exist = false)
    private String enterpriseName;
    private String areaCode;
    private String areaName;
    private Long mergeId;
    private String closeReason;
    private String handleStatus;
    private String penaltyStatus;
    private Long createUserId;
}
