package com.ruoyi.tuyt.business.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.message.entity.MessageNotification;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;
import java.util.Map;

public interface IMessageNotificationService extends IService<MessageNotification> {

    PageResult<MessageNotification> queryPage(Long targetUserId, String type, Integer pageNum, Integer pageSize);

    Long getUnreadCount(Long targetUserId);

    void markRead(List<Long> ids, Long targetUserId);

    void markAllRead(Long targetUserId);
}
