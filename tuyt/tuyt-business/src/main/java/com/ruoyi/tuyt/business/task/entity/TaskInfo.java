package com.ruoyi.tuyt.business.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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
}
