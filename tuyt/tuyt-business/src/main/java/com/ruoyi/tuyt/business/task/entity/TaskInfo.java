package com.ruoyi.tuyt.business.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_info")
public class TaskInfo extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String taskNo;
    private String taskTitle;
    private String taskType;
    private String urgency;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private LocalDateTime dispatchTime;
    private Long initiatorId;
    private Long handlerId;
    private Long handlerUnitId;
    private Long gridId;
    private Long enterpriseId;
    private Long problemId;
    private Long checkTemplateId;
    private String taskContent;
    private String ccUsers;
    private String status;
    private LocalDateTime finishTime;

    // ===== 非数据库字段（巡查计划展示用） =====

    /** 网格名称（查询时填充） */
    @TableField(exist = false)
    private String gridName;

    /** 催办次数（从taskContent解析） */
    @TableField(exist = false)
    private Integer urgeCount;

    /** 督办次数（从taskContent解析） */
    @TableField(exist = false)
    private Integer superviseCount;

    /** 巡查周期：MONTHLY/QUARTERLY/SEMIANNUAL */
    @TableField(exist = false)
    private String cycle;

    /** 检查模板名称 */
    @TableField(exist = false)
    private String checkTemplateName;

    /** 上次执行时间 */
    @TableField(exist = false)
    private LocalDateTime lastExecuteTime;

    /** 下次执行时间 */
    @TableField(exist = false)
    private LocalDateTime nextExecuteTime;

    /** 监管企业数量 */
    @TableField(exist = false)
    private Integer enterpriseCount;

    /** 监管企业列表 */
    @TableField(exist = false)
    private List<Map<String, Object>> enterprises;

    // ===== 运转件专用字段（关联 task_transfer 表） =====

    /** 运转类型：RETURN=退回, FORWARD=转交 */
    @TableField(exist = false)
    private String transferType;

    /** 运转时间 */
    @TableField(exist = false)
    private LocalDateTime transferTime;

    /** 运转说明/原因 */
    @TableField(exist = false)
    private String transferReason;
}
