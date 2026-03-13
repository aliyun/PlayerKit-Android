package com.aliyun.playerkit.strategy.strategies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
 * 流量保护策略
 * <p>
 * 监听网络状态变化。当从 WiFi 切换到移动网络时，如果正在播放，则自动暂停并提示用户。
 * </p>
 * <p>
 * 使用场景：适用于需要保护用户移动流量的场景，避免用户因意外切换网络而产生高额流量费用。
 * </p>
 * <p>
 * Traffic Protection Strategy
 * <p>
 * Listens to network changes and protects users from unexpected mobile data usage
 * by detecting Wi‑Fi → mobile switches during playback and notifying via callbacks or UI hints.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public class TrafficProtectionStrategy extends BaseStrategy {

    private static final String TAG = "TrafficProtectionStrategy";

    // 回调接口
    @Nullable
    private final TrafficProtectionStrategy.Callback mCallback;

    /**
     * 应用上下文（使用 ApplicationContext 避免内存泄漏）
     */
    private final Context mContext;

    /**
     * 当前播放状态，用于判断是否需要触发流量保护
     */
    private PlayerState mCurrentState = PlayerState.IDLE;

    /**
     * 是否已提示“当前使用移动网络播放”（防止重复提示）
     */
    private boolean mMobileHintShown = false;

    /**
     * 是否已注册广播监听器
     */
    private boolean mIsRegistered = false;

    /**
     * 网络状态变化监听器
     * <p>
     * 监听系统网络连接状态变化广播。
     * </p>
     */
    private final BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkStatus();
        }
    };

    /**
     * 流量保护策略回调接口
     */
    public interface Callback {
        /**
         * 当播放时切换到移动网络时调用。
         */
        void onMobileNetworkPlaying();
    }

    /**
     * 创建流量保护策略实例
     */
    public TrafficProtectionStrategy() {
        this(null);
    }

    /**
     * 创建流量保护策略实例
     *
     * @param callback 流量保护策略回调接口
     */
    public TrafficProtectionStrategy(@Nullable Callback callback) {
        mCallback = callback;

        mContext = AliPlayerKit.getContext();
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
        events.add(PlayerEvents.StateChanged.class);  // 监听播放状态，用于判断是否需要触发保护
        return events;
    }

    @Override
    public void onEvent(@NonNull PlayerEvent event) {
        if (!isCurrentPlayer(event)) return;

        if (event instanceof PlayerEvents.StateChanged) {
            // 更新当前播放状态
            mCurrentState = ((PlayerEvents.StateChanged) event).newState;

            // 新增：当进入播放状态且处于移动网络时，提示一次“正在使用流量播放”
            if (mCurrentState == PlayerState.PLAYING) {
                maybeShowMobileHint();
            }
        }
    }

    @Override
    public void onStart(@NonNull StrategyContext context) {
        super.onStart(context);
        // 策略启动时注册网络状态监听器
        registerReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        // 策略停止时注销网络状态监听器，避免内存泄漏
        unregisterReceiver();

        // 停止时重置提示状态（下次启动可再次提示）
        mMobileHintShown = false;
    }

    /**
     * 注册网络状态监听器
     * <p>
     * 注册 BroadcastReceiver 以监听网络连接状态变化。
     * </p>
     */
    private void registerReceiver() {
        // 检查是否已注册
        if (mIsRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetworkReceiver, filter);
        mIsRegistered = true;
    }

    /**
     * 注销网络状态监听器
     * <p>
     * 安全地注销 BroadcastReceiver，避免重复注销导致的异常。
     * </p>
     */
    private void unregisterReceiver() {
        // 检查是否已注册
        if (!mIsRegistered) {
            return;
        }
        try {
            mContext.unregisterReceiver(mNetworkReceiver);
        } catch (Exception e) {
            // 忽略异常：可能已经注销或未注册
            LogHub.w(TAG, "Failed to unregister network receiver", e);
        } finally {
            mIsRegistered = false;
        }
    }

    /**
     * 检查网络状态
     * <p>
     * 当网络状态变化时调用。如果当前正在播放且切换到移动网络，则暂停播放。
     * </p>
     */
    private void checkNetworkStatus() {
        // 检查是否切换到移动网络
        if (isMobileNetwork(mContext)) {
            LogHub.i(TAG, "Switched to mobile network, pausing playback for traffic protection.");

            // 新增：提示一次“当前使用流量播放”
            maybeShowMobileHint();

            // Note keria: 此处可根据实际业务需求，选择仅提示、自动暂停或引导用户确认。
            // 如需暂停播放，请发送 PlayerCommand.Pause 事件实现自动暂停。

            // 回调通知
            if (mCallback != null) {
                mCallback.onMobileNetworkPlaying();
            }
        } else {
            // 回到 WiFi：重置提示标记，下次进入移动网络可再次提示
            mMobileHintShown = false;
        }
    }

    /**
     * 在移动网络播放时提示一次（防重复）
     */
    private void maybeShowMobileHint() {
        if (mMobileHintShown) {
            return;
        }
        // 仅在播放状态且处于移动网络时提示
        if (mCurrentState != PlayerState.PLAYING) {
            return;
        }
        if (!isMobileNetwork(mContext)) {
            return;
        }

        mMobileHintShown = true;
        ToastUtils.showToast(mContext.getString(R.string.strategy_tip_mobile_network_playing));
    }

    /**
     * 判断当前是否使用移动网络
     *
     * @param context 上下文
     * @return 如果当前使用移动网络返回 true，否则返回 false
     */
    private boolean isMobileNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }
}
