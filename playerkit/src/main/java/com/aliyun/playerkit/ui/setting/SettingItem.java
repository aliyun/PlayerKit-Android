package com.aliyun.playerkit.ui.setting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 设置项的抽象数据模型。
 * <p>
 * 用于描述设置菜单中的单个配置项，包括其标识、展示标题、类型、当前值、
 * 可选值列表以及值变更回调。
 * </p>
 *
 * <p>
 * Setting item abstract data model.
 * Represents a single configurable entry in the settings menu.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public class SettingItem<T> {

    /**
     * 设置项唯一标识（Key）。
     * <p>
     * 用于区分不同设置项，可用于持久化存储或状态恢复。
     * </p>
     *
     * <p>
     * Unique key of this setting item.
     * Used for identification, persistence or state restoration.
     * </p>
     */
    public final String key;

    /**
     * 设置项显示标题。
     * <p>
     * 通常为已国际化的字符串（如来自 resources）。
     * </p>
     *
     * <p>
     * Display title of this setting item.
     * Usually an internationalized string resolved from resources.
     * </p>
     */
    public final String title;

    /**
     * 设置项类型。
     * <p>
     * 决定该设置项在 UI 中的表现形式，例如选择器（Selector）或开关（Switcher）。
     * </p>
     *
     * <p>
     * Setting item type.
     * Determines how this item is presented in UI, e.g. selector or switcher.
     * </p>
     */
    public final @SettingItemType int type;

    /**
     * 当前选中的值。
     * <p>
     * 对于 {@link SettingItemType#SELECTOR}，表示当前选中的选项值；
     * 对于 {@link SettingItemType#SWITCHER}，通常为 {@link Boolean}。
     * </p>
     *
     * <p>
     * Current value of this item.
     * For {@link SettingItemType#SELECTOR}, it indicates the selected option;
     * for {@link SettingItemType#SWITCHER}, it is usually a {@link Boolean}.
     * </p>
     */
    public T currentValue;

    /**
     * 可选值列表（仅适用于 Selector 类型）。
     * <p>
     * 当 {@link #type} 为 {@link SettingItemType#SELECTOR} 时生效；
     * 对于其他类型可为 {@code null}。
     * </p>
     *
     * <p>
     * Available options (only applicable to selector type).
     * Takes effect when {@link #type} is {@link SettingItemType#SELECTOR};
     * for other types it may be {@code null}.
     * </p>
     */
    @Nullable
    public SettingOptions<T> options;

    /**
     * 值变更监听回调。
     * <p>
     * 当用户修改该设置项的值时触发，用于将变更同步到业务层或播放器控制层。
     * </p>
     *
     * <p>
     * Value change callback.
     * Triggered when user changes the value, used to propagate changes to business/player control layer.
     * </p>
     */
    @Nullable
    public OnValueChangeListener<T> listener;

    /**
     * 值格式化器。
     * <p>
     * 用于将当前值转换为用户可读的显示字符串，例如：
     * </p>
     * <ul>
     *   <li>倍速：{@code 1.5f -> "1.5x"}</li>
     *   <li>旋转角度：{@code 90 -> "90°"}</li>
     * </ul>
     *
     * <p>
     * Value formatter.
     * Converts the current value into a user-readable string, e.g.
     * </p>
     * <ul>
     *   <li>Speed: {@code 1.5f -> "1.5x"}</li>
     *   <li>Rotation: {@code 90 -> "90°"}</li>
     * </ul>
     */
    @Nullable
    public ValueFormatter<T> formatter;

    /**
     * 构造一个设置项。
     * <p>
     * Construct a setting item.
     * </p>
     *
     * @param key          设置项唯一标识 / Unique key of the item
     * @param title        设置项显示标题 / Display title
     * @param type         设置项类型 / Item type
     * @param initialValue 初始值 / Initial value
     */
    public SettingItem(String key, String title, @SettingItemType int type, T initialValue) {
        this.key = key;
        this.title = title;
        this.type = type;
        this.currentValue = initialValue;
    }

    /**
     * 设置项值变更监听接口。
     * <p>
     * Listener for setting value changes.
     * </p>
     *
     * @param <T> 值类型 / Value type
     */
    public interface OnValueChangeListener<T> {

        /**
         * 当设置项的值发生变化时回调。
         * <p>
         * Called when the setting item's value changes.
         * </p>
         *
         * @param item     当前设置项 / Current item
         * @param newValue 新的值 / New value
         */
        void onValueChanged(SettingItem<T> item, T newValue);
    }

    /**
     * 值格式化接口。
     * <p>
     * Formatter for displaying value.
     * </p>
     *
     * @param <T> 值类型 / Value type
     */
    public interface ValueFormatter<T> {

        /**
         * 将值格式化为可显示的字符串。
         * <p>
         * Formats the value into a displayable string.
         * </p>
         *
         * @param value 原始值 / Raw value
         * @return 用于 UI 显示的字符串 / String for UI display
         */
        String format(T value);
    }

    /**
     * 获取当前值对应的显示文本。
     * <p>
     * 若设置了 {@link #formatter}，则优先使用格式化结果；
     * 否则使用 {@link String#valueOf(Object)}。
     * </p>
     *
     * <p>
     * Returns the display text for the current value.
     * If {@link #formatter} is set, the formatted result is used;
     * otherwise {@link String#valueOf(Object)} is used.
     * </p>
     *
     * @return 用于 UI 显示的字符串 / String for UI display
     */
    @NonNull
    public String getDisplayValue() {
        return formatter != null ? formatter.format(currentValue) : String.valueOf(currentValue);
    }
}
