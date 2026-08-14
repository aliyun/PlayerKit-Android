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
| Alibaba Cloud Services | [ApsaraVideo VOD](https://www.alibabacloud.com/en/product/apsaravideo-for-vod) and [Intelligent Media Services (IMS)](Intelligent Media Services) have been activated |
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

**Component Responsibilities**:

| Component | Layer | Description |
| ---------------------------- | ----- | ------------------------------------------------------------ |
| `AiEducationActivity` | Scene | Scene entry point (composition root), orchestrates data loading, player initialization, Slot registration, and pushing AI content into the panel |
| `AiContentLoader` | Scene | Requests play credentials and AI analysis results on a background thread, and callbacks results on the main thread |
| `ChapterButtonSlot` | Scene | Custom slot registered through `CustomSlotType`; displays the "Chapter" button in the player control bar and triggers expansion or collapse of the chapter content panel |
| `ChapterContentPanelSlot` | Scene | Panel controller, responsible for managing `DialogFragment` display, dismissal, and state synchronization; AI content is pushed in by the scene layer via `updateAiContent()` |
| `ChapterContentDialogFragment` | Scene | AI content panel UI, displaying the chapter list, video summary, chapter summary, and knowledge points |
| `MarkdownBinder` | Scene | Renders the Markdown text of AI analysis content into rich text (based on Markwon) |
| `ChapterPanelSlot` | PlayerKit | Built-in chapter panel of the component layer; slides in from the right in fullscreen to show the chapter list and seek on tap (`SceneType.AI_VOD` scene, triggered by the bottom bar chapter button) |
| `AliPlayerController` | PlayerKit | Provides fundamental capabilities including video loading, playback control, seek, and playback progress callbacks |

> 💡 The scene-layer `ChapterContentDialogFragment` carries the full AI content (chapters + summaries + knowledge points) and is the differentiating UI of the AI Education Scene, while the component-layer `ChapterPanelSlot` handles pure chapter navigation and can be reused directly by `SceneType.AI_VOD` playback that has no AI analysis content. The two are independent — pick whichever fits your business.

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
| `MarkdownContent` | `String` | Markdown-formatted content |

**API Protocol**:

| Item | Description |
|------|------|
| HTTP Timeout | 5 seconds (connect + read) |
| Error Handling | Degrades to normal playback mode (no AI panel) when API fails |
| Time Unit | `StartTime` / `EndTime` are in milliseconds |

---

## **7. Data Model & Mapping**

### **7.1 Three-Layer Mapping**

| Server JSON Field | Client DTO | Business Model | Description |
|-----------------|-----------|---------|------|
| `ChapterId` | `ChapterContentItem.chapterId` | `ChapterInfo.id` | Converted to String |
| `StartTime` | `ChapterContentItem.startTime` | `ChapterInfo.startMs` | Milliseconds |
| `EndTime` | `ChapterContentItem.endTime` | `ChapterInfo.endMs` | Milliseconds |
| `ChapterTitle` | `ChapterContentItem.chapterTitle` | `ChapterInfo.title` | |
| `ThumbnailUrl` | `ChapterContentItem.thumbnailUrl` | `ChapterInfo.thumbnailUrl` | |
| `Summary` | `ChapterContentItem.summary` | `AiSegment.summary` | Chapter summary |
| `KnowledgePoints[]` | `ChapterContentItem.knowledgePoints` | `AiSegment.markdown` | Concatenated as Markdown |
| `ParagraphSummary` | `SummaryContent.paragraphSummary` | `AiSummary.description` | Video-level summary |

### **7.2 Aggregation Model**

The final aggregation results in `AiVideoContent`, which contains:

| Field | Type | Description |
|------|------|------|
| `videoId` | `String` | Video ID |
| `type` | `@AiContentType int` | Content type (determines panel rendering mode) |
| `summary` | `AiSummary` | Video-level summary (nullable) |
| `chapters` | `List<ChapterInfo>` | Chapter directory list |
| `segments` | `List<AiSegment>` | Segment analysis list |

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
loader.load(vid, aiContent -> {
    // After obtaining AiVideoContent, configure the player and push it into the panel...
});
```

> 💡 Chapter data (`chapters`) can be delivered through `AliPlayerModel`; AI content such as the video summary and chapter segments cannot be carried on `AliPlayerModel`, so the scene layer must push it in via `ChapterContentPanelSlot.updateAiContent()`.

### **8.2 Customize UI**

- Replace chapter content panel style: inherit `ChapterContentDialogFragment` and override the layout
- Extend Slots: register custom slots via `CustomSlotType`
- See [Slot System](../advanced/SlotSystem-EN.md) for details

### **8.3 Integrate into Existing Activity**

If you don't want to use the built-in `AiEducationActivity`, you can refer to its implementation and do the following in your own Activity:

1. Initialize `AliPlayerController`
2. Register `ChapterButtonSlot` + `ChapterContentPanelSlot` (keep the panel slot instance when registering)
3. Load the PlayAuth and the AI content
4. Configure and attach the player, then push the AI content into the panel slot

**Example Skeleton**:

```java
public class MyActivity extends AppCompatActivity {

    @Nullable
    private ChapterContentPanelSlot mChapterContentPanelSlot;

    private void registerSlots() {
        SlotManager slotManager = playerView.getSlotManager();
        slotManager.register(ChapterButtonSlot.TYPE, parent -> new ChapterButtonSlot(parent.getContext()));
        slotManager.register(ChapterContentPanelSlot.TYPE, parent -> {
            // Keep the slot instance so AI content can be pushed in after attach
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

        // After attach, this page (the composition root) pushes AI content into the panel slot
        if (mChapterContentPanelSlot != null) {
            mChapterContentPanelSlot.updateAiContent(aiContent);
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
