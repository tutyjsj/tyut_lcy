package com.ruoyi.tuyt.framework.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 配置
 * 统一处理前后端日期格式：yyyy-MM-dd HH:mm:ss
 * Long 类型序列化为字符串，避免前端 JS 数字精度丢失
 */
@Configuration
public class JacksonConfig {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // java.util.Date 格式
            builder.simpleDateFormat(DATETIME_PATTERN);

            // LocalDateTime 序列化/反序列化
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            builder.serializers(new LocalDateTimeSerializer(formatter));
            builder.deserializers(new LocalDateTimeDeserializer(formatter));

            // Long / long 序列化为字符串，防止前端 JS Number 精度丢失
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
