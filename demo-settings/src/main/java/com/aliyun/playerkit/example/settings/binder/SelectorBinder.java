package com.aliyun.playerkit.example.settings.binder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.aliyun.playerkit.example.settings.R;
import com.aliyun.playerkit.example.settings.SettingModel;

import java.util.List;

/**
 * 选择器项绑定器
 * <p>
 * 职责：展示当前选中项，并在点击时弹出 {@link AlertDialog} 进行单选操作
 * </p>
 * <p>
 * Selector Item Binder
 * <p>
 * Responsibility: Displays the currently selected item and pops up an {@link AlertDialog} for single-choice operations when clicked.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class SelectorBinder implements ISettingBinder {

    /**
     * 执行特定业务绑定
     * <p>
     * 设置当前选中项文本，并设置弹窗触发点击监听。
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

        // 设置当前选中项文本
        TextView tvSelected = view.findViewById(R.id.tvSelected);
        tvSelected.setText(model.getValue());

        // 添加点击事件
        view.setOnClickListener(v -> showSelectorDialog(view, model, context));
    }

    /**
     * 显示选择弹窗
     * <p>
     * 构造并展示单选列表弹窗。
     * </p>
     *
     * @param view    视图
     * @param model   数据模型
     * @param context 上下文
     */
    private void showSelectorDialog(View view, SettingModel model, IBinderContext context) {
        List<String> options = model.getOptions();
        if (options == null || options.isEmpty()) return;

        String[] optionsArray = options.toArray(new String[0]);
        String currentValue = model.getValue();
        int checkedItem = options.indexOf(currentValue);

        new AlertDialog.Builder(view.getContext())
                .setTitle(model.getTitle())
                .setSingleChoiceItems(optionsArray, checkedItem, (dialog, which) -> {
                    String newValue = options.get(which);
                    model.setValue(newValue);
                    context.notifyItemChanged(model);

                    if (model.getOnValueChangedListener() != null) {
                        model.getOnValueChangedListener().onValueChanged(model, newValue);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
