package com.aliyun.playerkit.examples.slotsystem;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.CustomSlotType;

/**
 * 自定义水印插槽（演示层级控制）
 * <p>
 * 一个简单的水印插槽，在播放器右上角显示 "WATERMARK" 文本。
 * 通过 {@link CustomSlotType} 定义 order=25，使其自动位于
 * GESTURE_CONTROL(20) 和 LANDSCAPE_HINT(30) 之间的层级。
 * </p>
 * <p>
 * Custom Watermark Slot (demonstrating layer control)
 * <p>
 * A simple watermark slot that displays "WATERMARK" text in the top-right corner of the player.
 * Defined with order=25 via {@link CustomSlotType}, automatically placed between
 * GESTURE_CONTROL(20) and LANDSCAPE_HINT(30) layers.
 * </p>
 *
 * @author keria
 * @date 2026/05/11
 */
public class WatermarkSlot extends BaseSlot {

    public WatermarkSlot(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_watermark_slot;
    }
}
