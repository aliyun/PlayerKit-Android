package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.data.SceneType;

import java.util.EnumMap;
import java.util.Map;

/**
 * 插槽可见性检查器
 * <p>
 * 负责根据场景类型和插槽配置判断插槽是否应该显示。
 * 集中管理插槽可见性检查逻辑，确保规则一致性和可维护性。
 * </p>
 * <p>
 * Slot Visibility Checker
 * <p>
 * Responsible for determining whether a slot should be displayed based on scene type and slot configuration.
 * Centralizes slot visibility check logic to ensure rule consistency and maintainability.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public class SlotVisibilityChecker {

    /**
     * 插槽配置映射表
     * <p>
     * Key: 插槽类型，Value: 对应的配置。
     * </p>
     */
    @NonNull
    private final Map<SlotType, SlotConfig> configs = new EnumMap<>(SlotType.class);

    /**
     * 检查插槽是否应该在指定场景下显示
     * <p>
     * Check if slot should be displayed in the specified scenario
     * </p>
     *
     * @param slotType  插槽类型
     * @param sceneType 场景类型
     * @return true 如果应该显示，false 否则
     */
    public boolean shouldShow(@NonNull SlotType slotType, @SceneType int sceneType) {
        // 播放 Surface 视图始终显示
        if (slotType == SlotType.PLAYER_SURFACE) {
            return true;
        }

        // MINIMAL 场景下只显示播放 Surface 视图
        if (sceneType == SceneType.MINIMAL) {
            return false;
        }

        // 检查插槽配置
        SlotConfig config = configs.get(slotType);
        if (config != null) {
            // 检查是否在排除场景中
            if (config.isExcluded(sceneType)) {
                return false;
            }
            // 检查额外条件
            if (!config.extraConditionPass()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 注册插槽配置
     * <p>
     * Register slot configuration
     * </p>
     *
     * @param config 插槽配置
     */
    public void registerConfig(@NonNull SlotConfig config) {
        configs.put(config.getType(), config);
    }

    /**
     * 清空所有配置
     * <p>
     * Clear all configurations
     * </p>
     */
    public void clear() {
        configs.clear();
    }
}
