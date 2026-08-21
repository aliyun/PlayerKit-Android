package com.aliyun.playerkit.utils;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.locale.PlayerLocale;
import com.aliyun.playerkit.player.IMediaPlayer;

import java.util.Locale;

/**
 * 格式化工具类
 * <p>
 * Format Utility Class
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public class FormatUtil {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private FormatUtil() {
        throw new UnsupportedOperationException("Cannot instantiate FormatUtil");
    }

    /**
     * 格式化时长
     * <p>
     * 将时长格式化为 "HH:mm:ss" 或 "mm:ss" 格式。
     * 如果时长少于 1 小时，则省略小时部分。
     * </p>
     * <p>
     * Format Duration
     * <p>
     * Format the given duration into "HH:mm:ss" or "mm:ss" format.
     * If the duration is less than 1 hour, the hour part will be omitted.
     * </p>
     *
     * @param durationMs 时长（毫秒）
     * @return 格式化后的字符串
     */
    public static String formatDuration(long durationMs) {
        // Extract time components once to avoid redundant calculations
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600; // Total hours (integer division)
        long minutes = (totalSeconds % 3600) / 60; // Remaining minutes
        long seconds = totalSeconds % 60; // Remaining seconds

        // Return formatted string based on whether hours are present
        if (hours > 0) {
            // Include hours if duration is 1 hour or more
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            // Omit hours if duration is less than 1 hour
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 格式化渲染填充模式
     * <p>
     * 返回当前语言下的展示文案，用于设置面板等 UI 展示。
     * </p>
     *
     * @param scaleType 渲染填充模式
     * @return 格式化后的字符串
     */
    public static String formatScaleType(@IMediaPlayer.ScaleType int scaleType) {
        switch (scaleType) {
            case IMediaPlayer.ScaleType.FIT_XY:
                return PlayerLocale.get(R.string.setting_option_scale_fit_xy);
            case IMediaPlayer.ScaleType.FIT_CENTER:
                return PlayerLocale.get(R.string.setting_option_scale_fit_center);
            case IMediaPlayer.ScaleType.CENTER_CROP:
                return PlayerLocale.get(R.string.setting_option_scale_center_crop);
            default:
                return PlayerLocale.get(R.string.common_unknown);
        }
    }

    /**
     * 格式化镜像模式
     * <p>
     * 返回当前语言下的展示文案，用于设置面板等 UI 展示。
     * </p>
     *
     * @param mirrorType 镜像模式
     * @return 格式化后的字符串
     */
    public static String formatMirrorType(@IMediaPlayer.MirrorType int mirrorType) {
        switch (mirrorType) {
            case IMediaPlayer.MirrorType.NONE:
                return PlayerLocale.get(R.string.setting_option_mirror_none);
            case IMediaPlayer.MirrorType.HORIZONTAL:
                return PlayerLocale.get(R.string.setting_option_mirror_horizontal);
            case IMediaPlayer.MirrorType.VERTICAL:
                return PlayerLocale.get(R.string.setting_option_mirror_vertical);
            default:
                return PlayerLocale.get(R.string.common_unknown);
        }
    }

    /**
     * 格式化旋转角度
     *
     * @param rotation 旋转角度
     * @return 格式化后的字符串
     */
    public static String formatRotation(@IMediaPlayer.Rotation int rotation) {
        return rotation + "°";
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static String formatCurrentTime() {
        return String.format(Locale.getDefault(), "%tT", System.currentTimeMillis());
    }
}
