package com.aliyun.playerkit.strategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.core.IPlayerStateStore;

/**
 * 策略上下文
 * <p>
 * 提供只读的播放器信息，供策略使用。
 * 策略不能通过此上下文修改播放器状态，只能读取信息。
 * </p>
 * <p>
 * Strategy Context
 * <p>
 * Provides read-only player information for strategies.
 * Strategies cannot modify player state through this context.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public class StrategyContext {

    /**
     * 播放器唯一标识
     */
    private final String playerId;

    /**
     * 播放数据模型，包含视频源、标题等信息
     */
    private final AliPlayerModel model;

    /**
     * 播放器状态存储，提供对播放状态的只读访问
     */
    private final IPlayerStateStore stateStore;

    /**
     * 创建策略上下文
     *
     * @param playerId   播放器 ID，不能为 null
     * @param model      播放模型，可能为 null（在某些场景下可能未设置）
     * @param stateStore 播放状态存储（只读），不能为 null
     */
    public StrategyContext(@NonNull String playerId, @Nullable AliPlayerModel model, @NonNull IPlayerStateStore stateStore) {
        this.playerId = playerId;
        this.model = model;
        this.stateStore = stateStore;
    }

    /**
     * 获取播放器 ID
     *
     * @return 播放器 ID，不为 null
     */
    @NonNull
    public String getPlayerId() {
        return playerId;
    }

    /**
     * 获取播放数据模型
     *
     * @return 播放数据模型，可能为 null
     */
    @Nullable
    public AliPlayerModel getModel() {
        return model;
    }

    /**
     * 获取播放器状态存储
     * <p>
     * 提供对播放器状态（如播放状态、时长、当前位置等）的只读访问。
     * 策略可以通过此接口主动查询当前状态，而无需等待事件通知。
     * </p>
     *
     * @return 播放器状态存储，不为 null
     */
    @NonNull
    public IPlayerStateStore getPlayerStateStore() {
        return stateStore;
    }
}
