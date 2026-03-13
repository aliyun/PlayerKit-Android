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
import com.aliyun.playerkit.strategy.StrategyContext;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡顿检测策略
 * <p>
 * 监听播放器的 Loading 状态，统计卡顿次数和时长，并在满足条件时触发回调或提示。
 * </p>
 * <p>
 * Stutter Detect Strategy
 * <p>
 * Monitors player loading/buffering events to collect stutter count and duration,
 * and reports analysis results via callbacks or debug toasts.
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class StutterDetectStrategy extends BaseStrategy {

    private static final String TAG = "StutterDetectStrategy";

    // Note keria: 暂且不设置卡顿阈值，哪怕 1ms 也会认为卡顿
    private static final int DEFAULT_THRESHOLD_MS = 0;

    // 卡顿阈值
    private final int mStutterThresholdMs;

    // 回调接口
    @Nullable
    private final Callback mCallback;

    // 卡顿开始时间（首帧后才记录）
    private long mLoadingStartTime = 0;
    // 卡顿次数
    private int mStutterCount = 0;
    // 卡顿总时长
    private long mTotalStutterDuration = 0;
    // 会话开始时间
    private long mSessionStartTime = 0;
    // 是否已渲染首帧
    private boolean mFirstFrameRendered = false;

    // 有效播放时长
    // Note keria：仅统计用户真正“在观看”的时间；无论是主动暂停（用户行为），还是被动暂停（卡顿 Loading / Buffering）都不算。
    private long mValidPlayDuration = 0;

    // ====== 为实现“PLAYING && !LOADING 才计有效播放”新增的最小状态 ======
    private boolean mIsPlaying = false;
    private boolean mIsLoading = false;
    private long mEffectivePlayStartTime = 0; // 当前有效播放段起点（满足 PLAYING && !LOADING 时）

    /**
     * 卡顿回调接口
     */
    public interface Callback {
        /**
         * 发生卡顿（单次）
         *
         * @param durationMs 本次卡顿耗时（毫秒）
         */
        void onStutter(long durationMs);

        /**
         * 播放会话结束统计
         *
         * @param stayDurationMs         停留时长（从进入会话到结束的总时长，包含暂停）
         * @param validPlayDurationMs    有效播放时长
         * @param totalStutterDurationMs 卡顿总时长
         * @param stutterCount           卡顿总次数
         */
        void onSessionAnalysis(long stayDurationMs, long validPlayDurationMs, long totalStutterDurationMs, int stutterCount);
    }

    /**
     * 创建卡顿检测策略（默认配置）
     */
    public StutterDetectStrategy() {
        this(DEFAULT_THRESHOLD_MS, null);
    }

    /**
     * 创建卡顿检测策略
     *
     * @param callback 回调接口
     */
    public StutterDetectStrategy(@Nullable Callback callback) {
        this(DEFAULT_THRESHOLD_MS, callback);
    }

    /**
     * 创建卡顿检测策略
     *
     * @param thresholdMs 卡顿阈值（毫秒），超过该时长才视为卡顿
     * @param callback    回调接口
     */
    public StutterDetectStrategy(int thresholdMs, @Nullable Callback callback) {
        mStutterThresholdMs = thresholdMs;
        mCallback = callback;
    }

    @NonNull
    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void onStart(@NonNull StrategyContext context) {
        super.onStart(context);
        resetState();
    }

    @Override
    public void onReset() {
        super.onReset();
        resetState();
    }

    private void resetState() {
        mStutterCount = 0;
        mLoadingStartTime = 0;
        mTotalStutterDuration = 0;
        mSessionStartTime = 0;
        mFirstFrameRendered = false;

        mValidPlayDuration = 0;

        mIsPlaying = false;
        mIsLoading = false;
        mEffectivePlayStartTime = 0;
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        List<Class<? extends PlayerEvent>> events = new ArrayList<>();
        events.add(PlayerEvents.LoadingBegin.class);
        events.add(PlayerEvents.LoadingEnd.class);
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

        if (event instanceof PlayerEvents.FirstFrameRendered) {
            mFirstFrameRendered = true;
            if (mSessionStartTime == 0) {
                mSessionStartTime = System.currentTimeMillis();
            }
            // 首帧后，如果当前已经满足有效播放条件，则开始累计
            maybeStartEffectivePlay(System.currentTimeMillis());
        } else if (event instanceof PlayerEvents.LoadingBegin) {
            // 仅在首帧渲染后才开始统计算卡顿（首帧前的 Loading 算首帧耗时）
            if (mFirstFrameRendered) {
                long now = System.currentTimeMillis();

                // 记录 loading 起点（防重复 begin 覆盖）
                if (mLoadingStartTime == 0) {
                    mLoadingStartTime = now;
                    LogHub.w(TAG, "Loading started (Stutter begin)");
                }

                // 进入 loading：有效播放段需要停止
                mIsLoading = true;
                stopEffectivePlay(now);
            }
        } else if (event instanceof PlayerEvents.LoadingEnd) {
            if (mFirstFrameRendered) {
                long now = System.currentTimeMillis();
                mIsLoading = false;

                handleLoadingEnd(now);

                // loading 结束：如果满足条件，恢复有效播放累计
                maybeStartEffectivePlay(now);
            }
        } else if (event instanceof PlayerEvents.Prepared) {
            // 准备完成，视为会话起点，重置统计信息
            resetState();
            mSessionStartTime = System.currentTimeMillis();
        } else if (event instanceof PlayerEvents.StateChanged) {
            PlayerEvents.StateChanged stateEvent = (PlayerEvents.StateChanged) event;
            handleStateChanged(stateEvent.newState);
        }
    }

    private void handleStateChanged(@NonNull PlayerState newState) {
        long now = System.currentTimeMillis();

        // 先根据状态变化停止/开始有效播放段
        boolean willBePlaying = (newState == PlayerState.PLAYING);

        if (mIsPlaying && !willBePlaying) {
            // 离开 PLAYING（暂停/停止/完成/错误等）：停止累计
            stopEffectivePlay(now);
        }

        mIsPlaying = willBePlaying;

        if (mIsPlaying) {
            // 进入 PLAYING：如果满足条件（首帧后且非 loading）则开始累计
            maybeStartEffectivePlay(now);
        }

        switch (newState) {
            case STOPPED:
            case ERROR:
            case IDLE:
            case COMPLETED:
                reportSessionAnalysis();
                resetState();
                break;
            default:
                break;
        }
    }

    private void handleLoadingEnd(long now) {
        if (mLoadingStartTime == 0) {
            return;
        }

        long duration = now - mLoadingStartTime;
        mLoadingStartTime = 0;

        LogHub.w(TAG, "Loading ended, duration: " + duration + "ms");

        if (duration >= mStutterThresholdMs) {
            mStutterCount++;
            mTotalStutterDuration += duration;

            // 卡顿提示
            Context context = AliPlayerKit.getContext();
            try {
                String message = context.getString(R.string.strategy_tip_stutter_detected, duration);
                LogHub.i(TAG, message);

                // Toast 提示 (Debug Only)
                ToastUtils.showDebugToast(message);
            } catch (Throwable e) {
                // Fallback log if resource missing
                LogHub.w(TAG, "Stutter detected: " + duration + "ms");
            }

            // 单次卡顿回调
            if (mCallback != null) {
                mCallback.onStutter(duration);
            }
        }
    }

    private void reportSessionAnalysis() {
        if (mSessionStartTime == 0) {
            return;
        }

        long now = System.currentTimeMillis();

        // 会话结束前，先停止有效播放段（如果还在累计）
        stopEffectivePlay(now);

        // 如果正在 Loading 中退出，把这段算进卡顿（并遵守阈值）
        if (mLoadingStartTime > 0) {
            long duration = now - mLoadingStartTime;
            mLoadingStartTime = 0;

            if (duration >= mStutterThresholdMs) {
                mTotalStutterDuration += duration;
                mStutterCount++;
                LogHub.i(TAG, "Session ended during loading, added stutter duration: " + duration + "ms");
            }
        }

        long stayDuration = now - mSessionStartTime;

        // 统计会话（这里的 mValidPlayDuration 已经是不含暂停、不含卡顿的有效播放时长）
        Context context = AliPlayerKit.getContext();
        String message = context.getString(R.string.strategy_tip_stutter_session_analysis, stayDuration, mValidPlayDuration, mTotalStutterDuration, mStutterCount);
        LogHub.i(TAG, message);

        // Toast 提示 (Debug Only)
        ToastUtils.showDebugToast(message);

        if (mCallback != null && stayDuration > 0) {
            mCallback.onSessionAnalysis(stayDuration, mValidPlayDuration, mTotalStutterDuration, mStutterCount);
        }
    }

    // ====================== 有效播放段累计（核心） ======================

    private void maybeStartEffectivePlay(long now) {
        // Note keria：只有在：首帧后 + PLAYING + 非 LOADING，才开始累计。
        if (mFirstFrameRendered && mIsPlaying && !mIsLoading) {
            if (mEffectivePlayStartTime == 0) {
                mEffectivePlayStartTime = now;
            }
        }
    }

    private void stopEffectivePlay(long now) {
        if (mEffectivePlayStartTime > 0) {
            long delta = now - mEffectivePlayStartTime;
            if (delta > 0) {
                mValidPlayDuration += delta;
            }
            mEffectivePlayStartTime = 0;
        }
    }
}
