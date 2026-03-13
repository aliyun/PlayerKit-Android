package com.aliyun.playerkit.event;

import androidx.annotation.NonNull;

/**
 * AliPlayerKit 播放事件基类
 * <p>
 * 所有播放事件都应该继承此类，提供统一的事件结构。
 * 每个事件都包含播放器 ID，用于标识事件来源。
 * </p>
 * <p>
 * AliPlayerKit Player Event Base Class
 * <p>
 * All player events should extend this class to provide a unified event structure.
 * Each event contains a player ID to identify the event source.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public abstract class PlayerEvent {

    /**
     * 播放器 ID
     * <p>
     * 用于标识事件来源的播放器实例。
     * </p>
     * <p>
     * Player ID
     * <p>
     * Used to identify the player instance that generated the event.
     * </p>
     */
    @NonNull
    public final String playerId;

    /**
     * 构造函数
     *
     * @param playerId 播放器 ID，不能为 null
     */
    protected PlayerEvent(@NonNull String playerId) {
        this.playerId = playerId;
    }
}
