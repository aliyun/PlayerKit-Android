package com.aliyun.playerkit.data;

import static java.lang.annotation.ElementType.TYPE_USE;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AliPlayerKit 播放场景类型定义
 * <p>
 * 定义了播放器在不同业务场景下的行为模式。
 * 不同的场景类型会影响播放器的 UI 显示、功能可用性和交互方式。
 * </p>
 * <p>
 * AliPlayerKit Playback Scene Type Definition
 * <p>
 * Defines the behavior patterns of the player in different business scenarios.
 * Different scene types affect the player's UI display, feature availability, and interaction methods.
 * </p>
 *
 * @author keria
 * @date 2024/11/26
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(TYPE_USE)
@IntDef({
        SceneType.VOD,
        SceneType.LIVE,
        SceneType.VIDEO_LIST,
        SceneType.RESTRICTED,
        SceneType.MINIMAL,
})
public @interface SceneType {

    /**
     * 点播场景
     * <p>
     * 适用场景：常规视频播放
     * </p>
     * <p>
     * 功能特性：支持所有播放控制功能，包括播放/暂停、进度拖拽、快进/快退、
     * 倍速播放、音量/亮度调节、全屏切换、设置菜单等
     * </p>
     * <p>
     * VOD Scene
     * <p>
     * Use case: Regular video playback
     * </p>
     * <p>
     * Features: Supports all playback controls including play/pause, seek,
     * fast forward/rewind, speed control, volume/brightness adjustment,
     * fullscreen toggle, settings menu, etc.
     * </p>
     */
    int VOD = 0;

    /**
     * 直播场景
     * <p>
     * 适用场景：实时直播流播放
     * </p>
     * <p>
     * 功能特性：支持播放/暂停、刷新、音量/亮度调节、全屏切换、设置菜单等，
     * 但不支持进度拖拽、快进/快退、倍速播放等时间轴相关操作
     * </p>
     * <p>
     * Live Scene
     * <p>
     * Use case: Real-time live stream playback
     * </p>
     * <p>
     * Features: Supports play/pause, refresh, volume/brightness adjustment, fullscreen toggle,
     * settings menu, but excludes timeline-related operations like seek,
     * fast forward/rewind, speed control
     * </p>
     */
    int LIVE = 1;

    /**
     * 列表播放场景
     * <p>
     * 适用场景：视频列表中的播放器，如信息流、短视频列表等
     * </p>
     * <p>
     * 功能特性：支持基本播放控制，但禁用垂直手势（音量/亮度调节），
     * 避免与列表滚动手势冲突
     * </p>
     * <p>
     * Video List Scene
     * <p>
     * Use case: Player in video lists, such as feeds or short video lists
     * </p>
     * <p>
     * Features: Supports basic playback controls but disables vertical gestures
     * (volume/brightness adjustment) to avoid conflicts with list scrolling
     * </p>
     */
    int VIDEO_LIST = 2;

    /**
     * 受限播放场景（限制时间轴操作）
     * <p>
     * 适用场景：教育培训、考试监控、演示展示等需要限制用户跳跃播放的场景
     * </p>
     * <p>
     * 功能特性：支持播放/暂停、音量/亮度调节、全屏切换、设置菜单、字幕显示等，
     * 但禁用进度拖拽、快进/快退、倍速播放等时间轴相关操作，
     * 确保用户只能按正常速度顺序观看，无法跳过内容
     * </p>
     * <p>
     * Restricted Scene (Timeline operations restricted)
     * <p>
     * Use case: Educational training, exam monitoring, demonstrations where
     * timeline manipulation needs to be restricted
     * </p>
     * <p>
     * Features: Supports play/pause, volume/brightness adjustment, fullscreen toggle,
     * settings menu, subtitle display, but disables timeline-related operations
     * like seek, fast forward/rewind, speed control, ensuring users can only
     * watch content at normal speed in sequence without skipping
     * </p>
     */
    int RESTRICTED = 3;

    /**
     * 最小化播放场景（仅播放视图，无任何UI）
     * <p>
     * 适用场景：背景视频、装饰性视频、嵌入式播放器、自定义UI覆盖等
     * </p>
     * <p>
     * 功能特性：仅显示纯净的视频播放画面，不显示任何UI元素，
     * 包括封面图、字幕、播放状态、控制界面等，
     * 适合需要完全自定义UI或作为背景元素的场景
     * </p>
     * <p>
     * Minimal Scene (Only surface view, no UI)
     * <p>
     * Use case: Background videos, decorative videos, embedded players,
     * custom UI overlays
     * </p>
     * <p>
     * Features: Only displays pure video playback surface without any UI elements
     * including cover image, subtitles, play state, control interfaces,
     * suitable for scenarios requiring completely custom UI or
     * background elements
     * </p>
     */
    int MINIMAL = 4;
}
