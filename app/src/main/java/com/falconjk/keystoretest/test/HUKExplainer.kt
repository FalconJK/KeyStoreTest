package com.falconjk.keystoretest.test

class HUKExplainer {

    fun getExplanation(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║      HUK 原理與架構詳細說明          ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📚 什麼是 HUK？\n")
        builder.appendLine("HUK (Hardware Unique Key) 是硬體唯一金鑰,")
        builder.appendLine("是裝置安全架構的信任根 (Root of Trust)。\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("🏭 HUK 的生成與存儲:\n")
        builder.appendLine("1. 製造階段:")
        builder.appendLine("   • 在晶片製造時生成隨機密鑰")
        builder.appendLine("   • 燒錄到 eFuse 或 OTP 記憶體")
        builder.appendLine("   • 燒錄後永久固定,無法修改\n")

        builder.appendLine("2. 存儲位置:")
        builder.appendLine("   • eFuse (電子熔絲)")
        builder.appendLine("   • OTP (One-Time Programmable) 記憶體")
        builder.appendLine("   • 位於 SoC 內部,物理隔離\n")

        builder.appendLine("3. 安全特性:")
        builder.appendLine("   • 每個裝置唯一")
        builder.appendLine("   • 無法讀取 (只能在 TEE 內使用)")
        builder.appendLine("   • 無法修改")
        builder.appendLine("   • 無法刪除\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("🔄 HUK 的密鑰派生鏈:\n")
        builder.appendLine("HUK (硬體,eFuse)")
        builder.appendLine("  ↓ HKDF/KDF")
        builder.appendLine("Device Root Key")
        builder.appendLine("  ↓")
        builder.appendLine("Keymaster Master Key")
        builder.appendLine("  ↓")
        builder.appendLine("App-specific Key Encryption Key")
        builder.appendLine("  ↓")
        builder.appendLine("你的應用密鑰 (加密存儲)")
        builder.appendLine("")
        builder.appendLine("每一層派生都在 TEE 中進行,")
        builder.appendLine("上層密鑰永不暴露。\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("🔐 HUK 的使用場景:\n")
        builder.appendLine("1. 密鑰派生:")
        builder.appendLine("   • 派生 Keymaster 密鑰")
        builder.appendLine("   • 派生應用專用密鑰")
        builder.appendLine("   • 派生加密存儲密鑰\n")

        builder.appendLine("2. 裝置綁定:")
        builder.appendLine("   • 確保密鑰只能在本裝置使用")
        builder.appendLine("   • 防止密鑰被複製到其他裝置")
        builder.appendLine("   • 實現硬體級別的 DRM\n")

        builder.appendLine("3. 安全啟動:")
        builder.appendLine("   • 驗證啟動鏈完整性")
        builder.appendLine("   • 確保系統未被篡改\n")

        builder.appendLine("4. 裝置認證:")
        builder.appendLine("   • 證明裝置身份")
        builder.appendLine("   • 遠端認證 (Remote Attestation)\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("🛡️ HUK 的安全保證:\n")
        builder.appendLine("✓ 防止密鑰提取:")
        builder.appendLine("  即使攻擊者獲得 root 權限,也無法")
        builder.appendLine("  讀取 HUK 或派生的密鑰\n")

        builder.appendLine("✓ 防止密鑰複製:")
        builder.appendLine("  密鑰與裝置硬體綁定,無法複製到")
        builder.appendLine("  其他裝置\n")

        builder.appendLine("✓ 防止離線攻擊:")
        builder.appendLine("  即使提取加密的密鑰檔案,沒有 HUK")
        builder.appendLine("  也無法解密\n")

        builder.appendLine("✓ 防止克隆攻擊:")
        builder.appendLine("  每個裝置的 HUK 唯一,無法克隆\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("🏗️ Android Keystore 架構:\n")
        builder.appendLine("應用層 (App)")
        builder.appendLine("  ↓ Android Keystore API")
        builder.appendLine("Framework 層")
        builder.appendLine("  ↓ Binder IPC")
        builder.appendLine("Keystore Daemon (system/security/keystore)")
        builder.appendLine("  ↓ HIDL/AIDL")
        builder.appendLine("Keymaster HAL")
        builder.appendLine("  ↓")
        builder.appendLine("TEE (TrustZone)")
        builder.appendLine("  ├─ Keymaster TA (Trusted App)")
        builder.appendLine("  └─ HUK (eFuse)")
        builder.appendLine("")
        builder.appendLine("所有敏感操作都在 TEE 中執行,")
        builder.appendLine("主系統無法接觸密鑰。\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("📊 不同廠商的 HUK 實作:\n")
        builder.appendLine("• Qualcomm (高通):")
        builder.appendLine("  使用 QSEE (Qualcomm Secure Execution")
        builder.appendLine("  Environment),HUK 存儲在 QFPROM\n")

        builder.appendLine("• MediaTek (聯發科):")
        builder.appendLine("  使用 Trustonic TEE 或 OP-TEE,")
        builder.appendLine("  HUK 存儲在 eFuse\n")

        builder.appendLine("• Samsung Exynos:")
        builder.appendLine("  使用 TEEGRIS,HUK 存儲在 eFuse\n")

        builder.appendLine("• Google Tensor:")
        builder.appendLine("  基於 ARM TrustZone,整合 Titan M2\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("🔬 如何驗證 HUK 是否運作？\n")
        builder.appendLine("1. 檢查密鑰是否在安全硬體中:")
        builder.appendLine("   KeyInfo.isInsideSecureHardware() == true\n")

        builder.appendLine("2. 嘗試導出密鑰:")
        builder.appendLine("   應該失敗,因為密鑰無法離開 TEE\n")

        builder.appendLine("3. 裝置綁定測試:")
        builder.appendLine("   • 在裝置 A 加密資料")
        builder.appendLine("   • 複製加密檔案到裝置 B")
        builder.appendLine("   • 裝置 B 無法解密 (不同 HUK)\n")

        builder.appendLine("4. 檢查安全等級:")
        builder.appendLine("   Android 12+ 可查看 securityLevel\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("⚠️ HUK 的限制:\n")
        builder.appendLine("• 無法備份:")
        builder.appendLine("  密鑰與裝置綁定,無法備份到雲端\n")

        builder.appendLine("• 裝置損壞:")
        builder.appendLine("  如果裝置損壞,資料可能永久丟失\n")

        builder.appendLine("• 系統重置:")
        builder.appendLine("  恢復出廠設置後,舊密鑰無法恢復\n")

        builder.appendLine("💡 建議:")
        builder.appendLine("  對於重要資料,應該有額外的備份機制\n")

        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        builder.appendLine("📖 參考資料:\n")
        builder.appendLine("• Android Keystore 官方文檔:")
        builder.appendLine("  developer.android.com/training/articles/keystore\n")

        builder.appendLine("• OP-TEE HUK 文檔:")
        builder.appendLine("  optee.readthedocs.io/en/latest/")
        builder.appendLine("  architecture/porting_guidelines.html\n")

        builder.appendLine("• ARM TrustZone 技術:")
        builder.appendLine("  developer.arm.com/ip-products/security-ip/")
        builder.appendLine("  trustzone\n")

        return builder.toString()
    }
}