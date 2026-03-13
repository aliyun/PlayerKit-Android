package com.aliyun.playerkit.strategy;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.strategy.strategies.FirstFrameStrategy;
import com.aliyun.playerkit.strategy.strategies.StutterDetectStrategy;
import com.aliyun.playerkit.strategy.strategies.TrafficProtectionStrategy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 策略配置
 * <p>
 * 管理全局默认策略。支持通过工厂模式注册策略，确保每个播放器实例都能获得独立的策略对象。
 * </p>
 * <p>
 * Strategy Registry
 * <p>
 * Manages globally registered default strategies using factory interfaces,
 * ensuring each player instance gets its own independent strategy objects.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public final class StrategyRegistry {

    /**
     * 私有构造函数，防止实例化
     */
    private StrategyRegistry() {
        throw new UnsupportedOperationException("Cannot instantiate StrategyRegistry");
    }

    /**
     * 策略工厂接口
     */
    public interface StrategyFactory {
        /**
         * 创建策略实例
         *
         * @return 新创建的策略实例
         */
        @NonNull
        IStrategy create();
    }

    // 全局策略工厂列表
    private static final List<StrategyFactory> GLOBAL_FACTORIES = new CopyOnWriteArrayList<>();

    static {
        // 添加默认策略
        addDefaultStrategies();
    }

    /**
     * 添加默认策略
     */
    private static void addDefaultStrategies() {
        // 默认配置：启用首帧策略
        addGlobalStrategy(() -> new FirstFrameStrategy(null));
        // 默认配置：启用卡顿检测策略
        addGlobalStrategy(() -> new StutterDetectStrategy(null));
        // 默认配置：启用流量保护策略
        addGlobalStrategy(() -> new TrafficProtectionStrategy(null));
    }

    /**
     * 添加全局策略
     * <p>
     * 注册的策略将自动应用于所有新创建的 {@link AliPlayerController}。
     * </p>
     *
     * @param factory 策略工厂
     */
    public static void addGlobalStrategy(@NonNull StrategyFactory factory) {
        GLOBAL_FACTORIES.add(factory);
    }

    /**
     * 清除所有全局策略
     */
    public static void clearGlobalStrategies() {
        GLOBAL_FACTORIES.clear();
    }

    /**
     * 将全局策略应用到指定的策略管理器
     *
     * @param manager 策略管理器
     */
    public static void applyTo(@NonNull StrategyManager manager) {
        for (StrategyFactory factory : GLOBAL_FACTORIES) {
            IStrategy strategy = factory.create();
            manager.register(strategy);
        }
    }

    /**
     * 恢复默认配置
     * <p>
     * 清除当前配置并恢复默认策略。
     * </p>
     */
    public static void resetToDefault() {
        clearGlobalStrategies();
        addDefaultStrategies();
    }
}
