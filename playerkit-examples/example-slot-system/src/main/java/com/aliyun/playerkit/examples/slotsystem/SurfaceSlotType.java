package com.aliyun.playerkit.examples.slotsystem;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Surface 插槽类型枚举
 * <p>
 * 定义了所有可用的 Surface 插槽类型及其特性说明。
 * 每个枚举值包含名称、标签和特性的字符串资源 ID，支持国际化。
 * </p>
 * <p>
 * Surface Slot Type Enumeration
 * <p>
 * Defines all available Surface slot types and their feature descriptions.
 * Each enum value contains string resource IDs for name, tag, and features, supporting internationalization.
 * </p>
 *
 * @author keria
 * @date 2025/12/11
 */
public enum SurfaceSlotType {
    /**
     * SurfaceViewSlot：传统 SurfaceView 实现
     * <p>
     * 基于 Android 原生 SurfaceView，具有以下特点：
     * - 独立渲染线程：Surface 在独立线程中渲染，不阻塞 UI 线程
     * - 适合独立渲染层：需要独立渲染层时使用
     * - 不支持动画：无法进行视图动画和变换
     * </p>
     */
    SURFACE_VIEW(R.string.slot_type_surface_view, R.string.slot_tag_traditional, R.string.slot_features_surface_view),

    /**
     * TextureViewSlot：支持动画
     * <p>
     * 基于 Android 原生 TextureView，具有以下特点：
     * - 支持动画和变换：可以进行视图动画、旋转、缩放等操作
     * - 适合动画场景：需要视图动画时使用
     * - 性能略低：相比 SurfaceView 性能略低
     * </p>
     */
    TEXTURE_VIEW(R.string.slot_type_texture_view, R.string.slot_tag_animation, R.string.slot_features_texture_view),

    /**
     * 空插槽：移除 Surface
     * <p>
     * 不注册任何 Surface 插槽，适用于以下场景：
     * - 纯音频播放：不需要视频显示
     * - 后台播放：视频在后台播放，不需要显示
     * - 特殊场景：需要自定义 Surface 管理逻辑
     * </p>
     */
    EMPTY(R.string.slot_type_empty, R.string.slot_tag_remove_surface, R.string.slot_features_empty);

    /**
     * 插槽类型名称资源 ID
     */
    private final int nameResId;

    /**
     * 插槽类型标签资源 ID
     */
    private final int tagResId;

    /**
     * 插槽特性说明资源 ID
     */
    private final int featuresResId;

    /**
     * 构造函数
     *
     * @param nameResId     名称资源 ID
     * @param tagResId      标签资源 ID
     * @param featuresResId 特性资源 ID
     */
    SurfaceSlotType(int nameResId, int tagResId, int featuresResId) {
        this.nameResId = nameResId;
        this.tagResId = tagResId;
        this.featuresResId = featuresResId;
    }

    /**
     * 获取插槽类型名称
     * <p>
     * Get slot type name
     * </p>
     *
     * @param context Context 实例，用于获取字符串资源
     * @return 插槽类型名称
     */
    @NonNull
    public String getName(@NonNull Context context) {
        return context.getString(nameResId);
    }

    /**
     * 获取插槽类型标签
     * <p>
     * Get slot type tag
     * </p>
     *
     * @param context Context 实例，用于获取字符串资源
     * @return 插槽类型标签
     */
    @NonNull
    public String getTag(@NonNull Context context) {
        return context.getString(tagResId);
    }

    /**
     * 获取插槽特性说明
     * <p>
     * Get slot features description
     * </p>
     *
     * @param context Context 实例，用于获取字符串资源
     * @return 插槽特性说明
     */
    @NonNull
    public String getFeatures(@NonNull Context context) {
        return context.getString(featuresResId);
    }

    /**
     * 获取显示名称（包含标签）
     * <p>
     * Get display name (including tag)
     * </p>
     *
     * @param context Context 实例，用于获取字符串资源
     * @return 格式化的显示名称，格式为：名称（标签）
     */
    @NonNull
    public String getDisplayName(@NonNull Context context) {
        return getName(context) + "（" + getTag(context) + "）";
    }
}
