Language: 中文简体 | [English](SlotSystem-EN.md)

# **插槽系统 (Slot System)**

**插槽系统 (Slot System)** 是 AliPlayerKit 的核心架构设计。它通过组件化与可插拔机制，将播放器 UI 拆分为多个独立的插槽组件，并由统一的系统进行管理与调度，从而实现播放器界面的解耦、组合与灵活扩展。

---

## **1. 概念介绍**

### **1.1 什么是插槽？**

**插槽 (Slot)** 是播放器 UI 的基本组成单元，每个插槽代表一个独立的界面组件，负责播放器界面中的某一部分功能，例如顶部控制栏、底部进度条、封面图或提示信息等。

所有插槽按照预定义的 **z-order 层级** 进行叠加排列，共同构成完整的播放器界面。每个插槽只关注自身的 UI 展示与交互逻辑，从而实现职责清晰、粒度可控的界面结构。

通过这种设计，播放器 UI 被抽象为一组 **层级化、组件化** 的界面单元，使界面结构更加清晰，也为后续的功能扩展与组件替换提供了良好的基础。

### **1.2 什么是插槽系统？**

**插槽系统 (Slot System)** 是用于统一管理这些插槽组件的架构机制。

它负责定义插槽的 **注册机制、生命周期管理、场景适配以及渲染顺序**，并在播放器运行过程中统一管理插槽的创建、展示、隐藏与销毁。

基于这一机制，开发者可以像搭积木一样灵活构建播放器界面：既可以使用系统提供的默认插槽快速搭建播放器 UI，也可以按需替换或扩展特定插槽，甚至完全自定义所有组件，实现高度定制化的播放器界面。

---

## **2. 功能特性**

### **2.1 解决问题**

- UI 组件耦合度高，难以替换和扩展
- 不同场景需要不同的 UI 组合，缺乏灵活性
- 自定义 UI 需要修改框架源码

### **2.2 核心价值**

插槽系统将播放器组件中的 UI 组件拆离出来，客户可以自主选择使用方式：

| 使用方式 | 说明 | 优势 |
|---------|------|------|
| 使用默认界面 | 播放器组件使用官方默认 UI | 简化接入流程，降低接入成本，低代码接入 |
| 自定义使用 | 替换部分或全部插槽实现 | 满足各类丰富的 UI 需求 |
| 不使用插槽 | 仅使用播放能力 | 纯播放场景，无 UI 依赖 |

**架构优势**：

- **解耦**：UI 组件与播放器核心逻辑分离，职责清晰
- **灵活**：运行时动态组合、替换 UI 组件，无需重启
- **可扩展**：无需修改框架即可自定义 UI，扩展性强
- **关注点分离**：UI 样式与业务逻辑分离，布局定义在 XML 文件中，业务逻辑在 Slot 类中处理

### **2.3 核心能力**

| 能力 | 说明 |
|-----|------|
| 动态组合 | 运行时自由组合 UI 组件 |
| 热替换 | 无需重启即可替换组件实现 |
| 场景适配 | 不同播放场景自动切换 UI 行为 |

---

## **3. 内置组件**

### **3.1 插槽类型**

| 插槽类型 | 说明 | 默认实现 |
|---------|------|---------|
| PLAYER_SURFACE | 视频画面显示，支持多种渲染视图 | DisplayViewSlot / SurfaceViewSlot / TextureViewSlot |
| FULLSCREEN | 全屏管理，处理屏幕方向切换 | FullscreenSlot |
| GESTURE_CONTROL | 手势控制，处理单击/双击/长按/拖动 | GestureControlSlot |
| LANDSCAPE_HINT | 横屏观看提示，引导用户全屏 | LandscapeHintSlot |
| COVER | 封面图，播放前显示视频封面 | CoverSlot |
| CENTER_DISPLAY | 中心显示，显示倍速/亮度/音量等状态 | CenterDisplaySlot |
| PLAY_STATE | 播放状态，显示加载中/错误提示等 | PlayStateSlot |
| LOG_PANEL | 日志面板，调试时显示播放器日志 | LogPanelSlot |
| TOP_BAR | 顶部控制栏，显示返回/标题/设置等 | TopBarSlot |
| BOTTOM_BAR | 底部控制栏，显示播放控制/进度条等 | BottomBarSlot |
| SETTING_MENU | 设置菜单，显示倍速/清晰度/镜像等设置 | SettingMenuSlot |

### **3.2 场景适配**

AliPlayerKit 定义了 5 种播放场景，不同场景下插槽行为自动适配：

