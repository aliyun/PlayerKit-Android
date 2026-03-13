Language: 中文简体 | [English](README-EN.md)

![alibaba_cloud_logo](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/AlibabaCloud.svg)

# **AliPlayerKit Android**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://www.android.com/) [![Docs](https://img.shields.io/badge/Docs-AliPlayer-blue?logo=aliyun)](https://help.aliyun.com/zh/vod/developer-reference/apsaravideo-player-sdk-for-android/) [![website](https://img.shields.io/badge/Product-VOD-FF6A00)](https://www.aliyun.com/product/vod)

---

## **1. 项目简介**

**AliPlayerKit 是面向视频业务的核心播放器 UI 架构设计。**

它提供 **低代码、可扩展的播放器 UI 组件** 与 **场景化解决方案**，使应用 **无需处理复杂的播放器 API 与 UI 实现**，即可 **快速构建完整的视频播放能力**。

---

## **2. 核心特性**

- **低代码接入** — 无需直接调用底层播放器 API，几行代码即可完成视频播放能力接入。
- **开箱即用的 UI 组件** — 提供可配置的播放器 UI 组件，覆盖基础播放与常见交互能力。
- **场景化解决方案** — 内置中长视频、短视频、直播、播放列表等典型业务场景，快速搭建完整播放体验。
- **高度可扩展的架构设计** — 插槽系统支持 UI 自由组合，策略系统支持业务逻辑灵活扩展。

---

## **3. 项目结构**

在架构层级上，**AliPlayerKit 位于播放器内核之上**，通过统一的 UI 组件体系与播放场景抽象，承载不同播放业务的共性能力：

![ProductArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/ProductArchitecture.png)

项目模块结构如下：

```
PlayerKit-Android/
├── demo-app/                   # 演示应用：完整功能演示
├── demo-settings/              # 设置模块：演示应用配置界面
├── docs/                       # 项目文档：架构说明、接入指南与 API 文档
├── playerkit/                  # 核心模块：播放器 UI 组件
├── playerkit-examples/         # 示例模块：功能演示
└── playerkit-scenes/           # 场景模块：业务场景解决方案
```

---

## **4. 快速集成**

AliPlayerKit 提供两种集成方案，完整的集成流程如下：

![Integration](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Integration.png)

> **详细步骤**：请参阅 [集成准备](./docs/Integration.md)。如需了解更多内容，请参考 [文档目录](./docs/README.md)。

---

## **5. 快速接入**

AliPlayerKit 采用 **分层架构设计**，提供两种接入方式，您可以根据业务需求灵活选择：

| 层级 | 模块 | 说明 | 适用场景 |
|-----|------|------|---------|
| **组件层** | `playerkit` | 核心 UI 组件，提供播放器视图、控制器、数据模型 | 需要自定义 UI 或灵活控制播放行为 |
| **场景层** | `playerkit-scenes` | 完整场景解决方案，包含 UI 和业务逻辑 | 快速实现标准播放场景 |

只需几步，您就可以轻松实现视频播放功能！下图展示了 **组件层接入** 的流程：

![QuickStart](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/QuickStart.png)

> **详细步骤**：请参阅 [快速开始](./docs/QuickStart.md)。如需了解更多内容，请参考 [文档目录](./docs/README.md)。

---

## **6. 效果演示**

为了帮助开发者快速体验 **AliPlayerKit** 的功能，我们提供两种方式：

- **项目运行**：运行示例工程体验完整功能
- **演示应用**：直接安装 APK 进行体验

### **6.1 项目运行**

#### **前置条件**

在运行项目前，请确保满足以下条件：

| 条件 | 说明 |
|-----|------|
| JDK 11 | 配置方式：Preferences → Build Tools → Gradle → Gradle JDK，选择 11 |
| Android Studio | 最新版本 |
| Android SDK | 最低 API 21（Android 5.0），建议 compileSdkVersion 31+ |
| Gradle | 版本不低于 7.0 |
| License | 已获取播放器 License 授权证书和 License Key，详见 [管理 License](https://help.aliyun.com/zh/vod/developer-reference/license-authorization-and-management) |

#### **运行步骤**

**步骤 1：添加 License 证书文件**

将 License 证书文件（如 `license.crt`）放置到 `demo-app/src/main/assets/cert/` 目录下。

**步骤 2：配置 License Key**

在 `demo-app/src/main/AndroidManifest.xml` 的 `<application>` 元素下添加：

```xml
<meta-data
    android:name="com.aliyun.alivc_license.licensekey"
    android:value="您的 License Key" />
<meta-data
    android:name="com.aliyun.alivc_license.licensefile"
    android:value="assets/cert/license.crt" />
```

**步骤 3：运行项目**

使用 Android Studio 打开项目，运行 `demo-app` 模块。

#### **常见问题**

**修改包名**：License 与应用包名绑定，如需修改 `demo-app` 包名：

1. 重新申请对应包名的 License
2. 更新 `demo-app/build.gradle` 中的 `applicationId`
3. 更新 `demo-app/src/main/AndroidManifest.xml` 中的 `package` 属性

> **注意**：如未正确配置 License，播放器将无法正常工作，并可能抛出授权异常。

### **6.2 演示应用**

为了帮助开发者快速体验 AliPlayerKit 的功能，我们基于此工程构建了演示包。该演示包可以直接安装到设备上运行，无需配置开发环境。

**获取方式：**

使用手机扫描以下二维码，即可快速下载并安装演示包：

![Demo QR Code](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/demo-qr-code.png)

> **注意**：二维码链接指向最新版本的演示包，请确保您的设备已开启允许安装第三方应用的权限。

---

## **7. 相关产品**

AliPlayerKit 基于阿里云音视频能力构建，建议结合以下云产品使用，通过端云结合获得更好的播放能力和视频体验：

- [阿里云视频点播（VOD）](https://www.aliyun.com/product/vod)
- [阿里云视频直播（Live）](https://www.aliyun.com/product/live)
- [音视频终端 SDK](https://help.aliyun.com/zh/apsara-video-sdk/)

---

## **8. 联系我们**

- 📘 **官方文档**：[播放器帮助中心](https://help.aliyun.com/zh/vod/)
- 💬 **GitHub Issues**：欢迎提交反馈与建议
- 🔍 **控制台**：[视频点播控制台](https://vod.console.aliyun.com)