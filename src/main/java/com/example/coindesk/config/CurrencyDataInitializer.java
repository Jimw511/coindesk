package com.example.coindesk.config; // 定義此類別所在的 package（專案中的模組位置）

import com.example.coindesk.domain.Currency; // 匯入 Currency 實體類別，用來建立/操作幣別資料
import com.example.coindesk.repository.CurrencyRepository; // 匯入 JPA Repository，用來存取 Currency 資料表（查詢/新增）
import org.springframework.boot.CommandLineRunner; // 匯入 Spring Boot 提供的介面，專門在應用程式啟動完成後執行一段程式碼
import org.springframework.context.annotation.Bean; // 匯入 @Bean，用來把某個方法的回傳物件註冊到 Spring 容器中
import org.springframework.context.annotation.Configuration; // 匯入 @Configuration，表示這是一個「設定類別」，會在 Spring 啟動時載入

/**
 * 啟動時預載資料：Mocking data 僅有 USD/GBP/EUR
 * - 若資料表尚未有這些代碼，則插入
 */
@Configuration
public class CurrencyDataInitializer {
    @Bean // 標示這個方法會回傳一個 Bean，讓 Spring 管理（在容器中可被其他地方注入使用）
    CommandLineRunner seedCurrencies(CurrencyRepository repo) {
        // 定義一個方法，回傳 CommandLineRunner，Spring Boot 啟動完成後會自動執行
        // 參數 repo：Spring 自動注入 CurrencyRepository，用來操作 DB
        return args -> { // Lambda 表達式：當 CommandLineRunner 執行時，會跑裡面的程式碼
            upsert(repo, "USD", "美元"); // 呼叫自訂的 upsert 方法，若沒有 USD 就新增，名稱為「美元」
            upsert(repo, "GBP", "英鎊");
            upsert(repo, "EUR", "歐元");
        };
    }

    // 定義一個私有方法 upsert，用來檢查並新增幣別
    // 參數：
    // - repo：存取資料表
    // - code：幣別代碼（例如 USD）
    // - nameZh：幣別中文名稱（例如 美元）
    private void upsert(CurrencyRepository repo, String code, String nameZh) {
        if (!repo.existsByCode(code)) { // 判斷：資料表中是否已經有這個幣別代碼，如果不存在，就進行新增
            repo.save(new Currency(code, nameZh)); // 建立一個新的 Currency 實體，存進資料表
        }
    }
}
