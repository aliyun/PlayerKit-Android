package com.aliyun.playerkit.logging;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.utils.StringUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PlayerKit 日志中心工具类
 * <p>
 * 提供统一的日志输出功能，是 PlayerKit 框架中所有日志输出的统一入口。
 * 支持日志级别控制、日志开关控制、控制台输出控制以及日志监听器机制。
 * </p>
 * <p>
 * <b>Logcat 过滤建议：</b>
 * <pre>
 * package:mine tag:AliPlayerKit
 * </pre>
 * </p>
 * <p>
 * PlayerKit Log Center Utility Class
 * <p>
 * Provides unified log output functionality, serving as the unified entry point for all log outputs in the PlayerKit framework.
 * Supports log level control, log switch control, console output control, and log listener mechanism.
 * </p>
 * <p>
 * <b>Logcat Filter Suggestion:</b>
 * <pre>
 * package:mine tag:AliPlayerKit
 * </pre>
 * </p>
 *
 * @author keria
 * @date 2024/9/25
 */
public class LogHub {
    private static final String TAG = "AliPlayerKit";

    /**
     * 所有日志级别
     * <p>
     * All log levels
     * </p>
     */
    private static final int[] LOG_LEVEL_VALUES = {
            LogLevel.VERBOSE,
            LogLevel.DEBUG,
            LogLevel.INFO,
            LogLevel.WARN,
            LogLevel.ERROR,
            LogLevel.NONE,
    };

    /**
     * All log level display names.
     */
    private static final String[] LOG_LEVEL_NAMES = {
            "VERBOSE",
            "DEBUG",
            "INFO",
            "WARN",
            "ERROR",
            "NONE",
    };

    /**
     * 是否启用日志输出 (Master Switch)
     * <p>
     * 注意：建议使用 {@link #setEnableLog(boolean)} 方法来设置，而不是直接修改此变量
     * </p>
     */
    private static boolean mEnableLog = true;

    /**
     * 是否启用控制台日志输出 (Console Switch)
     * <p>
     * 注意：建议使用 {@link #setEnableConsoleLog(boolean)} 方法来设置，而不是直接修改此变量
     */
    private static boolean mEnableConsoleLog = true;

    /**
     * 当前日志级别 (Level Filter)
     * <p>
     * 低于此级别的日志将被忽略
     */
    @LogLevel
    private static int mLogLevel = LogLevel.INFO;

    /**
     * 日志缓冲区大小，用于分段输出长日志
     */
    private static final int BUFFER_SIZE = 3000;

    /**
     * 日志监听器列表
     * <p>
     * 用于外部获取日志信息，例如写入文件等。支持多个回调。
     * </p>
     */
    private static final List<LogHubListener> sListeners = new CopyOnWriteArrayList<>();

    /**
     * 私有构造函数，防止实例化
     */
    private LogHub() {
    }

    /**
     * 开启或关闭日志输出
     * <p>
     * 当设置为 false 时，所有日志都不会输出（包括控制台和监听器）
     * </p>
     * <p>
     * Enable or disable log output
     * <p>
     * When set to false, all logs will not be output (including console and listeners)
     * </p>
     *
     * @param enable true 表示开启日志，false 表示关闭日志
     */
    public static void setEnableLog(boolean enable) {
        mEnableLog = enable;
    }

    /**
     * 检查日志是否已启用
     * <p>
     * Check if log is enabled
     * </p>
     *
     * @return true 表示日志已启用，false 表示日志已关闭
     */
    public static boolean isLogEnabled() {
        return mEnableLog;
    }

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
    public static void setLogLevel(@LogLevel int level) {
        mLogLevel = level;
    }

    /**
     * 获取当前日志级别
     * <p>
     * Get log level
     * </p>
     *
     * @return 日志级别
     */
    @LogLevel
    public static int getLogLevel() {
        return mLogLevel;
    }

    /**
     * 设置是否启用控制台日志输出
     * <p>
     * Set whether to enable console log output
     * </p>
     *
     * @param enable 是否启用
     */
    public static void setEnableConsoleLog(boolean enable) {
        mEnableConsoleLog = enable;
    }

    /**
     * 检查控制台日志是否已启用
     * <p>
     * Check if console log is enabled
     * </p>
     *
     * @return true 启用，false 禁用
     */
    public static boolean isConsoleLogEnabled() {
        return mEnableConsoleLog;
    }

    /**
     * 添加日志监听器
     * <p>
     * Add log listener
     * </p>
     *
     * @param listener 日志监听器
     */
    public static void addListener(@NonNull LogHubListener listener) {
        if (!sListeners.contains(listener)) {
            sListeners.add(listener);
        }
    }

