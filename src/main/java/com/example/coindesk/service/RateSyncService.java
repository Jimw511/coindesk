package com.example.coindesk.service; // 定義這個類別所在的套件位置

import org.slf4j.Logger; // 匯入 Logger，提供日誌紀錄功能
import org.slf4j.LoggerFactory; // 匯入 LoggerFactory，用來建立 Logger 實例
import com.example.coindesk.domain.ExchangeRate; // 匯入 ExchangeRate 實體類別，用來存放匯率資訊
import com.example.coindesk.repository.ExchangeRateRepository; // 匯入 Repository，提供匯率資料的 CRUD 操作
import com.fasterxml.jackson.databind.JsonNode; // 匯入 Jackson 的 JsonNode，用於處理 JSON 樹狀結構
import com.fasterxml.jackson.databind.ObjectMapper; // 匯入 ObjectMapper，負責 JSON ↔ Java 物件轉換
import org.springframework.scheduling.annotation.Scheduled; // 匯入 @Scheduled，定期排程用
import org.springframework.stereotype.Service; // 匯入 @Service，標記為 Spring Service 元件
import org.springframework.transaction.annotation.Transactional; // 匯入 @Transactional，確保交易一致性

import java.math.BigDecimal; // 匯入 BigDecimal，處理精確數字（避免浮點誤差）
import java.time.LocalDateTime; // 匯入 LocalDateTime，表示本地端時間（不含時區）
import java.time.OffsetDateTime; // 匯入 OffsetDateTime，可處理含時區的時間
import java.util.Iterator; // 匯入 Iterator，用於遍歷 JSON 欄位名稱

/**
 * ===========================================
 * 匯率同步服務 (RateSyncService)
 * ===========================================
 * 功能：
 * - 呼叫 CoinDesk API 抓取匯率 JSON
 * - 解析更新時間與幣別匯率
 * - 儲存或更新到資料庫 (Upsert)
 * - 提供手動與排程同步兩種方式
 * <p>
 * 設計說明：
 * - @Transactional 確保 DB 操作具備交易性，失敗時會自動回滾
 * - @Scheduled 提供定期背景工作（每 10 分鐘）
 */
@Service // 標記此類別為 Spring 的 Service，交由 IoC 容器管理
public class RateSyncService {

    private static final Logger log = LoggerFactory.getLogger(RateSyncService.class); // 建立 Logger 實例，用於記錄系統運行過程中的訊息（例如錯誤、警告、調試資訊）
    private final CoinDeskService coinDeskService; // 依賴注入：負責呼叫 CoinDesk API
    private final ExchangeRateRepository rateRepo; // 依賴注入：存取 ExchangeRate 資料表
    private final ObjectMapper mapper = new ObjectMapper(); // JSON 解析器

    // 建構式注入，確保必要元件被提供
    public RateSyncService(CoinDeskService coinDeskService, ExchangeRateRepository rateRepo) {
        this.coinDeskService = coinDeskService;
        this.rateRepo = rateRepo;
    }

    /**
     * 單次同步匯率（可由 Controller 呼叫）
     * 1. 向 CoinDesk API 取回 JSON
     * 2. 解析 updatedISO（若缺少則用 now()）
     * 3. 逐一解析 bpi 幣別匯率，執行 upsert
     */
    @Transactional // DB 寫入操作，確保交易一致性
    public void syncOnce() {
        String raw = coinDeskService.fetchRawJson(); // 呼叫外部 API 取回原始 JSON 字串
        try {
            JsonNode root = mapper.readTree(raw); // 解析 JSON → 轉成樹狀結構

            // 取出更新時間：優先使用 updatedISO（UTC），若沒有則取當前時間
            String iso = root.path("time").path("updatedISO").asText(null);
            LocalDateTime updatedAt = iso != null
                    ? OffsetDateTime.parse(iso).toLocalDateTime()
                    : LocalDateTime.now();


            JsonNode bpi = root.path("bpi"); // 取出 bpi 節點，包含所有幣別匯率
            Iterator<String> it = bpi.fieldNames(); // 建立迭代器，逐一走訪幣別代碼（例如 USD / GBP / EUR）

            // 遍歷每一個幣別
            while (it.hasNext()) {
                String code = it.next(); // 幣別代碼
                BigDecimal rate = bpi.path(code).path("rate_float").decimalValue(); // 匯率數值

                // Upsert 操作：若存在則更新，不存在則新增
                ExchangeRate er = rateRepo.findById(code)
                        .orElse(new ExchangeRate(code, rate, updatedAt)); // 預設新建
                er.setRate(rate); // 更新匯率
                er.setUpdatedAt(updatedAt); // 更新時間
                rateRepo.save(er); // 寫入資料庫
            }
        } catch (Exception ex) {
            log.warn("同步匯率失敗", ex); // 異常時記錄警告訊息，並附帶 Exception
        }
    }

    /**
     * 定期同步（排程）
     * 每 10 分鐘執行一次
     * cron 表達式格式：秒 分 時 日 月 週
     */
    @Scheduled(cron = "0 */10 * * * *") // 每 10 分鐘觸發一次
    public void scheduleSync() {
        syncOnce(); // 呼叫單次同步邏輯
    }
}
