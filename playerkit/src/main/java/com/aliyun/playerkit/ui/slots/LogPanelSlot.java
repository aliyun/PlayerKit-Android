package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.logging.LogHubListener;
import com.aliyun.playerkit.logging.LogInfo;
import com.aliyun.playerkit.logging.LogLevel;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.ClipboardUtils;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 日志面板插槽
 * <p>
 * 实时展示 PlayerKit 内部的关键日志信息，便于调试和问题排查。
 * </p>
 * <p>
 * 实现手机 App 自助运维，提升问题响应灵活性，
 * 解决周末及非工作时间“不能工作”的痛点...
 * </p>
 *
 * <p>
 * Log panel slot.
 * Displays key internal logs of PlayerKit in real time for easier debugging.
 * </p>
 * <p>
 * Enable mobile app–based self-service operations to respond to issues more flexibly,
 * fix the “we’re not supposed to work on weekends” problem...
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class LogPanelSlot extends BaseSlot implements LogHubListener {

    /**
     * 最多显示的日志行数
     * <p>
     * 超过则截断。
     */
    private static final int MAX_LINES = 2 * 1000;

    /**
     * 复制到剪贴板时的最大字符数上限
     * <p>
     * 超过则只复制尾部内容，避免极端长文本导致卡顿。
     */
    private static final int MAX_COPY_CHARS = 20 * 1000;

    /**
     * 判断是否在底部的阈值（像素）
     * <p>
     * 当滚动位置距离底部小于此值时，认为用户在底部。
     */
    private static final int SCROLL_TO_BOTTOM_THRESHOLD = 100;

    private ScrollView mScrollView;
    private TextView mTvLogs;
    private View mHeader;
    private TextView mTvTitle;
    private TextView mTvClear;
    private TextView mTvFullscreen;

    /**
     * 是否全屏高度
     */
    private boolean mIsFullscreen = false;

    /**
     * 是否展开（显示内容区）
     */
    private boolean mExpanded = true;

    /**
     * 日志行缓存：
     * - 缓存日志行，避免每次都重新构建文本；
     * - 缓存日志行数，避免缓存过多导致内存占用过多。
     */
    private final Deque<String> mLines = new ArrayDeque<>(MAX_LINES + 4);

    /**
     * 展示文本缓存：
     * - 日志未发生截断时增量追加，避免每条日志都重建整段文本；
     * - 发生截断（removeFirst）时再重建一次。
     */
    private final StringBuilder mTextCache = new StringBuilder(8 * 1024);

    public LogPanelSlot(@NonNull Context context) {
        super(context);
    }

    public LogPanelSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LogPanelSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_log_panel_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        mScrollView = findViewById(R.id.sv_logs);
        mTvLogs = findViewById(R.id.tv_logs);
        mHeader = findViewById(R.id.ll_header);
        mTvTitle = findViewById(R.id.tv_title);
        mTvClear = findViewById(R.id.tv_clear);
        mTvFullscreen = findViewById(R.id.tv_fullscreen);

        if (mTvClear != null) {
            mTvClear.setOnClickListener(v -> {
                clearLogs();
            });
        }

        if (mTvFullscreen != null) {
            mTvFullscreen.setOnClickListener(v -> {
                toggleFullscreen();
            });
        }

        if (mHeader != null) {
            mHeader.setOnClickListener(v -> toggleExpandCollapse());
        }

        if (mTvLogs != null) {
            mTvLogs.setOnLongClickListener(v -> {
                copyLogsToClipboard();
                return true;
            });
        }

        // 执行默认逻辑
        collapseContent(!mExpanded);
        refreshLayoutHeights();
        updateUI();

        // 注册日志监听
        LogHub.addListener(this);
    }

    @Override
    public void onDetach() {
        // 先移除监听，避免泄漏
        LogHub.removeListener(this);
        super.onDetach();
    }

    @Override
    public void onLog(@NonNull LogInfo logInfo) {
        // 仅在启用时才处理，双重保护
        if (!AliPlayerKit.isLogPanelEnabled()) {
            return;
        }

        // 只展示特定日志，否则忽略
        if (!acceptLog(logInfo)) {
            return;
        }

        // 确保在主线程更新 UI
        post(() -> appendLogLine(logInfo.getFormattedMessage()));
    }

    /**
     * 是否接受该日志
     *
     * @param logInfo 日志信息
     * @return true 接受，false 拒绝
     */
    protected boolean acceptLog(@NonNull LogInfo logInfo) {
        @LogLevel int level = logInfo.getLevel();
        // 忽略低于 DEBUG 的日志，避免导致面板卡顿
        if (level < LogLevel.DEBUG) {
            return false;
        }
        String message = logInfo.getMessage();
        if (StringUtil.isEmpty(message)) {
            return false;
        }

        // Note keria: 后续可以加上对于特定字符串的筛选
        return true;
    }

    private void appendLogLine(@NonNull String line) {
        if (mTvLogs == null) {
            return;
        }

        mLines.addLast(line);

        boolean trimmed = false;
        while (mLines.size() > MAX_LINES) {
            mLines.removeFirst();
            trimmed = true;
        }

        // 在修改文本之前，先记录是否在底部
        boolean wasAtBottom = false;
        if (mExpanded && mScrollView != null) {
            // 如果行数极少，认为是初始状态，允许自动滚动
            if (mLines.size() <= 5) {
                wasAtBottom = true;
            } else {
                wasAtBottom = isScrolledToBottom();
            }
        }

        if (trimmed) {
            // 发生截断：重建一次缓存
            mTextCache.setLength(0);
            for (String l : mLines) {
                mTextCache.append(l).append('\n');
            }
        } else {
            // 未截断：增量追加，避免每条日志都全量重建
            mTextCache.append(line).append('\n');
        }

        mTvLogs.setText(mTextCache);

        // 仅在展开且之前就在底部时自动滚动
        if (mExpanded && mScrollView != null && wasAtBottom) {
            mScrollView.post(() -> mScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    /**
     * 判断 ScrollView 是否滚动到底部
     * <p>
     * 如果用户在底部（或接近底部），返回 true；否则返回 false。
     * </p>
     *
     * @return true 表示在底部，false 表示不在底部
     */
    private boolean isScrolledToBottom() {
        if (mScrollView == null) {
            return true;
        }

        int scrollY = mScrollView.getScrollY();
        int height = mScrollView.getHeight();
        View child = mScrollView.getChildAt(0);
        if (child == null) {
            return true;
        }

        int childHeight = child.getHeight();
        int maxScrollY = Math.max(0, childHeight - height);

        // 如果内容不足以滚动（maxScrollY <= 0），认为在底部
        if (maxScrollY <= 0) {
            return true;
        }

        // 如果当前滚动位置距离底部小于阈值，认为在底部
        return scrollY >= maxScrollY - SCROLL_TO_BOTTOM_THRESHOLD;
    }

    /**
     * 切换展开/折叠状态
     */
    private void toggleExpandCollapse() {
        mExpanded = !mExpanded;
        collapseContent(!mExpanded);
        refreshLayoutHeights();
        updateUI();

        // 切到展开时，滚动到底
        if (mExpanded && mScrollView != null) {
            mScrollView.post(() -> mScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    /**
     * 折叠内容区
     *
     * @param collapse true 折叠，false 展开
     */
    private void collapseContent(boolean collapse) {
        if (mScrollView != null) {
            mScrollView.setVisibility(collapse ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 切换面板全屏状态，折叠时隐藏内容区
     */
    private void toggleFullscreen() {
        mIsFullscreen = !mIsFullscreen;

        // 全屏时强制展开（如果当前是收起状态）
        if (mIsFullscreen && !mExpanded) {
            mExpanded = true;
            collapseContent(false);
        }

        // 修改按钮文本
        if (mTvFullscreen != null) {
            mTvFullscreen.setText(mIsFullscreen ? R.string.log_panel_exit_fullscreen : R.string.log_panel_fullscreen);
        }

        // 更新 UI 状态
        refreshLayoutHeights();
        updateUI();

        // 5. 滚动到底部
        if (mExpanded && mScrollView != null) {
            mScrollView.post(() -> mScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    /**
     * 更新布局高度（处理全屏和展开逻辑的组合）
     */
    private void refreshLayoutHeights() {
        View root = findViewById(R.id.ll_root);
        if (root == null || mScrollView == null) {
            return;
        }

        android.view.ViewGroup.LayoutParams rootParams = root.getLayoutParams();
        android.view.ViewGroup.LayoutParams scrollParams = mScrollView.getLayoutParams();
        if (rootParams == null || scrollParams == null) {
            return;
        }

        // 根部高度逻辑：只有在 展开 且 全屏 时才 MATCH_PARENT；否则 WRAP_CONTENT（自适应头部或当前容器）
        rootParams.height = (mExpanded && mIsFullscreen) ? android.view.ViewGroup.LayoutParams.MATCH_PARENT : android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        root.setLayoutParams(rootParams);

        // ScrollView 细节高度：全屏时占据剩余空间，否则恢复默认定高
        if (mIsFullscreen) {
            scrollParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            scrollParams.height = getContext().getResources().getDimensionPixelSize(R.dimen.log_panel_default_height);
        }
        mScrollView.setLayoutParams(scrollParams);
    }

    /**
     * 更新 UI 状态
     */
    private void updateUI() {
        if (mTvFullscreen != null) {
            // 收起时，不展示全屏按钮；仅在展开时显示
            mTvFullscreen.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
        }
        if (mTvClear != null) {
            // 收起时，不展示清除按钮；仅在展开时显示
            mTvClear.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
        }
        if (mTvTitle != null) {
            // 更新标题内容
            String content = mExpanded ? getContext().getString(R.string.log_panel_title_hide) : getContext().getString(R.string.log_panel_title_show);
            mTvTitle.setText(content);
        }
    }

    /**
     * 将当前缓冲的日志复制到剪贴板。
     * <p>
     * - 如果没有日志，则无操作并给出提示；
     * - 如果总长度超过 {@link #MAX_COPY_CHARS}，仅复制尾部一段，避免极端长文本导致复制卡顿。
     * </p>
     */
    private void copyLogsToClipboard() {
        if (mLines.isEmpty()) {
            ToastUtils.showToast(R.string.log_panel_copy_no_logs);
            return;
        }

        // 直接使用当前展示缓存，避免再次遍历拼接
        String tailedString = StringUtil.tail(mTextCache.toString(), MAX_COPY_CHARS);

        Context context = getContext();
        boolean success = ClipboardUtils.copyText(context, context.getString(R.string.log_panel_clipboard_label), tailedString);
        ToastUtils.showToast(success ? R.string.log_panel_copy_success : R.string.log_panel_copy_failed);
    }

    /**
     * 清除所有日志
     * <p>
     * 清空日志缓存和显示内容，重置滚动位置。
     * </p>
     */
    private void clearLogs() {
        // 清空日志行缓存
        mLines.clear();

        // 清空文本缓存
        mTextCache.setLength(0);

        // 清空显示
        if (mTvLogs != null) {
            mTvLogs.setText("");
        }

        // 重置滚动位置到底部
        if (mScrollView != null) {
            mScrollView.post(() -> mScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }
}
