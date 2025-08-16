package com.example.coindesk.repository; // 定義這個介面所在的套件位置


import com.example.coindesk.domain.ExchangeRate; // 匯入 ExchangeRate 實體類別
import org.springframework.data.jpa.repository.JpaRepository; // 匯入 JPA Repository，提供 CRUD 操作

/**
 * ExchangeRate 的 Repository 介面
 * - 使用 Spring Data JPA，自動提供常見的 CRUD 操作
 * - 不需要額外方法時，可以保持最簡潔
 */
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
}
