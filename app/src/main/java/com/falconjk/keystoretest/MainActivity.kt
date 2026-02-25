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

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val KEY_ALIAS_AES = "test_aes_key"
    private val KEY_ALIAS_RSA = "test_rsa_key"
    private val KEY_ALIAS_STRONGBOX = "test_strongbox_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 創建 UI
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val titleText = TextView(this).apply {
            text = "MTK G700 Keystore & StrongBox 測試"
            textSize = 20f
            setPadding(0, 0, 0, 32)
        }

        val btnTestKeystore = Button(this).apply {
            text = "測試 Android Keystore (TEE)"
            setOnClickListener { testKeystore() }
        }

        val btnTestStrongBox = Button(this).apply {
            text = "測試 StrongBox"
            setOnClickListener { testStrongBox() }
        }

        val btnTestEncryption = Button(this).apply {
            text = "測試加解密功能"
            setOnClickListener { testEncryption() }
        }

        val btnCheckKeyInfo = Button(this).apply {
            text = "檢查密鑰安全資訊"
            setOnClickListener { checkKeyInfo() }
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
            text = "點擊按鈕開始測試...\n"
            textSize = 14f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
        }

        scrollView.addView(resultTextView)

        layout.addView(titleText)
        layout.addView(btnTestKeystore)
        layout.addView(btnTestStrongBox)
        layout.addView(btnTestEncryption)
        layout.addView(btnCheckKeyInfo)
        layout.addView(btnClearKeys)
        layout.addView(scrollView)

        setContentView(layout)

        // 初始檢查
        appendResult("=== 系統資訊 ===")
        appendResult("Android 版本: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendResult("設備型號: ${Build.MODEL}")
        appendResult("製造商: ${Build.MANUFACTURER}")
        appendResult("SoC: MTK G700\n")
    }

    private fun testKeystore() {
        appendResult("\n=== 測試 Android Keystore (TEE) ===")
        try {
            // 生成 AES 密鑰
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS_AES,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            appendResult("✓ AES-256 密鑰生成成功")
            appendResult("密鑰別名: $KEY_ALIAS_AES")

            // 檢查密鑰是否在 Keystore 中
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            if (keyStore.containsAlias(KEY_ALIAS_AES)) {
                appendResult("✓ 密鑰已存儲在 Android Keystore")
            }

            // 檢查 TEE 支援
            checkTEESupport(KEY_ALIAS_AES)

        } catch (e: Exception) {
            appendResult("✗ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun testStrongBox() {
        appendResult("\n=== 測試 StrongBox ===")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            appendResult("✗ StrongBox 需要 Android 9.0 (API 28) 或更高版本")
            appendResult("當前版本: API ${Build.VERSION.SDK_INT}")
            return
        }

        try {
            // 嘗試使用 StrongBox 生成密鑰
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

            appendResult("✓ StrongBox 密鑰生成成功")
            appendResult("密鑰別名: $KEY_ALIAS_STRONGBOX")

            // 驗證是否真的使用 StrongBox
            val factory = SecretKeyFactory.getInstance(
                secretKey.algorithm,
                KEYSTORE_PROVIDER
            )
            val keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

            if (keyInfo.isInsideSecureHardware) {
                appendResult("✓ 密鑰存儲在安全硬體中")
            } else {
                appendResult("✗ 密鑰未存儲在安全硬體中")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX) {
                    appendResult("✓ 確認使用 StrongBox (最高安全等級)")
                } else if (keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT) {
                    appendResult("⚠ 使用 TEE (TrustZone)，非 StrongBox")
                    appendResult("  MTK G700 可能不支援 StrongBox，但支援 TEE")
                } else {
                    appendResult("✗ 使用軟體實作 (不安全)")
                }
            }

        } catch (e: Exception) {
            appendResult("✗ StrongBox 不可用: ${e.message}")
            appendResult("⚠ MTK G700 可能僅支援 TEE (TrustZone)")
            appendResult("  這仍然提供硬體級別的安全保護")
        }
    }

    private fun testEncryption() {
        appendResult("\n=== 測試加解密功能 ===")
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS_AES)) {
                appendResult("請先執行 '測試 Android Keystore' 生成密鑰")
                return
            }

            val secretKey = keyStore.getKey(KEY_ALIAS_AES, null) as SecretKey

            // 加密測試
            val plainText = "Hello MTK G700! 這是測試數據 🚁"
            appendResult("原始數據: $plainText")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plainText.toByteArray())

            appendResult("✓ 加密成功")
            appendResult("密文長度: ${encryptedData.size} bytes")
            appendResult("IV 長度: ${iv.size} bytes")

            // 解密測試
            cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.parameters)
            val decryptedData = cipher.doFinal(encryptedData)
            val decryptedText = String(decryptedData)

            appendResult("✓ 解密成功")
            appendResult("解密數據: $decryptedText")

            if (plainText == decryptedText) {
                appendResult("✓ 加解密驗證通過！")
            } else {
                appendResult("✗ 加解密驗證失敗！")
            }

        } catch (e: Exception) {
            appendResult("✗ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkKeyInfo() {
        appendResult("\n=== 檢查密鑰安全資訊 ===")
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val aliases = keyStore.aliases().toList()
            if (aliases.isEmpty()) {
                appendResult("沒有找到密鑰，請先生成密鑰")
                return
            }

            for (alias in aliases) {
                appendResult("\n--- 密鑰: $alias ---")

                val key = keyStore.getKey(alias, null)
                if (key is SecretKey) {
                    val factory = SecretKeyFactory.getInstance(
                        key.algorithm,
                        KEYSTORE_PROVIDER
                    )
                    val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo

                    appendResult("算法: ${keyInfo.keySize}-bit ${key.algorithm}")
                    appendResult("安全硬體: ${if (keyInfo.isInsideSecureHardware) "是 ✓" else "否 ✗"}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val securityLevel = when (keyInfo.securityLevel) {
                            KeyProperties.SECURITY_LEVEL_STRONGBOX -> "StrongBox (最高)"
                            KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "TEE/TrustZone (高)"
                            KeyProperties.SECURITY_LEVEL_SOFTWARE -> "軟體 (低)"
                            else -> "未知"
                        }
                        appendResult("安全等級: $securityLevel")
                    }

                    appendResult("用途: ${keyInfo.purposes}")
                    appendResult("區塊模式: ${keyInfo.blockModes.joinToString()}")
                    appendResult("填充模式: ${keyInfo.encryptionPaddings.joinToString()}")
                }
            }

            // 檢查 HUK 相關資訊
            appendResult("\n--- MTK HUK 資訊 ---")
            appendResult("MTK SoC 默認支援 HUK (Hardware Unique Key)")
            appendResult("HUK 用於派生其他密鑰，提供設備唯一性")
            appendResult("HUK 存儲在 eFuse 中，無法被讀取或修改")

        } catch (e: Exception) {
            appendResult("✗ 錯誤: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkTEESupport(alias: String) {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val key = keyStore.getKey(alias, null) as SecretKey
            val factory = SecretKeyFactory.getInstance(
                key.algorithm,
                KEYSTORE_PROVIDER
            )
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo

            if (keyInfo.isInsideSecureHardware) {
                appendResult("✓ TEE (TrustZone) 支援: 是")
                appendResult("✓ 密鑰受 MTK Keymaster 保護")
                appendResult("✓ 加解密操作在 TrustZone 中執行")
            } else {
                appendResult("✗ TEE 支援: 否 (使用軟體實作)")
            }

        } catch (e: Exception) {
            appendResult("檢查 TEE 支援時發生錯誤: ${e.message}")
        }
    }

    private fun clearAllKeys() {
        appendResult("\n=== 清除所有測試密鑰 ===")
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val aliases = listOf(KEY_ALIAS_AES, KEY_ALIAS_RSA, KEY_ALIAS_STRONGBOX)
            var deletedCount = 0

            for (alias in aliases) {
                if (keyStore.containsAlias(alias)) {
                    keyStore.deleteEntry(alias)
                    appendResult("✓ 已刪除: $alias")
                    deletedCount++
                }
            }

            if (deletedCount == 0) {
                appendResult("沒有找到測試密鑰")
            } else {
                appendResult("✓ 共刪除 $deletedCount 個密鑰")
            }

        } catch (e: Exception) {
            appendResult("✗ 錯誤: ${e.message}")
        }
    }

    private fun appendResult(text: String) {
        runOnUiThread {
            resultTextView.text = "${resultTextView.text}$text\n"
            // 自動滾動到底部
            (resultTextView.parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}