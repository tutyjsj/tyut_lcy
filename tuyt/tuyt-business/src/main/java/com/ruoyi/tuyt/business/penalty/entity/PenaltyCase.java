package com.ruoyi.tuyt.business.penalty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("penalty_case")
public class PenaltyCase extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 案号 */
    private String caseNo;

    /** 关联任务ID */
    private Long taskId;

    /** 关联问题ID */
    private Long problemId;

    /** 关联企业ID */
    private Long enterpriseId;

    /** 处罚类型：WARNING-警告, FINE-罚款, SHUTDOWN-停产整顿, CLOSE-关闭 */
    private String penaltyType;

    /** 罚款金额 */
    private BigDecimal penaltyAmount;

    /** 处罚内容 */
    private String penaltyContent;

    /** 法律依据 */
    private String legalBasis;

    /** 案件状态：FILED-立案, INVESTIGATING-调查中, PENALIZED-已处罚, CLOSED-结案 */
    private String status;

    /** 立案人ID */
    private Long applicantId;

    /** 立案人姓名 */
    private String applicantName;

    /** 案件描述 */
    private String caseDesc;

    /** 听证时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hearingDate;

    /** 裁决时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rulingDate;

    /** 裁决结果 */
    private String rulingResult;

    // ===== 关联展示字段 =====

    @TableField(exist = false)
    private String taskTitle;

    @TableField(exist = false)
    private String taskNo;

    @TableField(exist = false)
    private String enterpriseName;

    @TableField(exist = false)
    private String problemDesc;
}
