package com.ruoyi.tuyt.framework.sms;

import com.ruoyi.tuyt.common.sms.ISmsProvider;
import com.ruoyi.tuyt.common.sms.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信自动配置 — 根据 sms.provider 自动选择 ISmsProvider 实例
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfig {

    /**
     * Console 模式（默认）：短信内容打印到控制台，不产生费用
     */
    @Bean
    @ConditionalOnMissingBean(ISmsProvider.class)
    @ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "console", matchIfMissing = true)
    public ISmsProvider consoleSmsProvider(SmsProperties properties) {
        log.info("📱 短信服务 → Console 模式（内容打印到控制台）");
        return new ConsoleSmsProvider(properties);
    }

    /**
     * 云片模式（需配置 API Key）：真正发送短信到手机
     */
    @Bean
    @ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "yunpian")
    public ISmsProvider yunpianSmsProvider(SmsProperties properties) {
        log.info("📱 短信服务 → 云片真实发送模式");
        return new YunpianSmsProvider(properties);
    }
}
