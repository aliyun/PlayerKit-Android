package com.aliyun.playerkit.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * Created by keria on 2022/4/6.
 * <p>
 * 安全的Toast上下文包装类
 * 用于防止Toast引起的内存泄漏和BadTokenException异常
 */
final class SafeToastContext extends ContextWrapper {
    /**
     * 构造函数，创建安全的上下文包装器
     *
     * @param base 基础Context对象
     */
    SafeToastContext(@NonNull Context base) {
        super(base);
    }

    /**
     * 获取应用级别的Context包装器
     *
     * @return 应用级别的Context包装器实例
     */
    @Override
    public Context getApplicationContext() {
        return new ApplicationContextWrapper(getBaseContext().getApplicationContext());
    }

    /**
     * 应用级别的Context包装器内部类
     */
    private static final class ApplicationContextWrapper extends ContextWrapper {

        /**
         * 构造函数，创建应用级别的Context包装器
         *
         * @param base 基础Context对象
         */
        private ApplicationContextWrapper(@NonNull Context base) {
            super(base);
        }

        /**
         * 获取系统服务，对WindowManager进行特殊处理
         *
         * @param name 系统服务名称
         * @return 系统服务实例
         */
        @Override
        public Object getSystemService(@NonNull String name) {
            if (Context.WINDOW_SERVICE.equals(name)) {
                return new WindowManagerWrapper((WindowManager) getBaseContext().getSystemService(name));
            }
            return super.getSystemService(name);
        }
    }

    /**
     * WindowManager包装器，用于处理Toast相关的窗口管理操作
     */
    private static final class WindowManagerWrapper implements WindowManager {

        private static final String TAG = "WindowManagerWrapper";
        /**
         * 基础WindowManager实例
         */
        @NonNull
        private final WindowManager base;

        /**
         * 构造函数，创建WindowManager包装器
         *
         * @param base 基础WindowManager实例
         */
        private WindowManagerWrapper(@NonNull WindowManager base) {
            this.base = base;
        }

        /**
         * 获取默认显示设备
         *
         * @return 默认显示设备实例
         */
        @Override
        public Display getDefaultDisplay() {
            return base.getDefaultDisplay();
        }

        /**
         * 立即移除视图
         *
         * @param view 要移除的视图
         */
        @Override
        public void removeViewImmediate(View view) {
            base.removeViewImmediate(view);
        }

        /**
         * 添加视图到窗口，捕获可能的异常
         *
         * @param view   要添加的视图
         * @param params 布局参数
         */
        @Override
        public void addView(View view, ViewGroup.LayoutParams params) {
            try {
                Log.d(TAG, "WindowManager's addView(view, params) has been hooked.");
                base.addView(view, params);
            } catch (BadTokenException e) {
                Log.i(TAG, e.getMessage());
            } catch (Throwable throwable) {
                Log.e(TAG, throwable.toString());
            }
        }

        /**
         * 更新视图布局
         *
         * @param view   要更新的视图
         * @param params 新的布局参数
         */
        @Override
        public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
            base.updateViewLayout(view, params);
        }

        /**
         * 移除视图
         *
         * @param view 要移除的视图
         */
        @Override
        public void removeView(View view) {
            base.removeView(view);
        }
    }
}