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
        "MindMapSummary": [
          { "Title": "核心概念", "Topics": [...] }
        ],
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
| `MindMapSummary` | `Array` | 思维导图摘要列表（递归结构，每项包含 `Title` 和 `Topics`） |
| `MarkdownContent` | `String` | Markdown 格式内容 |

**接口协议**：

| 项目 | 说明 |
|------|------|
| HTTP 超时 | 5 秒（connect + read） |
| 错误处理 | 接口异常时降级为普通播放模式（无 AI 面板） |
| 时间单位 | `StartTime` / `EndTime` 为毫秒 |

---

## **7. 数据模型与映射**

### **7.1 数据映射**

| 服务端 JSON 字段 | 客户端 DTO | 业务模型 | 说明 |
|-----------------|-----------|---------|------|
| `ChapterId` | `ChapterContentItem.chapterId` | `ChapterInfo.id` | 转为 String |
| `StartTime` | `ChapterContentItem.startTime` | `ChapterInfo.startMs` | 毫秒 |
| `EndTime` | `ChapterContentItem.endTime` | `ChapterInfo.endMs` | 毫秒 |
| `ChapterTitle` | `ChapterContentItem.chapterTitle` | `ChapterInfo.title` | |
| `ThumbnailUrl` | `ChapterContentItem.thumbnailUrl` | `ChapterInfo.thumbnailUrl` | |
| `Summary` | `ChapterContentItem.summary` | `KnowledgeInfo.contentMarkdown` | 经三级降级生成 |
| `KnowledgePoints[]` | `ChapterContentItem.knowledgePoints` | `KnowledgeInfo.contentMarkdown` | 拼接为 Markdown（优先级高于 Summary） |
| `ParagraphSummary` | `SummaryContent.paragraphSummary` | `SummaryInfo.paragraphSummary` | 视频全局摘要 |
| `MindMapSummary` | `SummaryContent.mindMapSummary` | `SummaryInfo.mindMapTitles` | 提取顶层 Title 列表 |
| `MarkdownContent` | `SummaryContent.markdownContent` | `SummaryInfo.markdownContent` | Markdown 格式内容 |

### **7.2 展示模型**

最终组装为 `AiContentViewModel`，由 `AiContentAssembler.buildViewModel(AiAnalysisResponse)` 一次性生成，是面板唯一数据入口：

| 字段 / 方法 | 类型 | 说明 |
|------|------|------|
| `chapters` | `List<ChapterInfo>` | 章节列表（空列表表示无章节数据） |
| `knowledgeItems` | `List<KnowledgeInfo>` | 知识点条目列表 |
| `summaryInfo` | `SummaryInfo` | 视频摘要信息（可为 null） |
| `hasChapterData()` | `boolean` | 判断是否包含章节数据 |
| `hasSummaryData()` | `boolean` | 判断是否包含摘要数据 |

`@AiContentType` 不再是聚合模型字段，而是 TAB 级渲染样式枚举（`CHAPTERS_ONLY` / `TEXT_ANALYSIS` / `GRAPHIC_ANALYSIS`），由 `ChapterContentDialogFragment` 内部 `mTab0Type` / `mTab1Type` 推导。

### **7.3 客户端数据架构**

客户端采用分层设计，数据组装与 UI 渲染职责分离。服务端响应以纯 DTO 形式落地，经组装器转换为展示模型后再交给 UI 层消费。

**数据流：**

```
Server JSON → AiAnalysisResponse (DTO) → AiContentAssembler → AiContentViewModel → ChapterContentDialogFragment (UI)
```

**客户端新增模型说明：**

| 类名 | 职责 |
|------|------|
| `AiContentAssembler` | 纯静态无状态组装器，将 DTO 转换为展示模型；公开方法：`buildViewModel(AiAnalysisResponse)` / `buildChapters` / `buildKnowledgeItems` / `buildSummaryInfo` / `hasAnyContent` |
| `AiContentViewModel` | 面板展示层 ViewModel，字段：`chapters`、`knowledgeItems`、`summaryInfo`；方法：`hasChapterData()` / `hasSummaryData()` |
| `KnowledgeInfo` | 知识点信息，字段：`title`、`startMs`、`endMs`、`contentMarkdown`（预格式化：`- **{Point}**：{Description}`） |
| `SummaryInfo` | 视频摘要信息，字段：`paragraphSummary`、`mindMapTitles`（`List<String>`）、`markdownContent` |

