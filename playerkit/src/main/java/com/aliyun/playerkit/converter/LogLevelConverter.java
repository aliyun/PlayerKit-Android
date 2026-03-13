package com.aliyun.playerkit.converter;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.logging.LogLevel;
import com.cicada.player.utils.Logger;

/**
 * 日志级别转换器
 * <p>
 * 用于在 SDK 日志级别和 PlayerKit 日志级别之间进行转换。
 * </p>
 * <p>
 * Log Level Converter
 * <p>
 * Used to convert between SDK log levels and PlayerKit log levels.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public final class LogLevelConverter {

    /**
     * 私有构造函数,防止实例化
     */
    private LogLevelConverter() {
        throw new UnsupportedOperationException("Cannot instantiate LogLevelConverter");
    }

    /**
     * 转换 SDK 日志级别到 LogLevel
     * <p>
     * Convert SDK log level to LogLevel
     * </p>
     *
     * @param sdkLevel SDK 日志级别
     * @return LogLevel
     */
    @LogLevel
    public static int fromSdkLogLevel(@NonNull Logger.LogLevel sdkLevel) {
        switch (sdkLevel) {
            case AF_LOG_LEVEL_TRACE:
                return LogLevel.VERBOSE;
            case AF_LOG_LEVEL_DEBUG:
                return LogLevel.DEBUG;
            case AF_LOG_LEVEL_INFO:
                return LogLevel.INFO;
            case AF_LOG_LEVEL_WARNING:
                return LogLevel.WARN;
            case AF_LOG_LEVEL_ERROR:
            case AF_LOG_LEVEL_FATAL:
                return LogLevel.ERROR;
            case AF_LOG_LEVEL_NONE:
                return LogLevel.NONE;
            default:
                return LogLevel.INFO;
        }
    }

    /**
     * 转换 LogLevel 到 SDK 日志级别
     * <p>
     * Convert LogLevel to SDK log level
     * </p>
     *
     * @param logLevel 内部 LogLevel
     * @return SDK Logger.LogLevel
     */
    @NonNull
    public static Logger.LogLevel toSdkLogLevel(@LogLevel int logLevel) {
        switch (logLevel) {
            case LogLevel.VERBOSE:
                return Logger.LogLevel.AF_LOG_LEVEL_TRACE;
            case LogLevel.DEBUG:
                return Logger.LogLevel.AF_LOG_LEVEL_DEBUG;
            case LogLevel.INFO:
                return Logger.LogLevel.AF_LOG_LEVEL_INFO;
            case LogLevel.WARN:
                return Logger.LogLevel.AF_LOG_LEVEL_WARNING;
            case LogLevel.ERROR:
                return Logger.LogLevel.AF_LOG_LEVEL_ERROR;
            case LogLevel.NONE:
                return Logger.LogLevel.AF_LOG_LEVEL_NONE;
            default:
                return Logger.LogLevel.AF_LOG_LEVEL_INFO;
        }
    }
}
