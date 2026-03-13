# Changelog

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

