package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.SceneType;

import java.util.HashSet;
import java.util.Set;

/**
 * AliPlayerKit 插槽配置
 * <p>
 * 控制插槽在不同场景下的是否显示。
 * </p>
 * <p>
 * AliPlayerKit Slot Configuration
 * <p>
 * Controls whether the slot is displayed in different scenarios
 * Flexibly controls slot display through excluded scenes and additional conditions.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public class SlotConfig {

    // 插槽类型
    @NonNull
    private final SlotType type;

    // 排除的场景
    private final Set<Integer> excludedScenes;

    // 额外显示条件（可选）
    @Nullable
    private final VisibilityCondition condition;

    /**
     * 构造函数
     * <p>
     * 创建插槽配置实例。
     * </p>
     * <p>
     * Constructor
     * <p>
     * Create a slot configuration instance.
     * </p>
     *
     * @param builder 构建器实例，不能为 null
     */
    private SlotConfig(Builder builder) {
        this.type = builder.type;
        this.excludedScenes = builder.excludedScenes == null ? new HashSet<>() : new HashSet<>(builder.excludedScenes);
        this.condition = builder.condition;
    }

    /**
     * 获取插槽类型
     * <p>
     * Get slot type
     * </p>
     *
     * @return 插槽类型
     */
    @NonNull
    public SlotType getType() {
        return type;
    }

    /**
     * 检查指定场景是否被排除
     * <p>
     * 如果场景在排除列表中，返回 true，表示该插槽不应在此场景下显示。
     * </p>
     * <p>
     * Check if the specified scene is excluded
     * <p>
     * If the scene is in the exclusion list, returns true, indicating that the slot should not be displayed in this scenario.
     * </p>
     *
     * @param sceneType 场景类型，不能为 null
     * @return true 如果场景被排除，false 否则
     */
    public boolean isExcluded(@SceneType int sceneType) {
        return excludedScenes.contains(sceneType);
    }

    /**
     * 检查额外条件是否通过
     * <p>
     * 如果未设置额外条件，返回 true。
     * 如果设置了额外条件，返回条件的结果。
     * </p>
     * <p>
     * Check if extra condition passes
     * <p>
     * If no extra condition is set, returns true.
     * If an extra condition is set, returns the result of the condition.
     * </p>
     *
     * @return true 如果条件通过，false 否则
     */
    public boolean extraConditionPass() {
        return condition == null || condition.shouldShow();
    }

    /**
     * 可见性条件接口
     * <p>
     * 用于定义额外的显示条件，例如根据播放器状态、用户设置等动态判断是否显示。
     * </p>
     * <p>
     * Visibility Condition Interface
     * <p>
     * Used to define additional display conditions, such as dynamically determining whether to display based on player state, user settings, etc.
     * </p>
     */
    public interface VisibilityCondition {
        /**
         * 判断是否应该显示
         * <p>
         * Determine if should show
         * </p>
         *
         * @return true 如果应该显示，false 否则
         */
        boolean shouldShow();
    }

    /**
     * 插槽配置构建器
     * <p>
     * 采用 Builder 模式，方便创建 {@link SlotConfig} 实例。
     * </p>
     * <p>
     * Slot Configuration Builder
     * <p>
     * Uses Builder pattern to conveniently create {@link SlotConfig} instances.
     * </p>
     */
    public static class Builder {
        /**
         * 插槽类型
         */
        @Nullable
        private SlotType type;
        /**
         * 排除的场景列表
         */
        private Set<Integer> excludedScenes;
        /**
         * 额外显示条件
         */
        private VisibilityCondition condition;

        /**
         * 设置插槽类型
         * <p>
         * Set slot type
         * </p>
         *
         * @param type 插槽类型，不能为 null
         * @return 构建器实例，支持链式调用
         */
        public Builder type(@NonNull SlotType type) {
            this.type = type;
            return this;
        }

        /**
         * 设置排除的场景列表
         * <p>
         * 在这些场景下，插槽将不会显示。
         * </p>
         * <p>
         * Set excluded scenes
         * <p>
         * In these scenarios, the slot will not be displayed.
         * </p>
         *
         * @param scenes 排除的场景集合，不能为 null
         * @return 构建器实例，支持链式调用
         */
        public Builder excludeScenes(@NonNull Set<Integer> scenes) {
            this.excludedScenes = scenes;
            return this;
        }

        /**
         * 设置额外显示条件
         * <p>
         * 用于定义额外的显示条件，例如根据播放器状态、用户设置等动态判断。
         * </p>
         * <p>
         * Set extra visibility condition
         * <p>
         * Used to define additional display conditions, such as dynamically determining based on player state, user settings, etc.
         * </p>
         *
         * @param condition 可见性条件，可以为 null（表示无条件）
         * @return 构建器实例，支持链式调用
         */
        public Builder condition(@Nullable VisibilityCondition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * 构建插槽配置实例
         * <p>
         * 在构建之前会进行验证，确保插槽类型已设置。
         * </p>
         * <p>
         * Build slot configuration instance
         * <p>
         * Validation will be performed before building to ensure the slot type is set.
         * </p>
         *
         * @return 插槽配置实例，不会为 null
         * @throws IllegalArgumentException 如果插槽类型未设置
         */
        public SlotConfig build() {
            if (type == null) {
                throw new IllegalArgumentException("Slot type required");
            }
            return new SlotConfig(this);
        }
    }
}
