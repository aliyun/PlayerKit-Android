package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerLifecycleEvents;
import com.aliyun.playerkit.logging.LogHub;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * ID 作用域池生命周期策略
 * <p>
 * 为每个 uniqueId 维护一个独立的播放器实例。
 * 只要 uniqueId 不变，每次 acquire 都会返回同一个 Player 实例。
 * 策略内部使用 LRU（Least Recently Used）机制控制实例数量，
 * 当实例数量超过 {@link #maxPoolSize} 时，会自动销毁最近最少使用的实例。
 * </p>
 * <p>
 * 设计思想参考阿里云微短剧解决方案中的多实例播放器池 LRU 管理机制。
 * </p>
 *
 * <p>
 * Id Scoped Pool Lifecycle Strategy
 * <p>
 * Maintains a separate player instance for each uniqueId.
 * As long as the uniqueId remains the same, acquire will return the same Player instance.
 * The strategy uses an LRU (Least Recently Used) mechanism to control the number of instances.
 * When the number of instances exceeds {@link #maxPoolSize}, the least recently used instance is automatically destroyed.
 * </p>
 * <p>
 * The design is inspired by the multi-instance player pool LRU strategy used in
 * Alibaba Cloud Micro-Drama (Short Video) solution.
 * </p>
 *
 * @author keria
 * @date 2026/01/12
 */
public class IdScopedPoolLifecycleStrategy extends BaseLifecycleStrategy {

    private static final String TAG = "IdScopedPoolLifecycleStrategy";

    /**
     * 播放器映射表，使用 LinkedHashMap 实现 LRU（最近最少使用）机制
     * <p>
     * Key 为 uniqueId，Value 为对应的播放器实例。
     * 使用访问顺序（access order = true），最近访问的条目会被移到末尾。
     * </p>
     */
    private final Map<String, IMediaPlayer> playerMap = new LinkedHashMap<String, IMediaPlayer>(4, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, IMediaPlayer> eldest) {
            if (size() > maxPoolSize) {
                IMediaPlayer player = eldest.getValue();
                if (player != null) {
                    // 销毁被淘汰的实例
                    String playerId = player.getPlayerId();
                    activePlayers.remove(player);
                    destroyPlayer(player);
                    PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerEvicted(playerId));
                    LogHub.i(TAG, "LRU Evicted player for uniqueId=" + eldest.getKey());
                }
                return true;
            }
            return false;
        }
    };

    /**
     * 处于活跃状态（已被 acquire 但未被 recycle）的播放器集合
     */
    private final Set<IMediaPlayer> activePlayers = new HashSet<>();

    /**
     * 预加载但尚未绑定 uniqueId 的播放器队列
     */
    private final Deque<IMediaPlayer> unboundPlayers = new LinkedList<>();

    /**
     * 私有构造函数，防止外部实例化
     * <p>
     * 此类使用单例模式，应通过 {@link #getInstance()} 获取实例。
     * </p>
     */
    private IdScopedPoolLifecycleStrategy() {
    }

    /**
     * 获取 ID 作用域池策略的单例实例
     * <p>
     * 返回全局唯一的 IdScopedPoolLifecycleStrategy 实例。
     * </p>
     * <p>
     * Get Singleton Instance of Id Scoped Pool Strategy
     * <p>
     * Returns the globally unique IdScopedPoolLifecycleStrategy instance.
     * </p>
     *
     * @return ID 作用域池策略的单例实例
     */
    public static IdScopedPoolLifecycleStrategy getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 单例持有者，使用静态内部类实现延迟初始化
     */
    private static class Holder {
        static final IdScopedPoolLifecycleStrategy INSTANCE = new IdScopedPoolLifecycleStrategy();
    }

    /**
     * 当最大池大小更新时的处理逻辑
     * <p>
     * 如果新的池大小小于当前映射表中的实例数量，会立即清理超出限制的实例。
     * 按照 LRU 策略，优先清理最近最少使用的实例。
     * </p>
     * <p>
     * Handle Logic When Max Pool Size is Updated
     * <p>
     * If the new pool size is smaller than the current number of instances in the map,
     * instances exceeding the limit are immediately cleaned up.
     * Following the LRU strategy, the least recently used instances are cleaned up first.
     * </p>
     *
     * @param newSize 新的最大池大小
     */
    @Override
    protected void onMaxPoolSizeUpdated(int newSize) {
        synchronized (this) {
            // Trigger manual prune if the new size is smaller than current map size.
            while (playerMap.size() > newSize) {
                // Determine eldest by iterator (LinkedHashMap iterator returns in insertion/access order)
                String key = playerMap.keySet().iterator().next();
                IMediaPlayer player = playerMap.remove(key);
                if (player != null) {
                    String playerId = player.getPlayerId();
                    activePlayers.remove(player);
                    destroyPlayer(player);
                    PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerEvicted(playerId));
                    LogHub.i(TAG, "Pruned player for uniqueId=" + key + " after resize");
                }
            }
        }
    }

    /**
     * 获取播放器实例
     * <p>
     * 为每个 uniqueId 维护一个独立的播放器实例。
     * 如果该 uniqueId 已存在实例，直接返回（同时更新 LRU 访问顺序）。
     * 如果不存在，优先从 unboundPlayers 队列中获取预加载的实例，否则创建新实例。
     * 当映射表大小超过 maxPoolSize 时，会自动触发 LRU 淘汰机制。
     * </p>
     * <p>
     * Acquire Player Instance
     * <p>
     * Maintains a separate player instance for each uniqueId.
     * If an instance for this uniqueId already exists, returns it directly (updating LRU access order).
     * If not, first tries to get a preloaded instance from the unboundPlayers queue, otherwise creates a new instance.
     * When the map size exceeds maxPoolSize, the LRU eviction mechanism is automatically triggered.
     * </p>
     *
     * @param context  应用上下文，用于创建新实例，不能为 null
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @return 播放器实例（可能是已存在的、预加载的或新创建的），不会为 null
     */
    @NonNull
    @Override
    public IMediaPlayer acquire(@NonNull Context context, @NonNull String uniqueId) {
        synchronized (this) {
            // Trigger access order update
            IMediaPlayer existingPlayer = playerMap.get(uniqueId);
            if (existingPlayer != null) {
                LogHub.i(TAG, "Acquired existing player for uniqueId=" + uniqueId);
                activePlayers.add(existingPlayer);
                PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerHit(existingPlayer.getPlayerId()));
                return existingPlayer;
            }

            // Not found, create new (or get from unbound)
            // Use pollFirst() to get from head (if we treat unbound as Queue)
            IMediaPlayer newPlayer = unboundPlayers.poll();
            boolean isFromUnbound = (newPlayer != null);

            if (newPlayer == null) {
                newPlayer = createPlayer(context);
            }

            // Put into map (triggers removeEldestEntry if full)
            playerMap.put(uniqueId, newPlayer);
            activePlayers.add(newPlayer);

            if (isFromUnbound) {
                PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerReused(newPlayer.getPlayerId()));
            }

            LogHub.i(TAG, "Acquired new player (" + (isFromUnbound ? "preloaded" : "created") + ") for uniqueId=" + uniqueId);
            return newPlayer;
        }
    }

    /**
     * 回收播放器实例
     * <p>
     * 根据 force 参数决定处理方式：
     * <ul>
     *     <li>如果 force 为 true，立即从映射表中移除并销毁实例</li>
     *     <li>如果 force 为 false，仅停止播放器，但保留在映射表中供后续复用</li>
     * </ul>
     * 注意：此策略中，非强制回收时实例不会被移除，而是保留在映射表中。
     * 当映射表超过 maxPoolSize 时，LRU 机制会自动淘汰最近最少使用的实例。
     * </p>
     * <p>
     * Recycle Player Instance
     * <p>
     * Decides the handling method based on the force parameter:
     * <ul>
     *     <li>If force is true, immediately removes from the map and destroys the instance</li>
     *     <li>If force is false, only stops the player but keeps it in the map for future reuse</li>
     * </ul>
     * Note: In this strategy, when recycling is not forced, the instance is not removed but kept in the map.
     * When the map exceeds maxPoolSize, the LRU mechanism automatically evicts the least recently used instance.
     * </p>
     *
     * @param player   要回收的播放器实例，可以为 null（null 时不执行任何操作）
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @param force    是否强制销毁。如果为 true，则从映射表中移除并销毁实例；如果为 false，仅停止播放器
     */
    @Override
    public void recycle(@Nullable IMediaPlayer player, @NonNull String uniqueId, boolean force) {
        if (player == null) return;

        synchronized (this) {
            activePlayers.remove(player);

            if (force) {
                LogHub.i(TAG, "Force destroying player for uniqueId=" + uniqueId);
                playerMap.remove(uniqueId);
                destroyPlayer(player);
            } else {
                // Just stop, keep in map (LRU will handle eviction eventually)
                LogHub.i(TAG, "Recycle: keep player in map for uniqueId=" + uniqueId);
                try {
                    player.stop();
                } catch (Exception e) {
                    LogHub.e(TAG, "Error stopping player", e);
                }
            }
        }
    }

    /**
     * 清空所有资源
     * <p>
     * 只清理预加载且未绑定 uniqueId 的播放器实例，不破坏已分配 ID 的活跃实例。
     * 已绑定 uniqueId 的播放器会继续正常工作，直到被正常回收或通过 LRU 机制淘汰。
     * </p>
     * <p>
     * Clear All Resources
     * <p>
     * Only clears preloaded players that are not bound to a uniqueId, does not destroy active instances with assigned IDs.
     * Players bound to uniqueIds will continue to work normally until they are properly recycled or evicted by the LRU mechanism.
     * </p>
     */
    @Override
    public void clear() {
        synchronized (this) {
            // 1. 清理预加载且未绑定的播放器
            LogHub.i(TAG, "Clearing " + unboundPlayers.size() + " preloaded unbound players");
            for (IMediaPlayer player : unboundPlayers) {
                destroyPlayer(player);
            }
            unboundPlayers.clear();

            // 2. 清理 playerMap 中处于空闲（非活跃）状态的播放器
            // 已绑定 uniqueId 但目前没在用的播放器也应该被销毁
            Iterator<Map.Entry<String, IMediaPlayer>> iterator = playerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IMediaPlayer> entry = iterator.next();
                IMediaPlayer player = entry.getValue();
                if (!activePlayers.contains(player)) {
                    LogHub.i(TAG, "Clearing idle player for uniqueId=" + entry.getKey());
                    iterator.remove();
                    destroyPlayer(player);
                } else {
                    LogHub.i(TAG, "Skipping active player for uniqueId=" + entry.getKey());
                }
            }
        }
    }

    /**
     * 预加载播放器实例
     * <p>
     * 提前创建播放器实例并放入 unboundPlayers 队列，供后续新 uniqueId 使用。
     * 这些实例在首次被 acquire 时会绑定到对应的 uniqueId，并移入 playerMap。
     * </p>
     * <p>
     * Preload Player Instances
     * <p>
     * Creates player instances in advance and puts them into the unboundPlayers queue for future new uniqueIds.
     * These instances will be bound to corresponding uniqueIds when first acquired and moved into playerMap.
     * </p>
     *
     * @param context 应用上下文，用于创建播放器实例，不能为 null
     * @param count   预加载的数量
     */
    @Override
    public void preload(@NonNull Context context, int count) {
        if (count <= 0) return;

        synchronized (this) {
            LogHub.i(TAG, "Preloading " + count + " unbound players...");
            for (int i = 0; i < count; i++) {
                try {
                    unboundPlayers.offer(createPlayer(context));
                } catch (Exception e) {
                    LogHub.e(TAG, "Preload failed", e);
                }
            }
        }
    }
}
