package com.aliyun.playerkit.example.settings.binder;

import com.aliyun.playerkit.example.settings.SettingModel;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * 设置项视图绑定器接口
 * <p>
 * 职责：定义单一类型的设置项如何将其数据绑定到 UI 上，并处理在该视图上的交互逻辑
 * </p>
 * <p>
 * Setting Item Binder Interface
 * <p>
 * Responsibility: Defines how a single type of setting item binds its data to the UI and handles interaction logic on that view.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public interface ISettingBinder {
    /**
     * 将数据模型绑定到视图
     * <p>
     * 渲染视图并设置交互监听
     * </p>
     *
     * @param view    目标视图容器
     * @param model   设置项数据模型
     * @param context 绑定上下文
     */
    void bind(@NonNull View view, @NonNull SettingModel model, @NonNull IBinderContext context);
}
