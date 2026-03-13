package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 播放器生命周期管理策略接口
 * <p>
 * 定义播放器实例的初始化、获取、释放和清空操作。
 * 不同的实现可以提供不同的生命周期管理策略，例如：
 * <ul>
 *     <li>{@link DefaultLifecycleStrategy}：默认策略，每次创建新实例，用完立即销毁</li>
 *     <li>{@link ReusePoolLifecycleStrategy}：复用池策略，适合列表流式播放</li>
 *     <li>{@link IdScopedPoolLifecycleStrategy}：ID绑定策略，ID与实例一一对应</li>
 *     <li>{@link SingletonLifecycleStrategy}：单例策略，全局唯一实例</li>
 * </ul>
 * </p>
 * <p>
 * Player Lifecycle Management Strategy Interface
 * <p>
 * Defines the contract for initializing, acquiring, releasing, and clearing player instances.
 * Different implementations can provide different lifecycle management strategies, such as:
 * <ul>
 *     <li>{@link DefaultLifecycleStrategy}: Default strategy, creates new instance each time and destroys immediately</li>
 *     <li>{@link ReusePoolLifecycleStrategy}: Reuse pool strategy, suitable for list/feed playback</li>
 *     <li>{@link IdScopedPoolLifecycleStrategy}: ID scoped strategy, one to one mapping</li>
 *     <li>{@link SingletonLifecycleStrategy}: Singleton strategy, global unique instance</li>
 * </ul>
 * </p>
 *
 * <p>
 * 约定：
 * <ul>
 *     <li>调用方在使用前必须先调用 {@link #init(IPlayerFactory)} 完成策略初始化</li>
 *     <li>同一策略实例可以被多个控制器复用，但应只初始化一次</li>
 *     <li>播放器实例的管理以业务唯一标识 {@code uniqueId} 为维度进行跟踪</li>
 * </ul>
 * </p>
 *
 * <p>
 * Conventions:
 * <ul>
 *     <li>Caller must invoke {@link #init(IPlayerFactory)} before using the strategy</li>
 *     <li>The same strategy instance can be reused by multiple controllers but should be initialized only once</li>
 *     <li>Player instances are tracked by business unique identifier {@code uniqueId}</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2025/11/27
 */
public interface IPlayerLifecycleStrategy {

    /**
     * 初始化生命周期策略
     * <p>
     * 在使用策略前调用一次，用于注入全局依赖（如 IPlayerFactory）。
     * 策略实现内应确保多次调用时具备幂等性（例如忽略后续相同参数的调用）。
     * </p>
     * <p>
     * Initialize lifecycle strategy
     * <p>
     * Should be called once before using the strategy to inject global dependencies
     * (such as IPlayerFactory).
     * Implementation should be idempotent for repeated calls with the same parameters.
     * </p>
     *
     * @param factory 播放器工厂，用于创建和销毁实例，不能为 null
     */
    void init(@NonNull IPlayerFactory factory);

    /**
     * 获取播放器实例
     * <p>
     * 根据具体策略决定是创建新实例还是复用已有实例。
     * 调用者负责在不再使用时调用 {@link #recycle(IMediaPlayer, String, boolean)} 回收实例。
     * </p>
     * <p>
     * Acquire Player Instance
     * <p>
     * Decides whether to create a new instance or reuse an existing one based on the specific strategy.
     * The caller is responsible for calling {@link #recycle(IMediaPlayer, String, boolean)}
     * when the instance is no longer needed.
     * </p>
     *
     * @param context  应用上下文，用于创建新实例，不能为 null
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @return 播放器实例，不会为 null
     */
    @NonNull
    IMediaPlayer acquire(@NonNull Context context, @NonNull String uniqueId);

    /**
     * 回收播放器实例
     * <p>
     * 根据具体策略决定是回收实例（放入池中）还是立即销毁。
     * 如果 force 为 true，则无论策略如何，都会立即销毁实例。
     * </p>
     * Recycle Player Instance
     * <p>
     * Decides whether to recycle the instance (put into pool) or destroy it immediately based on the specific strategy.
     * If force is true, the instance will be destroyed immediately regardless of the strategy.
     * </p>
     *
     * @param player   要回收的播放器实例，可以为 null（null 时不执行任何操作）
     * @param uniqueId 业务唯一标识，用于区分不同视频源或业务场景，不能为 null
     * @param force    是否强制销毁。如果为 true，则忽略池化策略，直接销毁实例
     */
    void recycle(@Nullable IMediaPlayer player, @NonNull String uniqueId, boolean force);

    /**
     * 清空所有资源
     * <p>
     * 销毁所有管理的播放器实例，释放所有资源。
     * 通常在应用退出或需要完全清理时调用。
     * 注意：不同策略的实现可能不同，某些策略可能只清理空闲实例，保留正在使用的实例。
     * </p>
     * <p>
     * Clear All Resources
     * <p>
     * Destroys all managed player instances and releases all resources.
     * Typically called when the application exits or needs to be completely cleaned up.
     * Note: Different strategies may have different implementations. Some strategies may only clear idle instances,
     * keeping active instances in use.
     * </p>
     */
    void clear();

    /**
     * 预加载播放器实例
     * <p>
     * 提前创建一定数量的播放器实例以减少首帧/起播耗时。
     * 具体行为取决于策略实现：
     * <ul>
     *     <li>Default: 忽略（不预创建）</li>
     *     <li>ReusePool: 填充空闲池直到达到 count 或 maxPoolSize</li>
     *     <li>IdScoped: 创建 unbound 实例供未来新 ID 使用</li>
     *     <li>Singleton: 初始化单例实例（忽略 count）</li>
     * </ul>
     * </p>
     * Preload Player Instances
     * <p>
     * Create instances in advance to reduce playback startup latency.
     * Behavior depends on strategy implementation:
     * <ul>
     *     <li>Default: Ignore (no-op)</li>
     *     <li>ReusePool: Fill idle pool up to count or maxPoolSize</li>
     *     <li>IdScoped: Create unbound instances for future new IDs</li>
     *     <li>Singleton: Initialize singleton instance (ignore count)</li>
     * </ul>
     * </p>
     *
     * @param context 应用上下文，用于创建播放器实例，不能为 null
     * @param count   建议预加载的数量（策略可能根据自身限制调整，如 maxPoolSize）
     */
    void preload(@NonNull Context context, int count);
}
