package com.aliyun.playerkit.slot;

import android.view.Surface;

import androidx.annotation.Nullable;

import com.aliyun.player.videoview.AliDisplayView;

/**
 * AliPlayerKit Surface 管理器接口
 * <p>
 * 统一管理播放器的显示视图和 Surface 设置，避免在 SlotHost 接口中分散多个方法。
 * 通过此接口，插槽可以统一地设置播放器的显示视图或 Surface。
 * </p>
 * <p>
 * <strong>使用场景</strong>：
 * <ul>
 *     <li>{@link #setDisplayView(AliDisplayView)} - 用于 DisplayViewSlot，设置 AliDisplayView</li>
 *     <li>{@link #setSurface(Surface)} - 用于 SurfaceViewSlot 和 TextureViewSlot，设置 Surface</li>
 *     <li>{@link #surfaceChanged()} - 用于 SurfaceViewSlot 和 TextureViewSlot，通知 Surface 变化</li>
 * </ul>
 * </p>
 * <p>
 * AliPlayerKit Surface Manager Interface
 * <p>
 * Unified management of player display view and Surface settings, avoiding multiple scattered methods in SlotHost interface.
 * Through this interface, slots can uniformly set the player's display view or Surface.
 * </p>
 *
 * @author keria
 * @date 2025/12/08
 */
public interface ISurfaceManager {

    /**
     * 设置播放器显示视图
     * <p>
     * 插槽可以通过此方法设置播放器的显示视图（AliDisplayView）。
     * 这是插槽向播放器提供视图的标准方式，避免了直接访问 Controller。
     * </p>
     * <p>
     * Set player display view
     * <p>
     * Slots can set the player's display view (AliDisplayView) through this method.
     * This is the standard way for slots to provide views to the player, avoiding direct access to Controller.
     * </p>
     *
     * @param displayView 播放器显示视图，可以为 null（表示清除显示视图）
     */
    void setDisplayView(@Nullable AliDisplayView displayView);

    /**
     * 设置播放器渲染 Surface
     * <p>
     * 插槽可以通过此方法设置播放器的渲染 Surface。
     * 通常用于 SurfaceView 或 TextureView 场景。
     * </p>
     * <p>
     * Set player rendering Surface
     * <p>
     * Slots can set the player's rendering Surface through this method.
     * Usually used for SurfaceView or TextureView scenarios.
     * </p>
     *
     * @param surface Surface 实例，可以为 null（表示清除 Surface）
     */
    void setSurface(@Nullable Surface surface);

    /**
     * 通知播放器 Surface 已变化
     * <p>
     * 当 Surface 尺寸或格式发生变化时，插槽应调用此方法通知播放器。
     * </p>
     * <p>
     * Notify player that Surface has changed
     * <p>
     * When the Surface size or format changes, the slot should call this method to notify the player.
     * </p>
     */
    void surfaceChanged();
}
