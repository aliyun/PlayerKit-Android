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

### **Step 1：场景层集成**

将 `playerkit` 和 `scene-ai-education` 模块源码拷贝到 Android 工程。

模块拷贝、项目注册和依赖调整方式，请参阅 [集成准备](./docs/Integration.md) 中的“场景层集成”章节。本文不再重复说明通用集成步骤。

### **Step 2：配置 App Server 和测试视频**

打开 `AiContentConstants.java`，修改以下配置：

```java
public static final String APP_SERVER_BASE_URL = "http://your-server-host:port";

public static final String DEFAULT_MEDIA_ID = "your-media-id";
```

### **Step 3：启动 AI 教育场景**

**方式一：Deep Link（推荐）**

* **Java**：

```java
Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("playerkit://scenes/aieducation"));
startActivity(intent);
```

* **Kotlin**：

```kotlin
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("playerkit://scenes/aieducation"))
startActivity(intent)
```

**方式二：显式 Intent**

* **Java**：

```java
Intent intent = new Intent(context, AiEducationActivity.class);
startActivity(intent);
```

* **Kotlin**：

```kotlin
val intent = Intent(context, AiEducationActivity::class.java)
startActivity(intent)
```

页面启动后，视频将自动加载。播放器显示章节入口，点击后可以查看章节列表和 AI 分析内容，说明接入成功。

---

## **5. 架构概览**

### **5.1 业务流程**

![CallSequence](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/CallSequence.png)

### **5.2 分层架构**

![LayeredArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/LayeredArchitecture.png)

**组件职责**：

| 组件                         | 说明                                                        |
| ---------------------------- | ----------------------------------------------------------- |
| `AiEducationActivity`        | 实现 `AiContentRepository` 接口，编排数据加载与播放器初始化 |
| `AiContentLoader`            | 后台线程执行网络请求，主线程回调结果                        |
| `ChapterButtonSlot`          | 播放器控制栏显示「章节」按钮，点击触发面板展开              |
| `ChapterPanelSlot`           | 面板控制器，管理 `DialogFragment` 的展示与状态同步          |
| `ChapterPanelDialogFragment` | 实际面板 UI，含章节列表和 AI 分析 Tab 页                    |

---

## **6. 服务端接口**

### **接口 1：获取播放凭证**

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

### **接口 2：获取 AI 分析内容**

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

实现 `AiContentRepository` 接口，在你的 Activity 中提供数据：

```java
public interface AiContentRepository {
    AiVideoContent getAiVideoContent();
}
```

### **8.2 自定义 UI**

- 替换章节面板样式：继承 `ChapterPanelDialogFragment` 覆盖布局
- 扩展 Slot：通过 `CustomSlotType` 注册自定义插槽
- 详见 [插槽系统](../advanced/SlotSystem.md)

### **8.3 集成到已有 Activity**

如果不想使用内置的 `AiEducationActivity`，可以参考其实现，在自己的 Activity 中：

1. 初始化 `AliPlayerController`
2. 注册 `ChapterButtonSlot` + `ChapterPanelSlot`
3. 实现 `AiContentRepository` 接口
4. 加载数据并配置播放器

**示例骨架**：

```java
public class MyActivity extends AppCompatActivity implements AiContentRepository {

    @Override
    public AiVideoContent getAiVideoContent() {
        // 返回你的 AI 内容数据
        return myAiVideoContent;
    }

    private void setupPlayer() {
        // 1. 初始化播放器
        // 2. 注册 Slot
        // 3. 配置 SceneType.AI_VOD
        // 4. 加载 PlayAuth 并开始播放
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
