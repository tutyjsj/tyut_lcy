package com.ruoyi.tuyt.business.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_transfer")
public class TaskTransfer {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    private String transferType;
    private Long fromUserId;
    private Long toUserId;
    private String reason;
    private String suggestHandler;
    private String suggestUnit;
    private String auditResult;
    private LocalDateTime createTime;
}
