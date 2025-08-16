package com.example.coindesk.repository; // 定義這個介面所在的套件位置

import com.example.coindesk.domain.Currency; // 匯入 Currency 實體類別
import org.springframework.data.jpa.repository.JpaRepository; // 匯入 JPA Repository，提供 CRUD 操作
import org.springframework.stereotype.Repository; // 匯入 Repository 註解

import java.util.List; // 匯入 List 容器類別

/**
 * Currency 的 Repository 介面
 * - 使用 Spring Data JPA，自動提供常見的 CRUD 操作
 * - 透過方法命名規則（Query Method）額外實作排序與檢查功能
 */
@Repository // 資料存取層：提供對 Currency 的資料庫操作 CRUD
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    /**
     * 查詢所有 Currency，並依幣別代碼（code）升冪排序
     * - 使用 Spring Data JPA 的命名規則自動實作
     */
    List<Currency> findAllByOrderByCodeAsc();

    /**
     * 檢查指定幣別代碼是否存在於資料表中
     * - 用於建立新資料或系統預載時的驗證
     */
    boolean existsByCode(String code);
}
