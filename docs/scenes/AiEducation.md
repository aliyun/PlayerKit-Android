# **AI 教育场景化解决方案**

---

## **1. 场景介绍**

**AI 教育场景**是阿里云基于 AliPlayerKit 与视频点播 VOD 打造的一体化 AI 视频解决方案，适用于**在线教育、课程培训和知识传播**等业务。

它以播放器为统一入口，由视频点播 VOD 提供视频内容与服务支撑，并融合 AI 内容理解能力，自动生成摘要、章节和知识点。用户可以快速了解内容脉络、定位重点片段并随时回看，让长视频从“从头看到尾”转变为“按需浏览、快速学习”。

无论是在线课程、职业培训、企业学习，还是知识付费和学术讲座，都可以借助该方案提升学习效率和观看体验，盘活存量视频内容，并降低播放器与 AI 功能的接入和建设成本。

![AiEducation](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/ai-education-ui-preview.png)

---

## **2. 核心能力**

AI 教育场景围绕“**快速理解、精准定位和高效回看**”提供以下能力：

| 能力       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| 智能章节   | 自动识别视频内容结构并生成章节，在进度条和章节列表中同步展示；点击章节即可跳转到对应片段 |
| 内容摘要   | 提供视频整体摘要和章节摘要，帮助用户快速了解主要内容与章节要点 |
| 知识点提炼 | 自动提取各章节的关键知识点，并以结构化方式展示，便于学习和复习 |
| 章节缩略图 | 为章节展示代表性画面，帮助用户快速识别和定位目标内容         |
| 横竖屏适配 | 根据屏幕方向自动调整交互布局：竖屏显示底部面板，横屏显示右侧面板 |

---

## **3. 前提条件**

开始集成前，请完成以下准备：

