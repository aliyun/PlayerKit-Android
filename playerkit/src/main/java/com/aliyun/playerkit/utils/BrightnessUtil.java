package com.aliyun.playerkit.utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

import com.aliyun.playerkit.logging.LogHub;

/**
 * 屏幕亮度工具类
 * <p>
 * 提供屏幕亮度调整功能，支持获取和设置亮度。
 * 亮度值范围为 0 到 255，或使用百分比 0.0 到 1.0。
 * </p>
 * <p>
 * Brightness Utility Class
 * <p>
 * Provides screen brightness adjustment functionality, supports getting and setting brightness.
 * Brightness value ranges from 0 to 255, or use percentage 0.0 to 1.0.
 * </p>
 *
 * @author keria
 * @date 2025/12/09
 */
public final class BrightnessUtil {

    private static final String TAG = "BrightnessUtil";

    /**
     * 系统亮度最小值
     */
    private static final int MIN_BRIGHTNESS = 0;

    /**
     * 系统亮度最大值
     */
    private static final int MAX_BRIGHTNESS = 255;

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private BrightnessUtil() {
        throw new UnsupportedOperationException("Cannot instantiate BrightnessUtil");
    }

    /**
     * 获取系统亮度
     * <p>
     * 从系统设置中获取屏幕亮度值。
     * </p>
     * <p>
     * Get System Brightness
     * <p>
     * Gets screen brightness value from system settings.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @return 系统亮度值，范围 0 到 255，如果获取失败则返回 -1
     */
    @IntRange(from = -1, to = 255)
    public static int getSystemBrightness(@NonNull Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to get system brightness: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 设置系统亮度
     * <p>
     * 设置系统屏幕亮度值（需要 WRITE_SETTINGS 权限）。
     * 注意：Android 6.0+ 需要用户手动授予权限。
     * </p>
     * <p>
     * Set System Brightness
     * <p>
     * Sets system screen brightness value (requires WRITE_SETTINGS permission).
     * Note: Android 6.0+ requires user to manually grant permission.
     * </p>
     *
     * @param context    上下文对象，不能为 null
     * @param brightness 亮度值，范围 0 到 255
     * @return true 表示设置成功，false 表示设置失败
     */
    public static boolean setSystemBrightness(@NonNull Context context, @IntRange(from = 0, to = 255) int brightness) {
        try {
            int clampedBrightness = Math.max(MIN_BRIGHTNESS, Math.min(brightness, MAX_BRIGHTNESS));
            return Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, clampedBrightness);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to set system brightness: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取窗口亮度
     * <p>
     * 获取当前 Activity 窗口的亮度值。
     * 如果窗口亮度未设置，返回 -1.0（使用系统亮度）。
     * </p>
     * <p>
     * Get Window Brightness
     * <p>
     * Gets the brightness value of current Activity window.
     * If window brightness is not set, returns -1.0 (uses system brightness).
     * </p>
     *
     * @param activity Activity 对象，不能为 null
     * @return 窗口亮度值，范围 -1.0 到 1.0，-1.0 表示使用系统亮度
     */
    @FloatRange(from = -1.0f, to = 1.0f)
    public static float getWindowBrightness(@NonNull Activity activity) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return -1.0f;
            }

            WindowManager.LayoutParams params = window.getAttributes();
            return params.screenBrightness;
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to get window brightness: " + e.getMessage());
            return -1.0f;
        }
    }

    /**
     * 设置窗口亮度
     * <p>
     * 设置当前 Activity 窗口的亮度值（仅影响当前窗口，不影响系统设置）。
     * 亮度值范围：-1.0 到 1.0
     * - -1.0：使用系统亮度
     * - 0.0：最暗
     * - 1.0：最亮
     * </p>
     * <p>
     * Set Window Brightness
     * <p>
     * Sets the brightness value of current Activity window (only affects current window, does not affect system settings).
     * Brightness value range: -1.0 to 1.0
     * - -1.0: Use system brightness
     * - 0.0: Darkest
     * - 1.0: Brightest
     * </p>
     *
     * @param activity   Activity 对象，不能为 null
     * @param brightness 亮度值，范围 -1.0 到 1.0
     */
    public static void setWindowBrightness(@NonNull Activity activity, @FloatRange(from = -1.0f, to = 1.0f) float brightness) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }

            // 避免传入介于 (-1,0) 的值触发系统默认亮度导致闪烁：只有明确 -1 时才回退系统亮度
            float clampedBrightness = brightness < 0f ? -1.0f : Math.max(0.0f, Math.min(brightness, 1.0f));

            WindowManager.LayoutParams params = window.getAttributes();
            params.screenBrightness = clampedBrightness;
            window.setAttributes(params);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to set window brightness: " + e.getMessage());
        }
    }

    /**
     * 设置窗口亮度（通过 Context）
     * <p>
     * 从 Context 中提取 Activity 并设置窗口亮度。
     * 如果无法提取 Activity，则记录警告但不报错。
     * </p>
     * <p>
     * Set Window Brightness (via Context)
     * <p>
     * Extracts Activity from Context and sets window brightness.
     * If Activity cannot be extracted, logs a warning but does not throw an error.
     * </p>
     *
     * @param context    上下文对象，不能为 null
     * @param brightness 亮度值，范围 -1.0 到 1.0
     */
    public static void setWindowBrightness(@NonNull Context context, @FloatRange(from = -1.0f, to = 1.0f) float brightness) {
        Activity activity = ContextUtil.getActivity(context);
        if (activity == null) {
            LogHub.w(TAG, "Cannot set window brightness, Activity not found in Context");
            return;
        }

        setWindowBrightness(activity, brightness);
    }

    /**
     * 设置窗口亮度（使用系统亮度值）
     * <p>
     * 将系统亮度值（0-255）转换为窗口亮度值（0.0-1.0）并设置。
     * </p>
     * <p>
     * Set Window Brightness (Using System Brightness Value)
     * <p>
     * Converts system brightness value (0-255) to window brightness value (0.0-1.0) and sets it.
     * </p>
     *
     * @param activity   Activity 对象，不能为 null
     * @param brightness 系统亮度值，范围 0 到 255
     */
    public static void setWindowBrightnessFromSystem(@NonNull Activity activity, @IntRange(from = 0, to = 255) int brightness) {
        int clampedBrightness = Math.max(MIN_BRIGHTNESS, Math.min(brightness, MAX_BRIGHTNESS));
        float windowBrightness = clampedBrightness / (float) MAX_BRIGHTNESS;
        setWindowBrightness(activity, windowBrightness);
    }

    /**
     * 获取当前有效亮度（私有辅助方法）
     */
    private static float getCurrentEffectiveBrightness(@NonNull Activity activity) {
        float currentBrightness = getWindowBrightness(activity);
        // 如果当前使用系统亮度，先获取系统亮度并转换
        if (currentBrightness < 0) {
            Context context = activity.getApplicationContext();
            int systemBrightness = getSystemBrightness(context);
            if (systemBrightness >= 0) {
                return systemBrightness / (float) MAX_BRIGHTNESS;
            } else {
                return 0.5f; // 默认使用中等亮度
            }
        }
        return currentBrightness;
    }

    /**
     * 调整窗口亮度（相对值）
     * <p>
     * 在当前窗口亮度的基础上增加或减少指定的亮度值。
     * 正数表示增加亮度，负数表示减少亮度。
     * </p>
     * <p>
     * Adjust Window Brightness (Relative)
     * <p>
     * Increases or decreases brightness by the specified value based on current window brightness.
     * Positive value increases brightness, negative value decreases brightness.
     * </p>
     *
     * @param activity Activity 对象，不能为 null
     * @param delta    亮度变化量，范围 -1.0 到 1.0，正数增加，负数减少
     */
    public static void adjustWindowBrightness(@NonNull Activity activity, float delta) {
        float currentBrightness = getCurrentEffectiveBrightness(activity);
        float newBrightness = currentBrightness + delta;
        // 亮度调整只在 [0,1] 范围内变化，避免落入 (-1,0) 触发系统默认亮度导致闪屏
        newBrightness = Math.max(0.0f, Math.min(newBrightness, 1.0f));
        setWindowBrightness(activity, newBrightness);
    }

    /**
     * 调整窗口亮度（相对值，通过 Context）
     * <p>
     * 从 Context 中提取 Activity 并调整窗口亮度。
     * 如果无法提取 Activity，则尝试通过 View 的 context 来获取。
     * 如果仍然无法获取，则记录警告但不报错。
     * </p>
     * <p>
     * Adjust Window Brightness (Relative, via Context)
     * <p>
     * Extracts Activity from Context and adjusts window brightness.
     * If Activity cannot be extracted, tries to get it from View's context.
     * If still cannot be obtained, logs a warning but does not throw an error.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @param delta   亮度变化量，范围 -1.0 到 1.0，正数增加，负数减少
     */
    public static void adjustWindowBrightness(@NonNull Context context, float delta) {
        Activity activity = ContextUtil.getActivity(context);
        if (activity == null) {
            LogHub.w(TAG, "Cannot adjust window brightness, Activity not found in Context. " + "If context is ApplicationContext, please use adjustWindowBrightness(View, float) instead.");
            return;
        }

        adjustWindowBrightness(activity, delta);
    }

    /**
     * 调整窗口亮度（相对值，通过 View）
     * <p>
     * 从 View 的 context 中提取 Activity 并调整窗口亮度。
     * View 的 context 通常是 Activity Context，可以成功提取 Activity。
     * </p>
     * <p>
     * Adjust Window Brightness (Relative, via View)
     * <p>
     * Extracts Activity from View's context and adjusts window brightness.
     * View's context is usually Activity Context, so Activity can be successfully extracted.
     * </p>
     *
     * @param view  View 对象，不能为 null
     * @param delta 亮度变化量，范围 -1.0 到 1.0，正数增加，负数减少
     */
    public static void adjustWindowBrightness(@NonNull View view, float delta) {
        Context viewContext = view.getContext();
        Activity activity = ContextUtil.getActivity(viewContext);
        if (activity == null) {
            LogHub.w(TAG, "Cannot adjust window brightness, Activity not found in View's context");
            return;
        }

        adjustWindowBrightness(activity, delta);
    }

    /**
     * 增加窗口亮度
     * <p>
     * 在当前窗口亮度的基础上增加 0.1 个单位。
     * </p>
     * <p>
     * Increase Window Brightness
     * <p>
     * Increases window brightness by 0.1 unit based on current brightness.
     * </p>
     *
     * @param activity Activity 对象，不能为 null
     */
    public static void increaseWindowBrightness(@NonNull Activity activity) {
        adjustWindowBrightness(activity, 0.1f);
    }

    /**
     * 减少窗口亮度
     * <p>
     * 在当前窗口亮度的基础上减少 0.1 个单位。
     * </p>
     * <p>
     * Decrease Window Brightness
     * <p>
     * Decreases window brightness by 0.1 unit based on current brightness.
     * </p>
     *
     * @param activity Activity 对象，不能为 null
     */
    public static void decreaseWindowBrightness(@NonNull Activity activity) {
        adjustWindowBrightness(activity, -0.1f);
    }

    /**
     * 设置窗口亮度百分比
     * <p>
     * 根据百分比设置窗口亮度，0.0 表示最暗，1.0 表示最亮。
     * </p>
     * <p>
     * Set Window Brightness Percentage
     * <p>
     * Sets window brightness based on percentage, 0.0 means darkest, 1.0 means brightest.
     * </p>
     *
     * @param activity   Activity 对象，不能为 null
     * @param percentage 亮度百分比，范围 0.0 到 1.0
     */
    public static void setWindowBrightnessPercentage(@NonNull Activity activity, @FloatRange(from = 0.0f, to = 1.0f) float percentage) {
        float clampedPercentage = Math.max(0.0f, Math.min(1.0f, percentage));
        setWindowBrightness(activity, clampedPercentage);
    }

    /**
     * 获取窗口亮度百分比
     * <p>
     * 获取当前窗口亮度相对于最大亮度的百分比。
     * </p>
     * <p>
     * Get Window Brightness Percentage
     * <p>
     * Gets the percentage of current window brightness relative to maximum brightness.
     * </p>
     *
     * @param activity Activity 对象，不能为 null
     * @return 亮度百分比，范围 0.0 到 1.0，如果获取失败则返回 0.0
     */
    @FloatRange(from = 0.0f, to = 1.0f)
    public static float getWindowBrightnessPercentage(@NonNull Activity activity) {
        float brightness = getCurrentEffectiveBrightness(activity);
        return Math.max(0.0f, Math.min(1.0f, brightness));
    }
}
