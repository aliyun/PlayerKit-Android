package com.aliyun.playerkit.logging;

import static java.lang.annotation.ElementType.TYPE_USE;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志级别类型定义
 * <p>
 * 定义了日志系统支持的所有日志级别
 * </p>
 * <p>
 * Log Level Type Definition
 * <p>
 * Defines all log levels supported by the logging system
 * </p>
 *
 * @author keria
 * @date 2025/12/10
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(TYPE_USE)
@IntDef({
        LogLevel.VERBOSE,
        LogLevel.DEBUG,
        LogLevel.INFO,
        LogLevel.WARN,
        LogLevel.ERROR,
        LogLevel.NONE,
})
public @interface LogLevel {

    /**
     * VERBOSE 级别
     * <p>
     * 最详细的日志级别，用于输出所有调试信息。
     * </p>
     * <p>
     * VERBOSE Level
     * <p>
     * Most detailed log level for outputting all debug information.
     * </p>
     */
    int VERBOSE = 0;

    /**
     * DEBUG 级别
     * <p>
     * 调试信息级别，用于输出调试相关的日志。
     * </p>
     * <p>
     * DEBUG Level
     * <p>
     * Debug information level for outputting debug-related logs.
     * </p>
     */
    int DEBUG = 1;

    /**
     * INFO 级别
     * <p>
     * 信息级别，用于输出一般信息性日志。
     * </p>
     * <p>
     * INFO Level
     * <p>
     * Information level for outputting general informational logs.
     * </p>
     */
    int INFO = 2;

    /**
     * WARN 级别
     * <p>
     * 警告级别，用于输出警告信息。
     * </p>
     * <p>
     * WARN Level
     * <p>
     * Warning level for outputting warning information.
     * </p>
     */
    int WARN = 3;

    /**
     * ERROR 级别
     * <p>
     * 错误级别，用于输出错误信息。
     * </p>
     * <p>
     * ERROR Level
     * <p>
     * Error level for outputting error information.
     * </p>
     */
    int ERROR = 4;

    /**
     * 禁用所有日志
     * <p>
     * 禁用所有日志输出。
     * </p>
     * <p>
     * Disable All Logs
     * <p>
     * Disables all log output.
     * </p>
     */
    int NONE = 100;
}