| 准备项     | 要求                                                         |
| ---------- | ------------------------------------------------------------ |
| 阿里云服务 | 已开通[视频点播 VOD](https://www.aliyun.com/product/vod) 和[智能媒体服务 IMS](https://www.aliyun.com/product/apsaravideo/ice) |
| 媒体资源   | 已准备测试视频，并完成 AI 内容分析                           |
| App Server | 已完成部署，可正常提供播放凭证和 AI 分析数据；可参考开源项目 [VodAppServer](https://github.com/MediaBox-Demos/VodAppServer) |

完成以上准备后，在 `AiContentConstants` 类中配置 App Server 地址和测试视频 ID，即可开始体验 AI 教育场景。

---

## **4. 快速接入**

### **4.1 通用集成**

AI 教育场景基于 PlayerKit 场景层构建。如尚未完成通用集成步骤，请先参阅：

- [集成准备](../Integration.md) — 场景层集成（拷贝 `playerkit` 和 `scene-ai-education` 模块并完成项目注册与依赖调整）
- [快速开始](../QuickStart.md) — 场景层接入（启动场景页面）

通用集成完成后，仅需以下额外配置即可体验 AI 教育场景：

### **4.2 配置 App Server 和测试视频**

打开 `AiContentConstants.java`，修改以下配置：

> 💡 可直接使用官方提供的 App Server 地址进行体验，但更建议部署自有服务后使用自己的地址。部署方式请参考 [VodAppServer](https://github.com/MediaBox-Demos/VodAppServer)。

```java
public static final String APP_SERVER_BASE_URL = "http://your-server-host:port";

public static final String DEFAULT_MEDIA_ID = "your-media-id";
```

### **4.3 验证接入**

AI 教育场景对应信息：

| 项目 | 值 |
|------|------|
| Activity | `AiEducationActivity` |
| Schema | `playerkit://scenes/aieducation` |

页面启动后，视频将自动加载。播放器显示章节入口按钮，点击后可以查看章节列表和 AI 分析内容，说明接入成功。

---

## **5. 架构概览**

AI 教育场景由客户端、App Server、ICE / AI 工作流和 VOD 服务共同组成。

整体链路分为两个阶段：

- **生产阶段**：App Server 触发 AI 分析工作流，ICE 获取媒资并完成章节提取、摘要生成和知识点分析，随后将结构化结果回写至 VOD。
- **消费阶段**：客户端通过 App Server 获取播放凭证和 AI 分析结果，并由 PlayerKit 完成视频播放、章节跳转和 AI 内容展示。

### **5.1 全局业务流程**

全局业务流程描述 AI 分析结果从生产、存储到客户端消费的完整链路。

其中，VOD 负责媒资及 AI 分析结果的统一存储；ICE / AI 工作流负责内容理解和结构化分析；App Server 对服务端能力进行封装，并向客户端提供统一接口。

![ProductArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/ProductArchitecture.png)

### **5.2 客户端交互流程**

客户端交互流程描述用户进入 AI 教育播放页后，客户端、PlayerKit 与 App Server 之间的调用关系。

页面初始化时，客户端并行或依次获取播放凭证和 AI 分析结果；数据加载完成后，用户可以通过章节内容面板查看章节、摘要和知识点，并跳转到对应的视频位置。

![CallSequence](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/CallSequence.png)

当 AI 分析接口请求失败或结果为空时，客户端仍可使用播放凭证完成普通视频播放，但不展示章节和 AI 分析面板。

### **5.3 客户端分层架构**

客户端采用场景层、PlayerKit 能力层和播放器 SDK 分层设计。

场景层负责页面编排、数据加载和 AI 内容展示；PlayerKit 通过 Slot 机制提供播放器控制栏入口及面板扩展能力；底层播放器 SDK 负责媒资播放、进度控制和播放状态回调。

![LayeredArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/LayeredArchitecture-Android.png)

**组件职责**：

| 组件                             | 所属层级 | 说明                                                         |
| ------------------------------ | -------- | ------------------------------------------------------------ |
| `AiEducationActivity`          | 场景层   | 场景入口（组合根），编排数据加载、播放器初始化、Slot 注册以及 AI 内容推入 |
| `AiContentLoader`              | 场景层   | 在后台线程请求播放凭证和 AI 分析结果，并在主线程回调处理结果 |
| `ChapterButtonSlot`            | 场景层   | 通过 `CustomSlotType` 注册的自定义插槽，在播放器控制栏中展示「章节」按钮，并触发章节内容面板的展开或关闭 |
| `ChapterContentPanelSlot`      | 场景层   | 面板控制器，负责管理 `DialogFragment` 的展示、关闭和状态同步；AI 内容由场景层经 `updateAiContent()` 推入 |
| `ChapterContentDialogFragment` | 场景层   | AI 内容面板 UI，展示章节列表、视频摘要、章节摘要和知识点     |
| `MarkdownBinder`               | 场景层   | 将 AI 分析内容的 Markdown 文本渲染为富文本（基于 Markwon）   |
| `ChapterPanelSlot`             | PlayerKit | 组件层内置的章节面板，全屏下从右侧滑入展示章节列表并支持点击跳转（`SceneType.AI_VOD` 场景，由底部控制栏的章节按钮触发） |
| `AliPlayerController`          | PlayerKit | 提供视频加载、播放控制、Seek 和播放进度回调等基础能力        |

> 💡 场景层的 `ChapterContentDialogFragment` 承载完整的 AI 内容（章节 + 摘要 + 知识点），是 AI 教育场景的差异化 UI；组件层内置的 `ChapterPanelSlot` 只负责纯章节导航，供未接入 AI 分析内容的 `SceneType.AI_VOD` 播放直接复用。两者互不依赖，可按业务需要择一使用。

---

## **6. 服务端接口**

### **6.1 获取播放凭证**

| 项目 | 值 |
|------|---|
| 方法 | GET |
| 路径 | `/appServer/GetVideoPlayAuth` |
| 参数 | `videoId`（必填，视频 ID） |

**请求示例**：

```
GET http://your-server-host:port/appServer/GetVideoPlayAuth?videoId=201558617a9e71f1bfb9e6e7e5780102
```

**响应示例**：

```json
{
  "code": 0,
  "success": true,
  "data": {
    "videoId": "your-video-id",
    "playAuth": "eyJ..."
  }
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | `Integer` | 业务状态码，0 表示成功 |
| `success` | `Boolean` | 请求是否成功 |
| `data.videoId` | `String` | 视频 ID |
| `data.playAuth` | `String` | Base64 编码的播放授权凭证 |

### **6.2 获取 AI 分析内容**

| 项目 | 值 |
|------|---|
| 方法 | GET |
| 路径 | `/GetMediaAiAnalysisByResultType` |
| 必填参数 | `MediaId`（视频 ID） |
| 可选参数 | `ResultType`（`Chapter` / `Summary`，不传返回全部） |

**请求示例**：

```
GET http://your-server-host:port/GetMediaAiAnalysisByResultType?MediaId=201558617a9e71f1bfb9e6e7e5780102
```

**响应示例**：

```json
{
  "AiAnalysisResult": [
    {
      "ResultType": "Chapter",
      "Title": "视频章节",
      "chapterContentItems": [
        {
          "ChapterId": 1,
          "StartTime": 0,
          "EndTime": 120000,
          "ChapterTitle": "课程导论",
          "Summary": "本章介绍了课程的整体框架...",
          "ThumbnailUrl": "https://example.com/thumb1.jpg",
          "KnowledgePoints": [
            { "PointId": 1, "Point": "核心概念", "Description": "描述内容..." }
          ]
        }
      ]
    },
    {
      "ResultType": "Summary",
      "Title": "视频摘要",
      "summaryContent": {
        "ParagraphSummary": "本视频主要讲述了...",
        "MarkdownContent": "# 课程摘要\n\n..."
      }
    }
  ]
}
```

> 💡 如需对接自有服务端，只需返回相同 JSON 结构即可。客户端不关心服务端的实现方式。

**Chapter 字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `ChapterId` | `Integer` | 章节 ID |
| `StartTime` | `Long` | 章节开始时间（毫秒） |
| `EndTime` | `Long` | 章节结束时间（毫秒） |
| `ChapterTitle` | `String` | 章节标题 |
| `Summary` | `String` | 章节文字摘要 |
| `ThumbnailUrl` | `String` | 章节缩略图 URL |
| `KnowledgePoints[].PointId` | `Integer` | 知识点 ID |
| `KnowledgePoints[].Point` | `String` | 知识点名称 |
| `KnowledgePoints[].Description` | `String` | 知识点描述 |

**Summary 字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `ParagraphSummary` | `String` | 段落摘要文本 |
| `MarkdownContent` | `String` | Markdown 格式内容 |

**接口协议**：

| 项目 | 说明 |
|------|------|
| HTTP 超时 | 5 秒（connect + read） |
| 错误处理 | 接口异常时降级为普通播放模式（无 AI 面板） |
| 时间单位 | `StartTime` / `EndTime` 为毫秒 |

---

## **7. 数据模型与映射**

### **7.1 三层映射**

| 服务端 JSON 字段 | 客户端 DTO | 业务模型 | 说明 |
|-----------------|-----------|---------|------|
| `ChapterId` | `ChapterContentItem.chapterId` | `ChapterInfo.id` | 转为 String |
| `StartTime` | `ChapterContentItem.startTime` | `ChapterInfo.startMs` | 毫秒 |
| `EndTime` | `ChapterContentItem.endTime` | `ChapterInfo.endMs` | 毫秒 |
| `ChapterTitle` | `ChapterContentItem.chapterTitle` | `ChapterInfo.title` | |
| `ThumbnailUrl` | `ChapterContentItem.thumbnailUrl` | `ChapterInfo.thumbnailUrl` | |
| `Summary` | `ChapterContentItem.summary` | `AiSegment.summary` | 章节摘要 |
| `KnowledgePoints[]` | `ChapterContentItem.knowledgePoints` | `AiSegment.markdown` | 拼接为 Markdown |
| `ParagraphSummary` | `SummaryContent.paragraphSummary` | `AiSummary.description` | 视频全局摘要 |

### **7.2 聚合模型**

最终聚合为 `AiVideoContent`，包含：

| 字段 | 类型 | 说明 |
|------|------|------|
| `videoId` | `String` | 视频 ID |
| `type` | `@AiContentType int` | 内容类型（决定面板渲染模式） |
| `summary` | `AiSummary` | 视频级摘要（可为 null） |
| `chapters` | `List<ChapterInfo>` | 章节目录列表 |
| `segments` | `List<AiSegment>` | 分段解析列表 |

---

## **8. 自定义与扩展**

### **8.1 替换数据源**

内置的 `AiEducationActivity` 通过 `AiContentLoader` 加载数据。如需使用自己的数据源，可参考 Activity 的实现流程，替换数据加载逻辑：

```java
// 自定义加载示例
AiContentLoader loader = new AiContentLoader();
loader.loadPlayAuth(vid, playAuth -> {
    // 获取到 playAuth 后继续加载 AI 内容...
});
loader.load(vid, aiContent -> {
    // 获取到 AiVideoContent 后配置播放器，并推入面板...
});
```

> 💡 章节数据（`chapters`）可以随 `AliPlayerModel` 下发；视频摘要和章节分段等 AI 内容无法承载在 `AliPlayerModel` 上，需由场景层调用 `ChapterContentPanelSlot.updateAiContent()` 推入。

### **8.2 自定义 UI**

- 替换章节内容面板样式：继承 `ChapterContentDialogFragment` 覆盖布局
- 扩展 Slot：通过 `CustomSlotType` 注册自定义插槽
- 详见 [插槽系统](../advanced/SlotSystem.md)

### **8.3 集成到已有 Activity**

如果不想使用内置的 `AiEducationActivity`，可以参考其实现，在自己的 Activity 中：

1. 初始化 `AliPlayerController`
2. 注册 `ChapterButtonSlot` + `ChapterContentPanelSlot`（注册时保存面板插槽实例）
3. 加载 PlayAuth 和 AI 内容
4. 配置播放器并挂载，挂载后把 AI 内容推给面板插槽

**示例骨架**：

```java
public class MyActivity extends AppCompatActivity {

    @Nullable
    private ChapterContentPanelSlot mChapterContentPanelSlot;

    private void registerSlots() {
        SlotManager slotManager = playerView.getSlotManager();
        slotManager.register(ChapterButtonSlot.TYPE, parent -> new ChapterButtonSlot(parent.getContext()));
        slotManager.register(ChapterContentPanelSlot.TYPE, parent -> {
            // 保存插槽实例，attach 后推入 AI 内容
            mChapterContentPanelSlot = new ChapterContentPanelSlot(parent.getContext());
            return mChapterContentPanelSlot;
        });
    }

    private void configurePlayer(@NonNull String vid, @NonNull String playAuth,
                                 @Nullable AiVideoContent aiContent) {
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(VideoSourceFactory.createVidAuthSource(vid, playAuth))
                .sceneType(SceneType.AI_VOD)
                .chapters(aiContent != null ? aiContent.getChapters() : null)
                .build();

        playerController.configure(playerModel);
        playerView.attach(playerController);

        // attach 后由本页面（组合根）把 AI 内容推给面板插槽
        if (mChapterContentPanelSlot != null) {
            mChapterContentPanelSlot.updateAiContent(aiContent);
        }
    }
}
```

---

## **9. 集成 FAQ**

| 问题 | 解决方案 |
|------|---------|
| 播放凭证获取失败 | 检查 App Server 地址是否正确、网络是否连通、VideoId 是否有效 |
| AI 内容为空 | 确认视频已开通 AI 分析；客户端会降级为普通播放模式（无 AI 面板） |
| 如何使用自己的视频 ID | 修改 `AiContentConstants.DEFAULT_MEDIA_ID` 或在设置页面配置 |
| 横竖屏切换面板异常 | 确认 Activity 声明了 `android:configChanges="orientation|screenSize"` |
| 如何关闭某个 AI 能力 | 服务端接口中不返回对应 `ResultType` 即可（如只返回 Chapter 不返回 Summary） |
| SDK 版本兼容性 | 需要阿里云播放器 SDK ≥ 7.16.0，低版本不支持 `SceneType.AI_VOD` |
