package com.ruoyi.tuyt.business.dispatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.dispatch.entity.SmsRecord;
import com.ruoyi.tuyt.business.dispatch.mapper.SmsRecordMapper;
import com.ruoyi.tuyt.business.dispatch.service.ISmsRecordService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.sms.ISmsProvider;
import com.ruoyi.tuyt.common.sms.SmsSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsRecordServiceImpl extends ServiceImpl<SmsRecordMapper, SmsRecord> implements ISmsRecordService {

    private final ISmsProvider smsProvider;

    @Override
    public PageResult<SmsRecord> queryPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SmsRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SmsRecord::getContent, keyword)
                             .or().like(SmsRecord::getRecipientNames, keyword)
                             .or().like(SmsRecord::getSenderName, keyword));
        }
        wrapper.orderByDesc(SmsRecord::getSendTime);
        Page<SmsRecord> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public SmsRecord sendSms(SmsRecord record) {
        record.setSendTime(LocalDateTime.now());
        record.setRecipientCount(StringUtils.hasText(record.getRecipientPhones()) ? 1 : 0);

        // 真正调用短信提供商发送
        if (StringUtils.hasText(record.getRecipientPhones())) {
            SmsSendResult result = smsProvider.send(record.getRecipientPhones(), record.getContent());
            record.setStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
            if (!result.isSuccess()) {
                log.warn("短信发送失败 → {}: {}", record.getRecipientPhones(), result.getMessage());
            }
        } else {
            record.setStatus("FAILED");
        }

        save(record);
        return record;
    }

    @Override
    @Transactional
    public List<SmsRecord> batchSendSms(SmsRecord record, List<String> phones, List<String> names) {
        record.setSendTime(LocalDateTime.now());
        record.setRecipientCount(phones.size());
        record.setRecipientPhones(String.join(",", phones));
        if (names != null && !names.isEmpty()) {
            record.setRecipientNames(String.join(",", names));
        }
        if (!StringUtils.hasText(record.getSmsType())) {
            record.setSmsType("BATCH");
        }

        // 真正逐条调用短信提供商批量发送
        List<SmsSendResult> results = smsProvider.sendBatch(phones, record.getContent());
        long successCount = results.stream().filter(SmsSendResult::isSuccess).count();
        boolean allSuccess = successCount == results.size();
        record.setStatus(allSuccess ? "SUCCESS" : "PARTIAL");
        if (!allSuccess) {
            log.warn("批量短信部分失败: {}/{} 条发送成功", successCount, results.size());
        }

        save(record);
        return List.of(record);
    }
}
