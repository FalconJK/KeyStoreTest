package com.falconjk.keystoretest

object Keys {
    const val KEYSTORE_PROVIDER     = "AndroidKeyStore"
    const val KEY_ALIAS_TEE         = "test_tee_key"
    const val KEY_ALIAS_STRONGBOX   = "test_strongbox_key"
    const val KEY_ALIAS_HUK_TEST    = "test_huk_key"
    const val KEY_ALIAS_LEVEL_TEST  = "test_level_key"
    const val KEY_ALIAS_RSA_ENC     = "test_rsa_enc_key"   // ← 加解密用
    const val KEY_ALIAS_RSA_SIGN    = "test_rsa_sign_key"  // ← 簽章用
}