| 场景 | 说明 | 典型应用 |
|-----|------|---------|
| VOD | 点播场景，支持所有功能 | 常规视频播放 |
| LIVE | 直播场景，禁用时间轴操作 | 实时直播流 |
| VIDEO_LIST | 列表播放场景，禁用垂直手势 | 信息流、短视频列表 |
| RESTRICTED | 受限播放场景，限制跳跃播放 | 教育培训、考试监控 |
| MINIMAL | 最小化播放场景，仅显示视频画面 | 背景视频、装饰视频 |

**插槽可见性规则**：

| 插槽 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|-----|-----|------|------------|------------|---------|
| PLAYER_SURFACE | ✓ | ✓ | ✓ | ✓ | ✓ |
| FULLSCREEN | ✓ | ✓ | ✓ | ✓ | ✓ |
| GESTURE_CONTROL | ✓ | ✓ | ✓ | ✓ | ✗ |
| LANDSCAPE_HINT | ✓ | ✓ | ✓ | ✓ | ✗ |
| COVER | ✓ | ✗ | ✓ | ✗ | ✗ |
| CENTER_DISPLAY | ✓ | ✓ | ✓ | ✓ | ✗ |
| PLAY_STATE | ✓ | ✓ | ✓ | ✓ | ✓ |
| LOG_PANEL | 可配置 | 可配置 | 可配置 | 可配置 | ✗ |
| TOP_BAR | ✓ | ✓ | ✓ | ✓ | ✗ |
| BOTTOM_BAR | ✓ | ✓ | ✓ | ✓ | ✗ |
| SETTING_MENU | ✓ | ✓ | ✓ | ✓ | ✗ |

**手势行为差异**：

| 手势 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|-----|-----|------|------------|------------|---------|
| 单击 | 显示/隐藏控制栏 | 显示/隐藏控制栏 | 显示/隐藏控制栏 | 显示/隐藏控制栏 | 禁用 |
| 双击 | 切换播放/暂停 | 切换播放/暂停 | 切换播放/暂停 | 切换播放/暂停 | 禁用 |
| 长按 | 2倍速播放 | 禁用 | 2倍速播放 | 禁用 | 禁用 |
| 水平拖动 | 进度跳转 | 禁用 | 进度跳转 | 禁用 | 禁用 |
| 左侧垂直拖动 | 亮度调节 | 亮度调节 | 禁用 | 亮度调节 | 禁用 |
| 右侧垂直拖动 | 音量调节 | 音量调节 | 禁用 | 音量调节 | 禁用 |

**底部控制栏差异**：

| 控件 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|-----|-----|------|------------|------------|---------|
| 播放/暂停按钮 | ✓ | ✓ | ✓ | ✓ | ✗ |
| 进度条 | 可拖拽 | 不可拖拽 | 可拖拽 | 不可拖拽 | ✗ |
| 时间显示 | ✓ | ✓ | ✓ | ✓ | ✗ |
| 刷新按钮 | ✗ | ✓ | ✗ | ✗ | ✗ |
| 全屏按钮 | ✓ | ✓ | ✓ | ✓ | ✗ |

---

## **4. 基础使用**

插槽系统提供三种使用策略，开发者可根据需求选择合适的方式：

| 策略 | 说明 | 适用场景 |
|-----|------|---------|
| 策略一：使用默认界面 | 最简单的使用方式，播放器组件将使用默认界面 | 快速集成、标准播放场景 |
| 策略二：自定义部分插槽 | 只自定义特定插槽，其他使用默认界面 | 局部定制、保留默认交互 |
| 策略三：完全自定义界面 | 自定义所有插槽，创建完全个性化的播放器界面 | 深度定制、品牌专属 UI |

### **4.1 策略一：使用默认界面**

最简单的使用方式，播放器组件将使用默认界面：

```java
// 1. 获取播放器视图
AliPlayerView playerView = findViewById(R.id.player_view);

// 2. 创建控制器和播放数据
AliPlayerController controller = new AliPlayerController(this);
AliPlayerModel model = new AliPlayerModel.Builder()
        .videoSource(videoSource)
        .build();

// 3. 绑定到视图（自动使用默认插槽）
playerView.attach(controller, model);
```

### **4.2 策略二：自定义部分插槽**

只自定义特定插槽，其他使用默认界面。例如，只自定义顶部栏：

```java
// 1. 创建注册表
SlotRegistry registry = new SlotRegistry();

// 2. 只注册需要自定义的插槽
registry.register(SlotType.TOP_BAR, parent -> new MyTopBarSlot(parent.getContext()));

// 3. 绑定时传入注册表（未注册的插槽使用默认实现）
playerView.attach(controller, model, registry);
```

