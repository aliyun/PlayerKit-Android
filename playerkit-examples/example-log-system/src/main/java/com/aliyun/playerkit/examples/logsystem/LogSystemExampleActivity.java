package com.aliyun.playerkit.examples.logsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.logging.LogHubListener;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 日志系统使用示例 Activity
 * <p>
 * 演示如何使用 PlayerKit 的日志系统（LogHub）进行日志输出。
 * 重点说明如何通过日志监听器将日志保存到文件中，便于在问题发生时提供给技术支持团队进行分析。
 * </p>
 * <p>
 * <b>接入步骤（仅需 2 步）</b>：
 * <ol>
 *     <li>创建 FileLogListener 实例：<code>new FileLogListener()</code></li>
 *     <li>注册监听器：<code>LogHub.addListener(fileLogListener)</code></li>
 * </ol>
 * </p>
 * <p>
 * Log System Example Activity
 * <p>
 * Demonstrates how to use PlayerKit's logging system (LogHub) for log output.
 * Focuses on how to use a log listener to save logs to a file, making it easier to provide them to the technical support team for analysis when issues occur.
 * </p>
 * <p>
 * <b>Integration Steps (Only 2 steps)</b>:
 * <ol>
 *     <li>Create FileLogListener instance: <code>new FileLogListener()</code></li>
 *     <li>Register listener: <code>LogHub.addListener(fileLogListener)</code></li>
 * </ol>
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class LogSystemExampleActivity extends AppCompatActivity {

    // UI 最多展示的日志行数（可按需调整）
    private static final int MAX_LOG_LINES = 10;

    private ScrollView mSvLogOutput;
    private TextView mTvLogOutput;

    // UI 日志监听器
    private LogHubListener mUILogListener;
    // 文件日志监听器，建议放在全局位置，避免重复创建，确保整个应用生命周期内都能收集日志
    private FileLogListener mFileLogListener;

    private final Deque<String> mLogLines = new ArrayDeque<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_system_example);

        initViews();
        setupUILogListener();
        setupFileLogListener();
        setupTestButtons();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mSvLogOutput = findViewById(R.id.sv_log_output);
        mTvLogOutput = findViewById(R.id.tv_log_output);
    }

    /**
     * 设置 UI 日志监听器（用于在界面上显示日志）
     */
    private void setupUILogListener() {
        mUILogListener = logInfo -> runOnUiThread(() -> {
            appendLogLine(logInfo.getFormattedMessage());
            mTvLogOutput.setText(buildLogText());
            scrollToBottom();
        });
        LogHub.addListener(mUILogListener);
    }

    /**
     * 追加一行日志，并限制最大行数（仅保留最近 MAX_LOG_LINES 行）
     */
    private void appendLogLine(String line) {
        mLogLines.addLast(line);
        while (mLogLines.size() > MAX_LOG_LINES) {
            mLogLines.removeFirst();
        }
    }

    /**
     * 拼接日志文本用于显示
     */
    private String buildLogText() {
        StringBuilder sb = new StringBuilder();
        for (String line : mLogLines) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /**
     * 滚动 ScrollView 到底部
     */
    private void scrollToBottom() {
        if (mSvLogOutput != null) {
            mSvLogOutput.post(() -> mSvLogOutput.fullScroll(View.FOCUS_DOWN));
        }
    }

    /**
     * 设置文件日志监听器（演示如何接入）
     */
    private void setupFileLogListener() {
        // 创建文件日志监听器实例
        mFileLogListener = new FileLogListener();

        // 注册按钮：一键接入
        findViewById(R.id.btn_register_file_listener).setOnClickListener(v -> {
            LogHub.addListener(mFileLogListener);
            String message = getString(R.string.file_listener_registered_log);
            ToastUtils.showToast(message);
            LogHub.i(this, "setupFileLogListener", getString(R.string.file_listener_registered_log));
        });
    }

    /**
     * 设置测试按钮
     */
    private void setupTestButtons() {
        findViewById(R.id.btn_test_info).setOnClickListener(v -> {
            LogHub.i(this, "testInfo", getString(R.string.test_log_info));
        });

        findViewById(R.id.btn_test_warn).setOnClickListener(v -> {
            LogHub.w(this, "testWarn", getString(R.string.test_log_warn));
        });

        findViewById(R.id.btn_test_error).setOnClickListener(v -> {
            LogHub.e(this, "testError", getString(R.string.test_log_error));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除日志监听器
        if (mUILogListener != null) {
            LogHub.removeListener(mUILogListener);
        }
        // 移除文件日志监听器，正常来讲，文件日志监听器应该只存在一个
        if (mFileLogListener != null) {
            LogHub.removeListener(mFileLogListener);
        }
    }
}
