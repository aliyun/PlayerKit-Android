> 📚 **Recommended Reading Path**
>
> [Core Features](./CoreFeatures-EN.md) → **Integration Preparation** → [Quick Start](./QuickStart-EN.md) → [API Reference](./ApiReference-EN.md)
>
> 🤖 **AI-Friendly Tip**
>
> **This document is well-structured with clear steps, suitable for AI to read, understand, and execute. It can also be used as Skills, allowing AI to assist with the integration and use of AliPlayerKit.**
>
> **We will release dedicated Skills in the future to provide more complete integration support.**

---

# **AliPlayerKit Integration Preparation**

This document introduces the integration steps and prerequisites for AliPlayerKit, helping you quickly integrate AliPlayerKit into your project.

---

## **1. Integration Process Overview**

AliPlayerKit provides two integration solutions. You can choose based on your business needs:

| Integration Solution | Description | Use Cases |
|---------|------|---------|
| **Solution 1: Component-Layer Integration** | Integrate the `playerkit` core module | Custom Player UI is required, or flexible control of playback behavior |
| **Solution 2: Scene-Layer Integration** | Integrate scene modules on top of the component layer | Quickly implement standard playback scenarios |

> **Note**: The scene layer depends on the component layer. If you choose scene-layer integration, you must first complete component-layer integration.

The following diagram shows the complete integration process:

![Integration](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Integration-EN.png)

---

## **2. Prerequisites**

### **2.1 Development Environment Configuration**

Before using **AliPlayerKit**, please ensure your development environment meets the following requirements:

- **JDK 11**
  - Configuration path: Preferences → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK
  - Select 11 (if 11 is not available, please upgrade your Android Studio version)

- **Android Development Environment**
  - Install the latest version of Android Studio
  - Configure the Android SDK with a minimum supported API level of 21 (Android 5.0)
  - compileSdkVersion is recommended to be 31 or above
  - Gradle version 7.0 or above

### **2.2 License Preparation**

