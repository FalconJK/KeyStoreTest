package com.falconjk.keystoretest.test

import com.falconjk.keystoretest.Keys
import java.security.KeyStore

class KeyManager {

    fun clearAllKeys(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║         清除所有測試密鑰              ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        try {
            val keyStore = KeyStore.getInstance(Keys.KEYSTORE_PROVIDER)
            keyStore.load(null)

            val allAliases = keyStore.aliases().toList()
            val testAliases = allAliases.filter {
                it.startsWith("test_")
            }

            if (testAliases.isEmpty()) {
                builder.appendLine("📭 沒有找到測試密鑰")
                return builder.toString()
            }

            builder.appendLine("找到 ${testAliases.size} 個測試密鑰:\n")

            for (alias in testAliases) {
                keyStore.deleteEntry(alias)
                builder.appendLine("  ✓ 已刪除: $alias")
            }

            builder.appendLine("\n✅ 清除完成")
            builder.appendLine("共刪除 ${testAliases.size} 個測試密鑰")

        } catch (e: Exception) {
            builder.appendLine("❌ 錯誤: ${e.message}")
            e.printStackTrace()
        }

        return builder.toString()
    }
}