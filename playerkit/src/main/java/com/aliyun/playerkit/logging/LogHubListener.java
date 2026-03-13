package com.aliyun.playerkit.logging;

import androidx.annotation.NonNull;

/**
 * 日志回调接口
 * <p>
 * 用于接收 LogHub 输出的日志信息，方便外部进行日志记录、文件写入等操作。
 * </p>
 * <p>
 * Log callback interface
 * <p>
 * Used to receive log information output by LogHub, convenient for external log recording, file writing, etc.
 * </p>
 *
 * @author keria
 * @date 2025/12/10
 */
public interface LogHubListener {

    /**
     * 当日志输出时被调用
     * <p>
     * Called when a log is output
     * </p>
     *
     * @param logInfo 日志信息对象，包含日志级别、标签、消息、异常等信息
     */
    void onLog(@NonNull LogInfo logInfo);
}
