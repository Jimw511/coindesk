package com.example.coindesk.service; // 定義這個類別所在的套件位置

import com.example.coindesk.domain.ExchangeRate; // 匯入匯率實體
import com.example.coindesk.repository.ExchangeRateRepository; // 匯入匯率 Repo
import org.junit.jupiter.api.BeforeEach; // 匯入 @BeforeEach
import org.junit.jupiter.api.Test; // 匯入 @Test
import org.springframework.beans.factory.annotation.Autowired; // 匯入 @Autowired
import org.springframework.boot.test.context.SpringBootTest; // 啟動 Spring 測試環境
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 匯入 @MockitoBean
import org.springframework.transaction.annotation.Transactional; // 匯入 @Transactional

import java.time.LocalDateTime; // 匯入 LocalDateTime

import static org.junit.jupiter.api.Assertions.*; // 匯入斷言工具
import static org.mockito.BDDMockito.given; // 匯入 Mockito 的 given

/**
 * ===========================================
 * 匯率同步服務測試 (RateSyncServiceTest)
 * ===========================================
 * 目的：
 * - 驗證 syncOnce() 能：
 *   1) 從 CoinDesk JSON 解析匯率
 *   2) 以幣別為主鍵 upsert 至 DB
 *   3) 解析 updatedISO 成 LocalDateTime 寫入 updatedAt
 *
 * 作法：
 * - 用 @MockitoBean 取代 CoinDeskService 回傳固定 Mock JSON
 * - 呼叫 syncOnce() 後驗證 ExchangeRateRepository 的資料
 */
@SpringBootTest
@Transactional
class RateSyncServiceTest {

    @Autowired
    private RateSyncService rateSyncService; // 測試目標：同步服務

    @Autowired
    private ExchangeRateRepository rateRepo; // 用來驗證 DB 結果

    @MockitoBean
    private CoinDeskService coinDeskService; // Mock 外呼來源

    private static final String MOCK_JSON = """
        {
          "time": {
            "updated": "Aug 3, 2022 20:25:00 UTC",
            "updatedISO": "2022-08-03T20:25:00+00:00",
            "updateduk": "Aug 3, 2022 at 21:25 BST"
          },
          "bpi": {
            "USD": { "code": "USD", "rate_float": 23342.0112 },
            "GBP": { "code": "GBP", "rate_float": 19504.3978 },
            "EUR": { "code": "EUR", "rate_float": 22738.5269 }
          }
        }""";

    @BeforeEach
    void setUp() {
        rateRepo.deleteAll(); // 清空，避免殘留資料影響
        given(coinDeskService.fetchRawJson()).willReturn(MOCK_JSON); // Mock 回傳 JSON
    }

    @Test
    void syncOnce_shouldUpsertRatesWithUpdatedAt() {
        // Act
        rateSyncService.syncOnce();

        // Assert：三筆都應存在
        assertTrue(rateRepo.findById("USD").isPresent());
        assertTrue(rateRepo.findById("GBP").isPresent());
        assertTrue(rateRepo.findById("EUR").isPresent());

        var usd = rateRepo.findById("USD").orElseThrow();
        var gbp = rateRepo.findById("GBP").orElseThrow();
        var eur = rateRepo.findById("EUR").orElseThrow();

        assertEquals(0, usd.getRate().compareTo(new java.math.BigDecimal("23342.0112")));
        assertEquals(0, gbp.getRate().compareTo(new java.math.BigDecimal("19504.3978")));
        assertEquals(0, eur.getRate().compareTo(new java.math.BigDecimal("22738.5269")));

        // updatedAt 解析自 updatedISO → 2022-08-03T20:25:00
        LocalDateTime expected = java.time.OffsetDateTime.parse("2022-08-03T20:25:00+00:00").toLocalDateTime();
        assertEquals(expected, usd.getUpdatedAt());
        assertEquals(expected, gbp.getUpdatedAt());
        assertEquals(expected, eur.getUpdatedAt());
    }
}
