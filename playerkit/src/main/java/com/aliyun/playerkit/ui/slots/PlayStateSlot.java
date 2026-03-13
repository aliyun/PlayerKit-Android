package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 播放状态插槽
 * <p>
 * 用于显示播放器状态信息，主要处理错误状态的显示。
 * 当播放器发生错误时，会在界面上显示错误码和错误描述信息，帮助用户了解问题。
 * 当播放器状态从错误状态恢复时，会自动隐藏错误信息。
 * </p>
 * <p>
 * Play State Slot
 * <p>
 * Used to display player status information, mainly handling the display of error states.
 * When a player error occurs, it displays the error code and error description on the interface to help users understand the problem.
 * When the player state recovers from an error state, the error information will be automatically hidden.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public class PlayStateSlot extends BaseSlot {

    // ==================== UI 组件 ====================

    /**
     * 消息显示文本视图
     * <p>
     * 用于显示错误信息，包括错误码和错误描述。
     * </p>
     */
    private TextView mTvMessage;

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

        // 初始化 UI 组件
        mTvMessage = findViewByIdCompat(R.id.tv_message);
    }

    // ==================== 事件处理 ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Arrays.asList(PlayerEvents.Error.class, PlayerEvents.StateChanged.class);
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof PlayerEvents.Error) {
            onError((PlayerEvents.Error) event);
        } else if (event instanceof PlayerEvents.StateChanged) {
            onStateChanged((PlayerEvents.StateChanged) event);
        }
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理播放器错误事件
     * <p>
     * 当播放器发生错误时调用，构建错误信息并显示在界面上。
     * </p>
     *
     * @param event 错误事件，不能为 null
     */
    private void onError(@NonNull PlayerEvents.Error event) {
        String message = buildErrorMessage(event.errorCode, event.errorMsg);
        mTvMessage.setText(message);
        mTvMessage.setVisibility(View.VISIBLE);
        show();
    }

    /**
     * 处理播放器状态变化事件
     * <p>
     * 当播放器状态发生变化时调用。如果状态从错误状态恢复为其他状态，
     * 则隐藏错误信息，避免错误提示一直显示。
     * </p>
     *
     * @param event 状态变化事件，不能为 null
     */
    private void onStateChanged(@NonNull PlayerEvents.StateChanged event) {
        // 如果状态不再是 Error，隐藏错误信息
        if (event.newState != PlayerState.ERROR) {
            mTvMessage.setVisibility(View.GONE);
            gone();
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 构建错误信息文本
     * <p>
     * 将错误码和错误描述组合成格式化的错误信息字符串。
     * 如果错误描述为空，则使用默认的错误提示信息。
     * </p>
     * <p>
     * Build error message text
     * <p>
     * Combines error code and error description into a formatted error message string.
     * If the error description is empty, a default error prompt message is used.
     * </p>
     *
     * @param errorCode 错误码
     * @param errorMsg  错误描述，可能为 null 或空字符串
     * @return 格式化后的错误信息字符串
     */
    @NonNull
    private String buildErrorMessage(int errorCode, @Nullable String errorMsg) {
        String codeLabel = getContext().getString(R.string.play_state_error_code_label);
        String messageLabel = getContext().getString(R.string.play_state_error_message_label);
        String actualMsg = StringUtil.isEmpty(errorMsg) ? getContext().getString(R.string.play_state_error_message_default) : errorMsg;
        return getContext().getString(R.string.play_state_error_details_format, codeLabel, errorCode, messageLabel, actualMsg);
    }
}
