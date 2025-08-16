package com.example.coindesk; // 定義此應用程式的根套件

import org.springframework.boot.SpringApplication; // 匯入 SpringApplication，用於啟動 Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication; // 匯入 @SpringBootApplication，表示此類別是 Spring Boot 主程式入口
import org.springframework.scheduling.annotation.EnableScheduling; // 匯入 @EnableScheduling，用於啟用排程功能

/**
 * ===========================================
 * CoindeskApplication
 * ===========================================
 * 系統進入點 (Main Application)
 * 功能：
 * - 啟動 Spring Boot 應用程式
 * - 啟用自動組態 (Auto Configuration)
 * - 啟用排程功能 (EnableScheduling)，讓排程任務可以正常執行
 */
@SpringBootApplication // 標記為 Spring Boot 應用程式，包含 @Configuration、@EnableAutoConfiguration、@ComponentScan
@EnableScheduling // 啟用排程功能，允許 @Scheduled 任務定期執行
public class CoindeskApplication {

    /**
     * 應用程式進入點
     * SpringApplication.run() 會啟動 Spring Boot 的內嵌伺服器（例如 Tomcat），並初始化整個應用
     */
    public static void main(String[] args) {
        SpringApplication.run(CoindeskApplication.class, args); // 執行應用程式
    }

}
