# 🔐 Android Keystore Security Testing Tool

A comprehensive Android Keystore testing application for detecting and verifying device hardware security features, including TEE, StrongBox, and HUK.

## 📱 Features

### Core Testing Functions

1. **Device Information Display** 📱
   - Display device manufacturer, model, and brand
   - Show Android version and API level
   - Display security patch version
   - Predict security feature support

2. **Security Level Detection** 🛡️
   - Automatically detect the highest security level supported
   - Prioritize StrongBox, fallback to TEE
   - Display actual security level in use
   - Android 12+ supports detailed level information

3. **TEE (Trusted Execution Environment) Test** 🔒
   - Test TrustZone/SGX support
   - Verify keys are generated in secure hardware
   - Check TEE environment availability
   - Display security guarantee information

4. **StrongBox Test** 🏆
   - Test independent security chip support (e.g., Titan M/M2)
   - Verify highest-level hardware security module
   - Detect physically isolated security elements
   - Requires Android 9.0+ (API 28+)

5. **HUK (Hardware Unique Key) Test** 🔑
   - Test hardware unique key support
   - Verify device binding functionality
   - Check key derivation chain
   - Analyze HUK protection mechanism

6. **HUK Explanation** 📚
   - Detailed explanation of HUK concepts and architecture
   - Explain key derivation chain
   - Introduce different vendor implementations
   - Provide security guarantee explanations

7. **Encryption/Decryption Test** 🔐
   - Use AES-256-GCM encryption algorithm
   - Verify encryption/decryption integrity
   - Test actual key operation
   - Display detailed encryption process information

8. **Key Information Check** 🔍
   - List all generated test keys
   - Display detailed information for each key
   - Check security level and attributes
   - Analyze HUK protection status

9. **Key Management** 🗑️
   - Clear all test keys
   - Reset test environment
   - Secure deletion function

## 🏗️ Project Structure

```
com.falconjk.keystoretest/
├── MainActivity.kt              # Main activity, UI control
├── DeviceInfoTester.kt          # Device information detection
├── SecureLevelTester.kt         # Comprehensive security level test
├── TEETester.kt                 # TEE functionality test
├── StrongBoxTester.kt           # StrongBox functionality test
├── HUKTester.kt                 # HUK functionality test
├── HUKExplainer.kt              # HUK principle explanation
├── EncryptionTester.kt          # Encryption/decryption test
├── KeyInfoChecker.kt            # Key information checker
└── KeyManager.kt                # Key management utility
```

## 🔧 Technical Details

### Android APIs Used

- **Android Keystore System**
  - `KeyStore` - Key storage management
  - `KeyGenerator` - Key generation
  - `KeyGenParameterSpec` - Key parameter configuration
  - `KeyInfo` - Key information query

- **Encryption Algorithms**
  - AES-256-GCM (Symmetric encryption)
  - No padding mode (NoPadding)
  - 128-bit GCM tag

- **Security Levels** (Android 12+)
  - `SECURITY_LEVEL_STRONGBOX` - StrongBox level
  - `SECURITY_LEVEL_TRUSTED_ENVIRONMENT` - TEE level
  - `SECURITY_LEVEL_SOFTWARE` - Software level

### Key Aliases

```kotlin
test_tee_key              // TEE test key
test_strongbox_key        // StrongBox test key
test_huk_verification_key // HUK verification key
test_secure_level_key     // Security level test key
```

## 📋 System Requirements

- **Minimum Version**: Android 6.0 (API 23)
- **Recommended Version**: Android 12+ (API 31+) for full functionality
- **StrongBox Feature**: Requires Android 9.0+ (API 28+)
- **Detailed Security Level**: Requires Android 12+ (API 31+)

## 🚀 Usage

### Recommended Testing Order

1. **Display Device Information** - Understand basic device status
2. **Detect Highest Security Level** - Confirm the highest security level supported
3. **Test TEE** - Verify basic hardware security support
4. **Test StrongBox** - Check for independent security chip
5. **Test HUK** - Verify hardware unique key functionality
6. **Test Encryption/Decryption** - Verify actual encryption operation
7. **Check All Key Information** - View detailed key attributes
8. **HUK Explanation** - Deep dive into security architecture

### Test Result Interpretation

#### ✅ Ideal Result (Flagship Devices)
```
✓ TEE Supported
✓ StrongBox Supported
✓ HUK Supported
✓ Security Level: StrongBox
```

#### ✅ Good Result (Mainstream Devices)
```
✓ TEE Supported
✗ StrongBox Not Supported
✓ HUK Supported
✓ Security Level: TEE
```

#### ⚠️ Basic Result (Entry-level Devices)
```
✗ TEE Not Supported
✗ StrongBox Not Supported
✗ HUK Not Supported
✗ Security Level: Software
```

## 🔒 Security Architecture

### Key Derivation Chain

```
HUK (eFuse/OTP, Hardware Layer)
  ↓ HKDF/KDF Derivation
Device Root Key
  ↓ Derivation
Keymaster Master Key
  ↓ Derivation
App-specific KEK
  ↓ Encryption Protection
Application Key (Encrypted Storage)
```

### Android Keystore Architecture

```
Application Layer (App)
  ↓ Android Keystore API
Framework Layer
  ↓ Binder IPC
Keystore Daemon
  ↓ HIDL/AIDL
Keymaster HAL
  ↓
TEE/StrongBox
  ├─ Keymaster TA
  └─ HUK (eFuse)
```

## 🏭 Vendor Implementations

### Qualcomm
- **TEE**: QSEE (Qualcomm Secure Execution Environment)
- **HUK Storage**: QFPROM (eFuse)
- **StrongBox**: Supported on some high-end chips

### MediaTek
- **TEE**: Trustonic TEE or OP-TEE
- **HUK Storage**: eFuse
- **StrongBox**: Supported on some flagship chips

### Samsung Exynos
- **TEE**: TEEGRIS
- **HUK Storage**: eFuse
- **StrongBox**: Supported on high-end models

### Google Tensor
- **TEE**: ARM TrustZone
- **StrongBox**: Titan M2 security chip
- **HUK Storage**: Integrated in Titan M2

## ⚠️ Important Notes

### Keys Cannot Be Backed Up
- Keys are bound to device hardware
- Cannot be exported or backed up to cloud
- Factory reset permanently deletes keys

### Device Replacement Impact
- Keys cannot be transferred to new devices
- Need to regenerate keys
- Data encrypted on old device cannot be decrypted on new device

### System Updates
- Normal system updates do not affect keys
- Factory reset deletes all keys
- Recommend backing up important data (not keys) before updates

## 🛠️ Development Information

### Build Environment
- Kotlin
- Android Gradle Plugin
- Material Components
- AndroidX

### Permission Requirements
No special permissions required (only uses Android Keystore API)

### Compatibility Testing
Tested on the following devices:
- Google Pixel series
- Samsung Galaxy series
- Other mainstream Android devices

## 📚 References

- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [Hardware-backed Keystore](https://source.android.com/security/keystore)
- [StrongBox Keymaster](https://developer.android.com/training/articles/keystore#HardwareSecurityModule)
- [ARM TrustZone](https://www.arm.com/technologies/trustzone-for-cortex-a)

## 📄 License

This project is for learning and testing purposes only.

## 👨‍💻 Author

FalconJK

## 🤝 Contributing

Issues and Pull Requests are welcome!

## 📞 Contact

For questions or suggestions, please contact via GitHub Issues.

---

**⚡ Tip**: Test on different devices to understand security implementation differences across vendors!