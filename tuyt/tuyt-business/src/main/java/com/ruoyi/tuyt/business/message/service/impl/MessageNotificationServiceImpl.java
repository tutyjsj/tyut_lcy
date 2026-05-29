package com.ruoyi.tuyt.business.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.message.entity.MessageNotification;
import com.ruoyi.tuyt.business.message.mapper.MessageNotificationMapper;
import com.ruoyi.tuyt.business.message.service.IMessageNotificationService;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageNotificationServiceImpl
        extends ServiceImpl<MessageNotificationMapper, MessageNotification>
        implements IMessageNotificationService {

    @Override
    public PageResult<MessageNotification> queryPage(Long targetUserId, String type, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<MessageNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageNotification::getTargetUserId, targetUserId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(MessageNotification::getType, type);
        }
        wrapper.orderByDesc(MessageNotification::getCreateTime);
        Page<MessageNotification> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public Long getUnreadCount(Long targetUserId) {
        return count(new LambdaQueryWrapper<MessageNotification>()
                .eq(MessageNotification::getTargetUserId, targetUserId)
                .eq(MessageNotification::getReadStatus, 0));
    }

    @Override
    @Transactional
    public void markRead(List<Long> ids, Long targetUserId) {
        LambdaUpdateWrapper<MessageNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(MessageNotification::getId, ids)
               .eq(MessageNotification::getTargetUserId, targetUserId)
               .set(MessageNotification::getReadStatus, 1)
               .set(MessageNotification::getReadTime, LocalDateTime.now());
        update(wrapper);
    }

    @Override
    @Transactional
    public void markAllRead(Long targetUserId) {
        LambdaUpdateWrapper<MessageNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MessageNotification::getTargetUserId, targetUserId)
               .eq(MessageNotification::getReadStatus, 0)
               .set(MessageNotification::getReadStatus, 1)
               .set(MessageNotification::getReadTime, LocalDateTime.now());
        update(wrapper);
    }
}
