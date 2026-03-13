package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;

/**
 * 控制栏插槽基类
 * <p>
 * 提供控制栏插槽的通用功能，包括显示/隐藏动画和自动隐藏机制。
 * 通过 {@link ControlBarEvents} 实现多个控制栏插槽之间的同步显示和隐藏。
 * </p>
 * <p>
 * Control Bar Slot Base Class
 * <p>
 * Provides common functionality for control bar slots, including show/hide animations and auto-hide mechanism.
 * Synchronizes show/hide state across multiple control bar slots through {@link ControlBarEvents}.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public abstract class BaseControlBarSlot extends BaseSlot {

    // ==================== 常量 ====================

    /**
     * 自动隐藏延迟时间（毫秒）
     * <p>
     * 控制栏在无交互后自动隐藏的延迟时间。
     * </p>
     */
    protected static final long AUTO_HIDE_DELAY_MS = 3000;

    /**
     * 动画持续时间（毫秒）
     */
    protected static final long ANIMATION_DURATION_MS = 300;

    // ==================== 成员变量 ====================

    /**
     * 主线程 Handler
     * <p>
     * 用于在主线程中执行自动隐藏任务。
     * </p>
     */
    protected final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 自动隐藏任务
     * <p>
     * 延迟执行隐藏控制栏的操作。
     * </p>
     */
    protected final Runnable mAutoHideRunnable = this::postHideEvent;

    /**
     * 当前绑定的播放器 ID
     * <p>
     * 用于标识事件来源，确保只处理当前播放器的事件。
     * </p>
     */
    @Nullable
    protected String mPlayerId;

    // ==================== 构造函数 ====================

    public BaseControlBarSlot(@NonNull Context context) {
        super(context);
    }

    // ==================== 生命周期方法 ====================

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 初始状态为隐藏
        setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        // 取消自动隐藏任务，避免内存泄漏
        cancelAutoHide();

        super.onDetach();
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);

        mPlayerId = getPlayerId();
    }

    @Override
    public void onUnbindData() {
        mPlayerId = null;

        super.onUnbindData();
    }

    // ==================== 事件处理 ====================

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof ControlBarEvents.Show) {
            showWithAnimation();
        } else if (event instanceof ControlBarEvents.Hide) {
            hideWithAnimation();
        } else if (event instanceof ControlBarEvents.ResetTimer) {
            // 如果当前可见，重置自动隐藏计时器
            if (isShow()) {
                scheduleAutoHide();
            }
        }
    }

    // ==================== 交互管理 ====================

    /**
     * 通知发生了交互
     * <p>
     * 子类在处理点击、拖动等交互时调用此方法，以重置自动隐藏计时器。
     * 确保用户交互时控制栏保持显示状态。
     * </p>
     * <p>
     * Notify that an interaction occurred
     * <p>
     * Called by subclasses when handling interactions such as clicks and drags to reset the auto-hide timer.
     * Ensures the control bar remains visible during user interaction.
     * </p>
     */
    protected void notifyInteraction() {
        postEvent(new ControlBarEvents.ResetTimer(mPlayerId));
    }

    // ==================== 显示/隐藏控制 ====================

    /**
     * 发送隐藏事件
     * <p>
     * 通过事件总线发送隐藏事件，实现多插槽同步隐藏。
     * </p>
     */
    protected void postHideEvent() {
        postEvent(new ControlBarEvents.Hide(mPlayerId));
    }

    /**
     * 取消自动隐藏任务
     * <p>
     * 移除待执行的自动隐藏任务，通常在手动显示或隐藏时调用。
     * </p>
     */
    protected void cancelAutoHide() {
        mHandler.removeCallbacks(mAutoHideRunnable);
    }

    /**
     * 调度自动隐藏任务
     * <p>
     * 取消之前的自动隐藏任务，并重新调度一个新的延迟隐藏任务。
     * 在显示控制栏或重置计时器时调用。
     * </p>
     */
    protected void scheduleAutoHide() {
        cancelAutoHide();
        mHandler.postDelayed(mAutoHideRunnable, AUTO_HIDE_DELAY_MS);
    }

    /**
     * 显示控制栏（带动画）
     * <p>
     * 使用淡入动画显示控制栏，并启动自动隐藏计时器。
     * 如果控制栏已经可见，则只重置自动隐藏计时器。
     * </p>
     * <p>
     * Show control bar (with animation)
     * <p>
     * Shows the control bar with a fade-in animation and starts the auto-hide timer.
     * If the control bar is already visible, only resets the auto-hide timer.
     * </p>
     */
    protected void showWithAnimation() {
        if (isShow()) {
            scheduleAutoHide();
            return;
        }

        setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(ANIMATION_DURATION_MS);
        startAnimation(anim);
        scheduleAutoHide();
    }

    /**
     * 隐藏控制栏（带动画）
     * <p>
     * 使用淡出动画隐藏控制栏，并在动画结束后设置视图为不可见。
     * 同时取消自动隐藏任务。
     * </p>
     * <p>
     * Hide control bar (with animation)
     * <p>
     * Hides the control bar with a fade-out animation and sets the view to invisible after the animation ends.
     * Also cancels the auto-hide task.
     * </p>
     */
    protected void hideWithAnimation() {
        if (!isShow()) {
            return;
        }

        AlphaAnimation anim = new AlphaAnimation(1f, 0f);
        anim.setDuration(ANIMATION_DURATION_MS);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 动画开始，无需处理
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 动画重复，无需处理
            }
        });
        startAnimation(anim);
        cancelAutoHide();
    }
}
