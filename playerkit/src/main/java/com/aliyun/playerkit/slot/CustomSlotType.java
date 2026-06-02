package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.utils.StringUtil;

import java.util.Objects;

/**
 * AliPlayerKit 自定义插槽类型
 * <p>
 * 表示用户自定义的插槽类型，允许在播放器任意层级插入自定义 UI 组件。
 * 通过指定 {@code order} 值，自定义插槽可以插入到内置插槽的任意层级之间。
 * </p>
 * <p>
 * <strong>层级规则</strong>：
 * <ul>
 *     <li>内置插槽的层级以 10 为间距递增（参见 {@link SlotType#getOrder()}）</li>
 *     <li>自定义插槽可使用间隙中的任意整数值定义层级</li>
 *     <li>例如：order=25 的自定义插槽将位于 GESTURE_CONTROL(20) 和 LANDSCAPE_HINT(30) 之间</li>
 *     <li>多个相同 order 值的插槽按注册顺序排列</li>
 * </ul>
 * </p>
 * <p>
 * <strong>使用示例</strong>：
 * <pre>{@code
 * // 定义水印插槽（位于 GESTURE_CONTROL(20) 和 LANDSCAPE_HINT(30) 之间）
 * CustomSlotType WATERMARK = new CustomSlotType("watermark", 25);
 *
 * // 注册构建器
 * registry.register(WATERMARK, parent -> new WatermarkView(parent.getContext()));
 * }</pre>
 * </p>
 * <p>
 * AliPlayerKit Custom Slot Type
 * <p>
 * Represents a user-defined slot type that allows inserting custom UI components at any layer level of the player.
 * By specifying an {@code order} value, custom slots can be inserted between any built-in slot layers.
 * </p>
 *
 * @author keria
 * @date 2026/05/11
 */
public final class CustomSlotType {

    /**
     * 插槽唯一标识
     * <p>
     * 用于区分不同的自定义插槽类型。同一个 key 的 CustomSlotType 被视为相同类型。
     * </p>
     */
    @NonNull
    private final String key;

    /**
     * 层级顺序值
     * <p>
     * 值越小层级越低（越靠底层），值越大层级越高（越靠顶层）。
     * </p>
     */
    private final int order;

    /**
     * 创建自定义插槽类型
     *
     * @param key   插槽唯一标识，不能为 null 或空字符串
     * @param order 层级顺序值，值越小越靠底层
     * @throws IllegalArgumentException 如果 key 为 null 或空字符串
     */
    public CustomSlotType(@NonNull String key, int order) {
        if (StringUtil.isEmpty(key)) {
            throw new IllegalArgumentException("Custom slot key must not be null or empty");
        }
        this.key = key;
        this.order = order;
    }

    /**
     * 获取插槽唯一标识
     * <p>
     * Get slot unique key
     * </p>
     *
     * @return 插槽唯一标识，不会为 null
     */
    @NonNull
    public String getKey() {
        return key;
    }

    /**
     * 获取层级顺序值
     * <p>
     * 值越小层级越低（越靠底层），值越大层级越高（越靠顶层）。
     * </p>
     * <p>
     * Get layer order value
     * <p>
     * Lower values mean lower layers (closer to the bottom), higher values mean higher layers (closer to the top).
     * </p>
     *
     * @return 层级顺序值
     */
    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomSlotType that = (CustomSlotType) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomSlotType{" +
                "key='" + key + '\'' +
                ", order=" + order +
                '}';
    }
}
