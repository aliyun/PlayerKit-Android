# **SceneType**

**SceneType** is AliPlayerKit's playback scene abstraction. By specifying different scenes, the player automatically adapts UI behavior, gesture controls, and slot visibility.

> 💡 **Most developers do not need to worry about this configuration**: for an ordinary VOD scenario you do not need to set SceneType — the default is `SceneType.VOD`, which provides full playback capabilities. Only **non-VOD scenarios** such as live streaming or restricted playback require an explicit setting.

---

## **1. Scene Overview**

| Scene | Description | Typical Business | Differences vs. VOD |
|-------|-------------|------------------|---------------------|
| `VOD` | Default scene; no explicit setup needed. Supports all controls | General VOD, courses, media library | — |
| `LIVE` | Live streams have no fixed duration; timeline operations are disabled automatically | Live streaming, sports events, live commerce | Disables seek/fast-forward/playback-speed; hides `cover` (cover image) |
| `VIDEO_LIST` | Disables vertical gestures to avoid conflicting with list scrolling | Feeds, short-video lists | Disables volume/brightness vertical gestures; otherwise identical to VOD |
| `RESTRICTED` | Anti-skip; ensures users watch sequentially and completely | Exam monitoring, training courses | Disables seek/fast-forward/playback-speed; hides `coverImage` and `seekThumbnail` |
| `MINIMAL` | Pure picture; all controls must be implemented by you | Background playback, fully custom UI | Hides all control components and gestures; only `playerSurface`/`playState`/`overlays` are visible |
| `AI_VOD` | AI-enhanced VOD, focused on AI content display and understanding | AI courses, knowledge videos | Adds AI content display (summary, segments); chapter navigation is a generic capability available in any scene |

