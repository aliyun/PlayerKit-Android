# **场景类型（SceneType）**

**场景类型 (SceneType)** 是 AliPlayerKit 的播放场景抽象机制，通过指定不同场景自动适配 UI 行为、手势控制和插槽可见性。

> 💡 **大多数开发者不需要关心此配置**：如果你是最普通的点播场景，无需设置 SceneType，默认即为 `SceneType.VOD`，具备完整的播放能力。只有在直播、受限播放等**非点播场景**时，才需要显式指定。

---

## **1. 场景概览**

| 场景 | 说明 | 适用业务 | 与 VOD 的差异 |
|------|------|---------|---------------|
| `VOD` | 默认场景，无需显式指定。支持全部控制能力 | 常规点播、课程、媒体库 | — |
| `LIVE` | 实时流无固定时长，自动禁用时间轴操作 | 直播、赛事、电商直播 | 禁用进度拖拽/快进/倍速；隐藏 `cover`（封面图） |
| `VIDEO_LIST` | 禁用垂直手势，避免与列表滚动冲突 | 信息流、短视频列表 | 禁用音量/亮度垂直手势；其余与 VOD 一致 |
| `RESTRICTED` | 防跳播，确保用户按序完整观看 | 考试监控、培训课程 | 禁用进度拖拽/快进/倍速；隐藏 `coverImage`、`seekThumbnail` |
| `MINIMAL` | 纯净画面，所有控制需自行实现 | 背景播放、完全自定义 UI | 隐藏所有控制组件和手势；仅 `playerSurface`/`playState`/`overlays` 可见 |
| `AI_VOD` | AI 增强点播，侧重 AI 内容展示与理解 | AI 课程、知识视频 | 新增 AI 内容展示（摘要、分段）；章节导航为通用能力，不限于此场景 |

