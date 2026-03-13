package com.aliyun.playerkit.example.settings.binder;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.example.settings.R;
import com.aliyun.playerkit.example.settings.SettingModel;

/**
 * 开关项绑定器
 * <p>
 * 职责：同步开关状态，处理切换事件，并双向更新内存模型
 * </p>
 * <p>
 * Switch Item Binder
 * <p>
 * Responsibility: Synchronizes the switch state, handles switch events, and updates the memory model bi-directionally.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class SwitchBinder implements ISettingBinder {
    /**
     * 执行特定业务绑定
     * <p>
     * 设置开关状态及其变更监听
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

        // 设置开关状态
        final Switch switchCompat = view.findViewById(R.id.switch_compat);
        switchCompat.setOnCheckedChangeListener(null);
        switchCompat.setChecked(model.isChecked());
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.setChecked(isChecked);
            context.notifyItemChanged(model);
            if (model.getOnValueChangedListener() != null) {
                model.getOnValueChangedListener().onValueChanged(model, isChecked);
            }
        });

        // 添加点击事件
        view.setOnClickListener(v -> switchCompat.toggle());
    }
}
