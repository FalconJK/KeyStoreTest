package com.falconjk.keystoretest

import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.falconjk.keystoretest.databinding.ActivityMainBinding
import com.falconjk.keystoretest.test.AsymmetricTester
import com.falconjk.keystoretest.test.DeviceInfoTester
import com.falconjk.keystoretest.test.EncryptionTester
import com.falconjk.keystoretest.test.HUKExplainer
import com.falconjk.keystoretest.test.HUKTester
import com.falconjk.keystoretest.test.KeyInfoChecker
import com.falconjk.keystoretest.test.KeyManager
import com.falconjk.keystoretest.test.SecureLevelTester
import com.falconjk.keystoretest.test.StrongBoxTester
import com.falconjk.keystoretest.test.TEETester

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 測試模組
    private val deviceInfoTester = DeviceInfoTester()
    private val teeTester = TEETester()
    private val strongBoxTester = StrongBoxTester()
    private val hukTester = HUKTester()
    private val encryptionTester = EncryptionTester()
    private val keyInfoChecker = KeyInfoChecker()
    private val hukExplainer = HUKExplainer()
    private val keyManager = KeyManager()
    private val secureLevelTester = SecureLevelTester()
    private val asymmetricTester = AsymmetricTester()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 UI
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnDeviceInfo.setOnClickListener {
            showResult(deviceInfoTester.getDeviceInfo())
        }
        binding.btnDeviceInfo.setOnClickListener {
            showResult(deviceInfoTester.getDeviceInfo())
        }

        binding.btnTestTEE.setOnClickListener {
            showResult(teeTester.testTEE())
        }

        binding.btnTestStrongBox.setOnClickListener {
            showResult(strongBoxTester.testStrongBox())
        }

        binding.btnTestHUK.setOnClickListener {
            showResult(hukTester.testHUK())
        }

        binding.btnTestEncryption.setOnClickListener {
            showResult(encryptionTester.testEncryption())
        }

        binding.btnCheckAllKeys.setOnClickListener {
            showResult(keyInfoChecker.checkAllKeysInfo())
        }

        binding.btnExplainHUK.setOnClickListener {
            showResult(hukExplainer.getExplanation())
        }

        binding.btnClearKeys.setOnClickListener {
            showResult(keyManager.clearAllKeys())
        }

        binding.btnSecureLevelTest.setOnClickListener {
            showResult(secureLevelTester.testSecureLevel())
        }

        binding.btnTestAsymmetric.setOnClickListener {
            showResult(asymmetricTester.testAsymmetric())
        }
    }

    /**
     * 清空並顯示新結果
     */
    private fun showResult(text: String) {
        runOnUiThread {
            binding.resultTextView.text = text
            scrollToBottom()
        }
    }

    /**
     * 追加結果到現有內容
     */
    private fun appendResult(text: String) {
        runOnUiThread {
            val currentText = binding.resultTextView.text.toString()
            binding.resultTextView.text = if (currentText.isEmpty()) {
                text
            } else {
                "$currentText\n$text"
            }
            scrollToBottom()
        }
    }

    /**
     * 滾動到底部
     */
    private fun scrollToBottom() {
        binding.resultTextView.post {
            binding.resultScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}