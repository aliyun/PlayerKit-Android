package com.aliyun.playerkit.example.settings.binder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.example.settings.R;
import com.aliyun.playerkit.example.settings.SettingModel;

/**
 * 头布局绑定器
 * <p>
 * 职责：仅渲染设置菜单的分组标题。
 * </p>
 * <p>
 * Header Layout Binder
 * <p>
 * Responsibility: Only renders the grouping title of the settings menu.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class HeaderBinder implements ISettingBinder {
    /**
     * 执行特定业务绑定
     *
     * @param view    根视图
     * @param model   数据模型
     * @param context 交互上下文
     */
    @Override
    public void bind(@NonNull View view, @NonNull SettingModel model, @NonNull IBinderContext context) {
        // 设置标题
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(model.getTitle());
    }
}
