package com.ruoyi.tuyt.framework.sms;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.tuyt.common.sms.ISmsProvider;
import com.ruoyi.tuyt.common.sms.SmsProperties;
import com.ruoyi.tuyt.common.sms.SmsSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 云片短信提供商 — 真实发送
 * <p>需要在 application.yml 中配置 sms.yunpian.api-key。使用前先去 yunpian.com 注册获取 API Key。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class YunpianSmsProvider implements ISmsProvider {

    private final SmsProperties smsProperties;

    @Override
    public SmsSendResult send(String phone, String content) {
        SmsProperties.Yunpian cfg = smsProperties.getYunpian();
        if (StrUtil.isBlank(cfg.getApiKey())) {
            log.error("云片 API Key 未配置，请在 application.yml 中设置 sms.yunpian.api-key");
            return SmsSendResult.fail(phone, "API Key 未配置");
        }

        String fullContent = smsProperties.getSignName() + content;

        Map<String, Object> params = new HashMap<>();
        params.put("apikey", cfg.getApiKey());
        params.put("mobile", phone);
        params.put("text", fullContent);

        try {
            String response = HttpUtil.post(cfg.getUrl(), params);
            log.info("云片短信发送响应: {}", response);

            JSONObject json = JSON.parseObject(response);
            int code = json.getIntValue("code");
            if (code == 0) {
                log.info("短信发送成功 → {}", phone);
                return SmsSendResult.ok(phone);
            } else {
                String msg = json.getString("msg");
                String detail = json.getString("detail");
                log.error("短信发送失败 → {} | code={} msg={} detail={}", phone, code, msg, detail);
                return SmsSendResult.fail(phone, StrUtil.blankToDefault(detail, msg));
            }
        } catch (Exception e) {
            log.error("短信发送异常 → {} | {}", phone, e.getMessage(), e);
            return SmsSendResult.fail(phone, "网络异常: " + e.getMessage());
        }
    }
}
