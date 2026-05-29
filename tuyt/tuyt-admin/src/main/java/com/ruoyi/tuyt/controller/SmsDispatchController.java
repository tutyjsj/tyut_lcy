package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.dispatch.entity.CallRecord;
import com.ruoyi.tuyt.business.dispatch.entity.SmsRecord;
import com.ruoyi.tuyt.business.dispatch.service.ICallRecordService;
import com.ruoyi.tuyt.business.dispatch.service.ISmsRecordService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "短信/语音调度")
@RestController
@RequestMapping("/dispatch")
@RequiredArgsConstructor
public class SmsDispatchController {

    private final ISmsRecordService smsRecordService;
    private final ICallRecordService callRecordService;

    // ==================== 短信调度 ====================

    @Operation(summary = "查询短信记录")
    @GetMapping("/sms/list")
    public R<PageResult<SmsRecord>> smsList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(smsRecordService.queryPage(keyword, pageNum, pageSize));
    }

    @Operation(summary = "发送短信(单个)")
    @PostMapping("/sms/send")
    public R<SmsRecord> sendSms(@RequestBody SmsRecord record) {
        return R.ok(smsRecordService.sendSms(record));
    }

    @Operation(summary = "批量发送短信")
    @PostMapping("/sms/batch-send")
    public R<List<SmsRecord>> batchSendSms(@RequestBody Map<String, Object> params) {
        SmsRecord record = new SmsRecord();
        record.setContent((String) params.get("content"));
        record.setSenderId(params.get("senderId") != null ? Long.valueOf(params.get("senderId").toString()) : null);
        record.setSenderName((String) params.get("senderName"));
        record.setSmsType("BATCH");

        @SuppressWarnings("unchecked")
        List<String> phones = (List<String>) params.get("phones");
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) params.get("names");

        return R.ok(smsRecordService.batchSendSms(record, phones, names));
    }

    // ==================== 通话记录 ====================

    @Operation(summary = "查询最近通话")
    @GetMapping("/call/recent")
    public R<List<CallRecord>> recentCalls(
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(callRecordService.getRecent(limit));
    }

    @Operation(summary = "记录通话")
    @PostMapping("/call/record")
    public R<CallRecord> recordCall(@RequestBody CallRecord record) {
        return R.ok(callRecordService.recordCall(record));
    }
}
