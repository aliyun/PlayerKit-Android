package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.data.SceneType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AliPlayerKit 插槽常量定义
 * <p>
 * 定义所有插槽的默认配置和常量。
 * 集中管理插槽的可见性规则，便于维护和扩展。
 * </p>
 * <p>
 * AliPlayerKit Slot Constants Definition
 * <p>
 * Defines default configurations and constants for all slots.
 * Centralizes slot visibility rules for easy maintenance and extension.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public final class SlotConstants {

    // 私有构造函数，防止实例化
    private SlotConstants() {
        throw new UnsupportedOperationException("Cannot instantiate SlotConstants");
    }

    /**
     * 创建默认的插槽配置
     * <p>
     * 定义各插槽在不同场景下的默认可见性规则。
     * 返回的配置列表会被注册到 {@link SlotHostLayout} 中，用于控制插槽的显示。
     * </p>
     * <p>
     * Create default slot configurations
     * <p>
     * Defines default visibility rules for each slot in different scenarios.
     * The returned configuration list will be registered in {@link SlotHostLayout} to control slot display.
     * </p>
     *
     * @return 插槽配置列表，不会为 null
     */
    @NonNull
    public static List<SlotConfig> createDefaultConfigs() {
        List<SlotConfig> configs = new ArrayList<>();

        // 播放 Surface 视图：始终显示
        configs.add(new SlotConfig.Builder().type(SlotType.PLAYER_SURFACE).build());

        // 全屏管理插槽：始终显示（纯逻辑管理，不渲染 UI）
        configs.add(new SlotConfig.Builder().type(SlotType.FULLSCREEN).build());

        // 播放状态插槽：始终显示（错误提示、加载中等）
        configs.add(new SlotConfig.Builder().type(SlotType.PLAY_STATE).build());

        // 手势控制插槽：在 minimal 场景下不显示（禁用所有手势）
        configs.add(new SlotConfig.Builder()
                .type(SlotType.GESTURE_CONTROL)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .build());

        // 横屏观看提示插槽：在 minimal 场景下不显示
        configs.add(new SlotConfig.Builder()
                .type(SlotType.LANDSCAPE_HINT)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .build());

        // 封面图插槽：在 live、restricted、minimal 场景下不显示
        configs.add(new SlotConfig.Builder()
                .type(SlotType.COVER)
                .excludeScenes(createSet(SceneType.LIVE, SceneType.RESTRICTED, SceneType.MINIMAL))
                .build());

        // 中心显示插槽：在 minimal 场景下不显示
        configs.add(new SlotConfig.Builder()
                .type(SlotType.CENTER_DISPLAY)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .build());

        // 日志面板插槽：根据 AliPlayerKit 配置决定是否显示，minimal 场景下隐藏
        configs.add(new SlotConfig.Builder()
                .type(SlotType.LOG_PANEL)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .condition(AliPlayerKit::isLogPanelEnabled)
                .build());

        // 顶部控制栏插槽：在 minimal 场景下不显示
        configs.add(new SlotConfig.Builder()
                .type(SlotType.TOP_BAR)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .build());

        // 底部控制栏插槽：在 minimal 场景下不显示
        configs.add(new SlotConfig.Builder()
                .type(SlotType.BOTTOM_BAR)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .build());

        // 设置菜单插槽：在 minimal 场景下不显示
        configs.add(new SlotConfig.Builder()
                .type(SlotType.SETTING_MENU)
                .excludeScenes(createSet(SceneType.MINIMAL))
                .build());

        return configs;
    }

    /**
     * 创建场景类型集合的便捷方法
     * <p>
     * Helper method to create a set of scene types
     * </p>
     *
     * @param sceneTypes 场景类型数组
     * @return 场景类型集合
     */
    @NonNull
    private static Set<Integer> createSet(@SceneType int... sceneTypes) {
        Set<Integer> set = new HashSet<>();
        for (int sceneType : sceneTypes) {
            set.add(sceneType);
        }
        return set;
    }
}
