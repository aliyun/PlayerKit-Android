package com.aliyun.playerkit.strategy;

import androidx.annotation.NonNull;

/**
 * 策略接口
 * <p>
 * 定义了策略的核心生命周期和事件处理方法。
 * 策略用于封装特定的业务逻辑，通过监听播放器事件来实现监控、分析等功能。
 * </p>
 * <p>
 * Strategy Interface
 * <p>
 * Defines the core lifecycle and event handling methods for strategies.
 * Strategies are used to encapsulate specific business logic, implementing monitoring
 * and analysis functions by listening to player events.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public interface IStrategy {

    /**
     * 获取策略名称（用于日志和调试）
     * <p>
     * Get strategy name (for logging and debugging)
     * </p>
     *
     * @return 策略名称
     */
    @NonNull
    String getName();

    /**
     * 策略启动
     * <p>
     * 在此方法中订阅所需的事件。
     * </p>
     * <p>
     * Start strategy
     * <p>
     * Subscribe to required events in this method.
     * </p>
     *
     * @param context 策略上下文，提供只读的播放器信息
     */
    void onStart(@NonNull StrategyContext context);

    /**
     * 策略停止
     * <p>
     * 在此方法中取消事件订阅、清理资源。
     * </p>
     * <p>
     * Stop strategy
     * <p>
     * Unsubscribe events and clean up resources in this method.
     * </p>
     */
    void onStop();

    /**
     * 策略重置
     * <p>
     * 当播放内容切换时调用，用于重置策略内部状态。
     * </p>
     * <p>
     * Reset strategy
     * <p>
     * Called when playback content changes, used to reset internal strategy state.
     * </p>
     */
    void onReset();
}
