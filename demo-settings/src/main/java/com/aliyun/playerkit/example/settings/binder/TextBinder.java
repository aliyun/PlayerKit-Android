package com.aliyun.playerkit.example.settings.binder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.example.settings.R;
import com.aliyun.playerkit.example.settings.SettingModel;
import com.aliyun.playerkit.utils.ClipboardUtils;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.ToastUtils;

/**
 * 文本展示项绑定器
 * <p>
 * 职责：渲染标题和副标题，通常用于显示只读的状态信息。默认支持点击复制。
 * </p>
 * <p>
 * Text Item Binder
 * <p>
 * Responsibility: Renders the title and subtitle, usually used to display read-only status information. Supports click-to-copy by default.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class TextBinder implements ISettingBinder {
    /**
     * 执行特定业务绑定
     * <p>
     * 设置副标题内容。
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

        // 设置副标题
        TextView tvValue = view.findViewById(R.id.tvValue);
        String itemValue = model.getValue();
        if (StringUtil.isNotEmpty(itemValue)) {
            tvValue.setVisibility(View.VISIBLE);
            tvValue.setText(itemValue);
        } else {
            tvValue.setVisibility(View.GONE);
        }

        // 添加点击事件
        view.setOnClickListener(v -> {
            if (model.getOnClickListener() != null) {
                model.getOnClickListener().onClick(model);
            } else {
                // 默认点击复制
                copyToClipboard(v.getContext(), itemValue);
            }
        });
    }

    /**
     * 将指定文本复制到系统剪贴板，并显示提示 Toast。
     *
     * @param context 用于获取剪贴板服务和显示 Toast 的上下文
     * @param text    要复制的文本，为 null 或空字符串时不处理
     */
    private void copyToClipboard(Context context, String text) {
        if (StringUtil.isEmpty(text)) {
            return;
        }

        // 复制文本到剪贴板
        ClipboardUtils.copyText(context, "SettingValue", text);

        ToastUtils.showToast(R.string.setting_toast_copied);
    }
}
