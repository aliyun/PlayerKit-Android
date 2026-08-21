# **自定义配置 (Custom Config)**

AliPlayerKit 内置了最佳实践配置，大多数场景无需额外设置即可获得优质体验。当您需要的播放器能力未通过 AliPlayerKit 的接口直接提供时，可通过以下自定义配置机制，直接调用底层 AliPlayer SDK 的 API 来实现。

---

## **1. 配置层级概览**

AliPlayerKit 提供两个层级的自定义配置入口，满足不同粒度的需求：

| 层级 | 接口 | 调用时机 | 典型场景 |
|------|------|----------|----------|
| **全局配置** | `AliPlayerKit.setOnGlobalInit()` | 全局初始化完成后（仅一次） | `AliPlayerGlobalSettings.setOption()`（全局日志级别）、HTTP/2 多路复用等全局行为 |
| **实例配置** | `AliPlayerModel.Builder.onPlayerConfig()` | 每个播放器实例 `setDataSource()` 之后 | `aliPlayer.setConfig()`（缓冲、Referer）、`aliPlayer.setOption()` 等实例行为 |

> **说明**：播放器 SDK 本身分为全局配置和实例配置两种类型。AliPlayerKit 已内置最佳实践配置，但对于未直接透出的原生接口，提供了上述两个回调入口，供开发者自行获取播放器实例并调用。

---

## **2. 全局配置 (Global Config)**

### **2.1 适用场景**

- 设置全局 Option（如全局日志级别）
- 启用 HTTP/2 多路复用
- 其他需要在所有播放器实例创建前生效的全局行为

### **2.2 机制说明**

- 通过 `AliPlayerKit.setOnGlobalInit()` 注册回调
- 回调在 `AliPlayerKit.init()` 内部全局初始化完成后触发，**仅执行一次**
- 此时底层 SDK 已就绪，可安全调用全局 API

### **2.3 使用方式**

在 `Application.onCreate()` 中，调用 `AliPlayerKit.init()` **之前**注册回调：

```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. 注册全局自定义配置回调（在 init 之前调用）
        AliPlayerKit.setOnGlobalInit(() -> {
            // 示例：启用 HTTP/2 多路复用
            AliPlayerGlobalSettings.setOption(
                AliPlayerGlobalSettings.ENABLE_H2_MULTIPLEX, 1
            );
            // 示例：设置全局日志级别
            // AliPlayerGlobalSettings.setOption(...);
        });

        // 2. 初始化（内部会在初始化完成后触发上面注册的回调）
        AliPlayerKit.init(this);
    }
}
```

### **2.4 注意事项**

| 要点 | 说明 |
|-----|------|
| 调用顺序 | `setOnGlobalInit()` 必须在 `init()` **之前**调用，否则回调不会被触发 |
| 执行次数 | 全局配置仅执行一次，后续重复调用 `init()` 不会再次触发 |
| 传 null 取消 | 传入 `null` 可取消已注册的回调 |

---

## **3. 实例配置 (Player Config)**

### **3.1 适用场景**

- 自定义缓冲策略（`maxBufferDuration`、`highBufferDuration` 等）
- 设置 Referer 防盗链
- 配置 HTTP Header
- 设置实例级 Option
- 其他 AliPlayerKit 未直接透出的播放器接口

### **3.2 机制说明**

- 通过 `AliPlayerModel.Builder.onPlayerConfig()` 传入回调
- 在每次 `controller.configure(model)` 时触发
- 回调在 `player.setDataSource(model)` 执行完毕之后同步触发
- 回调参数为 `IMediaPlayer` 实例，需先调用 `player.getInternalPlayer()` 获取底层原生播放器实例，再访问底层 SDK 的配置能力

> **⚠️ 已知限制**：`setDataSource(model)` 内部已隐式调用 `prepare()`，因此 `onPlayerConfig` 回调实际在 `prepare()` **之后**触发，而非之前。这意味着依赖"prepare 前生效"的配置项（如部分缓冲参数）在当前实现下可能无法在首次 prepare 时生效。如有此类强依赖，建议关注后续版本更新。

### **3.3 使用方式**

在构建 `AliPlayerModel` 时，通过 `onPlayerConfig()` 方法传入回调：

```java
AliPlayerModel model = new AliPlayerModel.Builder()
        .videoSource(videoSource)
        .onPlayerConfig(player -> {
            // 先获取底层原生播放器实例
            AliPlayer aliPlayer = player.getInternalPlayer();

            // 示例 1：自定义缓冲策略和 Referer
            PlayerConfig config = aliPlayer.getConfig();
            config.mMaxBufferDuration = 50000;                  // 最大缓冲时长 50s
            config.mHighBufferDuration = 3000;                  // 高水位缓冲 3s
            config.mStartBufferDuration = 500;                  // 起播缓冲 500ms
            config.mReferrer = "https://your-domain.com";       // 设置 Referer 防盗链
            aliPlayer.setConfig(config);

            // 示例 2：设置实例级 Option
            aliPlayer.setOption(AliPlayer.ALLOW_PRE_RENDER, 1);
        })
        .build();

// 配置并播放
controller.configure(model);
```

### **3.4 注意事项**

