# 🔐 Android Keystore 安全測試工具

一個全面的 Android Keystore 測試應用程式，用於檢測和驗證裝置的硬體安全功能，包括 TEE、StrongBox 和 HUK。

## 📱 功能特性

### 核心測試功能

1. **裝置資訊顯示** 📱
   - 顯示裝置製造商、型號、品牌
   - 顯示 Android 版本和 API 等級
   - 顯示安全補丁版本
   - 預判安全功能支援情況

2. **安全等級檢測** 🛡️
   - 自動檢測裝置支援的最高安全等級
   - 優先嘗試 StrongBox，降級至 TEE
   - 顯示實際使用的安全等級
   - Android 12+ 支援詳細等級資訊

3. **TEE (Trusted Execution Environment) 測試** 🔒
   - 測試 TrustZone/SGX 支援
   - 驗證密鑰是否在安全硬體中生成
   - 檢查 TEE 環境可用性
   - 顯示安全保證資訊

4. **StrongBox 測試** 🏆
   - 測試獨立安全晶片支援（如 Titan M/M2）
   - 驗證最高等級硬體安全模組
   - 檢測物理隔離的安全元件
   - 需要 Android 9.0+ (API 28+)

5. **HUK (Hardware Unique Key) 測試** 🔑
   - 測試硬體唯一金鑰支援
   - 驗證裝置綁定功能
   - 檢查密鑰派生鏈
   - 分析 HUK 保護機制

6. **HUK 原理說明** 📚
   - 詳細解釋 HUK 概念和架構
   - 說明密鑰派生鏈
   - 介紹不同廠商實作方式
   - 提供安全保證說明

7. **加解密功能測試** 🔐
   - 使用 AES-256-GCM 加密演算法
   - 驗證加解密完整性
   - 測試密鑰實際運作
   - 顯示加密過程詳細資訊

8. **密鑰資訊檢查** 🔍
   - 列出所有已生成的測試密鑰
   - 顯示每個密鑰的詳細資訊
   - 檢查安全等級和屬性
   - 分析 HUK 保護狀態

9. **密鑰管理** 🗑️
   - 清除所有測試密鑰
   - 重置測試環境
   - 安全刪除功能

## 🏗️ 專案結構

```
com.falconjk.keystoretest/
├── MainActivity.kt              # 主活動，UI 控制
├── DeviceInfoTester.kt          # 裝置資訊檢測
├── SecureLevelTester.kt         # 安全等級綜合測試
├── TEETester.kt                 # TEE 功能測試
├── StrongBoxTester.kt           # StrongBox 功能測試
├── HUKTester.kt                 # HUK 功能測試
├── HUKExplainer.kt              # HUK 原理說明
├── EncryptionTester.kt          # 加解密功能測試
├── KeyInfoChecker.kt            # 密鑰資訊檢查
└── KeyManager.kt                # 密鑰管理工具
```

## 🔧 技術細節

### 使用的 Android API

- **Android Keystore System**
  - `KeyStore` - 密鑰存儲管理
  - `KeyGenerator` - 密鑰生成
  - `KeyGenParameterSpec` - 密鑰參數配置
  - `KeyInfo` - 密鑰資訊查詢

- **加密演算法**
  - AES-256-GCM (對稱加密)
  - 無填充模式 (NoPadding)
  - 128-bit GCM 標籤

- **安全等級** (Android 12+)
  - `SECURITY_LEVEL_STRONGBOX` - StrongBox 等級
  - `SECURITY_LEVEL_TRUSTED_ENVIRONMENT` - TEE 等級
  - `SECURITY_LEVEL_SOFTWARE` - 軟體等級

### 密鑰別名

```kotlin
test_tee_key              // TEE 測試密鑰
test_strongbox_key        // StrongBox 測試密鑰
test_huk_verification_key // HUK 驗證密鑰
test_secure_level_key     // 安全等級測試密鑰
```

## 📋 系統需求

- **最低版本**: Android 6.0 (API 23)
- **建議版本**: Android 12+ (API 31+) 以獲得完整功能
- **StrongBox 功能**: 需要 Android 9.0+ (API 28+)
- **詳細安全等級**: 需要 Android 12+ (API 31+)

