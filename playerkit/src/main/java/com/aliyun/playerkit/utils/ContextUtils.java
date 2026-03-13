package com.aliyun.playerkit.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Created by keria on 2022/4/6.
 * <p>
 * 上下文工具类，用于全局访问应用上下文
 * 提供安全的上下文访问方式，避免内存泄漏问题
 */
public class ContextUtils {
    /**
     * 全局静态Context引用，可能有内存泄漏风险，已通过SafeToastContext进行包装处理
     */
    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    /**
     * 安全的全局Context引用，用于Toast等操作，避免内存泄漏
     */
    @SuppressLint("StaticFieldLeak")
    private static Context sSafeContext = null;

    /**
     * 获取全局Context对象
     *
     * @return 全局Context实例
     */
    public static Context getContext() {
        return sContext;
    }

    /**
     * 设置全局Context对象，并初始化安全Context
     *
     * @param context 应用Context，不可为空
     */
    public static void setContext(@NonNull Context context) {
        sContext = context;
        sSafeContext = new SafeToastContext(context);
    }

    /**
     * 获取应用级别的Context对象
     *
     * @return 应用级别的Context实例
     */
    public static Context getApplicationContext() {
        return sContext;
    }

    /**
     * 获取安全的Toast Context对象，用于防止内存泄漏
     *
     * @return 安全的Context实例
     */
    public static Context getSafeToastContext() {
        return sSafeContext;
    }
}