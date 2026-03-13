package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Surface;

/**
 * AliPlayerKit Surface 提供者接口
 * <p>
 * 定义了提供播放器显示视图的统一契约。
 * 支持三种视图类型：AliDisplayView、SurfaceView 和 TextureView。
 * </p>
 * <p>
 * AliPlayerKit Surface Provider Interface
 * <p>
 * Defines the standard contract for providing player Surface.
 * Used to support SurfaceView and TextureView view types.
 * </p>
 *
 * @author keria
 * @date 2025/11/23
 */
public interface ISurfaceProvider {

    /**
     * 设置 Surface 提供者到播放器
     * <p>
     * 在插槽 attach 时自动调用，用于初始化 Surface。
     * 插槽应在此方法中设置视图或Surface回调监听器。
     * </p>
     * <p>
     * Setup Surface provider to player
     * <p>
     * Automatically called when slot is attached, used to initialize the Surface provider.
     * Slot should set up the view or Surface callback listeners in this method.
     * </p>
     *
     * @param host 插槽宿主，可能为 null（如果宿主未初始化）
     */
    void setupSurfaceProvider(@Nullable SlotHost host);

    /**
     * Surface 可用时调用
     * <p>
     * 当 Surface 创建完成并可用时，插槽应该调用此方法。
     * 框架会自动将 Surface 设置给播放器实例。
     * </p>
     * <p>
     * Surface Available
     * <p>
     * Called when Surface is created and available.
     * Framework will automatically set the Surface to the player instance.
     * </p>
     *
     * @param surface Surface 实例，不能为 null
     * @param host    插槽宿主，不能为 null
     */
    default void onSurfaceAvailable(@NonNull Surface surface, @NonNull SlotHost host) {
        host.getSurfaceManager().setSurface(surface);
    }

    /**
     * Surface 尺寸变化时调用
     * <p>
     * 当 Surface 尺寸发生变化时，插槽应该调用此方法。
     * 框架会自动通知播放器 Surface 已变化。
     * </p>
     * <p>
     * Surface Changed
     * <p>
     * Called when Surface size changes.
     * Framework will automatically notify the player that Surface has changed.
     * </p>
     *
     * @param host 插槽宿主，不能为 null
     */
    default void onSurfaceChanged(@NonNull SlotHost host) {
        host.getSurfaceManager().surfaceChanged();
    }

    /**
     * Surface 销毁时调用
     * <p>
     * 当 Surface 被销毁时，插槽应该调用此方法。
     * 框架会自动清理播放器的 Surface。
     * </p>
     * <p>
     * Surface Destroyed
     * <p>
     * Called when Surface is destroyed.
     * Framework will automatically clean up the player's Surface.
     * </p>
     *
     * @param host 插槽宿主，不能为 null
     */
    default void onSurfaceDestroyed(@NonNull SlotHost host) {
        host.getSurfaceManager().setSurface(null);
    }
}
