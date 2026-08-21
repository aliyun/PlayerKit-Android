Language: [中文](README.md) | English

![alibaba_cloud_logo](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/AlibabaCloud.svg)

# **AliPlayerKit Android**

[![Platform](https://img.shields.io/badge/Platform-Android%20API%2021%2B-brightgreen)](https://www.android.com/) [![Language](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com/) [![Gradle](https://img.shields.io/badge/Gradle-compatible-green)](https://gradle.org/) [![website](https://img.shields.io/badge/Product-VOD-FF6A00)](https://www.alibabacloud.com/en/product/apsaravideo-for-vod)

---

## **1. Project Introduction**

**AliPlayerKit** is Alibaba Cloud's **player UI integration solution** for video applications, offering **low-code, extensible player UI components** and **scenario-based solutions**. 

<p align="center">
  <img
    src="https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/playerkit-ui-preview.png"
    alt="AliPlayerKit UI preview"
    width="100%"
  />
</p>

<p align="center">
  <sub>Ready-to-use player UI components for multiple playback scenarios</sub>
</p>

By encapsulating player capabilities and UI interactions, it enables rapid development of app playback features with minimal integration effort — no need to call underlying player APIs directly or build complex player UIs from scratch.

---

## **2. Core Features**

- **Low-Code Integration** — Minimalist API design; integrate the player in just a few lines of code.
- **Ready-to-Use UI Components** — Configurable player UI components covering essential playback and common interactions, with support for on-demand customization.
- **All-in-One Playback Experience** — Simply pass a video VID to automatically display cover images, video titles, seek thumbnails, and other complete playback information for a one-stop playback experience.
- **Scenario-Based Solutions** — Built-in support for typical business scenarios such as mid/long-form video, short video, live streaming, list playback, and AI education to quickly build complete playback experiences.
- **Highly Extensible Architecture** — A slot system for flexible UI composition and a strategy system for extensible business logic.
- **Cross-Platform Unified Architecture** — All platforms share the same design philosophy and API semantics, reducing multi-platform integration and maintenance costs.

| Platform | Description | Source | Docs |
|----------|-------------|--------|------|
| **Android** | Native Android player UI components & scenario solutions | [PlayerKit-Android](https://github.com/aliyun/PlayerKit-Android) | [Online Docs](https://aliyun.github.io/PlayerKit-Android/) |
| **iOS** | Native iOS player UI components & scenario solutions | [PlayerKit-iOS](https://github.com/aliyun/PlayerKit-iOS) | [Online Docs](https://aliyun.github.io/PlayerKit-iOS/) |
| **Flutter** | Cross-platform Flutter player widget | [player_widget](https://github.com/aliyun/player_widget) | [Online Docs](https://aliyun.github.io/player_widget/) |

---

## **3. Project Structure**

In terms of architecture, **AliPlayerKit sits on top of the player core**, providing a unified UI component system and playback scenario abstraction to host the common capabilities of various playback businesses:

![ProductArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Android/ProductArchitecture-EN.png)

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

AliPlayerKit adopts a **layered architecture design**, providing two integration approaches that you can choose from based on your business needs:

| Layer                   | Module             | Description                                                          | Use Cases                                                 |
| ----------------------- | ------------------ | -------------------------------------------------------------------- | --------------------------------------------------------- |
| **Component Layer**     | `playerkit`        | Core UI components, providing player views, controllers, and data models | When custom UI or flexible playback control is needed  |
| **Scene Layer**         | `playerkit-scenes` | Complete scenario solutions, including UI and business logic         | Quickly implement standard playback scenarios              |

> **Note**: The scene layer depends on the component layer. If you choose scene-layer integration, you must first complete component-layer integration.

> **Detailed Steps**: See [Integration](./docs/Integration-EN.md). For more information, refer to the [Documentation Index](./docs/README-EN.md).

---

## **6. Quick Onboarding**

In just a few steps, you can easily implement video playback. The diagram below shows the **component layer integration** flow:

![QuickStart](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Android/QuickStart-EN.png)

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
| License | Player License certificate and License Key obtained. See [Obtain an ApsaraVideo Player SDK license](https://www.alibabacloud.com/help/en/vod/developer-reference/obtain-the-player-sdk-license) for details |

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

**Step 3: Configure License**

The player SDK requires a valid License to function properly. See [Integrate a license](https://www.alibabacloud.com/help/en/vod/developer-reference/access-to-license) for License acquisition and configuration.

**Step 4: Run the Project**

Open the project in Android Studio and run the `demo-app` module.

#### **FAQ**

**Changing the package name**: The License is bound to the application package name. To modify the `demo-app` package name:

1. Reapply for a License with the new package name
2. Update `applicationId` in `demo-app/build.gradle`
3. Update the `package` attribute in `demo-app/src/main/AndroidManifest.xml`

> **Note**: Without proper License configuration, the player will not work and may throw authorization exceptions. See [License FAQ](https://www.alibabacloud.com/help/en/vod/developer-reference/faqs-for-sdk-license) for details.

### **7.2 Demo App**

To help developers quickly experience the features of AliPlayerKit, we have built a demo package based on this project. The demo can be installed directly on a device, with no development environment required.

**How to obtain:**

Scan the following QR code with your phone to quickly download and install the demo:

![Demo QR Code](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/demo-qr-code_en.png)

> **Note**: The QR code points to the latest version of the demo. Please ensure that your device allows the installation of third-party applications.

---

## **8. Device-Cloud Synergy**

AliPlayerKit is built on top of Alibaba Cloud's audio and video capabilities. We recommend using it together with Alibaba Cloud products to take full advantage of device-cloud synergy and further enhance playback capabilities and the video experience.

| Cloud Product                                                                        | Description                                                                                        |
| ------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------- |
| [ApsaraVideo for VOD](https://www.alibabacloud.com/en/product/apsaravideo-for-vod)    | Provides one-stop video-on-demand services, including video upload, storage, transcoding, distribution, and playback |
| [ApsaraVideo for Live](https://www.alibabacloud.com/en/product/apsaravideo-for-live)  | Provides end-to-end live streaming services, including stream ingest, transcoding, distribution, and playback |

---

## **9. Contact Us**

- 📘 **Official Documentation**: [Player Help Center](https://www.alibabacloud.com/help/en/apsara-video-sdk/)
- 🔍 **Console**: [ApsaraVideo for VOD Console](https://vod.console.alibabacloud.com)
- 📝 **Submit a Ticket**: [Alibaba Cloud Official Technical Support](https://smartservice.console.alibabacloud.com/service/create-ticket)
- 💬 **GitHub Issues**: [Feedback and suggestions are welcome](https://github.com/aliyun/PlayerKit-Android/issues)

For more FAQs and fixes about using the Alibaba Cloud player, see [FAQ about ApsaraVideo Player](https://www.alibabacloud.com/help/en/vod/support/faq-about-apsaravideo-player/).
