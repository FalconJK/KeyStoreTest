package com.falconjk.keystoretest

import android.os.Build

class DeviceInfoTester {

    fun getDeviceInfo(): String {
        val builder = StringBuilder()

        builder.appendLine("╔═══════════════════════════════════════╗")
        builder.appendLine("║        裝置與系統資訊                    ║")
        builder.appendLine("╚═══════════════════════════════════════╝\n")

        builder.appendLine("📱 裝置資訊:")
        builder.appendLine("  • 製造商: ${Build.MANUFACTURER}")
        builder.appendLine("  • 品牌: ${Build.BRAND}")
        builder.appendLine("  • 型號: ${Build.MODEL}")
        builder.appendLine("  • 產品名稱: ${Build.PRODUCT}")
        builder.appendLine("  • 硬體: ${Build.HARDWARE}")

        builder.appendLine("\n🤖 系統資訊:")
        builder.appendLine("  • Android 版本: ${Build.VERSION.RELEASE}")
        builder.appendLine("  • API 等級: ${Build.VERSION.SDK_INT}")
        builder.appendLine("  • 安全補丁: ${Build.VERSION.SECURITY_PATCH}")

        builder.appendLine("\n🔐 安全功能支援:")
        builder.appendLine("  • Keystore: ✓ (所有裝置)")
        builder.appendLine("  • TEE/TrustZone: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) "✓ (API 23+)" else "✗"}")
        builder.appendLine("  • StrongBox: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "? (需測試)" else "✗ (需 API 28+)"}")
        builder.appendLine("  • HUK: ${if (isLikelyHasHUK()) "✓ (可能支援)" else "? (需測試)"}")

        builder.appendLine("\n💡 提示:")
        builder.appendLine("  請繼續執行其他測試以確認實際支援情況")

        return builder.toString()
    }

    private fun isLikelyHasHUK(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (Build.MANUFACTURER.lowercase().contains("qualcomm") ||
                        Build.MANUFACTURER.lowercase().contains("mediatek") ||
                        Build.MANUFACTURER.lowercase().contains("samsung") ||
                        Build.MANUFACTURER.lowercase().contains("google") ||
                        Build.HARDWARE.lowercase().contains("qcom") ||
                        Build.HARDWARE.lowercase().contains("mt"))
    }
}