### **4.3 策略三：完全自定义界面**

自定义所有插槽，创建完全个性化的播放器界面：

```java
// 1. 创建注册表
SlotRegistry registry = new SlotRegistry();

// 2. 注册所有插槽
registry.register(SlotType.PLAYER_SURFACE, parent -> new MySurfaceSlot(parent.getContext()));
registry.register(SlotType.COVER, parent -> new MyCoverSlot(parent.getContext()));
registry.register(SlotType.TOP_BAR, parent -> new MyTopBarSlot(parent.getContext()));
registry.register(SlotType.BOTTOM_BAR, parent -> new MyBottomBarSlot(parent.getContext()));
// ... 注册其他插槽

// 3. 绑定时传入注册表
playerView.attach(controller, model, registry);
```

---

## **5. 进阶使用**

### **5.1 如何实现动态切换插槽？**

运行时动态切换插槽实现：

```java
// 获取管理器
ISlotManager slotManager = playerView.getSlotManager();

// 切换 Surface 类型
registry.register(SlotType.PLAYER_SURFACE,
    parent -> new TextureViewSlot(parent.getContext()));
slotManager.rebuildSlots();
```

### **5.2 如何实现自定义插槽？**

AliPlayerKit 提供两种方式实现自定义插槽，开发者可根据需求选择合适的方式。

**方式一：继承 BaseSlot（推荐）**

继承 `BaseSlot` 是最简单的方式，框架已封装好生命周期管理、事件订阅等通用逻辑。

**适用场景**：大多数 UI 插槽，如封面、控制栏、状态显示等。

**Step by Step**：

1. **创建布局文件**

   在 `res/layout/` 目录下创建布局文件：

   ```xml
   <!-- res/layout/my_cover_layout.xml -->
   <?xml version="1.0" encoding="utf-8"?>
   <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
       android:layout_width="match_parent"
       android:layout_height="match_parent">

       <ImageView
           android:id="@+id/cover_image"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:scaleType="centerCrop" />

   </FrameLayout>
   ```

2. **创建 Slot 类**

   继承 `BaseSlot` 并重写必要方法：

   ```java
   public class MyCoverSlot extends BaseSlot {

       public MyCoverSlot(@NonNull Context context) {
           super(context);
       }

       @Override
       protected int getLayoutId() {
           return R.layout.my_cover_layout;  // 返回布局 ID
       }

       @Override
       public void onBindData(@NonNull AliPlayerModel model) {
           // 绑定数据
           ImageView coverImage = findViewByIdCompat(R.id.cover_image);
           Glide.with(getContext()).load(model.getCoverUrl()).into(coverImage);
       }

       @Override
       public void onUnbindData() {
           // 清理资源
           ImageView coverImage = findViewByIdCompat(R.id.cover_image);
           Glide.with(getContext()).clear(coverImage);
       }
   }
   ```

3. **注册使用**

   ```java
   SlotRegistry registry = new SlotRegistry();
   registry.register(SlotType.COVER, parent -> new MyCoverSlot(parent.getContext()));
   playerView.attach(controller, model, registry);
   ```

**示例参考**：`slots/DisplayViewSlot.java`

**方式二：实现 ISlot 接口**

直接实现 `ISlot` 接口可以获得更高的灵活性，但需要自行处理生命周期。

**适用场景**：需要完全控制视图层级，或需要继承特定 View 类型（如 SurfaceView、TextureView）。

**Step by Step**：

1. **创建布局文件**

   与方式一相同，创建对应的布局文件。

2. **创建 Slot 类**

   实现 `ISlot` 接口，并通过组合 `SlotBehavior` 获得插槽能力：

   ```java
   public class MySurfaceSlot extends FrameLayout implements ISlot, ISurfaceProvider {

       // 通过组合获得插槽核心能力
       private final SlotBehavior slotBehavior = new SlotBehavior();
       private SlotHost host;

       public MySurfaceSlot(@NonNull Context context) {
           super(context);
           View.inflate(context, R.layout.my_surface_layout, this);
       }

       @Override
       public void onAttach(@NonNull SlotHost host) {
           // 1. 委托给 SlotBehavior 处理生命周期
           slotBehavior.attach(host);
           // 2. 保存宿主引用
           this.host = host;
           // 3. 设置 Surface
           setupSurfaceProvider(host);
       }

       @Override
       public void onDetach() {
           if (host != null) {
               onSurfaceDestroyed(host);
           }
           slotBehavior.detach();
           host = null;
       }

       @Override
       public void onBindData(@NonNull AliPlayerModel model) {
           // 数据绑定逻辑
       }

       @Override
       public void onUnbindData() {
           // 数据清理逻辑
       }

       @Override
       public void setupSurfaceProvider(@Nullable SlotHost host) {
           // Surface 设置逻辑
       }
   }
   ```

