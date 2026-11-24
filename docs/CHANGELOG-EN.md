# Changelog

Language: [中文简体](CHANGELOG.md) | English

## [7.19.0] - 2026-11-24

### SDK Upgrade

- AliyunPlayer: 7.16.0 → 7.19.0
- AlivcArtc: 7.16.0 → 7.19.0
- PlayerKit version bumped to 7.19.0, readable via `AliPlayerKit.getPlayerKitVersion()`

### Added

- **Lazy chapter data (`ChapterInfo` dynamic updates)**: Chapter data is no longer limited to a single delivery at `configure()`; call `AliPlayerController.setChapters()` at any time after configuration and it takes effect immediately, without re-creating or re-configuring the player (initial values can still be passed via `AliPlayerModel.Builder.chapters()`)
  - **Data semantics**: The configured value and runtime writes share the same data; each write replaces the whole list and later writes override earlier ones; `null` or an empty list clears the chapters
  - **Write timing**: While the player is not ready (before `configure()` or after `destroy()`) the write is logged and ignored instead of throwing to the caller; writes do not need to be on the main thread, and UI refreshes are marshalled back to it
  - **Linked entry points**: A single write refreshes the progress bar chapter markers (portrait and landscape), the landscape "Chapter N · Title" row with the "Next" entry, the portrait chapter chips and the landscape chapter side panel, and notifies custom UI through the `PlayerEvents.ChapterListUpdated` event
  - **Empty data fallback**: With no chapters all of these entry points are not rendered (the panel closes and the landscape info row falls back to the time display), so the UI behaves like a normal player and "start playback first, add chapters later" is a safe path
  - **Lifecycle**: Chapter data is released with `destroy()` and reset to the new configuration value on re-`configure()`, so a pooled instance never leaks chapters from the previous video
  - **Portrait positioning**: The portrait chapter chips scroll to the current chapter after the data set is replaced, and return to the beginning when the position belongs to no chapter
- **New APIs**: `AliPlayerController.setChapters()` / `getChapters()` (returns an empty list while the player is not ready), `IMediaPlayer.setChapters()`, `IPlayerStateStore.getChapters()`, and the `PlayerEvents.ChapterListUpdated` event (also fired when chapters are cleared, so UI can fall back)

### Changed

