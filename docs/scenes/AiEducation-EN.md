# **AI Education Scene Solution**

---

## **1. Scene Introduction**

**AI Education Scene** is an integrated AI video solution built by Alibaba Cloud based on AliPlayerKit and ApsaraVideo VOD, designed for **online education, course training, and knowledge dissemination** scenarios.

It uses the player as a unified entry point, with VOD providing video content and service support, and integrates AI content understanding capabilities to automatically generate summaries, chapters, and knowledge points. Users can quickly grasp the content structure, locate key segments, and review them at any time, transforming long videos from "watching from start to finish" to "browsing on demand and learning efficiently."

Whether it's online courses, vocational training, corporate learning, paid knowledge services, or academic lectures, this solution can enhance learning efficiency and viewing experience, revitalize existing video content, and reduce the integration and development costs of player and AI features.

![AiEducation](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/ai-education-ui-preview.png)

---

## **2. Core Capabilities**

The AI Education Scene provides the following capabilities centered on "**quick understanding, precise navigation, and efficient review**":

| Capability | Description |
| ---------- | ------------------------------------------------------------ |
| Smart Chapters | Automatically identifies video content structure and generates chapters, displayed simultaneously on the progress bar and chapter list; click a chapter to jump to the corresponding segment |
| Content Summary | Provides overall video summary and chapter summaries, helping users quickly understand main content and chapter highlights |
| Knowledge Point Extraction | Automatically extracts key knowledge points from each chapter and displays them in a structured format for easy learning and review |
| Chapter Thumbnails | Displays representative frames for chapters, helping users quickly identify and locate target content |
| Orientation Adaptation | Automatically adjusts interaction layout based on screen orientation: bottom panel in portrait mode, side panel in landscape mode |

---

## **3. Prerequisites**

Before starting integration, please complete the following preparations:

| Item | Requirement |
| ---------- | ------------------------------------------------------------ |
| Alibaba Cloud Services | [ApsaraVideo VOD](https://www.alibabacloud.com/en/product/apsaravideo-for-vod) and [Intelligent Media Services (IMS)](https://www.alibabacloud.com/en/product/ims) have been activated |
| Media Resources | Test videos have been prepared and AI content analysis has been completed |
| App Server | Deployment has been completed and can properly provide play credentials and AI analysis data; refer to the open-source project [VodAppServer](https://github.com/MediaBox-Demos/VodAppServer) |

After completing the above preparations, configure the App Server address and test video ID in the `AiContentConstants` class to start experiencing the AI Education Scene.

---

## **4. Quick Start**

### **4.1 Common Integration**

The AI Education Scene is built on the PlayerKit scene layer. If you have not completed the common integration steps yet, start here:

- [Integration Guide](../Integration-EN.md) — Scene layer integration (copy the `playerkit` and `scene-ai-education` modules, then register them and adjust dependencies)
- [Quick Start](../QuickStart-EN.md) — Scene layer usage (launching a scene page)

Once common integration is done, only the following extra configuration is needed to experience the AI Education Scene:

### **4.2 Configure App Server and Test Video**

Open `AiContentConstants.java` and modify the following configuration:

> 💡 You can use the official App Server address to try the scene out, but deploying your own service and using your own address is recommended. See [VodAppServer](https://github.com/MediaBox-Demos/VodAppServer) for deployment instructions.

```java
public static final String APP_SERVER_BASE_URL = "http://your-server-host:port";

public static final String DEFAULT_MEDIA_ID = "your-media-id";
```

### **4.3 Verify the Integration**

Entry information for the AI Education Scene:

| Item | Value |
|------|------|
| Activity | `AiEducationActivity` |
| Schema | `playerkit://scenes/aieducation` |

After the page launches, the video will load automatically. The player displays the chapter entry button; tapping it reveals the chapter list and AI analysis content, indicating successful integration.

---

## **5. Architecture Overview**

The AI Education Scene is composed of the client, App Server, ICE / AI workflow, and VOD services.

The overall pipeline is divided into two phases:

- **Production Phase**: The App Server triggers the AI analysis workflow; ICE fetches media assets and completes chapter extraction, summary generation, and knowledge point analysis, then writes the structured results back to VOD.
- **Consumption Phase**: The client obtains play credentials and AI analysis results through the App Server, and PlayerKit handles video playback, chapter navigation, and AI content display.

### **5.1 Global Business Flow**

The global business flow describes the complete pipeline from production, storage, to client consumption of AI analysis results.

VOD is responsible for unified storage of media assets and AI analysis results; ICE / AI workflow handles content understanding and structured analysis; App Server encapsulates server-side capabilities and provides a unified interface to the client.

![ProductArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/ProductArchitecture-EN.png)

### **5.2 Client Interaction Flow**

The client interaction flow describes the calling relationships between the client, PlayerKit, and App Server after the user enters the AI Education playback page.

During page initialization, the client obtains play credentials and AI analysis results in parallel or sequentially; after data loading is complete, users can view chapters, summaries, and knowledge points through the chapter content panel, and jump to the corresponding video position.

![CallSequence](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/CallSequence-EN.png)

When the AI analysis API request fails or returns empty results, the client can still use the play credentials to complete normal video playback, but will not display the chapter and AI analysis panel.

### **5.3 Client Layered Architecture**

The client adopts a layered design consisting of the Scene Layer, PlayerKit Capability Layer, and Player SDK.

The Scene Layer is responsible for page orchestration, data loading, and AI content display; PlayerKit provides player control bar entry points and panel extension capabilities through the Slot mechanism; the underlying Player SDK handles media playback, progress control, and playback state callbacks.

![LayeredArchitecture](https://alivc-demo-cms.alicdn.com/versionProduct/resources/ai_education/LayeredArchitecture-Android-EN.png)

---

## **6. Server API**

### **6.1 Get Play Credential**

| Item | Value |
|------|---|
| Method | GET |
| Path | `/appServer/GetVideoPlayAuth` |
| Parameter | `videoId` (required, video ID) |

**Request Example**:

```
GET http://your-server-host:port/appServer/GetVideoPlayAuth?videoId=201558617a9e71f1bfb9e6e7e5780102
```

**Response Example**:

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

**Response Field Description**:

| Field | Type | Description |
|------|------|------|
| `code` | `Integer` | Business status code, 0 indicates success |
| `success` | `Boolean` | Whether the request was successful |
| `data.videoId` | `String` | Video ID |
| `data.playAuth` | `String` | Base64-encoded play authorization credential |

### **6.2 Get AI Analysis Content**

| Item | Value |
|------|---|
| Method | GET |
| Path | `/GetMediaAiAnalysisByResultType` |
| Required Parameter | `MediaId` (video ID) |
| Optional Parameter | `ResultType` (`Chapter` / `Summary`, returns all if not specified) |

**Request Example**:

```
GET http://your-server-host:port/GetMediaAiAnalysisByResultType?MediaId=201558617a9e71f1bfb9e6e7e5780102
```

**Response Example**:

```json
{
  "AiAnalysisResult": [
    {
      "ResultType": "Chapter",
      "Title": "Video Chapters",
      "chapterContentItems": [
        {
          "ChapterId": 1,
          "StartTime": 0,
          "EndTime": 120000,
          "ChapterTitle": "Course Introduction",
          "Summary": "This chapter introduces the overall framework of the course...",
          "ThumbnailUrl": "https://example.com/thumb1.jpg",
          "KnowledgePoints": [
            { "PointId": 1, "Point": "Core Concept", "Description": "Description content..." }
          ]
        }
      ]
    },
    {
      "ResultType": "Summary",
      "Title": "Video Summary",
      "summaryContent": {
        "ParagraphSummary": "This video mainly covers...",
        "MindMapSummary": [
          { "Title": "Core Concept", "Topics": [...] }
        ],
        "MarkdownContent": "# Course Summary\n\n..."
      }
    }
  ]
}
```

> 💡 To integrate with your own server, simply return the same JSON structure. The client does not care about the server-side implementation.

**Chapter Field Description**:

| Field | Type | Description |
|------|------|------|
| `ChapterId` | `Integer` | Chapter ID |
| `StartTime` | `Long` | Chapter start time (milliseconds) |
| `EndTime` | `Long` | Chapter end time (milliseconds) |
| `ChapterTitle` | `String` | Chapter title |
| `Summary` | `String` | Chapter text summary |
| `ThumbnailUrl` | `String` | Chapter thumbnail URL |
| `KnowledgePoints[].PointId` | `Integer` | Knowledge point ID |
| `KnowledgePoints[].Point` | `String` | Knowledge point name |
| `KnowledgePoints[].Description` | `String` | Knowledge point description |

**Summary Field Description**:

| Field | Type | Description |
|------|------|------|
| `ParagraphSummary` | `String` | Paragraph summary text |
| `MindMapSummary` | `Array` | Mind map summary list (recursive structure, each item contains `Title` and `Topics`) |
| `MarkdownContent` | `String` | Markdown-formatted content |

**API Protocol**:

| Item | Description |
|------|------|
| HTTP Timeout | 5 seconds (connect + read) |
| Error Handling | Degrades to normal playback mode (no AI panel) when API fails |
| Time Unit | `StartTime` / `EndTime` are in milliseconds |

---

## **7. Data Model & Mapping**

### **7.1 Data Mapping**

| Server JSON Field | Client DTO | Business Model | Description |
|-----------------|-----------|---------|------|
| `ChapterId` | `ChapterContentItem.chapterId` | `ChapterInfo.id` | Converted to String |
| `StartTime` | `ChapterContentItem.startTime` | `ChapterInfo.startMs` | Milliseconds |
| `EndTime` | `ChapterContentItem.endTime` | `ChapterInfo.endMs` | Milliseconds |
| `ChapterTitle` | `ChapterContentItem.chapterTitle` | `ChapterInfo.title` | |
| `ThumbnailUrl` | `ChapterContentItem.thumbnailUrl` | `ChapterInfo.thumbnailUrl` | |
| `Summary` | `ChapterContentItem.summary` | `KnowledgeInfo.contentMarkdown` | Generated via three-level fallback |
| `KnowledgePoints[]` | `ChapterContentItem.knowledgePoints` | `KnowledgeInfo.contentMarkdown` | Concatenated as Markdown (higher priority than Summary) |
| `ParagraphSummary` | `SummaryContent.paragraphSummary` | `SummaryInfo.paragraphSummary` | Video-level summary |
| `MindMapSummary` | `SummaryContent.mindMapSummary` | `SummaryInfo.mindMapTitles` | Extracts top-level Title list |
| `MarkdownContent` | `SummaryContent.markdownContent` | `SummaryInfo.markdownContent` | Markdown-formatted content |

### **7.2 Presentation Model**

The final assembly produces `AiContentViewModel`, generated once by `AiContentAssembler.buildViewModel(AiAnalysisResponse)`, serving as the panel's sole data entry:

| Field / Method | Type | Description |
|------|------|------|
| `chapters` | `List<ChapterInfo>` | Chapter list (empty list means no chapter data) |
| `knowledgeItems` | `List<KnowledgeInfo>` | Knowledge point item list |
| `summaryInfo` | `SummaryInfo` | Video summary info (nullable) |
| `hasChapterData()` | `boolean` | Whether chapter data is present |
| `hasSummaryData()` | `boolean` | Whether summary data is present |

`@AiContentType` is no longer an aggregation model field, but a TAB-level rendering style enum (`CHAPTERS_ONLY` / `TEXT_ANALYSIS` / `GRAPHIC_ANALYSIS`), derived internally by `ChapterContentDialogFragment` via `mTab0Type` / `mTab1Type`.

### **7.3 Client Data Architecture**

The client adopts a layered design with clear separation between data assembly and UI rendering responsibilities. The server response lands as a pure DTO, then the assembler converts it into a presentation model before handing it off to the UI layer.

**Data Flow:**

```
Server JSON → AiAnalysisResponse (DTO) → AiContentAssembler → AiContentViewModel → ChapterContentDialogFragment (UI)
```

**Client Model Descriptions:**

| Class | Responsibility |
|------|------|
| `AiContentAssembler` | Pure static stateless assembler; converts DTO to presentation models; public methods: `buildViewModel(AiAnalysisResponse)` / `buildChapters` / `buildKnowledgeItems` / `buildSummaryInfo` / `hasAnyContent` |
| `AiContentViewModel` | Panel presentation-layer ViewModel; fields: `chapters`, `knowledgeItems`, `summaryInfo`; methods: `hasChapterData()` / `hasSummaryData()` |
| `KnowledgeInfo` | Knowledge point info; fields: `title`, `startMs`, `endMs`, `contentMarkdown` (pre-formatted: `- **{Point}**：{Description}`) |
| `SummaryInfo` | Video summary info; fields: `paragraphSummary`, `mindMapTitles` (`List<String>`), `markdownContent` |

**Design Principles:**

- **Single Responsibility**: DTO handles only JSON mapping, Assembler handles only data conversion, UI handles only rendering
- **Lazy Construction**: ViewModel is built only when the panel shows, released on hide, avoiding memory waste
- **Stateless Assembly**: Assembler uses pure static methods with no instance state, callable any number of times
- **Three-Level Fallback**: KnowledgeInfo.contentMarkdown generation rule — KnowledgePoints first → Summary second → null if both empty

**TAB Combination Rules:**

| Data Scenario | TAB Structure | tab0 Type | tab1 Type |
|---------|---------|---------|----------|
| Chapters only (no summary) | Dual TAB | `CHAPTERS_ONLY` | `TEXT_ANALYSIS` |
| Summary only (no chapters) | No TAB | `GRAPHIC_ANALYSIS` | — |
| Both present | Dual TAB | `CHAPTERS_ONLY` | `GRAPHIC_ANALYSIS` |

---

## **8. Customization & Extension**

### **8.1 Replace Data Source**

The built-in `AiEducationActivity` loads data through `AiContentLoader`. To use your own data source, refer to the Activity's implementation flow and replace the data loading logic:

```java
// Custom loading example
AiContentLoader loader = new AiContentLoader();
loader.loadPlayAuth(vid, playAuth -> {
    // After obtaining playAuth, continue loading AI content...
});
loader.loadAiAnalysis(vid, response -> {
    // After obtaining AiAnalysisResponse (pure DTO), the consumer must explicitly call AiContentAssembler to assemble...
});
```

> 💡 Chapter data is assembled from the DTO by `AiContentAssembler.buildChapters()` and passed to `AliPlayerModel` only when non-empty; AI content such as the video summary and chapter segments cannot be carried on `AliPlayerModel`, so the scene layer must pass it in via `ChapterContentPanelSlot.updateAiContent()`.

### **8.2 Customize UI**

- Replace chapter content panel style: inherit `ChapterContentDialogFragment` and override the layout
- Extend Slots: register custom slots via `CustomSlotType`
- See [Slot System](../advanced/SlotSystem-EN.md) for details

### **8.3 Integrate into Existing Activity**

If you don't want to use the built-in `AiEducationActivity`, you can refer to its implementation and do the following in your own Activity:

1. Initialize `AliPlayerController`
2. Register `ChapterButtonSlot` + `ChapterContentPanelSlot` (keep instance references when registering)
3. Load the PlayAuth and the AI content
4. Configure and attach the player, then pass the AI content into the panel slot

**Example Skeleton**:

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
            // Keep the slot instance so AI content can be passed in after attach
            mChapterContentPanelSlot = new ChapterContentPanelSlot(parent.getContext());
            return mChapterContentPanelSlot;
        });
    }

    private void configurePlayer(@NonNull String vid, @NonNull String playAuth,
                                 @Nullable AiAnalysisResponse response) {
        // Consumer explicitly calls Assembler to build chapters
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

        // After attach, explicitly pass the AI DTO into the panel slot
        if (mChapterContentPanelSlot != null) {
            mChapterContentPanelSlot.updateAiContent(response);
        }

        // Control chapter button visibility based on AI content availability
        boolean chapterAvailable = AiContentAssembler.hasAnyContent(response);
        if (mChapterButtonSlot != null) {
            mChapterButtonSlot.updateChapterAvailable(chapterAvailable);
        }
    }
}
```

---

## **9. Integration FAQ**

| Question | Solution |
|------|---------|
| Failed to get play credential | Check if the App Server address is correct, network is connected, and VideoId is valid |
| AI content is empty | Confirm that AI analysis has been enabled for the video; the client will degrade to normal playback mode (no AI panel) |
| How to use my own video ID | Modify `AiContentConstants.DEFAULT_MEDIA_ID` or configure in the settings page |
| Panel abnormal on orientation change | Confirm that the Activity declares `android:configChanges="orientation|screenSize"` |
| How to disable a specific AI capability | Simply don't return the corresponding `ResultType` in the server API (e.g., return only Chapter without Summary) |
| SDK version compatibility | Requires Alibaba Cloud Player SDK ≥ 7.16.0; lower versions do not support `SceneType.AI_VOD` |