3. **注册使用**

   与方式一相同，通过 `SlotRegistry` 注册。

**示例参考**：
- `slots/SurfaceViewSlot.java`
- `slots/TextureViewSlot.java`

**两种方式对比**

| 特性 | 继承 BaseSlot | 实现 ISlot 接口 |
|-----|--------------|----------------|
| 代码量 | 少，只需关注业务逻辑 | 多，需手动处理生命周期 |
| 灵活性 | 中等，继承 FrameLayout | 高，可继承任意 View 类型 |
| 生命周期 | 框架自动管理 | 需手动委托给 SlotBehavior |
| 推荐场景 | 大多数 UI 插槽 | 需要特殊视图类型 |

### **5.3 如何实现 UI 还原？**

在实际项目中，您可能需要根据设计稿还原播放器 UI。以下是不同场景下的处理方式：

**场景一：官方未提供所需插槽**

如果播放器组件官方未提供您需要的插槽类型，需要自行实现：

1. **创建自定义插槽**

   建议将自定义插槽放在 `ui/slots/custom` 目录下，与官方实现区分：

   ```
   your-module/src/main/java/com/yourpackage/
   └── ui/
       └── slots/
           └── custom/
               ├── MyDanmakuSlot.java      // 弹幕插槽
               ├── MySubtitleSlot.java     // 字幕插槽
               └── MyWatermarkSlot.java    // 水印插槽
   ```

2. **反馈给官方**

   推荐将需求反馈给官方团队，由官方提供统一的插槽实现。这样可以：
   - 统一行业标准
   - 减少重复开发
   - 惠及更多客户

**场景二：官方提供了插槽但不需要**

如果播放器组件官方提供了插槽，但您的场景不需要，有两种处理方式：

*方式一：配置禁用（推荐）*

在 `SlotConstants` 的 `createDefaultConfigs()` 方法中删除对应的配置项：

```java
// 修改前：SlotRegistry 会注入默认组件
configs.add(new SlotConfig.Builder()
        .type(SlotType.LOG_PANEL)
        .excludeScenes(createSet(SceneType.MINIMAL))
        .condition(AliPlayerKit::isLogPanelEnabled)
        .build());

// 修改后：注释或删除该配置，SlotRegistry 将不再注入默认组件
// configs.add(new SlotConfig.Builder()
//         .type(SlotType.LOG_PANEL)
//         ...
//         .build());
```

*方式二：物理删除（不推荐）*

直接删除对应的 Slot 类文件及其相关资源，不建议使用。

**场景三：官方插槽 UI 样式不满足**

如果官方提供了插槽但 UI 样式不满足需求，可以直接修改 XML 布局文件进行 UI 还原。得益于 UI 样式与业务逻辑的关注点分离设计，修改布局文件不会影响业务逻辑。

**还原步骤**：

1. **找到对应的布局文件**

   布局文件位于 `playerkit/src/main/res/layout/` 目录，命名规则为 `layout_{slot_type}.xml`：

   | 插槽类型 | 布局文件 |
   |---------|---------|
   | TOP_BAR | `layout_top_bar_slot.xml` |
   | BOTTOM_BAR | `layout_bottom_bar_slot.xml` |
   | COVER | `layout_cover_slot.xml` |
   | PLAY_STATE | `layout_play_state_slot.xml` |
   | ... | ... |

2. **修改布局文件**

   直接修改 XML 文件中的样式属性，**注意保持控件 ID 不变**：

   ```xml
   <!-- 修改前：官方默认样式 -->
   <LinearLayout
       android:background="#80000000"
       android:padding="8dp">

       <ImageView
           android:id="@+id/iv_back"
           android:layout_width="40dp"
           android:layout_height="40dp" />
   </LinearLayout>

   <!-- 修改后：自定义样式（保持 ID 不变） -->
   <LinearLayout
       android:background="@drawable/custom_top_bar_bg"
       android:padding="12dp">

       <ImageView
           android:id="@+id/iv_back"
           android:layout_width="48dp"
           android:layout_height="48dp"
           android:src="@drawable/ic_custom_back" />
   </LinearLayout>
   ```

