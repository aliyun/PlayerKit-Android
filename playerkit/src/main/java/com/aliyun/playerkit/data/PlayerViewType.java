package com.aliyun.playerkit.data;

/**
 * 播放器视图类型枚举
 * <p>
 * 定义了播放器支持的视图类型，包括：
 * <ul>
 *     <li>DISPLAY_VIEW：AliDisplayView（推荐），官方提供的显示视图组件，具有更好的兼容性和性能优化</li>
 *     <li>SURFACE_VIEW：SurfaceView，传统 SurfaceView 实现，独立渲染线程，不支持动画</li>
 *     <li>TEXTURE_VIEW：TextureView，支持动画和变换，适合需要视图动画的场景</li>
 * </ul>
 * </p>
 * <p>
 * Player View Type Enumeration
 * <p>
 * Defines the view types supported by the player, including:
 * <ul>
 *     <li>DISPLAY_VIEW: AliDisplayView (Recommended), official display view component with better compatibility and performance optimization</li>
 *     <li>SURFACE_VIEW: SurfaceView, traditional SurfaceView implementation with independent rendering thread, no animation support</li>
 *     <li>TEXTURE_VIEW: TextureView, supports animation and transformation, suitable for scenarios requiring view animation</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public enum PlayerViewType {
    /**
     * AliDisplayView（推荐）
     * <p>
     * 阿里云播放器官方提供的显示视图组件，具有更好的兼容性和性能优化，建议优先使用。
     * </p>
     * <p>
     * AliDisplayView (Recommended)
     * <p>
     * Official display view component provided by Alibaba Cloud Player,
     * with better compatibility and performance optimization. Recommended for use.
     * </p>
     */
    DISPLAY_VIEW,

    /**
     * SurfaceView
     * <p>
     * 传统 SurfaceView 实现，具有以下特点：
     * - 独立渲染线程：Surface 在独立线程中渲染，不阻塞 UI 线程
     * - 适合独立渲染层：需要独立渲染层时使用
     * - 不支持动画：无法进行视图动画和变换
     * </p>
     * <p>
     * SurfaceView
     * <p>
     * Traditional SurfaceView implementation with the following characteristics:
     * - Independent rendering thread: Surface renders in an independent thread, does not block UI thread
     * - Suitable for independent rendering layer: Use when independent rendering layer is needed
     * - No animation support: Cannot perform view animation and transformation
     * </p>
     */
    SURFACE_VIEW,

    /**
     * TextureView
     * <p>
     * 基于 Android 原生 TextureView，具有以下特点：
     * - 支持动画和变换：可以进行视图动画、旋转、缩放等操作
     * - 适合动画场景：需要视图动画时使用
     * - 性能略低：相比 SurfaceView 性能略低
     * </p>
     * <p>
     * TextureView
     * <p>
     * Based on Android native TextureView with the following characteristics:
     * - Supports animation and transformation: Can perform view animation, rotation, scaling, etc.
     * - Suitable for animation scenarios: Use when view animation is needed
     * - Slightly lower performance: Performance is slightly lower compared to SurfaceView
     * </p>
     */
    TEXTURE_VIEW,

}

