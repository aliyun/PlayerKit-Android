Language: 中文简体 | [English](QuickStart-EN.md)

> 📚 **推荐阅读路径**
>
> [核心能力](./CoreFeatures.md) → [集成准备](./Integration.md) → **快速开始** → [API 参考](./ApiReference.md)

> 🤖 **AI 友好提示**
>
> **本文档结构清晰、步骤明确，适合 AI 读取、理解与执行；也可作为 Skills 使用，由 AI 辅助完成 AliPlayerKit 的接入与使用。**
>
> **后续我们将推出专门的 Skills，提供更完整的接入支持。**

---

# **AliPlayerKit 快速开始**

只需几步，您就可以轻松实现视频播放功能！

通过极简的 API 设计，帮助您以低代码的方式快速集成视频播放功能，无需直接调用底层播放器 API，也无需自行实现复杂的播放器 UI。

> **注意**：在开始之前，请确保您已经按照 [集成准备](./Integration.md) 完成环境配置和依赖添加。

---

## **1. 接入流程概览**

只需几步，您就可以轻松实现视频播放功能！**AliPlayerKit 提供了极简的 API 设计**，帮助您以低代码的方式快速集成视频播放功能。

下图展示了 **组件层接入** 的流程：

![QuickStart](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/QuickStart.png)

---

## **2. 选择接入方案**

AliPlayerKit 采用 **分层架构设计**。除组件层接入外，还提供 **场景层接入方式**，您可以根据业务需求灵活选择：

| 接入方案 | 模块 | 说明 | 适用场景 |
|---------|------|---------|---------|
| **方案一：组件层接入** | `playerkit` | 使用 `playerkit` 核心模块，提供可配置的播放器 UI 组件 | 需要自定义播放器 UI 或灵活控制播放行为 |
| **方案二：场景层接入** | `playerkit-scenes` | 在组件层基础上，使用完整业务场景解决方案 | 快速实现标准播放场景，如长视频、短视频、直播 |

> **说明**：场景层依赖组件层。若选择场景层接入，需先完成组件层接入。

---

## **3. 全局初始化**

在 `Application.onCreate()` 中调用 `AliPlayerKit.init()` 进行全局初始化。整个应用生命周期只需调用一次，用于初始化播放器内核和全局配置。

* **Java**：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 AliPlayerKit 全局设置
        AliPlayerKit.init(this);
    }
}
```

* **Kotlin**：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 AliPlayerKit 全局设置
        AliPlayerKit.init(this)
    }
}
```

> **说明**：`AliPlayerKit` 还提供其他全局配置接口，如 `setPlayerViewType()`、`setDisableScreenshot()` 等，详见 [API 参考](./ApiReference.md)。

---

## **4. 方案一：组件层接入**

组件层（`playerkit` 模块）提供开箱即用、可配置的播放器 UI 组件。在每个需要播放视频的页面中，按以下步骤实现播放功能。

