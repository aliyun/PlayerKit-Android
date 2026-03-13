package com.aliyun.playerkit.strategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogHub;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 策略管理器
 * <p>
 * 负责管理所有策略的生命周期，包括注册、启动、停止、重置等。
 * 每个播放器实例对应一个 {@link StrategyManager}。
 * </p>
 * <p>
 * Strategy Manager
 * <p>
 * Manages the lifecycle of all strategies, including registration, start,
 * stop and reset. Each player instance owns an independent {@link StrategyManager}.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public class StrategyManager {

    private static final String TAG = "StrategyManager";

    /**
     * 已注册的策略（key = strategy name，保持注册顺序）
     */
    private final Map<String, IStrategy> mStrategies = new LinkedHashMap<>();

    /**
     * 当前策略上下文
     */
    private StrategyContext mContext;

    /**
     * 是否已启动
     */
    private boolean mStarted = false;

    /**
     * 注册策略
     * <p>
     * 注册新的策略。如果已存在相同名称（{@link IStrategy#getName()}）的策略，则会<b>覆盖</b>旧策略。
     * 如果管理器已启动，新注册的策略会立即启动。
     * </p>
     * <p>
     * Register a strategy.
     * If there is already a strategy with the same name, the old one will be <b>overwritten</b>.
     * When the manager has been started, the newly registered strategy will be started immediately.
     * </p>
     *
     * @param strategy 要注册的策略，不能为 null
     */
    public void register(@NonNull IStrategy strategy) {
        if (strategy == null) {
            LogHub.e(TAG, "Cannot register null strategy");
            return;
        }

        String strategyName = strategy.getName();
        IStrategy existing = mStrategies.get(strategyName);
        if (existing != null) {
            LogHub.w(TAG, "Overwriting existing strategy: " + strategyName);
            unregister(existing);
        }

        mStrategies.put(strategyName, strategy);
        LogHub.i(TAG, "Registered strategy: " + strategyName);

        if (mStarted && mContext != null) {
            strategy.onStart(mContext);
        }
    }

    /**
     * 根据名称获取策略
     * <p>
     * Get strategy instance by name.
     * </p>
     *
     * @param name 策略名称
     * @return 策略实例，不存在返回 null
     */
    @Nullable
    public IStrategy getStrategy(@NonNull String name) {
        return mStrategies.get(name);
    }

    /**
     * 注销策略
     * <p>
     * Unregister a strategy and stop it if needed.
     * </p>
     *
     * @param strategy 要注销的策略
     */
    public void unregister(@NonNull IStrategy strategy) {
        if (strategy == null) {
            return;
        }

        String strategyName = strategy.getName();
        IStrategy removed = mStrategies.remove(strategyName);
        if (removed != null) {
            LogHub.i(TAG, "Unregistered strategy: " + strategyName);
            if (mStarted) {
                removed.onStop();
            }
        }
    }

    /**
     * 启动所有策略
     * <p>
     * Start all registered strategies with the given context.
     * </p>
     *
     * @param context 策略上下文
     */
    public void start(@NonNull StrategyContext context) {
        if (context == null) {
            LogHub.e(TAG, "Cannot start with null context");
            return;
        }

        mContext = context;
        mStarted = true;

        if (mStrategies.isEmpty()) {
            LogHub.i(TAG, "No strategies to start");
            return;
        }

        LogHub.i(TAG, "Starting " + mStrategies.size() + " strategies");
        for (IStrategy strategy : mStrategies.values()) {
            try {
                strategy.onStart(context);
            } catch (Exception e) {
                LogHub.e(TAG, "Error starting strategy: " + strategy.getName(), e);
            }
        }
    }

    /**
     * 停止所有策略
     * <p>
     * Stop all registered strategies and clear current context.
     * </p>
     */
    public void stop() {
        mStarted = false;

        if (mStrategies.isEmpty()) {
            LogHub.i(TAG, "No strategies to stop");
            mContext = null;
            return;
        }

        LogHub.i(TAG, "Stopping " + mStrategies.size() + " strategies");
        for (IStrategy strategy : mStrategies.values()) {
            try {
                strategy.onStop();
            } catch (Exception e) {
                LogHub.e(TAG, "Error stopping strategy: " + strategy.getName(), e);
            }
        }
        mContext = null;
    }

    /**
     * 重置所有策略
     * <p>
     * 当播放内容切换时调用，用于重置所有策略的内部状态。
     * </p>
     * <p>
     * Reset all strategies.
     * Called when playback content changes to reset internal state of strategies.
     * </p>
     */
    public void reset() {
        if (mStrategies.isEmpty()) {
            return;
        }

        LogHub.i(TAG, "Resetting " + mStrategies.size() + " strategies");
        for (IStrategy strategy : mStrategies.values()) {
            try {
                strategy.onReset();
            } catch (Exception e) {
                LogHub.e(TAG, "Error resetting strategy: " + strategy.getName(), e);
            }
        }
    }

    /**
     * 更新上下文并重启策略
     * <p>
     * 当播放内容切换时调用。为了确保策略使用最新的上下文（如播放模型、状态存储），
     * 此方法会先停止所有策略，然后使用新上下文重新启动它们。
     * </p>
     * <p>
     * Update context and restart strategies.
     * All strategies will be stopped first and then started again with the new context.
     * </p>
     *
     * @param context 新的策略上下文
     */
    public void updateContext(@NonNull StrategyContext context) {
        if (context == null) {
            LogHub.e(TAG, "Cannot update context with null value");
            return;
        }

        if (mStarted) {
            LogHub.i(TAG, "Updating context, restarting strategies...");
            stop();
            start(context);
        } else {
            LogHub.i(TAG, "updateContext called while not started, ignoring.");
        }
    }

    /**
     * 销毁管理器
     * <p>
     * 停止所有策略并清空策略列表。
     * </p>
     * <p>
     * Destroy this manager.
     * Stops all strategies and clears the registry.
     * </p>
     */
    public void destroy() {
        stop();
        mStrategies.clear();
        LogHub.i(TAG, "StrategyManager destroyed");
    }

    /**
     * 获取已注册的策略数量
     * <p>
     * Get count of registered strategies.
     * </p>
     *
     * @return 策略数量
     */
    public int getStrategyCount() {
        return mStrategies.size();
    }

    /**
     * 检查是否已启动
     * <p>
     * Check whether the manager has been started.
     * </p>
     *
     * @return 如果已启动返回 true
     */
    public boolean isStarted() {
        return mStarted;
    }
}
