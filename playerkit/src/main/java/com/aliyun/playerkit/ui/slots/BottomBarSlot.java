package com.aliyun.playerkit.ui.slots;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
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
public class BottomBarSlot extends BaseControlBarSlot {

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 初始化播放/暂停按钮
        mIvPlayPause = findViewByIdCompat(R.id.iv_play_pause);
        mIvPlayPause.setImageResource(R.drawable.ic_play_pause_selector);
        mIvPlayPause.setOnClickListener(v -> {
            notifyInteraction();
            postEvent(new PlayerCommand.Toggle(mPlayerId));
        });

        // 初始化刷新按钮（可选：直播场景）
        mIvReplay = findViewByIdCompat(R.id.iv_replay);
        mIvReplay.setOnClickListener(v -> {
            notifyInteraction();
            postEvent(new PlayerCommand.Replay(mPlayerId));
        });

        // 初始化进度条并设置监听器
        mSeekBar = findViewByIdCompat(R.id.seek_bar);

        // 特定场景下，进度条直接不允许触摸（仅展示进度，不允许用户拖拽跳转）
        // 说明：
        // 1) 通过拦截触摸事件避免"拖一下又回弹"的体验和不同 ROM 的行为差异；
        // 2) 不影响播放器 Info 更新进度（setProgress 仍然生效）。
        mSeekBar.setOnTouchListener((v, event) -> {
            if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                return true; // 消费事件，阻止进一步处理
            }
            return false; // 不消费事件，允许正常处理
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        // 初始化时间显示
        mTvTime = findViewByIdCompat(R.id.tv_time);

        // 初始化全屏按钮
        mIvFullscreen = findViewByIdCompat(R.id.iv_fullscreen);
        mIvFullscreen.setOnClickListener(v -> {
            // 全屏切换功能
            postEvent(new FullscreenEvents.Toggle(mPlayerId));
            notifyInteraction();
        });
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
        if (mIvReplay != null) {
            mIvReplay.setVisibility(mSceneType == SceneType.LIVE ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDetach() {
        // 清理状态
        mIsDragging = false;
        mDuration = 0;
        mSceneType = SceneType.VOD;

        // 取消自动隐藏任务
        cancelAutoHide();

        super.onDetach();
    }

    // ==================== 事件处理 ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Arrays.asList(ControlBarEvents.Show.class,
                ControlBarEvents.Hide.class,
                ControlBarEvents.ResetTimer.class,
                PlayerEvents.StateChanged.class,
                PlayerEvents.Prepared.class,
                PlayerEvents.Info.class
        );
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
        mIvPlayPause.setSelected(state == PlayerState.PLAYING);
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
        int progress = (int) (current * 100 / duration);
        int secondaryProgress = (int) (buffered * 100 / duration);
        mSeekBar.setProgress(progress);
        mSeekBar.setSecondaryProgress(secondaryProgress);
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
        String currentStr = FormatUtil.formatDuration(current);
        String durationStr = FormatUtil.formatDuration(duration);
        mTvTime.setText(String.format("%s/%s", currentStr, durationStr));
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
            seekBar.setProgress((int) ((currentPosition * 100.0f) / mDuration));
        }
    }
}
