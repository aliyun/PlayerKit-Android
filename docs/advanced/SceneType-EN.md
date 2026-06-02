# **SceneType**

**SceneType** is AliPlayerKit's playback scene abstraction. By specifying different scenes, the player automatically adapts UI behavior, gesture controls, and slot visibility.

> 💡 **Most developers do not need to worry about this configuration**: for an ordinary VOD scenario you do not need to set SceneType — the default is `SceneType.VOD`, which provides full playback capabilities. Only **non-VOD scenarios** such as live streaming or restricted playback require an explicit setting.

---

## **1. Scene Overview**

| Scene | Description | Typical Business | Differences vs. VOD |
|-------|-------------|------------------|---------------------|
| `VOD` | Default scene; no explicit setup needed. Supports all controls | General VOD, courses, media library | — |
| `LIVE` | Live streams have no fixed duration; timeline operations are disabled automatically | Live streaming, sports events, live commerce | Disables seek/fast-forward/playback-speed; hides `coverImage` and `seekThumbnail` |
| `VIDEO_LIST` | Disables vertical gestures to avoid conflicting with list scrolling | Feeds, short-video lists | Disables volume/brightness vertical gestures; otherwise identical to VOD |
| `RESTRICTED` | Anti-skip; ensures users watch sequentially and completely | Exam monitoring, training courses | Disables seek/fast-forward/playback-speed; hides `coverImage` and `seekThumbnail` |
| `MINIMAL` | Pure picture; all controls must be implemented by you | Background playback, fully custom UI | Hides all control components and gestures; only `playerSurface`/`playState`/`overlays` are visible |

---

## **2. Configuration**

The scene type is specified via `AliPlayerModel.Builder.sceneType()`. **The default VOD scene needs no setup**; only non-VOD scenarios require an explicit value.

**Live scene**:

```java
// Build the playback data — calling sceneType() is optional, default is VOD
AliPlayerModel playerModel = new AliPlayerModel.Builder()
    .videoSource(videoSource)
    .videoTitle("Live Stream")
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

| Slot | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|------|-----|------|------------|------------|---------|
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

### **3.2 Gesture Differences**

| Gesture | VOD | LIVE | VIDEO_LIST | RESTRICTED | MINIMAL |
|---------|-----|------|------------|------------|---------|
| Single tap (toggle control bar) | ✓ | ✓ | ✓ | ✓ | ✗ |
| Double tap (play/pause) | ✓ | ✓ | ✓ | ✓ | ✗ |
| Long press (2x speed) | ✓ | ✗ | ✓ | ✗ | ✗ |
| Horizontal drag (seek) | ✓ | ✗ | ✓ | ✗ | ✗ |
| Left vertical drag (brightness) | ✓ | ✓ | ✗ | ✓ | ✗ |
| Right vertical drag (volume) | ✓ | ✓ | ✗ | ✓ | ✗ |

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
