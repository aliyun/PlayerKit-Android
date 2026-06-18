package com.aliyun.playerkit.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.locale.PlayerLocale;

/**
 * 通用滑块控件，用于视频播放器中的音量和亮度调节。
 * <p>
 * 包含图标（{@link ImageView}）、进度条（{@link SeekBar}）和数值文本（{@link TextView}）三部分。
 * 通过 {@link #setSliderType(SliderType)} 切换音量/亮度模式，图标和文本格式会自动适配。
 * </p>
 *
 * <p>
 * Universal slider control for volume and brightness adjustment in the video player.
 * <p>
 * Consists of an icon ({@link ImageView}), a progress bar ({@link SeekBar}),
 * and a value label ({@link TextView}).
 * Use {@link #setSliderType(SliderType)} to switch between volume/brightness mode;
 * the icon and text format adapt automatically.
 * </p>
 *
 * @author wyq
 */
public class UniversalSlider extends LinearLayout {

    // -- Views --

    /** 滑块进度条 / Slider progress bar */
    private SeekBar mPbControl;

    /** 滑块类型图标（音量/亮度） / Slider type icon (volume/brightness) */
    private ImageView mIvControl;

    /** 数值文本显示 / Value text label */
    private TextView mTvControl;

    // -- State --

    /** 当前滑块类型，默认为音量 / Current slider type, defaults to volume */
    private SliderType mSliderType = SliderType.VOLUME;

    // ==================== 构造方法 / Constructors ====================

    public UniversalSlider(Context context) {
        super(context);
        init(context);
    }

    public UniversalSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UniversalSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // ==================== 初始化 / Initialization ====================

    /**
     * 加载布局并绑定子控件引用。
     * <p>
     * Inflate layout and bind child view references.
     * </p>
     */
    private void init(Context context) {
        View.inflate(context, R.layout.layout_universal_slider_view, this);
        mPbControl = findViewById(R.id.pb_control);
        mIvControl = findViewById(R.id.iv_control);
        mTvControl = findViewById(R.id.tv_control);
    }

    // ==================== 公开方法 / Public API ====================

    /**
     * 设置滑块类型，并更新图标为对应的音量或亮度图标。
     * <p>
     * Set the slider type and update the icon to the corresponding volume or brightness icon.
     * </p>
     *
     * @param type 滑块类型，为 {@code null} 时忽略 / Slider type; ignored if {@code null}
     */
    public void setSliderType(SliderType type) {
        if (type == null) return;
        mSliderType = type;
        boolean isVolume = (type == SliderType.VOLUME);
        if (null != mIvControl) {
            mIvControl.setImageResource(isVolume ? R.drawable.ic_volume : R.drawable.ic_brightness);
        }
    }

    /**
     * 程序化更新滑块值并同步文本显示。
     * <p>
     * 值会被 clamp 到 [0, 100]。
     * </p>
     * <p>
     * Programmatically update the slider value and sync the text display.
     * The value is clamped to [0, 100].
     * </p>
     *
     * @param value 目标进度值（超出范围时自动截取） / Target progress value (clamped if out of range)
     */
    public void updateSliderValue(int value) {
        int clampedValue = Math.min(100, Math.max(0, value));

        if (null != mPbControl) {
            mPbControl.setProgress(clampedValue);
        }
        updateDisplayText(clampedValue);
    }

    /**
     * 获取当前滑块进度值。
     * <p>
     * Get the current slider progress value.
     * </p>
     *
     * @return 当前进度值，控件为空时返回 0 / Current progress value; returns 0 if the control is null
     */
    public int getSliderValue() {
        return null != mPbControl ? mPbControl.getProgress() : 0;
    }

    // ==================== 内部方法 / Internal ====================

    /**
     * 根据当前滑块类型更新数值文本。
     * <p>
     * Update the value text based on the current slider type.
     * </p>
     *
     * @param value 当前进度值 / Current progress value
     */
    private void updateDisplayText(int value) {
        boolean isVolume = (mSliderType == SliderType.VOLUME);
        int resId = isVolume ? R.string.player_volume_format : R.string.player_brightness_format;

        if (null != mTvControl) {
            mTvControl.setText(PlayerLocale.get(resId, value));
        }
    }

    // ==================== 枚举 / Enum ====================

    /**
     * 滑块类型枚举。
     * <p>
     * Slider type enumeration.
     * </p>
     */
    public enum SliderType {
        /** 音量 / Volume */
        VOLUME,
        /** 亮度 / Brightness */
        BRIGHTNESS
    }
}
