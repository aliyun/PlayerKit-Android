package com.aliyun.playerkit.logging.logger;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.converter.LogLevelConverter;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.logging.LogLevel;
import com.aliyun.playerkit.utils.StringUtil;
import com.cicada.player.utils.Logger;

/**
 * 默认播放器全局日志实现
 * <p>
 * 基于播放器 SDK 的 Logger 实现全局日志功能。
 * </p>
 * <p>
 * Default Player Global Logger Implementation
 * <p>
 * Implements global logging functionality based on the player SDK's Logger.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class DefaultPlayerLogger implements IPlayerLogger {

    private static final String TAG = "DefaultPlayerLogger";

    private final Context mContext;

    private volatile boolean mConsoleLogEnabled = false;
    private volatile LoggerCallback mLogCallback = null;

    @LogLevel
    private volatile int mLogLevel;

    /**
     * 构造函数
     *
     * @param context 应用上下文
     */
    public DefaultPlayerLogger(@NonNull Context context) {
        this.mContext = context.getApplicationContext();
        this.mLogLevel = LogHub.getLogLevel();
        initializeLogger();
    }

    /**
     * 初始化 Logger
     */
    private void initializeLogger() {
        // 设置 SDK Logger 回调
        Logger.getInstance(mContext).setLogCallback(new Logger.OnLogCallback() {
            @Override
            public void onLog(Logger.LogLevel logLevel, String message) {
                // 转换日志级别并回调
                LoggerCallback callback = mLogCallback;
                if (callback != null && StringUtil.isNotEmpty(message)) {
                    @LogLevel int level = LogLevelConverter.fromSdkLogLevel(logLevel);
                    callback.onLog(level, message);
                }
            }
        });
        LogHub.i(TAG, "Logger initialized");
    }

    @Override
    public void enableConsoleLog(boolean enable) {
        mConsoleLogEnabled = enable;
        Logger.getInstance(mContext).enableConsoleLog(enable);
        LogHub.i(TAG, "Console log enabled: " + enable);
    }

    @Override
    public void setLogLevel(@LogLevel int level) {
        mLogLevel = level;
        Logger.getInstance(mContext).setLogLevel(LogLevelConverter.toSdkLogLevel(level));
        LogHub.i(TAG, "Log level set: " + level);
    }

    @Override
    @LogLevel
    public int getLogLevel() {
        return mLogLevel;
    }

    @Override
    public boolean isConsoleLogEnabled() {
        return mConsoleLogEnabled;
    }

    @Override
    public void setLogCallback(@Nullable LoggerCallback callback) {
        mLogCallback = callback;
        LogHub.i(TAG, "Log callback set: " + (callback != null));
    }
}
