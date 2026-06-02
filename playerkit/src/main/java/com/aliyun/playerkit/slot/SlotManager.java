package com.aliyun.playerkit.slot;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.logging.LogHub;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * AliPlayerKit 插槽管理器
 * <p>
 * 插槽系统的统一入口，负责插槽注册、可见性配置和运行时访问。
 * 通过 {@code playerView.getSlotManager()} 获取实例。
 * </p>
 * <p>
 * AliPlayerKit Slot Manager
 * <p>
 * Unified entry point for the slot system, responsible for slot registration,
 * visibility configuration, and runtime access.
 * Obtained via {@code playerView.getSlotManager()}.
 * </p>
 *
 * @author keria
 * @since 2026/05/14
 */
public class SlotManager {

    private static final String TAG = "SlotManager";

    /**
     * 插槽构建器映射表
     * <p>
     * Key: 插槽类型，Value: 对应的构建器。
     * </p>
     */
    @NonNull
    private final Map<SlotType, SlotBuilder> builders = new EnumMap<>(SlotType.class);

    /**
     * 自定义插槽构建器映射表
     * <p>
     * Key: 自定义插槽类型，Value: 对应的构建器。
     * </p>
     */
    @NonNull
    private final Map<CustomSlotType, SlotBuilder> customBuilders = new LinkedHashMap<>();

    /**
     * 被隐藏的插槽集合（整体不渲染）
     */
    @NonNull
    private final Set<SlotType> hiddenSlots = EnumSet.noneOf(SlotType.class);

    /**
     * 插槽内被隐藏的元素映射
     * <p>
     * Key: 插槽类型，Value: 该插槽内被隐藏的元素 ID 集合。
     * </p>
     */
    @NonNull
    private final Map<SlotType, Set<String>> hiddenElements = new EnumMap<>(SlotType.class);

    /**
     * 宿主布局引用，用于运行时委托
     */
    @NonNull
    final SlotHostLayout host;

    // ==================== 构造函数 ====================

    /**
     * 包级构造函数，由 SlotHostLayout 创建。
     *
     * @param host 宿主布局，不能为 null
     */
    SlotManager(@NonNull SlotHostLayout host) {
        this.host = host;
    }

    // ==================== 插槽注册 ====================

    /**
     * 注册插槽构建器
     * <p>
     * 注册一个 {@link SlotBuilder} 实例用于构建指定类型的插槽视图。
     * 如果该插槽类型已存在构建器，则会被新构建器替换。
     * </p>
     * <p>
     * Register a {@link SlotBuilder} instance for building slot views of the specified type.
     * If a builder already exists for this slot type, it will be replaced.
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
     * </p>
     * <p>
     * Register slot class via reflection. The class must have a constructor accepting {@link Context}.
     * </p>
     *
     * @param type      插槽类型，不能为 null
     * @param slotClass 插槽类，必须继承自 View 且有接受 Context 的构造函数
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
     * 直接使用 XML 布局资源作为插槽，框架会自动包装为 {@link BaseSlot}。
     * </p>
     * <p>
     * Register layout resource as slot. The framework wraps it in a {@link BaseSlot}.
     * </p>
     *
     * @param type     插槽类型，不能为 null
     * @param layoutId 布局资源ID
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
     * 注册自定义插槽构建器
     * <p>
     * 自定义插槽通过 {@link CustomSlotType#getOrder()} 指定层级位置。
     * </p>
     * <p>
     * Register custom slot builder. Custom slots specify layer position via {@link CustomSlotType#getOrder()}.
     * </p>
     *
     * @param type    自定义插槽类型，不能为 null
     * @param builder 插槽构建器，不能为 null
     */
    public void register(@NonNull CustomSlotType type, @NonNull SlotBuilder builder) {
        if (customBuilders.containsKey(type)) {
            customBuilders.remove(type);
            LogHub.i(TAG, "Replacing builder for custom slot: " + type.getKey());
        }
        customBuilders.put(type, builder);
        LogHub.i(TAG, "Registered builder for custom slot: " + type.getKey() + " (order=" + type.getOrder() + ")");
    }

