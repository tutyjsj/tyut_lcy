package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.message.entity.MessageNotification;
import com.ruoyi.tuyt.business.message.service.IMessageNotificationService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import com.ruoyi.tuyt.framework.config.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息通知")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageNotificationService messageService;

    @Operation(summary = "分页查询消息列表")
    @GetMapping("/list")
    public R<PageResult<MessageNotification>> list(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = LoginUserHolder.getUserId();
        return R.ok(messageService.queryPage(userId, type, pageNum, pageSize));
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        Long userId = LoginUserHolder.getUserId();
        return R.ok(messageService.getUnreadCount(userId));
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/read")
    public R<Void> markRead(@RequestBody Map<String, List<Long>> body) {
        Long userId = LoginUserHolder.getUserId();
        List<Long> ids = body.get("ids");
        messageService.markRead(ids, userId);
        return R.ok();
    }

    @Operation(summary = "标记全部已读")
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        Long userId = LoginUserHolder.getUserId();
        messageService.markAllRead(userId);
        return R.ok();
    }
}