**设计原则：**

- **单一职责**：DTO 只做 JSON 映射，Assembler 只做数据转换，UI 只做渲染
- **惰性构建**：ViewModel 在面板展示时才构建，隐藏时释放，避免内存浪费
- **无状态组装**：Assembler 纯静态方法，无实例状态，可任意多次调用
- **三级降级**：KnowledgeInfo.contentMarkdown 生成规则——KnowledgePoints 优先 → Summary 其次 → 都为空则 null

**TAB 组合规则：**

| 数据情况 | TAB 结构 | tab0 类型 | tab1 类型 |
|---------|---------|---------|----------|
| 仅章节（无摘要） | 双 TAB | `CHAPTERS_ONLY` | `TEXT_ANALYSIS` |
| 仅摘要（无章节） | 无 TAB | `GRAPHIC_ANALYSIS` | — |
| 两者都有 | 双 TAB | `CHAPTERS_ONLY` | `GRAPHIC_ANALYSIS` |

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
loader.loadAiAnalysis(vid, response -> {
    // 获取到 AiAnalysisResponse（纯 DTO）后，消费侧需显式调用 AiContentAssembler 组装...
});
```

> 💡 章节数据由 `AiContentAssembler.buildChapters()` 从 DTO 组装后传入 `AliPlayerModel`，且仅非空时下发；视频摘要和章节分段等 AI 内容无法承载在 `AliPlayerModel` 上，需由场景层调用 `ChapterContentPanelSlot.updateAiContent()` 传入。

### **8.2 自定义 UI**

- 替换章节内容面板样式：继承 `ChapterContentDialogFragment` 覆盖布局
- 扩展 Slot：通过 `CustomSlotType` 注册自定义插槽
- 详见 [插槽系统](../advanced/SlotSystem.md)

### **8.3 集成到已有 Activity**

如果不想使用内置的 `AiEducationActivity`，可以参考其实现，在自己的 Activity 中：

1. 初始化 `AliPlayerController`
2. 注册 `ChapterButtonSlot` + `ChapterContentPanelSlot`（注册时保存实例引用）
3. 加载 PlayAuth 和 AI 内容
4. 配置播放器并挂载，挂载后把 AI 内容传给面板插槽

**示例骨架**：

```java
public class MyActivity extends AppCompatActivity {

    @Nullable
    private ChapterContentPanelSlot mChapterContentPanelSlot;
    @Nullable
    private ChapterButtonSlot mChapterButtonSlot;

    private void registerSlots() {
        SlotManager slotManager = playerView.getSlotManager();
        slotManager.register(ChapterButtonSlot.TYPE, parent -> {
            mChapterButtonSlot = new ChapterButtonSlot(parent.getContext());
            return mChapterButtonSlot;
        });
        slotManager.register(ChapterContentPanelSlot.TYPE, parent -> {
            // 保存插槽实例，attach 后传入 AI 内容
            mChapterContentPanelSlot = new ChapterContentPanelSlot(parent.getContext());
            return mChapterContentPanelSlot;
        });
    }

    private void configurePlayer(@NonNull String vid, @NonNull String playAuth,
                                 @Nullable AiAnalysisResponse response) {
        // 消费侧显式调用 Assembler 构建章节列表
        List<ChapterInfo> chapters = response != null ? AiContentAssembler.buildChapters(response) : null;
        boolean hasChapters = chapters != null && !chapters.isEmpty();

        AliPlayerModel.Builder builder = new AliPlayerModel.Builder()
                .videoSource(VideoSourceFactory.createVidAuthSource(vid, playAuth))
                .sceneType(SceneType.AI_VOD);
        if (hasChapters) {
            builder.chapters(chapters);
        }
        AliPlayerModel playerModel = builder.build();

        playerController.configure(playerModel);
        playerView.attach(playerController);

        // attach 后显式把 AI DTO 传给面板插槽
        if (mChapterContentPanelSlot != null) {
            mChapterContentPanelSlot.updateAiContent(response);
        }

        // 根据 AI 内容是否存在控制章节按钮显隐
        boolean chapterAvailable = AiContentAssembler.hasAnyContent(response);
        if (mChapterButtonSlot != null) {
            mChapterButtonSlot.updateChapterAvailable(chapterAvailable);
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
