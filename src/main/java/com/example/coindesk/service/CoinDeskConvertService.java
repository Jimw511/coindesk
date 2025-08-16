package com.example.coindesk.service; // 定義這個類別所在的套件位置

import com.example.coindesk.dto.ConvertedDtos; // 匯入 DTO：轉換後回應格式
import com.example.coindesk.repository.CurrencyRepository; // 匯入 Currency 的 Repository，供查詢中文名稱使用
import com.fasterxml.jackson.databind.JsonNode; // 匯入 Jackson 的 JSON 節點物件
import com.fasterxml.jackson.databind.ObjectMapper; // 匯入 Jackson 的 ObjectMapper，解析 JSON 用
import org.springframework.stereotype.Service; // 匯入 Service 標註，標記此類別為 Service 元件

import java.math.BigDecimal; // 匯入 BigDecimal，精確表示匯率
import java.time.OffsetDateTime; // 匯入 OffsetDateTime，解析 updatedISO
import java.time.format.DateTimeFormatter; // 匯入時間格式器
import java.util.ArrayList; // 匯入 ArrayList，建立清單用
import java.util.Iterator; // 匯入 Iterator，遍歷 JSON 欄位
import java.util.List; // 匯入 List，統一回傳明細集合

/**
 * CoinDesk 轉換服務層
 * - 功能：將 CoinDesk 原始 JSON 轉換為題目要求格式
 * - 包含時間格式轉換、匯率解析、幣別中文名稱補全
 */
@Service
public class CoinDeskConvertService {
    private final CoinDeskService coinDeskService; // 依賴：負責抓取 CoinDesk API 或 Mock
    private final CurrencyRepository currencyRepo; // 依賴：查詢幣別中文名稱
    private final ObjectMapper mapper = new ObjectMapper(); // JSON 解析器
    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); // 時間輸出格式

    /**
     * 建構子注入
     */
    public CoinDeskConvertService(CoinDeskService coinDeskService, CurrencyRepository currencyRepo) {
        this.coinDeskService = coinDeskService;
        this.currencyRepo = currencyRepo;
    }

    /**
     * 取得轉換後的回應物件
     * - 先呼叫 CoinDeskService 抓取原始 JSON
     * - 解析更新時間（優先使用 updatedISO）
     * - 解析幣別明細，補充中文名稱
     * - 回傳標準化 Response DTO
     */
    public ConvertedDtos.Response getConverted() {
        String raw = coinDeskService.fetchRawJson(); // 成功→線上；失敗→Mock
        try {
            JsonNode root = mapper.readTree(raw); // 解析 JSON 根節點

            // 取出時間：優先 updatedISO，其次 updated
            String iso = root.path("time").path("updatedISO").asText(null);
            String updated = root.path("time").path("updated").asText(null);

            String formatted;
            if (iso != null) {
                formatted = OffsetDateTime.parse(iso).toLocalDateTime().format(OUT_FMT); // ISO 格式轉換
            } else if (updated != null) {
                formatted = updated; // 簡化處理：直接使用原字串
            } else {
                formatted = ""; // 皆不存在 → 回空字串
            }

            // 解析 bpi：逐一取出每個幣別
            List<ConvertedDtos.Item> items = new ArrayList<>(); // 建立結果清單
            JsonNode bpi = root.path("bpi"); // 取得 bpi 區塊
            Iterator<String> it = bpi.fieldNames(); // 取得 JSON 物件 bpi 的所有 key（幣別代碼），以 Iterator 形式逐一取出
            while (it.hasNext()) { // 逐一處理每個幣別代碼
                String code = it.next(); // 取出當前幣別代碼
                JsonNode node = bpi.path(code); // 取得該幣別的 JSON 節點
                BigDecimal rate = node.path("rate_float").decimalValue(); // 解析浮點數匯率

                // 從資料庫查詢中文名稱，若不存在則回傳空字串
                String nameZh = currencyRepo.findById(code).map(c -> c.getNameZh()).orElse("");

                items.add(new ConvertedDtos.Item(code, nameZh, rate)); // 將結果加入清單
            }

            return new ConvertedDtos.Response(formatted, items); // 組裝並回傳 Response
        } catch (Exception ex) {
            // 解析失敗時，回傳空 Response（或可改丟出例外 502）
            return new ConvertedDtos.Response("",List.of());
        }
    }
}