> 📖 **章节能力是通用能力**：除 `LIVE` / `MINIMAL` 外均可使用，各场景差异详见 [3.4 章节能力与场景](#34-章节能力与场景)。

---

## **2. 配置方式**

场景类型通过 `AliPlayerModel.Builder` 的 `sceneType()` 方法指定。**默认 VOD 场景无需设置**，只有非点播场景才需要显式指定。

**直播场景**：

```java
// 构建播放数据 —— 调用 sceneType()，默认即为 VOD
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
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


| 插槽 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL | AI_VOD |
|-----|-----|------|------------|------------|---------|--------|
| playerSurface | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| playState | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| overlays | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| coverImage | ✓ | ✗ | ✓ | ✗ | ✗ | ✓ |
| seekThumbnail | ✓ | ✗ | ✓ | ✗ | ✗ | ✓ |
| playControl | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| topBar | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| bottomBar | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| settingMenu | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| gestureControl | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |

### **3.2 手势行为差异**

| 手势 | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL | AI_VOD |
|-----|-----|------|------------|------------|---------|--------|
| 单击（显示/隐藏控制栏） | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| 双击（播放/暂停） | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| 长按（2倍速） | ✓ | ✗ | ✓ | ✗ | ✗ | ✓ |
| 水平拖动（进度跳转） | ✓ | ✗ | ✓ | ✗ | ✗ | ✓ |
| 左侧垂直拖动（亮度） | ✓ | ✓ | ✗ | ✓ | ✗ | ✓ |
| 右侧垂直拖动（音量） | ✓ | ✓ | ✗ | ✓ | ✗ | ✓ |

### **3.3 Seek 能力与拦截边界**

以上手势表中的「水平拖动（进度跳转）」只是交互层表现。跳转有两种通路：一是 `PlayerCommand.Seek` 事件通道——进度条拖拽、章节标记点 / 胶囊 / 面板点击，以及自定义 Slot 发送 `PlayerCommand.Seek` 事件（`BaseSlot.postEvent(...)`）都走这里，事件统一进入 `AliPlayerController` 的 Seek 命令入口并由场景门卫判定；二是业务层直接调用 `controller.getPlayer().seekTo()`（`IMediaPlayer`），跳过命令通道，不经过场景门卫。

| 场景 | 交互入口跳转 | 章节入口跳转 | 命令层拦截 | 说明 |
|------|-------------|-------------|-----------|------|
| VOD | ✓ | ✓ | 否 | 正常跳转 |
| LIVE | ✗ | —（不展示章节） | 是 | 直播无固定时间轴，无法 Seek |
| VIDEO_LIST | ✓ | ✓ | 否 | 正常跳转 |
| RESTRICTED | ✗ | ✗（展示但不跳转） | 是 | 禁止 Seek，确保按序完整观看 |
| MINIMAL | ✗（无内置 UI） | — | 否 | 无 UI 但支持 Seek，自定义控制发起的跳转正常生效 |
| AI_VOD | ✓ | ✓ | 否 | 正常跳转 |

- **拦截表现**：请求被直接丢弃，仅输出一条 `Seek blocked, sceneType=…` 告警日志；不抛异常、不回调业务，播放进度保持不变
- **章节联动**：`RESTRICTED` 下章节仅作展示，点击不发起跳转、不更新章节高亮、不提示「已跳转」
- **被拦截的通路**：自定义 Slot 发送的 `PlayerCommand.Seek` 事件（`BaseSlot.postEvent(...)`）与内置插槽的进度条 / 章节入口是同一个事件通道，`LIVE` / `RESTRICTED` 下事件被丢弃
- **不受限的通路**：业务层直接调用 `controller.getPlayer().seekTo()`（`IMediaPlayer`），不经过命令通道，无门槛、不受场景限制；该接口适合业务自行掌控可播区间，不应作为绕过防跳播的手段

### **3.4 章节能力与场景**

章节能力是**通用能力**，不绑定特定场景：只要章节数据非空，除 `LIVE` / `MINIMAL` 外的场景都会展示章节入口，差别只在「是否允许通过章节跳转」。

| 场景 | 章节入口展示 | 章节点击跳转 | 说明 |
|------|-------------|-------------|------|
| VOD | ✓ | ✓ | 通用点播，章节正常使用 |
| LIVE | ✗ | — | 直播无固定时间轴，章节能力不生效 |
| VIDEO_LIST | ✓ | ✓ | 与 VOD 一致 |
| RESTRICTED | ✓ | ✗ | 仅作内容结构展示，点击不跳转、不更新高亮、不提示 |
| MINIMAL | ✗ | — | 无内置 UI，章节能力随之不生效 |
| AI_VOD | ✓ | ✓ | 与 VOD 一致，额外承载 AI 内容展示 |

**入口范围**（一次数据写入联动全部入口）：

- 进度条章节标记点（竖屏 / 横屏）：仅当章节起始时间位于 `(0, duration)` 内时绘制，单章且从 0 开始时无标记点
- 竖屏章节胶囊列表
- 横屏「第 N 章 · 标题」行与「下一章」（仅一章时「下一章」按钮仍展示，点击不响应）
- 横屏章节侧滑面板（`SlotType.CHAPTER_PANEL`）

**数据来源**（两种方式写同一份数据）：配置期通过 `AliPlayerModel.Builder.chapters()` 传入初始值；配置完成后可通过 `AliPlayerController.setChapters()` 随时写入并立即生效，传入 `null` 或空列表即清空。章节数据为空时上述入口全部不渲染，界面等同普通播放器，因此「先起播、后补章节」是安全路径，接入方式详见 [AI 教育场景 - 章节数据异步更新（懒加载）](../scenes/AiEducation.md#84-章节数据异步更新懒加载)。

> 💡 章节点击与进度条拖拽一样走 `PlayerCommand.Seek` 事件通道，`RESTRICTED` 下点击被命令层丢弃的具体表现见 [3.3 Seek 能力与拦截边界](#33-seek-能力与拦截边界)。

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

### **5.3 AI_VOD 场景如何使用？**

`AI_VOD` 侧重 AI 内容展示（视频摘要、分段讲解）；章节导航本身是通用能力，不限于 `AI_VOD`——任何非 `LIVE` / `MINIMAL` 场景只要章节数据非空就会展示章节入口。通过 `chapters()` 配置章节数据：

```java
// 构建包含章节数据的 AI 播放模型
List<ChapterInfo> chapters = ...; // 从 AI 内容分析获取
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
    .sceneType(SceneType.AI_VOD)
    .chapters(chapters)
    .build();

controller.configure(playerModel);
playerView.attach(controller);
```

详细接入指南请参考 [AI 教育场景](../scenes/AiEducation.md)。
