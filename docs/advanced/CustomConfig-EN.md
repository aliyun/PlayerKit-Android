# **Custom Config**

AliPlayerKit ships with built-in best-practice configurations that deliver a high-quality experience in most scenarios without any extra setup. When the player capability you need is not directly exposed by AliPlayerKit's APIs, you can use the custom configuration mechanism below to invoke the underlying AliPlayer SDK APIs directly.

---

## **1. Configuration Tiers Overview**

AliPlayerKit provides two tiers of custom configuration entry points to satisfy different granularity needs:

| Tier | API | Invocation Timing | Typical Scenarios |
|------|-----|-------------------|-------------------|
| **Global Config** | `AliPlayerKit.setOnGlobalInit()` | After global initialization completes (only once) | setOption (global log level), HTTP/2 multiplexing, and other global behaviors |
| **Instance Config** | `AliPlayerModel.Builder.onPlayerConfig()` | Before each player instance's `prepare()` | setPlayConfig (buffering, Referer), setOption, and other per-instance behaviors |

> **Note**: The player SDK itself splits configuration into global and instance types. AliPlayerKit has built-in best-practice settings, but for native APIs that are not directly exposed, the two callback entry points above let developers obtain the player instance and invoke them directly.

---

## **2. Global Config**

### **2.1 Use Cases**

- Setting global Options (such as global log level)
- Enabling HTTP/2 multiplexing
- Other global behaviors that must take effect before any player instance is created

### **2.2 Mechanism**

- Register a callback via `AliPlayerKit.setOnGlobalInit()`
- The callback is triggered after global initialization completes inside `AliPlayerKit.init()`, **and runs only once**
- At this point the underlying SDK is ready, so global APIs can be safely invoked

### **2.3 Usage**

In `Application.onCreate()`, register the callback **before** calling `AliPlayerKit.init()`:

```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Register the global custom config callback (must be called before init)
        AliPlayerKit.setOnGlobalInit(() -> {
            // Example: enable HTTP/2 multiplexing
            AliPlayerFactory.setOption(
                AliPlayerGlobalSettings.ENABLE_H2_MULTIPLEX, 1
            );
            // Example: set the global log level
            // AliPlayerFactory.setOption(...);
        });

        // 2. Initialize (the registered callback fires after initialization completes)
        AliPlayerKit.init(this);
    }
}
```

### **2.4 Notes**

| Item | Description |
|------|-------------|
| Call order | `setOnGlobalInit()` must be called **before** `init()`, otherwise the callback will not fire |
| Execution count | The global config runs only once; subsequent calls to `init()` will not trigger it again |
| Pass null to cancel | Passing `null` cancels a previously registered callback |

---

## **3. Instance Config (Player Config)**

### **3.1 Use Cases**

- Custom buffering strategy (`maxBufferDuration`, `highBufferDuration`, etc.)
- Setting Referer for hot-link protection
- Configuring HTTP headers
- Setting per-instance Options
- Any other player APIs not directly exposed by AliPlayerKit

### **3.2 Mechanism**

- Register a callback via `AliPlayerModel.Builder.onPlayerConfig()`
- It is triggered on every `controller.configure(model)` call
- The callback runs **after** `setDataSource()` and **before** `prepare()`
- The callback parameter is an `IMediaPlayer` instance, granting access to all underlying SDK configuration capabilities

### **3.3 Usage**

When building an `AliPlayerModel`, pass the callback via `onPlayerConfig()`:

```java
AliPlayerModel model = new AliPlayerModel.Builder()
        .videoSource(videoSource)
        .onPlayerConfig(player -> {
            // Example 1: custom buffering strategy and Referer
            PlayerConfig config = player.getPlayConfig();
            config.mMaxBufferDuration = 50000;                  // Max buffer 50s
            config.mHighBufferDuration = 3000;                  // High watermark 3s
            config.mStartBufferDuration = 500;                  // Start-up buffer 500ms
            config.mReferrer = "https://your-domain.com";       // Set Referer for hot-link protection
            player.setPlayConfig(config);

            // Example 2: set an instance-level Option
            player.setOption(AliPlayerGlobalSettings.ALLOW_PRE_RENDER, 1);
        })
        .build();

// Configure and play
controller.configure(model);
```

### **3.4 Notes**

| Item | Description |
|------|-------------|
| Trigger frequency | The callback fires on every `controller.configure(model)` call, ideal for dynamic configuration |
| Execution timing | The callback runs after `setDataSource()` and before `prepare()` — the optimal point for custom configuration |
| Callback parameter | The `IMediaPlayer` instance, exposing all underlying SDK APIs |
| Optional | `onPlayerConfig()` is optional; not setting it does not affect normal playback |

