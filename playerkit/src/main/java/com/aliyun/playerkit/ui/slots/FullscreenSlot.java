package com.aliyun.playerkit.ui.slots;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.controller.PlayerStateStore;
import com.aliyun.playerkit.event.FullscreenEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.ContextUtil;

import java.util.Collections;
import java.util.List;

/**
 * 全屏管理插槽
 * <p>
 * 负责管理播放器的全屏切换逻辑，包括进入/退出全屏、设置屏幕方向、沉浸式全屏设置等。
 * 这是一个纯逻辑管理的插槽，不渲染任何 UI 内容。
 * </p>
 * <p>
 * 详细说明请参考 {@link com.aliyun.playerkit.slot.SlotType#FULLSCREEN}。
 * </p>
 * <p>
 * Fullscreen Management Slot
 * <p>
 * Manages fullscreen switching logic for the player, including entering/exiting fullscreen, setting screen orientation, immersive fullscreen settings, etc.
 * This is a pure logic management slot that does not render any UI content.
 * </p>
 * <p>
 * For detailed information, please refer to {@link com.aliyun.playerkit.slot.SlotType#FULLSCREEN}.
 * </p>
 *
 * @author keria
 * @date 2025/12/30
 */
public class FullscreenSlot extends BaseSlot {

    private static final String TAG = "FullscreenSlot";

    /**
     * 播放器组件的宿主视图
     */
    @Nullable
    private View hostView;

    /**
     * 原始父容器（非全屏时的父容器）
     */
    @Nullable
    private ViewGroup originalParent;

    /**
     * 原始布局参数（非全屏时的布局参数）
     */
    @Nullable
    private ViewGroup.LayoutParams originalLayoutParams;

    /**
     * 原始 Activity 方向（非全屏时的屏幕方向）
     */
    private int originalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    /**
     * 是否处于全屏状态
     */
    private boolean isFullscreen = false;

    /**
     * 等待设置横屏的标记
     * <p>
     * 当设置为 true 时，表示正在等待系统 UI 隐藏后再设置横屏方向。
     * </p>
     */
    private boolean pendingLandscapeOrientation = false;

    /**
     * 系统 UI 可见性变化监听器
     * <p>
     * 用于监听系统 UI 的隐藏状态，确保在系统 UI 真正隐藏后再设置横屏方向。
     * </p>
     */
    private final View.OnSystemUiVisibilityChangeListener systemUiVisibilityChangeListener = visibility -> {
        // 检查系统 UI 是否已隐藏
        boolean isSystemUiHidden = (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
                && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;

        if (isSystemUiHidden && pendingLandscapeOrientation) {
            // 系统 UI 已隐藏，现在可以安全地设置横屏方向
            // 注意：如果 Activity 未配置 configChanges="orientation|screenSize"，会导致 Activity 重建
            Activity activity = ContextUtil.getActivity(getContext());
            if (activity != null) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                pendingLandscapeOrientation = false;
                LogHub.i(TAG, "System UI hidden, setting landscape orientation");
            }
        }
    };