    /**
     * 移除日志监听器
     * <p>
     * Remove log listener
     * </p>
     *
     * @param listener 日志监听器
     */
    public static void removeListener(@NonNull LogHubListener listener) {
        sListeners.remove(listener);
    }

    /**
     * 判断当前级别是否允许输出日志
     * <p>
     * Determine whether the current level allows log output
     * </p>
     *
     * @param level 待检查的日志级别
     * @return true 表示允许输出，false 表示不允许输出
     */
    private static boolean isLoggable(@LogLevel int level) {
        return mEnableLog && level >= mLogLevel;
    }

    /**
     * 通知日志监听器
     * <p>
     * Notify log listeners
     * </p>
     *
     * @param level     日志级别
     * @param tag       日志标签
     * @param message   日志消息
     * @param throwable 异常信息（可为 null）
     */
    private static void notifyListeners(@LogLevel int level, String tag, String message, @Nullable Throwable throwable) {
        if (!sListeners.isEmpty()) {
            try {
                // 创建一次 LogInfo 对象，复用于所有监听器
                LogInfo logInfo = new LogInfo(level, tag, message, throwable);
                for (LogHubListener listener : sListeners) {
                    try {
                        listener.onLog(logInfo);
                    } catch (Exception e) {
                        // 监听器异常不应该影响正常的日志输出和其他监听器
                        Log.e(TAG, "LogHubListener.onLog() threw an exception: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in notifyListeners", e);
            }
        }
    }

    /**
     * 按指定 LogLevel 直接输出日志（保持原有 TAG）
     */
    public static void log(@LogLevel int level, @NonNull String tag, @NonNull String message) {
        if (!isLoggable(level)) return;

        switch (level) {
            case LogLevel.VERBOSE:
                Log.v(tag, message);
                break;
            case LogLevel.DEBUG:
                Log.d(tag, message);
                break;
            case LogLevel.INFO:
                Log.i(tag, message);
                break;
            case LogLevel.WARN:
                Log.w(tag, message);
                break;
            case LogLevel.ERROR:
                Log.e(tag, message);
                break;
            default:
                return;
        }

        notifyListeners(level, tag, message, null);
    }

    /**
     * 输出长日志，自动分段处理超长日志内容
     * <p>
     * 优化：对于监听器，发送完整的日志内容；对于 Logcat，进行分段输出。
     * </p>
     *
     * @param o      对象实例，用于标识日志来源
     * @param method 方法名，标识日志产生位置
     * @param s      日志内容
     */
    public static void log(Object o, String method, String s) {
        if (!isLoggable(mLogLevel)) return;

        if (s == null) {
            s = "null";
        }

        // 1. 生成完整日志并分发给监听器
        String fullLogMessage = createLog(o, method, new Object[]{s});
        notifyListeners(mLogLevel, TAG, fullLogMessage, null);

        // 2. 分段输出到 Logcat
        if (mEnableConsoleLog) {
            int length = s.length();
            int startIndex = 0;

            if (length == 0) {
                Log.v(TAG, fullLogMessage);
                return;
            }

            while (startIndex < length) {
                int endIndex = Math.min(length, startIndex + BUFFER_SIZE);
                String sub = s.substring(startIndex, endIndex);
                String chunkLog = createLog(o, method, new Object[]{sub});
                Log.v(TAG, chunkLog);
                startIndex = endIndex;
            }
        }
    }

    /**
     * 按指定级别输出日志
     *
     * @param level    日志级别
     * @param o        对象实例，用于标识日志来源
     * @param method   方法名，标识日志产生位置
     * @param messages 日志消息数组
     */
    public static void l(int level, Object o, String method, Object... messages) {
        if (!mEnableLog) return;
        @LogLevel int logLevel = convertLogLevel(level);
        if (logLevel < mLogLevel) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.println(level, TAG, logMessage);
        }
        notifyListeners(logLevel, TAG, logMessage, null);
    }

    /**
     * 输出VERBOSE级别日志
     *
     * @param o        对象实例，用于标识日志来源
     * @param method   方法名，标识日志产生位置
     * @param messages 日志消息数组
     */
    public static void v(Object o, String method, Object... messages) {
        if (!isLoggable(LogLevel.VERBOSE)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.v(TAG, logMessage);
        }
        notifyListeners(LogLevel.VERBOSE, TAG, logMessage, null);
    }

    /**
     * 输出VERBOSE级别日志，带异常信息
     *
     * @param o         对象实例，用于标识日志来源
     * @param method    方法名，标识日志产生位置
     * @param throwable 异常对象
     * @param messages  日志消息数组
     */
    public static void v(Object o, String method, Throwable throwable, Object... messages) {
        if (!isLoggable(LogLevel.VERBOSE)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.v(TAG, logMessage, throwable);
        }
        notifyListeners(LogLevel.VERBOSE, TAG, logMessage, throwable);
    }

    /**
     * 输出DEBUG级别日志
     *
     * @param o        对象实例，用于标识日志来源
     * @param method   方法名，标识日志产生位置
     * @param messages 日志消息数组
     */
    public static void d(Object o, String method, Object... messages) {
        if (!isLoggable(LogLevel.DEBUG)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.d(TAG, logMessage);
        }
        notifyListeners(LogLevel.DEBUG, TAG, logMessage, null);
    }

    /**
     * 输出DEBUG级别日志，带异常信息
     *
     * @param o         对象实例，用于标识日志来源
     * @param method    方法名，标识日志产生位置
     * @param throwable 异常对象
     * @param messages  日志消息数组
     */
    public static void d(Object o, String method, Throwable throwable, Object... messages) {
        if (!isLoggable(LogLevel.DEBUG)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.d(TAG, logMessage, throwable);
        }
        notifyListeners(LogLevel.DEBUG, TAG, logMessage, throwable);
    }

    /**
     * 输出INFO级别日志
     *
     * @param o        对象实例，用于标识日志来源
     * @param method   方法名，标识日志产生位置
     * @param messages 日志消息数组
     */
    public static void i(Object o, String method, Object... messages) {
        if (!isLoggable(LogLevel.INFO)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.i(TAG, logMessage);
        }
        notifyListeners(LogLevel.INFO, TAG, logMessage, null);
    }

    /**
     * 输出INFO级别日志，带异常信息
     *
     * @param o         对象实例，用于标识日志来源
     * @param method    方法名，标识日志产生位置
     * @param throwable 异常对象
     * @param messages  日志消息数组
     */
    public static void i(Object o, String method, Throwable throwable, Object... messages) {
        if (!isLoggable(LogLevel.INFO)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.i(TAG, logMessage, throwable);
        }
        notifyListeners(LogLevel.INFO, TAG, logMessage, throwable);
    }

    /**
     * 输出ERROR级别日志
     *
     * @param o        对象实例，用于标识日志来源
     * @param method   方法名，标识日志产生位置
     * @param messages 日志消息数组
     */
    public static void e(Object o, String method, Object... messages) {
        if (!isLoggable(LogLevel.ERROR)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.e(TAG, logMessage);
        }
        notifyListeners(LogLevel.ERROR, TAG, logMessage, null);
    }

    /**
     * 输出ERROR级别日志，带异常信息
     *
     * @param o         对象实例，用于标识日志来源
     * @param method    方法名，标识日志产生位置
     * @param throwable 异常对象
     * @param messages  日志消息数组
     */
    public static void e(Object o, String method, Throwable throwable, Object... messages) {
        if (!isLoggable(LogLevel.ERROR)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.e(TAG, logMessage, throwable);
        }
        notifyListeners(LogLevel.ERROR, TAG, logMessage, throwable);
    }

    /**
     * 输出WARN级别日志
     *
     * @param o        对象实例，用于标识日志来源
     * @param method   方法名，标识日志产生位置
     * @param messages 日志消息数组
     */
    public static void w(Object o, String method, Object... messages) {
        if (!isLoggable(LogLevel.WARN)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.w(TAG, logMessage);
        }
        notifyListeners(LogLevel.WARN, TAG, logMessage, null);
    }

    /**
     * 输出WARN级别日志，带异常信息
     *
     * @param o         对象实例，用于标识日志来源
     * @param method    方法名，标识日志产生位置
     * @param throwable 异常对象
     * @param messages  日志消息数组
     */
    public static void w(Object o, String method, Throwable throwable, Object... messages) {
        if (!isLoggable(LogLevel.WARN)) return;

        String logMessage = createLog(o, method, messages);
        if (mEnableConsoleLog) {
            Log.w(TAG, logMessage, throwable);
        }
        notifyListeners(LogLevel.WARN, TAG, logMessage, throwable);
    }

    /**
     * 输出性能时间日志
     *
     * @param method   方法名
     * @param costTime 耗时（毫秒）
     */
    public static void t(String method, long costTime) {
        if (!isLoggable(LogLevel.DEBUG)) return;

        String threadName = Looper.myLooper() == Looper.getMainLooper() ? "MAIN" : "SUB";
        String logMessage = createLog(null, method, new Object[]{threadName, "costTime: " + costTime});
        if (mEnableConsoleLog) {
            Log.d("TimeProfiler", logMessage);
        }
        notifyListeners(LogLevel.DEBUG, "TimeProfiler", logMessage, null);
    }

    /**
     * 创建格式化的日志内容
     *
     * @param o        对象实例
     * @param method   方法名
     * @param messages 消息数组
     * @return 格式化后的日志字符串
     */
    private static String createLog(Object o, String method, Object[] messages) {
        StringBuilder msg = new StringBuilder();
        if (o != null) {
            msg.append("[").append(slimObj(o)).append("]");
        }
        msg.append(" ").append(method);

        if (messages != null && messages.length > 0) {
            msg.append(": ");
            for (int i = 0; i < messages.length; i++) {
                Object message = messages[i];
                String str = slimObj(message);
                msg.append(str);
                if (i < messages.length - 1) {
                    msg.append(", ");
                }
            }
        }
        return msg.toString();
    }

    /**
     * 将对象转换为字符串表示
     *
     * @param o 对象实例
     * @return 对象的字符串表示
     */
    public static String string(Object o) {
        if (o == null) {
            return "null";
        }
        return mEnableLog ? o.toString() : "";
    }

    /**
     * 精简对象表示，避免输出过长内容
     *
     * @param o 对象实例
     * @return 精简后的对象字符串表示
     */
    public static String slimObj(Object o) {
        if (o == null) {
            return "null";
        } else if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Boolean) {
            return String.valueOf(o);
        } else if (o instanceof Number) {
            return String.valueOf(o);
        } else if (o instanceof Iterable<?>) {
            return String.valueOf(o);
        } else if (o.getClass().isAnonymousClass()) {
            String s = o.toString();
            return s.substring(s.lastIndexOf('.') + 1);
        } else if (o instanceof Class<?>) {
            return ((Class<?>) o).getSimpleName();
        } else {
            return o.getClass().getSimpleName() + '@' + Integer.toHexString(o.hashCode());
        }
    }

