package com.ruoyi.tuyt.common.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信配置属性（对应 application.yml 中 sms.*）
 * 通过 SmsConfig 的 @EnableConfigurationProperties 注册为 Bean
 */
@Data
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /** 提供商: console(控制台打印-默认) / yunpian(云片真实发送) */
    private String provider = "console";

    /** 签名（附加在短信内容前面） */
    private String signName = "【大同环保】";

    /** 云片配置 */
    private Yunpian yunpian = new Yunpian();

    @Data
    public static class Yunpian {
        /** 云片 API Key */
        private String apiKey = "";
        /** 云片单条发送接口地址 */
        private String url = "https://sms.yunpian.com/v2/sms/single_send.json";
    }
}
