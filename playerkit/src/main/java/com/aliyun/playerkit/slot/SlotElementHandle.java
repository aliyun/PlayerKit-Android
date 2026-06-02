package com.aliyun.playerkit.slot;

/**
 * 插槽元素控制句柄
 * <p>
 * 定义了控制插槽内部元素可见性/启用状态的抽象接口。
 * 配合 {@link BaseSlot#registerElement(String, SlotElementHandle)} 使用，
 * 实现框架层自动管理元素的显示/隐藏。
 * </p>
 * <p>
 * Slot Element Control Handle
 * <p>
 * Defines an abstract interface for controlling the visibility/enabled state of elements within a slot.
 * Used with {@link BaseSlot#registerElement(String, SlotElementHandle)} to achieve
 * framework-level automatic management of element show/hide.
 * </p>
 *
 * @author keria
 * @date 2026/05/10
 */
@FunctionalInterface
public interface SlotElementHandle {

    /**
     * 设置元素的可见性
     * <p>
     * 当 visible 为 true 时，元素应恢复为可见/启用状态；
     * 当 visible 为 false 时，元素应被隐藏/禁用。
     * </p>
     * <p>
     * Set element visibility
     * <p>
     * When visible is true, the element should be restored to visible/enabled state;
     * when visible is false, the element should be hidden/disabled.
     * </p>
     *
     * @param visible true 表示可见/启用，false 表示隐藏/禁用
     */
    void setVisible(boolean visible);
}
