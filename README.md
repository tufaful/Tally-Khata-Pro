# TallyKhata Pro 📒✨

[![GitHub Repo](https://img.shields.io/badge/GitHub-Tally--Khata--Pro-181717.svg?style=flat&logo=github)](https://github.com/tufaful/Tally-Khata-Pro)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Storage-Room%20DB-009688.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Design](https://img.shields.io/badge/Style-Material%20Design%203-FF7043.svg?style=flat)](https://m3.material.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **TallyKhata Pro** is a modern, offline-first digital ledger, bookkeeping, and customer credit management Android application built with Kotlin and Jetpack Compose. Engineered for shopkeepers, small business owners, freelancers, and individuals seeking private, fast, and reliable financial accounting.

---

### 📥 Direct APK Download
> 🚀 **Ready-to-install APK included right in this repository!** You do not need Android Studio or any developer tools to run the app.  
> 👉 **[Download `tallykhata-debug.apk` (Latest Build)](tallykhata-debug.apk?raw=true)**  
> 
> Simply download the APK file directly to your Android device, tap to install, and start managing your ledgers immediately!

---

## 📌 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [How to Install & Run](#-how-to-install--run)
  - [Option 1: Direct APK Install (Instant - Recommended)](#-option-1-direct-apk-install-instant---recommended)
  - [Option 2: Build from Source (For Developers)](#-option-2-build-from-source-for-developers)
- [Tech Stack & Architecture](#-tech-stack--architecture)
- [Project Structure](#-project-structure)
- [Privacy & Security](#-privacy--security)
- [Contributing](#-contributing)
- [License](#-license)

---

## 📌 Overview
TallyKhata Pro replaces traditional paper ledgers with a lightning-fast, secure, and intuitive mobile experience. Built with an offline-first philosophy, all financial balances, transactions, customer records, and audit logs remain strictly confidential on your local device—no internet connection required.

### Highlights:
- **100% Offline & Private:** Zero third-party telemetry, ads, or unauthorized cloud tracking. Your business data stays strictly yours.
- **Accurate Balance Calculation:** Automatically computes real-time dues, receivables, and payables across all customers.
- **Voice Transaction Support:** Speak natural phrases to quickly populate amounts and transaction notes.
- **Digital Receipts:** Generate printable, professional transaction vouchers to share with customers via WhatsApp, Messenger, or SMS.
- **Built-in Recycle Bin:** Safely restore accidentally deleted customer accounts or transactions within 30 days.

---

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| **Multi-Khata Management** | Organize separate ledgers for multiple stores, wholesale businesses, or personal accounts. |
| **Customer Credit Ledger** | Keep detailed records of balances, contact numbers, transaction history, and custom notes. |
| **Smart Voice Entry** | Speech-to-text input to rapidly record sales, payments, and credit entries on the go. |
| **Digital Memo & Voucher** | One-tap digital receipt preview with instant sharing to messaging apps. |
| **Recycle Bin (Trash)** | Soft-delete protection allowing 30-day recovery of deleted customers and transactions. |
| **Audit Logs & History** | Comprehensive chronological timeline of all ledger edits and entry changes. |
| **Automated & Manual Backup** | Create timestamped local database backups with one-tap restore capability. |
| **PIN Lock Security** | Protect sensitive financial records behind a 4-digit master PIN. |
| **Material Design 3** | Modern UI supporting dynamic system color schemes, light mode, and dark mode. |

---

## 💻 How to Install & Run

### 📲 Option 1: Direct APK Install (Instant - Recommended)
A pre-built, ready-to-use debug APK is included directly in the root of this repository: **[`tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)**.

1. **Download the APK:**
   * On your Android phone, tap: **[Download `tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)**.
   * Or download it on your PC and transfer the file to your phone via USB or messaging.
2. **Install on Phone:**
   * Open your phone's **Files** or **Downloads** app and tap `tallykhata-debug.apk`.
   * If prompted by Android with *"Install unknown apps"*, tap **Settings** and grant permission for your browser or file manager.
   * Tap **Install**.
3. **Launch:**
   * Open **TallyKhata Pro** from your app drawer and enjoy!

---

### 🛠 Option 2: Build from Source (For Developers)

#### Prerequisites
- **Android Studio:** Ladybug (2024.2+) or newer
- **JDK:** OpenJDK 17 or 21
- **Android SDK:**
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `targetSdk`: 35 (Android 15)
  - `compileSdk`: 35

#### Steps to Build

1. **Clone the repository:**
   ```bash
   # HTTPS (Recommended)
   git clone https://github.com/tufaful/Tally-Khata-Pro.git

   # SSH
   git clone git@github.com:tufaful/Tally-Khata-Pro.git

   # GitHub CLI
   gh repo clone tufaful/Tally-Khata-Pro

   cd Tally-Khata-Pro
   ```

2. **Open in Android Studio:**
   * Select **File > Open** and choose the `Tally-Khata-Pro` directory.
   * Wait for Gradle to finish syncing project dependencies.

3. **Build the Debug APK:**
   ```bash
   gradle assembleDebug
   ```
   The compiled APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. **Run Unit Tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🛠 Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/) (Coroutines, StateFlow, Flow)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Clean Unidirectional Data Flow
- **Local Persistence:** [Room Database](https://developer.android.com/training/data-storage/room) (SQLite ORM) with DAOs and TypeConverters
- **Preferences:** [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for app preferences & security states
- **Asynchronous Processing:** Kotlin Coroutines (`viewModelScope`, Dispatchers.IO)
- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`)

---

## 📂 Project Structure

```text
app/src/main/java/com/example/tallykhata/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt          # Room database configuration
│   │   ├── KhataDao.kt              # Khata, Customer, Transaction & Trash DAOs
│   │   └── Converters.kt            # Type converters for dates and enums
│   ├── model/                      # Entities (Khata, Customer, Transaction, AuditLog, Trash)
│   ├── repository/                 # Repository layer isolating Room operations
│   └── preferences/                # User preferences (PIN, language, theme, onboarding)
├── ui/
│   ├── components/                 # Reusable buttons, cards, dialogs & app bars
│   ├── navigation/                 # Navigation destinations, routes & backstack
│   ├── screens/
│   │   ├── onboarding/             # Initial setup & preferences
│   │   ├── dashboard/              # Summary cards, net balance, khata overview
│   │   ├── customers/              # Customer listing, search, credit tracking
│   │   ├── transactions/           # Transaction creation, voice entry, receipt preview
│   │   ├── trash/                  # Recycle bin and data restoration
│   │   ├── audit/                  # Audit log and activity history
│   │   └── settings/               # PIN protection, backup & restore, theme
│   └── theme/                      # M3 ColorScheme, Typography, Shapes
└── viewmodel/                      # MainViewModel & business logic handlers
```

---

## 🔒 Privacy & Security

- **Local Storage Only:** Data is stored exclusively in a secure, local Room SQLite database on your device.
- **PIN Lock Protection:** Optional 4-digit master PIN lock prevents unauthorized app access when your phone is unlocked.
- **Zero Third-Party Trackers:** No analytics SDKs, ad networks, or external telemetry libraries.

---

## 🤝 Contributing

Contributions, feature suggestions, and bug reports are welcome!
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <b>TallyKhata Pro</b> • Built with Kotlin & Jetpack Compose
</p>
