package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerLifecycleEvents;
import com.aliyun.playerkit.logging.LogHub;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 复用池生命周期策略
 * <p>
 * 实现对象池模式的生命周期管理：复用播放器实例以提高性能。
 * 维护空闲池和活跃列表，限制最大实例数量。
 * 适用于：列表播放（ListView/RecyclerView）、短视频流（TikTok类）等需要频繁切换视频源的场景。
 * </p>
 * Reuse Pool Lifecycle Strategy
 * <p>
 * Implements object pool pattern for lifecycle management: reuses player instances for better performance.
 * Maintains idle pool and active list, limits maximum instance count.
 * Suitable for: List playback (ListView/RecyclerView), short video feeds (TikTok like), etc. where video sources switch frequently.
 * </p>
 *
 * @author keria
 * @date 2025/11/27
 */
public class ReusePoolLifecycleStrategy extends BaseLifecycleStrategy {

    private static final String TAG = "ReusePoolLifecycleStrategy";

    /**
     * 空闲播放器池，使用 LIFO（后进先出）策略
     */
    private final Deque<IMediaPlayer> playerPool = new LinkedList<>();
    /**
     * 当前活跃的播放器列表
     */
    private final Deque<IMediaPlayer> activePlayers = new LinkedList<>();

    /**
     * 私有构造函数，防止外部实例化
     * <p>
     * 此类使用单例模式，应通过 {@link #getInstance()} 获取实例。
     * </p>
     */
    private ReusePoolLifecycleStrategy() {
    }

    /**
     * 获取复用池策略的单例实例
     * <p>
     * 返回全局唯一的 ReusePoolLifecycleStrategy 实例。
     * </p>
     * <p>
     * Get Singleton Instance of Reuse Pool Strategy
     * <p>
     * Returns the globally unique ReusePoolLifecycleStrategy instance.
     * </p>
     *
     * @return 复用池策略的单例实例
     */
    public static ReusePoolLifecycleStrategy getInstance() {
        return ReusePoolLifecycleStrategy.Holder.INSTANCE;
    }

    /**
     * 单例持有者，使用静态内部类实现延迟初始化
     */
    private static class Holder {
        static final ReusePoolLifecycleStrategy INSTANCE = new ReusePoolLifecycleStrategy();
    }

    /**
     * 获取播放器实例
     * <p>
     * 优先从空闲池中获取可复用的播放器实例（LIFO 策略）。
     * 如果池为空，则创建新的播放器实例。
     * 获取到的播放器会被添加到活跃列表中。
     * </p>
     * <p>
     * Acquire Player Instance
     * <p>
     * First tries to get a reusable player instance from the idle pool (LIFO strategy).
     * If the pool is empty, creates a new player instance.
     * The acquired player is added to the active list.
     * </p>
     *
     * @param context  应用上下文，用于创建新实例，不能为 null
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @return 播放器实例（可能是复用的或新创建的），不会为 null
     */
    @NonNull
    @Override
    public IMediaPlayer acquire(@NonNull Context context, @NonNull String uniqueId) {
        synchronized (this) {
            // LIFO: Poll from the head (which corresponds to push at head)
            // Deque.pop() throws if empty, poll() returns null.
            // We want LIFO, so if we use push() to add, we should use pop() or poll() to retrieve from the same end.
            // Stack: push() -> adds to front. pop() -> removes from front.
            // LinkedList: push() is equivalent to addFirst(). poll() is equivalent to pollFirst().
            // So push/poll combo gives LIFO.

            IMediaPlayer pooledPlayer = playerPool.poll(); // Retrieves and removes the head
            if (pooledPlayer != null) {
                LogHub.i(TAG, "Acquired player from pool (LIFO) for uniqueId=" + uniqueId + ": " + pooledPlayer.getPlayerId());
                activePlayers.add(pooledPlayer);
                PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerReused(pooledPlayer.getPlayerId()));
                return pooledPlayer;
            }
        }

        IMediaPlayer newPlayer = createPlayer(context);
        LogHub.i(TAG, "Created new player instance for uniqueId=" + uniqueId + ": " + newPlayer.getPlayerId());

