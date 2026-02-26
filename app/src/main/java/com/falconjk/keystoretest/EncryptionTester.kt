package com.falconjk.keystoretest

import android.security.keystore.KeyInfo
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

class EncryptionTester {

    fun testEncryption(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║         測試加解密功能                ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        try {
            val keyStore = KeyStore.getInstance(Keys.KEYSTORE_PROVIDER).apply { load(null) }

            val keyAlias = when {
                keyStore.containsAlias(Keys.KEY_ALIAS_TEE) -> Keys.KEY_ALIAS_TEE
                keyStore.containsAlias(Keys.KEY_ALIAS_STRONGBOX) -> Keys.KEY_ALIAS_STRONGBOX
                keyStore.containsAlias(Keys.KEY_ALIAS_HUK_TEST) -> Keys.KEY_ALIAS_HUK_TEST
                else -> {
                    builder.appendLine("❌ 找不到測試密鑰")
                    builder.appendLine("💡 請先執行「測試 TEE」生成密鑰")
                    return builder.toString()
                }
            }

            builder.appendLine("使用密鑰: $keyAlias\n")

            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey

            val plainText = "Hello Android Keystore! 🔐\n這是測試數據\nTimestamp: ${System.currentTimeMillis()}"
            builder.appendLine("📝 原始數據:")
            builder.appendLine("$plainText\n")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plainText.toByteArray())

            builder.appendLine("🔒 加密結果:")
            builder.appendLine("  ✓ 加密成功")
            builder.appendLine("  • 密文長度: ${encryptedData.size} bytes")
            builder.appendLine("  • IV 長度: ${iv.size} bytes")
            builder.appendLine("  • 演算法: AES-256-GCM\n")

            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val decryptedData = cipher.doFinal(encryptedData)
            val decryptedText = String(decryptedData)

            builder.appendLine("🔓 解密結果:")
            builder.appendLine("  ✓ 解密成功")
            builder.appendLine("$decryptedText\n")

            if (plainText == decryptedText) {
                builder.appendLine("✅ 驗證結果:")
                builder.appendLine("  ✓ 加解密驗證通過！")
                builder.appendLine("  ✓ 資料完整性確認")

                val keyInfo = getKeyInfo(secretKey)
                if (keyInfo.isInsideSecureHardware) {
                    builder.appendLine("\n🔒 安全說明:")
                    builder.appendLine("  • 加解密操作在 TEE 中執行")
                    builder.appendLine("  • 明文密鑰從未暴露給主系統")
                    builder.appendLine("  • 密鑰受 HUK 保護 (如支援)")
                }
            } else {
                builder.appendLine("❌ 驗證失敗：資料不一致")
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
}