3. **架构约束**

   通过以下约束保证自定义代码与官方代码不冲突：
   - 控件 ID 固定，Slot 类通过 ID 查找控件并绑定事件
   - 布局结构可调整，但核心控件必须存在
   - 业务逻辑无需修改，自动适配新布局

4. **升级兼容建议**

   如果需要对 UI 进行大量修改，我们建议采用 **自定义 Slot 策略** 来避免后续升级 AliPlayerKit 时的冲突：

   - **不要直接修改** 官方的布局文件或 Slot 类
   - **创建 V2 版本**，如 `TopBarSlotV2.java` 和 `layout_top_bar_slot_v2.xml`
   - **通过 SlotRegistry 注册** 或 **修改 SlotConstants** 替换默认实现

   ```java
   public class TopBarSlotV2 extends BaseSlot {
   
       @Override
       protected int getLayoutId() {
           return R.layout.layout_top_bar_slot_v2;
       }
   }
   ```


### **5.4 如何实现无头插槽？**

插槽系统的价值不仅在于 UI 组件化，还在于**业务逻辑组件化**。除了渲染界面的 UI 插槽，还支持**无头插槽（Headless Slot）**——只处理业务逻辑、不渲染任何 UI 的插槽。

**示例：全屏管理插槽**

官方提供的 `FullscreenSlot` 就是一个典型的无头插槽，它只负责全屏切换的逻辑管理，不渲染任何界面：

```java
public class FullscreenSlot extends BaseSlot {

    @Override
    protected int getLayoutId() {
        return 0;  // 返回 0 表示无布局，不渲染 UI
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        // 监听全屏切换事件
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof FullscreenEvents.Toggle) {
            // 处理全屏切换逻辑
            toggleFullscreen();
        }
    }

    private void toggleFullscreen() {
        // 纯逻辑处理：修改 Activity 方向、调整系统 UI 等
    }
}
```

**实现方式**：

| 方式 | 说明 | 适用场景 |
|-----|------|---------|
| 继承 `BaseSlot`，`getLayoutId()` 返回 0 | 简单，继承 FrameLayout 但不渲染 | 大多数无头插槽 |
| 实现 `ISlot` 接口 + 组合 `SlotBehavior` | 灵活，完全不继承 View | 纯逻辑组件 |

**设计价值**：

无头插槽让业务逻辑也能享受插槽系统的好处：
- 统一的生命周期管理
- 与播放器状态自动同步
- 可插拔、可替换
- 与 UI 插槽平等协作

---

## **6. 最佳实践**

### **6.1 生命周期管理**

```java
public class MySlot extends BaseSlot {

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        // 初始化视图
    }

    @Override
    public void onDetach() {
        // 清理资源
        super.onDetach();
    }
}
```

### **6.2 事件订阅**

```java
@Override
protected List<Class<? extends PlayerEvent>> observedEvents() {
    return Arrays.asList(PlayerEvents.StateChanged.class);
}

@Override
protected void onEvent(@NonNull PlayerEvent event) {
    if (event instanceof PlayerEvents.StateChanged) {
        // 处理状态变化
    }
}
```

### **6.3 Surface 选择**

| 场景 | 推荐类型 | 原因 |
|-----|---------|------|
| 普通播放 | SurfaceView | 性能更好 |
| 需要动画 | TextureView | 支持变换 |
| 后台播放 | 无 Surface | 节省资源 |

---

## **7. 示例参考**

项目提供了完整的示例，位于 `playerkit-examples/example-slot-system`。

### **7.1 示例功能**

| 功能 | 说明 |
|-----|------|
| SurfaceView 切换 | 适合普通播放场景 |
| TextureView 切换 | 适合需要动画的场景 |
| 空插槽切换 | 适合纯音频播放 |

### **7.2 运行示例**

在 Demo App 中选择「Slot System」示例查看效果。

---

## **8. API 参考**

### **8.1 类结构**

