package com.aliyun.playerkit.converter;

import androidx.annotation.NonNull;

import com.aliyun.player.IPlayer;
import com.aliyun.playerkit.data.PlayerState;

/**
 * 播放器状态转换工具类
 * <p>
 * 提供 IPlayer 状态与 {@link PlayerState} 之间的转换功能。
 * </p>
 * <p>
 * Player State Converter Utility
 * <p>
 * Provides conversion between IPlayer state and {@link PlayerState}.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public final class PlayerStateConverter {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private PlayerStateConverter() {
        throw new UnsupportedOperationException("Cannot instantiate PlayerStateConverter");
    }

    /**
     * 将 IPlayer 的状态值转换为 {@link PlayerState}
     * <p>
     * 状态映射关系：
     * <ul>
     *     <li>{@link IPlayer#unknow} (-1) -> {@link PlayerState#UNKNOWN}</li>
     *     <li>{@link IPlayer#idle} (0) -> {@link PlayerState#IDLE}</li>
     *     <li>{@link IPlayer#initalized} (1) -> {@link PlayerState#INITIALIZED}</li>
     *     <li>{@link IPlayer#prepared} (2) -> {@link PlayerState#PREPARED}</li>
     *     <li>{@link IPlayer#started} (3) -> {@link PlayerState#PLAYING}</li>
     *     <li>{@link IPlayer#paused} (4) -> {@link PlayerState#PAUSED}</li>
     *     <li>{@link IPlayer#stopped} (5) -> {@link PlayerState#STOPPED}</li>
     *     <li>{@link IPlayer#completion} (6) -> {@link PlayerState#COMPLETED}</li>
     *     <li>{@link IPlayer#error} (7) -> {@link PlayerState#ERROR}</li>
     * </ul>
     * </p>
     * <p>
     * Convert IPlayer state value to {@link PlayerState}
     * </p>
     *
     * @param playerState IPlayer 的状态值（如 IPlayer.stopped）
     * @return 对应的 {@link PlayerState}，如果状态值未知则返回 {@link PlayerState#UNKNOWN}
     */
    @NonNull
    public static PlayerState convert(int playerState) {
        switch (playerState) {
            case IPlayer.idle:
                return PlayerState.IDLE;
            case IPlayer.initalized:
                return PlayerState.INITIALIZED;
            case IPlayer.prepared:
                return PlayerState.PREPARED;
            case IPlayer.started:
                return PlayerState.PLAYING;
            case IPlayer.paused:
                return PlayerState.PAUSED;
            case IPlayer.stopped:
                return PlayerState.STOPPED;
            case IPlayer.completion:
                return PlayerState.COMPLETED;
            case IPlayer.error:
                return PlayerState.ERROR;
            default:
                return PlayerState.UNKNOWN;
        }
    }
}