> **前置条件**：在接入组件层之前，请先完成 [集成准备](./Integration.md) 中的组件层集成步骤，并完成 [全局初始化](#3-全局初始化)。

### **步骤 1：添加播放器视图**

在布局 XML 中添加 `AliPlayerView`：

```xml
<!-- 播放器组件视图 -->
<com.aliyun.playerkit.AliPlayerView
    android:id="@+id/player_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

`AliPlayerView` 可直接放入您的布局中，宽高可根据实际需求设置。如需外层容器（如全屏切换场景），可自行添加 FrameLayout 或其他容器包裹。

### **步骤 2：绑定控制器并播放**

在 Activity/Fragment 中创建控制器、配置数据、绑定视图：

* **Java**：

```java
public class VideoPlayerActivity extends AppCompatActivity {

    private AliPlayerView playerView;
    private AliPlayerController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // 1. 获取播放器视图
        playerView = findViewById(R.id.player_view);

        // 2. 创建播放控制器
        controller = new AliPlayerController(this);

        // 3. 配置播放数据（推荐使用 VidAuth 方式）
        AliPlayerModel model = new AliPlayerModel.Builder()
                .videoSource(VideoSourceFactory.createVidAuthSource(
                        "您的视频 ID",    // 视频 ID
                        "您的播放凭证"    // 播放凭证
                ))
                .coverUrl("https://example.com/cover.jpg")  // 替换为实际封面图地址
                .videoTitle("示例视频")
                .autoPlay(true)
                .build();

        // 4. 绑定控制器和视图
        playerView.attach(controller, model);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 5. 解绑并释放资源
        playerView.detach();
    }
}
```

* **Kotlin**：

```kotlin
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: AliPlayerView
    private lateinit var controller: AliPlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        // 1. 获取播放器视图
        playerView = findViewById(R.id.player_view)

        // 2. 创建播放控制器
        controller = AliPlayerController(this)

        // 3. 配置播放数据（推荐使用 VidAuth 方式）
        val model = AliPlayerModel.Builder()
                .videoSource(VideoSourceFactory.createVidAuthSource(
                        "您的视频 ID",    // 视频 ID
                        "您的播放凭证"    // 播放凭证
                ))
                .coverUrl("https://example.com/cover.jpg")  // 替换为实际封面图地址
                .videoTitle("示例视频")
                .autoPlay(true)
                .build()

        // 4. 绑定控制器和视图
        playerView.attach(controller, model)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 5. 解绑并释放资源
        playerView.detach()
    }
}
```

> **提示**：如果暂时无法获取 **视频 ID** 和 **播放凭证**，可使用 `SceneConstants.java` 中提供的示例数据进行测试，以便快速跑通示例。

### **步骤 3（可选）：处理返回键**

如果您的应用支持全屏播放，需要在 Activity 中处理返回键事件：

* **Java**：

```java
@Override
public void onBackPressed() {
    // 如果处于全屏状态，退出全屏
    if (playerView.onBackPressed()) {
        return;  // 已处理，不执行默认行为
    }
    super.onBackPressed();  // 未处理，执行默认行为
}
```

* **Kotlin**：

```kotlin
@Deprecated("Deprecated in Java")
override fun onBackPressed() {
    // 如果处于全屏状态，退出全屏
    if (playerView.onBackPressed()) {
        return  // 已处理，不执行默认行为
    }
    super.onBackPressed()  // 未处理，执行默认行为
}
```

> **说明**：各组件的完整 API 说明，请参阅 [API 参考](./ApiReference.md)。

**相关参考**：核心接口调用流程请参阅 [核心能力 - 调用时序](./CoreFeatures.md#2.5 调用时序)。

---

## **5. 方案二：场景层接入**

场景层提供针对特定业务场景的完整播放解决方案，如长视频、短视频、直播等。场景层基于组件层封装，集成后即可开箱使用，无需额外开发。

> **前置条件**：在接入场景层之前，请先完成 [集成准备](./Integration.md) 中的场景层集成步骤，并完成 [全局初始化](#3-全局初始化)。

### **步骤 1：选择场景模块**

场景模块位于 `playerkit-scenes` 目录下，当前包含：

| 场景模块 | 说明 |
|---------|------|
| `scene-common` | 场景公共模块（必需，其他场景模块的基础依赖） |
| `scene-longvideo` | **中长视频场景**：提供中长视频播放的完整解决方案 |
| `scene-shortvideo` | **短视频场景**：提供短视频滑动播放的完整解决方案 |
| `scene-live` | **直播场景**：提供直播播放的完整解决方案 |
| `scene-playlist` | **列表播放场景**：提供视频列表播放的完整解决方案 |

### **步骤 2：启动场景页面**

场景模块集成完成后，可通过以下两种方式启动场景页面：

#### **方式一：Intent 跳转**

* **Java**：

```java
// 跳转到中长视频场景
Intent intent = new Intent(this, LongVideoActivity.class);
startActivity(intent);
```

* **Kotlin**：

```kotlin
// 跳转到中长视频场景
val intent = Intent(this, LongVideoActivity::class.java)
startActivity(intent)
```

#### **方式二：Schema 跳转**

场景模块支持通过 Schema 协议启动对应的播放场景。各场景的 Schema 定义在对应模块的 `AndroidManifest.xml` 中，可查看 `<intent-filter>` 的 `<data>` 标签获取具体协议地址。

例如，`AndroidManifest.xml` 中的配置如下：

```xml
<data
    android:host="scenes"
    android:path="/longvideo"
    android:scheme="playerkit" />
```

可以拼接得到对应的 Schema 地址：

```
playerkit://scenes/longvideo
```

* **Java**：

```java
// 跳转到目标场景（替换为实际 Schema）
Intent intent = new Intent(Intent.ACTION_VIEW);
intent.setData(Uri.parse("对应的 Schema 地址"));
startActivity(intent);
```

* **Kotlin**：

```kotlin
// 跳转到目标场景（替换为实际 Schema）
val intent = Intent(Intent.ACTION_VIEW).apply {
    data = Uri.parse("对应的 Schema 地址")
}
startActivity(intent)
```

---

## **6. 示例与扩展**

通过以上步骤，您已经完成了 AliPlayerKit 的基础接入，并成功实现了视频播放。

如需查看更多示例，可参考以下目录：

- `playerkit-examples`：提供常见 API 的使用示例
- `playerkit-scenes`：提供典型播放场景的解决方案

如果您在接入或使用过程中遇到问题，可联系官方技术支持获取帮助。

---

如需查看各组件和接口的详细说明，请参考 **[API 参考](./ApiReference.md)**。