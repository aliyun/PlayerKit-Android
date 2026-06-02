package com.aliyun.playerkit.ui.custom;

// Copyright © 2026 Alibaba Cloud. All rights reserved.
//
// Author: wyq
// Date: 2026/5/18
// Brief: 通用滑块控件，用于音量和亮度调节

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
 * 包含图标、SeekBar 和数值显示。
 */
public class UniversalSlider extends LinearLayout {

    private SeekBar mPbControl;
    private ImageView mIvControl;
    private TextView mTvControl;
    private SliderType mSliderType = SliderType.VOLUME;

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


    private void init(Context context) {
        View.inflate(context, R.layout.layout_universal_slider_view, this);
        mPbControl = findViewById(R.id.pb_control);
        mIvControl = findViewById(R.id.iv_control);
        mTvControl = findViewById(R.id.tv_control);
    }


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
     * 值会被 clamp 到 [0, 100]。
     */
    public void updateSliderValue(int value) {
        int clampedValue = Math.min(100, Math.max(0, value));

        if (null != mPbControl) {
            mPbControl.setProgress(clampedValue);
        }
        updateDisplayText(clampedValue);
    }

    public int getSliderValue() {
        return null != mPbControl ? mPbControl.getProgress() : 0;
    }

    private void updateDisplayText(int value) {
        boolean isVolume = (mSliderType == SliderType.VOLUME);
        int resId = isVolume ? R.string.player_volume_format : R.string.player_brightness_format;

        if (null != mTvControl) {
            mTvControl.setText(PlayerLocale.get(resId, value));
        }
    }

    public enum SliderType {
        VOLUME, BRIGHTNESS
    }
}