        synchronized (this) {
            activePlayers.add(newPlayer);
        }
        return newPlayer;
    }

    /**
     * 回收播放器实例
     * <p>
     * 将播放器从活跃列表中移除，并根据情况决定是放入空闲池还是立即销毁：
     * <ul>
     *     <li>如果 force 为 true，立即销毁实例</li>
     *     <li>如果池已满（达到 maxPoolSize），立即销毁实例</li>
     *     <li>否则，停止播放器并放入空闲池供后续复用</li>
     * </ul>
     * </p>
     * <p>
     * Recycle Player Instance
     * <p>
     * Removes the player from the active list and decides whether to put it into the idle pool or destroy it immediately:
     * <ul>
     *     <li>If force is true, destroy the instance immediately</li>
     *     <li>If the pool is full (reached maxPoolSize), destroy the instance immediately</li>
     *     <li>Otherwise, stop the player and put it into the idle pool for future reuse</li>
     * </ul>
     * </p>
     *
     * @param player   要回收的播放器实例，可以为 null（null 时不执行任何操作）
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @param force    是否强制销毁。如果为 true，则忽略池化策略，直接销毁实例
     */
    @Override
    public void recycle(@Nullable IMediaPlayer player, @NonNull String uniqueId, boolean force) {
        if (player == null) return;

        synchronized (this) {
            activePlayers.remove(player);

            if (force || playerPool.size() >= maxPoolSize) {
                if (!force) {
                    PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerEvicted(player.getPlayerId()));
                }
                destroyPlayer(player);
                LogHub.i(TAG, (force ? "Force destroyed" : "Pool full, destroyed") + " player for uniqueId=" + uniqueId);
                return;
            }

            try {
                player.stop();
            } catch (Exception e) {
                LogHub.e(TAG, "Error stopping player for pool", e);
                destroyPlayer(player);
                return;
            }

            // LIFO: Push to the front
            playerPool.push(player);
            LogHub.i(TAG, "Recycled player to pool (LIFO) for uniqueId=" + uniqueId + ", pool size: " + playerPool.size());
        }
    }

    /**
     * 清空所有资源
     * <p>
     * 只清理空闲池中的播放器实例，不破坏正在使用的活跃播放器。
     * 活跃播放器会继续正常工作，直到被正常回收。
     * </p>
     * <p>
     * Clear All Resources
     * <p>
     * Only clears players in the idle pool, does not destroy active players in use.
     * Active players will continue to work normally until they are properly recycled.
     * </p>
     */
    @Override
    public void clear() {
        synchronized (this) {
            // 只清理空闲池中的播放器，不破坏正在使用的播放器
            for (IMediaPlayer player : playerPool) {
                destroyPlayer(player);
            }
            playerPool.clear();

            LogHub.i(TAG, "Cleared idle player pool. Active players remain untouched.");
        }
    }

    /**
     * 预加载播放器实例
     * <p>
     * 提前创建播放器实例并放入空闲池，以减少后续 acquire 时的创建耗时。
     * 预加载的数量受 maxPoolSize 限制，不会超过池的最大容量。
     * </p>
     * <p>
     * Preload Player Instances
     * <p>
     * Creates player instances in advance and puts them into the idle pool to reduce
     * creation latency for subsequent acquire calls.
     * The preload count is limited by maxPoolSize and will not exceed the pool's maximum capacity.
     * </p>
     *
     * @param context 应用上下文，用于创建播放器实例，不能为 null
     * @param count   建议预加载的数量（实际数量受 maxPoolSize 限制）
     */
    @Override
    public void preload(@NonNull Context context, int count) {
        if (count <= 0) return;

        synchronized (this) {
            int currentSize = playerPool.size() + activePlayers.size();
            int needed = Math.min(count, maxPoolSize - currentSize);

            if (needed <= 0) {
                LogHub.i(TAG, "Preload skipped, pool full/enough.");
                return;
            }

            LogHub.i(TAG, "Preloading " + needed + " players...");
            for (int i = 0; i < needed; i++) {
                try {
                    // LIFO: Preloaded players are pushed
                    playerPool.push(createPlayer(context));
                } catch (Exception e) {
                    LogHub.e(TAG, "Preload failed", e);
                }
            }
        }
    }
}
