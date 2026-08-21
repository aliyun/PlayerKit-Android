package com.aliyun.playerkit.scenes.aieducation.ui;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;

/**
 * Markdown 渲染绑定器。
 * <p>
 * 封装 Markwon 实例的创建和使用，提供统一的渲染入口。
 * <ul>
 *     <li>单例复用 Markwon 实例，避免重复创建开销</li>
 *     <li>渲染失败时降级为纯文本显示，防止崩溃</li>
 *     <li>按需注册插件（HTML、Table、Strikethrough）</li>
 * </ul>
 *
 * @author keria
 * @date 2026/07/03
 */
public final class MarkdownBinder {

    private final Markwon markwon;

    public MarkdownBinder(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        markwon = Markwon.builder(appContext)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TablePlugin.create(appContext))
                .usePlugin(StrikethroughPlugin.create())
                .build();
    }

    /**
     * 将 Markdown 文本渲染到 TextView。
     *
     * @param textView 目标 TextView
     * @param markdown Markdown 文本，为空时清空 TextView
     */
    public void bind(@NonNull TextView textView, @Nullable String markdown) {
        if (TextUtils.isEmpty(markdown)) {
            textView.setText("");
            return;
        }
        try {
            markwon.setMarkdown(textView, markdown);
        } catch (Throwable e) {
            // 降级：渲染异常时显示纯文本，避免 item 空白或崩溃
            textView.setText(markdown);
        }
    }

    /**
     * 清空 TextView 内容。用于 RecyclerView 回收时避免复用闪烁。
     */
    public void clear(@NonNull TextView textView) {
        textView.setText(null);
    }
}
