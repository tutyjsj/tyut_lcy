package com.ruoyi.tuyt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.ruoyi.tuyt.business.**.mapper")
public class TuytApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuytApplication.class, args);
        System.out.println("========================================");
        System.out.println("  环境网格化综合管理系统 启动成功！");
        System.out.println("  TYUT - 太原理工大学");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
