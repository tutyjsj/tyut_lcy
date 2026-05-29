package com.ruoyi.tuyt.business.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("env_problem_log")
public class EnvProblemLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long problemId;
    /** 操作类型: warn/update/dispatch/process/close/merge */
    private String operationType;
    private String content;
    private Long operatorId;
    private LocalDateTime createTime;
}
