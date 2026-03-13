package com.aliyun.playerkit.logging.logger;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.logging.LogLevel;

/**
 * 播放器日志回调接口
 * <p>
 * Player Log Callback Interface
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public interface LoggerCallback {
    /**
     * 日志回调
     *
     * @param level   日志级别
     * @param message 日志消息
     */
    void onLog(@LogLevel int level, @NonNull String message);
}
