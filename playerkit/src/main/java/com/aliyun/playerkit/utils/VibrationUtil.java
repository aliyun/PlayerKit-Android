package com.aliyun.playerkit.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.logging.LogHub;

/**
 * 震动工具类
 * <p>
 * 提供设备震动功能，支持不同强度的震动效果。
 * 兼容 Android 8.0 (API 26) 及以上版本的震动 API。
 * </p>
 * <p>
 * Vibration Utility Class
 * <p>
 * Provides device vibration functionality with support for different vibration intensities.
 * Compatible with vibration API for Android 8.0 (API 26) and above.
 * </p>
 *
 * @author keria
 * @date 2025/12/09
 */
public final class VibrationUtil {

    private static final String TAG = "VibrationUtil";

    /**
     * 默认震动时长（毫秒）
     */
    private static final long DEFAULT_DURATION_MS = 100;

    /**
     * 轻震动时长（毫秒）
     */
    private static final long LIGHT_DURATION_MS = 50;

    /**
     * 中等震动时长（毫秒）
     */
    private static final long MEDIUM_DURATION_MS = 100;

    /**
     * 强震动时长（毫秒）
     */
    private static final long HEAVY_DURATION_MS = 150;

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private VibrationUtil() {
        throw new UnsupportedOperationException("Cannot instantiate VibrationUtil");
    }

    /**
     * 执行默认震动
     * <p>
     * 使用默认震动时长进行震动。
     * </p>
     * <p>
     * Perform default vibration
     * <p>
     * Vibrate with default duration.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     */
    public static void vibrate(@NonNull Context context) {
        vibrate(context, DEFAULT_DURATION_MS);
    }

    /**
     * 执行指定时长的震动
     * <p>
     * 根据指定的时长进行震动。
     * </p>
     * <p>
     * Perform vibration with specified duration
     * <p>
     * Vibrate for the specified duration.
     * </p>
     *
     * @param context  上下文对象，不能为 null
     * @param duration 震动时长，单位：毫秒
     */
    public static void vibrate(@NonNull Context context, long duration) {
        Vibrator vibrator = getVibrator(context);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上使用 VibrationEffect
                VibrationEffect effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                // Android 8.0 以下使用旧 API
                vibrator.vibrate(duration);
            }
        } catch (Exception e) {
            // 忽略震动异常，避免影响主流程
            LogHub.w(TAG, "Vibration failed: " + e.getMessage());
        }
    }

    /**
     * 执行轻震动
     * <p>
     * 使用较短的震动时长，适合轻微反馈。
     * </p>
     * <p>
     * Perform light vibration
     * <p>
     * Use shorter vibration duration, suitable for light feedback.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     */
    public static void vibrateLight(@NonNull Context context) {
        vibrate(context, LIGHT_DURATION_MS);
    }

    /**
     * 执行中等震动
     * <p>
     * 使用中等震动时长，适合一般操作反馈。
     * </p>
     * <p>
     * Perform medium vibration
     * <p>
     * Use medium vibration duration, suitable for general operation feedback.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     */
    public static void vibrateMedium(@NonNull Context context) {
        vibrate(context, MEDIUM_DURATION_MS);
    }

    /**
     * 执行强震动
     * <p>
     * 使用较长的震动时长，适合重要操作反馈。
     * </p>
     * <p>
     * Perform heavy vibration
     * <p>
     * Use longer vibration duration, suitable for important operation feedback.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     */
    public static void vibrateHeavy(@NonNull Context context) {
        vibrate(context, HEAVY_DURATION_MS);
    }

    /**
     * 获取震动器实例
     * <p>
     * 根据 Android 版本获取合适的震动器实例。
     * Android 12 (API 31) 及以上使用 VibratorManager，其他版本使用 Vibrator。
     * </p>
     * <p>
     * Get vibrator instance
     * <p>
     * Get appropriate vibrator instance based on Android version.
     * Android 12 (API 31) and above use VibratorManager, other versions use Vibrator.
     * </p>
     *
     * @param context 上下文对象
     * @return 震动器实例，如果获取失败则返回 null
     */
    private static Vibrator getVibrator(@NonNull Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 (API 31) 及以上使用 VibratorManager
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                return vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
            } else {
                // Android 12 以下使用 Vibrator
                return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to get vibrator: " + e.getMessage());
            return null;
        }
    }
}