| 要点 | 说明 |
|-----|------|
| 触发频率 | 每次调用 `controller.configure(model)` 都会触发该回调，适合动态配置场景 |
| 执行时机 | 回调在 `setDataSource()` 之后执行（`setDataSource()` 内部已完成 `prepare()`），见上方已知限制 |
| 回调参数 | `IMediaPlayer` 实例，需通过 `getInternalPlayer()` 获取底层原生播放器实例后再调用底层 SDK 接口 |
| 传 null 可选 | `onPlayerConfig()` 为可选配置，不设置时不影响正常播放 |

---

## **4. 配置时序**

### **4.1 全局配置时序**

```
Application.onCreate()
    │
    ├── AliPlayerKit.setOnGlobalInit(callback)   ← 注册回调
    │
    └── AliPlayerKit.init(context)
            │
            ├── GlobalManager.initialize()    ← 底层 SDK 初始化
            │
            └── callback.onGlobalInit()           ← 触发全局配置回调（仅一次）
```

### **4.2 实例配置时序**

```
controller.configure(model)
    │
    ├── lifecycleStrategy.acquire()               ← 获取播放器实例
    │
    ├── player.setDataSource(model)               ← 配置视频源（内部已隐式调用 prepare()）
    │
    └── onPlayerConfig.onPlayerConfig(player)     ← 触发实例配置回调（此时已在 prepare() 之后）
```

### **4.3 完整生命周期**

```
┌──────────────────────────────────────────────────────────────────┐
│                       Application 启动                           │
│                                                                  │
│  setOnGlobalInit(callback)  ──→  init(context)                  │
│                                      │                           │
│                               GlobalManager                  │
│                                      │                           │
│                               callback.onGlobalInit() ← 全局配置 │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                      播放器实例创建                               │
│                                                                  │
│  AliPlayerModel.Builder()                                        │
│      .videoSource(source)                                        │
│      .onPlayerConfig(callback)  ← 注册实例配置回调                │
│      .build()                                                    │
│                                                                  │
│  controller.configure(model)                                     │
│      │                                                           │
│      ├── setDataSource(model)  ← 内部已隐式调用 prepare()          │
│      └── callback.onPlayerConfig(player)  ← 实例配置（prepare 后） │
└──────────────────────────────────────────────────────────────────┘
```

---

## **5. API 参考**

### **5.1 核心接口**

#### **OnGlobalInitCallback**

全局初始化自定义配置回调接口。

```java
@FunctionalInterface
public interface OnGlobalInitCallback {
    /**
     * 全局初始化完成后的配置回调。
     */
    void onGlobalInit();
}
```

| 方法 | 说明 |
|-----|------|
| `onGlobalInit()` | 全局初始化完成后触发，仅执行一次 |

#### **OnPlayerConfigCallback**

实例级播放器自定义配置回调接口。

```java
@FunctionalInterface
public interface OnPlayerConfigCallback {
    /**
     * 播放器自定义配置回调，在 setDataSource() 之后调用。
     *
     * @param player 播放器实例，需通过 getInternalPlayer() 获取底层实例后访问底层 SDK 配置能力
     */
    void onPlayerConfig(@NonNull IMediaPlayer player);
}
```

| 方法 | 说明 |
|-----|------|
| `onPlayerConfig(player)` | 播放器配置回调，参数为 `IMediaPlayer` 实例 |

### **5.2 注册方法**

| 类 | 方法 | 说明 |
|----|------|------|
| `AliPlayerKit` | `setOnGlobalInit(@Nullable OnGlobalInitCallback)` | 注册全局配置回调，传 `null` 可取消 |
| `AliPlayerModel.Builder` | `onPlayerConfig(@Nullable OnPlayerConfigCallback)` | 设置实例级配置回调 |

### **5.3 相关文件**

| 文件 | 说明 |
|------|------|
| `config/OnGlobalInitCallback.java` | 全局配置回调接口定义 |
| `config/OnPlayerConfigCallback.java` | 实例配置回调接口定义 |
| `AliPlayerKit.java` | 全局配置注册入口（`setOnGlobalInit`） |
| `AliPlayerModel.java` | 实例配置注册入口（Builder 的 `onPlayerConfig`） |
| `AliPlayerController.java` | 配置执行逻辑（`configure` 方法中调用回调） |

---

## **6. 常见问题**

### **6.1 全局配置和实例配置的执行顺序是什么？**

全局配置（`setOnGlobalInit`）在应用启动时执行一次；实例配置（`onPlayerConfig`）在每个播放器实例 `setDataSource()` 之后执行（此时已完成 `prepare()`）。两者互不影响。

### **6.2 如果不设置自定义配置会怎样？**

AliPlayerKit 已内置最佳实践配置，不设置任何自定义配置即可正常使用，适合大多数场景。

### **6.3 实例配置回调中可以做异步操作吗？**

不建议。该回调在主线程同步执行，若在回调中执行耗时操作，会阻塞 `controller.configure()` 的返回及后续播放流程（如 `autoPlay` 场景下的启动播放）。

### **6.4 可以在实例配置中修改视频源吗？**

不建议。视频源在回调前已通过 `setDataSource()` 设置完成，在回调中修改视频源可能导致不可预期的行为。实例配置回调应仅用于设置播放参数（如缓冲策略、Referer 等）。
