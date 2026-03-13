package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerLifecycleEvents;
import com.aliyun.playerkit.logging.LogHub;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 播放器生命周期策略基类
 * <p>
 * 提供通用的 Context/Factory 管理和辅助方法，减少子类样板代码。
 * </p>
 * <p>
 * Base Player Lifecycle Strategy
 * <p>
 * Provides common Context/Factory management and helper methods to reduce boilerplate in subclasses.
 * </p>
 *
 * @author keria
 * @date 2026/01/12
 */
public abstract class BaseLifecycleStrategy implements IPlayerLifecycleStrategy {

    private final String TAG = getClass().getSimpleName() + ".BaseLifecycleStrategy";

    /**
     * 默认播放器池最大容量
     * <p>
     * Note keria:
     * 背景说明：之前我们在微短剧解决方案（多实例播放器池）中，做过内存 profile：
     * - 每增加 1 个播放器实例，内存占用约增加 35MB ~ 40MB。
     * <p>
     * 建议策略：
     * - 中高端设备：可使用多实例播放器池，DEFAULT_MAX_POOL_SIZE = 3
     * - 低端设备：建议将 pool size 降为 2
     * - 极低端或强内存敏感场景：直接使用 {@link SingletonLifecycleStrategy}，以保证全局只有一个播放器实例，最大限度降低内存占用
     */
    protected static final int DEFAULT_MAX_POOL_SIZE = 3;

    // 播放器池最大容量，仅对池策略有效
    protected int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    @Nullable
    protected IPlayerFactory playerFactory;

    // 初始化状态
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

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
    @Override
    public void init(@NonNull IPlayerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }

        if (isInitialized.compareAndSet(false, true)) {
            this.playerFactory = factory;
            LogHub.i(TAG, "Initialized with factory: " + factory.getClass().getSimpleName());
        } else {
            LogHub.w(TAG, "Already initialized, ignoring repeated init call.");
        }
    }

    /**
     * 预加载播放器实例
     * <p>
     * 提前创建一定数量的播放器实例以减少首帧/起播耗时。
     * 基类提供默认空实现，子类按需覆盖。
     * </p>
     * <p>
     * Preload Player Instances
     * <p>
     * Create instances in advance to reduce playback startup latency.
     * Base class provides default no-op implementation, subclasses should override as needed.
     * </p>
     *
     * @param context 应用上下文，用于创建播放器实例，不能为 null
     * @param count   建议预加载的数量（策略可能根据自身限制调整）
     */
    @Override
    public void preload(@NonNull Context context, int count) {
        // 默认空实现，子类按需覆盖
        LogHub.i(TAG, "Preload no-op");
    }

    /**
     * 清空所有资源
     * <p>
     * 销毁所有管理的播放器实例，释放所有资源。
     * 基类提供默认空实现，子类按需覆盖。
     * </p>
     * <p>
     * Clear All Resources
     * <p>
     * Destroys all managed player instances and releases all resources.
     * Base class provides default no-op implementation, subclasses should override as needed.
     * </p>
     */
    @Override
    public void clear() {
        // 默认空实现，子类按需覆盖
        LogHub.i(TAG, "Clear no-op");
    }

    /**
     * 辅助方法：创建播放器实例
     * <p>
     * 通过 playerFactory 创建新的播放器实例，并发送 PlayerCreated 事件。
     * 子类在需要创建播放器时调用此方法。
     * </p>
     * <p>
     * Helper Method: Create Player Instance
     * <p>
     * Creates a new player instance through playerFactory and posts a PlayerCreated event.
     * Subclasses should call this method when they need to create a player.
     * </p>
     *
     * @param context 应用上下文，用于创建播放器实例，不能为 null
     * @return 新创建的播放器实例，不会为 null
     * @throws IllegalStateException 如果策略未初始化（playerFactory 为 null）
     */
    @NonNull
    protected IMediaPlayer createPlayer(@NonNull Context context) {
        if (playerFactory == null) {
            throw new IllegalStateException(TAG + " not initialized. Call init() first.");
        }
        IMediaPlayer player = playerFactory.create(context);
        PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerCreated(player.getPlayerId()));
        return player;
    }

    /**
     * 辅助方法：销毁播放器实例
     * <p>
     * 通过 playerFactory 销毁播放器实例，并发送 PlayerDestroyed 事件。
     * 如果 playerFactory 为 null，则只记录错误日志，不执行销毁操作。
     * </p>
     * <p>
     * Helper Method: Destroy Player Instance
     * <p>
     * Destroys the player instance through playerFactory and posts a PlayerDestroyed event.
     * If playerFactory is null, only logs an error without performing the destroy operation.
     * </p>
     *
     * @param player 要销毁的播放器实例，可以为 null（null 时不执行任何操作）
     */
    protected void destroyPlayer(@Nullable IMediaPlayer player) {
        if (player == null) return;
        if (playerFactory != null) {
            String playerId = player.getPlayerId();
            playerFactory.destroy(player);
            PlayerEventBus.getInstance().post(new PlayerLifecycleEvents.PlayerDestroyed(playerId));
        } else {
            LogHub.e(TAG, "PlayerFactory is null, cannot destroy player: " + player.getPlayerId());
        }
    }

    /**
     * 设置最大池大小
     * <p>
     * 设置播放器池的最大容量限制。仅对池策略（如 ReusePoolLifecycleStrategy、
     * IdScopedPoolLifecycleStrategy）有效。
     * 当池大小更新时，会触发 {@link #onMaxPoolSizeUpdated(int)} 钩子方法。
     * </p>
     * <p>
     * Set Max Pool Size
     * <p>
     * Sets the maximum capacity limit for the player pool. Only effective for pool strategies
     * (such as ReusePoolLifecycleStrategy, IdScopedPoolLifecycleStrategy).
     * When the pool size is updated, the {@link #onMaxPoolSizeUpdated(int)} hook method is triggered.
     * </p>
     *
     * @param maxPoolSize 最大池大小，必须大于 0
     */
    public void setMaxPoolSize(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            LogHub.e(TAG, "Max pool size must be greater than 0");
            return;
        }
        this.maxPoolSize = maxPoolSize;
        LogHub.i(TAG, "Max pool size updated to: " + maxPoolSize);
        onMaxPoolSizeUpdated(maxPoolSize);
    }

    /**
     * 策略子类钩子：当最大池大小更新时调用
     * <p>
     * 当通过 {@link #setMaxPoolSize(int)} 更新池大小时，会调用此方法。
     * 子类可以重写此方法以实现自定义的池大小调整逻辑（例如立即清理超出限制的实例）。
     * </p>
     * <p>
     * Strategy Subclass Hook: Called when max pool size is updated
     * <p>
     * This method is called when the pool size is updated via {@link #setMaxPoolSize(int)}.
     * Subclasses can override this method to implement custom pool size adjustment logic
     * (e.g., immediately clean up instances that exceed the limit).
     * </p>
     *
     * @param newSize 新的最大池大小
     */
    protected void onMaxPoolSizeUpdated(int newSize) {
        // 默认空实现
    }
}
