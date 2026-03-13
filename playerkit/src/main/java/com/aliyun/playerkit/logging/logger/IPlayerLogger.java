package com.aliyun.playerkit.logging.logger;

import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogLevel;

/**
 * 播放器全局日志接口
 * <p>
 * 封装播放器 SDK 的全局日志功能,提供统一的日志配置和回调机制。
 * </p>
 * <p>
 * Player Global Logger Interface
 * <p>
 * Encapsulates the player SDK's global logging functionality, providing unified log configuration and callback mechanism.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public interface IPlayerLogger {

    /**
     * 启用或禁用控制台日志输出
     * <p>
     * Enable or disable console log output
     * </p>
     *
     * @param enable true 启用,false 禁用
     */
    void enableConsoleLog(boolean enable);

    /**
     * 检查控制台日志是否已启用
     * <p>
     * Check if console log is enabled
     * </p>
     *
     * @return true 已启用,false 已禁用
     */
    boolean isConsoleLogEnabled();

    /**
     * 设置日志级别
     * <p>
     * 低于此级别的日志将被忽略，不会输出到控制台或通知监听器。
     * </p>
     * <p>
     * Set log level
     * <p>
     * Logs below this level will be ignored and will not be output to console or notified to listeners.
     * </p>
     *
     * @param level 日志级别
     */
    void setLogLevel(@LogLevel int level);

    /**
     * 获取当前日志级别
     * <p>
     * Get current log level
     * </p>
     *
     * @return 日志级别
     */
    @LogLevel
    int getLogLevel();

    /**
     * 设置日志回调监听器
     * <p>
     * Set log callback listener
     * </p>
     *
     * @param callback 日志回调接口,传 null 表示移除回调
     */
    void setLogCallback(@Nullable LoggerCallback callback);
}
