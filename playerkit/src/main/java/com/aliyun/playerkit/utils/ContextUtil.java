package com.aliyun.playerkit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 上下文工具类
 * <p>
 * 提供 Context 相关的工具方法，如从 Context 中提取 Activity 等。
 * </p>
 * <p>
 * Context Utility Class
 * <p>
 * Provides Context-related utility methods, such as extracting Activity from Context.
 * </p>
 *
 * @author keria
 * @date 2025/12/09
 */
public final class ContextUtil {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private ContextUtil() {
        throw new UnsupportedOperationException("Cannot instantiate ContextUtil");
    }

    /**
     * 从 Context 中提取 Activity 实例
     * <p>
     * 通过遍历 ContextWrapper 链来查找 Activity 实例。
     * 如果 Context 本身就是 Activity，直接返回；如果是 ContextWrapper，则递归查找其 baseContext。
     * </p>
     * <p>
     * Extract Activity instance from Context
     * <p>
     * Finds Activity instance by traversing the ContextWrapper chain.
     * If Context is itself an Activity, returns directly; if it's a ContextWrapper, recursively searches its baseContext.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @return Activity 实例，如果无法获取则返回 null
     */
    @Nullable
    public static Activity getActivity(@NonNull Context context) {
        Context ctx = context;

        while (ctx != null) {
            if (ctx instanceof Activity) {
                return (Activity) ctx;
            }

            if (ctx instanceof ContextWrapper) {
                ctx = ((ContextWrapper) ctx).getBaseContext();
            } else {
                break;
            }
        }

        return null;
    }
}
