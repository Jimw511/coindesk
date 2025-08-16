package com.example.coindesk.controller; // 定義此類別所在的 package（模組位置）

import com.example.coindesk.dto.ConvertedDtos; // 匯入 DTO（資料傳輸物件），用於回傳轉換後的匯率資訊
import com.example.coindesk.service.CoinDeskConvertService; // 匯入 Service：負責處理 CoinDesk 原始 JSON → 轉換後的格式
import com.example.coindesk.service.CoinDeskService; // 匯入 Service：負責呼叫 CoinDesk API（或回傳 fallback）
import com.example.coindesk.service.RateSyncService; // 匯入 Service：負責執行匯率同步的邏輯
import org.springframework.http.MediaType; // 匯入 Spring 提供的 MediaType，用來指定 API 回傳的 Content-Type
import org.springframework.web.bind.annotation.GetMapping; // 匯入 @GetMapping，標示 HTTP GET 方法的 API
import org.springframework.web.bind.annotation.PostMapping; // 匯入 @PostMapping，標示 HTTP POST 方法的 API
import org.springframework.web.bind.annotation.RestController; // 匯入 @RestController，表示這是一個 REST API 控制器（回傳 JSON 而非頁面）

/**
 * 提供「原始 CoinDesk JSON」的端點（含 fallback）。
 * 先確認取值正常；下一步再做資料轉換的新 API。
 */
@RestController // 標示這個類別是一個 REST Controller，方法會直接回傳 JSON
public class CoinDeskController { // 定義一個控制器類別，名稱 CoinDeskController
    private final CoinDeskService service; // 宣告一個成員變數，用來存放 CoinDeskService
    private final CoinDeskConvertService convertService; // 宣告一個成員變數，用來存放 CoinDeskConvertService
    private final RateSyncService rateSyncService; // 宣告一個成員變數，用來存放 RateSyncService

    // 建構子注入（Spring 會自動幫你注入三個 service 並存起來）
    public CoinDeskController(CoinDeskService service, CoinDeskConvertService convertService, RateSyncService rateSyncService) {
        this.service = service;
        this.convertService = convertService;
        this.rateSyncService = rateSyncService;
    }

    // 定義 GET API，回傳 JSON 格式
    @GetMapping(value = "/coindesk/raw", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getRaw() {
        return service.fetchRawJson(); // 呼叫 CoinDeskService 取回原始 JSON 成功→線上API的JSON；失敗→Mock JSON
    }

    // 定義 GET API
    @GetMapping("/coindesk/converted")
    public ConvertedDtos.Response getConverted() {
        return convertService.getConverted(); // 呼叫 CoinDeskConvertService，取得轉換後的 DTO
    }

    // 定義 POST API
    @PostMapping("/coindesk/sync")
    public void sync() {
        rateSyncService.syncOnce(); // 呼叫 RateSyncService，手動觸發一次匯率同步
    }
}
