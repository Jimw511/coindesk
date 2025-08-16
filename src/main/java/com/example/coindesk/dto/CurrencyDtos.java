package com.example.coindesk.dto; // 定義這個類別所在的套件位置

import jakarta.validation.constraints.NotBlank; // 驗證欄位不可為空白
import jakarta.validation.constraints.Pattern; // 驗證字串格式（例如只允許字母）
import jakarta.validation.constraints.Size; // 驗證字串長度限制

/**
 * Currency 的 DTO 集合
 * - 使用 record 表達不可變物件（Immutable），程式更簡潔與安全
 * - CreateRequest / UpdateRequest 加上輸入驗證，確保資料合法
 * - Response 只回傳必要欄位，不會外洩內部資訊
 */
public class CurrencyDtos {
    /**
     * 建立用請求 DTO
     * - 需要輸入幣別代碼（code）與中文名稱（nameZh）
     * - code 僅允許英文字母（ISO 4217 格式），大小寫不拘，實際儲存時會轉為大寫
     */
    public record CreateRequest(
            @NotBlank // 不可為空
            @Size(max = 10) // 最長 10 字元
            @Pattern(regexp = "^[A-Za-z]+$", message = "code 僅允許英文字母") // 僅允許 A-Z, a-z
            String code, // 幣別代碼（例：USD、EUR）

            @NotBlank // 不可為空
            @Size(max = 50) // 最長 50 字元
            String nameZh // 幣別中文名稱（例：美元、歐元）
    ) {
    }

    /**
     * 更新用請求 DTO
     * - 只允許修改中文名稱（nameZh）
     */
    public record UpdateRequest(
            @NotBlank // 不可為空
            @Size(max = 50) // 最長 50 字元
            String nameZh // 幣別中文名稱（例：日圓、英鎊）
    ) {
    }

    /**
     * 查詢/回應用 DTO
     * - 對外只回傳幣別代碼與中文名稱
     * - 不包含內部使用的 createdAt、updatedAt
     */
    public record Response(
            String code, // 幣別代碼
            String nameZh // 幣別中文名稱
    ) {
    }
}
