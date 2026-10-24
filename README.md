# TallyKhata Pro (টালি খাতা প্রো) 📒✨

[![GitHub Repo](https://img.shields.io/badge/GitHub-Tally--Khata--Pro-181717.svg?style=flat&logo=github)](https://github.com/tufaful/Tally-Khata-Pro)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Storage-Room%20DB-009688.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Design](https://img.shields.io/badge/Style-Material%20Design%203-FF7043.svg?style=flat)](https://m3.material.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **TallyKhata Pro** is a modern, offline-first digital ledger, bookkeeping, and customer credit management Android app built with Kotlin and Jetpack Compose. Designed for shopkeepers, small business owners, freelancers, and individuals in Bangladesh and across the globe.
> 
> **টালি খাতা প্রো** হলো আধুনিক, ১০০% অফলাইন ও সম্পূর্ণ নিরাপদ ডিজিটাল হিসাব খাতা অ্যান্ড্রয়েড অ্যাপ। ছোট-বড় দোকানদার, ব্যবসায়ী ও ব্যক্তিগত হিসাব সহজে পরিচালনা করার জন্য এটি তৈরি করা হয়েছে।

---

### 📥 Direct APK Download / সরাসরি এপিকে ডাউনলোড
> 🚀 **Ready-to-install APK included in this repository!** You don't need Android Studio or Gradle to run the app.  
> 👉 **[Download `tallykhata-debug.apk` (Latest Build)](tallykhata-debug.apk?raw=true)**  
> 
> 📲 **কোনো কোডিং বা অ্যান্ড্রয়েড স্টুডিও ছাড়াই সরাসরি মোবাইলে চালান!**  
> রিপোজিটরির রুট ফোল্ডারেই প্রস্তুতকৃত ফাইল যুক্ত রয়েছে: **[`tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)** ডাউনলোড করে মোবাইলে ওপেন করলেই ইনস্টল হয়ে যাবে।

---

## 🌐 Language Navigation / ভাষা নির্বাচন
- [English Documentation](#-english-documentation)
  - [Overview](#-overview)
  - [Direct APK Install](#-option-1-direct-apk-install-instant---no-dev-tools-needed)
  - [Key Features](#-key-features)
  - [Tech Stack & Architecture](#-tech-stack--architecture)
  - [Project Structure](#-project-structure)
  - [Build from Source](#-option-2-build-from-source-for-developers)
  - [License](#-license)
- [বাংলা বিবরণ ও নির্দেশিকা](#-বাংলা-বিবরণ-ও-নির্দেশিকা)
  - [সংক্ষিপ্ত পরিচিতি](#-সংক্ষিপ্ত-পরিচিতি)
  - [সরাসরি APK ইনস্টল নির্দেশিকা](#-বিকল্প-১-সরাসরি-apk-ডাউনলোড-ও-ইনস্টল-সবচেয়ে-সহজ)
  - [মূল সুবিধাসমূহ](#-মূল-সুবিধাসমূহ)
  - [ব্যবহৃত প্রযুক্তি](#-ব্যবহৃত-প্রযুক্তি)
  - [সোর্স কোড থেকে বিল্ড](#-বিকল্প-২-সোর্স-কোড-থেকে-বিল্ড-ডেভেলপারদের-জন্য)
  - [লাইসেন্স ও স্বত্ব](#-লাইসেন্স-ও-স্বত্ব)

---

# 🇬🇧 English Documentation

## 📌 Overview
TallyKhata Pro replaces traditional paper ledgers (খাতা) with a lightning-fast, secure, and beautiful Android experience. With an offline-first philosophy, your business data stays strictly confidential and accessible anytime, anywhere—no internet connection required.

### Why TallyKhata Pro?
- **100% Offline & Private:** Zero unauthorized data tracking. All customer balances and financial records live on your device.
- **Bilingual Interface:** Seamlessly switch between Bengali (বাংলা) and English.
- **Accurate Balance Calculation:** Automatically distinguishes between Receivable (পাওনা / বাকি) and Payable (দেনা / অগ্রিম).
- **Voice Transaction Support:** Speak in natural phrases to quickly populate transaction details.
- **Digital Receipts:** Generate printable, professional voucher slips to share with customers via WhatsApp, Messenger, or SMS.

---

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| **Multi-Khata Management** | Organize multiple shops, businesses, or personal ledgers in separate khatas. |
| **Customer Credit Ledger** | Keep detailed records of customer dues, payments, contact numbers, and notes. |
| **Smart Voice Entry** | Speech-to-text transaction input to log sales and payments on the go. |
| **Digital Memo / Receipt** | One-tap digital receipt preview with instant sharing capability. |
| **Recycle Bin (Trash)** | Accidental deletions can be restored within 30 days from the trash screen. |
| **Audit Logs & History** | Comprehensive timeline of all ledger edits and financial entries. |
| **Automated & Manual Backup** | Create timestamped local database backups and easily restore anytime. |
| **PIN Lock Security** | Keep sensitive financial numbers safe with a 4-digit master PIN. |
| **Material Design 3** | Elegant theme supporting dynamic system colors, light mode, and dark mode. |

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
│   │   └── settings/               # PIN protection, backup & restore, language/theme
│   └── theme/                      # M3 ColorScheme, Typography, Shapes
└── viewmodel/                      # MainViewModel & specialized ViewModels
```

---

## 💻 How to Install & Run

### 📲 Option 1: Direct APK Install (Instant - No Dev Tools Needed)
A pre-built, ready-to-use debug APK is included directly in the root of this repository: **[`tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)**.

1. **Download the APK:**
   * On your Android phone, tap: **[Download `tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)**.
   * Or download it on your PC and transfer it to your phone via USB/WhatsApp/Bluetooth.
2. **Install on Phone:**
   * Open your phone's **Files** or **Downloads** app and tap `tallykhata-debug.apk`.
   * If prompted by Android with *"Install unknown apps"*, tap **Settings** and allow permission for your browser or file manager.
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
   Select **File > Open** and select the `Tally-Khata-Pro` folder. Allow Gradle to sync.

3. **Build the Debug APK:**
   ```bash
   gradle assembleDebug
   ```
   The compiled APK will be created at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. **Run Unit Tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

# 🇧🇩 বাংলা বিবরণ ও নির্দেশিকা

## 📖 সংক্ষিপ্ত পরিচিতি
**টালি খাতা প্রো (TallyKhata Pro)** হলো আধুনিক ও নির্ভরযোগ্য ডিজিটাল হিসাব খাতা। এটি ব্যবসা বা ব্যক্তিগত জীবনের বাকি, জমা, দেনা ও পাওনার হিসাব অত্যন্ত সহজ ও নির্ভুলভাবে পরিচালনা করতে সাহায্য করে। কোনো ইন্টারনেট সংযোগ ছাড়াই এটি সম্পূর্ণ অফলাইনে কাজ করে, ফলে আপনার আর্থিক তথ্যের গোপনীয়তা থাকে ১০০% সুরক্ষিত।

---

## 🌟 মূল সুবিধাসমূহ

- 🏢 **মাল্টি-খাতা সাপোর্ট:** আপনার দোকান, পাইকারি ব্যবসা অথবা ব্যক্তিগত খরচের জন্য পৃথক খাতা তৈরি ও পরিচালনা।
- 👥 **কাস্টমার বাকি-জমা খাতা:** কার কাছে কত টাকা পাওনা বা কাকে কত পরিশোধ করতে হবে তা স্বয়ংক্রিয়ভাবে হিসাব করা।
- 🎙️ **ভয়েস লেনদেন এন্ট্রি:** মুখে বাংলায় বা ইংরেজিতে বলে দ্রুত ট্রানজাকশন ইনপুট করার সুবিধা।
- 🧾 **ডিজিটাল মেমো ও রশিদ:** প্রতিটি লেনদেনের পর আকর্ষণীয় ডিজিটাল ভাউচার তৈরি করে সরাসরি হোয়াটসঅ্যাপ বা মেসেঞ্জারে কাস্টমারকে শেয়ার করা।
- 🗑️ **রিসাইকেল বিন (ট্র্যাশ):** ভুলবশত কোনো কাস্টমার বা লেনদেন ডিলিট হয়ে গেলেও ৩০ দিনের মধ্যে তা পুনরায় রিস্টোর করার সুবিধা।
- 📜 **অডিট হিস্ট্রি:** কবে, কখন কোন হিসাব পরিবর্তন বা এন্ট্রি করা হয়েছে তার পূর্ণাঙ্গ সময়ানুক্রমিক লগ।
- 💾 **অটো ও ম্যানুয়াল ব্যাকআপ:** এক ক্লিকে সম্পূর্ণ ডাটাবেজের ব্যাকআপ ফাইল তৈরি এবং ফোন বদল করলেও রিস্টোর করার সুবিধা।
- 🔒 **পিন লক নিরাপত্তা:** ৪ ডিজিটের মাস্টার পিন দিয়ে অ্যাপ লক করে অনাকাঙ্ক্ষিত প্রবেশ রোধ।
- 🎨 **ম্যাটেরিয়াল ৩ ডিজাইন:** আধুনিক ইন্টারফেস, বাংলা ফন্ট সাপোর্ট এবং চোখবান্ধব ডার্ক মোড।

---

## ⚙️ ব্যবহৃত প্রযুক্তি

- **প্রোগ্রামিং ভাষা:** [Kotlin](https://kotlinlang.org/) (Coroutines, StateFlow)
- **ইউজার ইন্টারফেস:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
- **ডাটাবেজ:** [Room Database](https://developer.android.com/training/data-storage/room) (SQLite ORM)
- **সেটিংস স্টোরেজ:** [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **আর্কিটেকচার:** MVVM (Model-View-ViewModel)

---

## 🔨 কিভাবে অ্যাপ ইনস্টল ও ব্যবহার করবেন

### 📱 বিকল্প ১: সরাসরি APK ডাউনলোড ও ইনস্টল (সবচেয়ে সহজ)
এই রিপোজিটরির রুট ফোল্ডারেই সম্পূর্ণ তৈরি করা ইনস্টলেশন ফাইল রাখা আছে: **[`tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)**। কোনো অ্যান্ড্রয়েড স্টুডিও বা টেকনিক্যাল সেটআপ ছাড়াই আপনি এটি সরাসরি ফোনে চালাতে পারবেন।

1. **ফাইলটি ডাউনলোড করুন:**
   * মোবাইল থেকে সরাসরি ডাউনলোড করতে ট্যাপ করুন: **[Download `tallykhata-debug.apk`](tallykhata-debug.apk?raw=true)**।
   * অথবা কম্পিউটারে ডাউনলোড করে ক্যাবল/শেয়ারইট/হোয়াটসঅ্যাপের মাধ্যমে ফোনে পাঠিয়ে নিন।
2. **ফোনে ইনস্টল করুন:**
   * ফোনের **Files** বা **Downloads** ফোল্ডারে গিয়ে `tallykhata-debug.apk` ফাইলে ক্লিক করুন।
   * ফোনে যদি *"Install unknown apps"* বা অজানা উৎস থেকে অ্যাপ ইনস্টলের অনুমতি চায়, তবে সেটিংসে গিয়ে অনুমতি দিন।
   * **Install** বাটনে ট্যাপ করলেই অ্যাপটি সফলভাবে ইনস্টল হয়ে যাবে!
3. **ব্যবহার শুরু করুন:**
   * অ্যাপ ড্রয়ার থেকে **টালি খাতা প্রো** ওপেন করুন এবং সম্পূর্ণ অফলাইনে নিরাপদ হিসাব রাখা শুরু করুন।

---

### 💻 বিকল্প ২: সোর্স কোড থেকে বিল্ড (ডেভেলপারদের জন্য)

#### প্রয়োজনীয় রিকোয়ারমেন্টস
- অ্যান্ড্রয়েড স্টুডিও (Android Studio Ladybug বা লেটেস্ট ভার্সন)
- জাভা JDK ১৭ বা ২১
- Android SDK API লেভেল ২৬ থেকে ৩৫

#### যেভাবে বিল্ড করবেন
```bash
# প্রজেক্ট ক্লোন করুন (HTTPS - প্রস্তাবিত)
git clone https://github.com/tufaful/Tally-Khata-Pro.git

# অথবা SSH দিয়ে ক্লোন করতে চাইলে
git clone git@github.com:tufaful/Tally-Khata-Pro.git

# প্রজেক্ট ফোল্ডারে প্রবেশ করুন
cd Tally-Khata-Pro

# ডিবাগ APK তৈরি করুন
gradle assembleDebug
```
বিল্ড সম্পন্ন হলে `app/build/outputs/apk/debug/app-debug.apk` ফোল্ডারে নতুন কম্পাইল করা APK ফাইলটি পাওয়া যাবে।

---

## 📄 License / লাইসেন্স ও স্বত্ব

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for full details.

এই প্রজেক্টটি **MIT লাইসেন্স**-এর আওতাভুক্ত। বিস্তারিত জানতে [LICENSE](LICENSE) ফাইলটি দেখুন।

---

<p align="center">
  Crafted with ❤️ for Bangladeshi shopkeepers & global ledger managers.<br/>
  <b>Made with Kotlin & Jetpack Compose</b>
</p>
