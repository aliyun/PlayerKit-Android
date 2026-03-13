package com.aliyun.playerkit.strategy.strategies;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.strategy.BaseStrategy;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 首帧耗时策略
 * <p>
 * 监听播放器的状态变化、Prepared 和 FirstFrameRendered 事件，计算首帧耗时。
 * 支持通过 Toast 显示结果（仅 Debug 模式）和回调通知。
 * </p>
 * <p>
 * First Frame Strategy
 * <p>
 * Monitors player state changes, Prepared, and FirstFrameRendered events to calculate
 * first frame rendering time. Supports displaying results via Toast (Debug mode only)
 * and callback notification.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public class FirstFrameStrategy extends BaseStrategy {

    private static final String TAG = "FirstFrameStrategy";

    /**
     * 首帧耗时结果回调
     */
    public interface Callback {
        /**
         * 首帧耗时计算完成
         *
         * @param prepareTime prepare 耗时（ms），从 PREPARING 到 Prepared
         * @param renderTime  render 耗时（ms），从 Prepared 到 FirstFrameRendered
         * @param totalTime   总耗时（ms），从 PREPARING 到 FirstFrameRendered
         */
        void onFirstFrameTime(long prepareTime, long renderTime, long totalTime);
    }

    /**
     * 结果回调
     */
    @Nullable
    private final Callback mCallback;

    /**
     * 开始准备时间戳
     */
    private volatile long mStartTime = 0;

    /**
     * 准备完成时间戳
     */
    private volatile long mPreparedTime = 0;

    /**
     * 是否已完成首帧计算（避免重复回调）
     */
    private volatile boolean mFirstFrameCompleted = false;

    /**
     * 创建首帧耗时策略（默认配置）
     */
    public FirstFrameStrategy() {
        this(null);
    }

    /**
     * 创建首帧耗时策略
     *
     * @param callback 结果回调，可为 null
     */
    public FirstFrameStrategy(@Nullable Callback callback) {
        mCallback = callback;
    }

    @NonNull
    @Override
    public String getName() {
        return TAG;
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        List<Class<? extends PlayerEvent>> events = new ArrayList<>();
        events.add(PlayerEvents.StateChanged.class);
        events.add(PlayerEvents.Prepared.class);
        events.add(PlayerEvents.FirstFrameRendered.class);
        return events;
    }

    @Override
    public void onEvent(@NonNull PlayerEvent event) {
        // 过滤非当前播放器事件，避免串台统计
        if (!isCurrentPlayer(event)) {
            return;
        }

        if (event instanceof PlayerEvents.StateChanged) {
            handleStateChanged((PlayerEvents.StateChanged) event);
        } else if (event instanceof PlayerEvents.Prepared) {
            handlePrepared((PlayerEvents.Prepared) event);
        } else if (event instanceof PlayerEvents.FirstFrameRendered) {
            handleFirstFrameRendered((PlayerEvents.FirstFrameRendered) event);
        }
    }

    @Override
    public void onReset() {
        super.onReset();

        // 重置计时状态
        resetTiming();
    }

    // ==================== Private Methods ====================

    /**
     * 处理状态变化事件
     */
    private void handleStateChanged(@NonNull PlayerEvents.StateChanged event) {
        PlayerState newState = event.newState;
        // 检测到开始准备状态，记录开始时间
        if (newState == PlayerState.PREPARING) {
            // 每次 prepare 都开启新一轮计时
            mStartTime = System.currentTimeMillis();
            mPreparedTime = 0;              // 防止沿用上一轮 prepared 时间
            mFirstFrameCompleted = false;   // 开启新一轮首帧统计
            LogHub.i(TAG, "Start timing at PREPARING state");
        } else if (newState == PlayerState.ERROR || newState == PlayerState.IDLE || newState == PlayerState.STOPPED) {
            // 可选：进入这些状态时清理
            resetTiming();
        }
    }

    /**
     * 处理准备完成事件
     */
    private void handlePrepared(@NonNull PlayerEvents.Prepared event) {
        if (mStartTime == 0) {
            // 如果没有记录开始时间，说明错过了 PREPARING 状态
            LogHub.w(TAG, "Prepared event received but no start time recorded");
            return;
        }

        mPreparedTime = System.currentTimeMillis();
        long prepareTime = mPreparedTime - mStartTime;
        LogHub.i(TAG, "Prepared, prepare time: " + prepareTime + "ms");
    }

    /**
     * 处理首帧渲染完成事件
     */
    private void handleFirstFrameRendered(@NonNull PlayerEvents.FirstFrameRendered event) {
        if (mFirstFrameCompleted) {
            // 避免重复回调
            return;
        }

        if (mStartTime == 0) {
            LogHub.w(TAG, "FirstFrameRendered event received but no start time recorded");
            return;
        }

        long firstFrameTime = System.currentTimeMillis();
        long prepareTime = mPreparedTime > 0 ? mPreparedTime - mStartTime : 0;
        long renderTime = mPreparedTime > 0 ? firstFrameTime - mPreparedTime : 0;
        long totalTime = firstFrameTime - mStartTime;

        mFirstFrameCompleted = true;

        // 日志输出
        Context context = AliPlayerKit.getContext();
        String message = context.getString(R.string.strategy_tip_first_frame_cost, totalTime, prepareTime, renderTime);
        LogHub.i(TAG, message);

        // Toast 显示（仅 Debug 模式）
        ToastUtils.showDebugToast(message);

        // 回调通知
        if (mCallback != null) {
            mCallback.onFirstFrameTime(prepareTime, renderTime, totalTime);
        }
    }

    /**
     * 重置计时状态
     */
    private void resetTiming() {
        mStartTime = 0;
        mPreparedTime = 0;
        mFirstFrameCompleted = false;
        LogHub.i(TAG, "Timing reset");
    }
}
