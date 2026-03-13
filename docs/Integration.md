Language: 中文简体 | [English](Integration-EN.md)

> 📚 **推荐阅读路径**
>
> [核心能力](./CoreFeatures.md) → **集成准备** → [快速开始](./QuickStart.md) → [API 参考](./ApiReference.md)
>
> 🤖 **AI 友好提示**
>
> **本文档结构清晰、步骤明确，适合 AI 读取、理解与执行；也可作为 Skills 使用，由 AI 辅助完成 AliPlayerKit 的接入与使用。**
>
> **后续我们将推出专门的 Skills，提供更完整的接入支持。**

---

# **AliPlayerKit 集成准备**

本文档将介绍 AliPlayerKit 的集成步骤和前提条件，帮助您快速将 AliPlayerKit 集成到项目中。

---

## **1. 集成流程概览**

AliPlayerKit 提供两种集成方案，您可以根据业务需求选择：

| 集成方案 | 说明 | 适用场景 |
|---------|------|---------|
| **方案一：组件层集成** | 集成 `playerkit` 核心模块 | 需要自定义播放器 UI 或灵活控制播放行为 |
| **方案二：场景层集成** | 在组件层基础上集成场景模块 | 快速实现标准播放场景 |

> **说明**：场景层依赖组件层。若选择场景层集成，需先完成组件层集成。

下图展示了完整的集成流程：

![Integration](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Integration.png)

---

## **2. 前提条件**

### **2.1 开发环境配置**

在使用 **AliPlayerKit** 之前，请确保您的开发环境满足以下要求：

- **JDK 11**
  - 配置方式：Preferences → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK
  - 选择 11（如果没有 11，请升级你的 Android Studio 版本）

- **Android 开发环境**
  - 安装最新版本的 Android Studio
  - 配置 Android SDK，最低支持 API 级别为 21（Android 5.0）
  - compileSdkVersion 建议使用 31 或以上
  - Gradle 版本不低于 7.0

### **2.2 License 准备**

