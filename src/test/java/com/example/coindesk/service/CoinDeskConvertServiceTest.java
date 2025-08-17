package com.example.coindesk.service; // 定義這個類別所在的套件位置

import com.example.coindesk.domain.Currency; // 匯入 Currency 實體類別
import com.example.coindesk.dto.ConvertedDtos; // 匯入轉換後回應 DTO
import com.example.coindesk.repository.CurrencyRepository; // 匯入 Currency 資料存取
import org.junit.jupiter.api.BeforeEach; // 匯入 JUnit 5 的 @BeforeEach
import org.junit.jupiter.api.Test; // 匯入 JUnit 5 的 @Test
import org.springframework.beans.factory.annotation.Autowired; // 匯入 @Autowired 進行注入
import org.springframework.boot.test.context.SpringBootTest; // 啟動 Spring 測試環境
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 匯入 @MockitoBean 用來替換 Bean
import org.springframework.transaction.annotation.Transactional; // 匯入 @Transactional，確保交易一致性

import java.util.Map; // 匯入 Map，用來快速建立查找表

import static org.junit.jupiter.api.Assertions.*; // 匯入斷言工具
import static org.mockito.BDDMockito.given; // 匯入 Mockito 的 given 語法

/**
 * ===========================================
 * CoinDesk 轉換服務測試 (CoinDeskConvertServiceTest)
 * ===========================================
 * 目的：
 * - 驗證 getConverted() 會：
 *   1) 正確格式化 updatedTime（yyyy/MM/dd HH:mm:ss）
 *   2) 回傳幣別代碼 / 中文名稱（由 DB 對照）/ 匯率
 *
 * 作法：
 * - 用 @MockitoBean 取代 CoinDeskService，回傳固定的 Mock JSON
 * - 事前把 USD/GBP/EUR 的中文名寫入 H2，讓轉換服務可查到 nameZh
 */
@SpringBootTest
@Transactional
class CoinDeskConvertServiceTest {

    @Autowired
    private CoinDeskConvertService convertService; // 測試目標：轉換服務

    @Autowired
    private CurrencyRepository currencyRepo; // 存放幣別中文名對照

    @MockitoBean
    private CoinDeskService coinDeskService; // 以 Mock 取代，避免對外打網路

    private static final String MOCK_JSON = """
        {
          "time": {
            "updated": "Aug 3, 2022 20:25:00 UTC",
            "updatedISO": "2022-08-03T20:25:00+00:00",
            "updateduk": "Aug 3, 2022 at 21:25 BST"
          },
          "chartName": "Bitcoin",
          "bpi": {
            "USD": { "code": "USD", "symbol": "$", "rate": "23,342.0112", "rate_float": 23342.0112 },
            "GBP": { "code": "GBP", "symbol": "£", "rate": "19,504.3978", "rate_float": 19504.3978 },
            "EUR": { "code": "EUR", "symbol": "€", "rate": "22,738.5269", "rate_float": 22738.5269 }
          }
        }""";

    @BeforeEach
    void setUp() {
        // 安排：DB 先有幣別中文對照
        currencyRepo.deleteAll();
        currencyRepo.save(new Currency("USD", "美元"));
        currencyRepo.save(new Currency("GBP", "英鎊"));
        currencyRepo.save(new Currency("EUR", "歐元"));

        // Mock：讓 CoinDeskService 回傳作業給的 Mock JSON
        given(coinDeskService.fetchRawJson()).willReturn(MOCK_JSON);
    }

    @Test
    void getConverted_shouldReturnFormattedTimeAndItems() {
        // Act
        ConvertedDtos.Response res = convertService.getConverted();

        // Assert：時間格式應為 yyyy/MM/dd HH:mm:ss，且與 updatedISO 對應的本地時間
        assertEquals("2022/08/03 20:25:00", res.updatedTime());

        // 轉為 Map 好比對
        Map<String, ConvertedDtos.Item> map = res.items().stream()
                .collect(java.util.stream.Collectors.toMap(ConvertedDtos.Item::code, i -> i));

        assertEquals(3, map.size());

        // USD
        assertTrue(map.containsKey("USD"));
        assertEquals("美元", map.get("USD").nameZh());
        assertEquals(0, map.get("USD").rate().compareTo(new java.math.BigDecimal("23342.0112")));

        // GBP
        assertTrue(map.containsKey("GBP"));
        assertEquals("英鎊", map.get("GBP").nameZh());
        assertEquals(0, map.get("GBP").rate().compareTo(new java.math.BigDecimal("19504.3978")));

        // EUR
        assertTrue(map.containsKey("EUR"));
        assertEquals("歐元", map.get("EUR").nameZh());
        assertEquals(0, map.get("EUR").rate().compareTo(new java.math.BigDecimal("22738.5269")));
    }
}
