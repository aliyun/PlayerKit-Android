package com.aliyun.playerkit.logging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志信息数据类
 * <p>
 * 封装单条日志的完整信息，包括级别、标签、消息、异常等。
 * </p>
 * <p>
 * Log information data class
 * <p>
 * Encapsulates complete information of a single log, including level, tag, message, exception, etc.
 * </p>
 *
 * @author keria
 * @date 2025/12/10
 */
public class LogInfo {

    /**
     * 日志级别
     * <p>
     * Log level
     * </p>
     */
    @LogLevel
    private final int level;

    /**
     * 日志标签
     * <p>
     * Log tag
     * </p>
     */
    @NonNull
    private final String tag;

    /**
     * 日志消息内容
     * <p>
     * Log message content
     * </p>
     */
    @NonNull
    private final String message;

    /**
     * 异常信息（如果有）
     * <p>
     * Exception information (if any)
     * </p>
     */
    @Nullable
    private final Throwable throwable;

    /**
     * 线程名称
     * <p>
     * Thread name
     * </p>
     */
    @NonNull
    private final String threadName;

    /**
     * 时间戳（毫秒）
     * <p>
     * Timestamp (milliseconds)
     * </p>
     */
    private final long timestamp;

    /**
     * 构造函数
     * <p>
     * Constructor
     * </p>
     *
     * @param level     日志级别
     * @param tag       日志标签
     * @param message   日志消息
     * @param throwable 异常信息（可为 null）
     */
    public LogInfo(@LogLevel int level, @NonNull String tag, @NonNull String message, @Nullable Throwable throwable) {
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.throwable = throwable;
        this.threadName = Thread.currentThread().getName();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取日志级别
     * <p>
     * Get log level
     * </p>
     *
     * @return 日志级别
     */
    @LogLevel
    public int getLevel() {
        return level;
    }

    /**
     * 获取日志标签
     * <p>
     * Get log tag
     * </p>
     *
     * @return 日志标签
     */
    @NonNull
    public String getTag() {
        return tag;
    }

    /**
     * 获取日志消息
     * <p>
     * Get log message
     * </p>
     *
     * @return 日志消息
     */
    @NonNull
    public String getMessage() {
        return message;
    }

    /**
     * 获取异常信息
     * <p>
     * Get exception information
     * </p>
     *
     * @return 异常信息，如果没有则返回 null
     */
    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * 获取线程名称
     * <p>
     * Get thread name
     * </p>
     *
     * @return 线程名称
     */
    @NonNull
    public String getThreadName() {
        return threadName;
    }

    /**
     * 获取时间戳
     * <p>
     * Get timestamp
     * </p>
     *
     * @return 时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 获取格式化的日志字符串
     * <p>
     * 格式：yyyy-MM-dd HH:mm:ss.SSS [LEVEL] [TAG] [THREAD] message
     * </p>
     * <p>
     * Get formatted log string
     * <p>
     * Format: yyyy-MM-dd HH:mm:ss.SSS [LEVEL] [TAG] [THREAD] message
     * </p>
     *
     * @return 格式化的日志字符串
     */
    @NonNull
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(formatTimestamp(timestamp)).append(" [").append(LogHub.levelToString(level)).append("]").append(" [").append(tag).append("]").append(" [").append(threadName).append("]").append(" ").append(message);

        if (throwable != null) {
            sb.append("\n").append(getStackTraceString(throwable));
        }

        return sb.toString();
    }

    private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        }
    };

    /**
     * 格式化时间戳
     * <p>
     * Format timestamp
     * </p>
     *
     * @param timestamp 时间戳（毫秒）
     * @return 格式化的时间字符串 yyyy-MM-dd HH:mm:ss.SSS
     */
    @NonNull
    private static String formatTimestamp(long timestamp) {
        return TIMESTAMP_FORMATTER.get().format(new Date(timestamp));
    }

    /**
     * 获取异常的堆栈跟踪字符串
     * <p>
     * Get exception stack trace string
     * </p>
     *
     * @param throwable 异常对象
     * @return 堆栈跟踪字符串
     */
    @NonNull
    private static String getStackTraceString(@NonNull Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    @NonNull
    public String toString() {
        return getFormattedMessage();
    }
}
