package com.falconjk.keystoretest.test

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import com.falconjk.keystoretest.Keys
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class SecureLevelTester {

    fun testSecureLevel(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║      測試密鑰安全等級 (Security Level) ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📖 說明:")
        builder.appendLine("  此測試將嘗試生成密鑰並檢查其實際存儲位置")
        builder.appendLine("  與安全等級。優先嘗試使用 StrongBox。")
        builder.appendLine("  (Android 12+ 支援詳細等級顯示)\n")

        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                Keys.KEYSTORE_PROVIDER
            )

            var isStrongBoxRequested = false
            var secretKey: SecretKey? = null

            // 步驟 1: 嘗試生成密鑰
            builder.appendLine("⚙️ 執行生成測試:")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    builder.appendLine("  1. 嘗試請求 StrongBox (獨立安全晶片)...")
                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        Keys.KEY_ALIAS_LEVEL_TEST,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setIsStrongBoxBacked(true)
                        .setUserAuthenticationRequired(false)
                        .build()
                    
                    keyGenerator.init(keyGenParameterSpec)
                    secretKey = keyGenerator.generateKey()
                    isStrongBoxRequested = true
                    builder.appendLine("     ✓ StrongBox 請求成功")
                } catch (e: Exception) {
                    builder.appendLine("     ✗ StrongBox 請求失敗或不支援")
                    // builder.appendLine("       原因: ${e.message}") 
                }
            } else {
                builder.appendLine("  • Android 版本低於 9.0，跳過 StrongBox 測試")
            }

            // 如果 StrongBox 失敗或未執行，嘗試標準 TEE
            if (secretKey == null) {
                builder.appendLine("  2. 降級嘗試標準 TEE 環境...")
                try {
                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        Keys.KEY_ALIAS_LEVEL_TEST,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setUserAuthenticationRequired(false)
                        .build()
                    
                    keyGenerator.init(keyGenParameterSpec)
                    secretKey = keyGenerator.generateKey()
                    builder.appendLine("     ✓ TEE 密鑰生成成功")
                } catch (e: Exception) {
                    builder.appendLine("     ❌ TEE 密鑰生成失敗: ${e.message}")
                    return builder.toString()
                }
            }

            // 步驟 2: 檢查 KeyInfo
            val factory = SecretKeyFactory.getInstance(
                secretKey!!.algorithm,
                Keys.KEYSTORE_PROVIDER
            )
            val keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

            builder.appendLine("\n📊 檢測結果分析:")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val level = keyInfo.securityLevel
                val levelStr = when (level) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> "StrongBox (最高安全等級)"
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "TEE (可信執行環境)"
                    KeyProperties.SECURITY_LEVEL_SOFTWARE -> "Software (軟體模擬)"
                    else -> "Unknown (未知)"
                }
                
                builder.appendLine("  • Security Level ID: $level")
                builder.appendLine("  • 判定結果: $levelStr")
                
                builder.appendLine("\n📝 結論:")
                when (level) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> {
                        builder.appendLine("  ✅ 您的裝置支援並使用了最高等級的硬體安全模組！")
                        builder.appendLine("  密鑰存儲在獨立的防篡改硬體晶片中。")
                    }
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> {
                        builder.appendLine("  ✅ 您的裝置使用 TEE 保護，安全性良好。")
                        builder.appendLine("  密鑰存儲在主處理器的隔離安全區域中。")
                        if (isStrongBoxRequested) {
                            builder.appendLine("  (注意: 雖然請求了 StrongBox 但系統回退到了 TEE)")
                        }
                    }
                    else -> {
                        builder.appendLine("  ⚠️ 注意：您的密鑰僅由軟體保護，安全性較低。")
                        builder.appendLine("  容易受到 Root 攻擊或記憶體讀取攻擊。")
                    }
                }
                
            } else {
                // Android 12 以下
                val isSecure = keyInfo.isInsideSecureHardware
                builder.appendLine("  • Inside Secure Hardware: ${if(isSecure) "True (是)" else "False (否)"}")
                
                builder.appendLine("\n📝 結論:")
                if (isSecure) {
                    builder.appendLine("  ✅ 安全性確認：密鑰受硬體保護。")
                    builder.appendLine("  密鑰存儲在 TEE 或 StrongBox 中。")
                } else {
                    builder.appendLine("  ⚠️ 安全性警告：密鑰未受硬體保護。")
                    builder.appendLine("  密鑰存儲在 Android 系統軟體中。")
                }
                builder.appendLine("  (註: Android 12 以下系統無法區分 TEE 與 StrongBox)")
            }

        } catch (e: Exception) {
            builder.appendLine("\n❌ 測試發生嚴重錯誤:")
            builder.appendLine("  ${e.message}")
            e.printStackTrace()
        }

        return builder.toString()
    }
}