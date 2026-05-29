package com.ruoyi.tuyt.common.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信发送结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendResult {

    /** 是否成功 */
    private boolean success;

    /** 消息 */
    private String message;

    /** 发送的号码 */
    private String phone;

    public static SmsSendResult ok(String phone) {
        return new SmsSendResult(true, "发送成功", phone);
    }

    public static SmsSendResult fail(String phone, String message) {
        return new SmsSendResult(false, message, phone);
    }
}
