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
}
