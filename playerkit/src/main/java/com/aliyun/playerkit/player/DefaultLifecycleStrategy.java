package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogHub;

/**
 * 默认生命周期策略
 * <p>
 * 实现最简单的生命周期管理：每次创建新实例，用完立即销毁。
 * 不进行实例复用，不维护对象池。
 * </p>
 * <p>
 * Default Lifecycle Strategy
 * <p>
 * Implements the simplest lifecycle management: creates a new instance each time and destroys it immediately after use.
 * Does not reuse instances or maintain an object pool.
 * </p>
 *
 * @author keria
 * @date 2025/11/26
 */
public class DefaultLifecycleStrategy extends BaseLifecycleStrategy {

    private static final String TAG = "DefaultLifecycleStrategy";

    /**
     * 获取播放器实例
     * <p>
     * 每次调用都会创建新的播放器实例，不进行任何复用。
     * 这是最简单的策略，适合对内存敏感或不需要复用的场景。
     * </p>
     * <p>
     * Acquire Player Instance
     * <p>
     * Creates a new player instance on each call, with no reuse.
     * This is the simplest strategy, suitable for memory-sensitive scenarios or when reuse is not needed.
     * </p>
     *
     * @param context  应用上下文，用于创建新实例，不能为 null
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @return 新创建的播放器实例，不会为 null
     */
    @NonNull
    @Override
    public IMediaPlayer acquire(@NonNull Context context, @NonNull String uniqueId) {
        IMediaPlayer player = createPlayer(context);
        LogHub.i(TAG, "Created new player instance for uniqueId=" + uniqueId, player.getPlayerId());
        return player;
    }

    /**
     * 回收播放器实例
     * <p>
     * 立即销毁播放器实例，无论 force 参数为何值。
     * 此策略不维护对象池，所有回收的实例都会被立即销毁。
     * </p>
     * <p>
     * Recycle Player Instance
     * <p>
     * Immediately destroys the player instance, regardless of the force parameter.
     * This strategy does not maintain an object pool, and all recycled instances are immediately destroyed.
     * </p>
     *
     * @param player   要回收的播放器实例，可以为 null（null 时不执行任何操作）
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @param force    是否强制销毁（此策略中此参数无效，始终销毁）
     */
    @Override
    public void recycle(@Nullable IMediaPlayer player, @NonNull String uniqueId, boolean force) {
        if (player == null) return;
        destroyPlayer(player);
        LogHub.i(TAG, "Destroyed player instance for uniqueId=" + uniqueId, player.getPlayerId());
    }
}
