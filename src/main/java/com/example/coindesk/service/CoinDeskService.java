package com.example.coindesk.service; // 定義這個類別所在的套件位置

import org.slf4j.Logger; // 匯入 Logger，提供日誌紀錄功能
import org.slf4j.LoggerFactory; // 匯入 LoggerFactory，用來建立 Logger 實例
import org.springframework.http.MediaType; // 匯入 MediaType，指定回應格式 JSON
import org.springframework.stereotype.Service; // 匯入 Service 標註，標記此類別為 Service 元件
import org.springframework.web.client.RestClient; // 匯入 RestClient，Spring 6+ 輕量 HTTP client

/**
 * CoinDesk 服務層
 * - 功能：負責呼叫 CoinDesk API，或在失敗時回傳 Mock JSON
 */
@Service
public class CoinDeskService {
    private static final Logger log = LoggerFactory.getLogger(CoinDeskService.class); // 建立 Logger 實例，用於記錄系統運行過程中的訊息（例如錯誤、警告、調試資訊）
    private final RestClient rest; // 依賴：Spring 提供的 HTTP client，用於呼叫外部 API
    private static final String COINDESK_URL = "https://api.coindesk.com/v1/bpi/currentprice.json"; // CoinDesk API URL

    // 作業提供的 Mock data（Java 17 支援文字區塊 """..."""）
    private static final String MOCK_JSON = """
            {
              "time": {
                "updated": "Aug 3, 2022 20:25:00 UTC",
                "updatedISO": "2022-08-03T20:25:00+00:00",
                "updateduk": "Aug 3, 2022 at 21:25 BST"
              },
              "disclaimer": "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
              "chartName": "Bitcoin",
              "bpi": {
                "USD": { "code": "USD", "symbol": "$", "rate": "23,342.0112", "description": "US Dollar", "rate_float": 23342.0112 },
                "GBP": { "code": "GBP", "symbol": "£", "rate": "19,504.3978", "description": "British Pound Sterling", "rate_float": 19504.3978 },
                "EUR": { "code": "EUR", "symbol": "€", "rate": "22,738.5269", "description": "Euro", "rate_float": 22738.5269 }
              }
            }""";

    /**
     * 建構子：初始化 RestClient
     * - 預設即可；若之後需要 proxy/timeout 再加設定
     */
    public CoinDeskService() {
        this.rest = RestClient.create(); // 建立 RestClient 實例
    }

    /**
     * 取得 CoinDesk 原始 JSON
     * - 成功：呼叫線上 API 並回傳結果
     * - 失敗：保留原本呼叫程式碼（try 內），但改回傳 Mock JSON（符合題目需求）
     */
    public String fetchRawJson() {
        try {
            return rest.get() // 發送 GET 請求
                    .uri(COINDESK_URL)// 指定 API URL
                    .accept(MediaType.APPLICATION_JSON) // 指定回應格式 JSON
                    .retrieve()// 執行請求
                    .body(String.class); // 轉換為字串回傳
        } catch (Exception ex) {
            log.warn("CoinDesk 呼叫失敗，改用 Mock", ex); // 異常時記錄警告訊息，並附帶 Exception
            return MOCK_JSON; // 發生例外時，改用 Mock JSON
        }
    }

}
