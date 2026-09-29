# 🎓 One STI • Student Portal Android App
**Made by MJ**

[![Build and Release One STI APK](https://github.com/macjamesrequillas10/One-STI/actions/workflows/build-apk.yml/badge.svg)](https://github.com/macjamesrequillas10/One-STI/actions)
[![Display](https://img.shields.io/badge/Display-144Hz%20Ultra--Smooth-FFC220?style=flat&logo=speedtest)](https://github.com)
[![Status Bar](https://img.shields.io/badge/Top%20Bar-Solid%20Black-000000?style=flat)](https://github.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An optimized Android application exclusively for the **One STI Student Portal** (`https://one.sti.edu`). Engineered with a sleek solid black status bar, permanent multi-account session vaulting (all accounts never log out), native 144Hz display mode, and real-time push notifications when professors post grades.

---

## 📥 Download One STI APK directly from this GitHub Repository

Download the ready-to-install Android APK using any of the options below:

### Option 1: Direct File Download in Repository
- 📱 **[Download One_STI.apk (From apk/ folder)](apk/One_STI.apk?raw=true)**
- 📱 **[Alternative: ONE_STI_Made_by_MJ.apk](apk/ONE_STI_Made_by_MJ.apk?raw=true)**

### Option 2: GitHub Releases
- Download from the [**Releases**](releases) section of this GitHub repository.

---

## 📲 How to Install on Android

1. Download **`One_STI.apk`** to your phone.
2. Open the file from your **Downloads** or notification bar.
3. If prompted with *"Install unknown apps"*, tap **Settings** and enable **"Allow from this source"**.
4. Tap **Install** and log in to One STI!

---

## ✨ Features

- 🖤 **Sleek Solid Black Top Bar:** Status bar header matches edge-to-edge black styling with crisp white status indicators.
- ⚡ **144Hz Ultra-Smooth Engine:** Full support for 144Hz, 120Hz, and 90Hz displays with minimal post-processing latency and GPU tile pre-rasterization.
- 🔒 **Permanent Login (All Accounts Never Log Out):** 
  - Converts all session cookies into persistent records with 10-year expiration dates.
  - Native token vaulting (`StiSessionBridge`) mirrors all `sessionStorage` and `localStorage` auth tokens into native storage.
  - Automatic Microsoft KMSI ("Stay signed in?") confirmation.
- 🔄 **Anti-Inactivity Keep-Alive:** Periodic 25-second heartbeats and idle resets prevent university portal session timeouts.
- 🔔 **Grade Release Push Notifications:** Automatically monitors student profile grades and sends high-priority notifications with direct deep links.
- 📶 **Offline Caching & Smart Reconnect:** View previously loaded portal content offline with automatic reconnection when data restores.

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
- **Application:** One STI
