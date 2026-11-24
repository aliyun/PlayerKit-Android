# Changelog

## [7.19.0] - 2026-11-24

### SDK 升级

- AliyunPlayer: 7.16.0 → 7.19.0
- AlivcArtc: 7.16.0 → 7.19.0
- PlayerKit 版本号更新为 7.19.0，可通过 `AliPlayerKit.getPlayerKitVersion()` 获取

### 新增

- **章节数据懒加载（`ChapterInfo` 动态更新）**：章节数据不再只能随 `configure()` 一次性传入，配置完成后可随时通过 `AliPlayerController.setChapters()` 写入并立即生效，无需重建播放器或重新配置；初始值仍可经 `AliPlayerModel.Builder.chapters()` 传入
  - **数据语义**：配置值与运行期写入共用同一份数据，运行期写入整体替换、后写覆盖先写；传入 `null` 或空列表等同清空章节
  - **写入时机**：播放器未就绪（`configure()` 之前或 `destroy()` 之后）时告警并忽略本次写入，不向调用方抛异常；写入不要求主线程，UI 侧刷新统一回到主线程
  - **联动入口**：一次写入同步刷新进度条章节标记点（横竖屏）、横屏「第 N 章 · 标题」与「下一章」、竖屏章节胶囊、横屏章节侧滑面板，并通过 `PlayerEvents.ChapterListUpdated` 事件通知自定义 UI
  - **空数据降级**：章节为空时上述入口全部不渲染（面板收起、横屏信息行回退为时间显示），界面等同普通播放器，支持「先起播、后补章节」
  - **生命周期**：章节数据随 `destroy()` 释放，重新 `configure()` 时以新配置值重置，池化实例复用不残留上一部影片的章节
  - **竖屏定位**：竖屏章节胶囊在数据整体替换后自动定位到当前章节，播放位置无章节归属时回到列表开头
- **新增接口**：`AliPlayerController.setChapters()` / `getChapters()`（未就绪时返回空列表）、`IMediaPlayer.setChapters()`、`IPlayerStateStore.getChapters()`，以及 `PlayerEvents.ChapterListUpdated` 事件（章节清空同样触发，用于通知 UI 降级）

### 变更

- **行为变更：`LIVE` / `RESTRICTED` 场景拦截 Seek**：经 `PlayerCommand.Seek` 事件通道发起的跳转（进度条拖拽、章节入口、自定义 Slot `postEvent(...)`）统一被丢弃并输出告警日志，播放进度保持不变
  - 受影响：此前 `RESTRICTED` 只在 UI 层禁用交互（进度条不可拖、水平手势禁用），自定义 Slot 发送该事件仍可跳转，升级后不再生效
  - 不受影响：业务层 `controller.getPlayer().seekTo()`（`IMediaPlayer`）不经过命令通道，无门槛
  - 各场景差异详见 [场景类型 - Seek 能力与拦截边界](./advanced/SceneType.md#33-seek-能力与拦截边界)
- **章节插槽统一读数据通道**：`BottomBarSlot` / `ChapterPanelSlot` 不再各自持有 `AliPlayerModel` 快照，改为订阅 `ChapterListUpdated` 并从 `PlayerStateStore` 读取，保证各章节入口数据始终一致
- **`AliPlayerModel.getChapters()` 语义明确**：仅代表 `configure()` 时的初始值快照，不随运行期写入变化；运行期最新数据请使用 `AliPlayerController.getChapters()` 或 `IPlayerStateStore.getChapters()`
- **AI 教育场景切换为章节懒加载**：先起播、AI 内容返回后再补章节，首帧不再等待 AI 接口。接入方式详见 [AI 教育场景 - 章节数据异步更新（懒加载）](./scenes/AiEducation.md#84-章节数据异步更新懒加载)

### 修复

- 切换播放数据源时章节 UI 残留：章节胶囊、横屏章节按钮、进度条章节标记点未被清理，其点击监听仍会向新视频发起跳转
- 切换视频后立即进入全屏时，横屏章节标记点按旧时长重建，导致标记位置与高亮错位
- 播放位置位于首个章节之前或落在章节空隙时，横屏「下一章」无响应或目标章节计算错误；现改为按最近有效章节顺延，越界时提示「已是最后一章」
- 章节侧滑面板打开期间章节数据变更不同步刷新，导致高亮落到错误章节、面板残留已清空的入口；现改为打开时重建快照、数据变更时同步刷新，章节被清空时自动收起

### 文档

- [API 参考](./ApiReference.md) 补充章节数据通道与 `ChapterListUpdated` 事件说明
- [场景类型](./advanced/SceneType.md) 新增「Seek 能力与拦截边界」「章节能力与场景」说明，明确章节能力为通用能力：`LIVE` / `MINIMAL` 不展示，`RESTRICTED` 仅展示不可跳转
- [AI 教育场景](./scenes/AiEducation.md) 新增「章节数据异步更新（懒加载）」章节

## [7.16.0] - 2026-08-26

### SDK 升级

- AliyunPlayer: 7.15.0 → 7.16.0
- AlivcArtc: 7.15.0 → 7.16.0

### 新增

- **VID All-in-One 播放体验**：依托 AliPlayerKit 与阿里云视频点播 VOD 的协同能力，业务侧仅需传入视频 VID，即可自动获取播放资源，以及关联的视频标题、封面图和缩略图等媒资信息，减少播放地址获取、媒资信息传递及端侧参数配置，简化播放接入
- **Seek 缩略图**：拖动进度条时显示视频缩略图。使用 VID 播放时会自动获取缩略图，也支持手动传入缩略图数据
- **视频拆条**：将长视频按内容结构智能拆分为多个章节片段，通过进度条标记和章节面板帮助用户快速定位重点内容
- **AI 教育场景**：
  - 新增面向在线教育、知识课程等场景的 AI 教育视频解决方案，提供智能章节、内容摘要、知识点及图文解析能力，帮助用户快速定位重点内容，提升视频学习与内容理解效率
  - 新增 `SceneType.AI_VOD` 场景类型，在标准 VOD 播放能力基础上，支持章节拆条、章节导航及 Seek 过程中的章节信息展示

### 文档

- 新增 [AI 教育场景](./scenes/AiEducation.md) 场景方案文档

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

