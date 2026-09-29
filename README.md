# 🎓 One STI • Native Android App (v2.0)
**Crafted with ❤️ by MJ**

[![Build and Release One STI APK](https://github.com/macjamesrequillas10/One-STI/actions/workflows/build-apk.yml/badge.svg)](https://github.com/macjamesrequillas10/One-STI/actions)
[![Latest Release](https://img.shields.io/badge/Release-v2.0-0B5592?style=flat&logo=android)](releases)
[![Refresh Rate](https://img.shields.io/badge/Display-144Hz%20Ultra--Smooth-FFC220?style=flat&logo=speedtest)](https://github.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A high-performance **Native Android App** for STI College students, faculty, and alumni. Features a complete native student suite (Virtual Student ID, Grades & GWA, Class Schedule, Student Ledger), permanent multi-account session vaulting, 144Hz high refresh rate hardware acceleration, and integrated live campus portals (One STI, STI ELMS Canvas, Microsoft 365, and Student Services).

---

## 📥 Download APK v2.0 directly from this Repository

Download the ready-to-install Android APK (v2.0) using any of the options below:

### Option 1: Direct File Download in Repository
- 📱 **[Download One_STI.apk (v2.0)](apk/One_STI.apk?raw=true)**
- 📱 **[Alternative: ONE_STI_Made_by_MJ.apk (v2.0)](apk/ONE_STI_Made_by_MJ.apk?raw=true)**

### Option 2: GitHub Releases
- Head over to the [**Releases**](releases) tab of this GitHub repository to download the latest signed APK.

### Option 3: GitHub Actions Artifacts
- Check the [**Actions**](actions) tab on every commit to download automated test-verified build artifacts.

---

## 📲 How to Install on Your Android Phone

1. Download **`One_STI.apk`** from the link above to your phone.
2. Open the downloaded APK from your **Downloads** folder or notification tray.
3. If Android prompts *"Install unknown apps"*, tap **Settings** and enable **"Allow from this source"**.
4. Tap **Install** and launch **One STI**!

---

## ✨ Key Features & Capabilities

### 📱 1. Complete Native Student Suite
- 🏠 **Native Home / Dashboard**: Interactive digital **Virtual Student ID Card** with student barcode, today's schedule preview, campus announcements, and quick services.
- 📊 **Grades & Evaluations**: Real-time **GWA / GPA calculation**, term filters (1st Term, 2nd Term, Summer), individual subject breakdowns (Prelim, Midterm, Pre-Final, Final), and academic standing status.
- 📅 **Class Schedule**: Day-by-day timetable (Monday to Saturday) with room/laboratory, instructor, section, and enrolled units.
- 💳 **Student Ledger**: Tuition assessment summary, remaining balance, installment due dates (Downpayment, Prelims, Midterms, Pre-Finals, Finals), and accredited payment channels (GCash, Maya, BDO, UnionBank, Cashier).
- 👤 **Student Profile & Settings**: Student number, enrolled program, campus, 144Hz mode status, and notification controls.

### ⚡ 2. 144Hz & 120Hz Ultra-Smooth Engine
- Native hardware refresh rate mode selection up to **144Hz, 120Hz, and 90Hz** with minimal post-processing latency.
- GPU tile pre-rasterization and direct hardware compositing for stutter-free scrolling.

### 🔒 3. Universal Keep-Signed-In Session Vault
- **Accounts Never Logout**: Automatically transforms session cookies into persistent SQLite records with **10-year expiration dates**.
- **Native Token Vaulting (`StiSessionBridge`)**: Mirrors `sessionStorage` and `localStorage` authentication tokens (MSAL, ADAL, OAuth) into native Android storage, restoring them across app restarts.
- **Anti-Inactivity Heartbeat**: Automated background pings and idle resets prevent university portal session timeouts.
- **Auto-KMSI Confirmation**: Automatically confirms Microsoft's "Stay signed in?" prompt to acquire persistent Primary Refresh Tokens.

### 🔔 4. Real-Time Grade Release Notifications
- Automatically monitors grade postings and triggers high-priority local push notifications with deep links directly to the grades screen.

### 🌐 5. Integrated Live Campus Portals Hub
- Instant 1-tap switching between:
  - 🏫 **One STI Student Portal** (`one.sti.edu`)
  - 📖 **STI ELMS Canvas** (`elms.sti.edu`)
  - 💼 **Microsoft 365 / Outlook** (`portal.office.com`)
  - 📋 **Student Services System** (`sts.sti.edu`)

### 🖤 6. Sleek Black Top & Bottom System Bars
- Solid black status bar and bottom navigation bar with crisp white icons and edge-to-edge Material 3 layout.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 100%
- **UI Toolkit:** Jetpack Compose & Material Design 3 (M3)
- **Architecture:** Clean MVVM + Repository Pattern
- **Navigation:** Jetpack Navigation Compose with type-safe route keys
- **Local Persistence:** Android SharedPreferences + SQLite Persistent Cookie Jar
- **Testing:** Local JVM Robolectric Test Suite
- **CI/CD:** GitHub Actions Automated APK Builder & Release Publisher

---

## 💻 Building from Source

```bash
# Clone the repository
git clone https://github.com/macjamesrequillas10/One-STI.git
cd One-STI

# Run Robolectric Unit Tests
gradle testDebugUnitTest

# Build the Debug APK
gradle assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 👨‍💻 Developer & Credits

- **Developer:** MJ (Mac James)
- **Application:** One STI Native Android Edition
- **Version:** v2.0
