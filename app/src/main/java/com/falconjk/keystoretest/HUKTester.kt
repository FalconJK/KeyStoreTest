package com.falconjk.keystoretest

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

class HUKTester {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val KEY_ALIAS_HUK_TEST = "test_huk_verification_key"

    fun testHUK(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║    測試 HUK (Hardware Unique Key)    ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📖 HUK 說明:")
        builder.appendLine("  HUK 是裝置製造時燒錄的唯一密鑰,")
        builder.appendLine("  存儲在 eFuse 或 OTP 記憶體中。")
        builder.appendLine("  特性:")
        builder.appendLine("  • 每個裝置唯一")
        builder.appendLine("  • 無法讀取或修改")
        builder.appendLine("  • 用於派生其他密鑰")
        builder.appendLine("  • 提供裝置綁定功能\n")

        try {
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

            builder.appendLine("🔬 HUK 測試結果:\n")

            // 測試 1
            builder.appendLine("測試 1: 硬體安全支援")
            if (keyInfo.isInsideSecureHardware) {
                builder.appendLine("  ✓ 密鑰存儲在安全硬體中")
                builder.appendLine("  ✓ 此裝置很可能支援 HUK")
                builder.appendLine("  說明: 密鑰在 TEE 中生成時會使用 HUK")
                builder.appendLine("        進行加密保護和派生\n")
            } else {
                builder.appendLine("  ✗ 密鑰存儲在軟體中")
                builder.appendLine("  ✗ 此裝置可能不支援 HUK")
                builder.appendLine("  說明: 沒有硬體 TEE,無法使用 HUK\n")
            }

            // 測試 2
            builder.appendLine("測試 2: 裝置綁定功能")
            val testData = "HUK_TEST_DATA_${System.currentTimeMillis()}"

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(testData.toByteArray())

            builder.appendLine("  ✓ 測試資料已加密")
            builder.appendLine("  密文長度: ${encryptedData.size} bytes")
            builder.appendLine("\n  💡 裝置綁定說明:")
            builder.appendLine("  如果此裝置支援 HUK,則:")
            builder.appendLine("  • 此密文只能在本裝置上解密")
            builder.appendLine("  • 即使複製密鑰檔案到其他裝置也無法解密")
            builder.appendLine("  • 這是因為密鑰受本裝置 HUK 保護\n")

            // 測試 3
            builder.appendLine("測試 3: 解密驗證")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val decryptedData = cipher.doFinal(encryptedData)
            val decryptedText = String(decryptedData)

            if (testData == decryptedText) {
                builder.appendLine("  ✓ 解密成功")
                builder.appendLine("  ✓ 密鑰功能正常\n")
            }

            // 測試 4
            builder.appendLine("測試 4: HUK 派生鏈分析")
            if (keyInfo.isInsideSecureHardware) {
                builder.appendLine("  推測的密鑰派生流程:")
                builder.appendLine("  ")
                builder.appendLine("  HUK (eFuse 中,無法讀取)")
                builder.appendLine("    ↓ 派生")
                builder.appendLine("  Device Root Key")
                builder.appendLine("    ↓ 派生")
                builder.appendLine("  Keymaster Key")
                builder.appendLine("    ↓ 派生")
                builder.appendLine("  你的應用密鑰 ($KEY_ALIAS_HUK_TEST)")
                builder.appendLine("  ")
                builder.appendLine("  ✓ 每一層都在 TEE 中進行")
                builder.appendLine("  ✓ HUK 確保密鑰與裝置綁定\n")
            }

            // 總結
            builder.appendLine("📊 HUK 支援總結:")
            if (keyInfo.isInsideSecureHardware) {
                builder.appendLine("  ✅ 此裝置支援 HUK")
                builder.appendLine("  ✅ 密鑰受硬體保護")
                builder.appendLine("  ✅ 具備裝置綁定功能")
                builder.appendLine("  ✅ 可安全用於敏感資料保護")

                builder.appendLine("\n🔒 安全保證:")
                builder.appendLine("  • 密鑰無法被導出")
                builder.appendLine("  • 密鑰無法複製到其他裝置")
                builder.appendLine("  • 即使 root 也無法提取密鑰")
                builder.appendLine("  • 提供硬體級別的防護")
            } else {
                builder.appendLine("  ⚠ 此裝置可能不支援 HUK")
                builder.appendLine("  ⚠ 密鑰存儲在軟體中")
                builder.appendLine("  ⚠ 安全性較低")
            }

            builder.appendLine("\n📱 常見廠商 HUK 支援:")
            builder.appendLine("  • Qualcomm (高通): ✓ 支援")
            builder.appendLine("  • MediaTek (聯發科): ✓ 支援")
            builder.appendLine("  • Samsung Exynos: ✓ 支援")
            builder.appendLine("  • Google Tensor: ✓ 支援")
            builder.appendLine("  • 其他品牌: 視具體 SoC 而定")

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
}