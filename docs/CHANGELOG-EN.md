# Changelog

Language: [中文简体](CHANGELOG.md) | English

## [7.14.0] - 2026-06-02

### SDK Upgrade

- AliyunPlayer: 7.12.0 → 7.14.0
- AlivcArtc: 7.12.0 → 7.14.0

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
- Added full English documentation translation.
- Added documentation site language switching support (sidebar and content synchronization).
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
