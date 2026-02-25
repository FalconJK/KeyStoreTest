package com.falconjk.keystoretest

import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class KeyInfoChecker {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"

    fun checkAllKeysInfo(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║       檢查所有密鑰詳細資訊            ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val aliases = keyStore.aliases().toList()
            if (aliases.isEmpty()) {
                builder.appendLine("📭 沒有找到任何密鑰")
                builder.appendLine("💡 請先執行其他測試生成密鑰")
                return builder.toString()
            }

            builder.appendLine("找到 ${aliases.size} 個密鑰\n")

            for ((index, alias) in aliases.withIndex()) {
                builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                builder.appendLine("密鑰 ${index + 1}: $alias")
                builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                val key = keyStore.getKey(alias, null)
                if (key is SecretKey) {
                    val keyInfo = getKeyInfo(key)

                    builder.appendLine("🔑 基本資訊:")
                    builder.appendLine("  • 演算法: ${key.algorithm}")
                    builder.appendLine("  • 密鑰長度: ${keyInfo.keySize} bits")
                    builder.appendLine("  • 區塊模式: ${keyInfo.blockModes.joinToString()}")
                    builder.appendLine("  • 填充模式: ${keyInfo.encryptionPaddings.joinToString()}")

                    builder.appendLine("\n🔒 安全資訊:")
                    builder.appendLine("  • 安全硬體: ${if (keyInfo.isInsideSecureHardware) "✓ 是" else "✗ 否"}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val securityLevel = getSecurityLevelString(keyInfo.securityLevel)
                        builder.appendLine("  • 安全等級: $securityLevel")
                    }

                    builder.appendLine("\n⚙️ 使用限制:")
                    val purposes = mutableListOf<String>()
                    if (keyInfo.purposes and KeyProperties.PURPOSE_ENCRYPT != 0) purposes.add("加密")
                    if (keyInfo.purposes and KeyProperties.PURPOSE_DECRYPT != 0) purposes.add("解密")
                    if (keyInfo.purposes and KeyProperties.PURPOSE_SIGN != 0) purposes.add("簽名")
                    if (keyInfo.purposes and KeyProperties.PURPOSE_VERIFY != 0) purposes.add("驗證")
                    builder.appendLine("  • 用途: ${purposes.joinToString(", ")}")

                    builder.appendLine("  • 需要用戶認證: ${if (keyInfo.isUserAuthenticationRequired) "是" else "否"}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        builder.appendLine("  • 需要解鎖: ${if (keyInfo.isUserAuthenticationValidWhileOnBody) "否 (身體感應)" else "是"}")
                    }

                    if (keyInfo.isInsideSecureHardware) {
                        builder.appendLine("\n🔐 HUK 保護分析:")
                        builder.appendLine("  ✓ 此密鑰受 HUK 保護")
                        builder.appendLine("  ✓ 密鑰與裝置綁定")
                        builder.appendLine("  ✓ 無法複製到其他裝置")
                        builder.appendLine("  ✓ 加解密在 TEE 中執行")
                    }

                    builder.appendLine("")
                }
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
}