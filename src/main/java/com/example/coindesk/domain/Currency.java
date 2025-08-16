package com.example.coindesk.domain; // 定義這個類別所在的套件位置

import jakarta.persistence.*; // 匯入JPA(Jakarta Persistence API)，用來標註類別與資料庫表格的對應，以及生命周期事件處理

import java.time.LocalDateTime; // 匯入 Java 標準庫的 LocalDateTime

@Entity // 告訴 JPA 這是一個實體類別，會對應到資料表
@Table(name = "currency") // 對應到資料表名稱（小寫、單數，以免與保留字衝突）
public class Currency {

    @Id // 主鍵（Primary Key）
    @Column(name = "code", length = 10, nullable = false, unique = true) // 設定長度 10，不可為空，且必須唯一
    private String code; // 幣別代碼，符合 ISO 4217 標準（例：USD、EUR）

    @Column(name = "name_Zh", length = 50, nullable = false) // 設定長度 50，不可為空
    private String nameZh; // 幣別中文名稱（例：美元、歐元）

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 建立時間（由 @PrePersist 自動帶入）

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---- 生命週期掛鉤：在持久化/更新前自動寫入時間戳 ----
    @PrePersist // 在 Entity 初次儲存（INSERT）前執行
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now; // 設定建立時間
        this.updatedAt = now; // 同時設定更新時間
    }

    @PreUpdate // 在 Entity 更新（UPDATE）前執行
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---- 建構子（JPA 需要無參數建構子 protected 避免外部隨意呼叫） ----
    protected Currency(){}

    // 自訂建構子，方便直接用幣別代碼與中文名稱建立 Currency 實例
    // 範例：new Currency("USD", "美元");
    public Currency(String code, String nameZh) {
        this.code = code;
        this.nameZh = nameZh;
    }

    // ---- Getter / Setter ----
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
