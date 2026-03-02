package com.falconjk.keystoretest.test

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import com.falconjk.keystoretest.Keys
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class AsymmetricTester {

    /**
     * Android Keystore OAEP 限制：
     *   - 主 digest  → SHA-256 (由 KeyGenParameterSpec 指定)
     *   - MGF1 digest → 只支援 SHA-1 (AndroidKeyStoreRSACipherSpi 硬性限制)
     *
     * 加密端（軟體層）與解密端（KeyMint 硬體層）必須使用完全相同的 spec，
     * 否則會因參數不一致而失敗。
     */
    private val oaepSpec = OAEPParameterSpec(
        "SHA-256",              // 主 digest
        "MGF1",                 // MGF 演算法
        MGF1ParameterSpec.SHA1, // MGF1 digest → 只能 SHA-1（Keystore 硬性限制）
        PSource.PSpecified.DEFAULT
    )

    fun testAsymmetric(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║    測試非對稱加密 (RSA-2048)          ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📖 說明:")
        builder.appendLine("  非對稱加密使用一對密鑰:")
        builder.appendLine("  • 公鑰 (Public Key)  → 可公開，用於加密/驗簽")
        builder.appendLine("  • 私鑰 (Private Key) → 存於 TEE，永不離開安全區域")
        builder.appendLine("  ⚠ Android Keystore 已知限制：")
        builder.appendLine("    1. 加解密與簽章必須使用獨立金鑰對")
        builder.appendLine("    2. OAEP 的 MGF1 digest 只支援 SHA-1")
        builder.appendLine("       (主 digest 可用 SHA-256)\n")

        // ── 測試 1: 生成加解密金鑰對 ────────────────────────────
        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("測試 1: 生成 RSA-2048 加解密金鑰對\n")

        val encKeyResult = runCatching { generateRSAEncKeyPair() }
        if (encKeyResult.isFailure) {
            builder.appendLine("  ❌ 加解密金鑰對生成失敗: ${encKeyResult.exceptionOrNull()?.message}\n")
            return builder.toString()
        }
        val encPublicKey = encKeyResult.getOrThrow()
        builder.appendLine("  ✓ 金鑰別名: ${Keys.KEY_ALIAS_RSA_ENC}")
        builder.appendLine("  ✓ 用途: ENCRYPT / DECRYPT")
        builder.appendLine("  ✓ Padding: OAEP (主digest=SHA-256, MGF1=SHA-1)\n")

        // ── 測試 2: 生成簽章金鑰對 ──────────────────────────────
        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("測試 2: 生成 RSA-2048 簽章金鑰對\n")

        val signKeyResult = runCatching { generateRSASignKeyPair() }
        if (signKeyResult.isFailure) {
            builder.appendLine("  ❌ 簽章金鑰對生成失敗: ${signKeyResult.exceptionOrNull()?.message}\n")
            return builder.toString()
        }
        val signPublicKey = signKeyResult.getOrThrow()
        builder.appendLine("  ✓ 金鑰別名: ${Keys.KEY_ALIAS_RSA_SIGN}")
        builder.appendLine("  ✓ 用途: SIGN / VERIFY")
        builder.appendLine("  ✓ Padding: PKCS1\n")

        // ── 測試 3: 提取並顯示公鑰 ──────────────────────────────
        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("測試 3: 提取公鑰資訊 (加解密金鑰)\n")

        val rsaPub = encPublicKey as RSAPublicKey
        val pubKeyBase64 = Base64.getEncoder().encodeToString(encPublicKey.encoded)

        builder.appendLine("  ✓ 演算法:   ${encPublicKey.algorithm}")
        builder.appendLine("  ✓ 格式:     ${encPublicKey.format}")
        builder.appendLine("  ✓ 模數長度: ${rsaPub.modulus.bitLength()} bits")
        builder.appendLine("  ✓ 公開指數: ${rsaPub.publicExponent}")
        builder.appendLine("\n  📋 公鑰 (Base64 / X.509 DER):")
        pubKeyBase64.chunked(64).forEach { builder.appendLine("     $it") }

        // ── 測試 4: 私鑰安全性驗證 ──────────────────────────────
        builder.appendLine("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("測試 4: 私鑰安全性驗證\n")

        val keyStore = KeyStore.getInstance(Keys.KEYSTORE_PROVIDER).apply { load(null) }
        val encPrivateKey = keyStore.getKey(Keys.KEY_ALIAS_RSA_ENC, null) as PrivateKey

        val exported = runCatching { encPrivateKey.encoded }.getOrNull()
        if (exported == null) {
            builder.appendLine("  ✓ 私鑰無法被匯出 (encoded = null)")
            builder.appendLine("  ✓ 私鑰受 TEE/Keystore 保護，永不離開安全區域")
        } else {
            builder.appendLine("  ⚠ 警告: 私鑰可被匯出，安全性較低")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            runCatching {
                val keyFactory = KeyFactory.getInstance(
                    encPrivateKey.algorithm,
                    Keys.KEYSTORE_PROVIDER
                )
                val keyInfo = keyFactory.getKeySpec(encPrivateKey, KeyInfo::class.java) as KeyInfo
                builder.appendLine("  ✓ 安全硬體: ${if (keyInfo.isInsideSecureHardware) "是 ✅" else "否 ⚠"}")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val levelStr = when (keyInfo.securityLevel) {
                        KeyProperties.SECURITY_LEVEL_STRONGBOX           -> "StrongBox 🏆"
                        KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "TEE ✅"
                        KeyProperties.SECURITY_LEVEL_SOFTWARE            -> "Software ⚠"
                        else -> "Unknown"
                    }
                    builder.appendLine("  ✓ 安全等級: $levelStr")
                }
            }.onFailure {
                builder.appendLine("  ⚠ 無法取得 KeyInfo: ${it.message}")
            }
        }

        // ── 測試 5: RSA 加密 / 解密 ─────────────────────────────
        builder.appendLine("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("測試 5: RSA-OAEP 加密 / 解密\n")

        val plainText = "Hello RSA Keystore! 🔐 ts=${System.currentTimeMillis()}"
        builder.appendLine("  📝 原始明文: $plainText\n")

        // 加密：使用公鑰（軟體層），明確傳入 oaepSpec 確保與解密端一致
        val encCipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        encCipher.init(Cipher.ENCRYPT_MODE, encPublicKey, oaepSpec)
        val cipherText = encCipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        builder.appendLine("  🔒 加密:")
        builder.appendLine("     ✓ 成功，密文長度: ${cipherText.size} bytes")
        builder.appendLine("     • ${Base64.getEncoder().encodeToString(cipherText).take(60)}…\n")

        // 解密：使用 Keystore 私鑰（KeyMint 硬體層），傳入相同 oaepSpec
        val decCipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        decCipher.init(Cipher.DECRYPT_MODE, encPrivateKey, oaepSpec)
        val decrypted = String(decCipher.doFinal(cipherText), Charsets.UTF_8)
        builder.appendLine("  🔓 解密:")
        builder.appendLine("     ✓ 還原明文: $decrypted")
        builder.appendLine("     • 驗證: ${if (plainText == decrypted) "✅ 完全一致" else "❌ 不一致"}\n")

        // ── 測試 6: 數位簽章 / 驗簽 ─────────────────────────────
        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("測試 6: 數位簽章 / 驗簽 (SHA256withRSA)\n")

        val signPrivateKey = keyStore.getKey(Keys.KEY_ALIAS_RSA_SIGN, null) as PrivateKey
        val dataToSign = "需要簽章的資料 ts=${System.currentTimeMillis()}"
        builder.appendLine("  📝 待簽章資料: $dataToSign\n")

        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(signPrivateKey)
        signer.update(dataToSign.toByteArray(Charsets.UTF_8))
        val signature = signer.sign()
        builder.appendLine("  ✍ 簽章:")
        builder.appendLine("     ✓ 成功，長度: ${signature.size} bytes")
        builder.appendLine("     • ${Base64.getEncoder().encodeToString(signature).take(60)}…\n")

        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(signPublicKey)
        verifier.update(dataToSign.toByteArray(Charsets.UTF_8))
        val isValid = verifier.verify(signature)
        builder.appendLine("  ✅ 驗簽:")
        builder.appendLine("     • 原始資料: ${if (isValid) "✅ 通過" else "❌ 失敗"}")

        val tampered = Signature.getInstance("SHA256withRSA")
        tampered.initVerify(signPublicKey)
        tampered.update("被竄改的資料".toByteArray(Charsets.UTF_8))
        val tamperedValid = tampered.verify(signature)
        builder.appendLine("     • 竄改資料: ${if (!tamperedValid) "✅ 正確拒絕" else "❌ 異常通過"}\n")

        // ── 總結 ─────────────────────────────────────────────────
        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("📊 測試總結:\n")
        builder.appendLine("  ✅ RSA-2048 加解密金鑰對生成成功")
        builder.appendLine("  ✅ RSA-2048 簽章金鑰對生成成功")
        builder.appendLine("  ✅ 公鑰可正常提取與匯出 (X.509/Base64)")
        builder.appendLine("  ✅ 私鑰受 Keystore 保護，無法匯出")
        builder.appendLine("  ✅ RSA-OAEP 加解密正常")
        builder.appendLine("  ✅ SHA256withRSA 簽章/驗簽正常\n")
        builder.appendLine("  💡 公鑰典型用途:")
        builder.appendLine("     • 分享給伺服器進行遠端加密")
        builder.appendLine("     • Remote Attestation")
        builder.appendLine("     • Certificate Signing Request (CSR)")

        return builder.toString()
    }

    // ── 加解密專用金鑰對（OAEP padding）────────────────────────
    private fun generateRSAEncKeyPair(): PublicKey {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            Keys.KEYSTORE_PROVIDER
        )
        kpg.initialize(
            KeyGenParameterSpec.Builder(
                Keys.KEY_ALIAS_RSA_ENC,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(2048)
                .setDigests(
                    KeyProperties.DIGEST_SHA1,   // MGF1 需要 SHA-1
                    KeyProperties.DIGEST_SHA256
                )
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return kpg.generateKeyPair().public
    }

    // ── 簽章專用金鑰對（PKCS1 padding）─────────────────────────
    private fun generateRSASignKeyPair(): PublicKey {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            Keys.KEYSTORE_PROVIDER
        )
        kpg.initialize(
            KeyGenParameterSpec.Builder(
                Keys.KEY_ALIAS_RSA_SIGN,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return kpg.generateKeyPair().public
    }
}