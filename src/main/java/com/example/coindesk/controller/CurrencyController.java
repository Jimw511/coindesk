package com.example.coindesk.controller; // 定義這個類別所在的套件位置

import com.example.coindesk.dto.CurrencyDtos; // 匯入 DTO（資料傳輸物件），用來定義幣別 API 的請求與回應格式
import com.example.coindesk.service.CurrencyService; // 匯入 CurrencyService，負責處理幣別資料表的業務邏輯
import jakarta.validation.Valid; // 匯入 @Valid，用來驗證請求物件（例如新增、修改幣別）
import org.springframework.http.HttpStatus; // 匯入 HttpStatus，讓 API 可以指定回應的 HTTP 狀態碼
import org.springframework.web.bind.annotation.*; // 匯入 Spring Web 的註解（@RestController, @GetMapping, @PostMapping 等）

import java.util.List; // 匯入 List，用於回傳多筆資料

@RestController // 標記這是一個 REST API Controller（方法會直接輸出 JSON）
@RequestMapping("/currencies") // 定義這個 Controller 底下的 API 路徑前綴為 /currencies
public class CurrencyController {

    private final CurrencyService service; // 宣告成員變數：幣別服務，用來處理邏輯

    // 建構子注入 CurrencyService，Spring 會自動幫你注入
    public CurrencyController(CurrencyService service) {
        this.service = service;
    }

    // 查詢所有幣別，並依代碼排序
    @GetMapping
    public List<CurrencyDtos.Response> list() {
        return service.listSorted();
    }

    // 查詢單一幣別（依代碼）
    @GetMapping("/{code}")
    public CurrencyDtos.Response getOne(@PathVariable String code) {
        return service.getOne(code);
    }

    // 新增幣別，成功會回傳 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyDtos.Response create(@RequestBody @Valid CurrencyDtos.CreateRequest req) {
        return service.create(req);
    }

    // 更新幣別，成功回傳 200 OK
    @PutMapping("/{code}")
    public CurrencyDtos.Response update(@PathVariable String code, @RequestBody @Valid CurrencyDtos.UpdateRequest req) {
        return service.update(code, req);
    }

    // 刪除幣別，成功回傳 204 No Content
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        service.delete(code);
    }

}
