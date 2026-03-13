package com.aliyun.playerkit.slot;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * AliPlayerKit 插槽构建器接口
 * <p>
 * 用于构建特定类型的插槽视图。支持多种构建方式：
 * </p>
 * AliPlayerKit Slot Builder Interface
 * <p>
 * Used to build slot views of specific types. Supports multiple build methods.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public interface SlotBuilder {
    /**
     * 构建插槽视图
     * <p>
     * 根据父容器创建并返回插槽视图实例。
     * 构建的视图会被添加到父容器中，并调用其生命周期方法。
     * </p>
     * <p>
     * Build slot view
     * <p>
     * Create and return a slot view instance based on the parent container.
     * The built view will be added to the parent container and its lifecycle methods will be called.
     * </p>
     *
     * @param parent 父视图容器，不能为 null
     * @return 构建的插槽视图，不能为 null
     */
    @NonNull
    View build(@NonNull ViewGroup parent);
}
