package com.aliyun.playerkit.utils;

import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.annotation.IntRange;
import androidx.annotation.FloatRange;

import com.aliyun.playerkit.logging.LogHub;

/**
 * 音量工具类
 * <p>
 * 提供设备音量调整功能，支持获取和设置音量。
 * 音量值范围为 0 到最大音量值。
 * </p>
 * <p>
 * Volume Utility Class
 * <p>
 * Provides device volume adjustment functionality, supports getting and setting volume.
 * Volume value ranges from 0 to maximum volume value.
 * </p>
 *
 * @author keria
 * @date 2025/12/09
 */
public final class VolumeUtil {

    private static final String TAG = "VolumeUtil";

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private VolumeUtil() {
        throw new UnsupportedOperationException("Cannot instantiate VolumeUtil");
    }

    /**
     * 获取音频管理器
     * <p>
     * Get Audio Manager
     * </p>
     *
     * @param context 上下文对象
     * @return 音频管理器，如果获取失败则返回 null
     */
    private static AudioManager getAudioManager(@NonNull Context context) {
        try {
            return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to get AudioManager: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前音量
     * <p>
     * 获取媒体音量的当前值。
     * </p>
     * <p>
     * Get Current Volume
     * <p>
     * Gets the current value of media volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @return 当前音量值，范围 0 到最大音量值，如果获取失败则返回 0
     */
    public static int getCurrentVolume(@NonNull Context context) {
        AudioManager audioManager = getAudioManager(context);
        if (audioManager == null) {
            return 0;
        }

        try {
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to get current volume: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取最大音量
     * <p>
     * 获取媒体音量的最大值。
     * </p>
     * <p>
     * Get Maximum Volume
     * <p>
     * Gets the maximum value of media volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @return 最大音量值，如果获取失败则返回 0
     */
    public static int getMaxVolume(@NonNull Context context) {
        AudioManager audioManager = getAudioManager(context);
        if (audioManager == null) {
            return 0;
        }

        try {
            return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to get max volume: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 设置音量
     * <p>
     * 设置媒体音量的绝对值。
     * 音量值会被限制在 0 到最大音量值之间。
     * </p>
     * <p>
     * Set Volume
     * <p>
     * Sets the absolute value of media volume.
     * Volume value will be clamped between 0 and maximum volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @param volume  音量值，范围 0 到最大音量值
     */
    public static void setVolume(@NonNull Context context, @IntRange(from = 0) int volume) {
        AudioManager audioManager = getAudioManager(context);
        if (audioManager == null) {
            return;
        }

        try {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int clampedVolume = Math.max(0, Math.min(volume, maxVolume));
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clampedVolume, 0);
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to set volume: " + e.getMessage());
        }
    }

    /**
     * 调整音量（相对值）
     * <p>
     * 在当前音量的基础上增加或减少指定的音量值。
     * 正数表示增加音量，负数表示减少音量。
     * </p>
     * <p>
     * Adjust Volume (Relative)
     * <p>
     * Increases or decreases volume by the specified value based on current volume.
     * Positive value increases volume, negative value decreases volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @param delta   音量变化量，正数增加，负数减少
     */
    public static void adjustVolume(@NonNull Context context, int delta) {
        int currentVolume = getCurrentVolume(context);
        int newVolume = currentVolume + delta;
        setVolume(context, newVolume);
    }

    /**
     * 增加音量
     * <p>
     * 在当前音量的基础上增加 1 个单位。
     * </p>
     * <p>
     * Increase Volume
     * <p>
     * Increases volume by 1 unit based on current volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     */
    public static void increaseVolume(@NonNull Context context) {
        adjustVolume(context, 1);
    }

    /**
     * 减少音量
     * <p>
     * 在当前音量的基础上减少 1 个单位。
     * </p>
     * <p>
     * Decrease Volume
     * <p>
     * Decreases volume by 1 unit based on current volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     */
    public static void decreaseVolume(@NonNull Context context) {
        adjustVolume(context, -1);
    }

    /**
     * 设置音量百分比
     * <p>
     * 根据百分比设置音量，0.0 表示静音，1.0 表示最大音量。
     * </p>
     * <p>
     * Set Volume Percentage
     * <p>
     * Sets volume based on percentage, 0.0 means mute, 1.0 means maximum volume.
     * </p>
     *
     * @param context    上下文对象，不能为 null
     * @param percentage 音量百分比，范围 0.0 到 1.0
     */
    public static void setVolumePercentage(@NonNull Context context, @FloatRange(from = 0.0f, to = 1.0f) float percentage) {
        int maxVolume = getMaxVolume(context);
        if (maxVolume <= 0) {
            return;
        }

        float clampedPercentage = Math.max(0.0f, Math.min(1.0f, percentage));
        int volume = Math.round(clampedPercentage * maxVolume);
        setVolume(context, volume);
    }

    /**
     * 获取音量百分比
     * <p>
     * 获取当前音量相对于最大音量的百分比。
     * </p>
     * <p>
     * Get Volume Percentage
     * <p>
     * Gets the percentage of current volume relative to maximum volume.
     * </p>
     *
     * @param context 上下文对象，不能为 null
     * @return 音量百分比，范围 0.0 到 1.0，如果获取失败则返回 0.0
     */
    @FloatRange(from = 0.0f, to = 1.0f)
    public static float getVolumePercentage(@NonNull Context context) {
        int maxVolume = getMaxVolume(context);
        if (maxVolume <= 0) {
            return 0.0f;
        }

        int currentVolume = getCurrentVolume(context);
        return (float) currentVolume / maxVolume;
    }
}
