<div align="center">
  <img src="assets\Android Battery - Logo.svg" width="96" height="96" alt="Battery Mode Checker App Icon" />
  <h1>Android Battery Unrestricted Checker (ABUC)</h1>

<p align="center">
  <a href="https://github.com/junksidetm/Android-Battery-Unrestricted-Checker"><img src="https://img.shields.io/badge/GitHub-Main-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub Main" /></a>
  <a href="https://codeberg.org/mrdarksidetm/Android-Battery-Unrestricted-Checker"><img src="https://img.shields.io/badge/Codeberg-Mirror-2185d0?style=flat-square&logo=codeberg&logoColor=white" alt="Codeberg Mirror" /></a>
  <a href="https://gitlab.com/mrdarksidetm/Android-Battery-Unrestricted-Checker"><img src="https://img.shields.io/badge/GitLab-Mirror-fc6d26?style=flat-square&logo=gitlab&logoColor=white" alt="GitLab Mirror" /></a>
  <a href="https://github.com/junksidetm/Android-Battery-Unrestricted-Checker/actions/workflows/build.yml"><img src="https://github.com/junksidetm/Android-Battery-Unrestricted-Checker/actions/workflows/build.yml/badge.svg" alt="CI Status" /></a>
  <a href="https://github.com/RikkaApps/Shizuku"><img src="https://img.shields.io/badge/Bridge-Shizuku_%26_Shevery-7C4DFF?style=flat-square" alt="Shizuku & Shevery" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat-square" alt="License" /></a>
</div>

<p align="center">
This app uses <b>Shizuku/Shevery</b> to handle one task and on task only and that is to show you the apps that have <u>Unrestricted</u> access to your battery and System can't Optimise or close them
</p>

## 🤖 Context

There are many OEMs and their skins, even the most basic Google Pixel OS that i sconsidered the Android Vanilla doesn't let you see the apps in one place who are given the permission <b>"Let's app X run in Background"</b> or something like <b>Don't optimise</b>. Eg: Realme OS is a big example thatdoesn't let you see these app permission at all. You have to check them indivisually

## 📱 Direct Standalone Download

Download the pure production-signed `.apk` directly to your phone without extracting zip files:

<p align="center">
  <a href="https://github.com/junksidetm/Android-Battery-Unrestricted-Checker/releases/latest/download/Android-Battery-Unrestricted-Checker-universal.apk" target="_blank">
    <img src="assets/images/Direct%20Link%20Frame%20Badge.svg" height="96" alt="Direct APK Download" />
  </a>
</p>

<p align="center">
  <a href="http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22com.unrestricted.batterychecker%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fjunksidetm%2FAndroid-Battery-Unrestricted-Checker%22%2C%22author%22%3A%22mrdarksidetm%22%2C%22name%22%3A%22Battery%20Mode%20Checker%22%2C%22preferredApkIndex%22%3A0%7D" target="_blank">
    <img src="assets/images/obtanium-badge.png" height="84" alt="Obtainium Badge" />
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/junksidetm/Android-Battery-Unrestricted-Checker/releases/latest" target="_blank">
    <img src="assets/images/github-badge.png" height="84" alt="GitHub Releases Badge" />
  </a>
</p>

## ✨ Core Features