> 📖 **Chapter capability is generic**: available in every scene except `LIVE` / `MINIMAL`; see [3.4 Chapter Capability and Scenes](#34-chapter-capability-and-scenes) for the per-scene differences.

---

## **2. Configuration**

The scene type is specified via `AliPlayerModel.Builder.sceneType()`. **The default VOD scene needs no setup**; only non-VOD scenarios require an explicit value.

**Live scene**:

```java
// Build the playback data — calling sceneType() is optional, default is VOD
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
    .sceneType(SceneType.LIVE)
    .build();

// Bind to the player view
controller.configure(playerModel);
playerView.attach(controller);
```

---

## **3. Slots and Scenes**

### **3.1 Slot Visibility**

The slot system automatically toggles component visibility based on the scene type:

| Slot | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL | AI_VOD |
|------|-----|------|------------|------------|---------|--------|
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

### **3.2 Gesture Differences**

| Gesture | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL | AI_VOD |
|---------|-----|------|------------|------------|---------|--------|
| Single tap (toggle control bar) | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| Double tap (play/pause) | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| Long press (2x speed) | ✓ | ✗ | ✓ | ✗ | ✗ | ✓ |
| Horizontal drag (seek) | ✓ | ✗ | ✓ | ✗ | ✗ | ✓ |
| Left vertical drag (brightness) | ✓ | ✓ | ✗ | ✓ | ✗ | ✓ |
| Right vertical drag (volume) | ✓ | ✓ | ✗ | ✓ | ✗ | ✓ |

### **3.3 Seek Capability and Blocking Boundaries**

The "Horizontal drag (seek)" row above only describes the interaction layer. There are two seek paths: the `PlayerCommand.Seek` event channel — progress bar dragging, chapter marker / chip / panel taps, and custom Slots posting `PlayerCommand.Seek` events (`BaseSlot.postEvent(...)`) all go through it, entering the seek command entry of `AliPlayerController` where the scene guard decides; and business code calling `controller.getPlayer().seekTo()` (`IMediaPlayer`) directly, which bypasses the command channel and the scene guard.

| Scene | Interaction entry seek | Chapter entry seek | Blocked at command layer | Notes |
|-------|------------------------|-------------------|--------------------------|-------|
| VOD | ✓ | ✓ | No | Normal seeking |
| LIVE | ✗ | — (chapters not displayed) | Yes | Live streams have no fixed timeline, so seek is unavailable |
| VIDEO_LIST | ✓ | ✓ | No | Normal seeking |
| RESTRICTED | ✗ | ✗ (displayed but not seekable) | Yes | Seek is forbidden to ensure content is watched in order |
| MINIMAL | ✗ (no built-in UI) | — | No | No UI but seek is supported; seeks from custom controls work as usual |
| AI_VOD | ✓ | ✓ | No | Normal seeking |

- **Blocked behavior**: the request is dropped and a single `Seek blocked, sceneType=…` warning is logged; no exception is thrown, no callback is invoked, and the playback position stays unchanged
- **Chapter interplay**: in `RESTRICTED`, chapters are display-only — tapping does not seek, does not update the chapter highlight, and does not show the "seeked" toast
- **Blocked path**: `PlayerCommand.Seek` events posted by custom Slots (`BaseSlot.postEvent(...)`) share the same event channel as the built-in progress bar / chapter entries, so they are dropped in `LIVE` / `RESTRICTED`
- **Unrestricted path**: business code calling `controller.getPlayer().seekTo()` (`IMediaPlayer`) bypasses the command channel, so there is no gating and no scene restriction; use it when the business side controls the playable range, not as a way to bypass anti-skip protection

### **3.4 Chapter Capability and Scenes**

Chapter capability is **generic** and not bound to any specific scene: as soon as chapter data is non-empty, every scene except `LIVE` / `MINIMAL` shows chapter entry points; the only difference is whether chapter taps may seek.

| Scene | Chapter entry points | Chapter tap seek | Notes |
|-------|---------------------|------------------|-------|
| VOD | ✓ | ✓ | Generic VOD; chapters work as usual |
| LIVE | ✗ | — | Live streams have no fixed timeline; chapter capability does not apply |
| VIDEO_LIST | ✓ | ✓ | Identical to VOD |
| RESTRICTED | ✓ | ✗ | Display-only: tapping does not seek, does not update the highlight and shows no toast |
| MINIMAL | ✗ | — | No built-in UI, so the chapter capability does not apply |
| AI_VOD | ✓ | ✓ | Identical to VOD, plus AI content display |

**Entry points** (a single data write updates all of them):

- Progress bar chapter markers (portrait / landscape): drawn only when a chapter start time falls inside `(0, duration)`; a single chapter starting at 0 has no marker
- Portrait chapter chip list
- Landscape "Chapter N · Title" row and the "Next" entry (with a single chapter the "Next" entry stays visible but does not respond)
- Landscape chapter side panel (`SlotType.CHAPTER_PANEL`)

**Data sources** (both write the same data): pass the initial value via `AliPlayerModel.Builder.chapters()` at configure time, or call `AliPlayerController.setChapters()` at any time afterwards to take effect immediately; `null` or an empty list clears the chapters. When chapter data is empty none of the entry points render, so the UI behaves like a normal player and "start playback first, add chapters later" is a safe path; see [AI Education Scene - Lazy Chapter Data Updates](../scenes/AiEducation-EN.md#84-lazy-chapter-data-updates).

> 💡 Chapter taps travel the same `PlayerCommand.Seek` event channel as progress bar dragging; for how taps are dropped by the command layer in `RESTRICTED`, see [3.3 Seek Capability and Blocking Boundaries](#33-seek-capability-and-blocking-boundaries).

---

## **4. Best Practices**

1. **SceneType is fixed at creation time**: `SceneType` is set when building the `AliPlayerModel` and cannot be changed dynamically during playback. To switch scenes, create a new `AliPlayerModel` and rebind it
2. **List scenarios first**: when the player is embedded in scrollable containers such as a RecyclerView, always use the `VIDEO_LIST` scene to avoid vertical gesture conflicts
3. **MINIMAL requires custom controls**: when you choose `MINIMAL`, all playback controls must be implemented by the developer through custom slots
4. **Deep customization**: when SceneType presets do not fully meet your needs, combine them with the slot system to further control component visibility and custom layouts. See [Slot System](SlotSystem-EN.md) for details

---

## **5. FAQ**

### **5.1 What is the difference between VOD and VIDEO_LIST?**

The main difference is vertical gestures: `VIDEO_LIST` disables the volume/brightness vertical gestures to avoid conflicting with the list container's scroll gestures. Everything else is identical to VOD.

### **5.2 How do I implement custom controls in the MINIMAL scene?**

Layer your control UI on top with custom slots:

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

### **5.3 How do I use the AI_VOD scene?**

`AI_VOD` focuses on AI content display (video summary and segmented explanation); chapter navigation itself is a generic capability not limited to `AI_VOD` — any scene other than `LIVE` / `MINIMAL` shows chapter entry points as soon as chapter data is non-empty. Configure chapter data via `chapters()`:

```java
// Build an AI playback model with chapter data
List<ChapterInfo> chapters = ...; // Obtained from AI content analysis
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
    .sceneType(SceneType.AI_VOD)
    .chapters(chapters)
    .build();

controller.configure(playerModel);
playerView.attach(controller);
```

For a detailed integration guide, see [AI Education Scene](../scenes/AiEducation-EN.md).
