package com.aliyun.playerkit.event;

import androidx.annotation.NonNull;

/**
 * 控制栏事件
 * <p>
 * 用于同步顶部栏和底部栏的显示状态。
 * </p>
 * <p>
 * Control Bar Events
 * <p>
 * Used to synchronize the display state of the top and bottom bars.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public final class ControlBarEvents {

    private ControlBarEvents() {
        throw new UnsupportedOperationException("Cannot instantiate ControlBarEvents");
    }

    /**
     * 显示控制栏事件
     * <p>
     * 当用户与控制栏交互时触发，用于显示控制栏。
     * </p>
     */
    public static final class Show extends PlayerEvent {
        public Show(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 隐藏控制栏事件
     * <p>
     * 当用户与控制栏交互时触发，用于隐藏控制栏。
     * </p>
     */
    public static final class Hide extends PlayerEvent {
        public Hide(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 重置控制栏计时器事件
     * <p>
     * 当用户与控制栏交互时触发，用于重置自动隐藏计时器。
     * </p>
     */
    public static final class ResetTimer extends PlayerEvent {
        public ResetTimer(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 显示设置界面事件
     * <p>
     * 当用户与控制栏交互时触发，用于显示设置界面。
     * </p>
     */
    public static final class ShowSettings extends PlayerEvent {
        public ShowSettings(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 显示倍速选择面板事件
     * <p>
     * 触发横屏场景下的倍速选择面板。
     * </p>
     */
    public static final class ShowSpeedPanel extends PlayerEvent {
        public ShowSpeedPanel(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 显示清晰度选择面板事件
     * <p>
     * 触发横屏场景下的清晰度选择面板。
     * </p>
     */
    public static final class ShowQualityPanel extends PlayerEvent {
        public ShowQualityPanel(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * Seek 缩略图显示事件
     * <p>
     * 当用户开始拖动进度条时触发，通知 SeekThumbnailSlot 显示缩略图浮层。
     * </p>
     * <p>
     * Show Seek Thumbnail Event
     * <p>
     * Triggered when user starts dragging the progress bar, notifying SeekThumbnailSlot to show the thumbnail overlay.
     * </p>
     */
    public static final class ShowSeekThumbnail extends PlayerEvent {
        public ShowSeekThumbnail(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * Seek 缩略图更新事件
     * <p>
     * 用户拖动进度条过程中触发，通知 SeekThumbnailSlot 更新缩略图、章节高亮和时间显示。
     * </p>
     * <p>
     * Update Seek Thumbnail Event
     * <p>
     * Triggered during progress bar dragging, notifying SeekThumbnailSlot to update thumbnail, chapter highlight, and time display.
     * </p>
     */
    public static final class UpdateSeekThumbnail extends PlayerEvent {
        private final long positionMs;
        private final long durationMs;

        public UpdateSeekThumbnail(@NonNull String playerId, long positionMs, long durationMs) {
            super(playerId);
            this.positionMs = positionMs;
            this.durationMs = durationMs;
        }

        public long getPositionMs() {
            return positionMs;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }

    /**
     * Seek 缩略图隐藏事件
     * <p>
     * 当用户停止拖动进度条时触发，通知 SeekThumbnailSlot 隐藏缩略图浮层。
     * </p>
     * <p>
     * Hide Seek Thumbnail Event
     * <p>
     * Triggered when user stops dragging the progress bar, notifying SeekThumbnailSlot to hide the thumbnail overlay.
     * </p>
     */
    public static final class HideSeekThumbnail extends PlayerEvent {
        public HideSeekThumbnail(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 显示章节面板
     * <p>
     * Show chapter panel
     * </p>
     */
    public static final class ShowChapterPanel extends PlayerEvent {
        public ShowChapterPanel(@NonNull String playerId) {
            super(playerId);
        }
    }
}
