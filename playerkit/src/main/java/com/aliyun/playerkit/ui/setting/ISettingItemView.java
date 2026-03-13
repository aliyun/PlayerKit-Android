package com.aliyun.playerkit.ui.setting;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * Setting 菜单中单个设置项对应的 View 接口定义。
 * <p>
 * 该接口用于约束所有设置项视图（如开关、列表、单选项等）的基本行为，
 * 使其能够与 {@link com.aliyun.playerkit.ui.slots.SettingMenuSlot} 进行统一交互和管理。
 * </p>
 *
 * @param <T> 设置项的值类型，例如 Boolean、Integer、String 或自定义类型
 * @author keria
 * @date 2025/12/25
 */
public interface ISettingItemView<T> {

    /**
     * 获取该设置项对应的根 View。
     * <p>
     * SettingMenuSlot 会通过此方法将 View 添加到父容器中。
     * </p>
     *
     * @return 非空的 View 实例
     */
    @NonNull
    View getView();

    /**
     * 绑定完整的设置项数据。
     * <p>
     * 通常在 View 初始化或首次展示时调用
     * </p>
     *
     * @param item 要绑定的设置项数据，不能为空
     */
    void bind(@NonNull SettingItem<T> item);

    /**
     * 仅更新设置项的值，而不进行完整的数据重新绑定。
     * <p>
     * 该方法适用于设置值发生变化，但标题、描述等信息未变化的场景，
     * 可以减少不必要的 UI 刷新，提高性能。
     * </p>
     *
     * @param value 最新的设置值，不能为空
     */
    void updateValueOnly(@NonNull T value);
}
