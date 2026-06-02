package com.aliyun.playerkit.ui.slots;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.FullscreenEvents;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.FormatUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 底部控制栏插槽
 * <p>
 * 提供播放器底部控制栏功能，包括播放/暂停控制、进度条、时间显示和全屏按钮。
 * 实时更新播放进度和缓冲进度，响应用户的拖动操作进行视频跳转。
 * </p>
 * <p>
 * Bottom Control Bar Slot
 * <p>
 * Provides bottom control bar functionality for the player, including play/pause control, progress bar, time display, and fullscreen button.
 * Updates playback progress and buffering progress in real-time, and responds to user drag operations for video seeking.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public class BottomBarSlot extends BaseControlBarSlot implements View.OnClickListener {

    // ==================== UI 组件 ====================

    /**
     * 播放/暂停按钮
     * <p>
     * 点击后切换播放和暂停状态。
     * </p>
     */
    private ImageView mIvPlayPause;

    /**
     * 刷新按钮
     * <p>
     * 点击后重新播放视频。
     * 仅在直播场景（LIVE）下显示。
     * </p>
     */
    private ImageView mIvReplay;

    /**
     * 进度条
     * <p>
     * 显示播放进度和缓冲进度，支持拖动跳转。
     * </p>
     */
    private SeekBar mSeekBar;

    /**
     * 时间显示文本视图
     * <p>
     * 显示当前播放时间和总时长，格式为 "当前时间/总时长"。
     * </p>
     */
    private TextView mTvTime;

    /**
     * 全屏按钮
     * <p>
     * 点击后切换全屏模式。
     * </p>
     */
    private ImageView mIvFullscreen;

    // ==================== 状态变量 ====================

    /**
     * 是否正在拖动进度条
     * <p>
     * 拖动时暂停进度更新，避免拖动操作被进度更新覆盖。
     * </p>
     */
    private boolean mIsDragging = false;

    // ==================== 横屏 UI 组件（ViewStub 懒加载） ====================

    /**
     * 横屏布局的 ViewStub
     * <p>
     * 首次进入全屏时 inflate，避免竖屏场景下承担不必要的布局开销。
     * </p>
     */
    private ViewStub mViewStub;

    /**
     * 横屏布局根视图
     * <p>
     * 由 {@link #mViewStub} inflate 生成，为 null 表示尚未进入过全屏。
     * </p>
     */
    private View mLandScapeView;

    /**
     * 竖屏布局根视图
     */
    private View mPortraitView;

    /**
     * 横屏 - 时间显示文本视图
     */
    private TextView mTvTimeLS;

    /**
     * 横屏 - 进度条
     */
    private SeekBar mSeekBarLS;

    /**
     * 横屏 - 播放/暂停按钮
     */
    private ImageView mIvPlayPauseLS;

    /**
     * 横屏 - 刷新按钮
     */
    private ImageView mIvReplayLS;

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(ControlBarEvents.Show.class, ControlBarEvents.Hide.class, ControlBarEvents.ResetTimer.class, PlayerEvents.StateChanged.class, PlayerEvents.Prepared.class, PlayerEvents.Info.class, FullscreenEvents.FullScreenChanged.class);


    /**
     * 视频总时长（毫秒）
     * <p>
     * 用于计算进度百分比和显示总时长。
     * </p>
     */
    private long mDuration = 0;

    /**
     * 当前场景类型
     * <p>
     * 用于判断是否允许进度条拖拽（RESTRICTED 场景下禁用）。
     * </p>
     */
    @SceneType
    private int mSceneType = SceneType.VOD;

    // ==================== 构造函数 ====================

    public BottomBarSlot(@NonNull Context context) {
        super(context);
    }

    // ==================== 生命周期方法 ====================

    @Override
    protected int getLayoutId() {
        return R.layout.layout_bottom_bar_slot;
    }

    @Override
    protected void onRegisterElements() {
        registerElement(SlotElements.BottomBar.PLAY, visible -> setViewVisible(visible, mIvPlayPause, mIvPlayPauseLS));
        registerElement(SlotElements.BottomBar.REFRESH, visible -> setViewVisible(visible, mIvReplay, mIvReplayLS));
        registerElement(SlotElements.BottomBar.FULLSCREEN, mIvFullscreen);
        // PROGRESS 控制多个 View，保持 SlotElementHandle 方式
        registerElement(SlotElements.BottomBar.PROGRESS, visible -> setViewVisible(visible, mSeekBar, mTvTime, mSeekBarLS, mTvTimeLS));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 竖屏
        mPortraitView = findViewByIdCompat(R.id.ll_view_portrait);

        // 初始化播放/暂停按钮
        mIvPlayPause = findViewByIdCompat(R.id.iv_play_pause);

        // 初始化刷新按钮（可选：直播场景）
        mIvReplay = findViewByIdCompat(R.id.iv_replay);

        // 初始化时间显示
        mTvTime = findViewByIdCompat(R.id.tv_time);

        // 初始化全屏按钮
        mIvFullscreen = findViewByIdCompat(R.id.iv_fullscreen);
        setViewClickListener(this, mIvPlayPause, mIvReplay, mIvFullscreen);

        // 初始化进度条并设置监听器
        mSeekBar = findViewByIdCompat(R.id.seek_bar);
        setSeekbarListener(mSeekBar);

        mViewStub = findViewByIdCompat(R.id.stub_bottom_bar_landscape);
    }


    /**
     * 处理全屏切换
     * <p>
     * 进入全屏时懒加载横屏布局并同步状态；退出全屏时切回竖屏布局。
     * </p>
     *
     * @param isFullScreen 是否进入全屏
     */
    private void onToggleFullscreen(boolean isFullScreen) {
        if (isFullScreen) {
            if (mLandScapeView == null) {
                buildLandScapeView();
                // 首次 inflate，同步当前状态
                syncLandscapeState();
            }
            mPortraitView.setVisibility(View.GONE);
            mLandScapeView.setVisibility(View.VISIBLE);
        } else {
            if (mLandScapeView != null) {
                mLandScapeView.setVisibility(View.GONE);
            }
            mPortraitView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 同步竖屏状态到横屏
     * <p>
     * 首次 inflate 横屏布局后调用，将播放状态、进度条、时间文本同步到横屏视图。
     * </p>
     */
    private void syncLandscapeState() {
        if (mIvPlayPauseLS != null && mIvPlayPause != null) {
            mIvPlayPauseLS.setSelected(mIvPlayPause.isSelected());
        }
        if (mSeekBarLS != null && mSeekBar != null) {
            mSeekBarLS.setProgress(mSeekBar.getProgress());
            mSeekBarLS.setSecondaryProgress(mSeekBar.getSecondaryProgress());
        }
        if (mTvTimeLS != null && mTvTime != null) {
            mTvTimeLS.setText(mTvTime.getText());
        }
    }


    /**
     * 构建横屏布局
     * <p>
     * 通过 ViewStub 懒加载横屏布局，绑定 UI 组件引用并设置监听器。
     * 仅在首次进入全屏时调用一次。
     * </p>
     */
    private void buildLandScapeView() {
        mLandScapeView = mViewStub.inflate();

        mTvTimeLS = mLandScapeView.findViewById(R.id.tv_time_landscape);

        mSeekBarLS = mLandScapeView.findViewById(R.id.seek_bar_landscape);

        mIvReplayLS = mLandScapeView.findViewById(R.id.iv_replay_landscape);

        mIvPlayPauseLS = mLandScapeView.findViewById(R.id.iv_play_pause_landscape);

        TextView tvQuality = mLandScapeView.findViewById(R.id.tv_quality);

        TextView tvSpeed = mLandScapeView.findViewById(R.id.tv_speed);

        setSeekbarListener(mSeekBarLS);

        setViewClickListener(this, mIvPlayPauseLS, mIvReplayLS, tvQuality, tvSpeed);
    }


    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        // 存储场景类型，用于判断是否允许进度条拖拽
        mSceneType = model.getSceneType();
        // 刷新按钮（可选：直播场景）
        updateReplayButtonVisibility();
    }

    /**
     * 更新刷新按钮可见性
     * <p>
     * 仅在直播场景（LIVE）下显示刷新按钮，其他场景隐藏。
     * </p>
     */
    private void updateReplayButtonVisibility() {
        setViewVisible((mSceneType == SceneType.LIVE), mIvReplay, mIvReplayLS);
    }

    @Override
    public void onDetach() {
        // 清理状态
        mIsDragging = false;
        mDuration = 0;
        mSceneType = SceneType.VOD;

        // 清理横屏视图引用，防止重新 attach 时持有已脱离视图树的旧引用
        mViewStub = null;
        mLandScapeView = null;
        mTvTimeLS = null;
        mSeekBarLS = null;
        mIvPlayPauseLS = null;
        mIvReplayLS = null;

        // 取消自动隐藏任务
        cancelAutoHide();

        super.onDetach();
    }

    // ==================== 事件处理 ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        super.onEvent(event);

        if (event instanceof PlayerEvents.StateChanged) {
            onStateChanged((PlayerEvents.StateChanged) event);
        } else if (event instanceof PlayerEvents.Prepared) {
            onPrepared((PlayerEvents.Prepared) event);
        } else if (event instanceof PlayerEvents.Info) {
            onInfo((PlayerEvents.Info) event);
        } else if (event instanceof FullscreenEvents.FullScreenChanged) {
            onToggleFullscreen(((FullscreenEvents.FullScreenChanged) event).isFullscreen);
        }
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理播放器状态变化事件
     * <p>
     * 根据新的播放状态更新播放/暂停按钮图标。
     * </p>
     *
     * @param event 状态变化事件，不能为 null
     */
    private void onStateChanged(@NonNull PlayerEvents.StateChanged event) {
        updatePlayPauseIcon(event.newState);
    }

    /**
     * 处理播放器准备完成事件
     * <p>
     * 当视频准备完成时，获取视频总时长并初始化时间显示。
     * </p>
     *
     * @param event 准备完成事件，不能为 null
     */
    private void onPrepared(@NonNull PlayerEvents.Prepared event) {
        mDuration = event.duration;
        updateTimeText(0, mDuration);
    }

    /**
     * 处理播放器信息更新事件
     * <p>
     * 当不在拖动状态时，更新播放进度、缓冲进度和时间显示。
     * 拖动时暂停更新，避免拖动操作被进度更新覆盖。
     * </p>
     *
     * @param event 信息更新事件，不能为 null
     */
    private void onInfo(@NonNull PlayerEvents.Info event) {
        // 不在拖动状态时更新
        if (mIsDragging) {
            return;
        }
        mDuration = event.duration;
        updateProgress(event.currentPosition, event.bufferedPosition, event.duration);
    }


    // ==================== UI 更新方法 ====================

    /**
     * 更新播放/暂停按钮图标
     * <p>
     * 根据播放状态显示对应的图标（播放中显示暂停图标，其他状态显示播放图标）。
     * </p>
     *
     * @param state 播放器状态，不能为 null
     */
    private void updatePlayPauseIcon(@NonNull PlayerState state) {
        if (null != mIvPlayPause) {
            mIvPlayPause.setSelected(state == PlayerState.PLAYING);
        }

        if (null != mIvPlayPauseLS) {
            mIvPlayPauseLS.setSelected(state == PlayerState.PLAYING);
        }
    }

    /**
     * 更新进度条和时间显示
     * <p>
     * 根据当前播放位置、缓冲位置和总时长更新进度条和时间显示。
     * </p>
     *
     * @param current  当前播放位置（毫秒）
     * @param buffered 缓冲位置（毫秒）
     * @param duration 总时长（毫秒）
     */
    private void updateProgress(long current, long buffered, long duration) {
        if (duration <= 0) {
            return;
        }
        int progress = Math.round((float) current * 100 / duration);
        int secondaryProgress = Math.round((float) buffered * 100 / duration);

        if (null != mSeekBar) {
            mSeekBar.setProgress(progress);
            mSeekBar.setSecondaryProgress(secondaryProgress);
        }

        if (null != mSeekBarLS) {
            mSeekBarLS.setProgress(progress);
            mSeekBarLS.setSecondaryProgress(secondaryProgress);
        }

        updateTimeText(current, duration);
    }

    /**
     * 更新时间显示文本
     * <p>
     * 格式化并显示当前播放时间和总时长，格式为 "当前时间/总时长"。
     * </p>
     *
     * @param current  当前播放时间（毫秒）
     * @param duration 总时长（毫秒）
     */
    private void updateTimeText(long current, long duration) {
        String text = FormatUtil.formatDuration(current) + "/" + FormatUtil.formatDuration(duration);
        if (null != mTvTime) {
            mTvTime.setText(text);
        }
        if (null != mTvTimeLS) {
            mTvTimeLS.setText(text);
        }
    }

    /**
     * 恢复进度条位置
     * <p>
     * 根据当前播放位置更新进度条位置。
     * </p>
     *
     * @param seekBar 进度条，不能为 null
     */
    private void restoreSeekBarPosition(SeekBar seekBar) {
        SlotHost host = getHost();
        if (host != null && seekBar != null && mDuration > 0) {
            long currentPosition = host.getPlayerStateStore().getCurrentPosition();
            seekBar.setProgress(Math.round((float) currentPosition * 100 / mDuration));
        }
    }


    // ==================== 工具方法 ====================

    /**
     * 批量设置视图可见性
     * <p>
     * 安全处理 null 视图（横屏视图在 inflate 前为 null）。
     * </p>
     *
     * @param visible 是否可见
     * @param views   目标视图列表
     */
    private void setViewVisible(boolean visible, View... views) {
        int visibleValue = visible ? View.VISIBLE : View.GONE;
        if (null != views) {
            for (View v : views) {
                if (null != v) {
                    v.setVisibility(visibleValue);
                }
            }
        }
    }

    /**
     * 批量设置点击监听器
     * <p>
     * 安全处理 null 视图。
     * </p>
     *
     * @param listener 点击监听器
     * @param views    目标视图列表
     */
    private void setViewClickListener(View.OnClickListener listener, View... views) {
        if (null != views) {
            for (View v : views) {
                if (null != v) {
                    v.setOnClickListener(listener);
                }
            }
        }
    }

    /**
     * 为进度条设置触摸拦截和拖动监听
     * <p>
     * 统一竖屏和横屏 SeekBar 的行为：特定场景下禁止拖动，正常场景下支持拖动跳转。
     * </p>
     *
     * @param bar 目标进度条
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setSeekbarListener(SeekBar bar) {
        if (null == bar) return;

        // 特定场景下，进度条直接不允许触摸（仅展示进度，不允许用户拖拽跳转）
        // 说明：
        // 1) 通过拦截触摸事件避免"拖一下又回弹"的体验和不同 ROM 的行为差异；
        // 2) 不影响播放器 Info 更新进度（setProgress 仍然生效）。
        bar.setOnTouchListener((v, event) -> {
            if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                return true; // 消费事件，阻止进一步处理
            }
            return false; // 不消费事件，允许正常处理
        });

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 特定场景下禁用进度拖拽
                    // 说明：通常不会走到这里（因为上面拦截了触摸），保留判断作为防御
                    if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                        // 恢复进度条到当前播放位置
                        restoreSeekBarPosition(seekBar);
                        return;
                    }

                    // 用户拖动时，实时更新时间显示
                    long targetPosition = (long) ((progress / 100.0f) * mDuration);
                    updateTimeText(targetPosition, mDuration);
                    notifyInteraction();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 特定场景下禁用进度拖拽
                // 说明：通常不会走到这里（因为上面拦截了触摸），保留判断作为防御
                if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                    // 取消拖动操作
                    seekBar.setPressed(false);
                    return;
                }

                // 开始拖动时，标记拖动状态并重置自动隐藏计时器
                mIsDragging = true;
                notifyInteraction();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 特定场景下禁用进度拖拽
                // 说明：通常不会走到这里（因为上面拦截了触摸），保留判断作为防御
                if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                    // 恢复进度条到当前播放位置
                    restoreSeekBarPosition(seekBar);
                    return;
                }

                // 结束拖动时，执行跳转操作
                mIsDragging = false;
                long targetPosition = (long) ((seekBar.getProgress() / 100.0f) * mDuration);
                seekTo(targetPosition);

                notifyInteraction();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (null == mPlayerId) return;

        // 播放/暂停
        if (id == R.id.iv_play_pause || id == R.id.iv_play_pause_landscape) {
            notifyInteraction();
            postEvent(new PlayerCommand.Toggle(mPlayerId));
        }
        // 直播场景: 刷新
        else if (id == R.id.iv_replay || id == R.id.iv_replay_landscape) {
            notifyInteraction();
            postEvent(new PlayerCommand.Replay(mPlayerId));
        }
        // 全屏
        else if (id == R.id.iv_fullscreen) {
            notifyInteraction();
            postEvent(new FullscreenEvents.Toggle(mPlayerId));
        }
        // 清晰度
        else if (id == R.id.tv_quality) {
            showQualityDialog();
        }
        // 倍速
        else if (id == R.id.tv_speed) {
            showSpeedDialog();
        }
    }

    private void showQualityDialog() {
        if (mPlayerId == null) return;
        postEvent(new ControlBarEvents.Hide(mPlayerId));
        postEvent(new ControlBarEvents.ShowQualityPanel(mPlayerId));
    }

    private void showSpeedDialog() {
        if (mPlayerId == null) return;
        postEvent(new ControlBarEvents.Hide(mPlayerId));
        postEvent(new ControlBarEvents.ShowSpeedPanel(mPlayerId));
    }
}
