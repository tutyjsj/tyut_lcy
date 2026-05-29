package com.ruoyi.tuyt.framework.sms;

import com.ruoyi.tuyt.common.sms.ISmsProvider;
import com.ruoyi.tuyt.common.sms.SmsProperties;
import com.ruoyi.tuyt.common.sms.SmsSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 控制台短信提供商 — 开发/演示模式
 * <p>将短信内容打印到控制台，不产生任何费用。默认启用。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class ConsoleSmsProvider implements ISmsProvider {

    private final SmsProperties smsProperties;

    @Override
    public SmsSendResult send(String phone, String content) {
        String fullContent = smsProperties.getSignName() + content;
        log.info("\n" +
                 "╔══════════════════════════════════════════╗\n" +
                 "║  📱 模拟短信发送（Console 模式）          ║\n" +
                 "╠══════════════════════════════════════════╣\n" +
                 "║  号码: {}                              \n" +
                 "║  内容: {}                              \n" +
                 "║  签名: {}                              \n" +
                 "╚══════════════════════════════════════════╝",
                 phone, fullContent, smsProperties.getSignName());
        return SmsSendResult.ok(phone);
    }
}
