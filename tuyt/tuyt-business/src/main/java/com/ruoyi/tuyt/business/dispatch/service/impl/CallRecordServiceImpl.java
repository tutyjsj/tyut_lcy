package com.ruoyi.tuyt.business.dispatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.dispatch.entity.CallRecord;
import com.ruoyi.tuyt.business.dispatch.mapper.CallRecordMapper;
import com.ruoyi.tuyt.business.dispatch.service.ICallRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CallRecordServiceImpl extends ServiceImpl<CallRecordMapper, CallRecord> implements ICallRecordService {

    @Override
    public List<CallRecord> getRecent(Integer limit) {
        if (limit == null) limit = 10;
        LambdaQueryWrapper<CallRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CallRecord::getCallTime);
        Page<CallRecord> page = page(new Page<>(1, limit), wrapper);
        return page.getRecords();
    }

    @Override
    @Transactional
    public CallRecord recordCall(CallRecord record) {
        record.setCallTime(LocalDateTime.now());
        save(record);
        return record;
    }
}
