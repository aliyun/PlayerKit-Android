package com.aliyun.playerkit.event;

import androidx.annotation.NonNull;

/**
 * AliPlayerKit 手势事件定义
 * <p>
 * 定义了所有手势相关的事件类型，所有事件都继承自 {@link PlayerEvent}。
 * 这些事件描述手势行为本身，而不包含业务逻辑，业务逻辑应由事件订阅者实现。
 * </p>
 * <p>
 * AliPlayerKit Gesture Events Definition
 * <p>
 * Defines all gesture-related event types, all events extend from {@link PlayerEvent}.
 * These events describe gesture behaviors without business logic, which should be implemented by event subscribers.
 * </p>
 *
 * @author keria
 * @date 2025/12/09
 */
public final class GestureEvents {

    private GestureEvents() {
        throw new UnsupportedOperationException("Cannot instantiate GestureEvents");
    }

    /**
     * 单击事件
     * <p>
     * 当用户单击屏幕时触发。
     * </p>
     * <p>
     * Single Tap Event
     * <p>
     * Triggered when user taps the screen once.
     * </p>
     */
    public static final class SingleTapEvent extends PlayerEvent {
        /**
         * 点击位置 X 坐标
         */
        public final float x;

        /**
         * 点击位置 Y 坐标
         */
        public final float y;

