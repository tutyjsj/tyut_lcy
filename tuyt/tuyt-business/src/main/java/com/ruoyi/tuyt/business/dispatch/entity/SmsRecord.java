package com.ruoyi.tuyt.business.dispatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信调度记录
 */
@Data
@TableName("sms_record")
public class SmsRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 短信内容 */
    private String content;

    /** 发送人ID */
    private Long senderId;

    /** 发送人姓名 */
    private String senderName;

    /** 收件人数量 */
    private Integer recipientCount;

    /** 收件人列表(逗号分隔的姓名) */
    private String recipientNames;

    /** 收件人号码列表(comma-separated) */
    private String recipientPhones;

    /** 关联任务ID */
    private Long taskId;

    /** 关联问题ID */
    private Long problemId;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /** 状态: SUCCESS/FAILED */
    private String status;

    /** 短信类型: MANUAL(手动编辑)/TEMPLATE(模板发送)/BATCH(批量发送) */
    private String smsType;
}