## 🚀 使用方法

### 建議測試順序

1. **顯示裝置資訊** - 了解裝置基本情況
2. **檢測最高安全等級** - 確認裝置支援的最高安全等級
3. **測試 TEE** - 驗證基本硬體安全支援
4. **測試 StrongBox** - 檢查是否有獨立安全晶片
5. **測試 HUK** - 驗證硬體唯一金鑰功能
6. **測試加解密功能** - 驗證實際加密運作
7. **檢查所有密鑰資訊** - 查看詳細密鑰屬性
8. **HUK 原理說明** - 深入了解安全架構

### 測試結果解讀

#### ✅ 理想結果（旗艦裝置）
```
✓ TEE 支援
✓ StrongBox 支援
✓ HUK 支援
✓ 安全等級: StrongBox
```

#### ✅ 良好結果（主流裝置）
```
✓ TEE 支援
✗ StrongBox 不支援
✓ HUK 支援
✓ 安全等級: TEE
```

#### ⚠️ 基本結果（入門裝置）
```
✗ TEE 不支援
✗ StrongBox 不支援
✗ HUK 不支援
✗ 安全等級: Software
```

## 🔒 安全架構說明

### 密鑰派生鏈

```
HUK (eFuse/OTP，硬體層)
  ↓ HKDF/KDF 派生
Device Root Key
  ↓ 派生
Keymaster Master Key
  ↓ 派生
App-specific KEK
  ↓ 加密保護
應用密鑰 (加密存儲)
```

### Android Keystore 架構

```
應用層 (App)
  ↓ Android Keystore API
Framework 層
  ↓ Binder IPC
Keystore Daemon
  ↓ HIDL/AIDL
Keymaster HAL
  ↓
TEE/StrongBox
  ├─ Keymaster TA
  └─ HUK (eFuse)
```

## 🏭 廠商實作

### Qualcomm (高通)
- **TEE**: QSEE (Qualcomm Secure Execution Environment)
- **HUK 存儲**: QFPROM (eFuse)
- **StrongBox**: 部分高階晶片支援

### MediaTek (聯發科)
- **TEE**: Trustonic TEE 或 OP-TEE
- **HUK 存儲**: eFuse
- **StrongBox**: 部分旗艦晶片支援

### Samsung Exynos
- **TEE**: TEEGRIS
- **HUK 存儲**: eFuse
- **StrongBox**: 高階機型支援

### Google Tensor
- **TEE**: ARM TrustZone
- **StrongBox**: Titan M2 安全晶片
- **HUK 存儲**: 整合於 Titan M2

## ⚠️ 注意事項

### 密鑰無法備份
- 密鑰與裝置硬體綁定
- 無法匯出或備份到雲端
- 恢復原廠設定會永久刪除密鑰

### 裝置更換影響
- 密鑰無法轉移到新裝置
- 需要重新生成密鑰
- 舊裝置加密的資料無法在新裝置解密

### 系統更新
- 一般系統更新不影響密鑰
- 恢復原廠設定會刪除所有密鑰
- 建議在更新前備份重要資料（非密鑰）

## 🛠️ 開發資訊

### 建置環境
- Kotlin
- Android Gradle Plugin
- Material Components
- AndroidX

### 權限需求
無需特殊權限（僅使用 Android Keystore API）

### 相容性測試
已在以下裝置測試：
- Google Pixel 系列
- Samsung Galaxy 系列
- 其他主流 Android 裝置

## 📚 參考資料

- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [Hardware-backed Keystore](https://source.android.com/security/keystore)
- [StrongBox Keymaster](https://developer.android.com/training/articles/keystore#HardwareSecurityModule)
- [ARM TrustZone](https://www.arm.com/technologies/trustzone-for-cortex-a)

## 📄 授權

本專案僅供學習和測試用途。

## 👨‍💻 作者

FalconJK

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

## 📞 聯絡方式

如有問題或建議，請透過 GitHub Issues 聯繫。

---

**⚡ 提示**: 建議在不同裝置上測試以了解各廠商的安全實作差異！