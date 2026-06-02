# **多语言 (Locale)**

**多语言(Locale)** 是 AliPlayerKit 的国际化支撑模块。它通过统一的语言管理器，提供自动跟随系统语言、全量语言替换和运行时语言切换等核心能力，使播放器 UI 文案能够灵活适配多语言场景。

---

## **1. 默认行为（零配置）**

AliPlayerKit **开箱即用**，无需编写任何代码即可支持多语言：

- 内置中文和英文两种语言（通过 `values/strings.xml` 和 `values-en/strings.xml`）
- 播放器 UI 文案**自动跟随系统语言**切换，无需任何初始化或 API 调用
- 用户在系统设置中切换语言后，播放器文案会自动切换为对应语言
- 如果系统语言不在内置支持范围内，则自动回退到默认语言（中文）

| 内置语言 | 资源目录 |
|---------|--------|
| 中文（默认） | `res/values/strings.xml` |
| 英文 | `res/values-en/strings.xml` |

> 对于大多数只需要中英文支持的应用，**无需调用任何 API，集成 AliPlayerKit 即可**。

---

## **2. 自定义场景**

仅当默认的「跟随系统语言」行为无法满足需求时，才需要使用以下 API。

### **2.1 强制指定语言（setLanguage）**

当需要在应用内提供语言切换入口、不跟随系统语言时，使用 `setLanguage()` 强制指定：

```java
// 强制切换到英文（不再跟随系统语言）
PlayerLocale.setLanguage("en");

// 强制切换回中文
PlayerLocale.setLanguage("zh");
```

> 💡 调用 `setLanguage()` 后，当前 Activity 会自动重建以使新语言生效。

### **2.2 新增语言支持（values-xx/strings.xml）**

当需要新增 AliPlayerKit 尚未内置的语言（如日语、韩语）时，使用 Android 标准资源机制。新增后同样支持自动跟随系统语言。

**Step by Step**：

1. **在应用模块中创建对应的资源目录**

   ```
   your-app/src/main/res/
   ├── values/strings.xml          ← 中文（默认）
   ├── values-en/strings.xml       ← 英文
   ├── values-ja/strings.xml       ← 新增：日语
   └── values-ko/strings.xml       ← 新增：韩语
   ```

   > 参考示例：`playerkit-examples/example-locale/src/main/res/values-ja/strings.xml`

2. **复制默认 `values/strings.xml` 并翻译全部条目**

   ```xml
   <!-- res/values-ko/strings.xml -->
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="setting_item_speed">재생 속도</string>
       <string name="setting_item_quality">화질</string>
       <string name="setting_item_loop">반복 재생</string>
       <string name="setting_item_mute">음소거</string>
       <string name="player_brightness">밝기</string>
       <string name="player_volume">볼륨</string>
       <!-- ... 翻译所有条目 ... -->
   </resources>
   ```

3. **使新语言生效**

   - **自动跟随系统**：用户将系统语言设为韩语后，播放器文案自动切换，无需调用任何 API
   - **强制指定**：如需在应用内手动切换，调用 `PlayerLocale.setLanguage("ko")`

> 💡 推荐全量翻译：将 `values/strings.xml` 中的所有条目都翻译到新语言文件中，避免部分文案回退到默认语言导致混合显示。

### **2.3 监听语言变更**

通过监听器感知语言切换，可用于刷新自定义 UI 或记录日志：

```java
// 创建监听器
PlayerLocale.OnLanguageChangedListener listener = (oldLanguage, newLanguage) -> {
    Log.i(TAG, "Language changed: " + oldLanguage + " → " + newLanguage);
    // 刷新自定义 UI
    refreshMyCustomUI();
};

// 注册监听器
PlayerLocale.addOnLanguageChangedListener(listener);

// 不再需要时移除（如 Activity onDestroy）
PlayerLocale.removeOnLanguageChangedListener(listener);
```

---

## **3. API 参考**

### **3.1 PlayerLocale 方法**

| 方法 | 说明 |
|-----|------|
| `get(resId)` | 获取文案（从 strings.xml 资源获取） |
| `get(resId, formatArgs)` | 获取格式化文案（支持 `%s`、`%d` 占位符） |
| `setLanguage(languageCode)` | 设置应用语言，触发 Activity 重建 |
| `getLanguage()` | 获取当前语言代码 |
| `addOnLanguageChangedListener(listener)` | 添加语言变更监听器 |
| `removeOnLanguageChangedListener(listener)` | 移除语言变更监听器 |

---

## **4. 设计说明**

### **4.1 文案查找机制**

`PlayerLocale.get()` 通过 Android 标准资源机制获取当前语言对应的文案：

- 如果存在当前语言对应的 `values-xx/strings.xml`，则返回该文件中的翻译
- 如果当前语言没有对应的资源目录，则回退到默认语言（中文 `values/strings.xml`）

### **4.2 线程安全**

| 组件 | 线程安全机制 |
|-----|------------|
| 监听器列表 | `CopyOnWriteArrayList`，读多写少场景性能优秀 |

### **4.3 语言切换机制**

`setLanguage()` 内部使用 `AppCompatDelegate.setApplicationLocales()` 实现：

- Android 13+：系统原生支持 per-app language，无需特殊处理
- Android 12 及以下：通过 `createConfigurationContext` 创建带正确 locale 的 Context
- 调用后会触发当前 Activity 重建，UI 自动刷新

### **4.4 版本兼容**

| Android 版本 | 行为 |
|-------------|------|
| Android 13+ (API 33+) | 使用系统原生 per-app language 能力，Application Context 自动同步 locale |
| Android 12 及以下 | 通过 AppCompatDelegate + createConfigurationContext 兼容实现 |

---

## **5. 最佳实践**

### **5.1 选择合适的方案**

| 场景 | 推荐方案 | 说明 |
|-----|---------|------|
| 仅需中英文 | 无需任何操作 | 内置中英文，自动跟随系统语言 |
| 新增整套语言 | `values-xx/strings.xml` | 全量翻译，Android 原生机制，新增后自动跟随系统语言 |
| 强制指定语言 | `PlayerLocale.setLanguage()` | 不跟随系统，由应用自行控制语言 |

### **5.2 注意事项**

| 事项 | 说明 |
|-----|------|
| 全量翻译 | 新增语言时建议翻译所有条目，避免混合语言显示 |
| 监听器生命周期 | 在 Activity/Fragment 销毁时移除监听器，避免内存泄漏 |
| 语言代码规范 | 使用 ISO 639-1 标准小写两字母格式（如 `"zh"`、`"en"`、`"ja"`） |

---

## **6. 示例参考**

项目提供了完整的示例，位于 `playerkit-examples/example-locale`。

### **6.1 示例功能**

| 功能 | 说明 |
|-----|------|
| 语言切换 | 演示中/英/日三种语言的实时切换（日语通过 `values-ja/strings.xml` 新增） |
| 变更监听 | 演示语言变更监听器的注册与回调 |

### **6.2 运行示例**

在 Demo App 中选择「Locale」示例查看效果。
