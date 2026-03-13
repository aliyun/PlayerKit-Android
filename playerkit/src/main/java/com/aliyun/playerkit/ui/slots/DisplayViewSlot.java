package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.player.videoview.AliDisplayView;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.ISurfaceProvider;
import com.aliyun.playerkit.slot.SlotHost;

/**
 * 播放器显示视图插槽（推荐）
 * <p>
 * 负责创建和管理播放器视频显示视图（AliDisplayView）。
 * 此插槽是播放器视图的承载者，Controller 从此插槽获取视图并设置给播放器实例。
 * </p>
 * <p>
 * <strong>推荐使用</strong>：AliDisplayView 是阿里云播放器官方提供的显示视图组件，
 * 具有更好的兼容性和性能优化，建议优先使用此插槽。
 * </p>
 * <p>
 * Player Display View Slot (Recommended)
 * <p>
 * Responsible for creating and managing the player video display view (AliDisplayView).
 * This slot is the carrier of the player view, and the Controller gets the view from this slot and sets it to the player instance.
 * </p>
 * <p>
 * <strong>Recommended</strong>: AliDisplayView is the official display view component provided by Alibaba Cloud Player,
 * with better compatibility and performance optimization. It is recommended to use this slot first.
 * </p>
 *
 * @author keria
 * @date 2025/11/23
 */
public class DisplayViewSlot extends BaseSlot implements ISurfaceProvider {

    public DisplayViewSlot(@NonNull Context context) {
        super(context);
    }

    public DisplayViewSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DisplayViewSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 获取布局资源ID
     * <p>
     * BaseSlot 会自动调用此方法并加载对应的 XML 布局。
     * </p>
     *
     * @return 布局资源ID
     */
    @Override
    protected int getLayoutId() {
        return R.layout.layout_display_view_slot;
    }

    /**
     * 设置 Surface 提供者到播放器
     * <p>
     * 对于 AliDisplayView，直接通过 SlotHost 设置给播放器。
     * </p>
     *
     * @param host 插槽宿主，可能为 null
     */
    @Override
    public void setupSurfaceProvider(@Nullable SlotHost host) {
        if (host == null) {
            return;
        }
        AliDisplayView displayView = findViewByIdCompat(R.id.display_view);
        host.getSurfaceManager().setDisplayView(displayView);
    }
}