- You have obtained the Player License authorization certificate and License Key for the Audio & Video Terminal SDK
- For detailed steps, please refer to [Manage License](https://www.alibabacloud.com/help/en/vod/developer-reference/license-authorization-and-management)

> **Note**: If the License is not configured correctly, the player will not work properly and may throw an authorization exception.

---

## **3. Solution 1: Component-Layer Integration**

The `playerkit` core module provides out-of-the-box, configurable Player UI components, covering basic playback and common interaction capabilities.

### **Step 1: Copy the Component Module**

Copy the `playerkit` core module (library) directory into your project:

```bash
# Copy the playerkit core module to the root directory of your project
# Note: copy only the playerkit directory, not the entire project or example modules such as playerkit-examples / playerkit-scenes
cp -r playerkit /path/to/your/project/
```

### **Step 2: Gradle Project Configuration**

#### **2.1 Configure the Maven Repository**

Add the Aliyun SDK Maven source to your project:

* **Groovy DSL** (`settings.gradle`):

```groovy
repositories {
    // aliyun maven
    maven { url "https://maven.aliyun.com/repository/releases" }
}
```

* **Kotlin DSL** (`settings.gradle.kts`):

```kotlin
repositories {
    // aliyun maven
    maven("https://maven.aliyun.com/repository/releases")
}
```

#### **2.2 Add Module Reference**

* **Groovy DSL** (`settings.gradle`):

```groovy
include ':playerkit'
```

* **Kotlin DSL** (`settings.gradle.kts`):

```kotlin
include(":playerkit")
```

#### **2.3 Add Module Dependency**

* **Groovy DSL** (`build.gradle` of the app module):

```groovy
dependencies {
    // AliPlayerKit core module: provides out-of-the-box, configurable Player UI components covering basic playback and common interaction capabilities
    implementation project(':playerkit')
}
```

* **Kotlin DSL** (`build.gradle.kts` of the app module):

```kotlin
dependencies {
    // AliPlayerKit core module: provides out-of-the-box, configurable Player UI components covering basic playback and common interaction capabilities
    implementation(project(":playerkit"))
}
```

### **Step 3: Configure the License**

#### **3.1 Add the License Certificate File**

Place the prepared License certificate file (such as `license.crt`) into the `src/main/assets/cert/` directory of the app module.

#### **3.2 Add License Configuration**

Add the following configuration to `AndroidManifest.xml` of the app module:

```xml
<!--    Configure license    -->
<!--    Access License: https://www.alibabacloud.com/help/en/vod/developer-reference/access-to-license     -->
<meta-data
    android:name="com.aliyun.alivc_license.licensekey"
    android:value="Your License Key" />
<meta-data
    android:name="com.aliyun.alivc_license.licensefile"
    android:value="assets/cert/license.crt" />
```

> **Note**:
>
> - Make sure the `<meta-data>` nodes are placed under the `<application>` element and the `name` attribute is correct
> - If the player SDK service environment is overseas, please refer to [Access License on Android](https://www.alibabacloud.com/help/en/vod/developer-reference/access-to-license) to complete the configuration
> - For a complete configuration example, refer to the `demo-app` module

### **Step 4: Verify Integration**

#### **4.1 Sync and Build the Project**

Sync Gradle and build the project to confirm that dependencies and module configurations are correct:

```bash
./gradlew clean assemble --stacktrace --info
```

If the build succeeds with no dependency errors, the `playerkit` module has been correctly referenced.

#### **4.2 Record the Integration Version (Recommended)**

**💡 Recommendation**: After integration is complete and the build passes, it is recommended to perform a `git commit` to record the current **commit ID** of the component source code. This record can serve as the version baseline at the time of component integration, providing important traceability for future component upgrades or source modifications, and helps control the integration access quality of the component. It also makes it easier to quickly locate the currently integrated component version during troubleshooting or technical support. For example:

```bash
git add .
git commit -m "feat: integrate AliPlayerKit module (source commit: 2c30d92)"
```

---

## **4. Solution 2: Scene-Layer Integration**

The scene layer provides playback examples for specific business scenarios such as medium/long video, short video, live streaming, and list playback. Each scene module is an independent example. You can choose to reference or directly integrate them based on your business needs.

> **Prerequisite**: The scene layer depends on the capabilities of the component layer. Before integrating the scene layer, please complete the **component-layer integration** first.

### **Step 1: Copy Scene Modules**

According to your business needs, copy the required scene modules into your project:

| Module | Description |
|------|------|
| `scene-common` | Scene common module, contains example video constants (can be omitted if you implement your own video sources) |
| `scene-longvideo` | Medium/long video scene example |
| `scene-shortvideo` | Short video scene example |
| `scene-live` | Live streaming scene example |
| `scene-playlist` | List playback scene example |

```bash
# scene-common contains example video constants and can be omitted if you implement your own video sources
cp -r playerkit-scenes/scene-common /path/to/your/project/

# Other scene modules: choose as needed
cp -r playerkit-scenes/scene-longvideo /path/to/your/project/
cp -r playerkit-scenes/scene-live /path/to/your/project/
# ...
```

> **Important**:
>
> - The `demo-settings` module **does not need to be copied**. This module is only used for the Demo, providing features such as QR code configuration and playback link storage.
> - The `scene-common` module provides example video constants. If you have implemented your own video source acquisition logic, you can omit this module.

### **Step 2 (Optional): Handle Module Dependencies**

The `build.gradle` of the scene modules depends on `demo-settings` and `scene-common`. Adjust according to actual conditions during integration:

| Module | Dependencies | Handling During Integration |
|------|--------|----------------|
| `scene-longvideo` | `demo-settings`, `scene-common` | Remove `demo-settings`; `scene-common` is optional |
| `scene-live` | `demo-settings`, `scene-common` | Remove `demo-settings`; `scene-common` is optional |
| `scene-shortvideo` | `scene-common` | `scene-common` is optional |
| `scene-playlist` | `scene-common` | `scene-common` is optional |

**2.1 Remove demo-settings Dependency** (if applicable)

The `build.gradle` of `scene-longvideo` and `scene-live` references `demo-settings`. It needs to be removed:

```groovy
dependencies {
    // ...
    // Delete this line: demo-settings is for Demo only
    // implementation project(':demo-settings')
}
```

**2.2 Handle scene-common Dependency** (Optional)

The `scene-common` module mainly provides example video constants (such as `SceneConstants`). You can choose to:

- **Keep the dependency**: Use the example videos directly for testing without extra work
- **Remove the dependency**: If you implement your own video source, you can delete the dependency in `build.gradle`

```groovy
dependencies {
    // ...
    // If you implement your own video source, you can comment out or delete this line
    // implementation project(':scene-common')
}
```

**2.3 Replace Video Source Configuration Code**

After removing the `demo-settings` or `scene-common` dependency, you need to modify the video source acquisition logic in the Activity:

```java
// ===== Original code (using demo-settings / scene-common) =====
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.scenes.common.SceneConstants;

private String getVideoVid() {
    String savedVid = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID);
    return StringUtil.isNotEmpty(savedVid) ? savedVid : SceneConstants.LANDSCAPE_SAMPLE_VID;
}

// ===== After replacement (using your own video source) =====
private String getVideoVid() {
    return "Your Video Vid";  // Or fetch from your business interface
}

private String getVideoPlayAuth() {
    return "Your PlayAuth";  // Or fetch from your business interface
}
```

### **Step 3: Gradle Project Configuration**

#### **3.1 Add Module References**

* **Groovy DSL** (`settings.gradle`):

```groovy
include ':scene-common'
include ':scene-longvideo'
// Add other scene modules as needed...
```

* **Kotlin DSL** (`settings.gradle.kts`):

```kotlin
include(":scene-common")
include(":scene-longvideo")
// Add other scene modules as needed...
```

#### **3.2 Add Module Dependencies**

* **Groovy DSL** (`build.gradle` of the app module):

```groovy
dependencies {
    // AliPlayerKit playback scene module: based on AliPlayerKit core components, provides out-of-the-box standard playback scene solutions
    // Medium/long video scene module: a complete solution for medium/long video playback
    implementation project(':scene-longvideo')
}
```

* **Kotlin DSL** (`build.gradle.kts` of the app module):

```kotlin
dependencies {
    // AliPlayerKit playback scene module: based on AliPlayerKit core components, provides out-of-the-box standard playback scene solutions
    // Medium/long video scene module: a complete solution for medium/long video playback
    implementation(project(":scene-longvideo"))
}
```

### **Step 4: Verify Integration**

Sync Gradle and build the project to confirm that dependencies and module configurations are correct:

```bash
./gradlew clean assemble --stacktrace --info
```

> **💡 Recommendation**: After scene module integration, it is also recommended to perform a `git commit` to record the commit ID for future traceability.

---

## **5. (Optional) SDK Upgrade Guide**

AliPlayerKit internally depends on the following SDKs:

| SDK | Description |
|-----|------|
| **AliPlayer SDK** | Aliyun Player SDK, provides basic playback capabilities such as video decoding, rendering, and playback control |
| **RtsSDK** | Aliyun RTS SDK, provides RTS ultra-low-latency live streaming capabilities, supporting millisecond-level real-time streaming playback |

If you need to upgrade the underlying SDK version, refer to the steps below.

### **5.1 View Version Information**

The current SDK version information that AliPlayerKit depends on is defined in the `playerkit/build.gradle` file.

You can view the latest versions and documentation of the related SDKs through the following links:

- [Download Player SDK](https://www.alibabacloud.com/help/en/vod/developer-reference/sdk-download)
- [Android Player SDK Release Notes](https://www.alibabacloud.com/help/en/vod/developer-reference/release-notes-for-apsaravideo-player-sdk-for-android)
- [Pull Streams over RTS on Android](https://www.alibabacloud.com/help/en/live/pull-streams-over-rts-on-android)

### **5.2 Upgrade Steps**

1. **Confirm Version Compatibility**

   Before upgrading, it is recommended to read the SDK release notes and changelog to confirm whether the target version contains Breaking Changes or other compatibility adjustments.

2. **Modify the Version Number**

   Modify the SDK version number in `playerkit/build.gradle`:

   ```groovy
   def player_sdk_version = "x.x.x"  // Replace with the target version
   api "com.aliyun.sdk.android:AliyunPlayer:$player_sdk_version-full"
   
   // The RtsSDK version must be consistent with the player version
   def rts_sdk_version = "x.x.x"  // Replace with the target version
   api "com.aliyun.rts.android:RtsSDK:$rts_sdk_version"
   ```

3. **Verify the Upgrade**:

   - Clean and rebuild the project:

     ```bash
     ./gradlew clean assemble --stacktrace --info
     ```

   - Verify core playback functions: play, pause, seek, playback rate, etc.

   - Verify specific playback scenarios: such as RTS ultra-low-latency live streaming, etc.

> **💡 Recommendation**: After upgrading the SDK, it is recommended to record the version numbers before and after the upgrade for future troubleshooting and version traceability.

---

## **6. FAQ**

### **6.1 License-Related Issues**

**Issue**: The player reports a License error

**Solution**:

1. Verify that the License certificate and Key are correctly configured
2. Check whether the License has expired
3. Confirm that the package name matches the package name bound to the License
4. If the License is not configured correctly, abnormalities such as a black screen during playback may occur after integration

### **6.2 Dependency Conflict Issues**

**Issue**: Gradle sync fails with a dependency conflict

**Solution**:
1. Check whether the project already contains a different version of the same dependency
2. Use `exclude` to exclude the conflicting dependency
3. Unify dependency versions across the project
4. If your project already contains the same third-party library, please adjust the version number in the playerkit module to ensure compatibility and avoid conflicts

### **6.3 Initialization Failure Issues**

**Issue**: Calling `AliPlayerKit.init()` throws an exception

**Solution**:
1. Confirm that Context is not null
2. Confirm that it is called in `Application.onCreate()`
3. Check whether it is called repeatedly (repeated calls will be ignored and will not throw an exception)

### **6.4 SDK Version Configuration Issues**

**Issue**: SDK version mismatch error during build

**Solution**:

Make sure the configuration of the playerkit module (such as compileSdkVersion, buildToolsVersion, minSdkVersion, targetSdkVersion, etc.) is consistent with the settings in your project.

### **6.5 Namespace-Related Issues**

**Issue**: Namespace-related errors occur during compilation after directly copying modules

**Solution**:

The `build.gradle` of each module already declares a namespace by default, adapting to AGP 7.x and above. Adjust as follows according to the AGP version of your project:

| AGP Version | Configuration Requirement |
|---------|---------|
| **≥ 8.0** (e.g., 8.3.2) | Keep the namespace declaration in `build.gradle` |
| **7.x** (e.g., 7.4.2) | Namespace declaration is optional; recommended to keep for future upgrade compatibility |
| **< 7.0** (e.g., 4.0.1) | Comment out or remove the namespace declaration in `build.gradle`, and use the package attribute in `AndroidManifest.xml` |

**Configuration location**: In the `android {}` block of each module's `build.gradle`

```groovy
android {
    /// Integration FAQ
    /// Notes about namespace declaration:
    /// - AGP version ≥ 8.0 (e.g., 8.3.2): namespace must be declared
    /// - AGP version < 7.0 (e.g., 4.0.1): remove namespace declaration and use the package attribute in AndroidManifest.xml
    /// - AGP version 7.x: namespace declaration is optional; recommended for future upgrade compatibility
    namespace = "com.aliyun.playerkit"
    // ...
}
```

### **6.6 Emulator Not Supported**

**Issue**: Exceptions occur when running on an emulator

**Solution**:

The Android Player SDK does not support emulators. After integration, testing must be performed on a real device.

### **6.7 Repository Priority Conflict**

**Issue**: Conflicts occur when Gradle handles repository priority

**Solution**:

Add the repository in `settings.gradle` first.

---

After completing the above steps, you can integrate and use AliPlayerKit in your project.

Next, you can refer to **[Quick Start](./QuickStart-EN.md)** to learn how to create a player and implement basic video playback.
