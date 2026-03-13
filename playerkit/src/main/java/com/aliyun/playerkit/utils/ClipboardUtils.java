package com.aliyun.playerkit.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 剪贴板工具类
 * <p>
 * 提供系统剪贴板操作的便捷方法，用于将文本内容复制到系统剪贴板。
 * 封装了 ClipboardManager 的常用操作，简化剪贴板使用。
 * </p>
 * <p>
 * Clipboard Utility Class
 * <p>
 * Provides convenient methods for system clipboard operations, used to copy text content to the system clipboard.
 * Encapsulates common ClipboardManager operations to simplify clipboard usage.
 * </p>
 *
 * @author keria
 * @date 2026/1/6
 */
public final class ClipboardUtils {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private ClipboardUtils() {
        throw new UnsupportedOperationException("Cannot instantiate ClipboardUtils");
    }

    /**
     * 将文本复制到系统剪贴板
     * <p>
     * 将指定的文本内容复制到系统剪贴板，并设置剪贴板标签。
     * 如果 context 为 null、text 为 null 或空字符串，则不进行任何操作并返回 false。
     * </p>
     * <p>
     * Copy text to system clipboard
     * <p>
     * Copies the specified text content to the system clipboard and sets the clipboard label.
     * If context is null, or text is null or empty, no operation is performed and returns false.
     * </p>
     *
     * @param context 上下文，不能为 null
     * @param label   剪贴板标签，用于标识剪贴板内容
     * @param text    要复制的文本，如果为 null 或空字符串则不处理
     * @return true 表示复制成功，false 表示复制失败（context 为 null、text 为空或 ClipboardManager 不可用）
     */
    public static boolean copyText(@NonNull Context context, @Nullable String label, @Nullable String text) {
        if (context == null || StringUtil.isEmpty(text)) {
            return false;
        }

        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            return false;
        }

        cm.setPrimaryClip(ClipData.newPlainText(label, text));
        return true;
    }
}