```mermaid
classDiagram
      %% ==================== 核心接口 ====================
      class ISlot {
          <<interface>>
          +onAttach(SlotHost host) void
          +onDetach() void
          +onBindData(AliPlayerModel model) void
          +onUnbindData() void
      }

      class SlotHost {
          <<interface>>
          +getContext() Context
          +getPlayerStateStore() IPlayerStateStore
          +getSurfaceManager() ISurfaceManager
          +postEvent(PlayerEvent event) void
      }

      class ISlotManager {
          <<interface>>
          +rebuildSlots() void
          +getSlotView(SlotType type) View
          +bindData(AliPlayerModel model) void
          +updateSceneType(int sceneType) void
      }

      class ISurfaceManager {
          <<interface>>
          +setDisplayView(AliDisplayView view) void
          +setSurface(Surface surface) void
      }

      class SlotBuilder {
          <<interface>>
          +build(ViewGroup parent) View
      }

      %% ==================== 枚举 ====================
      class SlotType {
          <<enumeration>>
          PLAYER_SURFACE
          FULLSCREEN
          GESTURE_CONTROL
          COVER
          TOP_BAR
          BOTTOM_BAR
          SETTING_MENU
      }

      %% ==================== 核心类 ====================
      class BaseSlot {
          <<abstract>>
          #SlotBehavior slotBehavior
          #getLayoutId() int
          +onAttach(SlotHost host) void
          +onDetach() void
          +getHost() SlotHost
          #postEvent(PlayerEvent event) void
          +show() void
          +hide() void
      }

      class SlotBehavior {
          -SlotHost host
          +attach(SlotHost host) void
          +detach() void
          +getHost() SlotHost
      }

      class SlotRegistry {
          -Map~SlotType, SlotBuilder~ builders
          +register(SlotType type, SlotBuilder builder) void
          +build(SlotType type, ViewGroup parent) View
      }

      class SlotHostLayout {
          -Map~SlotType, View~ slotViews
          -SlotRegistry slotRegistry
          -int sceneType
          +bind(controller, sceneType, registry) void
          +rebuildSlots() void
          +getSlotView(SlotType type) View
      }

      %% ==================== 继承关系 ====================
      ISlot <|.. BaseSlot
      SlotHost <|.. SlotHostLayout
      ISlotManager <|.. SlotHostLayout
      ISurfaceManager <|.. SlotHostLayout

      %% ==================== 组合关系 ====================
      BaseSlot *-- SlotBehavior : contains
      SlotHostLayout *-- SlotRegistry : contains

      %% ==================== 依赖关系 ====================
      ISlot ..> SlotHost : depends on
      SlotBehavior ..> SlotHost : depends on
      SlotRegistry ..> SlotBuilder : uses
      SlotRegistry ..> SlotType : uses
      SlotHostLayout ..> SlotType : uses
```

### **8.2 核心接口**

| 接口/类 | 说明 |
|--------|------|
| `ISlot` | 插槽接口，定义生命周期 |
| `BaseSlot` | 插槽基类，封装通用逻辑 |
| `SlotRegistry` | 注册中心，管理构建器 |
| `ISlotManager` | 管理器，提供重建和查询 |

### **8.3 BaseSlot 方法**

| 方法 | 说明 |
|-----|------|
| `getLayoutId()` | 返回布局资源 ID |
| `getHost()` | 获取插槽宿主 |
| `show()` / `hide()` / `gone()` | 控制可见性 |
| `postEvent(event)` | 发布事件 |

---

## **9. 技术原理**

### **9.1 槽位选择策略**

```mermaid
flowchart TD
    A[请求创建插槽] --> B{自定义注册?}
    B -->|是| C[使用自定义实现]
    B -->|否| D{默认实现?}
    D -->|是| E[使用默认实现]
    D -->|否| F[跳过]
```

**优先级**：自定义实现 > 默认实现 > 跳过

### **9.2 生命周期**

插槽采用双生命周期系统：

```mermaid
stateDiagram-v2
    [*] --> Attached: onAttach
    Attached --> DataBound: onBindData
    DataBound --> DataUnbound: onUnbindData
    DataUnbound --> Detached: onDetach
    Detached --> [*]
```

| 生命周期 | 说明 |
|---------|------|
| View 生命周期 | `onAttach` → `onDetach` |
| Data 生命周期 | `onBindData` → `onUnbindData` |

**与 Android 生命周期的关系**：

插槽生命周期独立于 Android Activity/Fragment 生命周期。如果需要在 App 前后台切换时执行特定操作（如暂停动画、停止轮询），建议通过以下方式处理：

**方式一：监听播放状态事件**

通过 `observedEvents()` 监听播放状态变化，当播放器暂停时停止动画：

```java
@Override
protected List<Class<? extends PlayerEvent>> observedEvents() {
    return Arrays.asList(PlayerEvents.StateChanged.class);
}

@Override
protected void onEvent(@NonNull PlayerEvent event) {
    if (event instanceof PlayerEvents.StateChanged) {
        PlayerState state = ((PlayerEvents.StateChanged) event).newState;
        if (state == PlayerState.PAUSED || state == PlayerState.STOPPED) {
            stopAnimation();  // 暂停动画
        } else if (state == PlayerState.PLAYING) {
            startAnimation();  // 恢复动画
        }
    }
}
```

