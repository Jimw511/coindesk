package com.example.coindesk.service; // 定義 service 層測試所屬的套件路徑

import com.example.coindesk.domain.Currency; // 匯入幣別實體類別
import com.example.coindesk.dto.CurrencyDtos; // 匯入幣別相關的 DTO 定義
import com.example.coindesk.repository.CurrencyRepository; // 匯入幣別 Repository，負責資料存取
import org.junit.jupiter.api.BeforeEach; // 匯入 JUnit 5 的 @BeforeEach，用於每個測試方法前執行初始化
import org.junit.jupiter.api.Test; // 匯入 JUnit 5 的 @Test，用於標記測試方法
import org.springframework.beans.factory.annotation.Autowired; // 匯入 @Autowired，用於依賴注入
import org.springframework.boot.test.context.SpringBootTest; // 匯入 @SpringBootTest，用於啟動 Spring Boot 測試環境
import org.springframework.test.annotation.Rollback; // 匯入 @Rollback，可標記測試資料在結束後回滾
import org.springframework.transaction.annotation.Transactional; // 匯入 @Transactional，讓每個測試在交易中執行
import org.springframework.web.server.ResponseStatusException; // 匯入 ResponseStatusException，用於模擬 API 錯誤情境

import java.util.List; // 匯入 List，用於存放多筆資料

import static org.junit.jupiter.api.Assertions.*; // 匯入 JUnit 5 的斷言工具（assertEquals、assertThrows 等）

/**
 * ===========================================
 * CurrencyService 測試類別 (CurrencyServiceTest)
 * ===========================================
 * 測試目標：
 * - 驗證 CurrencyService 的增刪改查功能是否正確
 *
 * 測試環境：
 * - 使用 SpringBootTest 啟動完整 Spring 容器
 * - 搭配 H2 內存資料庫進行整合測試
 * - 每個測試方法皆在 @Transactional 中執行，結束後會自動回滾，避免測試資料汙染
 *
 * 測試重點：
 * - listSorted() 能否依代碼排序
 * - create() 是否會自動轉大寫並避免重複
 * - getOne() / update() / delete() 是否正確處理存在與不存在的情況
 * - 錯誤情境是否能正確丟出 ResponseStatusException
 */
@SpringBootTest // 啟動完整 Spring Boot 測試環境
@Transactional // 每個測試方法都在交易中執行，結束自動回滾
class CurrencyServiceTest {
    @Autowired
    private CurrencyService service; // 測試目標：幣別服務

    @Autowired
    private CurrencyRepository repo; // Repository，用於直接檢查資料狀態

    /**
     * 測試前初始化：
     * - 先清空資料庫，避免殘留影響
     * - 預設新增三筆幣別（USD、GBP、EUR），確保測試基準一致
     */
    @BeforeEach
    void setUp() {
        repo.deleteAll(); // 先清空（避免殘留）
        // 確保測試初始有三筆（跟 CurrencyDataInitializer 一樣）
        upsert("USD", "美元");
        upsert("GBP", "英鎊");
        upsert("EUR", "歐元");
    }

    /**
     * 協助方法：若資料不存在則插入（避免重複）
     */
    private void upsert(String code, String name) {
        if (!repo.existsByCode(code)) {
            repo.save(new Currency(code, name));
        }
    }

    /**
     * 測試：listSorted() 應依代碼升冪排序
     */
    @Test
    void listSorted_shouldReturnAscendingByCode() {
        List<CurrencyDtos.Response> list = service.listSorted();
        assertEquals("EUR", list.get(0).code());
        assertEquals("GBP", list.get(1).code());
        assertEquals("USD", list.get(2).code());
    }

    /**
     * 測試：能否正確查詢已存在的幣別
     */
    @Test
    void testGetCurrencyByCode() throws Exception {
        // Arrange：新增一個測試用的幣別
        repo.save(new Currency("TWD", "新台幣"));

        // Act
        var res = service.getOne("TWD");

        // Assert
        assertNotNull(res);
        assertEquals("TWD", res.code());
        assertEquals("新台幣", res.nameZh());
    }

    /**
     * 測試：create() 會自動轉大寫並成功建立
     */
    @Test
    void create_shouldUppercaseAndSucceed() {
        var res = service.create(new CurrencyDtos.CreateRequest("twd", "新台幣"));
        assertEquals("TWD", res.code());
        assertTrue(repo.existsByCode("TWD"));
    }

    /**
     * 測試：重複新增相同幣別應丟出 409 Conflict
     */
    @Test
    void create_conflict_shouldThrow409() {
        // 先插入一筆
        service.create(new CurrencyDtos.CreateRequest("twd", "新台幣"));
        // 再次新增相同代碼 → 409
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(new CurrencyDtos.CreateRequest("TWD", "新台幣")));
        assertEquals(409, ex.getStatusCode().value());
    }

    /**
     * 測試：更新不存在的幣別應丟出 404 Not Found
     */
    @Test
    void update_notFound_shouldThrow404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.update("ZZZ", new CurrencyDtos.UpdateRequest("不存在")));
        assertEquals(404, ex.getStatusCode().value());
    }

    /**
     * 測試：update() 應能正確修改幣別中文名稱
     */
    @Test
    void update_shouldChangeName() {
        service.create(new CurrencyDtos.CreateRequest("TWD", "新台幣"));
        var res = service.update("TWD", new CurrencyDtos.UpdateRequest("新臺幣"));
        assertEquals("新臺幣", res.nameZh());
    }

    /**
     * 測試：delete() 應能刪除幣別
     */
    @Test
    void delete_shouldRemove() {
        service.create(new CurrencyDtos.CreateRequest("twd", "新台幣"));
        service.delete("TWD");
        assertFalse(repo.existsByCode("TWD"));
    }
}
