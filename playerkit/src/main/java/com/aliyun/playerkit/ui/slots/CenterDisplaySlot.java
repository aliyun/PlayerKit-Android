package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.event.GestureEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.locale.PlayerLocale;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.ui.custom.UniversalSlider;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 中心显示插槽
 * <p>
 * 用于在屏幕中心显示倍速、亮度和音量等信息。
 * 响应手势事件进行显示和隐藏，提供用户友好的交互反馈。
 * </p>
 * <p>
 * Center Display Slot
 * <p>
 * Used to display information such as playback speed, brightness, and volume in the center of the screen.
 * Responds to gesture events to show and hide, providing user-friendly interactive feedback.
 * </p>
 *
 * @author keria
 * @date 2025/12/23
 */
public class CenterDisplaySlot extends BaseSlot {

    // ==================== UI 组件 ====================

    /**
     * 倍速显示文本视图
     * <p>
     * 显示当前播放倍速状态，在长按时显示。
     * </p>
     */
    private TextView mTvSpeed;

    /**
     * 亮度控制容器布局
     * <p>
     * 包含亮度进度条和百分比文本，在左侧垂直拖动时显示。
     * </p>
     */
    private UniversalSlider mBrightnessSlider;

    /**
     * 音量控制容器布局
     * <p>
     * 包含音量进度条和百分比文本，在右侧垂直拖动时显示。
     * </p>
     */
    private UniversalSlider mVolumeSlider;

    // ==================== 数据字段 ====================

    /**
     * 当前绑定的播放器 ID
     * <p>
     * 用于过滤事件，确保只处理当前播放器的事件。
     * </p>
     */
    @Nullable
    private String mPlayerId;

    // ==================== 构造函数 ====================

    public CenterDisplaySlot(@NonNull Context context) {
        super(context);
    }

    public CenterDisplaySlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CenterDisplaySlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ==================== 生命周期方法 ====================

    @Override
    protected int getLayoutId() {
        return R.layout.layout_center_display_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 初始化 UI 组件
        mTvSpeed = findViewById(R.id.tv_speed);
        mBrightnessSlider = findViewById(R.id.slider_brightness);
        mBrightnessSlider.setSliderType(UniversalSlider.SliderType.BRIGHTNESS);

        mVolumeSlider = findViewById(R.id.slider_volume);
        mVolumeSlider.setSliderType(UniversalSlider.SliderType.VOLUME);
    }

    @Override
    public void onDetach() {
        // 隐藏所有显示内容
        hideAllDisplays();

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

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Arrays.asList(
                GestureEvents.LongPressEvent.class,
                GestureEvents.LongPressEndEvent.class,
                GestureEvents.LeftVerticalDragUpdateEvent.class,
                GestureEvents.LeftVerticalDragEndEvent.class,
                GestureEvents.RightVerticalDragUpdateEvent.class,
                GestureEvents.RightVerticalDragEndEvent.class
        );
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        super.onEvent(event);
        if (event instanceof GestureEvents.LongPressEvent) {
            onLongPress((GestureEvents.LongPressEvent) event);
        } else if (event instanceof GestureEvents.LongPressEndEvent) {
            onLongPressEnd((GestureEvents.LongPressEndEvent) event);
        } else if (event instanceof GestureEvents.LeftVerticalDragUpdateEvent) {
            onLeftDragUpdate((GestureEvents.LeftVerticalDragUpdateEvent) event);
        } else if (event instanceof GestureEvents.LeftVerticalDragEndEvent) {
            onLeftDragEnd((GestureEvents.LeftVerticalDragEndEvent) event);
        } else if (event instanceof GestureEvents.RightVerticalDragUpdateEvent) {
            onRightDragUpdate((GestureEvents.RightVerticalDragUpdateEvent) event);
        } else if (event instanceof GestureEvents.RightVerticalDragEndEvent) {
            onRightDragEnd((GestureEvents.RightVerticalDragEndEvent) event);
        }
    }

    // ==================== 手势事件处理方法 ====================

