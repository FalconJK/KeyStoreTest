package com.falconjk.keystoretest.test

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import com.falconjk.keystoretest.Keys
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class TEETester {

    fun testTEE(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║     測試 TEE (Trusted Execution        ║")
        builder.appendLine("║          Environment)                 ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📖 TEE 說明:")
        builder.appendLine("  TEE (可信執行環境) 是主處理器上的隔離")
        builder.appendLine("  安全區域,也稱為 TrustZone (ARM) 或")
        builder.appendLine("  SGX (Intel)。密鑰在此環境中生成和使用,")
        builder.appendLine("  永不暴露給主系統。\n")

        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                Keys.KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                Keys.KEY_ALIAS_TEE,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            builder.appendLine("✅ 測試結果:")
            builder.appendLine("  ✓ AES-256 密鑰生成成功")
            builder.appendLine("  ✓ 密鑰別名: ${Keys}.KEY_ALIAS_TEE")

            val keyInfo = getKeyInfo(secretKey)

            if (keyInfo.isInsideSecureHardware) {
                builder.appendLine("  ✓ 密鑰存儲在安全硬體中")
                builder.appendLine("  ✓ TEE 支援: 是")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val securityLevel = getSecurityLevelString(keyInfo.securityLevel)
                    builder.appendLine("  ✓ 安全等級: $securityLevel")
                }

                builder.appendLine("\n🔒 安全保證:")
                builder.appendLine("  • 密鑰在 TEE 中生成,永不離開安全區域")
                builder.appendLine("  • 加解密操作在 TEE 內執行")
                builder.appendLine("  • 主系統無法讀取密鑰內容")
                builder.appendLine("  • 密鑰受 HUK 保護 (如果裝置支援)")

            } else {
                builder.appendLine("  ⚠ 密鑰存儲在軟體中 (安全性較低)")
                builder.appendLine("  ⚠ 此裝置可能不支援硬體 TEE")
            }

        } catch (e: Exception) {
            builder.appendLine("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }

        return builder.toString()
    }

    private fun getKeyInfo(secretKey: SecretKey): KeyInfo {
        val factory = SecretKeyFactory.getInstance(
            secretKey.algorithm,
            Keys.KEYSTORE_PROVIDER
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
}