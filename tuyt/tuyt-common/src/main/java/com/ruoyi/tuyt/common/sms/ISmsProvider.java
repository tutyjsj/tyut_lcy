package com.ruoyi.tuyt.common.sms;

import java.util.List;

/**
 * 短信发送提供商接口
 */
public interface ISmsProvider {

    /**
     * 发送短信给单个号码
     * @param phone  手机号
     * @param content 短信内容
     * @return 发送结果
     */
    SmsSendResult send(String phone, String content);

    /**
     * 批量发送（逐条调用 send）
     * @param phones  手机号列表
     * @param content 短信内容
     * @return 每个号码的发送结果
     */
    default List<SmsSendResult> sendBatch(List<String> phones, String content) {
        return phones.stream().map(phone -> send(phone, content)).toList();
    }
}