**方式二：在 Activity/Fragment 中控制**

在 Activity 的 `onPause()`/`onResume()` 中通过插槽管理器获取插槽并调用方法：

```java
@Override
protected void onPause() {
    super.onPause();
    MyAnimationSlot slot = playerView.getSlotManager().getSlot(SlotType.CUSTOM);
    if (slot != null) {
        slot.pauseAnimation();
    }
}

@Override
protected void onResume() {
    super.onResume();
    MyAnimationSlot slot = playerView.getSlotManager().getSlot(SlotType.CUSTOM);
    if (slot != null) {
        slot.resumeAnimation();
    }
}
```

### **9.3 单向数据流**

插槽系统采用单向数据流架构，插槽不直接持有控制器引用，而是通过宿主提供的接口进行解耦交互：

```
Controller → State / Event → Slot（状态下行，只读）
Slot → Command → Controller（命令上行，只发）
```

- **状态下行**：两种方式获取状态
  - **主动查询**：通过 `getPlayerStateStore()` 查询当前状态
  - **被动监听**：通过订阅事件接收状态变化通知
- **命令上行**：插槽通过 `postEvent()` 发送命令，由控制器执行，插槽不关心执行细节

**主动查询状态**：通过 `host.getPlayerStateStore()` 获取播放器状态的只读访问：

```java
@Override
public void onAttach(@NonNull SlotHost host) {
    super.onAttach(host);

    // 查询播放状态
    PlayerState state = host.getPlayerStateStore().getPlayState();

    // 查询当前播放位置和总时长
    long position = host.getPlayerStateStore().getCurrentPosition();
    long duration = host.getPlayerStateStore().getDuration();
}
```

**被动监听状态**：通过订阅事件接收状态变化：

```java
@Override
protected List<Class<? extends PlayerEvent>> observedEvents() {
    return Arrays.asList(
        PlayerEvents.StateChanged.class,    // 播放状态变化
        PlayerEvents.Info.class             // 播放进度更新
    );
}

@Override
protected void onEvent(@NonNull PlayerEvent event) {
    if (event instanceof PlayerEvents.StateChanged) {
        // 处理播放状态变化
        updatePlayPauseIcon(((PlayerEvents.StateChanged) event).newState);
    } else if (event instanceof PlayerEvents.Info) {
        // 处理播放进度更新
        PlayerEvents.Info info = (PlayerEvents.Info) event;
        updateProgress(info.currentPosition, info.bufferedPosition, info.duration);
    }
}
```

**发送命令**：通过 `postEvent()` 发送命令事件触发播放器行为：

```java
// 播放/暂停切换
postEvent(new PlayerCommand.Toggle(mPlayerId));

// 跳转到指定位置
postEvent(new PlayerCommand.Seek(mPlayerId, 30000));

// 设置播放速度
postEvent(new PlayerCommand.SetSpeed(mPlayerId, 1.5f));

// 截图
postEvent(new PlayerCommand.Snapshot(mPlayerId));
```

**设计理念**：这种单向数据流架构彻底解耦了插槽与控制器。插槽无需持有控制器引用，只需关心"查询什么状态"和"发送什么命令"，实现了真正的关注点分离。

### **9.4 插槽间的横向隔离与事件规范**

为保障架构的稳定性，**插槽之间严禁直接获取对方实例或进行点对点通信。**

当一个插槽（如：设置浮层插槽）引发了状态变更，需要影响另一个插槽（如：底部控制栏插槽）时：
1. **单一信源兜转**：发起插槽通过 `postEvent()` 发送事件给 `Controller`。
2. **状态分发下沉**：由 `Controller` 或 `StateStore` 统筹后，散播新的状态事件给所有关注此事件的插槽。

通过这种“**事件上浮，状态下沉**”的 U 型链路，彻底避免了网状的 UI 耦合和死锁。

**事件拦截提醒：** 对于覆盖在页面表层区域的插槽容器，请确保在无需响应手势时处理好 Android 的 `onTouchEvent` 下发，避免遮挡底部 `GestureControlSlot` 的正常手势检测。

---

## **10. 常见问题**

### **10.1 onAttach 什么时候调用？**

调用 `playerView.attach()` 或 `slotManager.rebuildSlots()` 时触发。

### **10.2 自定义插槽注意事项？**

1. 在 `onAttach` 中调用 `super.onAttach(host)`
2. 在 `onDetach` 前清理资源

### **10.3 如何调试？**

使用 `LogHub` 查看日志，TAG 格式：`类名.BaseSlot`

