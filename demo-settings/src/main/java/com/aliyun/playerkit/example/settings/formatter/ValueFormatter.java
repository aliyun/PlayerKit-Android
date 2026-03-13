package com.aliyun.playerkit.example.settings.formatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 值格式化器接口
 * <p>
 * 用于定义值类型（T）和显示字符串之间的双向转换。
 * 主要用于选择器类型的设置项，将内部值类型（如枚举、整数等）与用户界面显示的字符串进行转换。
 * </p>
 * <p>
 * Value Formatter Interface
 * <p>
 * Used to define bidirectional conversion between value type (T) and display string.
 * Mainly used for selector-type setting items to convert internal value types (such as enums, integers, etc.)
 * to/from user interface display strings.
 * </p>
 *
 * @param <T> 值类型，如枚举、整数等
 * @author keria
 * @date 2026/01/04
 */
public interface ValueFormatter<T> {

    /**
     * 将值类型转换为显示字符串
     * <p>
     * Convert value type to display string
     * </p>
     *
     * @param value 值类型实例，可能为 null
     * @return 显示字符串，不能为 null
     */
    @NonNull
    String format(@Nullable T value);

    /**
     * 将显示字符串解析为值类型
     * <p>
     * Parse display string to value type
     * </p>
     *
     * @param displayString 显示字符串，不能为 null
     * @return 值类型实例，可能为 null（如果解析失败）
     */
    @Nullable
    T parse(@NonNull String displayString);

    /**
     * 获取默认值
     * <p>
     * 当解析失败或没有保存的值时使用
     * </p>
     * <p>
     * Get default value
     * <p>
     * Used when parsing fails or no saved value exists
     * </p>
     *
     * @return 默认值，不能为 null
     */
    @NonNull
    T getDefaultValue();
}

