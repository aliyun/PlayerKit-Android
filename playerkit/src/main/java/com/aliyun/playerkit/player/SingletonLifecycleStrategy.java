package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerLifecycleEvents;
import com.aliyun.playerkit.logging.LogHub;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 单例生命周期策略
 * <p>
 * 全局维护唯一一个播放器实例。
 * 无论使用什么 uniqueId，获取到的永远是同一个 Player 实例。
 * 当 uniqueId 发生变化时，策略会重置播放器状态以供新的内容使用。
 * </p>
 * <p>
 * Singleton Lifecycle Strategy
 * <p>
 * Maintains a single global player instance.
 * Regardless of the uniqueId, it always returns the same Player instance.
 * </p>
 *
 * @author keria
 * @date 2026/01/12
 */
public class SingletonLifecycleStrategy extends BaseLifecycleStrategy {

    private static final String TAG = "SingletonLifecycleStrategy";

    /**
     * 全局唯一的播放器实例引用，使用原子引用保证线程安全
     */
    private final AtomicReference<IMediaPlayer> globalPlayerRef = new AtomicReference<>();

    /**
     * 私有构造函数，防止外部实例化
     * <p>
     * 此类使用单例模式，应通过 {@link #getInstance()} 获取实例。
     * </p>
     */
    private SingletonLifecycleStrategy() {
    }

    /**
     * 获取单例策略的单例实例
     * <p>
     * 返回全局唯一的 SingletonLifecycleStrategy 实例。
     * </p>
     * <p>
     * Get Singleton Instance of Singleton Strategy
     * <p>
     * Returns the globally unique SingletonLifecycleStrategy instance.
     * </p>
     *
     * @return 单例策略的单例实例
     */
    public static SingletonLifecycleStrategy getInstance() {
        return SingletonLifecycleStrategy.Holder.INSTANCE;
    }

    /**
     * 单例持有者，使用静态内部类实现延迟初始化
     */
    private static class Holder {
        static final SingletonLifecycleStrategy INSTANCE = new SingletonLifecycleStrategy();
    }

    /**
     * 获取播放器实例
     * <p>
     * 无论 uniqueId 为何值，始终返回同一个全局播放器实例。
     * 如果实例不存在，则创建新实例；如果已存在，则直接返回。
     * 使用双重检查锁定（Double-Checked Locking）模式保证线程安全。
     * </p>
     * <p>
     * Acquire Player Instance
     * <p>
     * Always returns the same global player instance regardless of the uniqueId value.
     * If the instance does not exist, creates a new one; if it exists, returns it directly.
     * Uses Double-Checked Locking pattern to ensure thread safety.
     * </p>
     *
     * @param context  应用上下文，用于创建新实例（仅在首次调用时），不能为 null
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null（此策略中此参数不影响返回值）
     * @return 全局唯一的播放器实例，不会为 null
     */
    @NonNull
    @Override
    public IMediaPlayer acquire(@NonNull Context context, @NonNull String uniqueId) {
        IMediaPlayer player = globalPlayerRef.get();
        if (player == null) {
            synchronized (this) {
                player = globalPlayerRef.get();
                if (player == null) {
                    player = createPlayer(context);
                    globalPlayerRef.set(player);
                    LogHub.i(TAG, "Created global singleton player: " + player.getPlayerId());
                }
            }
        } else {
            LogHub.i(TAG, "Reuse global singleton player: " + player.getPlayerId() + " for id: " + uniqueId);
            PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerHit(player.getPlayerId()));
        }
        return player;
    }

    /**
     * 回收播放器实例
     * <p>
     * 根据 force 参数决定处理方式：
     * <ul>
     *     <li>如果 force 为 true，立即销毁全局播放器实例并清空引用</li>
     *     <li>如果 force 为 false，仅停止播放器，但保留实例供后续复用</li>
     * </ul>
     * 注意：如果传入的 player 不是全局单例实例，则忽略此次回收操作。
     * </p>
     * <p>
     * Recycle Player Instance
     * <p>
     * Decides the handling method based on the force parameter:
     * <ul>
     *     <li>If force is true, immediately destroys the global player instance and clears the reference</li>
     *     <li>If force is false, only stops the player but keeps the instance for future reuse</li>
     * </ul>
     * Note: If the passed player is not the global singleton instance, this recycle operation is ignored.
     * </p>
     *
     * @param player   要回收的播放器实例，可以为 null（null 时不执行任何操作）
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null（此策略中此参数不影响处理逻辑）
     * @param force    是否强制销毁。如果为 true，则销毁全局实例；如果为 false，仅停止播放器
     */
    @Override
    public void recycle(@Nullable IMediaPlayer player, @NonNull String uniqueId, boolean force) {
        if (player == null) return;

        if (player != globalPlayerRef.get()) {
            LogHub.w(TAG, "Recycling known player but it's not the singleton instance. Ignoring.");
            return;
        }

        if (force) {
            LogHub.i(TAG, "Force destroying global player");
            destroyPlayer(player);
            globalPlayerRef.set(null);
        } else {
            LogHub.i(TAG, "Recycle called for singleton (soft). Just stopping.");
            try {
                player.stop();
            } catch (Exception e) {
                LogHub.e(TAG, "Error stopping singleton player", e);
            }
        }
    }

    /**
     * 清空所有资源
     * <p>
     * 对于单例策略，clear 操作极具风险，因为播放器可能正在被使用。
     * 此方法不执行任何销毁操作，仅记录警告日志。
     * 如需销毁全局实例，应调用 {@link #recycle(IMediaPlayer, String, boolean)} 并设置 force 为 true。
     * </p>
     * <p>
     * Clear All Resources
     * <p>
     * For singleton strategy, clear operation is extremely risky because the player may be in use.
     * This method does not perform any destroy operation, only logs a warning.
     * To destroy the global instance, call {@link #recycle(IMediaPlayer, String, boolean)} with force set to true.
     * </p>
     */
    @Override
    public void clear() {
        // 对于单例策略，clear 操作极具风险，因为播放器可能正在被使用。
        // 我们不在这里强制销毁播放器，而是依赖 recycle(force=true) 或应用生命周期结束时的自动销毁。
        LogHub.w(TAG, "Clear skipped for Singleton. Use recycle(force=true) if you really want to destroy the instance.");
    }

    /**
     * 预加载播放器实例
     * <p>
     * 如果全局播放器实例尚未创建，则提前创建它。
     * count 参数在此策略中被忽略，因为单例策略只维护一个实例。
     * 使用双重检查锁定模式保证线程安全。
     * </p>
     * <p>
     * Preload Player Instance
     * <p>
     * If the global player instance has not been created, creates it in advance.
     * The count parameter is ignored in this strategy because singleton strategy only maintains one instance.
     * Uses Double-Checked Locking pattern to ensure thread safety.
     * </p>
     *
     * @param context 应用上下文，用于创建播放器实例，不能为 null
     * @param count   预加载数量（此策略中此参数被忽略）
     */
    @Override
    public void preload(@NonNull Context context, int count) {
        if (globalPlayerRef.get() == null) {
            synchronized (this) {
                if (globalPlayerRef.get() == null) {
                    try {
                        IMediaPlayer player = createPlayer(context);
                        globalPlayerRef.set(player);
                        LogHub.i(TAG, "Preloaded global singleton player");
                    } catch (Exception e) {
                        LogHub.e(TAG, "Preload failed", e);
                    }
                }
            }
        }
    }
}
