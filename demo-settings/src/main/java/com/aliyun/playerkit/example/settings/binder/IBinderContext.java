package com.aliyun.playerkit.example.settings.binder;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.example.settings.SettingModel;

/**
 * 绑定上下文接口
 * <p>
 * 职责：作为 Binder 与外界（通常是 Adapter）通信的桥梁，避免具体的 Binder 直接持有 Adapter 引用
 * </p>
 * <p>
 * Binder Context Interface
 * <p>
 * Responsibility: Acts as a bridge for communication between the Binder and the outside (usually the Adapter), avoiding the specific Binder holding the Adapter reference directly.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public interface IBinderContext {
    /**
     * 通知外部项变更
     * <p>
     * 当设置项的数据发生变化时调用，用于触发 UI 刷新
     * </p>
     *
     * @param model 数据发生变化的设置项
     */
    void notifyItemChanged(@NonNull SettingModel model);
}
