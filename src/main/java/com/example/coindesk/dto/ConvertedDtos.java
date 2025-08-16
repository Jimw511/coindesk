package com.example.coindesk.dto; // 定義此類別所在的套件位置

import java.math.BigDecimal; // BigDecimal 用於處理金額或精確數值（避免浮點數誤差）
import java.util.List; // 匯入 List，用於存放多筆幣別明細

/**
 * 定義轉換後的新 API 回應資料結構
 * 透過 record 簡化 DTO（資料傳輸物件）的建立
 */
public class ConvertedDtos {
    /**
     * Response：API 的回傳物件
     * - updatedTime：更新時間（格式 yyyy/MM/dd HH:mm:ss）
     * - items：幣別明細清單
     */
    public record Response(
            String updatedTime, // 1990/01/01 00:00:00
            List<Item> items // 幣別明細
    ) {
    }

    /**
     * Item：單筆幣別的明細
     * - code：幣別代碼（例如：USD、EUR）
     * - nameZh：幣別中文名稱（從 DB 對照表取得）
     * - rate：匯率數值（對應 coindesk API 的 rate_float）
     */
    public record Item(
            String code, // 幣別代碼
            String nameZh, // 幣別中文名 (DB 對照)
            BigDecimal rate // rate_float
    ) {
    }
}
