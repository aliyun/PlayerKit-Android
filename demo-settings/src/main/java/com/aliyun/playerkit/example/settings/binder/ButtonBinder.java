package com.aliyun.playerkit.example.settings.binder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.example.settings.R;
import com.aliyun.playerkit.example.settings.SettingModel;

/**
 * 普通按钮绑定器
 * <p>
 * 职责：渲染按钮标题并分发点击事件。
 * </p>
 * <p>
 * Normal Button Binder
 * <p>
 * Responsibility: Renders the button title and dispatches click events.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class ButtonBinder implements ISettingBinder {
    /**
     * 执行特定业务绑定
     * <p>
     * 设置点击监听并触发回调
     * </p>
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

        // 设置箭头
        View ivArrow = view.findViewById(R.id.arrowView);
        ivArrow.setVisibility(model.isShowArrow() ? View.VISIBLE : View.GONE);

        // 添加点击事件
        view.setOnClickListener(v -> {
            if (model.getOnClickListener() != null) {
                model.getOnClickListener().onClick(model);
            }
        });
    }
}
