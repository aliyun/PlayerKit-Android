package com.aliyun.playerkit.examples.preload;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.preload.PlayerPreloadConfig;
import com.aliyun.playerkit.preload.PlayerPreloadTask;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.preload.IPlayerPreloader;
import com.aliyun.playerkit.preload.PreloadCallback;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.strategy.StrategyManager;
import com.aliyun.playerkit.strategy.strategies.FirstFrameStrategy;
import com.aliyun.playerkit.utils.FormatUtil;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.Locale;

/**
 * 预加载使用示例 Activity
 * <p>
 * 演示如何使用预加载 API：
 * <ul>
 *     <li>通过 {@link AliPlayerKit#getPreloader()} 获取预加载器</li>
 *     <li>通过 {@link IPlayerPreloader#addTask(PlayerPreloadTask, PreloadCallback)} 添加预加载任务</li>
 *     <li>通过 {@link PreloadCallback} 监听预加载状态</li>
 *     <li>通过 {@link FirstFrameStrategy} 监听首帧时间，对比预加载效果</li>
 *     <li>通过 {@link AliPlayerKit#clearCaches()} 清除缓存</li>
 * </ul>
 * </p>
 * <p>
 * Preload Example Activity
 * <p>
 * Demonstrates how to use preload API:
 * <ul>
 *     <li>Get preloader via {@link AliPlayerKit#getPreloader()}</li>
 *     <li>Add preload task via {@link IPlayerPreloader#addTask(PlayerPreloadTask, PreloadCallback)}</li>
 *     <li>Monitor preload status via {@link PreloadCallback}</li>
 *     <li>Monitor first frame time via {@link FirstFrameStrategy} to compare preload effects</li>
 *     <li>Clear cache via {@link AliPlayerKit#clearCaches()}</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class PreloadExampleActivity extends AppCompatActivity {

    // 预加载开关切换后的冷却时间（毫秒）
    private static final long PRELOAD_SWITCH_COOLDOWN_MS = 3 * 1000;

    /**
     * 页面状态
     */
    private enum PageState {
        /**
         * 空闲状态：未播放，显示预加载开关和播放按钮
         */
        IDLE,
        /**
         * 预加载状态：预加载中或已预加载，显示预加载开关和播放按钮
         */
        PRELOAD,
        /**
         * 播放状态：正在播放，显示清除缓存按钮
         */
        PLAYING,
    }

    // ==================== 组件 ====================

    // 播放器组件视图
    private AliPlayerView mPlayerView;
    // 播放器组件控制器（每次重新创建，不复用）
    private AliPlayerController mPlayerController;

    // ==================== 预加载 ====================

    // 预加载器
    private final IPlayerPreloader mPreloader = AliPlayerKit.getPreloader();
    // 预加载回调
    private final PreloadCallback mPreloadCallback = new PreloadCallback() {
        @Override
        public void onCompleted(@NonNull String taskId, @NonNull VideoSource source) {
            runOnUiThread(() -> {
                appendPreloadStatus(R.string.status_preload_completed, taskId);
                mIsPreloaded = true;
            });
        }

        @Override
        public void onError(@NonNull String taskId, @NonNull VideoSource source, int code, @NonNull String message) {
            runOnUiThread(() -> {
                appendPreloadStatus(R.string.status_preload_error, taskId, code, message);
                mIsPreloaded = false;
                mPreloadTaskId = null;
                mSwitchPreload.setChecked(false);
                updatePageState(PageState.IDLE);
            });
        }

        @Override
        public void onCanceled(@NonNull String taskId, @NonNull VideoSource source) {
            runOnUiThread(() -> {
                appendPreloadStatus(R.string.status_preload_canceled, taskId);
                mIsPreloaded = false;
                mPreloadTaskId = null;
                mSwitchPreload.setChecked(false);
                updatePageState(PageState.IDLE);
            });
        }
    };

    // ==================== UI 组件 ====================

    private Switch mSwitchPreload;
    private Button mBtnPlay;
    private Button mBtnClear;
    private TextView mTvFirstFrameTime;
    private ScrollView mSvPreloadStatus;
    private TextView mTvPreloadStatus;

    // ==================== 状态管理 ====================

    // 预加载任务 ID
    private String mPreloadTaskId;
    // 是否已预加载
    private boolean mIsPreloaded = false;
    // 播放器是否已 attach
    private boolean mIsPlayerAttached = false;
    // 当前页面状态
    private PageState mPageState = PageState.IDLE;
    // 预加载开关切换后的冷却时间标记
    private long mPreloadSwitchCooldownUntil = 0;

    // ==================== 事件和工具 ====================

    // 事件总线
    private PlayerEventBus mEventBus;
    // 状态变化监听器
    private PlayerEventBus.EventListener<PlayerEvents.StateChanged> mStateChangedListener;
    // 预加载开关监听器
    private final CompoundButton.OnCheckedChangeListener mPreloadSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // 检查冷却时间
            if (isInCooldown()) {
                restoreSwitchState(isChecked);
                return;
            }

            // 设置冷却时间并禁用控件
            startCooldown();

            // 清除缓存（每次切换都清除缓存）
            clearCache();

            // 如果关闭预加载，更新状态
            if (!isChecked) {
                appendPreloadStatus(R.string.status_preload_closed);
            }
        }
    };

    // Handler 用于延迟操作
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // 视频源（与播放器使用相同的数据源）
    private final VideoSource.VidAuthSource mVideoSource = VideoSourceFactory.createVidAuthSource(
            SceneConstants.LANDSCAPE_SAMPLE_VID,
            SceneConstants.LANDSCAPE_SAMPLE_PLAY_AUTH
    );

    // ==================== 生命周期 ====================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preload_example);

        initViews();
        clearCacheOnEntry();
        setupPreloadSwitch();
        setupButtons();
        updatePageState(PageState.IDLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消 Handler 中的延迟任务
        mHandler.removeCallbacksAndMessages(null);

        // 取消订阅，避免内存泄漏
        if (mStateChangedListener != null && mEventBus != null) {
            mEventBus.unsubscribe(PlayerEvents.StateChanged.class, mStateChangedListener);
        }

        // 取消预加载任务
        if (mPreloadTaskId != null && mPreloader != null) {
            mPreloader.cancelTask(mPreloadTaskId);
        }

        // 解绑播放器组件，释放资源
        if (mPlayerView != null) {
            mPlayerView.detach();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView != null && mPlayerView.onBackPressed()) {
            // 已处理返回键（退出全屏），不需要执行默认行为
            return;
        }
        // 未处理，执行默认行为（关闭 Activity）
        super.onBackPressed();
    }

    // ==================== 初始化方法 ====================

    /**
     * 初始化视图
     */
    private void initViews() {
        mPlayerView = findViewById(R.id.v_player_kit);
        mSwitchPreload = findViewById(R.id.switch_preload);
        mBtnPlay = findViewById(R.id.btn_play_pause);
        mBtnClear = findViewById(R.id.btn_clear);
        mTvFirstFrameTime = findViewById(R.id.tv_first_frame_time);
        mSvPreloadStatus = findViewById(R.id.sv_preload_status);
        mTvPreloadStatus = findViewById(R.id.tv_preload_status);
    }

    /**
     * 进入页面时清除缓存
     */
    private void clearCacheOnEntry() {
        clearCache();
        appendPreloadStatus(R.string.tip_cache_cleared_on_entry);
    }

    /**
     * 设置预加载开关
     */
    private void setupPreloadSwitch() {
        mSwitchPreload.setOnCheckedChangeListener(mPreloadSwitchListener);
    }

    /**
     * 设置按钮
     */
    private void setupButtons() {
        mBtnPlay.setOnClickListener(v -> play());
        mBtnClear.setOnClickListener(v -> clearCache());
    }

    // ==================== 预加载开关处理 ====================

    /**
     * 恢复开关状态（用于冷却时间检查失败时）
     */
    private void restoreSwitchState(boolean isChecked) {
        mSwitchPreload.setOnCheckedChangeListener(null);
        mSwitchPreload.setChecked(!isChecked);
        mSwitchPreload.setOnCheckedChangeListener(mPreloadSwitchListener);
    }

    /**
     * 检查是否在冷却时间内
     */
    private boolean isInCooldown() {
        return System.currentTimeMillis() < mPreloadSwitchCooldownUntil;
    }

    /**
     * 开始冷却时间
     */
    private void startCooldown() {
        mPreloadSwitchCooldownUntil = System.currentTimeMillis() + PRELOAD_SWITCH_COOLDOWN_MS;
        setPreloadAndPlayEnabled(false);
        mHandler.postDelayed(() -> setPreloadAndPlayEnabled(true), PRELOAD_SWITCH_COOLDOWN_MS);
    }

    // ==================== 播放控制 ====================

    /**
     * 播放：attach 播放器（attach 后会自动起播）
     */
    private void play() {
        // 检查冷却时间
        if (isInCooldown()) {
            return;
        }

        // 如果播放器已 attach，无需重复操作
        if (mIsPlayerAttached) {
            return;
        }

        // attach 播放器组件
        attachPlayer();
    }

    // ==================== 缓存管理 ====================

    /**
     * 清除缓存：停止播放并清除缓存
     */
    private void clearCache() {
        // 如果播放器已 attach，先 detach（停止播放）
        if (mIsPlayerAttached) {
            detachPlayer();
        }

        // 如果预加载任务还在进行中，先取消
        cancelPreload();

        // 清除缓存示例：通过 AliPlayerKit.clearCaches() 清除所有播放器缓存
        AliPlayerKit.clearCaches();

        // 清除缓存后，如果预加载开关是开启的，需要重置预加载状态并重新预加载
        boolean wasPreloadEnabled = mSwitchPreload.isChecked();
        mIsPreloaded = false; // 重置预加载状态

        if (wasPreloadEnabled) {
            // 如果预加载开关是开启的，重新开始预加载
            appendPreloadStatus(R.string.status_cache_cleared_and_reload);
            startPreload();
            updatePageState(PageState.PRELOAD);
        } else {
            // 如果预加载开关是关闭的，切换到 IDLE 状态
            appendPreloadStatus(R.string.status_cache_cleared);
            updatePageState(PageState.IDLE);
        }
    }

    // ==================== 播放器管理 ====================

    /**
     * 初始化并 attach 播放器
     * <p>
     * Controller 每次重新创建，不复用
     * </p>
     */
    private void attachPlayer() {
        // 1. 创建播放器组件控制器（每次重新创建，不复用）
        mPlayerController = new AliPlayerController(this);

        // 2. 配置播放器组件数据
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(mVideoSource)
                .videoTitle("Preload Example")
                .build();

        // 3. 注册首帧策略，用于监听首帧时间
        registerFirstFrameStrategy();

        // 4. 设置事件总线：监听播放器状态变化
        setupEventBus();

        // 5. 绑定控制器和数据到视图
        mPlayerView.attach(mPlayerController, playerModel);
        mIsPlayerAttached = true;

        // 6. 显示首帧时间显示区域
        showFirstFrameTimeView();

        // 7. 记录状态
        int statusResId = mIsPreloaded
                ? R.string.status_player_attached_with_preload
                : R.string.status_player_attached_without_preload;
        appendPreloadStatus(statusResId);

        // 8. attach 后会自动起播，通过事件监听更新页面状态
    }

    /**
     * 注册首帧策略
     */
    private void registerFirstFrameStrategy() {
        StrategyManager strategyManager = mPlayerController.getStrategyManager();
        FirstFrameStrategy firstFrameStrategy = new FirstFrameStrategy(
                (prepareTime, renderTime, totalTime) -> runOnUiThread(() -> {
                    String message = getString(R.string.first_frame_time_format, totalTime, prepareTime, renderTime, FormatUtil.formatCurrentTime());
                    mTvFirstFrameTime.setText(message);
                })
        );
        strategyManager.register(firstFrameStrategy);
    }

    /**
     * Detach 播放器
     */
    private void detachPlayer() {
        if (!mIsPlayerAttached) {
            return;
        }

        // 取消订阅，避免内存泄漏
        unsubscribeEventBus();

        // 解绑播放器组件，释放资源
        if (mPlayerView != null) {
            mPlayerView.detach();
        }

        // 隐藏首帧时间显示区域
        hideFirstFrameTimeView();

        // 重置状态
        mPlayerController = null;
        mIsPlayerAttached = false;
        appendPreloadStatus(R.string.status_player_detached);
    }

    /**
     * 取消订阅事件总线
     */
    private void unsubscribeEventBus() {
        if (mStateChangedListener != null && mEventBus != null) {
            mEventBus.unsubscribe(PlayerEvents.StateChanged.class, mStateChangedListener);
            mStateChangedListener = null;
        }
    }

    /**
     * 隐藏首帧时间视图
     */
    private void hideFirstFrameTimeView() {
        if (mTvFirstFrameTime != null) {
            mTvFirstFrameTime.setVisibility(View.GONE);
        }
    }

    /**
     * 显示首帧时间视图
     */
    private void showFirstFrameTimeView() {
        if (mTvFirstFrameTime != null) {
            mTvFirstFrameTime.setVisibility(View.VISIBLE);
        }
    }

    // ==================== 事件总线 ====================

    /**
     * 设置事件总线：监听播放器状态变化
     */
    private void setupEventBus() {
        mEventBus = PlayerEventBus.getInstance();

        mStateChangedListener = event -> {
            if (isEventFromCurrentPlayer(event)) {
                runOnUiThread(() -> handlePlayerStateChanged(event));
            }
        };

        mEventBus.subscribe(PlayerEvents.StateChanged.class, mStateChangedListener);
    }

    /**
     * 检查事件是否来自当前播放器
     */
    private boolean isEventFromCurrentPlayer(PlayerEvents.StateChanged event) {
        if (mPlayerController == null || mPlayerController.getPlayer() == null) {
            return false;
        }
        String playerId = mPlayerController.getPlayer().getPlayerId();
        return StringUtil.equals(playerId, event.playerId);
    }

    /**
     * 处理播放器状态变化
     */
    private void handlePlayerStateChanged(PlayerEvents.StateChanged event) {
        PlayerState newState = event.newState;

        // attach 后会自动起播，当状态变为 PLAYING 时，切换到 PLAYING 状态
        if (newState == PlayerState.PLAYING) {
            updatePageState(PageState.PLAYING);
        }
    }

    // ==================== 预加载管理 ====================

    /**
     * 开始预加载
     * <p>
     * 预加载任务添加示例：通过 IPlayerPreloader.addTask() 添加预加载任务
     * </p>
     */
    private void startPreload() {
        // 创建预加载配置
        PlayerPreloadConfig preloadConfig = new PlayerPreloadConfig();

        // 创建预加载任务
        PlayerPreloadTask preloadTask = new PlayerPreloadTask(mVideoSource, preloadConfig);

        // 添加预加载任务示例：通过 IPlayerPreloader.addTask() 添加预加载任务
        mPreloadTaskId = mPreloader.addTask(preloadTask, mPreloadCallback);
        appendPreloadStatus(R.string.status_preload_started, mPreloadTaskId);
    }

    /**
     * 取消预加载
     * <p>
     * 预加载任务取消示例：通过 IPlayerPreloader.cancelTask() 取消预加载任务
     * </p>
     */
    private void cancelPreload() {
        if (StringUtil.isEmpty(mPreloadTaskId)) {
            return;
        }

        appendPreloadStatus(R.string.status_preload_canceling, mPreloadTaskId);
        mPreloader.cancelTask(mPreloadTaskId);

        mPreloadTaskId = null;
        mIsPreloaded = false;
    }

    // ==================== 状态管理 ====================

    /**
     * 更新页面状态
     */
    private void updatePageState(PageState newState) {
        mPageState = newState;

        switch (newState) {
            case IDLE:
            case PRELOAD:
                // IDLE 和 PRELOAD 状态：显示预加载开关和播放按钮
                showIdleOrPreloadControls();
                break;

            case PLAYING:
                // PLAYING 状态：显示清除缓存按钮
                showPlayingControls();
                break;
        }

        // 更新控件可用状态
        updateControlsEnabled();
    }

    /**
     * 显示 IDLE 或 PRELOAD 状态的控件
     */
    private void showIdleOrPreloadControls() {
        mSwitchPreload.setVisibility(View.VISIBLE);
        mBtnPlay.setVisibility(View.VISIBLE);
        mBtnClear.setVisibility(View.GONE);
    }

    /**
     * 显示 PLAYING 状态的控件
     */
    private void showPlayingControls() {
        mSwitchPreload.setVisibility(View.GONE);
        mBtnPlay.setVisibility(View.GONE);
        mBtnClear.setVisibility(View.VISIBLE);
    }

    /**
     * 更新控件可用状态
     */
    private void updateControlsEnabled() {
        boolean inCooldown = isInCooldown();

        switch (mPageState) {
            case IDLE:
            case PRELOAD:
                // IDLE 和 PRELOAD 状态：预加载开关和播放按钮可用（不在冷却时间内）
                mSwitchPreload.setEnabled(!inCooldown);
                mBtnPlay.setEnabled(!inCooldown);
                break;

            case PLAYING:
                // PLAYING 状态：清除缓存按钮可用
                mBtnClear.setEnabled(true);
                break;
        }
    }

    /**
     * 设置预加载开关和播放按钮的可用状态
     */
    private void setPreloadAndPlayEnabled(boolean enabled) {
        mSwitchPreload.setEnabled(enabled);
        mBtnPlay.setEnabled(enabled);
    }

    // ==================== UI 辅助方法 ====================

    /**
     * 追加预加载状态信息（使用字符串资源）
     *
     * @param resId 字符串资源 ID
     * @param args  格式化参数（可选）
     */
    private void appendPreloadStatus(int resId, Object... args) {
        String message = args.length > 0
                ? getString(resId, args)
                : getString(resId);
        appendPreloadStatusInternal(message);
    }

    /**
     * 追加预加载状态信息内部实现
     *
     * @param message 状态消息
     */
    private void appendPreloadStatusInternal(String message) {
        if (mTvPreloadStatus == null) {
            return;
        }

        String time = FormatUtil.formatCurrentTime();
        String statusLine = String.format(Locale.getDefault(), "[%s] %s\n", time, message);
        String currentText = mTvPreloadStatus.getText().toString();
        mTvPreloadStatus.setText(currentText + statusLine);
        scrollToBottom();
    }

    /**
     * 滚动 ScrollView 到底部
     */
    private void scrollToBottom() {
        if (mSvPreloadStatus != null) {
            mSvPreloadStatus.post(() -> mSvPreloadStatus.fullScroll(View.FOCUS_DOWN));
        }
    }
}
