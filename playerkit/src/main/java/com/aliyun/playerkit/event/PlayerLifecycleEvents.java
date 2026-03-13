package com.aliyun.playerkit.event;

import androidx.annotation.NonNull;

/**
 * 播放器生命周期策略事件定义
 * <p>
 * 用于通过事件总线观察生命周期策略内部的行为，如创建、销毁、复用和淘汰。
 * </p>
 * <p>
 * Player Lifecycle Strategy Events Definition
 * <p>
 * Used to observe internal behaviors of lifecycle strategies through the event bus,
 * such as creation, destruction, reuse, and eviction.
 * </p>
 *
 * @author keria
 * @date 2026/01/12
 */
public final class PlayerLifecycleEvents {

    private PlayerLifecycleEvents() {
        throw new UnsupportedOperationException("Cannot instantiate PlayerLifecycleEvents");
    }

    /**
     * 播放器创建事件
     * <p>
     * 当策略创建了一个崭新的播放器实例时触发。
     * </p>
     * <p>
     * Player Created Event
     * <p>
     * Triggered when the strategy creates a brand new player instance.
     * </p>
     */
    public static final class PlayerCreated extends PlayerEvent {
        public PlayerCreated(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 播放器销毁事件
     * <p>
     * 当策略彻底销毁了一个播放器实例时触发（通常是 pool 已满或强制销毁）。
     * </p>
     * <p>
     * Player Destroyed Event
     * <p>
     * Triggered when the strategy completely destroys a player instance (usually when the pool is full or forced).
     * </p>
     */
    public static final class PlayerDestroyed extends PlayerEvent {
        public PlayerDestroyed(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 播放器复用事件
     * <p>
     * 当策略从空闲池中复用了一个已有的播放器实例时触发。
     * </p>
     * <p>
     * Player Reused Event
     * <p>
     * Triggered when the strategy reuses an existing player instance from the idle pool.
     * </p>
     */
    public static final class PlayerReused extends PlayerEvent {
        public PlayerReused(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 播放器命中事件
     * <p>
     * 当策略发现 uniqueId 已有绑定的活跃实例，并直接返回该实例时触发。
     * 常见于 IdScopedPool 或 Singleton 策略。
     * </p>
     * <p>
     * Player Hit Event
     * <p>
     * Triggered when the strategy finds an active instance bound to the uniqueId and returns it directly.
     * Common in IdScopedPool or Singleton strategies.
     * </p>
     */
    public static final class PlayerHit extends PlayerEvent {
        public PlayerHit(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 播放器淘汰事件
     * <p>
     * 当策略因容量限制（如 LRU 淘汰）而主动销毁一个播放器实例时触发。
     * </p>
     * <p>
     * Player Evicted Event
     * <p>
     * Triggered when the strategy actively destroys a player instance due to capacity limits (e.g., LRU eviction).
     * </p>
     */
    public static final class PlayerEvicted extends PlayerEvent {
        public PlayerEvicted(@NonNull String playerId) {
            super(playerId);
        }
    }
}
