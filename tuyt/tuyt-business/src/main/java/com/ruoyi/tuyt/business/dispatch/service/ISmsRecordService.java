package com.ruoyi.tuyt.business.dispatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.dispatch.entity.SmsRecord;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ISmsRecordService extends IService<SmsRecord> {

    /** 分页查询短信记录 */
    PageResult<SmsRecord> queryPage(String keyword, Integer pageNum, Integer pageSize);

    /** 发送短信(单个) */
    SmsRecord sendSms(SmsRecord record);

    /** 批量发送短信 */
    List<SmsRecord> batchSendSms(SmsRecord record, List<String> phones, List<String> names);
}
