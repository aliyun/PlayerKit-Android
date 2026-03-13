package com.aliyun.playerkit.slot;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogHub;

import java.util.EnumMap;
import java.util.Map;

/**
 * AliPlayerKit 插槽注册表
 * <p>
 * 维护插槽类型（{@link SlotType}）到插槽构建器（{@link SlotBuilder}）的映射关系。
 * 负责管理所有插槽的构建逻辑，支持注册、注销和构建插槽视图。
 * </p>
 * <p>
 * AliPlayerKit Slot Registry
 * <p>
 * Maintains the mapping relationship between slot types ({@link SlotType}) and slot builders ({@link SlotBuilder}).
 * Responsible for managing the build logic of all slots, supporting registration, logout, and building slot views.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public class SlotRegistry {

    private static final String TAG = "SlotRegistry";

    /**
     * 插槽构建器映射表
     * <p>
     * Key: 插槽类型，Value: 对应的构建器。
     * </p>
     */
    @NonNull
    private final Map<SlotType, SlotBuilder> builders = new EnumMap<>(SlotType.class);

    // ==================== 公开方法 ====================

    /**
     * 注册插槽构建器
     * <p>
     * 注册一个 {@link SlotBuilder} 实例用于构建指定类型的插槽视图。
     * 如果该插槽类型已存在构建器，则会被新构建器替换。
     * </p>
     * <p>
     * Register slot builder
     * <p>
     * Register a {@link SlotBuilder} instance for building slot views of the specified type.
     * If a builder already exists for this slot type, it will be replaced by the new builder.
     * </p>
     *
     * @param type    插槽类型，不能为 null
     * @param builder 插槽构建器，不能为 null
     */
    public void register(@NonNull SlotType type, @NonNull SlotBuilder builder) {
        if (builders.containsKey(type)) {
            LogHub.i(TAG, "Replacing builder for slot: " + type);
        }
        builders.put(type, builder);
        LogHub.i(TAG, "Registered builder for slot: " + type);
    }

    /**
     * 注册插槽类
     * <p>
     * 通过反射创建插槽实例。插槽类必须有一个接受 {@link Context} 参数的构造函数。
     * 如果反射创建失败，构建时会返回 null。
     * </p>
     * <p>
     * Register slot class
     * <p>
     * Create slot instance through reflection. The slot class must have a constructor that accepts a {@link Context} parameter.
     * If reflection creation fails, null will be returned during build.
     * </p>
     *
     * @param type      插槽类型，不能为 null
     * @param slotClass 插槽类，必须继承自 View 且有一个接受 Context 的构造函数，不能为 null
     */
    public void register(@NonNull SlotType type, @NonNull Class<? extends View> slotClass) {
        register(type, parent -> {
            try {
                return slotClass.getConstructor(Context.class).newInstance(parent.getContext());
            } catch (Exception e) {
                LogHub.e(TAG, "Failed to create slot: " + slotClass.getName(), e);
                return null;
            }
        });
    }

    /**
     * 注册布局资源插槽
     * <p>
     * 直接使用 XML 布局资源作为插槽。
     * 框架会自动将布局包装在一个 {@link BaseSlot} 实例中，并加载指定的布局资源。
     * </p>
     * <p>
     * Register layout resource slot
     * <p>
     * Use XML layout resource directly as slot.
     * The framework will automatically wrap the layout in a {@link BaseSlot} instance and load the specified layout resource.
     * </p>
     *
     * @param type     插槽类型，不能为 null
     * @param layoutId 布局资源ID，必须有效
     */
    public void register(@NonNull SlotType type, int layoutId) {
        register(type, parent -> new BaseSlot(parent.getContext()) {
            @Override
            protected int getLayoutId() {
                return layoutId;
            }
        });
    }

    /**
     * 注销插槽构建器
     * <p>
     * 注销指定插槽类型的构建器。注销后，该插槽类型将无法构建视图（{@link #build(SlotType, ViewGroup)} 方法返回 null）。
     * </p>
     * <p>
     * Unregister slot builder
     * <p>
     * Unregister the builder for the specified slot type. After unregistration, the slot type will not be able to build views
     * ({@link #build(SlotType, ViewGroup)} method returns null).
     * </p>
     *
     * @param type 插槽类型，不能为 null
     * @return 被注销的构建器，如果不存在则返回 null
     */
    @Nullable
    public SlotBuilder unregister(@NonNull SlotType type) {
        SlotBuilder removed = builders.remove(type);
        if (removed != null) {
            LogHub.i(TAG, "Unregistered builder for slot: " + type);
        }
        return removed;
    }

    /**
     * 构建插槽视图
     * <p>
     * 根据插槽类型查找对应的构建器，并构建视图实例。
     * 如果该插槽类型未注册构建器，返回 null。
     * 如果构建过程中发生异常，会记录错误日志并返回 null。
     * </p>
     * <p>
     * Build slot view
     * <p>
     * Find the corresponding builder based on the slot type and build the view instance.
     * If no builder is registered for this slot type, returns null.
     * If an exception occurs during the build process, an error log will be recorded and null will be returned.
     * </p>
     *
     * @param type   插槽类型，不能为 null
     * @param parent 父视图容器，不能为 null
     * @return 构建的视图实例，如果未注册构建器或构建失败则返回 null
     */
    @Nullable
    public View build(@NonNull SlotType type, @NonNull ViewGroup parent) {
        SlotBuilder builder = builders.get(type);
        if (builder == null) {
            LogHub.i(TAG, "No builder registered for slot: " + type);
            return null;
        }

        try {
            View view = builder.build(parent);
            if (view == null) {
                LogHub.w(TAG, "Builder returned null for slot: " + type);
            }
            return view;
        } catch (Exception e) {
            LogHub.e(TAG, "Error building slot: " + type, e);
            return null;
        }
    }

    /**
     * 检查是否已注册指定插槽类型的构建器
     * <p>
     * Check if builder is registered for the specified slot type
     * </p>
     *
     * @param type 插槽类型，不能为 null
     * @return true 如果已注册，false 否则
     */
    public boolean isRegistered(@NonNull SlotType type) {
        return builders.containsKey(type);
    }

    /**
     * 清空所有注册的构建器
     * <p>
     * 清空后，所有插槽类型都将无法构建视图。
     * </p>
     * <p>
     * Clear all registered builders
     * <p>
     * After clearing, all slot types will not be able to build views.
     * </p>
     */
    public void clear() {
        builders.clear();
        LogHub.i(TAG, "All builders cleared");
    }

    /**
     * 获取已注册的插槽类型数量
     * <p>
     * Get the number of registered slot types
     * </p>
     *
     * @return 已注册的插槽类型数量
     */
    public int size() {
        return builders.size();
    }
}