* ⚡ **Instant Tri-State Auditing**: Accurately classifies all installed packages into **Unrestricted** (Doze exempt), **Optimized** (Dynamic standby bucketing), or **Restricted** (`STANDBY_BUCKET_RESTRICTED`).
* 🎛️ **Live Mode Switching**: Directly toggle any application between Unrestricted, Optimized, and Restricted with one tap via [Shizuku](https://github.com/RikkaApps/Shizuku) and [Shevery](https://github.com/HmnDev-Tech/shevery) privileged binder IPC.
* 🛡️ **Strict Shizuku Guard Lock**: If Shizuku or Shevery is offline or lacks authorization, all app battery data, search controls, and distribution graphs are locked and hidden behind an expressive locked state container with direct authorization actions.
* 📊 **Native Canvas Capsule**: Smooth real-time multi-segment distribution bar rendered directly with Compose `Canvas` and physics-based spring animations.
* 🔍 **Ergonomic Top-Down Search**: Borderless 28dp pill search bar positioned directly above category filter chips for seamless thumb navigation.
* 🏷️ **Expressive Filter Chips**: Fully rounded pill chips (`CircleShape`) with category-specific tonal containers and discrete count badges.
* 🛡️ **Official Shizuku & Framework Vector Logos**:
  - Authentic Shizuku Arcticons 48x48 vector line art (`ic_shizuku_logo.xml`).
  - Dynamic status banner dynamically transitioning from primary accent to emerald green when connected.
  - Official Android (`ic_android_logo.xml`) and Jetpack Compose (`ic_jetpack_compose_logo.xml`) vector logos in `AboutScreen.kt`.
* 📱 **Async Package App Icons**: Real application icons fetched dynamically via `PackageManager` with graceful placeholders inside squircle frames.
* 🧭 **3-Dots Overflow Menu**: Quick access to Refresh, in-app Guide dialog, Show system apps toggle (hidden by default), and About page.
* 🎭 **Android 13+ Themed Icons**: Native Adaptive Icon bundled with a dedicated `<monochrome>` layer for dynamic Material You wallpaper theming.
* 🛡️ **Zero Telemetry & Offline**: 100% offline, zero internet permissions, zero analytics, and zero background services.

---

## 🛠️ Architecture & Specifications

| Component | Technology | Detail |
| :--- | :--- | :--- |
| **Platform** | Native Android | 100% Kotlin & Jetpack Compose (BOM 2024.10.01) |
| **Design Language** | **Material 3 Expressive** | Full surface container token layering, 28dp capsules |
| **Target / Min SDK** | Android 15 (API 35) / Android 8.0 (API 26) | Edge-to-edge transparent system bars |
| **Privilege Engine** | Shizuku API 13.1.5 | Reflection-based binder IPC (compatible with Shizuku & Shevery) |
| **State Management** | Kotlin Coroutines & `StateFlow` | Lifecycle-aware collection with `collectAsStateWithLifecycle` |
| **Binary Output** | Universal FAT APK | Multi-ABI support (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) |
| **CI / CD** | GitHub Actions | Automated keystore persistence, static analysis, and releases |

---

## 📖 Background: Why This App Exists

In modern Android, battery optimization settings are buried deep within each individual application's system settings submenu. To inspect 150 installed apps, a user would need to navigate through 150 separate screens.

**Battery Mode Checker** consolidates everything into a single, unified dashboard:
1. **Unrestricted**: Exempt from Doze mode and background network/execution caps. Essential for VPNs, music streaming, VoIP, and navigation.
2. **Optimized**: Default dynamic App Standby Bucketing. Android throttles background work based on frequency of use.
3. **Restricted**: App is forcefully placed into `STANDBY_BUCKET_RESTRICTED`. Background tasks, alarms, and network access are aggressively restricted.

---

## 👤 Developer & Philosophy

Built with ❤️ by **Abhijeet Yadav** ([@junksidetm](https://github.com/junksidetm)).

This application adheres to the **Single-Purpose Utility Philosophy**: it is engineered to do one specific job with uncompromising precision, maximum performance, and zero bloat. Because its scope is tightly bounded, it requires no perpetual updates or tracking.

---

## 🌐 Source Mirrors

- **Main (GitHub)**: [github.com/junksidetm/Android-Battery-Unrestricted-Checker](https://github.com/junksidetm/Android-Battery-Unrestricted-Checker)
- **Mirror (Codeberg)**: [codeberg.org/mrdarksidetm/Android-Battery-Unrestricted-Checker](https://codeberg.org/mrdarksidetm/Android-Battery-Unrestricted-Checker)
- **Mirror (GitLab)**: [gitlab.com/mrdarksidetm/Android-Battery-Unrestricted-Checker](https://gitlab.com/mrdarksidetm/Android-Battery-Unrestricted-Checker)

---

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).

---

<div align="center">
<a href="https://github.com/junksidetm/junksidetm.github.io">
  <img src="https://raw.githubusercontent.com/junksidetm/assests/981a029b9f59b8ed581bbee9be318c86da52dcca/Images/Codium/Codium%20Banner/SVG/Codium%20-%20Banner%20Black.svg" width="360"></a>
<br><br>
<sub>
<p>This repository is a part of `"Codeium"`. A part of Darkside Studio.
</p>
</sub>
<p><b><sub>© 2026 Abhijeet Yadav.  All rights reserved. All logos are Copyright Law </sub></b></p>
