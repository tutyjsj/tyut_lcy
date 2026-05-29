package com.ruoyi.tuyt.business.dispatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通话记录
 */
@Data
@TableName("call_record")
public class CallRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 主叫姓名 */
    private String callerName;

    /** 主叫号码 */
    private String callerPhone;

    /** 被叫姓名 */
    private String calleeName;

    /** 被叫号码 */
    private String calleePhone;

    /** 被叫职位 */
    private String calleePosition;

    /** 被叫机构 */
    private String calleeOrgName;

    /** 通话时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;

    /** 通话类型: OUTGOING(呼出)/INCOMING(呼入) */
    private String callType;

    /** 通话时长(秒) */
    private Integer duration;
}
