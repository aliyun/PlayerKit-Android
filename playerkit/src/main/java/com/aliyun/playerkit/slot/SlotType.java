package com.aliyun.playerkit.slot;

/**
 * AliPlayerKit 插槽类型定义
 * <p>
 * 定义了 AliPlayerKit 支持的插槽类型。
 * 插槽按照定义的顺序添加到布局中，确保层级关系正确。
 * </p>
 * <p>
 * Slot Type Definition
 * <p>
 * Defines slot types supported by the AliPlayerKit.
 * Slots are added to the layout in the defined order, ensuring correct layering.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public enum SlotType {

    /**
     * 播放 Surface 视图插槽
     * <p>
     * 用于显示播放器视频内容的核心视图。
     * </p>
     * <p>
     * Player Surface Slot
     * <p>
     * Core view for displaying player video content.
     * </p>
     */
    PLAYER_SURFACE,

    /**
     * 全屏管理插槽
     * <p>
     * 负责管理播放器的全屏切换逻辑，包括进入/退出全屏、沉浸式全屏设置等。
     * 这是一个纯逻辑管理的插槽，不渲染任何 UI 内容。
     * </p>
     * <p>
     * <strong>重要说明</strong>：
     * <ul>
     *     <li><strong>必需插槽</strong>：此插槽是必需的，不要轻易自定义实现它，否则可能会导致全屏功能失效。
     *     全屏功能涉及 Activity 级别的操作（屏幕方向、系统 UI、View 层级切换），
     *     需要与 FullscreenSlot 的实现逻辑保持一致。</li>
     *     <li><strong>配置要求</strong>：使用此插槽的 Activity 必须在 AndroidManifest.xml 中配置：
     *     <pre>
     *     {@code
     *     <activity
     *         android:name=".YourActivity"
     *         android:configChanges="orientation|screenSize">
     *         ...
     *     </activity>
     *     }
     *     </pre>
     *     如果不配置此属性，切换全屏时会触发 Activity 重建，导致播放状态丢失和重新起播。
     *     配置后，Activity 不会重建，可以安全地切换屏幕方向，保持播放状态。</li>
     * </ul>
     * </p>
     * <p>
     * Fullscreen Management Slot
     * <p>
     * Manages fullscreen switching logic for the player, including entering/exiting fullscreen, immersive fullscreen settings, etc.
     * This is a pure logic management slot that does not render any UI content.
     * </p>
     * <p>
     * <strong>Important Notes</strong>:
     * <ul>
     *     <li><strong>Required Slot</strong>: This slot is required. Do not easily customize its implementation, otherwise the fullscreen functionality may fail.
     *     Fullscreen functionality involves Activity-level operations (screen orientation, system UI, View hierarchy switching),
     *     which need to be consistent with FullscreenSlot's implementation logic.</li>
     *     <li><strong>Configuration Requirement</strong>: Activities using this slot must configure in AndroidManifest.xml:
     *     <pre>
     *     {@code
     *     <activity
     *         android:name=".YourActivity"
     *         android:configChanges="orientation|screenSize">
     *         ...
     *     </activity>
     *     }
     *     </pre>
     *     Without this configuration, switching to fullscreen will trigger Activity recreation, causing playback state loss and restart.
     *     With this configuration, Activity will not be recreated, allowing safe screen orientation switching while maintaining playback state.</li>
     * </ul>
     * </p>
     */
    FULLSCREEN,

    /**
     * 手势控制插槽
     * <p>
     * 用于处理播放器手势控制（单击、双击、长按、拖动等）。
     * </p>
     * <p>
     * Gesture Control Slot
     * <p>
     * Used for handling player gesture controls (tap, double-tap, long-press, drag, etc.).
     * </p>
     */
    GESTURE_CONTROL,

    /**
     * 横屏观看提示插槽
     * <p>
     * 当检测到视频为横屏且显示区域为竖屏时，提示用户全屏观看。
     * </p>
     * <p>
     * Landscape Hint Slot
     * <p>
     * Prompts the user to view in fullscreen when a landscape video is detected in a portrait display area.
     * </p>
     */
    LANDSCAPE_HINT,

    /**
     * 封面图插槽
     * <p>
     * 用于显示视频封面图，覆盖在 Surface 之上。
     * </p>
     * <p>
     * Cover Image Slot
     * <p>
     * Used to display video cover image, overlaying on top of Surface.
     * </p>
     */
    COVER,

    /**
     * 中心显示插槽
     * <p>
     * 用于显示中心区域的状态信息（如倍速、亮度、音量等）。
     * </p>
     * <p>
     * Center Display Slot
     * <p>
     * Used to display status information in the center area (such as speed, brightness, volume, etc.).
     * </p>
     */
    CENTER_DISPLAY,

    /**
     * 播放状态插槽
     * <p>
     * 用于显示播放状态（如错误提示、加载中等）。
     * </p>
     * <p>
     * Play State Slot
     * <p>
     * Used to display playback status (such as error tips, loading, etc.).
     * </p>
     */
    PLAY_STATE,

    /**
     * 日志面板插槽
     * <p>
     * 用于显示播放器日志信息，便于调试和问题排查。
     * </p>
     * <p>
     * Log Panel Slot
     * <p>
     * Used to display player log information, facilitating debugging and problem troubleshooting.
     * </p>
     */
    LOG_PANEL,

    /**
     * 顶部控制栏插槽
     * <p>
     * 显示返回按钮、标题、设置等。
     * </p>
     * <p>
     * Top Control Bar Slot
     * <p>
     * Displays back button, title, settings, etc.
     * </p>
     */
    TOP_BAR,

    /**
     * 底部控制栏插槽
     * <p>
     * 显示播放控制、进度条、全屏切换等。
     * </p>
     * <p>
     * Bottom Control Bar Slot
     * <p>
     * Displays playback controls, seek bar, fullscreen toggle, etc.
     * </p>
     */
    BOTTOM_BAR,

    /**
     * 设置菜单插槽
     * <p>
     * 用于显示设置菜单（如倍速、清晰度、镜像、旋转等）。
     * </p>
     * <p>
     * Setting Menu Slot
     * </p>
     * <p>
     * Used to display the setting menu (such as speed, quality, mirror, rotation, etc.).
     * </p>
     */
    SETTING_MENU,

}
