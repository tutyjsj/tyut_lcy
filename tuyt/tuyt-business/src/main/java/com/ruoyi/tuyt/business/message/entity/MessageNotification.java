package com.ruoyi.tuyt.business.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_notification")
public class MessageNotification extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 消息类型: URGE/SUPERVISE/CALENDAR/DEADLINE/SMS */
    private String type;
    /** 消息标题 */
    private String title;
    /** 消息内容 */
    private String content;
    /** 来源人姓名 */
    private String sourceName;
    /** 来源人ID */
    private Long sourceId;
    /** 接收用户ID */
    private Long targetUserId;
    /** 关联业务ID */
    private Long relatedId;
    /** 关联类型: task/problem/sms/calendar */
    private String relatedType;
    /** 0未读 1已读 */
    private Integer readStatus;
    /** 阅读时间 */
    private LocalDateTime readTime;
}
