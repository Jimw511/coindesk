package com.example.coindesk.domain; // 定義這個類別所在的套件位置

import jakarta.persistence.Column; // JPA(Jakarta Persistence API) 的標註，用來把 Java 類別與資料表做映射（ORM）
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal; // BigDecimal 用於處理金額或精確數值（避免浮點數誤差）
import java.time.LocalDateTime; // 匯入 Java 標準庫的 LocalDateTime

/**
 * ExchangeRate 實體類別
 * - 對應到資料表 exchange_rate
 * - 儲存某幣別的即時匯率與最後更新時間
 */
@Entity // 宣告這是一個 JPA 實體類別
@Table(name = "exchange_rate") // 對應的資料表名稱
public class ExchangeRate {
    @Id // 主鍵，使用「幣別代碼」作為唯一識別
    @Column(name = "code", length = 30, nullable = false) // 設定長度 30，不可為空，且必須唯一
    private String code; // 幣別代碼，例如：USD、GBP、EUR

    @Column(name = "rate", precision = 18, scale = 6, nullable = false) // 數字位數 18（含小數點前後），小數點後的位數（最多6），不可為空
    private BigDecimal rate; // 匯率數值，使用 BigDecimal 以避免浮點誤差

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 匯率的最後更新時間

    // ---- 建構子（JPA 需要無參數建構子 protected 避免外部隨意呼叫） ----
    protected ExchangeRate() {
    }

    // 自訂建構子，方便直接用幣別代碼、匯率與更新時間建立 ExchangeRate 實例
    // 範例：new ExchangeRate("USD", new BigDecimal("123.456789"), LocalDateTime.now());
    public ExchangeRate(String code, BigDecimal rate, LocalDateTime updatedAt) {
        this.code = code;
        this.rate = rate;
        this.updatedAt = updatedAt;
    }

    // ---- Getter / Setter ----
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