    /**
     * 将 Android Log 级别转换为 LogLevel
     * <p>
     * Convert Android Log level to LogLevel
     * </p>
     *
     * @param level Android Log 级别
     * @return LogLevel
     */
    @LogLevel
    public static int convertLogLevel(int level) {
        if (level <= Log.VERBOSE) {
            return LogLevel.VERBOSE;
        } else if (level == Log.DEBUG) {
            return LogLevel.DEBUG;
        } else if (level == Log.INFO) {
            return LogLevel.INFO;
        } else if (level == Log.WARN) {
            return LogLevel.WARN;
        } else if (level == Log.ERROR) {
            return LogLevel.ERROR;
        } else {
            return LogLevel.INFO;
        }
    }

    /**
     * 获取所有日志级别
     * <p>
     * Get all log levels
     * </p>
     *
     * @return 所有日志级别
     */
    @NonNull
    public static int[] getLogLevelValues() {
        return LOG_LEVEL_VALUES.clone();
    }

    /**
     * 获取所有日志级别名称
     * <p>
     * Get all log level names
     * </p>
     *
     * @return 所有日志级别名称
     */
    @NonNull
    public static String[] getLogLevelNames() {
        return LOG_LEVEL_NAMES.clone();
    }

    /**
     * 将日志级别转换为字符串
     * <p>
     * Convert log level to string
     * </p>
     *
     * @param level 日志级别
     * @return 对应的字符串表示
     */
    @NonNull
    public static String levelToString(@LogLevel int level) {
        for (int i = 0; i < LOG_LEVEL_VALUES.length; i++) {
            if (LOG_LEVEL_VALUES[i] == level) {
                return LOG_LEVEL_NAMES[i];
            }
        }
        return "UNKNOWN";
    }

    /**
     * 将字符串表示的日志级别转换为 LogLevel
     * <p>
     * Convert string representation of log level to LogLevel
     * </p>
     *
     * @param levelName 日志级别名称
     * @return 对应的 LogLevel
     */
    @LogLevel
    public static int stringToLevel(@Nullable String levelName) {
        if (StringUtil.isEmpty(levelName)) {
            return mLogLevel;
        }

        for (int i = 0; i < LOG_LEVEL_NAMES.length; i++) {
            if (LOG_LEVEL_NAMES[i].equalsIgnoreCase(levelName)) {
                return LOG_LEVEL_VALUES[i];
            }
        }
        return mLogLevel;
    }
}