    public FullscreenSlot(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 获取宿主视图
        hostView = host.getHostView();
    }

    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Collections.singletonList(FullscreenEvents.Toggle.class);
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof FullscreenEvents.Toggle) {
            onToggleFullscreen((FullscreenEvents.Toggle) event);
        }
    }

    /**
     * 处理切换全屏事件
     */
    private void onToggleFullscreen(@NonNull FullscreenEvents.Toggle event) {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    /**
     * 进入全屏模式
     */
    private void enterFullscreen() {
        if (hostView == null) {
            LogHub.w(TAG, "HostView is null, cannot enter fullscreen");
            return;
        }

        Activity activity = ContextUtil.getActivity(getContext());
        if (activity == null) {
            LogHub.w(TAG, "Activity is null, cannot enter fullscreen");
            return;
        }

        LogHub.i(TAG, "Entering fullscreen mode");

        // 保存原始状态
        originalParent = (ViewGroup) hostView.getParent();
        originalLayoutParams = hostView.getLayoutParams();
        originalOrientation = activity.getRequestedOrientation();

        // 从原父容器中移除
        if (originalParent != null) {
            originalParent.removeView(hostView);
        }

        // 将播放器 View 添加到 Activity 的根布局
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            rootView.addView(this.hostView, params);
        } else {
            // rootView 为 null，恢复到原始父容器
            LogHub.w(TAG, "Root view is null, restoring to original parent");
            if (originalParent != null && originalLayoutParams != null) {
                originalParent.addView(hostView, originalLayoutParams);
            }
            return;
        }

        // 先设置沉浸式全屏，并监听系统 UI 隐藏
        // 这样可以避免出现"横屏但导航栏可见"的中间状态
        Window window = activity.getWindow();
        if (window != null) {
            View decorView = window.getDecorView();
            // 设置系统 UI 可见性变化监听器
            decorView.setOnSystemUiVisibilityChangeListener(systemUiVisibilityChangeListener);

            // 设置沉浸式全屏
            setImmersiveFullscreen(activity, true);

            // 标记等待系统 UI 隐藏后再设置横屏方向
            pendingLandscapeOrientation = true;

            // 如果系统 UI 已经隐藏（可能在某些设备上立即生效），立即设置横屏方向
            // 注意：如果 Activity 未配置 configChanges="orientation|screenSize"，会导致 Activity 重建
            int currentVisibility = decorView.getSystemUiVisibility();
            boolean isSystemUiHidden = (currentVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
                    && (currentVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
            if (isSystemUiHidden) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                pendingLandscapeOrientation = false;
                LogHub.i(TAG, "System UI already hidden, setting landscape orientation immediately");
            }
        } else {
            // 如果无法获取 Window，直接设置横屏方向（降级方案）
            // 注意：如果 Activity 未配置 configChanges="orientation|screenSize"，会导致 Activity 重建
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            LogHub.w(TAG, "Window is null, setting landscape orientation directly");
        }

        // 更新状态
        isFullscreen = true;
        updateFullscreenState(true);

        LogHub.i(TAG, "Entered fullscreen mode");
    }

    /**
     * 退出全屏模式
     */
    private void exitFullscreen() {
        if (hostView == null) {
            LogHub.w(TAG, "HostView is null, cannot exit fullscreen");
            return;
        }

        Activity activity = ContextUtil.getActivity(getContext());
        if (activity == null) {
            LogHub.w(TAG, "Activity is null, cannot exit fullscreen");
            return;
        }

        LogHub.i(TAG, "Exiting fullscreen mode");

        // 清除等待横屏标记
        pendingLandscapeOrientation = false;

        // 移除系统 UI 可见性变化监听器
        Window window = activity.getWindow();
        if (window != null) {
            View decorView = window.getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(null);
        }

        // 先恢复 Activity 的屏幕方向（在取消沉浸式全屏之前）
        // 这样可以避免出现"竖屏但导航栏隐藏"的中间状态
        // 注意：如果 Activity 未配置 configChanges="orientation|screenSize"，会导致 Activity 重建
        if (originalOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.setRequestedOrientation(originalOrientation);
            LogHub.i(TAG, "Restored orientation to: " + originalOrientation);
        } else {
            // 如果没有保存原始方向，恢复为竖屏
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            LogHub.i(TAG, "Restored orientation to portrait (default)");
        }

        // 然后取消沉浸式全屏（显示导航栏）
        setImmersiveFullscreen(activity, false);

        // 从当前父容器中移除
        ViewGroup currentParent = (ViewGroup) hostView.getParent();
        if (currentParent != null) {
            currentParent.removeView(hostView);
        }

        // 恢复到原始父容器
        if (originalParent != null && originalLayoutParams != null) {
            originalParent.addView(hostView, originalLayoutParams);
        }

        // 更新状态
        isFullscreen = false;
        updateFullscreenState(false);

        LogHub.i(TAG, "Exited fullscreen mode");
    }

    /**
     * 设置沉浸式全屏
     */
    private void setImmersiveFullscreen(@NonNull Activity activity, boolean enabled) {
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }

        View decorView = window.getDecorView();
        int flags;

        if (enabled) {
            // 进入全屏：隐藏状态栏和导航栏
            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        } else {
            // 退出全屏：显示状态栏和导航栏
            flags = View.SYSTEM_UI_FLAG_VISIBLE;
        }

        decorView.setSystemUiVisibility(flags);

        // 设置窗口标志
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 更新全屏状态
     */
    private void updateFullscreenState(boolean fullscreen) {
        try {
            SlotHost host = getHost();
            if (host != null) {
                PlayerStateStore stateStore = (PlayerStateStore) host.getPlayerStateStore();
                stateStore.updateFullscreen(fullscreen);
            }
        } catch (Exception e) {
            // 播放器可能已被销毁，忽略状态更新
            LogHub.w(TAG, "Cannot update fullscreen state: " + e.getMessage());
        }
    }

    /**
     * 处理返回键按下事件
     * <p>
     * 在 Activity 的 onBackPressed 中调用此方法。
     * 如果当前处于全屏状态，则退出全屏并返回 true（表示已处理）。
     * 否则返回 false（表示未处理，由 Activity 自行处理）。
     * </p>
     *
     * @return true 如果已处理返回键（退出全屏），false 否则
     */
    public boolean onBackPressed() {
        if (isFullscreen) {
            LogHub.i(TAG, "Back pressed in fullscreen mode, exiting fullscreen");
            exitFullscreen();
            return true; // 已处理，阻止 Activity 的默认行为
        }
        return false; // 未处理，由 Activity 自行处理
    }

    @Override
    public void onDetach() {
        // 1. 清理系统 UI 监听器
        Activity activity = ContextUtil.getActivity(getContext());
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                View decorView = window.getDecorView();
                decorView.setOnSystemUiVisibilityChangeListener(null);
            }
        }

        // 2. 重置内部状态
        originalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        isFullscreen = false;
        pendingLandscapeOrientation = false;

        // 3. 清空引用（可选）
        hostView = null;
        originalParent = null;
        originalLayoutParams = null;

        super.onDetach();
    }
}
