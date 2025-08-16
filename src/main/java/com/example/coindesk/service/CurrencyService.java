package com.example.coindesk.service; // 定義這個類別所在的套件位置

import com.example.coindesk.domain.Currency; // 匯入 Currency 實體類別（對應資料表）
import com.example.coindesk.dto.CurrencyDtos; // 匯入 DTO（用來收/回傳 API 資料）
import com.example.coindesk.repository.CurrencyRepository; // 匯入 Repository，負責存取幣別資料
import org.springframework.http.HttpStatus; // 匯入 HttpStatus，提供 404/409 等狀態碼
import org.springframework.stereotype.Service; // 匯入 Service 標註，標記此類別為 Service 元件
import org.springframework.transaction.annotation.Transactional; // 匯入 Transactional，控制資料庫交易
import org.springframework.web.server.ResponseStatusException; // 匯入 ResponseStatusException，丟出 HTTP 錯誤
import org.springframework.http.HttpStatus.*; // 匯入 HttpStatus.*，可直接使用 NOT_FOUND, CONFLICT 等常數

import java.util.List; // 匯入 List，用於回傳多筆資料

/**
 * CurrencyService
 * ===========================================
 * 幣別相關的商業邏輯服務層
 * - 功能：處理幣別的新增、查詢、更新、刪除
 * - 包含排序、驗證、錯誤處理
 * <p>
 * 註：
 * 1. Service 層不直接處理 HTTP，這是 Controller 的責任
 * 2. Service 串接 Repository（資料存取層）與 Controller
 */
@Service // 標記此類別為 Spring 的 Service，交由 IoC 容器管理
public class CurrencyService {

    private final CurrencyRepository repo; // 依賴注入的 Repository，負責存取幣別資料

    /**
     * 建構子注入 CurrencyRepository
     */
    public CurrencyService(CurrencyRepository repo) {
        this.repo = repo;
    }

    /**
     * 查詢所有幣別（依代碼升冪排序）
     *
     * @return 幣別回應 DTO 清單
     */
    @Transactional(readOnly = true) // 查詢操作，標記為唯讀
    public List<CurrencyDtos.Response> listSorted() {
        return repo.findAllByOrderByCodeAsc() // 呼叫 Repository 依代碼排序查詢
                .stream() // 將 List 轉換成 Stream，方便做資料處理
                .map(c -> new CurrencyDtos.Response(c.getCode(), c.getNameZh())) // 把每個 Currency 轉換成 Response DTO
                .toList(); // 收集成 List，最後回傳
    }

    /**
     * 查詢單一幣別
     *
     * @param codePath 使用者輸入的幣別代碼（path variable）
     * @return 幣別回應 DTO
     * @throws ResponseStatusException 若找不到幣別則丟出 404
     */
    @Transactional(readOnly = true) // 查詢操作，標記為唯讀
    public CurrencyDtos.Response getOne(String codePath) {
        String code = codePath.trim().toUpperCase(); // 去掉前後空白（trim），再轉成大寫（toUpperCase），確保代碼格式一致
        Currency c = repo.findById(code) // 從資料庫查詢該代碼
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到幣別： " + code)); // 查無資料 → 404
        return new CurrencyDtos.Response(c.getCode(), c.getNameZh()); // 組成回應 DTO
    }

    /**
     * 建立新幣別
     *
     * @param req 建立請求 DTO
     * @return 幣別回應 DTO
     * @throws ResponseStatusException 若幣別代碼已存在則丟出 409
     */
    @Transactional // 開啟交易，允許 INSERT（若失敗會自動回滾）
    public CurrencyDtos.Response create(CurrencyDtos.CreateRequest req) {
        String code = req.code().trim().toUpperCase(); // 去掉前後空白（trim），再轉成大寫（toUpperCase），確保代碼格式一致
        // 驗證是否已存在相同代碼
        // - 若代碼已存在，拋出 409 Conflict（避免重複建立同一幣別）
        if (repo.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "幣別代碼已存在： " + code);
        }
        Currency saved = repo.save(new Currency(code, req.nameZh())); // 新增資料
        return new CurrencyDtos.Response(saved.getCode(), saved.getNameZh()); // 回傳建立後資料
    }

    /**
     * 更新幣別（只允許修改中文名稱）
     *
     * @param codepath 路徑上的幣別代碼
     * @param req      更新請求 DTO（只含 nameZh）
     * @return 幣別回應 DTO
     * @throws ResponseStatusException 若找不到幣別則丟出 404
     */
    @Transactional // 開啟交易，允許 UPDATE；發生例外會自動回滾
    public CurrencyDtos.Response update(String codepath, CurrencyDtos.UpdateRequest req) {
        String code = codepath.trim().toUpperCase(); // 去掉前後空白（trim），再轉成大寫（toUpperCase），確保代碼格式一致
        Currency c = repo.findById(code) // 從資料庫查詢該代碼
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到幣別： " + code)); // 查無資料 → 404
        c.setNameZh(req.nameZh()); // 僅更新中文名稱
        // updatedAt 欄位由 @PreUpdate 自動帶入，無需手動設定
        Currency saved = repo.save(c); // 儲存更新
        return new CurrencyDtos.Response(saved.getCode(), saved.getNameZh()); // 回傳更新後資料
    }

    /**
     * 刪除幣別
     *
     * @param codePath 路徑上的幣別代碼
     * @throws ResponseStatusException 若找不到幣別則丟出 404
     */
    @Transactional // 開啟交易，允許 DELETE；發生例外會自動回滾
    public void delete(String codePath) {
        String code = codePath.trim().toUpperCase(); // 去掉前後空白（trim），再轉成大寫（toUpperCase），確保代碼格式一致
        // 從資料庫查詢該代碼，查無資料 → 404
        if (!repo.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到幣別： " + code);
        }
        repo.deleteById(code); // 執行刪除
    }
}
