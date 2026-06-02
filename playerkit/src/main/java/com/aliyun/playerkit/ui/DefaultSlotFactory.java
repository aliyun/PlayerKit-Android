package com.aliyun.playerkit.ui;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.slot.SlotConfig;
import com.aliyun.playerkit.slot.SlotConstants;
import com.aliyun.playerkit.slot.SlotHostLayout;
import com.aliyun.playerkit.slot.SlotManager;
import com.aliyun.playerkit.slot.SlotType;
import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.data.PlayerViewType;
import com.aliyun.playerkit.ui.slots.CenterDisplaySlot;
import com.aliyun.playerkit.ui.slots.BottomBarSlot;
import com.aliyun.playerkit.ui.slots.CoverSlot;
import com.aliyun.playerkit.ui.slots.DisplayViewSlot;
import com.aliyun.playerkit.ui.slots.FullscreenSlot;
import com.aliyun.playerkit.ui.slots.GestureControlSlot;
import com.aliyun.playerkit.ui.slots.LogPanelSlot;
import com.aliyun.playerkit.ui.slots.PlayStateSlot;
import com.aliyun.playerkit.ui.slots.OptionPanelSlot;
import com.aliyun.playerkit.ui.slots.SettingMenuSlot;
import com.aliyun.playerkit.ui.slots.SurfaceViewSlot;
import com.aliyun.playerkit.ui.slots.TextureViewSlot;
import com.aliyun.playerkit.ui.slots.TopBarSlot;
import com.aliyun.playerkit.ui.slots.LandscapeHintSlot;

/**
 * 默认插槽工厂
 * <p>
 * 提供创建默认插槽注册表的便捷方法。
 * 同时注册默认的插槽构建器和插槽配置。
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public final class DefaultSlotFactory {

    // 私有构造函数，防止实例化
    private DefaultSlotFactory() {
        throw new UnsupportedOperationException("Cannot instantiate DefaultSlotFactory");
    }

    /**
     * 填充默认插槽构建器到 SlotManager
     * <p>
     * 注册所有默认插槽的构建器。
     * </p>
     *
     * @param slotManager 插槽管理器
     */
    public static void populateDefaults(@NonNull SlotManager slotManager) {
        registerDefaultBuilders(slotManager);
    }

    /**
     * 注册默认插槽构建器到指定管理器
     * <p>
     * 可以在自定义管理器的基础上添加默认构建器。
     * </p>
     *
     * @param slotManager 插槽管理器
     */
    public static void registerDefaultBuilders(@NonNull SlotManager slotManager) {
        // 注册播放 Surface 视图插槽（必需，根据全局配置选择对应的视图类型）
        registerPlayerSurfaceSlot(slotManager);
        // 注册全屏管理插槽（必需，请勿轻易修改，否则可能导致全屏功能异常）
        slotManager.register(SlotType.FULLSCREEN, parent -> new FullscreenSlot(parent.getContext()));
        // 注册手势控制插槽
        slotManager.register(SlotType.GESTURE_CONTROL, parent -> new GestureControlSlot(parent.getContext()));
        // 注册横屏观看提示插槽
        slotManager.register(SlotType.LANDSCAPE_HINT, parent -> new LandscapeHintSlot(parent.getContext()));
        // 注册封面图插槽
        slotManager.register(SlotType.COVER, parent -> new CoverSlot(parent.getContext()));
        // 注册中心显示插槽
        slotManager.register(SlotType.CENTER_DISPLAY, parent -> new CenterDisplaySlot(parent.getContext()));
        // 注册播放状态插槽
        slotManager.register(SlotType.PLAY_STATE, parent -> new PlayStateSlot(parent.getContext()));
        // 注册日志面板插槽
        slotManager.register(SlotType.LOG_PANEL, parent -> new LogPanelSlot(parent.getContext()));
        // 注册顶部控制栏插槽
        slotManager.register(SlotType.TOP_BAR, parent -> new TopBarSlot(parent.getContext()));
        // 注册底部控制栏插槽
        slotManager.register(SlotType.BOTTOM_BAR, parent -> new BottomBarSlot(parent.getContext()));
        // 注册选项面板插槽（横屏倍速/清晰度）
        slotManager.register(SlotType.OPTION_PANEL, parent -> new OptionPanelSlot(parent.getContext()));
        // 注册设置菜单插槽
        slotManager.register(SlotType.SETTING_MENU, parent -> new SettingMenuSlot(parent.getContext()));
    }

    /**
     * 注册播放 Surface 视图插槽
     * <p>
     * 根据全局配置选择对应的视图类型。
     * </p>
     *
     * @param slotManager 插槽管理器
     */
    private static void registerPlayerSurfaceSlot(@NonNull SlotManager slotManager) {
        PlayerViewType viewType = AliPlayerKit.getPlayerViewType();
        switch (viewType) {
            case DISPLAY_VIEW:
                // 推荐使用 DisplayViewSlot（AliDisplayView）
                slotManager.register(SlotType.PLAYER_SURFACE, parent -> new DisplayViewSlot(parent.getContext()));
                break;
            case SURFACE_VIEW:
                // 使用 SurfaceViewSlot
                slotManager.register(SlotType.PLAYER_SURFACE, parent -> new SurfaceViewSlot(parent.getContext()));
                break;
            case TEXTURE_VIEW:
                // 使用 TextureViewSlot
                slotManager.register(SlotType.PLAYER_SURFACE, parent -> new TextureViewSlot(parent.getContext()));
                break;
            default:
                // 应对未知的 PlayerViewType 值，使用默认的 DISPLAY_VIEW
                slotManager.register(SlotType.PLAYER_SURFACE, parent -> new DisplayViewSlot(parent.getContext()));
                break;
        }
    }

    /**
     * 注册默认插槽配置到指定宿主布局
     * <p>
     * 配置各插槽在不同场景下的可见性规则。
     * </p>
     *
     * @param hostLayout 宿主布局
     */
    public static void registerDefaultConfigs(@NonNull SlotHostLayout hostLayout) {
        for (SlotConfig config : SlotConstants.createDefaultConfigs()) {
            hostLayout.registerSlotConfig(config);
        }
    }
}