- 您已获取音视频终端 SDK 的播放器 License 授权证书和 License Key
- 获取详细步骤请参见 [管理 License](https://help.aliyun.com/zh/vod/developer-reference/license-authorization-and-management)

> **注意**：如未正确配置 License，播放器将无法正常工作，并可能抛出授权异常。

---

## **3. 方案一：组件层集成**

`playerkit` 核心模块提供开箱即用、可配置的播放器 UI 组件，覆盖基础播放与常见交互能力。

### **步骤 1：拷贝组件模块**

将 `playerkit` 核心模块（library）目录拷贝到您的项目中：

```bash
# 将 playerkit 核心模块拷贝到您的项目根目录下
# 注意：仅拷贝 playerkit 目录，而非整个项目或 playerkit-examples、playerkit-scenes 等示例模块
cp -r playerkit /path/to/your/project/
```

### **步骤 2：Gradle 项目配置**

#### **2.1 配置 Maven 仓库**

在项目中引入阿里云 SDK 的 Maven 源：

* **Groovy DSL**（`settings.gradle`）：

```groovy
repositories {
    // aliyun maven
    maven { url "https://maven.aliyun.com/repository/releases" }
}
```

* **Kotlin DSL**（`settings.gradle.kts`）：

```kotlin
repositories {
    // aliyun maven
    maven("https://maven.aliyun.com/repository/releases")
}
```

#### **2.2 添加模块引用**

* **Groovy DSL**（`settings.gradle`）：

```groovy
include ':playerkit'
```

* **Kotlin DSL**（`settings.gradle.kts`）：

```kotlin
include(":playerkit")
```

#### **2.3 添加模块依赖**

* **Groovy DSL**（app 模块的 `build.gradle`）：

```groovy
dependencies {
    // AliPlayerKit 核心模块：提供开箱即用、可配置的播放器 UI 组件，覆盖基础播放与常见交互能力
    implementation project(':playerkit')
}
```

* **Kotlin DSL**（app 模块的 `build.gradle.kts`）：

```kotlin
dependencies {
    // AliPlayerKit 核心模块：提供开箱即用、可配置的播放器 UI 组件，覆盖基础播放与常见交互能力
    implementation(project(":playerkit"))
}
```

### **步骤 3：配置 License**

#### **3.1 添加 License 证书文件**

将准备好的 License 证书文件（如 `license.crt`）放置到 app 模块的 `src/main/assets/cert/` 目录下。

#### **3.2 添加 License 配置**

在 app 模块的 `AndroidManifest.xml` 中添加以下配置：

```xml
<!--    配置license    -->
<!--    接入 License：https://help.aliyun.com/zh/vod/developer-reference/access-to-license     -->
<meta-data
    android:name="com.aliyun.alivc_license.licensekey"
    android:value="您的 License Key" />
<meta-data
    android:name="com.aliyun.alivc_license.licensefile"
    android:value="assets/cert/license.crt" />
```

> **注意**：
>
> - 确保 `<meta-data>` 节点处于 `<application>` 元素下面，且 `name` 属性正确
> - 若播放器 SDK 服务环境为海外，请参考 [Android 端接入 License](https://help.aliyun.com/zh/vod/developer-reference/access-to-license) 完成配置
> - 完整配置示例可参考 `demo-app` 模块

### **步骤 4：验证集成**

#### **4.1 同步并编译项目**

同步 Gradle 并编译项目，确认依赖与模块配置正确：

```bash
./gradlew clean assemble --stacktrace --info
```

若编译成功且无依赖错误，说明 `playerkit` 模块已被正确引用。

#### **4.2 记录集成版本（建议）**

**💡 建议**：集成完成并编译通过后，建议执行一次 `git commit`，记录当前组件源码的 **commit ID**。该记录可作为组件集成时的版本基线，为后续组件升级或源码修改提供重要的追溯依据，并有助于把控组件的集成准入质量。在问题排查或技术支持时，也可以快速定位当前集成的组件版本。例如：

```bash
git add .
git commit -m "feat: integrate AliPlayerKit module (source commit: 2c30d92)"
```

---

## **4. 方案二：场景层集成**

场景层提供针对具体业务场景的播放示例，如中长视频、短视频、直播和列表播放等。每个场景模块都是一个独立的示例，您可以根据业务需求选择参考或直接集成。

> **前置条件**：场景层依赖组件层能力。在集成场景层之前，请先完成 **组件层集成**。

### **步骤 1：拷贝场景模块**

根据业务需求，拷贝所需的场景模块到您的项目中：

| 模块 | 说明 |
|------|------|
| `scene-common` | 场景公共模块，包含示例视频常量（如自行实现视频源可省略） |
| `scene-longvideo` | 中长视频场景示例 |
| `scene-shortvideo` | 短视频场景示例 |
| `scene-live` | 直播场景示例 |
| `scene-playlist` | 列表播放场景示例 |

```bash
# scene-common 包含示例视频常量，如自行实现视频源可省略
cp -r playerkit-scenes/scene-common /path/to/your/project/

# 其他场景模块按需选择
cp -r playerkit-scenes/scene-longvideo /path/to/your/project/
cp -r playerkit-scenes/scene-live /path/to/your/project/
# ...
```

> **重要提示**：
>
> - `demo-settings` 模块**不需要拷贝**。该模块仅用于 Demo 演示，提供扫码配置、播放链接存储等功能。
> - `scene-common` 模块提供示例视频常量。如果您已自行实现视频源获取逻辑，可省略该模块。

### **步骤 2（可选）：处理模块依赖**

场景模块的 `build.gradle` 中依赖了 `demo-settings` 和 `scene-common`，集成时需根据实际情况调整：

| 模块 | 依赖项 | 集成时处理方式 |
|------|--------|----------------|
| `scene-longvideo` | `demo-settings`、`scene-common` | 移除 `demo-settings`，`scene-common` 可选 |
| `scene-live` | `demo-settings`、`scene-common` | 移除 `demo-settings`，`scene-common` 可选 |
| `scene-shortvideo` | `scene-common` | `scene-common` 可选 |
| `scene-playlist` | `scene-common` | `scene-common` 可选 |

**2.1 移除 demo-settings 依赖**（如适用）

`scene-longvideo` 和 `scene-live` 模块的 `build.gradle` 中引用了 `demo-settings`，需移除：

```groovy
dependencies {
    // ...
    // 删除此行：demo-settings 仅用于 Demo 演示
    // implementation project(':demo-settings')
}
```

**2.2 处理 scene-common 依赖**（可选）

`scene-common` 模块主要提供示例视频常量（如 `SceneConstants`）。您可以选择：

- **保留依赖**：直接使用示例视频进行测试，无需额外处理
- **移除依赖**：如自行实现视频源，可删除 `build.gradle` 中的依赖

```groovy
dependencies {
    // ...
    // 如自行实现视频源，可注释或删除此行
    // implementation project(':scene-common')
}
```

**2.3 替换视频源配置代码**

移除 `demo-settings` 或 `scene-common` 依赖后，需修改 Activity 中的视频源获取逻辑：

```java
// ===== 原代码（使用 demo-settings / scene-common）=====
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.scenes.common.SceneConstants;

private String getVideoVid() {
    String savedVid = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID);
    return StringUtil.isNotEmpty(savedVid) ? savedVid : SceneConstants.LANDSCAPE_SAMPLE_VID;
}

// ===== 替换后（使用您的视频源）=====
private String getVideoVid() {
    return "您的视频 Vid";  // 或从您的业务接口获取
}

private String getVideoPlayAuth() {
    return "您的 PlayAuth";  // 或从您的业务接口获取
}
```

### **步骤 3：Gradle 项目配置**

#### **3.1 添加模块引用**

* **Groovy DSL**（`settings.gradle`）：

```groovy
include ':scene-common'
include ':scene-longvideo'
// 按需添加其他场景模块...
```

* **Kotlin DSL**（`settings.gradle.kts`）：

```kotlin
include(":scene-common")
include(":scene-longvideo")
// 按需添加其他场景模块...
```

#### **3.2 添加模块依赖**

* **Groovy DSL**（app 模块的 `build.gradle`）：

```groovy
dependencies {
    // AliPlayerKit 播放场景模块：基于 AliPlayerKit 核心组件，提供开箱即用的标准播放场景方案
    // 中长视频场景模块：提供中长视频播放的完整解决方案
    implementation project(':scene-longvideo')
}
```

* **Kotlin DSL**（app 模块的 `build.gradle.kts`）：

```kotlin
dependencies {
    // AliPlayerKit 播放场景模块：基于 AliPlayerKit 核心组件，提供开箱即用的标准播放场景方案
    // 中长视频场景模块：提供中长视频播放的完整解决方案
    implementation(project(":scene-longvideo"))
}
```

### **步骤 4：验证集成**

同步 Gradle 并编译项目，确认依赖与模块配置正确：

```bash
./gradlew clean assemble --stacktrace --info
```

> **💡 建议**：场景模块集成完成后，同样建议执行一次 `git commit` 记录 commit ID，便于后续追溯。

---

## **5. （可选）SDK 升级指南**

AliPlayerKit 内部依赖以下 SDK：

| SDK | 说明 |
|-----|------|
| **AliPlayer SDK** | 阿里云播放器 SDK，提供视频解码、渲染及播放控制等基础播放能力 |
| **RtsSDK** | 阿里云 RTS SDK，提供 RTS 超低延时直播播放能力，支持毫秒级延迟的实时流媒体播放 |

如需升级底层 SDK 版本，可参考以下步骤。

### **5.1 查看版本信息**

当前 AliPlayerKit 依赖的 SDK 版本信息定义在 `playerkit/build.gradle` 文件中。

您可以通过以下链接查看相关 SDK 的最新版本及文档：

- [下载播放器 SDK](https://help.aliyun.com/zh/vod/developer-reference/sdk-download)
- [Android 播放器 SDK 发布历史](https://help.aliyun.com/zh/vod/developer-reference/release-notes-for-apsaravideo-player-sdk-for-android)
- [Android 端实现 RTS 拉流](https://help.aliyun.com/zh/live/pull-streams-over-rts-on-android)

### **5.2 升级步骤**

1. **确认版本兼容性**

   升级前建议查阅 SDK 发布历史与更新日志，确认目标版本是否存在 Breaking Changes 或其他兼容性调整。

2. **修改版本号**

   在 `playerkit/build.gradle` 中修改 SDK 版本号：

   ```groovy
   def player_sdk_version = "x.x.x"  // 替换为目标版本
   api "com.aliyun.sdk.android:AliyunPlayer:$player_sdk_version-full"
   
   // RtsSDK 版本号需与播放器版本保持一致
   def rts_sdk_version = "x.x.x"  // 替换为目标版本
   api "com.aliyun.rts.android:RtsSDK:$rts_sdk_version"
   ```

3. **验证升级**：

   - 清理并重新编译项目：

     ```bash
     ./gradlew clean assemble --stacktrace --info
     ```

   - 验证核心播放功能：播放、暂停、Seek、倍速等

   - 验证特定播放场景：如 RTS 超低延时直播等

> **💡 建议**：升级 SDK 后，建议记录升级前后的版本号，以便后续问题排查和版本追溯。

---

## **6. 常见问题**

### **6.1 License 相关问题**

**问题**：播放器报 License 错误

**解决方案**：

1. 确认已正确配置 License 证书和 Key
2. 检查 License 是否已过期
3. 确认包名与 License 绑定的包名一致
4. 如未配置正确的 License，集成完毕后会出现播放黑屏等异常问题

### **6.2 依赖冲突问题**

**问题**：Gradle 同步失败，提示依赖冲突

**解决方案**：
1. 检查项目中是否已存在相同依赖的不同版本
2. 使用 `exclude` 排除冲突依赖
3. 统一项目中的依赖版本
4. 如果您的项目中已有相同第三方库，请调整 playerkit 模块中的版本号，以确保兼容性并避免冲突

### **6.3 初始化失败问题**

**问题**：调用 `AliPlayerKit.init()` 抛出异常

**解决方案**：
1. 确认 Context 不为 null
2. 确认在 `Application.onCreate()` 中调用
3. 检查是否重复调用（重复调用会被忽略，不会抛异常）

### **6.4 SDK 版本配置问题**

**问题**：编译时报 SDK 版本不匹配错误

**解决方案**：

请确保 playerkit 模块的配置（如 compileSdkVersion、buildToolsVersion、minSdkVersion、targetSdkVersion 等）与您的项目中的设置保持一致。

### **6.5 Namespace 相关问题**

**问题**：直接拷贝模块后，编译时出现 Namespace 相关的错误

**解决方案**：

各模块的 `build.gradle` 中已默认声明了 namespace，适配 AGP 7.x 及以上版本。根据您项目的 AGP 版本进行以下调整：

| AGP 版本 | 配置要求 |
|---------|---------|
| **≥ 8.0**（如 8.3.2） | 保持 `build.gradle` 中的 namespace 声明即可 |
| **7.x**（如 7.4.2） | namespace 声明可选，建议保留以兼容未来升级 |
| **< 7.0**（如 4.0.1） | 需注释或删除 `build.gradle` 中的 namespace 声明，使用 `AndroidManifest.xml` 的 package 属性 |

**配置位置**：各模块 `build.gradle` 的 `android {}` 块中

```groovy
android {
    /// 集成FAQ (Integration FAQ)
    /// 关于 namespace 声明的注意事项：
    /// - AGP 版本 ≥ 8.0（如 8.3.2）：必须声明 namespace
    /// - AGP 版本 < 7.0（如 4.0.1）：需移除 namespace 声明，使用 AndroidManifest.xml 的 package 属性
    /// - AGP 版本 7.x：namespace 声明可选，建议声明以兼容未来升级
    namespace = "com.aliyun.playerkit"
    // ...
}
```

### **6.6 模拟器不支持问题**

**问题**：在模拟器上运行出现异常

**解决方案**：

Android 播放器 SDK 不支持模拟器，集成完成后需在真机上测试。

### **6.7 Repository 优先级冲突**

**问题**：Gradle 在处理 repository 的优先级时出现冲突

**解决方案**：

请优先在 `settings.gradle` 中添加 repository。

---

完成以上步骤后，您即可在项目中集成并使用 AliPlayerKit。

接下来，您可以参考 **[快速开始](./QuickStart.md)**，了解如何创建播放器并实现基本的视频播放功能。