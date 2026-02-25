package com.falconjk.keystoretest

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class StrongBoxTester {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val KEY_ALIAS_STRONGBOX = "test_strongbox_key"

    fun testStrongBox(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║         測試 StrongBox                ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📖 StrongBox 說明:")
        builder.appendLine("  StrongBox 是獨立的硬體安全模組 (HSM),")
        builder.appendLine("  與主處理器物理隔離。提供最高等級的")
        builder.appendLine("  安全保護,常見於旗艦裝置。")
        builder.appendLine("  例如: Google Pixel (Titan M/M2 晶片)\n")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            builder.appendLine("❌ 測試結果:")
            builder.appendLine("  ✗ StrongBox 需要 Android 9.0 (API 28)+")
            builder.appendLine("  ✗ 當前版本: API ${Build.VERSION.SDK_INT}")
            builder.appendLine("\n💡 建議: 升級到 Android 9.0 或更高版本")
            return builder.toString()
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
                .setIsStrongBoxBacked(true)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            builder.appendLine("✅ 測試結果:")
            builder.appendLine("  ✓ StrongBox 密鑰生成成功")
            builder.appendLine("  ✓ 密鑰別名: $KEY_ALIAS_STRONGBOX")

            val keyInfo = getKeyInfo(secretKey)

            if (keyInfo.isInsideSecureHardware) {
                builder.appendLine("  ✓ 密鑰存儲在安全硬體中")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                when (keyInfo.securityLevel) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> {
                        builder.appendLine("  ✓ 確認使用 StrongBox (最高安全等級)")
                        builder.appendLine("\n🏆 恭喜！此裝置支援 StrongBox")
                        builder.appendLine("  • 密鑰存儲在獨立安全晶片中")
                        builder.appendLine("  • 提供最高等級的防護")
                        builder.appendLine("  • 可抵禦高級硬體攻擊")
                    }
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> {
                        builder.appendLine("  ⚠ 實際使用 TEE,非 StrongBox")
                        builder.appendLine("\n💡 說明:")
                        builder.appendLine("  此裝置不支援 StrongBox,但支援 TEE")
                        builder.appendLine("  TEE 仍然提供硬體級別的安全保護")
                    }
                    else -> {
                        builder.appendLine("  ✗ 使用軟體實作 (不安全)")
                    }
                }
            }

        } catch (e: Exception) {
            builder.appendLine("❌ 測試結果:")
            builder.appendLine("  ✗ StrongBox 不可用")
            builder.appendLine("  錯誤: ${e.javaClass.simpleName}")

            builder.appendLine("\n💡 說明:")
            builder.appendLine("  此裝置不支援 StrongBox 硬體安全模組")
            builder.appendLine("  這是正常的,大多數裝置只支援 TEE")
            builder.appendLine("  TEE 仍然提供足夠的安全保護")

            builder.appendLine("\n📊 StrongBox 支援情況:")
            builder.appendLine("  • Google Pixel 3+ : ✓")
            builder.appendLine("  • Samsung 旗艦機 : 部分支援")
            builder.appendLine("  • 其他品牌 : 少數旗艦機支援")
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
}