package com.aliyun.playerkit.examples.logsystem;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.logging.LogHubListener;
import com.aliyun.playerkit.logging.LogInfo;

/**
 * 日志文件存储监听器示例
 * <p>
 * 这是一个示例实现，展示如何通过 LogHubListener 接口将日志保存到文件中。
 * 当问题发生时，请协助提供相关日志文件，以便技术支持团队进行更深入、快速的分析和定位。
 * </p>
 * <p>
 * <b>使用方式</b>：
 * <pre>
 * // 1. 创建实例
 * FileLogListener listener = new FileLogListener();
 *
 * // 2. 注册监听器
 * LogHub.addListener(listener);
 * </pre>
 * </p>
 * <p>
 * <b>实现说明</b>：
 * <ul>
 *     <li>在 {@link #onLog(LogInfo)} 方法中实现日志文件写入逻辑</li>
 *     <li>日志文件路径、写入方式等可根据实际需求自行实现</li>
 *     <li>建议使用异步方式写入文件，避免阻塞主线程</li>
 *     <li><b>建议将文件日志监听器放在全局位置（如 Application 类）</b>，避免重复创建，确保整个应用生命周期内都能收集日志</li>
 * </ul>
 * </p>
 * <p>
 * File Log Listener Example
 * <p>
 * This is an example implementation showing how to save logs to files through the LogHubListener interface.
 * When an issue occurs, please assist by providing the relevant log files so that the technical support team can perform a more in-depth and efficient analysis and troubleshooting.
 * </p>
 * <p>
 * <b>Usage</b>:
 * <pre>
 * // 1. Create instance
 * FileLogListener listener = new FileLogListener();
 *
 * // 2. Register listener
 * LogHub.addListener(listener);
 * </pre>
 * </p>
 * <p>
 * <b>Implementation Notes</b>:
 * <ul>
 *     <li>Implement log file writing logic in the {@link #onLog(LogInfo)} method</li>
 *     <li>Log file path, writing method, etc. can be implemented according to actual needs</li>
 *     <li>It is recommended to use asynchronous file writing to avoid blocking the main thread</li>
 *     <li><b>It is recommended to place the file log listener in a global location (such as the Application class)</b> to avoid repeated creation and ensure logs are collected throughout the application lifecycle</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class FileLogListener implements LogHubListener {

    /**
     * 当日志输出时被调用
     * <p>
     * 此方法会在每次有日志输出时被调用，包括所有级别的日志。
     * </p>
     *
     * @param logInfo 日志信息对象，包含日志级别、标签、消息、异常等信息
     */
    @Override
    public void onLog(@NonNull LogInfo logInfo) {
        // TODO keria: 请在此处实现日志文件写入逻辑
    }

}
