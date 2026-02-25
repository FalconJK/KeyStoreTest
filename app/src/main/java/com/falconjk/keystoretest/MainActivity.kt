package com.falconjk.keystoretest

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var resultScrollView: ScrollView

    // 測試模組
    private val deviceInfoTester = DeviceInfoTester()
    private val teeTester = TEETester()
    private val strongBoxTester = StrongBoxTester()
    private val hukTester = HUKTester()
    private val encryptionTester = EncryptionTester()
    private val keyInfoChecker = KeyInfoChecker()
    private val hukExplainer = HUKExplainer()
    private val keyManager = KeyManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 UI
        initViews()
        setupListeners()
    }

    private fun initViews() {
        resultTextView = findViewById(R.id.resultTextView)
        resultScrollView = findViewById(R.id.resultScrollView)
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.btnDeviceInfo).setOnClickListener {
            showResult(deviceInfoTester.getDeviceInfo())
        }

        findViewById<MaterialButton>(R.id.btnTestTEE).setOnClickListener {
            appendResult(teeTester.testTEE())
        }

        findViewById<MaterialButton>(R.id.btnTestStrongBox).setOnClickListener {
            appendResult(strongBoxTester.testStrongBox())
        }

        findViewById<MaterialButton>(R.id.btnTestHUK).setOnClickListener {
            appendResult(hukTester.testHUK())
        }

        findViewById<MaterialButton>(R.id.btnTestEncryption).setOnClickListener {
            appendResult(encryptionTester.testEncryption())
        }

        findViewById<MaterialButton>(R.id.btnCheckAllKeys).setOnClickListener {
            appendResult(keyInfoChecker.checkAllKeysInfo())
        }

        findViewById<MaterialButton>(R.id.btnExplainHUK).setOnClickListener {
            showResult(hukExplainer.getExplanation())
        }

        findViewById<MaterialButton>(R.id.btnClearKeys).setOnClickListener {
            appendResult(keyManager.clearAllKeys())
        }
    }

    /**
     * 清空並顯示新結果
     */
    private fun showResult(text: String) {
        runOnUiThread {
            resultTextView.text = text
            scrollToBottom()
        }
    }

    /**
     * 追加結果到現有內容
     */
    private fun appendResult(text: String) {
        runOnUiThread {
//            val currentText = resultTextView.text.toString()
//            resultTextView.text = if (currentText.isEmpty()) {
//                text
//            } else {
//                "$currentText\n$text"
//            }
            resultTextView.text = text
            scrollToBottom()
        }
    }

    /**
     * 滾動到底部
     */
    private fun scrollToBottom() {
        resultTextView.post {
            resultScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}