---

## **4. Configuration Sequence**

### **4.1 Global Config Sequence**

```
Application.onCreate()
    │
    ├── AliPlayerKit.setOnGlobalInit(callback)   ← Register callback
    │
    └── AliPlayerKit.init(context)
            │
            ├── GlobalManager.initialize()        ← Underlying SDK init
            │
            └── callback.onGlobalInit()           ← Fire global config callback (once)
```

### **4.2 Instance Config Sequence**

```
controller.configure(model)
    │
    ├── lifecycleStrategy.acquire()               ← Acquire player instance
    │
    ├── player.setDataSource(model)               ← Configure video source
    │
    ├── onPlayerConfig.onPlayerConfig(player)     ← Fire instance config callback
    │
    └── player.prepare() / player.start()         ← Start playback
```

### **4.3 Full Lifecycle**

```
┌──────────────────────────────────────────────────────────────────┐
│                       Application Startup                        │
│                                                                  │
│  setOnGlobalInit(callback)  ──→  init(context)                   │
│                                      │                           │
│                               GlobalManager                      │
│                                      │                           │
│                               callback.onGlobalInit() ← Global   │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Player Instance Creation                     │
│                                                                  │
│  AliPlayerModel.Builder()                                        │
│      .videoSource(source)                                        │
│      .onPlayerConfig(callback)  ← Register instance callback     │
│      .build()                                                    │
│                                                                  │
│  controller.configure(model)                                     │
│      │                                                           │
│      ├── setDataSource(model)                                    │
│      ├── callback.onPlayerConfig(player)  ← Instance config      │
│      └── prepare() / start()                                     │
└──────────────────────────────────────────────────────────────────┘
```

---

## **5. API Reference**

### **5.1 Core Interfaces**

#### **OnGlobalInitCallback**

Custom configuration callback invoked after global initialization.

```java
@FunctionalInterface
public interface OnGlobalInitCallback {
    /**
     * Configuration callback fired after global initialization completes.
     */
    void onGlobalInit();
}
```

| Method | Description |
|--------|-------------|
| `onGlobalInit()` | Fires after global initialization, executed only once |

#### **OnPlayerConfigCallback**

Per-instance player configuration callback.

```java
@FunctionalInterface
public interface OnPlayerConfigCallback {
    /**
     * Player configuration callback invoked before prepare().
     *
     * @param player the player instance, exposing the underlying SDK config capabilities
     */
    void onPlayerConfig(@NonNull IMediaPlayer player);
}
```

| Method | Description |
|--------|-------------|
| `onPlayerConfig(player)` | Player configuration callback; the parameter is an `IMediaPlayer` instance |

### **5.2 Registration Methods**

| Class | Method | Description |
|-------|--------|-------------|
| `AliPlayerKit` | `setOnGlobalInit(@Nullable OnGlobalInitCallback)` | Register the global config callback; pass `null` to cancel |
| `AliPlayerModel.Builder` | `onPlayerConfig(@Nullable OnPlayerConfigCallback)` | Set the instance-level config callback |

### **5.3 Related Files**

| File | Description |
|------|-------------|
| `config/OnGlobalInitCallback.java` | Global config callback interface definition |
| `config/OnPlayerConfigCallback.java` | Instance config callback interface definition |
| `AliPlayerKit.java` | Global config registration entry (`setOnGlobalInit`) |
| `AliPlayerModel.java` | Instance config registration entry (`onPlayerConfig` on the Builder) |
| `AliPlayerController.java` | Configuration execution logic (callback invoked in `configure`) |

---

## **6. FAQ**

### **6.1 What is the execution order between global config and instance config?**

The global config (`setOnGlobalInit`) runs once at application startup; the instance config (`onPlayerConfig`) runs before each player instance's `prepare()`. They are independent of each other.

### **6.2 What happens if I do not set any custom configuration?**

AliPlayerKit ships with best-practice defaults — no custom configuration is required for normal use, which suits most scenarios.

### **6.3 Can I perform asynchronous operations inside the instance config callback?**

Not recommended. On Android the callback runs synchronously, and `prepare()` only proceeds after it returns. Performing time-consuming work inside the callback would block player startup.

### **6.4 Can I change the video source inside the instance config?**

Not recommended. The video source is already set via `setDataSource()` before the callback runs; changing it inside the callback may cause unpredictable behavior. The instance config callback should only be used to set playback parameters such as buffering strategy or Referer.
