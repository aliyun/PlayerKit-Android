package com.aliyun.playerkit.example.settings;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;

/**
 * 定义设置项支持的所有视图类型
 * <p>
 * Supported view types for setting items.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
@IntDef({
        SettingType.HEADER,
        SettingType.BUTTON,
        SettingType.TEXT,
        SettingType.SWITCH,
        SettingType.SELECTOR,
})
@Retention(RetentionPolicy.SOURCE)
@Target(TYPE_USE)
public @interface SettingType {
    /**
     * 纯文本头布局
     * <p>
     * Header layout (Text only)
     */
    int HEADER = 0;

    /**
     * 普通交互按钮
     * <p>
     * Normal interactive button
     */
    int BUTTON = 1;

    /**
     * 纯文本展示项
     * <p>
     * Pure text display item
     */
    int TEXT = 2;

    /**
     * 开关切换项
     * <p>
     * Switch toggle item
     */
    int SWITCH = 3;

    /**
     * 弹出式单选列表项
     * <p>
     * Popup selector item
     */
    int SELECTOR = 4;
}
