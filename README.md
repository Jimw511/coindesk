# Coindesk Spring Boot 專案

## 專案簡介
此專案為 **國泰世華 Java Engineer 線上作業**的實作，使用 **Spring Boot (Java 17)** 與 **H2 Database** 建立，並透過 **Maven** 進行建置與依賴管理。  
主要功能為呼叫 **Coindesk API** 取得匯率資料，並建立自有的 **幣別 ↔ 中文名稱對應資料表**，提供一系列 API 供查詢、維護與轉換。  

---

## 作業題目需求 (原文)
1. 使用 Maven 建置 Spring Boot 專案，Java 17，資料庫 H2（OpenJPA / Spring Data JPA）。
2. 功能：  
   - 呼叫 coindesk API 解析並轉換資料，建立新的 API。  
   - 建立幣別與中文名稱對應的資料表（附 SQL 語法），提供查詢、新增、修改、刪除 API，查詢需依幣別代碼排序。  
   - 新 API：回傳更新時間（格式 yyyy/MM/dd HH:mm:ss）與各幣別資訊（代碼、中文名、匯率）。  
   - 所有功能需有單元測試。  
   - 排程同步匯率。  
3. 加分項：  
   - AOP 應用（API 與外部 API 呼叫的 request/response log、Error handling）  
   - swagger-ui  
   - 多語系設計  
   - 兩種以上設計模式  
   - Docker 運行  
   - 加解密技術應用  

---

## 功能需求完成度
### 必要功能 ✅
- [x] **呼叫 Coindesk API (/v1/bpi/currentprice.json)**  
  - 若 API 不可用，可切換使用指定的 Mock Data（並保留原呼叫程式碼）。
- [x] **幣別 ↔ 中文名稱資料表**
  - 建立資料表（H2 Database，附建表 SQL）
  - 提供 API：查詢 / 新增 / 修改 / 刪除
  - 查詢結果依幣別代碼排序
- [x] **新 API**
  - 回傳更新時間（格式：`yyyy/MM/dd HH:mm:ss`）
  - 回傳幣別代碼、中文名稱、匯率資訊
- [x] **單元測試**
  - 覆蓋幣別維護與 Coindesk API 相關功能
- [x] **排程**
  - 定時同步匯率資料

### 加分功能（此專案聚焦於完成必須功能，未花時間於額外功能）❌
- [ ] AOP 應用：API request/response log、錯誤處理
- [ ] Swagger UI
- [ ] 多語系設計
- [ ] 兩種以上設計模式
- [ ] Docker 運行
- [ ] 加解密技術（AES / RSA）

---

## 環境需求
- Java 17
- Maven 3.x
- Spring Boot 3.x

---

## 建置與執行
1. 下載專案
   ```bash
   git clone https://github.com/Jimw511/coindesk.git
   cd coindesk
   ```
2. 編譯並執行
   ```bash
   ./mvnw spring-boot:run
   ```
3. 專案啟動後，預設監聽 `http://localhost:8080`

---

### API 使用說明

以下為幣別維護 API 的操作整理：

| HTTP 方法 | URL               | 功能         |
|-----------|-------------------|--------------|
| GET       | /currencies       | 查詢全部幣別 |
| POST      | /currencies       | 新增幣別     |
| GET       | /currencies/{code}| 查詢單一幣別 |
| PUT       | /currencies/{code}| 修改幣別     |
| DELETE    | /currencies/{code}| 刪除幣別     |

#### 測試用 curl 範例

```bash
# 查詢所有幣別
curl http://localhost:8080/currencies

# 新增幣別
curl -X POST http://localhost:8080/currencies -H "Content-Type: application/json" -d '{"code":"TWD","nameZh":"新台幣"}'

# 修改幣別
curl -X PUT http://localhost:8080/currencies/TWD -H "Content-Type: application/json" -d '{"nameZh":"新臺幣"}'

# 查詢單一幣別
curl http://localhost:8080/currencies/TWD

# 刪除幣別
curl -X DELETE http://localhost:8080/currencies/TWD -i
```

---

## 🔧 curl 測試注意事項

由於不同作業系統對於字串與引號的處理方式不同，以下提供範例：

### Linux / macOS (bash / zsh)
```bash
curl -X POST http://localhost:8080/currencies   -H "Content-Type: application/json"   -d '{"code":"TWD","nameZh":"新台幣"}'
```

### Windows PowerShell
```powershell
curl -X POST http://localhost:8080/currencies `
  -H "Content-Type: application/json" `
  -d "{\"code\":\"TWD\",\"nameZh\":\"新台幣\"}"
```

### Windows cmd
```cmd
curl http://localhost:8080/currencies
curl -X POST http://localhost:8080/currencies -H "Content-Type: application/json" -d "{\"code\":\"twd\",\"nameZh\":\"新台幣\"}"
curl http://localhost:8080/currencies/TWD
curl -X PUT  http://localhost:8080/currencies/TWD -H "Content-Type: application/json" -d "{\"nameZh\":\"新臺幣\"}"
curl -X DELETE http://localhost:8080/currencies/TWD -i
```

---

## 匯率 API
- 取得原始 Coindesk JSON（含 fallback）：`GET /coindesk/raw`
- 取得轉換後的匯率資訊：`GET /coindesk/converted`
- 手動觸發匯率同步：`POST /coindesk/sync` (自動排程為10分鐘同步一次)
---

## 單元測試
執行：
```bash
./mvnw test
```

測試涵蓋：
- 幣別維護CRUD：查詢（含排序）、單筆查詢、新增（含 409）、更新（含 404）、刪除
- Coindesk API 呼叫與轉換
- Mock Data fallback

---

## 補充說明
- 使用 H2 Database（記憶體模式），專案啟動後會自動建立 schema 與資料。  
- Mock Data 與 API 呼叫程式碼同時存在，確保 API 不可用時仍可運作。  
- README 說明清楚列出已完成的需求與未實作的加分項目。  

---

## 作者
Jimw511