    /**
     * 注销插槽构建器
     * <p>
     * Unregister the builder for the specified slot type.
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
     * 注销自定义插槽构建器
     * <p>
     * Unregister custom slot builder.
     * </p>
     *
     * @param type 自定义插槽类型，不能为 null
     * @return 被注销的构建器，如果不存在则返回 null
     */
    @Nullable
    public SlotBuilder unregister(@NonNull CustomSlotType type) {
        SlotBuilder removed = customBuilders.remove(type);
        if (removed != null) {
            LogHub.i(TAG, "Unregistered builder for custom slot: " + type.getKey());
        }
        return removed;
    }

    /**
     * 检查是否已注册指定插槽类型的构建器
     * <p>
     * Check if builder is registered for the specified slot type.
     * </p>
     *
     * @param type 插槽类型，不能为 null
     * @return true 如果已注册
     */
    public boolean isRegistered(@NonNull SlotType type) {
        return builders.containsKey(type);
    }

    /**
     * 检查是否已注册指定自定义插槽类型的构建器
     * <p>
     * Check if builder is registered for the specified custom slot type.
     * </p>
     *
     * @param type 自定义插槽类型，不能为 null
     * @return true 如果已注册
     */
    public boolean isRegistered(@NonNull CustomSlotType type) {
        return customBuilders.containsKey(type);
    }

    /**
     * 清空所有配置
     * <p>
     * 清空所有构建器、自定义构建器、隐藏插槽和隐藏元素配置。
     * </p>
     * <p>
     * Clear all builders, custom builders, hidden slots, and hidden elements.
     * </p>
     */
    public void clearAll() {
        builders.clear();
        customBuilders.clear();
        hiddenSlots.clear();
        hiddenElements.clear();
        LogHub.i(TAG, "All configurations cleared");
    }

    // ==================== 可见性配置 ====================

    /**
     * 隐藏指定插槽（整体不渲染）。
     * <p>
     * Hide specified slot (will not be rendered at all).
     * </p>
     *
     * @param type 插槽类型，不能为 null
     */
    public void hide(@NonNull SlotType type) {
        hiddenSlots.add(type);
    }

    /**
     * 显示之前隐藏的插槽。
     * <p>
     * Show a previously hidden slot.
     * </p>
     *
     * @param type 插槽类型，不能为 null
     */
    public void show(@NonNull SlotType type) {
        hiddenSlots.remove(type);
    }

    /**
     * 检查指定插槽是否被隐藏。
     * <p>
     * Check if the specified slot is hidden.
     * </p>
     *
     * @param type 插槽类型，不能为 null
     * @return true 如果被隐藏
     */
    public boolean isHidden(@NonNull SlotType type) {
        return hiddenSlots.contains(type);
    }

    /**
     * 隐藏指定插槽内的特定元素。
     * <p>
     * Hide specific elements within a slot.
     * </p>
     *
     * @param type       插槽类型，不能为 null
     * @param elementIds 元素 ID，不能为 null
     */
    public void hideElements(@NonNull SlotType type, @NonNull String... elementIds) {
        Set<String> elements = hiddenElements.get(type);
        if (elements == null) {
            elements = new LinkedHashSet<>();
            hiddenElements.put(type, elements);
        }
        Collections.addAll(elements, elementIds);
    }

    /**
     * 显示指定插槽内之前隐藏的特定元素。
     * <p>
     * Show previously hidden elements within a slot.
     * </p>
     *
     * @param type       插槽类型，不能为 null
     * @param elementIds 元素 ID，不能为 null
     */
    public void showElements(@NonNull SlotType type, @NonNull String... elementIds) {
        Set<String> elements = hiddenElements.get(type);
        if (elements != null) {
            for (String id : elementIds) {
                elements.remove(id);
            }
            if (elements.isEmpty()) {
                hiddenElements.remove(type);
            }
        }
    }

    /**
     * 设置指定插槽的隐藏元素集合（替换之前的配置）。
     * <p>
     * Set hidden elements for a slot (replaces previous configuration).
     * </p>
     *
     * @param type       插槽类型，不能为 null
     * @param elementIds 元素 ID 集合，不能为 null
     */
    public void setHiddenElements(@NonNull SlotType type, @NonNull Set<String> elementIds) {
        if (elementIds.isEmpty()) {
            hiddenElements.remove(type);
        } else {
            hiddenElements.put(type, new LinkedHashSet<>(elementIds));
        }
    }

