package com.aliyun.playerkit.event;

import androidx.annotation.NonNull;

/**
 * 全屏事件
 * <p>
 * 用于全屏模式切换的事件定义。
 * </p>
 * <p>
 * Fullscreen Events
 * <p>
 * Event definitions for fullscreen mode switching.
 * </p>
 *
 * @author keria
 * @date 2025/12/30
 */
public final class FullscreenEvents {

    private FullscreenEvents() {
        throw new UnsupportedOperationException("Cannot instantiate FullscreenEvents");
    }

    /**
     * 切换全屏事件
     * <p>
     * 当需要切换全屏状态时触发（如果当前是全屏则退出，否则进入）。
     * </p>
     * <p>
     * Toggle Fullscreen Event
     * <p>
     * Triggered when toggling fullscreen state is needed (exit if currently fullscreen, enter otherwise).
     * </p>
     */
    public static final class Toggle extends PlayerEvent {
        public Toggle(@NonNull String playerId) {
            super(playerId);
        }
    }
}