    /**
     * 处理长按事件（倍速显示）
     * <p>
     * 当用户长按屏幕时，显示倍速提示信息。
     * </p>
     *
     * @param event 长按事件，不能为 null
     */
    private void onLongPress(@NonNull GestureEvents.LongPressEvent event) {
        if (shouldIgnore(event.playerId)) {
            return;
        }
        // 如果当前没有显示倍速，则不处理
        if (!isElementVisible(SlotElements.CenterDisplay.SPEED)) {
            return;
        }
        mTvSpeed.setVisibility(View.VISIBLE);
        mTvSpeed.setText(PlayerLocale.get(R.string.player_playback_speed_active));
    }

    /**
     * 处理长按结束事件（隐藏倍速显示）
     * <p>
     * 当用户结束长按时，隐藏倍速提示信息。
     * </p>
     *
     * @param event 长按结束事件，不能为 null
     */
    private void onLongPressEnd(@NonNull GestureEvents.LongPressEndEvent event) {
        if (shouldIgnore(event.playerId)) {
            return;
        }
        mTvSpeed.setVisibility(View.GONE);
    }

    /**
     * 处理左侧垂直拖动更新事件（亮度调节）
     * <p>
     * 当用户在屏幕左侧垂直拖动时，显示亮度调节界面并更新进度。
     * </p>
     *
     * @param event 左侧垂直拖动更新事件，不能为 null
     */
    private void onLeftDragUpdate(@NonNull GestureEvents.LeftVerticalDragUpdateEvent event) {
        if (shouldIgnore(event.playerId)) {
            return;
        }
        // 如果当前没有显示亮度，则不处理
        if (!isElementVisible(SlotElements.CenterDisplay.BRIGHTNESS)) {
            return;
        }
        mBrightnessSlider.setVisibility(View.VISIBLE);

        int progress = (int) (event.currentPercent * 100);

        mBrightnessSlider.updateSliderValue(progress);
    }

    /**
     * 处理左侧垂直拖动结束事件（隐藏亮度显示）
     * <p>
     * 当用户结束左侧垂直拖动时，隐藏亮度调节界面。
     * </p>
     *
     * @param event 左侧垂直拖动结束事件，不能为 null
     */
    private void onLeftDragEnd(@NonNull GestureEvents.LeftVerticalDragEndEvent event) {
        if (shouldIgnore(event.playerId)) {
            return;
        }
        mBrightnessSlider.setVisibility(View.GONE);
    }

    /**
     * 处理右侧垂直拖动更新事件（音量调节）
     * <p>
     * 当用户在屏幕右侧垂直拖动时，显示音量调节界面并更新进度。
     * </p>
     *
     * @param event 右侧垂直拖动更新事件，不能为 null
     */
    private void onRightDragUpdate(@NonNull GestureEvents.RightVerticalDragUpdateEvent event) {
        if (shouldIgnore(event.playerId)) {
            return;
        }
        // 如果当前没有显示音量，则不处理
        if (!isElementVisible(SlotElements.CenterDisplay.VOLUME)) {
            return;
        }
        mVolumeSlider.setVisibility(View.VISIBLE);

        int progress = (int) (event.currentPercent * 100);
        mVolumeSlider.updateSliderValue(progress);
    }

    /**
     * 处理右侧垂直拖动结束事件（隐藏音量显示）
     * <p>
     * 当用户结束右侧垂直拖动时，隐藏音量调节界面。
     * </p>
     *
     * @param event 右侧垂直拖动结束事件，不能为 null
     */
    private void onRightDragEnd(@NonNull GestureEvents.RightVerticalDragEndEvent event) {
        if (shouldIgnore(event.playerId)) {
            return;
        }
        mVolumeSlider.setVisibility(View.GONE);
    }

    // ==================== 工具方法 ====================

    /**
     * 判断是否应该忽略该事件
     * <p>
     * 如果事件来自其他播放器实例，则忽略该事件，避免多播放器场景下的干扰。
     * </p>
     *
     * @param eventPlayerId 事件中的播放器 ID
     * @return true 表示应该忽略，false 表示应该处理
     */
    private boolean shouldIgnore(@Nullable String eventPlayerId) {
        return mPlayerId == null || StringUtil.notEquals(mPlayerId, eventPlayerId);
    }

    /**
     * 隐藏所有显示内容
     * <p>
     * 在与宿主分离时调用，确保所有 UI 元素都被隐藏。
     * </p>
     */
    private void hideAllDisplays() {
        mTvSpeed.setVisibility(View.GONE);
        mBrightnessSlider.setVisibility(View.GONE);
        mVolumeSlider.setVisibility(View.GONE);
    }
}
