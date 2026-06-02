Language: [中文](README.md) | English

![alibaba_cloud_logo](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/AlibabaCloud.svg)

# **AliPlayerKit Android**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://www.android.com/) [![Docs](https://img.shields.io/badge/Docs-AliPlayer-blue?logo=aliyun)](https://www.alibabacloud.com/help/en/apsara-video-sdk/) [![website](https://img.shields.io/badge/Product-VOD-FF6A00)](https://www.alibabacloud.com/en/product/apsaravideo-for-vod)

---

## **1. Project Introduction**

**AliPlayerKit is a core player UI architecture designed for video business scenarios.**

It provides **low-code, extensible player UI components** and **scenario-based solutions**, enabling applications to **rapidly build complete video playback capabilities** **without dealing with complex player APIs or UI implementations**.

---

## **2. Core Features**

- **Low-code Integration** — Integrate video playback capabilities with just a few lines of code, without directly calling low-level player APIs.
- **Out-of-the-Box UI Components** — Configurable player UI components covering basic playback and common interaction capabilities.
- **Scenario-based Solutions** — Built-in typical business scenarios such as medium/long video, short video, live streaming, and playlists, enabling rapid construction of complete playback experiences.
- **Highly Extensible Architecture** — The slot system supports flexible UI composition, while the strategy system supports flexible extension of business logic.

---

## **3. Project Structure**

In terms of architecture, **AliPlayerKit sits on top of the player core**, providing a unified UI component system and playback scenario abstraction to host the common capabilities of various playback businesses:

![ProductArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/ProductArchitecture-EN.png)

The project module structure is as follows:

```
PlayerKit-Android/
├── demo-app/                   # Demo App: Full feature demonstration
├── demo-settings/              # Settings Module: Configuration UI for the demo app
├── docs/                       # Project Documentation: Architecture, integration guides, and API docs
├── playerkit/                  # Core Module: Player UI components
├── playerkit-examples/         # Examples Module: Feature demonstrations
└── playerkit-scenes/           # Scenes Module: Business scenario solutions
```

---

## **4. Quick Start**

### **4.1 Online Reading**

**[🌐 Click here to start reading online](https://aliyun.github.io/PlayerKit-Android/)**

### **4.2 Local Reading**

If you prefer to read the documentation locally, refer to the [**Documentation Index**](./docs/README-EN.md).

This index provides a clear documentation structure with step-by-step instructions, making it easy for **both AI and developers to understand and execute**. In the future, we will also provide dedicated **Skills support** for a more complete and convenient integration experience.

## **5. Quick Integration**

AliPlayerKit provides two integration approaches. The complete integration flow is as follows:

![Integration](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Integration-EN.png)

> **Detailed Steps**: See [Integration](./docs/Integration-EN.md). For more information, refer to the [Documentation Index](./docs/README-EN.md).

---

## **6. Quick Onboarding**

AliPlayerKit adopts a **layered architecture design**, providing two integration approaches that you can choose from based on your business needs:

| Layer | Module | Description | Use Cases |
|-----|------|------|---------|
| **Component Layer** | `playerkit` | Core UI components, providing player views, controllers, and data models | When custom UI or flexible playback control is needed |
| **Scene Layer** | `playerkit-scenes` | Complete scenario solutions, including UI and business logic | Quickly implement standard playback scenarios |

In just a few steps, you can easily implement video playback. The diagram below shows the **component layer integration** flow:

![QuickStart](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/QuickStart-EN.png)

> **Detailed Steps**: See [Quick Start](./docs/QuickStart-EN.md). For more information, refer to the [Documentation Index](./docs/README-EN.md).

---

## **7. Demo Showcase**

To help developers quickly experience the features of **AliPlayerKit**, we provide two methods:

- **Run the Project**: Run the sample project to experience the full feature set
- **Demo App**: Install the APK directly to try it out

### **7.1 Run the Project**

#### **Prerequisites**

Before running the project, ensure the following requirements are met:

| Requirement | Description |
|-----|------|
| JDK 11 | Configuration: Preferences → Build Tools → Gradle → Gradle JDK, select 11 |
| Android Studio | Latest version |
| Android SDK | Minimum API 21 (Android 5.0), recommended compileSdkVersion 31+ |
| Gradle | Version 7.0 or higher |
| License | Player License certificate and License Key obtained. See [License Authorization and Management](https://www.alibabacloud.com/help/en/apsara-video-sdk/) for details |

#### **Running Steps**

**Step 1: Add the License Certificate File**

Place the License certificate file (e.g., `license.crt`) into the `demo-app/src/main/assets/cert/` directory.

**Step 2: Configure the License Key**

Add the following inside the `<application>` element of `demo-app/src/main/AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.aliyun.alivc_license.licensekey"
    android:value="Your License Key" />
<meta-data
    android:name="com.aliyun.alivc_license.licensefile"
    android:value="assets/cert/license.crt" />
```

**Step 3: Run the Project**

Open the project in Android Studio and run the `demo-app` module.

#### **FAQ**

**Changing the package name**: The License is bound to the application package name. To modify the `demo-app` package name:

1. Reapply for a License with the new package name
2. Update `applicationId` in `demo-app/build.gradle`
3. Update the `package` attribute in `demo-app/src/main/AndroidManifest.xml`

> **Note**: Without proper License configuration, the player will not work and may throw authorization exceptions.

### **7.2 Demo App**

To help developers quickly experience the features of AliPlayerKit, we have built a demo package based on this project. The demo can be installed directly on a device, with no development environment required.

**How to obtain:**

Scan the following QR code with your phone to quickly download and install the demo:

![Demo QR Code](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/demo-qr-code_en.png)

> **Note**: The QR code points to the latest version of the demo. Please ensure that your device allows the installation of third-party applications.

---

## **8. Related Products**

AliPlayerKit is built on top of Alibaba Cloud's audio and video capabilities. We recommend combining it with the following cloud products to deliver a better playback experience through end-to-cloud integration:

- [ApsaraVideo for VOD](https://www.alibabacloud.com/en/product/apsaravideo-for-vod)
- [ApsaraVideo for Live](https://www.alibabacloud.com/en/product/apsaravideo-for-live)
- [ApsaraVideo SDK](https://www.alibabacloud.com/help/en/apsara-video-sdk/)

---

## **9. Contact Us**

- 📘 **Official Documentation**: [Player Help Center](https://www.alibabacloud.com/help/en/apsara-video-sdk/)
- 🔍 **Console**: [ApsaraVideo for VOD Console](https://vod.console.alibabacloud.com)
- 📝 **Submit a Ticket**: [Alibaba Cloud Official Technical Support](https://smartservice.console.alibabacloud.com/service/create-ticket)
- 💬 **GitHub Issues**: [Feedback and suggestions are welcome](https://github.com/aliyun/PlayerKit-Android/issues)

For more FAQs and fixes about using the Alibaba Cloud Player SDK, see [FAQ about ApsaraVideo Player](https://www.alibabacloud.com/help/en/vod/support/faq-about-apsaravideo-player/).