### **10.4 高频崩溃错例**

以下是客户反馈中最常导致崩溃的问题，请务必避免：

#### **错例 1：忘记调用 super.onAttach() 导致生命周期断层**

**错误代码**：

```java
public class MySlot extends BaseSlot {

    @Override
    public void onAttach(@NonNull SlotHost host) {
        // ❌ 忘记调用 super.onAttach(host)
        // 直接初始化视图
        ImageView iv = findViewByIdCompat(R.id.iv_icon);
        iv.setOnClickListener(v -> postEvent(...));  // NullPointerException!
    }
}
```

**崩溃原因**：`super.onAttach(host)` 会初始化 `slotBehavior`、订阅事件、设置 Surface 等。不调用会导致 `getHost()` 返回 null、`postEvent()` 失败、事件订阅无效。

**正确代码**：

```java
@Override
public void onAttach(@NonNull SlotHost host) {
    super.onAttach(host);  // ✅ 必须首先调用
    // 然后再初始化视图
    ImageView iv = findViewByIdCompat(R.id.iv_icon);
    iv.setOnClickListener(v -> postEvent(...));
}
```

---

#### **错例 2：自定义 XML 时 ID 覆盖导致 NullPointerException**

**错误代码**：

```xml
<!-- 自定义布局时，ID 与官方默认 ID 冲突 -->
<LinearLayout ...>
    <!-- 官方使用 @+id/iv_back，你覆盖了它 -->
    <ImageView
        android:id="@+id/iv_back"
        android:src="@drawable/my_icon" />  <!-- 官方期望的是返回按钮 -->
</LinearLayout>
```

**崩溃原因**：Slot 类通过 `findViewByIdCompat(R.id.iv_back)` 查找控件并绑定点击事件。如果布局中缺少该 ID 或 ID 指向了错误的控件类型，会导致强转失败或点击事件绑定到错误的控件。

**正确做法**：

1. **保持核心控件 ID 不变**：如果 Slot 类中使用了某个 ID，布局中必须保留该 ID
2. **类型必须匹配**：ID 对应的控件类型必须与代码中的类型一致

```xml
<!-- ✅ 保持核心 ID 不变 -->
<LinearLayout ...>
    <ImageView
        android:id="@+id/iv_back"  <!-- 保持 ID，但可以改样式 -->
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:src="@drawable/custom_back" />  <!-- 可以换图标 -->
</LinearLayout>
```

---

#### **错例 3：在构造函数中调用 getHost()**

**错误代码**：

```java
public class MySlot extends BaseSlot {

    public MySlot(@NonNull Context context) {
        super(context);
        // ❌ 构造函数中 getHost() 返回 null
        SlotHost host = getHost();  // null!
    }
}
```

**崩溃原因**：插槽实例在构造时还未 attach 到宿主，`getHost()` 返回 null。

**正确做法**：

```java
@Override
public void onAttach(@NonNull SlotHost host) {
    super.onAttach(host);
    // ✅ 在 onAttach 之后才能访问 host
    SlotHost host = getHost();  // 正常获取
}
```

---

#### **错例 4：onDetach 未清理资源导致内存泄漏**

**错误代码**：

```java
public class MySlot extends BaseSlot {

    private Handler handler = new Handler();

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        handler.postDelayed(() -> updateUI(), 1000);  // 延迟任务
    }

    @Override
    public void onDetach() {
        // ❌ 忘记移除延迟任务
        super.onDetach();
    }
}
```

**崩溃原因**：延迟任务持有的 Slot 引用无法释放，导致内存泄漏，且任务执行时 Slot 可能已 detach。

**正确代码**：

```java
@Override
public void onDetach() {
    handler.removeCallbacksAndMessages(null);  // ✅ 清理所有延迟任务
    super.onDetach();
}
```

---

#### **错例 5：事件订阅未在 observedEvents() 中声明**

**错误代码**：

```java
public class MySlot extends BaseSlot {

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        // ❌ 期望收到事件，但未在 observedEvents() 中声明
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        // 永远不会被调用！
    }
}
```

**崩溃原因**：BaseSlot 依赖 `observedEvents()` 返回值来订阅事件，未声明则不会订阅。

**正确代码**：

```java
@Override
protected List<Class<? extends PlayerEvent>> observedEvents() {
    return Arrays.asList(
        PlayerEvents.StateChanged.class,  // ✅ 声明要订阅的事件
        PlayerEvents.Info.class
    );
}

@Override
protected void onEvent(@NonNull PlayerEvent event) {
    // 现在可以正常收到事件了
}
```