- **Chapter capability now applies more broadly**: Previously limited to "`AI_VOD` with at least 2 chapters", it now applies to any scene except `LIVE` / `MINIMAL` as soon as chapters are non-empty, so `VOD` / `VIDEO_LIST` / `RESTRICTED` also show chapter entry points; a single chapter works as well — chips are still shown, no marker is drawn when its start time is 0, and the landscape "Next" entry stays visible but does not respond; in `RESTRICTED` chapters are display-only — tapping does not seek, does not update the highlight and does not show the "seeked" toast
- **Behavior change: seek is blocked in the `LIVE` / `RESTRICTED` scenes**: Jumps initiated through the `PlayerCommand.Seek` event channel (progress bar dragging, chapter entries, custom Slots posting `postEvent(...)`) are all dropped with a warning log, and the playback position stays unchanged
  - Affected: previously `RESTRICTED` only disabled interactions at the UI layer (progress bar not draggable, horizontal gesture disabled), so custom Slots could still seek by posting this event; that no longer works after the upgrade
  - Not affected: business code calling `controller.getPlayer().seekTo()` (`IMediaPlayer`) bypasses the command channel and is not gated
  - See [SceneType - Seek Capability and Blocking Boundaries](./advanced/SceneType-EN.md#33-seek-capability-and-blocking-boundaries) for the per-scene differences
- **Chapter slots now read a single data channel**: `BottomBarSlot` / `ChapterPanelSlot` no longer hold their own `AliPlayerModel` snapshots; they subscribe to `ChapterListUpdated` and read from `PlayerStateStore`, keeping every chapter entry point consistent
- **`AliPlayerModel.getChapters()` semantics clarified**: It now represents only the initial snapshot taken at `configure()` and does not change with runtime writes; use `AliPlayerController.getChapters()` or `IPlayerStateStore.getChapters()` for the latest data at runtime
- **AI Education scene switched to lazy chapters**: Playback starts first and chapters are added once the AI content returns, so the first frame no longer waits for the AI request. See [AI Education Scene - Lazy Chapter Data Updates](./scenes/AiEducation-EN.md#84-lazy-chapter-data-updates) for the integration flow

### Fixed

- Stale chapter UI when switching data sources: chapter chips, landscape chapter buttons and progress bar markers were not cleared, and their click listeners could still seek on the new video
- Entering fullscreen right after switching videos rebuilt the landscape chapter markers with the previous duration, causing misplaced markers and highlighting
- When the position is before the first chapter or falls into a chapter gap, the landscape "Next" entry did not respond or computed the wrong target chapter; it now advances from the most recent valid chapter and reports "last chapter" when out of range
- The chapter side panel did not refresh while open when chapter data changed, so highlighting could land on the wrong chapter and cleared entries could remain; the panel now rebuilds its snapshot on open, refreshes in sync with data changes, and closes automatically when the chapters are cleared

### Documentation

- [API Reference](./ApiReference-EN.md) now documents the chapter data channel and the `ChapterListUpdated` event
- [SceneType](./advanced/SceneType-EN.md) now documents Seek capability and blocking boundaries, plus chapter capability and scene coverage: generic across scenes, hidden in `LIVE` / `MINIMAL`, display-only in `RESTRICTED`
- [AI Education Scene](./scenes/AiEducation-EN.md) now documents lazy chapter data updates

## [7.16.0] - 2026-08-26

### SDK Upgrade

- AliyunPlayer: 7.15.0 → 7.16.0
- AlivcArtc: 7.15.0 → 7.16.0

### Added

- **VID All-in-One Playback**: Through the integration of AliPlayerKit with ApsaraVideo VOD, applications only need to pass a video VID to automatically retrieve the playback resources and associated media information, including the video title, cover image, and thumbnails. This reduces the need to obtain playback URLs, transfer media information, and configure client-side parameters, simplifying playback integration.
- **Video Chapter Splitting**: Intelligently segments long videos into multiple chapter clips by content structure, with progress bar markers and a chapter panel to help users quickly locate key content
- **Seek Thumbnails**: Displays video thumbnails while the user drags the progress bar. When playing a video by VID, thumbnails are retrieved automatically. Manually supplied thumbnail data is also supported.
- **AI-Powered Education Scenarios**:
  - Introduces an AI-powered video solution for online education, knowledge-based courses, and similar scenarios. It provides intelligent chapters, content summaries, key knowledge points, and illustrated explanations, helping users quickly locate important content and improve learning and content comprehension.
  - Adds the `SceneType.AI_VOD` scene type. In addition to standard VOD playback capabilities, it supports chapter segmentation, chapter navigation, and the display of chapter information during seeking.

### Documentation

- Added [AI Education Scene](./scenes/AiEducation-EN.md) scenario solution documentation

## [7.15.0] - 2026-06-04

### SDK Upgrade

- AliyunPlayer: 7.12.0 → 7.15.0
- AlivcArtc: 7.12.0 → 7.15.0

### Breaking Changes

- **SlotRegistry migrated to SlotManager**: Removed `SlotRegistry`, `ISlotManager` and `HiddenSlotElements`. Introduced `SlotManager` as the unified slot management entry point.
  - `AliPlayerView.attach(controller, model)` signature changed to `attach(controller)`
  - Data configuration migrated to `controller.configure(model)`, which must be called before `attach()`
  - `detach()` only unbinds the view; `controller.destroy()` must be explicitly called to release resources
  - Slot visibility control moved from the builder pattern to runtime `SlotManager` methods
- **GlobalInitializer renamed to GlobalManager**
- **Removed dynamic language pack support**: Only the Android standard resource mechanism (`values-xx/strings.xml`) is retained.

### Added

- **UI Enhancement**: Brand-new player UI design and implementation, ready to use out of the box without requiring customers to rebuild the UI.
- **Two-tier custom configuration mechanism**: Introduced `OnGlobalInitCallback` for global SDK settings and `OnPlayerConfigCallback` for instance-level player configuration; exposed `getInternalPlayer()` to access native SDK interfaces.
- **Internationalization support**: Introduced the `PlayerLocale` module to automatically adapt to the system language; added the `setLanguage` API to force a specific language.
- **Fine-grained slot element control**: Introduced the Element Registry architecture, supporting declarative hiding of UI elements or disabling of gestures; added `SlotElements` constants and `hideElements()` / `showElements()` methods.
- **Custom slot layer control**: Introduced `CustomSlotType` to define custom slot layers with explicit order values.
- **Universal slider component**: Introduced the `UniversalSlider` component for volume and brightness control.
- **Landscape mode support**: Implemented lazy-loaded landscape layouts based on `ViewStub`; introduced the `FullScreenChanged` event.
- **Track switch events**: Added `TrackSwitchCompleted` and `TrackSwitchFailed` events; `PlayStateSlot` supports Error, Loading, Snapshot, and Track Switch state display.
- **Option panels**: Added option panel components for speed and quality selection.
- **Unified capability detection**: Refactored into `ScenarioManager` to ensure consistent JSON structures for playback optimization and diagnostics.

### Changed

- **Removed fullscreen button from portrait bottom bar**: The fullscreen button is no longer displayed in portrait mode; users enter fullscreen exclusively via `LandscapeHintSlot`.
- **Setting item control update**: Replaced `SwitchCompat` with custom `CheckBox` in the settings menu for a unified toggle style.
- Debug features are disabled by default and must be explicitly enabled via public setters.
- The log panel is collapsed by default.
- The seek bar uses `Math.round` to fix precision issues.
- Unified resource naming conventions and cleaned up unused resources.
- Removed the BlurView dependency.

### Fixed

- The hide method now enforces at least one element key to ensure interface safety.
- Optimized layout update logic by switching from `onLayout` to `onSizeChanged` to avoid redundant traversals.
- Gracefully handle RtsSDK library loading exceptions.

### Documentation

- Added the SceneType configuration guide (`docs/advanced/SceneType.md`).
- Added the Locale internationalization documentation (`docs/advanced/Locale.md`).
- Added full English documentation translation and dynamic language switching support for the documentation site.
- Updated contact information and added GitHub issue templates.

## [7.12.0] - 2025-03-11

### Added

- **SDK Upgrade** - Upgraded AliyunPlayer and RtsSDK to version 7.12.0.
- **Slot System** - Added a slot-based UI extension architecture that supports flexible composition of player UI components.
  - Cover slot (CoverSlot)
  - Log panel slot (LogPanelSlot)
  - Landscape hint slot (LandscapeHintSlot)
- **Strategy System** - Added strategy patterns to support flexible extension of business logic.
  - Player lifecycle strategy (PlayerLifecycleStrategy)
  - Resume play strategy (ResumePlayStrategy)
- **Event System** - Added the player event bus, supporting event subscription and dispatch.
- **Log System** - Added a global log API with debug mode control.
- **Global Preload** - Added a global preload API to optimize playback startup speed.
- **Gesture Control** - Added gesture support for brightness, volume, and playback speed adjustments.
- **Settings**
  - Added a debug mode toggle with persistent storage support.
  - Added a screenshot prevention feature.
  - Added a hardware decoding toggle.
  - Added screen-on control.
  - Added player view type configuration.
- **Mobile-Ops Module** - Added the operations and monitoring module.
- **Build Info Display** - Added build ID and timestamp display.
- **CI/CD Workflow** - Added source code packaging and continuous integration configuration.
- **MIT Open Source License** - Added the MIT LICENSE file.

### Changed

- **Architecture Refactoring**
  - Refactored the player architecture and introduced the `IMediaPlayer` interface for decoupling.
  - Renamed core components.
  - Renamed modules to distinguish scenes from usage examples.
  - Migrated preload and log packages to a new package structure.
- **Lifecycle Strategy** - Refactored the lifecycle strategy system, adding a base class and multiple strategy implementations.
- **Default Scale Mode** - Updated the default scale type to `FIT_CENTER`.

### Fixed

- **RTS Low-Latency Live Streaming** - Integrated RTS low-latency live streaming support (fix #78046811).
- **Resolution Matching** - Optimized resolution matching logic to account for video orientation (fix #78040354).
- **Context Issue** - Fixed an issue caused by using Application Context when creating the player.
- **First Frame Rendering State** - Replaced the `FIRST_FRAME_RENDERED` state with a dedicated event.

### Documentation

- Added a complete project documentation system.
  - [Core Features Documentation](./CoreFeatures-EN.md)
  - [Integration Documentation](./Integration-EN.md)
  - [Quick Start Documentation](./QuickStart-EN.md)
  - [API Reference Documentation](./ApiReference-EN.md)
- Added system feature documentation.
  - [Slot System Documentation](./advanced/SlotSystem-EN.md)
  - [Strategy System Documentation](./advanced/StrategySystem-EN.md)
  - [Event System Documentation](./advanced/EventSystem-EN.md)
  - [Log System Documentation](./advanced/LogSystem-EN.md)
  - [Player Lifecycle Strategy Documentation](./advanced/PlayerLifecycleStrategy-EN.md)
  - [Video Source Documentation](./advanced/VideoSource-EN.md)
- Optimized documentation navigation and recommended reading paths.
- Updated architecture diagrams and integration flowcharts.

---
