package com.aliyun.playerkit.ui.setting;

import static java.lang.annotation.ElementType.TYPE_USE;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置项类型定义。
 * <p>
 * Setting item type definition.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(TYPE_USE)
@IntDef({
        SettingItemType.SELECTOR,
        SettingItemType.SWITCHER,
})
public @interface SettingItemType {

    /**
     * 选择器类型。
     * <p>
     * 用于显示一个列表供用户选择，
     * 列表项的显示内容由 {@link SettingItem#getDisplayValue()} 提供。
     * </p>
     *
     * <p>
     * Selector type.
     * Used to display a list for user selection.
     * The display content of each item is provided by {@link SettingItem#getDisplayValue()}.
     * </p>
     */
    int SELECTOR = 0;

    /**
     * 开关类型。
     * <p>
     * 用于显示一个开关控件，
     * 用户切换开关时会触发 {@link SettingItem#currentValue} 的变化。
     * </p>
     *
     * <p>
     * Switch type.
     * Used to display a switch.
     * Toggling the switch triggers a change to {@link SettingItem#currentValue}.
     * </p>
     */
    int SWITCHER = 1;
}