    /**
     * 获取指定插槽的隐藏元素集合。
     * <p>
     * Get hidden elements for a slot.
     * </p>
     *
     * @param type 插槽类型，不能为 null
     * @return 隐藏元素集合（不可变），如果没有配置则返回 null
     */
    @Nullable
    public Set<String> getHiddenElements(@NonNull SlotType type) {
        Set<String> elements = hiddenElements.get(type);
        return elements != null ? Collections.unmodifiableSet(elements) : null;
    }

    /**
     * 批量设置所有插槽的隐藏元素配置。
     * <p>
     * Set hidden elements configuration for all slots at once.
     * </p>
     *
     * @param config 配置映射，传 null 清空所有配置
     */
    public void setAllHiddenElements(@Nullable Map<SlotType, Set<String>> config) {
        hiddenElements.clear();
        if (config != null) {
            for (Map.Entry<SlotType, Set<String>> entry : config.entrySet()) {
                SlotType key = entry.getKey();
                Set<String> value = entry.getValue();
                if (key != null && value != null && !value.isEmpty()) {
                    hiddenElements.put(key, new LinkedHashSet<>(value));
                }
            }
        }
    }

    /**
     * 清空所有隐藏元素配置。
     * <p>
     * Clear all hidden elements configuration.
     * </p>
     */
    public void clearAllHiddenElements() {
        hiddenElements.clear();
    }

    // ==================== 运行时访问（委托给 SlotHostLayout）====================

    /**
     * 获取指定类型的插槽视图
     * <p>
     * Get slot view of specified type.
     * </p>
     *
     * @param type 插槽类型，不能为 null
     * @return 插槽视图，如果不存在则返回 null
     */
    @Nullable
    public View getSlotView(@NonNull SlotType type) {
        return host.getSlotViewInternal(type);
    }

    /**
     * 重建所有插槽
     * <p>
     * 根据当前配置重新构建所有插槽视图。
     * </p>
     * <p>
     * Rebuild all slot views based on current configuration.
     * </p>
     */
    public void rebuildSlots() {
        host.rebuildSlots();
    }

    /**
     * 更新场景类型
     * <p>
     * 切换播放场景，采用增量更新策略。
     * </p>
     * <p>
     * Update scene type with incremental update strategy.
     * </p>
     *
     * @param sceneType 新的场景类型
     */
    public void updateSceneType(@SceneType int sceneType) {
        host.updateSlotsIncremental(sceneType);
    }

    // ==================== 包级方法（供 SlotHostLayout 使用）====================

    /**
     * 构建插槽视图
     *
     * @param type   插槽类型
     * @param parent 父视图容器
     * @return 构建的视图实例，如果未注册构建器或构建失败则返回 null
     */
    @Nullable
    View buildSlot(@NonNull SlotType type, @NonNull ViewGroup parent) {
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
     * 构建自定义插槽视图
     *
     * @param type   自定义插槽类型
     * @param parent 父视图容器
     * @return 构建的视图实例，如果未注册构建器或构建失败则返回 null
     */
    @Nullable
    View buildCustomSlot(@NonNull CustomSlotType type, @NonNull ViewGroup parent) {
        SlotBuilder builder = customBuilders.get(type);
        if (builder == null) {
            LogHub.i(TAG, "No builder registered for custom slot: " + type.getKey());
            return null;
        }

        try {
            View view = builder.build(parent);
            if (view == null) {
                LogHub.w(TAG, "Builder returned null for custom slot: " + type.getKey());
            }
            return view;
        } catch (Exception e) {
            LogHub.e(TAG, "Error building custom slot: " + type.getKey(), e);
            return null;
        }
    }

    /**
     * 获取所有已注册的自定义插槽类型
     *
     * @return 已注册的自定义插槽类型集合（不可修改）
     */
    @NonNull
    Set<CustomSlotType> getCustomSlotTypes() {
        return Collections.unmodifiableSet(customBuilders.keySet());
    }

    /**
     * 获取已注册的插槽类型数量
     *
     * @return 已注册的插槽类型数量
     */
    int size() {
        return builders.size();
    }
}
