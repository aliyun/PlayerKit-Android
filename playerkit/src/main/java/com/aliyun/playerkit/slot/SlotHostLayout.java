package com.aliyun.playerkit.slot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.player.videoview.AliDisplayView;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.logging.LogHub;

import java.util.EnumMap;
import java.util.Map;

/**
 * AliPlayerKit 插槽宿主布局
 * <p>
 * 负责管理和布局所有插槽视图（Slot View）的核心容器。
 * 根据插槽注册表（{@link SlotRegistry}）和场景类型（{@link SceneType}）动态构建和显示插槽。
 * 实现了 {@link SlotHost} 接口，为插槽提供统一的宿主能力。
 * </p>
 * <p>
 * <strong>插槽生命周期管理</strong>：
 * <ul>
 *     <li>当插槽被添加时，调用 {@link ISlot#onAttach(SlotHost)}</li>
 *     <li>当插槽被移除时，调用 {@link ISlot#onDetach()}</li>
 * </ul>
 * </p>
 * <p>
 * <strong>插槽顺序</strong>：
 * 插槽按照 {@link SlotType} 枚举定义的顺序添加，确保层级关系正确。
 * 例如：播放 Surface 视图在最底层，控制层在上层，浮层在最上层。
 * </p>
 * <p>
 * AliPlayerKit Slot Host Layout
 * <p>
 * Core container responsible for managing and laying out all slot views.
 * Dynamically builds and displays slots based on slot registry ({@link SlotRegistry}) and scene type ({@link SceneType}).
 * Implements the {@link SlotHost} interface, providing unified host capabilities for slots.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public class SlotHostLayout extends FrameLayout implements SlotHost, ISurfaceManager, ISlotManager {

    private static final String TAG = "SlotHostLayout";

    /**
     * 插槽视图映射表
     * <p>
     * Key: 插槽类型，Value: 对应的视图实例。
     * </p>
     */
    @NonNull
    private final Map<SlotType, View> slotViews = new EnumMap<>(SlotType.class);

    /**
     * 当前场景类型
     * <p>
     * 用于决定哪些插槽应该显示。不同场景下会显示不同的插槽组合。
     * </p>
     */
    @SceneType
    private int sceneType = SceneType.VOD;

    /**
     * 插槽注册表，用于构建插槽视图
     * <p>
     * 在 {@link #bind(AliPlayerController, int, SlotRegistry)} 时设置。
     * </p>
     */
    @Nullable
    private SlotRegistry slotRegistry;

    /**
     * 绑定的播放控制器
     * <p>
     * 在 {@link #bind(AliPlayerController, int, SlotRegistry)} 时设置。
     * 通过 {@link SlotHost} 接口的 Surface 设置方法提供给插槽使用，而不是直接暴露。
     * </p>
     */
    @Nullable
    private AliPlayerController controller;

    /**
     * 当前播放数据配置
     * <p>
     * 在 {@link #bindData(AliPlayerModel)} 时设置。
     * 此缓存确保 Slot 在数据生命周期内可以稳定访问数据。
     * </p>
     */
    @Nullable
    private AliPlayerModel model;

    /**
     * 插槽可见性检查器
     * <p>
     * 根据场景类型和插槽配置判断插槽是否应该显示。
     * </p>
     */
    @NonNull
    private final SlotVisibilityChecker visibilityChecker = new SlotVisibilityChecker();

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * <p>
     * Constructor
     * </p>
     *
     * @param context 上下文，不能为 null
     */
    public SlotHostLayout(@NonNull Context context) {
        this(context, null);
    }

    /**
     * 构造函数
     * <p>
     * 初始化布局，设置允许子视图超出父视图边界（用于浮层等场景）。
     * </p>
     * <p>
     * Constructor
     * <p>
     * Initialize layout, set to allow child views to exceed parent view boundaries (for overlay scenarios).
     * </p>
     *
     * @param context 上下文，不能为 null
     * @param attrs   属性集，可以为 null
     */
    public SlotHostLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 允许子视图超出父视图边界（用于浮层等场景）
        setClipChildren(false);
        setClipToPadding(false);
    }

    // ==================== 公开方法 ====================

    /**
     * 绑定控制器和插槽注册表
     * <p>
     * 绑定后会自动重建所有插槽视图。
     * 这是使用插槽系统的第一步，必须在构建插槽之前调用。
     * </p>
     * <p>
     * Bind controller and slot registry
     * <p>
     * After binding, all slot views will be automatically rebuilt.
     * This is the first step in using the slot system and must be called before building slots.
     * </p>
     *
     * @param controller 播放控制器，不能为 null
     * @param sceneType  场景类型
     * @param registry   插槽注册表，不能为 null
     */
    public void bind(@NonNull AliPlayerController controller, @SceneType int sceneType, @NonNull SlotRegistry registry) {
        LogHub.i(TAG, "Bind controller, sceneType=" + sceneType);
        this.controller = controller;
        this.sceneType = sceneType;
        this.slotRegistry = registry;
        rebuildSlots();
    }

    /**
     * 解绑控制器和插槽注册表
     * <p>
     * 先调用所有插槽的 {@link ISlot#onDetach()}，然后清除所有插槽视图并释放资源。
     * 在组件销毁时调用，确保所有资源都被正确清理。
     * </p>
     * <p>
     * Unbind controller and slot registry
     * <p>
     * First calls {@link ISlot#onDetach()} for all slots, then clears all slot views and releases resources.
     * Called when the component is destroyed to ensure all resources are properly cleaned up.
     * </p>
     */
    public void unbind() {
        LogHub.i(TAG, "Unbind");

        // 先清理数据生命周期
        unbindData();

        // 再清理视图生命周期
        detachAllSlots();
        slotViews.clear();
        removeAllViews();

        controller = null;
        slotRegistry = null;
        visibilityChecker.clear();
    }

    /**
     * 获取指定类型的插槽视图
     * <p>
     * Get slot view of specified type
     *
     * @param slotType 插槽类型，不能为 null
     * @return 插槽视图，如果不存在则返回 null
     */
    @Override
    @Nullable
    public View getSlotView(@NonNull SlotType slotType) {
        return slotViews.get(slotType);
    }

    /**
     * 获取当前场景类型
     * <p>
     * Get current scene type
     * </p>
     *
     * @return 场景类型，默认为 {@link SceneType#VOD}
     */
    @SceneType
    public int getSceneType() {
        return sceneType;
    }

    /**
     * 更新场景类型并重建插槽
     * <p>
     * 当播放场景发生变化时（例如从 VOD 切换到 LIVE），调用此方法更新场景类型并重建插槽。
     * 采用增量更新策略，只更新变化的插槽。
     * </p>
     * <p>
     * Update scene type and rebuild slots
     * <p>
     * When the playback scene changes (e.g., switching from VOD to LIVE), call this method to update the scene type and rebuild slots.
     * Uses incremental update strategy to only update changed slots.
     * </p>
     *
     * @param sceneType 新的场景类型
     */
    @Override
    public void updateSceneType(@SceneType int sceneType) {
        if (this.sceneType == sceneType) {
            LogHub.i(TAG, "Scene type unchanged: " + sceneType);
            return;
        }
        LogHub.i(TAG, "Scene type changed: " + this.sceneType + " -> " + sceneType);

        // 使用增量更新策略
        updateSlotsIncremental(sceneType);
        this.sceneType = sceneType;
    }

    // ==================== ISlotManager 接口实现 ====================

    /**
     * 重建所有插槽
     * <p>
     * 根据当前场景类型和插槽配置，重新构建所有插槽视图。
     * 会先调用旧插槽的 {@link ISlot#onDetach()}，然后创建新插槽并调用 {@link ISlot#onAttach(SlotHost)}。
     * </p>
     * <p>
     * Rebuild all slots
     * <p>
     * Rebuild all slot views based on current scene type and slot configuration.
     * Will first call {@link ISlot#onDetach()} for old slots, then create new slots and call {@link ISlot#onAttach(SlotHost)}.
     * </p>
     */
    @Override
    public void rebuildSlots() {
        detachAllSlots();
        removeAllViews();
        slotViews.clear();

        if (slotRegistry == null) {
            LogHub.w(TAG, "SlotRegistry is null, cannot rebuild slots");
            return;
        }

        if (controller == null) {
            LogHub.w(TAG, "Controller is null, cannot rebuild slots");
            return;
        }

        LogHub.i(TAG, "Rebuild slots, sceneType=" + sceneType);

        // 按照 SlotType 定义的顺序构建插槽
        for (SlotType type : SlotType.values()) {
            // 检查该插槽是否应该在当前场景下显示
            if (visibilityChecker.shouldShow(type, sceneType)) {
                View slotView = slotRegistry.build(type, this);
                if (slotView != null) {
                    attachSlot(type, slotView);
                }
            } else {
                LogHub.i(TAG, "Slot skipped: " + type + " (not visible in scene " + sceneType + ")");
            }
        }

        LogHub.i(TAG, "Rebuild slots completed, total=" + slotViews.size());
    }

    /**
     * 增量更新插槽
     * <p>
     * 只更新可见性发生变化的插槽，避免不必要的销毁和重建。
     * </p>
     * <p>
     * Incrementally update slots
     * <p>
     * Only update slots whose visibility has changed, avoiding unnecessary destruction and reconstruction.
     * </p>
     *
     * @param newSceneType 新的场景类型
     */
    private void updateSlotsIncremental(@SceneType int newSceneType) {
        if (slotRegistry == null) {
            LogHub.w(TAG, "SlotRegistry is null, cannot update slots");
            return;
        }

        if (controller == null) {
            LogHub.w(TAG, "Controller is null, cannot update slots");
            return;
        }

        LogHub.i(TAG, "Incremental update slots, sceneType=" + sceneType + " -> " + newSceneType);

        // 按照 SlotType 定义的顺序处理插槽
        for (SlotType type : SlotType.values()) {
            boolean shouldShow = visibilityChecker.shouldShow(type, newSceneType);
            View existingView = slotViews.get(type);

            if (shouldShow) {
                // 应该显示
                if (existingView == null) {
                    // 之前未显示，现在需要显示 - 创建新插槽
                    View slotView = slotRegistry.build(type, this);
                    if (slotView != null) {
                        attachSlot(type, slotView);
                        LogHub.i(TAG, "Slot created: " + type);
                    }
                } else {
                    // 之前已显示，现在仍显示 - 保持不变
                    LogHub.i(TAG, "Slot unchanged: " + type);
                }
            } else {
                // 不应该显示
                if (existingView != null) {
                    // 之前显示，现在需要隐藏 - 移除插槽
                    detachSlot(type);
                    LogHub.i(TAG, "Slot removed: " + type);
                } else {
                    // 之前未显示，现在仍不显示 - 保持不变
                    LogHub.i(TAG, "Slot still hidden: " + type);
                }
            }
        }

        LogHub.i(TAG, "Incremental update completed, total=" + slotViews.size());
    }

    // ==================== SlotHost 接口实现 ====================

    /**
     * 获取宿主视图
     * <p>
     * 返回当前布局实例作为宿主视图，供插槽使用。
     * </p>
     *
     * @return 宿主视图，即当前布局实例，不会为 null
     */
    @NonNull
    @Override
    public View getHostView() {
        return this;
    }

    /**
     * 获取 Surface 管理器
     * <p>
     * 返回当前布局实例作为 Surface 管理器，供插槽设置 Surface 或 DisplayView。
     * </p>
     *
     * @return Surface 管理器，即当前布局实例，不会为 null
     */
    @NonNull
    @Override
    public ISurfaceManager getSurfaceManager() {
        return this;
    }

    /**
     * 获取播放器状态存储
     * <p>
     * 从绑定的控制器获取播放器状态存储，供插槽查询播放器状态。
     * </p>
     *
     * @return 播放器状态存储，不会为 null
     * @throws IllegalStateException 如果控制器未绑定（为 null）
     */
    @NonNull
    @Override
    public IPlayerStateStore getPlayerStateStore() {
        if (controller == null) {
            throw new IllegalStateException("Controller is null. Call bind() first.");
        }
        return controller.getStateStore();
    }

    /**
     * 获取播放器 ID
     * <p>
     * 从绑定的控制器获取播放器 ID，用于标识播放器实例。
     * </p>
     *
     * @return 播放器 ID，如果控制器未绑定或播放器未初始化则返回 null
     */
    @Nullable
    @Override
    public String getPlayerId() {
        if (controller != null && controller.getPlayer() != null) {
            return controller.getPlayer().getPlayerId();
        }
        return null;
    }

    /**
     * 获取当前播放数据配置
     * <p>
     * 返回缓存的播放数据，该数据在 {@link #bindData(AliPlayerModel)} 时设置。
     * </p>
     * <p>
     * Get current playback data configuration
     * <p>
     * Returns the cached playback data, which is set in {@link #bindData(AliPlayerModel)}.
     * </p>
     *
     * @return 播放数据配置，如果未配置则返回 null
     */
    @Nullable
    @Override
    public AliPlayerModel getModel() {
        return model;
    }

    // ==================== ISurfaceManager 接口实现 ====================

    /**
     * 设置播放器显示视图
     * <p>
     * 将 DisplayView 设置给播放器，用于显示视频画面。
     * 通常由 {@link com.aliyun.playerkit.ui.slots.DisplayViewSlot} 调用。
     * </p>
     *
     * @param displayView 播放器显示视图，可以为 null（表示清除显示视图）
     */
    @Override
    public void setDisplayView(@Nullable AliDisplayView displayView) {
        if (controller != null) {
            controller.setDisplayView(displayView);
        } else {
            LogHub.w(TAG, "Controller is null, cannot set display view");
        }
    }

    /**
     * 设置播放器 Surface
     * <p>
     * 将 Surface 设置给播放器，用于显示视频画面。
     * 通常由 {@link com.aliyun.playerkit.ui.slots.SurfaceViewSlot} 或
     * {@link com.aliyun.playerkit.ui.slots.TextureViewSlot} 调用。
     * </p>
     *
     * @param surface Surface 实例，可以为 null（表示清除 Surface）
     */
    @Override
    public void setSurface(@Nullable Surface surface) {
        if (controller != null) {
            controller.setSurface(surface);
        } else {
            LogHub.w(TAG, "Controller is null, cannot set surface");
        }
    }

    /**
     * 通知播放器 Surface 已变化
     * <p>
     * 当 Surface 的尺寸或格式发生变化时调用，通知播放器更新渲染。
     * 通常由 SurfaceView 或 TextureView 的 Surface 回调触发。
     * </p>
     */
    @Override
    public void surfaceChanged() {
        if (controller != null) {
            controller.surfaceChanged();
        } else {
            LogHub.w(TAG, "Controller is null, cannot notify surface changed");
        }
    }

    /**
     * 发送播放器事件
     * <p>
     * 将播放器事件发送到事件总线，供其他组件订阅和处理。
     * 通常由插槽调用，用于发送播放命令或状态变化事件。
     * </p>
     *
     * @param event 播放器事件，不能为 null
     */
    @Override
    public void postEvent(@NonNull PlayerEvent event) {
        PlayerEventBus.getInstance().post(event);
    }

    /**
     * 注册插槽配置
     * <p>
     * 用于配置插槽在不同场景下的可见性规则。
     * 可以通过 {@link SlotConfig} 定义插槽在哪些场景下显示，哪些场景下隐藏。
     * </p>
     * <p>
     * Register slot configuration
     * <p>
     * Used to configure visibility rules for slots in different scenarios.
     * Can define which scenarios the slot should be displayed in and which scenarios it should be hidden through {@link SlotConfig}.
     * </p>
     *
     * @param config 插槽配置，不能为 null
     */
    public void registerSlotConfig(@NonNull SlotConfig config) {
        visibilityChecker.registerConfig(config);
    }

    /**
     * 绑定播放数据
     * <p>
     * 将播放数据绑定到 SlotHostLayout，并通知所有 Slot 数据已配置。
     * 每个 Slot 的 {@link ISlot#onBindData(AliPlayerModel)} 方法会被调用。
     * </p>
     * <p>
     * Bind Playback Data
     * <p>
     * Binds playback model to SlotHostLayout and notifies all Slots that model is configured.
     * Each Slot's {@link ISlot#onBindData(AliPlayerModel)} method will be called.
     * </p>
     *
     * @param model 播放数据，不能为 null
     */
    @Override
    public void bindData(@NonNull AliPlayerModel model) {
        this.model = model;
        LogHub.i(TAG, "Bind model, notifying " + slotViews.size() + " slots");

        // 通知所有已附加的 Slot
        for (Map.Entry<SlotType, View> entry : slotViews.entrySet()) {
            View slotView = entry.getValue();
            if (slotView instanceof ISlot) {
                try {
                    ((ISlot) slotView).onBindData(model);
                    LogHub.i(TAG, "Data bound to slot: " + entry.getKey());
                } catch (Exception e) {
                    LogHub.e(TAG, "Error binding model to slot: " + entry.getKey(), e);
                }
            }
        }
    }

    /**
     * 解绑播放数据
     * <p>
     * 清除 SlotHostLayout 的数据，并通知所有 Slot 数据已清除。
     * 每个 Slot 的 {@link ISlot#onUnbindData()} 方法会被调用。
     * </p>
     * <p>
     * Unbind Playback Data
     * <p>
     * Clears the data in SlotHostLayout and notifies all Slots that data is cleared.
     * Each Slot's {@link ISlot#onUnbindData()} method will be called.
     * </p>
     */
    @Override
    public void unbindData() {
        if (model == null) {
            LogHub.w(TAG, "Data is already null, skip clearing");
            return;
        }

        LogHub.i(TAG, "Unbind data, notifying " + slotViews.size() + " slots");

        // 通知所有已附加的 Slot
        for (Map.Entry<SlotType, View> entry : slotViews.entrySet()) {
            View slotView = entry.getValue();
            if (slotView instanceof ISlot) {
                try {
                    ((ISlot) slotView).onUnbindData();
                    LogHub.i(TAG, "Data unbound from slot: " + entry.getKey());
                } catch (Exception e) {
                    LogHub.e(TAG, "Error unbinding data from slot: " + entry.getKey(), e);
                }
            }
        }

        this.model = null;
    }

    // ==================== 私有方法 ====================

    /**
     * 附加插槽到宿主
     * <p>
     * 将插槽视图添加到布局中，并调用其生命周期方法。
     * </p>
     * <p>
     * Attach slot to host
     * <p>
     * Add slot view to layout and call its lifecycle methods.
     * </p>
     *
     * @param slotType 插槽类型，不能为 null
     * @param slotView 插槽视图，不能为 null
     */
    private void attachSlot(@NonNull SlotType slotType, @NonNull View slotView) {
        slotViews.put(slotType, slotView);
        addView(slotView);

        // 如果插槽实现了 ISlot 接口，调用 onAttach
        if (slotView instanceof ISlot) {
            try {
                ((ISlot) slotView).onAttach(this);
                LogHub.i(TAG, "Slot attached: " + slotType);
            } catch (Exception e) {
                LogHub.e(TAG, "Error attaching slot: " + slotType, e);
            }
        }

        LogHub.i(TAG, "Slot added: " + slotType);
    }

    /**
     * 解绑所有插槽
     * <p>
     * 遍历所有插槽视图，调用其 onDetach 方法进行清理。
     * </p>
     * <p>
     * Detach all slots
     * <p>
     * Iterate through all slot views and call their onDetach methods for cleanup.
     * </p>
     */
    private void detachAllSlots() {
        for (View slotView : slotViews.values()) {
            if (slotView instanceof ISlot) {
                try {
                    ((ISlot) slotView).onDetach();
                } catch (Exception e) {
                    LogHub.e(TAG, "Error detaching slot: " + slotView.getClass().getSimpleName(), e);
                }
            }
        }
    }

    /**
     * 解绑指定插槽
     * <p>
     * 移除指定类型的插槽视图，并调用其 onDetach 方法进行清理。
     * </p>
     * <p>
     * Detach specified slot
     * <p>
     * Remove the slot view of the specified type and call its onDetach method for cleanup.
     * </p>
     *
     * @param slotType 插槽类型，不能为 null
     */
    private void detachSlot(@NonNull SlotType slotType) {
        View slotView = slotViews.remove(slotType);
        if (slotView != null) {
            if (slotView instanceof ISlot) {
                try {
                    ((ISlot) slotView).onDetach();
                } catch (Exception e) {
                    LogHub.e(TAG, "Error detaching slot: " + slotType, e);
                }
            }
            removeView(slotView);
            LogHub.i(TAG, "Slot detached: " + slotType);
        }
    }
}