        public SingleTapEvent(@NonNull String playerId, float x, float y) {
            super(playerId);
            this.x = x;
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "SingleTapEvent{" +
                    "x=" + x +
                    ", y=" + y +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 双击事件
     * <p>
     * 当用户双击屏幕时触发。
     * </p>
     * <p>
     * Double Tap Event
     * <p>
     * Triggered when user double-taps the screen.
     * </p>
     */
    public static final class DoubleTapEvent extends PlayerEvent {
        /**
         * 点击位置 X 坐标
         */
        public final float x;

        /**
         * 点击位置 Y 坐标
         */
        public final float y;

        public DoubleTapEvent(@NonNull String playerId, float x, float y) {
            super(playerId);
            this.x = x;
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "DoubleTapEvent{" +
                    "x=" + x +
                    ", y=" + y +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 长按开始事件
     * <p>
     * 当用户开始长按屏幕时触发。
     * 通常用于实现倍速播放等功能。
     * </p>
     * <p>
     * Long Press Event
     * <p>
     * Triggered when user starts a long press on the screen.
     * Typically used for implementing speed playback and other features.
     * </p>
     */
    public static final class LongPressEvent extends PlayerEvent {
        /**
         * 长按位置 X 坐标（相对于 View 的坐标）
         */
        public final float x;

        /**
         * 长按位置 Y 坐标（相对于 View 的坐标）
         */
        public final float y;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         * @param x        长按位置 X 坐标
         * @param y        长按位置 Y 坐标
         */
        public LongPressEvent(@NonNull String playerId, float x, float y) {
            super(playerId);
            this.x = x;
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "LongPressEvent{" +
                    "x=" + x +
                    ", y=" + y +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 长按结束事件
     * <p>
     * 当用户结束长按时触发（手指抬起或取消）。
     * 通常用于恢复正常的播放速度。
     * </p>
     * <p>
     * Long Press End Event
     * <p>
     * Triggered when user ends a long press (finger up or cancelled).
     * Typically used to restore normal playback speed.
     * </p>
     */
    public static final class LongPressEndEvent extends PlayerEvent {
        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public LongPressEndEvent(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "LongPressEndEvent{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 水平拖动开始事件
     * <p>
     * 当用户开始水平拖动时触发。
     * 通常用于实现播放进度调节功能。
     * </p>
     * <p>
     * Horizontal Drag Start Event
     * <p>
     * Triggered when user starts dragging horizontally.
     * Typically used for implementing playback progress adjustment.
     * </p>
     */
    public static final class HorizontalDragStartEvent extends PlayerEvent {
        /**
         * 拖动起始 X 坐标（相对于 View 的坐标）
         */
        public final float startX;

        /**
         * 拖动起始 Y 坐标（相对于 View 的坐标）
         */
        public final float startY;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         * @param startX   拖动起始 X 坐标
         * @param startY   拖动起始 Y 坐标
         */
        public HorizontalDragStartEvent(@NonNull String playerId, float startX, float startY) {
            super(playerId);
            this.startX = startX;
            this.startY = startY;
        }

        @NonNull
        @Override
        public String toString() {
            return "HorizontalDragStartEvent{" +
                    "startY=" + startY +
                    ", startX=" + startX +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 水平拖动更新事件
     * <p>
     * 当用户水平拖动屏幕时持续触发，提供增量变化（百分比）。
     * 用于实时更新播放进度显示。
     * </p>
     * <p>
     * Horizontal Drag Update Event
     * <p>
     * Triggered continuously when user drags horizontally, providing delta changes as a percentage of width.
     * Used for real-time playback progress display updates.
     * </p>
     */
    public static final class HorizontalDragUpdateEvent extends PlayerEvent {
        /**
         * X 轴增量百分比（相对于 View 宽度的比例）
         * <p>
         * 正值表示向右拖动，负值表示向左拖动。
         * 范围通常在 -1.0 到 1.0 之间。
         * </p>
         */
        public final float deltaPercent;

        /**
         * 构造函数
         *
         * @param playerId     播放器 ID，不能为 null
         * @param deltaPercent 增量百分比，正值向右，负值向左
         */
        public HorizontalDragUpdateEvent(@NonNull String playerId, float deltaPercent) {
            super(playerId);
            this.deltaPercent = deltaPercent;
        }

        @NonNull
        @Override
        public String toString() {
            return "HorizontalDragUpdateEvent{" +
                    "deltaPercent=" + deltaPercent +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 水平拖动结束事件
     * <p>
     * 当用户结束水平拖动时触发（手指抬起）。
     * 通常用于执行最终的播放进度跳转。
     * </p>
     * <p>
     * Horizontal Drag End Event
     * <p>
     * Triggered when user ends horizontal drag (finger up).
     * Typically used to perform final playback position seek.
     * </p>
     */
    public static final class HorizontalDragEndEvent extends PlayerEvent {
        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public HorizontalDragEndEvent(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "HorizontalDragEndEvent{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 左侧垂直拖动开始事件
     * <p>
     * 当用户开始在屏幕左侧垂直拖动时触发。
     * </p>
     * <p>
     * Left Vertical Drag Start Event
     * <p>
     * Triggered when user starts dragging vertically on the left side.
     * </p>
     */
    public static final class LeftVerticalDragStartEvent extends PlayerEvent {
        /**
         * 拖动起始 X 坐标
         */
        public final float startX;

        /**
         * 拖动起始 Y 坐标
         */
        public final float startY;

        public LeftVerticalDragStartEvent(@NonNull String playerId, float startX, float startY) {
            super(playerId);
            this.startX = startX;
            this.startY = startY;
        }

        @NonNull
        @Override
        public String toString() {
            return "LeftVerticalDragStartEvent{" +
                    "startX=" + startX +
                    ", startY=" + startY +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 左侧垂直拖动更新事件
     * <p>
     * 当用户在屏幕左侧垂直拖动时持续触发，提供增量变化（百分比）。
     * </p>
     * <p>
     * Left Vertical Drag Update Event
     * <p>
     * Triggered continuously when user drags vertically on the left side, providing delta changes as a percentage of height.
     * </p>
     */
    public static final class LeftVerticalDragUpdateEvent extends PlayerEvent {
        /**
         * Y 轴增量百分比（相对于 View 高度的比例，0.0 - 1.0）
         */
        public final float deltaPercent;

        /**
         * 当前值百分比（0.0 - 1.0）
         * <p>
         * 表示当前的水位（如亮度、音量）占最大值的百分比。
         * </p>
         */
        public final float currentPercent;

        public LeftVerticalDragUpdateEvent(@NonNull String playerId, float deltaPercent, float currentPercent) {
            super(playerId);
            this.deltaPercent = deltaPercent;
            this.currentPercent = currentPercent;
        }

        @NonNull
        @Override
        public String toString() {
            return "LeftVerticalDragUpdateEvent{" +
                    "deltaPercent=" + deltaPercent +
                    ", currentPercent=" + currentPercent +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 右侧垂直拖动开始事件
     * <p>
     * 当用户开始在屏幕右侧垂直拖动时触发。
     * </p>
     * <p>
     * Right Vertical Drag Start Event
     * <p>
     * Triggered when user starts dragging vertically on the right side.
     * </p>
     */
    public static final class RightVerticalDragStartEvent extends PlayerEvent {
        /**
         * 拖动起始 X 坐标
         */
        public final float startX;

        /**
         * 拖动起始 Y 坐标
         */
        public final float startY;

        public RightVerticalDragStartEvent(@NonNull String playerId, float startX, float startY) {
            super(playerId);
            this.startX = startX;
            this.startY = startY;
        }

        @NonNull
        @Override
        public String toString() {
            return "RightVerticalDragStartEvent{" +
                    "startX=" + startX +
                    ", startY=" + startY +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 右侧垂直拖动更新事件
     * <p>
     * 当用户在屏幕右侧垂直拖动时持续触发，提供增量变化（百分比）。
     * </p>
     * <p>
     * Right Vertical Drag Update Event
     * <p>
     * Triggered continuously when user drags vertically on the right side, providing delta changes as a percentage of height.
     * </p>
     */
    public static final class RightVerticalDragUpdateEvent extends PlayerEvent {
        /**
         * Y 轴增量百分比（相对于 View 高度的比例，0.0 - 1.0）
         */
        public final float deltaPercent;

        /**
         * 当前值百分比（0.0 - 1.0）
         * <p>
         * 表示当前的水位（如亮度、音量）占最大值的百分比。
         * </p>
         */
        public final float currentPercent;

        public RightVerticalDragUpdateEvent(@NonNull String playerId, float deltaPercent, float currentPercent) {
            super(playerId);
            this.deltaPercent = deltaPercent;
            this.currentPercent = currentPercent;
        }

        @NonNull
        @Override
        public String toString() {
            return "RightVerticalDragUpdateEvent{" +
                    "deltaPercent=" + deltaPercent +
                    ", currentPercent=" + currentPercent +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 左侧垂直拖动结束事件
     * <p>
     * 当用户结束左侧垂直拖动时触发（手指抬起）。
     * </p>
     * <p>
     * Left Vertical Drag End Event
     * <p>
     * Triggered when user ends left vertical drag (finger up).
     * </p>
     */
    public static final class LeftVerticalDragEndEvent extends PlayerEvent {
        public LeftVerticalDragEndEvent(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "LeftVerticalDragEndEvent{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 右侧垂直拖动结束事件
     * <p>
     * 当用户结束右侧垂直拖动时触发（手指抬起）。
     * </p>
     * <p>
     * Right Vertical Drag End Event
     * <p>
     * Triggered when user ends right vertical drag (finger up).
     * </p>
     */
    public static final class RightVerticalDragEndEvent extends PlayerEvent {
        public RightVerticalDragEndEvent(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "RightVerticalDragEndEvent{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }
}
