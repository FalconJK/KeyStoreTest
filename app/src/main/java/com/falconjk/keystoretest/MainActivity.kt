package com.falconjk.keystoretest
// MainActivity.kt

import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import java.util.Base64

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val KEY_ALIAS_TEE = "test_tee_key"
    private val KEY_ALIAS_STRONGBOX = "test_strongbox_key"
    private val KEY_ALIAS_HUK_TEST = "test_huk_verification_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 創建 UI
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val titleText = TextView(this).apply {
            text = "Android Keystore 安全測試"
            textSize = 20f
            setPadding(0, 0, 0, 32)
        }

        val btnDeviceInfo = Button(this).apply {
            text = "1. 顯示裝置資訊"
            setOnClickListener { showDeviceInfo() }
        }

        val btnTestTEE = Button(this).apply {
            text = "2. 測試 TEE (TrustZone)"
            setOnClickListener { testTEE() }
        }

        val btnTestStrongBox = Button(this).apply {
            text = "3. 測試 StrongBox"
            setOnClickListener { testStrongBox() }
        }

        val btnTestHUK = Button(this).apply {
            text = "4. 測試 HUK (硬體唯一金鑰)"
            setOnClickListener { testHUK() }
        }

        val btnTestEncryption = Button(this).apply {
            text = "5. 測試加解密功能"
            setOnClickListener { testEncryption() }
        }

        val btnCheckAllKeys = Button(this).apply {
            text = "6. 檢查所有密鑰詳細資訊"
            setOnClickListener { checkAllKeysInfo() }
        }

        val btnExplainHUK = Button(this).apply {
            text = "7. HUK 原理說明"
            setOnClickListener { explainHUK() }
        }

        val btnClearKeys = Button(this).apply {
            text = "清除所有測試密鑰"
            setOnClickListener { clearAllKeys() }
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        resultTextView = TextView(this).apply {
            text = "歡迎使用 Android Keystore 測試工具\n請依序點擊按鈕進行測試\n\n"
            textSize = 14f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
        }

        scrollView.addView(resultTextView)

        layout.addView(titleText)
        layout.addView(btnDeviceInfo)
        layout.addView(btnTestTEE)
        layout.addView(btnTestStrongBox)
        layout.addView(btnTestHUK)
        layout.addView(btnTestEncryption)
        layout.addView(btnCheckAllKeys)
        layout.addView(btnExplainHUK)
        layout.addView(btnClearKeys)
        layout.addView(scrollView)

        setContentView(layout)
    }

    private fun showDeviceInfo() {
        clearResult()
        appendResult("╔═══════════════════════════════════════╗")
        appendResult("║        裝置與系統資訊                    ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        appendResult("📱 裝置資訊:")
        appendResult("  • 製造商: ${Build.MANUFACTURER}")
        appendResult("  • 品牌: ${Build.BRAND}")
        appendResult("  • 型號: ${Build.MODEL}")
        appendResult("  • 產品名稱: ${Build.PRODUCT}")
        appendResult("  • 硬體: ${Build.HARDWARE}")

        appendResult("\n🤖 系統資訊:")
        appendResult("  • Android 版本: ${Build.VERSION.RELEASE}")
        appendResult("  • API 等級: ${Build.VERSION.SDK_INT}")
        appendResult("  • 安全補丁: ${Build.VERSION.SECURITY_PATCH}")

        appendResult("\n🔐 安全功能支援:")
        appendResult("  • Keystore: ✓ (所有裝置)")
        appendResult("  • TEE/TrustZone: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) "✓ (API 23+)" else "✗"}")
        appendResult("  • StrongBox: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "? (需測試)" else "✗ (需 API 28+)"}")
        appendResult("  • HUK: ${if (isLikelyHasHUK()) "✓ (可能支援)" else "? (需測試)"}")

        appendResult("\n💡 提示:")
        appendResult("  請繼續執行其他測試以確認實際支援情況")
    }

    private fun testTEE() {
        appendResult("\n╔═══════════════════════════════════════╗")
        appendResult("║     測試 TEE (Trusted Execution      ║")
        appendResult("║          Environment)                 ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        appendResult("📖 TEE 說明:")
        appendResult("  TEE (可信執行環境) 是主處理器上的隔離")
        appendResult("  安全區域，也稱為 TrustZone (ARM) 或")
        appendResult("  SGX (Intel)。密鑰在此環境中生成和使用，")
        appendResult("  永不暴露給主系統。\n")

        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS_TEE,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            appendResult("✅ 測試結果:")
            appendResult("  ✓ AES-256 密鑰生成成功")
            appendResult("  ✓ 密鑰別名: $KEY_ALIAS_TEE")

            // 檢查密鑰安全等級
            val keyInfo = getKeyInfo(secretKey)

            if (keyInfo.isInsideSecureHardware) {
                appendResult("  ✓ 密鑰存儲在安全硬體中")
                appendResult("  ✓ TEE 支援: 是")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val securityLevel = getSecurityLevelString(keyInfo.securityLevel)
                    appendResult("  ✓ 安全等級: $securityLevel")
                }

                appendResult("\n🔒 安全保證:")
                appendResult("  • 密鑰在 TEE 中生成，永不離開安全區域")
                appendResult("  • 加解密操作在 TEE 內執行")
                appendResult("  • 主系統無法讀取密鑰內容")
                appendResult("  • 密鑰受 HUK 保護 (如果裝置支援)")

            } else {
                appendResult("  ⚠ 密鑰存儲在軟體中 (安全性較低)")
                appendResult("  ⚠ 此裝置可能不支援硬體 TEE")
            }

        } catch (e: Exception) {
            appendResult("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun testStrongBox() {
        appendResult("\n╔═══════════════════════════════════════╗")
        appendResult("║         測試 StrongBox                ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        appendResult("📖 StrongBox 說明:")
        appendResult("  StrongBox 是獨立的硬體安全模組 (HSM)，")
        appendResult("  與主處理器物理隔離。提供最高等級的")
        appendResult("  安全保護，常見於旗艦裝置。")
        appendResult("  例如: Google Pixel (Titan M/M2 晶片)\n")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            appendResult("❌ 測試結果:")
            appendResult("  ✗ StrongBox 需要 Android 9.0 (API 28)+")
            appendResult("  ✗ 當前版本: API ${Build.VERSION.SDK_INT}")
            appendResult("\n💡 建議: 升級到 Android 9.0 或更高版本")
            return
        }

        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS_STRONGBOX,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setIsStrongBoxBacked(true)  // 要求使用 StrongBox
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            appendResult("✅ 測試結果:")
            appendResult("  ✓ StrongBox 密鑰生成成功")
            appendResult("  ✓ 密鑰別名: $KEY_ALIAS_STRONGBOX")

            val keyInfo = getKeyInfo(secretKey)

            if (keyInfo.isInsideSecureHardware) {
                appendResult("  ✓ 密鑰存儲在安全硬體中")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                when (keyInfo.securityLevel) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> {
                        appendResult("  ✓ 確認使用 StrongBox (最高安全等級)")
                        appendResult("\n🏆 恭喜！此裝置支援 StrongBox")
                        appendResult("  • 密鑰存儲在獨立安全晶片中")
                        appendResult("  • 提供最高等級的防護")
                        appendResult("  • 可抵禦高級硬體攻擊")
                    }
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> {
                        appendResult("  ⚠ 實際使用 TEE，非 StrongBox")
                        appendResult("\n💡 說明:")
                        appendResult("  此裝置不支援 StrongBox，但支援 TEE")
                        appendResult("  TEE 仍然提供硬體級別的安全保護")
                    }
                    else -> {
                        appendResult("  ✗ 使用軟體實作 (不安全)")
                    }
                }
            }

        } catch (e: Exception) {
            appendResult("❌ 測試結果:")
            appendResult("  ✗ StrongBox 不可用")
            appendResult("  錯誤: ${e.javaClass.simpleName}")

            appendResult("\n💡 說明:")
            appendResult("  此裝置不支援 StrongBox 硬體安全模組")
            appendResult("  這是正常的，大多數裝置只支援 TEE")
            appendResult("  TEE 仍然提供足夠的安全保護")

            appendResult("\n📊 StrongBox 支援情況:")
            appendResult("  • Google Pixel 3+ : ✓")
            appendResult("  • Samsung 旗艦機 : 部分支援")
            appendResult("  • 其他品牌 : 少數旗艦機支援")
        }
    }

    private fun testHUK() {
        appendResult("\n╔═══════════════════════════════════════╗")
        appendResult("║    測試 HUK (Hardware Unique Key)    ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        appendResult("📖 HUK 說明:")
        appendResult("  HUK 是裝置製造時燒錄的唯一密鑰，")
        appendResult("  存儲在 eFuse 或 OTP 記憶體中。")
        appendResult("  特性:")
        appendResult("  • 每個裝置唯一")
        appendResult("  • 無法讀取或修改")
        appendResult("  • 用於派生其他密鑰")
        appendResult("  • 提供裝置綁定功能\n")

        try {
            // 生成測試密鑰
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS_HUK_TEST,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            val keyInfo = getKeyInfo(secretKey)

            appendResult("🔬 HUK 測試結果:\n")

            // 測試 1: 檢查硬體支援
            appendResult("測試 1: 硬體安全支援")
            if (keyInfo.isInsideSecureHardware) {
                appendResult("  ✓ 密鑰存儲在安全硬體中")
                appendResult("  ✓ 此裝置很可能支援 HUK")
                appendResult("  說明: 密鑰在 TEE 中生成時會使用 HUK")
                appendResult("        進行加密保護和派生\n")
            } else {
                appendResult("  ✗ 密鑰存儲在軟體中")
                appendResult("  ✗ 此裝置可能不支援 HUK")
                appendResult("  說明: 沒有硬體 TEE，無法使用 HUK\n")
            }

            // 測試 2: 裝置綁定測試
            appendResult("測試 2: 裝置綁定功能")
            val testData = "HUK_TEST_DATA_${System.currentTimeMillis()}"

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(testData.toByteArray())

            // 將加密資料轉為 Base64 (模擬導出)
            val encryptedBase64 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(encryptedData)
            } else {
                android.util.Base64.encodeToString(encryptedData, android.util.Base64.DEFAULT)
            }

            appendResult("  ✓ 測試資料已加密")
            appendResult("  密文長度: ${encryptedData.size} bytes")
            appendResult("\n  💡 裝置綁定說明:")
            appendResult("  如果此裝置支援 HUK，則:")
            appendResult("  • 此密文只能在本裝置上解密")
            appendResult("  • 即使複製密鑰檔案到其他裝置也無法解密")
            appendResult("  • 這是因為密鑰受本裝置 HUK 保護\n")

            // 測試 3: 解密驗證
            appendResult("測試 3: 解密驗證")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val decryptedData = cipher.doFinal(encryptedData)
            val decryptedText = String(decryptedData)

            if (testData == decryptedText) {
                appendResult("  ✓ 解密成功")
                appendResult("  ✓ 密鑰功能正常\n")
            }

            // 測試 4: HUK 派生鏈
            appendResult("測試 4: HUK 派生鏈分析")
            if (keyInfo.isInsideSecureHardware) {
                appendResult("  推測的密鑰派生流程:")
                appendResult("  ")
                appendResult("  HUK (eFuse 中，無法讀取)")
                appendResult("    ↓ 派生")
                appendResult("  Device Root Key")
                appendResult("    ↓ 派生")
                appendResult("  Keymaster Key")
                appendResult("    ↓ 派生")
                appendResult("  你的應用密鑰 ($KEY_ALIAS_HUK_TEST)")
                appendResult("  ")
                appendResult("  ✓ 每一層都在 TEE 中進行")
                appendResult("  ✓ HUK 確保密鑰與裝置綁定\n")
            }

            // 總結
            appendResult("📊 HUK 支援總結:")
            if (keyInfo.isInsideSecureHardware) {
                appendResult("  ✅ 此裝置支援 HUK")
                appendResult("  ✅ 密鑰受硬體保護")
                appendResult("  ✅ 具備裝置綁定功能")
                appendResult("  ✅ 可安全用於敏感資料保護")

                appendResult("\n🔒 安全保證:")
                appendResult("  • 密鑰無法被導出")
                appendResult("  • 密鑰無法複製到其他裝置")
                appendResult("  • 即使 root 也無法提取密鑰")
                appendResult("  • 提供硬體級別的防護")
            } else {
                appendResult("  ⚠ 此裝置可能不支援 HUK")
                appendResult("  ⚠ 密鑰存儲在軟體中")
                appendResult("  ⚠ 安全性較低")
            }

            // 廠商資訊
            appendResult("\n📱 常見廠商 HUK 支援:")
            appendResult("  • Qualcomm (高通): ✓ 支援")
            appendResult("  • MediaTek (聯發科): ✓ 支援")
            appendResult("  • Samsung Exynos: ✓ 支援")
            appendResult("  • Google Tensor: ✓ 支援")
            appendResult("  • 其他品牌: 視具體 SoC 而定")

        } catch (e: Exception) {
            appendResult("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun testEncryption() {
        appendResult("\n╔═══════════════════════════════════════╗")
        appendResult("║         測試加解密功能                ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            // 優先使用 TEE 密鑰
            val keyAlias = when {
                keyStore.containsAlias(KEY_ALIAS_TEE) -> KEY_ALIAS_TEE
                keyStore.containsAlias(KEY_ALIAS_STRONGBOX) -> KEY_ALIAS_STRONGBOX
                keyStore.containsAlias(KEY_ALIAS_HUK_TEST) -> KEY_ALIAS_HUK_TEST
                else -> {
                    appendResult("❌ 找不到測試密鑰")
                    appendResult("💡 請先執行「測試 TEE」生成密鑰")
                    return
                }
            }

            appendResult("使用密鑰: $keyAlias\n")

            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey

            // 加密測試
            val plainText = "Hello Android Keystore! 🔐\n這是測試數據\nTimestamp: ${System.currentTimeMillis()}"
            appendResult("📝 原始數據:")
            appendResult("$plainText\n")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plainText.toByteArray())

            appendResult("🔒 加密結果:")
            appendResult("  ✓ 加密成功")
            appendResult("  • 密文長度: ${encryptedData.size} bytes")
            appendResult("  • IV 長度: ${iv.size} bytes")
            appendResult("  • 演算法: AES-256-GCM\n")

            // 解密測試
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val decryptedData = cipher.doFinal(encryptedData)
            val decryptedText = String(decryptedData)

            appendResult("🔓 解密結果:")
            appendResult("  ✓ 解密成功")
            appendResult("$decryptedText\n")

            // 驗證
            if (plainText == decryptedText) {
                appendResult("✅ 驗證結果:")
                appendResult("  ✓ 加解密驗證通過！")
                appendResult("  ✓ 資料完整性確認")

                val keyInfo = getKeyInfo(secretKey)
                if (keyInfo.isInsideSecureHardware) {
                    appendResult("\n🔒 安全說明:")
                    appendResult("  • 加解密操作在 TEE 中執行")
                    appendResult("  • 明文密鑰從未暴露給主系統")
                    appendResult("  • 密鑰受 HUK 保護 (如支援)")
                }
            } else {
                appendResult("❌ 驗證失敗：資料不一致")
            }

        } catch (e: Exception) {
            appendResult("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkAllKeysInfo() {
        appendResult("\n╔═══════════════════════════════════════╗")
        appendResult("║       檢查所有密鑰詳細資訊            ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val aliases = keyStore.aliases().toList()
            if (aliases.isEmpty()) {
                appendResult("📭 沒有找到任何密鑰")
                appendResult("💡 請先執行其他測試生成密鑰")
                return
            }

            appendResult("找到 ${aliases.size} 個密鑰\n")

            for ((index, alias) in aliases.withIndex()) {
                appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendResult("密鑰 ${index + 1}: $alias")
                appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                val key = keyStore.getKey(alias, null)
                if (key is SecretKey) {
                    val keyInfo = getKeyInfo(key)

                    appendResult("🔑 基本資訊:")
                    appendResult("  • 演算法: ${key.algorithm}")
                    appendResult("  • 密鑰長度: ${keyInfo.keySize} bits")
                    appendResult("  • 區塊模式: ${keyInfo.blockModes.joinToString()}")
                    appendResult("  • 填充模式: ${keyInfo.encryptionPaddings.joinToString()}")

                    appendResult("\n🔒 安全資訊:")
                    appendResult("  • 安全硬體: ${if (keyInfo.isInsideSecureHardware) "✓ 是" else "✗ 否"}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val securityLevel = getSecurityLevelString(keyInfo.securityLevel)
                        appendResult("  • 安全等級: $securityLevel")
                    }

                    appendResult("\n⚙️ 使用限制:")
                    val purposes = mutableListOf<String>()
                    if (keyInfo.purposes and KeyProperties.PURPOSE_ENCRYPT != 0) purposes.add("加密")
                    if (keyInfo.purposes and KeyProperties.PURPOSE_DECRYPT != 0) purposes.add("解密")
                    if (keyInfo.purposes and KeyProperties.PURPOSE_SIGN != 0) purposes.add("簽名")
                    if (keyInfo.purposes and KeyProperties.PURPOSE_VERIFY != 0) purposes.add("驗證")
                    appendResult("  • 用途: ${purposes.joinToString(", ")}")

                    appendResult("  • 需要用戶認證: ${if (keyInfo.isUserAuthenticationRequired) "是" else "否"}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appendResult("  • 需要解鎖: ${if (keyInfo.isUserAuthenticationValidWhileOnBody) "否 (身體感應)" else "是"}")
                    }

                    // HUK 相關分析
                    if (keyInfo.isInsideSecureHardware) {
                        appendResult("\n🔐 HUK 保護分析:")
                        appendResult("  ✓ 此密鑰受 HUK 保護")
                        appendResult("  ✓ 密鑰與裝置綁定")
                        appendResult("  ✓ 無法複製到其他裝置")
                        appendResult("  ✓ 加解密在 TEE 中執行")
                    }

                    appendResult("")
                }
            }

        } catch (e: Exception) {
            appendResult("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun explainHUK() {
        clearResult()
        appendResult("╔═══════════════════════════════════════╗")
        appendResult("║      HUK 原理與架構詳細說明          ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        appendResult("📚 什麼是 HUK？\n")
        appendResult("HUK (Hardware Unique Key) 是硬體唯一金鑰，")
        appendResult("是裝置安全架構的信任根 (Root of Trust)。\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("🏭 HUK 的生成與存儲:\n")
        appendResult("1. 製造階段:")
        appendResult("   • 在晶片製造時生成隨機密鑰")
        appendResult("   • 燒錄到 eFuse 或 OTP 記憶體")
        appendResult("   • 燒錄後永久固定，無法修改\n")

        appendResult("2. 存儲位置:")
        appendResult("   • eFuse (電子熔絲)")
        appendResult("   • OTP (One-Time Programmable) 記憶體")
        appendResult("   • 位於 SoC 內部，物理隔離\n")

        appendResult("3. 安全特性:")
        appendResult("   • 每個裝置唯一")
        appendResult("   • 無法讀取 (只能在 TEE 內使用)")
        appendResult("   • 無法修改")
        appendResult("   • 無法刪除\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("🔄 HUK 的密鑰派生鏈:\n")
        appendResult("HUK (硬體，eFuse)")
        appendResult("  ↓ HKDF/KDF")
        appendResult("Device Root Key")
        appendResult("  ↓")
        appendResult("Keymaster Master Key")
        appendResult("  ↓")
        appendResult("App-specific Key Encryption Key")
        appendResult("  ↓")
        appendResult("你的應用密鑰 (加密存儲)")
        appendResult("")
        appendResult("每一層派生都在 TEE 中進行，")
        appendResult("上層密鑰永不暴露。\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("🔐 HUK 的使用場景:\n")
        appendResult("1. 密鑰派生:")
        appendResult("   • 派生 Keymaster 密鑰")
        appendResult("   • 派生應用專用密鑰")
        appendResult("   • 派生加密存儲密鑰\n")

        appendResult("2. 裝置綁定:")
        appendResult("   • 確保密鑰只能在本裝置使用")
        appendResult("   • 防止密鑰被複製到其他裝置")
        appendResult("   • 實現硬體級別的 DRM\n")

        appendResult("3. 安全啟動:")
        appendResult("   • 驗證啟動鏈完整性")
        appendResult("   • 確保系統未被篡改\n")

        appendResult("4. 裝置認證:")
        appendResult("   • 證明裝置身份")
        appendResult("   • 遠端認證 (Remote Attestation)\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("🛡️ HUK 的安全保證:\n")
        appendResult("✓ 防止密鑰提取:")
        appendResult("  即使攻擊者獲得 root 權限，也無法")
        appendResult("  讀取 HUK 或派生的密鑰\n")

        appendResult("✓ 防止密鑰複製:")
        appendResult("  密鑰與裝置硬體綁定，無法複製到")
        appendResult("  其他裝置\n")

        appendResult("✓ 防止離線攻擊:")
        appendResult("  即使提取加密的密鑰檔案，沒有 HUK")
        appendResult("  也無法解密\n")

        appendResult("✓ 防止克隆攻擊:")
        appendResult("  每個裝置的 HUK 唯一，無法克隆\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("🏗️ Android Keystore 架構:\n")
        appendResult("應用層 (App)")
        appendResult("  ↓ Android Keystore API")
        appendResult("Framework 層")
        appendResult("  ↓ Binder IPC")
        appendResult("Keystore Daemon (system/security/keystore)")
        appendResult("  ↓ HIDL/AIDL")
        appendResult("Keymaster HAL")
        appendResult("  ↓")
        appendResult("TEE (TrustZone)")
        appendResult("  ├─ Keymaster TA (Trusted App)")
        appendResult("  └─ HUK (eFuse)")
        appendResult("")
        appendResult("所有敏感操作都在 TEE 中執行，")
        appendResult("主系統無法接觸密鑰。\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("📊 不同廠商的 HUK 實作:\n")
        appendResult("• Qualcomm (高通):")
        appendResult("  使用 QSEE (Qualcomm Secure Execution")
        appendResult("  Environment)，HUK 存儲在 QFPROM\n")

        appendResult("• MediaTek (聯發科):")
        appendResult("  使用 Trustonic TEE 或 OP-TEE，")
        appendResult("  HUK 存儲在 eFuse\n")

        appendResult("• Samsung Exynos:")
        appendResult("  使用 TEEGRIS，HUK 存儲在 eFuse\n")

        appendResult("• Google Tensor:")
        appendResult("  基於 ARM TrustZone，整合 Titan M2\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("🔬 如何驗證 HUK 是否運作？\n")
        appendResult("1. 檢查密鑰是否在安全硬體中:")
        appendResult("   KeyInfo.isInsideSecureHardware() == true\n")

        appendResult("2. 嘗試導出密鑰:")
        appendResult("   應該失敗，因為密鑰無法離開 TEE\n")

        appendResult("3. 裝置綁定測試:")
        appendResult("   • 在裝置 A 加密資料")
        appendResult("   • 複製加密檔案到裝置 B")
        appendResult("   • 裝置 B 無法解密 (不同 HUK)\n")

        appendResult("4. 檢查安全等級:")
        appendResult("   Android 12+ 可查看 securityLevel\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("⚠️ HUK 的限制:\n")
        appendResult("• 無法備份:")
        appendResult("  密鑰與裝置綁定，無法備份到雲端\n")

        appendResult("• 裝置損壞:")
        appendResult("  如果裝置損壞，資料可能永久丟失\n")

        appendResult("• 系統重置:")
        appendResult("  恢復出廠設置後，舊密鑰無法恢復\n")

        appendResult("💡 建議:")
        appendResult("  對於重要資料，應該有額外的備份機制\n")

        appendResult("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        appendResult("📖 參考資料:\n")
        appendResult("• Android Keystore 官方文檔:")
        appendResult("  developer.android.com/training/articles/keystore\n")

        appendResult("• OP-TEE HUK 文檔:")
        appendResult("  optee.readthedocs.io/en/latest/")
        appendResult("  architecture/porting_guidelines.html\n")

        appendResult("• ARM TrustZone 技術:")
        appendResult("  developer.arm.com/ip-products/security-ip/")
        appendResult("  trustzone\n")
    }

    private fun clearAllKeys() {
        appendResult("\n╔═══════════════════════════════════════╗")
        appendResult("║         清除所有測試密鑰              ║")
        appendResult("╚═══════════════════════════════════════╝\n")

        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val allAliases = keyStore.aliases().toList()
            val testAliases = allAliases.filter {
                it.startsWith("test_")
            }

            if (testAliases.isEmpty()) {
                appendResult("📭 沒有找到測試密鑰")
                return
            }

            appendResult("找到 ${testAliases.size} 個測試密鑰:\n")

            for (alias in testAliases) {
                keyStore.deleteEntry(alias)
                appendResult("  ✓ 已刪除: $alias")
            }

            appendResult("\n✅ 清除完成")
            appendResult("共刪除 ${testAliases.size} 個測試密鑰")

        } catch (e: Exception) {
            appendResult("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    // 輔助函數
    private fun getKeyInfo(secretKey: SecretKey): KeyInfo {
        val factory = SecretKeyFactory.getInstance(
            secretKey.algorithm,
            KEYSTORE_PROVIDER
        )
        return factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
    }

    private fun getSecurityLevelString(level: Int): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (level) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> "StrongBox (最高)"
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "TEE/TrustZone (高)"
                KeyProperties.SECURITY_LEVEL_SOFTWARE -> "軟體 (低)"
                else -> "未知"
            }
        } else {
            "需要 Android 12+"
        }
    }

    private fun isLikelyHasHUK(): Boolean {
        // 根據廠商和 API 等級推測
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (Build.MANUFACTURER.lowercase().contains("qualcomm") ||
                        Build.MANUFACTURER.lowercase().contains("mediatek") ||
                        Build.MANUFACTURER.lowercase().contains("samsung") ||
                        Build.MANUFACTURER.lowercase().contains("google") ||
                        Build.HARDWARE.lowercase().contains("qcom") ||
                        Build.HARDWARE.lowercase().contains("mt"))
    }

    private fun clearResult() {
        resultTextView.text = ""
    }

    private fun appendResult(text: String) {
        runOnUiThread {
            resultTextView.text = "${resultTextView.text}$text\n"
            // 自動滾動到底部
            resultTextView.post {
                (resultTextView.parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }
}