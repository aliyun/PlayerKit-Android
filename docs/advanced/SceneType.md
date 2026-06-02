# **场景类型（SceneType）**

**场景类型 (SceneType)** 是 AliPlayerKit 的播放场景抽象机制，通过指定不同场景自动适配 UI 行为、手势控制和插槽可见性。

> 💡 **大多数开发者不需要关心此配置**：如果你是最普通的点播场景，无需设置 SceneType，默认即为 `SceneType.VOD`，具备完整的播放能力。只有在直播、受限播放等**非点播场景**时，才需要显式指定。

---

## **1. 场景概览**

| 场景 | 说明 | 适用业务 | 与 VOD 的差异 |
|------|------|---------|---------------|
| `VOD` | 默认场景，无需显式指定。支持全部控制能力 | 常规点播、课程、媒体库 | — |
| `LIVE` | 实时流无固定时长，自动禁用时间轴操作 | 直播、赛事、电商直播 | 禁用进度拖拽/快进/倍速；隐藏 `coverImage`、`seekThumbnail` |
| `VIDEO_LIST` | 禁用垂直手势，避免与列表滚动冲突 | 信息流、短视频列表 | 禁用音量/亮度垂直手势；其余与 VOD 一致 |
| `RESTRICTED` | 防跳播，确保用户按序完整观看 | 考试监控、培训课程 | 禁用进度拖拽/快进/倍速；隐藏 `coverImage`、`seekThumbnail` |
| `MINIMAL` | 纯净画面，所有控制需自行实现 | 背景播放、完全自定义 UI | 隐藏所有控制组件和手势；仅 `playerSurface`/`playState`/`overlays` 可见 |

---

## **2. 配置方式**

场景类型通过 `AliPlayerModel.Builder` 的 `sceneType()` 方法指定。**默认 VOD 场景无需设置**，只有非点播场景才需要显式指定。

**直播场景**：

```java
// 构建播放数据 —— 调用 sceneType()，默认即为 VOD
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
    .videoTitle("Live Stream")
    .sceneType(SceneType.LIVE)
    .build();

// 绑定到播放器视图
controller.configure(playerModel);
playerView.attach(controller);
```

---

## **3. 插槽与场景联动**

### **3.1 插槽可见性**

插槽系统根据场景类型自动控制组件可见性：

| 插槽 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|-----|-----|------|------------|------------|---------|
| playerSurface | ✓ | ✓ | ✓ | ✓ | ✓ |
| playState | ✓ | ✓ | ✓ | ✓ | ✓ |
| overlays | ✓ | ✓ | ✓ | ✓ | ✓ |
| coverImage | ✓ | ✗ | ✓ | ✗ | ✗ |
| seekThumbnail | ✓ | ✗ | ✓ | ✗ | ✗ |
| playControl | ✓ | ✓ | ✓ | ✓ | ✗ |
| topBar | ✓ | ✓ | ✓ | ✓ | ✗ |
| bottomBar | ✓ | ✓ | ✓ | ✓ | ✗ |
| settingMenu | ✓ | ✓ | ✓ | ✓ | ✗ |
| gestureControl | ✓ | ✓ | ✓ | ✓ | ✗ |

### **3.2 手势行为差异**

| 手势 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|-----|-----|------|------------|------------|---------|
| 单击（显示/隐藏控制栏） | ✓ | ✓ | ✓ | ✓ | ✗ |
| 双击（播放/暂停） | ✓ | ✓ | ✓ | ✓ | ✗ |
| 长按（2倍速） | ✓ | ✗ | ✓ | ✗ | ✗ |
| 水平拖动（进度跳转） | ✓ | ✗ | ✓ | ✗ | ✗ |
| 左侧垂直拖动（亮度） | ✓ | ✓ | ✗ | ✓ | ✗ |
| 右侧垂直拖动（音量） | ✓ | ✓ | ✗ | ✓ | ✗ |

---

## **4. 最佳实践**

1. **场景类型在创建时确定**：`SceneType` 在 `AliPlayerModel` 创建时指定，播放过程中不可动态切换。如需切换场景，需创建新的 `AliPlayerModel` 并重新绑定
2. **列表场景优先**：当播放器嵌入 RecyclerView 等可滚动容器时，务必使用 `VIDEO_LIST` 场景，避免垂直手势冲突
3. **MINIMAL 需自行处理控制**：选择 `MINIMAL` 场景后，所有播放控制需开发者通过自定义插槽自行实现
4. **高度定制**：当 SceneType 的预设行为不完全满足需求时，可结合插槽系统（Slot System）进一步控制组件的显示/隐藏和自定义布局。详见 [插槽系统](SlotSystem.md)

---

## **5. 常见问题**

### **5.1 VOD 和 VIDEO_LIST 的区别是什么？**

主要区别在于垂直手势：`VIDEO_LIST` 禁用音量/亮度垂直手势，避免与列表容器滚动手势冲突。其余功能与 VOD 一致。

### **5.2 MINIMAL 场景下如何实现自定义控制？**

通过自定义插槽叠加控制 UI：

```java
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
    .sceneType(SceneType.MINIMAL)
    .build();

AliPlayerController controller = new AliPlayerController(this);
controller.configure(playerModel);

SlotManager slotManager = playerView.getSlotManager();
slotManager.register(SlotType.BOTTOM_BAR, parent -> new MyCustomControlSlot(parent.getContext()));
playerView.attach(controller);
```
