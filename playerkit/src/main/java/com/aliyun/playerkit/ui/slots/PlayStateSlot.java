package com.aliyun.playerkit.ui.slots;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.locale.PlayerLocale;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.TrackUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 播放状态插槽
 * <p>
 * 用于显示播放器状态信息，包括错误、加载、截图、清晰度切换等状态的显示。
 * 通过 {@link #mDisplayType} 状态追踪机制避免不同类型状态之间的误杀问题。
 * </p>
 * <p>
 * 状态分类：
 * <ul>
 *     <li><b>持续性状态</b>（Error、Loading）：由事件对驱动显示/隐藏，无定时器</li>
 *     <li><b>瞬时反馈</b>（Screenshot、TrackSwitch）：Toast 式展示，定时器自动消失</li>
 * </ul>
 * </p>
 * <p>
 * Play State Slot
 * <p>
 * Used to display player status information, including error, loading, snapshot,
 * and track switch states. Uses {@link #mDisplayType} tracking to prevent unintended
 * dismissal between different state types.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public class PlayStateSlot extends BaseSlot {

    // ==================== 常量 ====================

    /**
     * Toast 类型状态的自动隐藏延迟（毫秒）
     */
    private static final long TOAST_DISMISS_DELAY = 2500L;

    /**
     * 淡入淡出动画时长（毫秒）
     */
    private static final long ANIM_DURATION = 200L;

    /**
     * Loading 图标旋转一圈的时长（毫秒）
     */
    private static final long ROTATE_DURATION = 1000L;

    /**
     * 无显示内容
     */
    private static final int DISPLAY_NONE = 0;

    /**
     * 当前显示错误信息（持续性，由 StateChanged 事件驱动隐藏）
     */
    private static final int DISPLAY_ERROR = 1;

    /**
     * 当前显示加载指示（持续性，由 LoadingEnd 事件驱动隐藏）
     */
    private static final int DISPLAY_LOADING = 2;

    /**
     * 当前显示瞬时提示（Toast 类型，由定时器自动隐藏）
     */
    private static final int DISPLAY_TOAST = 3;

    /**
     * 本插槽需要订阅的事件类型列表
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            PlayerEvents.Error.class,
            PlayerEvents.StateChanged.class,
            PlayerEvents.SnapshotCompleted.class,
            PlayerEvents.LoadingBegin.class,
            PlayerEvents.LoadingEnd.class,
            PlayerEvents.TrackSwitchCompleted.class,
            PlayerEvents.TrackSwitchFailed.class
    );

    // ==================== UI 组件 ====================

    /**
     * 消息显示文本视图
     * <p>
     * 用于显示状态信息文本（错误详情、加载提示、截图结果等）。
     * </p>
     */
    private TextView mTvMessage;

    /**
     * 状态图标
     * <p>
     * 用于显示当前状态对应的图标（错误、成功、加载等）。
     * </p>
     */
    private ImageView mIvMessageType;

    // ==================== 状态管理 ====================

    /**
     * 主线程 Handler
     * <p>
     * 用于调度 Toast 类型状态的自动隐藏任务。
     * </p>
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Loading 图标旋转动画
     */
    private ObjectAnimator mRotateAnimator;

    /**
     * 当前显示类型
     * <p>
     * 用于追踪当前 slot 正在显示的内容类型，
     * 避免不同状态之间的 hide 逻辑互相干扰。
     * 例如：onStateChanged 只在当前显示 Error 时才执行隐藏，
     * 不会误杀正在显示的 Toast 或 Loading。
     * </p>
     */
    private int mDisplayType = DISPLAY_NONE;

    /**
     * Toast 自动隐藏任务
     * <p>
     * 仅在当前显示类型为 {@link #DISPLAY_TOAST} 时执行隐藏，
     * 防止定时器触发时状态已被其他事件覆盖（如 Loading 覆盖了 Toast）。
     * </p>
     */
    private final Runnable mDismissRunnable = () -> {
        if (mDisplayType == DISPLAY_TOAST) {
            dismissDisplay();
        }
    };

    // ==================== 构造函数 ====================

    public PlayStateSlot(@NonNull Context context) {
        super(context);
    }

    public PlayStateSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayStateSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ==================== 生命周期方法 ====================

    @Override
    protected int getLayoutId() {
        return R.layout.layout_play_state_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        mTvMessage = findViewByIdCompat(R.id.tv_message);
        mIvMessageType = findViewByIdCompat(R.id.iv_message_type);

        gone();
    }

    @Override
    public void onDetach() {
        animate().cancel();
        stopRotateAnimation();
        mHandler.removeCallbacks(mDismissRunnable);
        mDisplayType = DISPLAY_NONE;
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post(() -> onEvent(event));
            return;
        }
        if (getHost() == null) {
            return;
        }
        if (event instanceof PlayerEvents.Error) {
            onError((PlayerEvents.Error) event);
        } else if (event instanceof PlayerEvents.StateChanged) {
            onStateChanged((PlayerEvents.StateChanged) event);
        } else if (event instanceof PlayerEvents.SnapshotCompleted) {
            onSnapshotCompleted((PlayerEvents.SnapshotCompleted) event);
        } else if (event instanceof PlayerEvents.LoadingBegin) {
            onLoadingBegin();
        } else if (event instanceof PlayerEvents.LoadingEnd) {
            onLoadingEnd();
        } else if (event instanceof PlayerEvents.TrackSwitchCompleted) {
            onTrackSwitchCompleted((PlayerEvents.TrackSwitchCompleted) event);
        } else if (event instanceof PlayerEvents.TrackSwitchFailed) {
            onTrackSwitchFailed((PlayerEvents.TrackSwitchFailed) event);
        }
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理播放器错误事件
     * <p>
     * 当播放器发生错误时调用，构建错误信息并显示在界面上。
     * 错误信息会持续显示，直到播放器状态从 ERROR 恢复。
     * </p>
     *
     * @param event 错误事件，不能为 null
     */
    private void onError(@NonNull PlayerEvents.Error event) {
        if (!isElementVisible(SlotElements.PlayState.ERROR_MESSAGE)) {
            return;
        }
        mHandler.removeCallbacks(mDismissRunnable);
        stopRotateAnimation();
        mIvMessageType.setImageResource(R.drawable.ic_error);
        mTvMessage.setText(buildErrorMessage(event.errorCode, event.errorMsg));
        mDisplayType = DISPLAY_ERROR;
        animateIn();
    }

    /**
     * 处理播放器状态变化事件
     * <p>
     * 当播放器状态从 ERROR 恢复为其他状态时，隐藏错误信息。
     * 仅在当前显示类型为 {@link #DISPLAY_ERROR} 时生效，
     * 不会误杀 Toast 或 Loading 的显示。
     * </p>
     *
     * @param event 状态变化事件，不能为 null
     */
    private void onStateChanged(@NonNull PlayerEvents.StateChanged event) {
        if (event.newState != PlayerState.ERROR && mDisplayType == DISPLAY_ERROR) {
            dismissDisplay();
        }
    }

    /**
     * 处理截图完成事件
     * <p>
     * 截图成功时显示提示信息，{@link #TOAST_DISMISS_DELAY} 后自动隐藏。
     * 截图失败时不显示（静默处理）。
     * </p>
     *
     * @param event 截图完成事件，不能为 null
     */
    private void onSnapshotCompleted(@NonNull PlayerEvents.SnapshotCompleted event) {
        if (!event.result) {
            return;
        }
        showToast(R.drawable.ic_success, PlayerLocale.get(R.string.play_state_snapshot_completed));
    }

    /**
     * 处理清晰度切换成功事件
     * <p>
     * 显示已切换的清晰度名称，{@link #TOAST_DISMISS_DELAY} 后自动隐藏。
     * </p>
     *
     * @param event 清晰度切换成功事件，不能为 null
     */
    private void onTrackSwitchCompleted(@NonNull PlayerEvents.TrackSwitchCompleted event) {
        String qualityName = TrackUtil.findNearestResolution(event.quality);
        showToast(R.drawable.ic_success, PlayerLocale.get(R.string.play_state_track_switch_completed, qualityName));
    }

    /**
     * 处理清晰度切换失败事件
     * <p>
     * 显示切换失败提示，{@link #TOAST_DISMISS_DELAY} 后自动隐藏。
     * Debug 模式下额外显示错误码和错误信息，便于开发排查。
     * </p>
     *
     * @param event 清晰度切换失败事件，不能为 null
     */
    private void onTrackSwitchFailed(@NonNull PlayerEvents.TrackSwitchFailed event) {
        String message;
        if (AliPlayerKit.isDebugModeEnabled() && event.errorInfo != null) {
            String codeLabel = PlayerLocale.get(R.string.play_state_track_switch_failed_code_label);
            String msgLabel = PlayerLocale.get(R.string.play_state_track_switch_failed_msg_label);
            String msg = event.errorInfo.getMsg() != null ? event.errorInfo.getMsg() : "";
            message = PlayerLocale.get(R.string.play_state_track_switch_failed_detail,
                    codeLabel, event.errorInfo.getCode(), msgLabel, msg);
        } else {
            message = PlayerLocale.get(R.string.play_state_track_switch_failed);
        }
        showToast(R.drawable.ic_error, message);
    }

    /**
     * 处理开始加载事件
     * <p>
     * 显示加载指示，持续显示直到收到 {@link PlayerEvents.LoadingEnd} 事件。
     * </p>
     */
    private void onLoadingBegin() {
        if (mDisplayType == DISPLAY_ERROR) {
            return;
        }
        if (!isElementVisible(SlotElements.PlayState.LOAD_MESSAGE)) {
            return;
        }
        mHandler.removeCallbacks(mDismissRunnable);
        mIvMessageType.setImageResource(R.drawable.ic_loading);
        mTvMessage.setText(PlayerLocale.get(R.string.play_state_loading));
        mDisplayType = DISPLAY_LOADING;
        startRotateAnimation();
        animateIn();
    }

    /**
     * 处理加载结束事件
     * <p>
     * 仅在当前显示类型为 {@link #DISPLAY_LOADING} 时隐藏，
     * 不会误杀 Error 或 Toast 的显示。
     * </p>
     */
    private void onLoadingEnd() {
        if (mDisplayType == DISPLAY_LOADING) {
            dismissDisplay();
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 显示 Toast 类型的瞬时提示
     * <p>
     * 统一的 Toast 展示入口，{@link #TOAST_DISMISS_DELAY} 后自动隐藏。
     * </p>
     *
     * @param iconRes 图标资源 ID
     * @param message 提示文本
     */
    private void showToast(int iconRes, @NonNull String message) {
        mHandler.removeCallbacks(mDismissRunnable);
        stopRotateAnimation();
        mIvMessageType.setImageResource(iconRes);
        mTvMessage.setText(message);
        mDisplayType = DISPLAY_TOAST;
        animateIn();
        mHandler.postDelayed(mDismissRunnable, TOAST_DISMISS_DELAY);
    }

    /**
     * 隐藏当前显示并重置状态
     * <p>
     * 统一的隐藏入口，清理定时器、重置 {@link #mDisplayType} 并播放淡出动画。
     * </p>
     */
    private void dismissDisplay() {
        mHandler.removeCallbacks(mDismissRunnable);
        stopRotateAnimation();
        mDisplayType = DISPLAY_NONE;
        animateOut();
    }

    /**
     * 淡入动画显示
     * <p>
     * 取消正在进行的动画，设置为可见后从透明渐变到不透明。
     * 如果已经可见则跳过，避免重复动画。
     * </p>
     */
    private void animateIn() {
        animate().cancel();
        if (getVisibility() == VISIBLE && getAlpha() >= 1f) {
            return;
        }
        setAlpha(0f);
        show();
        animate().alpha(1f).setDuration(ANIM_DURATION).setListener(null).start();
    }

    /**
     * 淡出动画隐藏
     * <p>
     * 从不透明渐变到透明，动画结束后设置为 GONE。
     * </p>
     */
    private void animateOut() {
        animate().cancel();
        animate().alpha(0f).setDuration(ANIM_DURATION).withEndAction(this::gone).start();
    }

    /**
     * 启动 Loading 图标旋转动画
     */
    private void startRotateAnimation() {
        if (mRotateAnimator == null) {
            mRotateAnimator = ObjectAnimator.ofFloat(mIvMessageType, "rotation", 0f, 360f);
            mRotateAnimator.setDuration(ROTATE_DURATION);
            mRotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mRotateAnimator.setInterpolator(new LinearInterpolator());
        }
        mRotateAnimator.start();
    }

    /**
     * 停止 Loading 图标旋转动画
     */
    private void stopRotateAnimation() {
        if (mRotateAnimator != null) {
            mRotateAnimator.cancel();
            mIvMessageType.setRotation(0f);
        }
    }

    /**
     * 构建错误信息文本
     * <p>
     * 将错误码和错误描述组合成格式化的错误信息字符串。
     * 如果错误描述为空，则使用默认的错误提示信息。
     * </p>
     *
     * @param errorCode 错误码
     * @param errorMsg  错误描述，可能为 null 或空字符串
     * @return 格式化后的错误信息字符串
     */
    @NonNull
    private String buildErrorMessage(int errorCode, @Nullable String errorMsg) {
        String codeLabel = PlayerLocale.get(R.string.play_state_error_code_label);
        String messageLabel = PlayerLocale.get(R.string.play_state_error_message_label);
        String actualMsg = StringUtil.isEmpty(errorMsg) ? PlayerLocale.get(R.string.play_state_error_message_default) : errorMsg;
        return PlayerLocale.get(R.string.play_state_error_details_format, codeLabel, errorCode, messageLabel, actualMsg);
    }
}
