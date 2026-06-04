# Changelog

## [7.15.0] - 2026-06-04

### SDK 升级

- AliyunPlayer: 7.12.0 → 7.15.0
- AlivcArtc: 7.12.0 → 7.15.0

### 不兼容变更

- **SlotRegistry 迁移至 SlotManager**：移除 `SlotRegistry`、`ISlotManager` 和 `HiddenSlotElements`，引入 `SlotManager` 作为统一插槽管理入口
  - `AliPlayerView.attach(controller, model)` 签名变更为 `attach(controller)`
  - 数据配置迁移至 `controller.configure(model)`，必须在 `attach()` 前调用
  - `detach()` 仅解绑视图，需显式调用 `controller.destroy()` 释放资源
  - 插槽可见性控制从 builder 模式移至运行时 `SlotManager` 方法
- **GlobalInitializer 重命名为 GlobalManager**
- **移除动态语言包支持**：仅保留 Android 标准资源机制 (`values-xx/strings.xml`)

### 新增

- **UI 增强**：全新播放器 UI 设计与还原，可直接使用，无需客户再还原 UI。
- **双层级自定义配置机制**：引入 `OnGlobalInitCallback` 用于全局 SDK 设置、`OnPlayerConfigCallback` 用于实例级播放器配置；暴露 `getInternalPlayer()` 以访问原生 SDK 接口
- **国际化支持**：引入 `PlayerLocale` 模块自动适配系统语言；添加 `setLanguage` API 强制指定语言
- **插槽元素细粒度控制**：引入 Element Registry 架构，支持声明式隐藏 UI 元素或禁用手势；添加 `SlotElements` 常量和 `hideElements()`/`showElements()` 方法
- **自定义插槽层级控制**：引入 `CustomSlotType` 定义自定义插槽层级，支持显式 order 值
- **通用滑块组件**：引入 `UniversalSlider` 组件用于音量和亮度控制
- **横屏模式支持**：基于 `ViewStub` 实现懒加载横屏布局；引入 `FullScreenChanged` 事件
- **轨道切换事件**：添加 `TrackSwitchCompleted` 和 `TrackSwitchFailed` 事件；`PlayStateSlot` 支持 Error、Loading、Snapshot、Track Switch 状态展示
- **选项面板**：添加速度和质量选择的选项面板组件
- **能力识别统一**：重构至 `ScenarioManager`，确保播放优化和诊断的一致 JSON 结构

### 变更

- **移除竖屏底部栏全屏按钮**：竖屏模式下不再显示全屏按钮，统一由 `LandscapeHintSlot` 提供全屏入口
- **设置项控件优化**：设置菜单中的 `SwitchCompat` 替换为自定义 `CheckBox`，统一切换控件样式
- 调试功能默认禁用，需通过公开 setter 显式启用
- 日志面板默认折叠
- 搜索栏使用 `Math.round` 修复精度问题
- 统一资源命名约定并清理未使用资源
- 移除 BlurView 依赖

### 修复

- 隐藏方法强制至少一个元素键，确保接口安全性
- 优化布局更新逻辑，从 `onLayout` 切换到 `onSizeChanged` 避免冗余遍历
- 优雅处理 RtsSDK 库加载异常

### 文档

- 添加 SceneType 配置指南 (`docs/advanced/SceneType.md`)
- 添加 Locale 多语言文档 (`docs/advanced/Locale.md`)
- 添加全套英文文档翻译及文档站中英文动态切换支持
- 更新联系方式并添加 GitHub issue 模板

## [7.12.0] - 2025-03-11

### Added

- **SDK 升级** - 升级 AliyunPlayer 和 RtsSDK 到版本 7.12.0
- **插槽系统** - 新增基于插槽的 UI 扩展架构，支持自由组合播放器 UI 组件
  - 封面图插槽 (CoverSlot)
  - 日志面板插槽 (LogPanelSlot)
  - 横屏提示插槽 (LandscapeHintSlot)
- **策略系统** - 新增策略模式支持业务逻辑灵活扩展
  - 播放器生命周期策略 (PlayerLifecycleStrategy)
  - 恢复播放策略 (ResumePlayStrategy)
- **事件系统** - 新增播放器事件总线，支持事件订阅与分发
- **日志系统** - 新增全局日志 API，支持调试模式控制
- **全局预加载** - 新增全局预加载 API，优化播放启动速度
- **手势控制** - 新增亮度、音量、播放速度手势调节支持
- **设置功能**
  - 新增调试模式开关，支持持久化存储
  - 新增禁止截屏功能
  - 新增硬件解码开关
  - 新增屏幕常亮控制
  - 新增播放器视图类型配置
- **Mobile-Ops 模块** - 新增运维监控模块支持
- **构建信息展示** - 新增构建 ID 和时间戳显示
- **CI/CD 工作流** - 新增源码打包和持续集成配置
- **MIT 开源许可证** - 添加 MIT LICENSE 文件

### Changed

- **架构重构**
  - 重构播放器架构，引入 IMediaPlayer 接口解耦
  - 重命名核心组件
  - 重命名模块以区分场景和使用示例
  - 迁移预加载和日志包至新的包结构
- **生命周期策略** - 重构生命周期策略系统，新增基类和多种策略实现
- **默认缩放模式** - 更新默认缩放类型为 FIT_CENTER

### Fixed

- **RTS 低延迟直播** - 集成 RTS 低延迟直播流支持 (fix #78046811)
- **分辨率匹配** - 优化分辨率匹配逻辑，考虑视频方向 (fix #78040354)
- **上下文问题** - 修复播放器创建时使用 Application Context 导致的问题
- **首帧渲染状态** - 替换 FIRST_FRAME_RENDERED 状态为专用事件

### Documentation

- 新增完整的项目文档体系
  - [核心功能文档](./docs/CoreFeatures.md)
  - [集成准备文档](./docs/Integration.md)
  - [快速开始文档](./docs/QuickStart.md)
  - [API 参考文档](./docs/ApiReference.md)
- 新增系统功能说明文档
  - [插槽系统文档](./docs/advanced/SlotSystem.md)
  - [策略系统文档](./docs/advanced/StrategySystem.md)
  - [事件系统文档](./docs/advanced/EventSystem.md)
  - [日志系统文档](./docs/advanced/LogSystem.md)
  - [播放器生命周期策略文档](./docs/advanced/PlayerLifecycleStrategy.md)
  - [视频源文档](./docs/advanced/VideoSource.md)
- 优化文档导航和推荐阅读路径
- 更新架构图和集成流程图

---

