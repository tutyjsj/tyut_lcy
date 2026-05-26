package com.ruoyi.tuyt.business.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_process")
public class TaskProcess {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    private LocalDate processDate;
    private String conclusion;
    private String suggestion;
    private String rectification;
    private LocalDateTime rectificationDeadline;
    private String productionStatus;
    private Integer isSignin;
    private LocalDateTime signinTime;
    private Long handlerId;
    private LocalDateTime createTime;
}
