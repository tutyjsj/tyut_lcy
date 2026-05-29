package com.ruoyi.tuyt.business.dispatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.dispatch.entity.CallRecord;

import java.util.List;

public interface ICallRecordService extends IService<CallRecord> {

    /** 获取最近通话记录(最新N条) */
    List<CallRecord> getRecent(Integer limit);

    /** 记录通话 */
    CallRecord recordCall(CallRecord record);
}
