package com.aliyun.playerkit.utils;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * 屏幕密度转换工具类
 * <p>
 * 提供屏幕密度相关的单位转换功能，包括：
 * <ul>
 *     <li>dp/dip 与 px 之间的转换</li>
 *     <li>sp 与 px 之间的转换</li>
 * </ul>
 * </p>
 * <p>
 * 所有转换方法都使用四舍五入的方式处理浮点数，确保转换结果的准确性。
 * </p>
 * <p>
 * Screen Density Conversion Utility Class
 * <p>
 * Provides screen density-related unit conversion functions, including:
 * <ul>
 *     <li>Conversion between dp/dip and px</li>
 *     <li>Conversion between sp and px</li>
 * </ul>
 * </p>
 * <p>
 * All conversion methods use rounding to handle floating-point numbers, ensuring the accuracy of conversion results.
 * </p>
 *
 * @author keria
 * @date 2026/1/12
 */
public final class DensityUtil {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * 此类为工具类，所有方法均为静态方法，不应被实例化。
     * </p>
     * <p>
     * Private constructor to prevent instantiation
     * <p>
     * This is a utility class with all static methods and should not be instantiated.
     * </p>
     */
    private DensityUtil() {
        throw new UnsupportedOperationException("Cannot instantiate DensityUtil");
    }

    /**
     * 将 dp/dip 值转换为 px 值
     * <p>
     * 根据设备的屏幕密度，将设备无关像素（dp/dip）转换为实际像素（px）。
     * 转换公式：px = dp * density，结果进行四舍五入处理。
     * </p>
     *
     * @param context  上下文对象，用于获取屏幕密度信息
     * @param dipValue dp/dip 值
     * @return 转换后的 px 值
     */
    public static int dip2px(@NonNull Context context, float dipValue) {
        return (int) (0.5F + dipValue * context.getResources().getDisplayMetrics().density);
    }

    /**
     * 将 px 值转换为 dp/dip 值
     * <p>
     * 根据设备的屏幕密度，将实际像素（px）转换为设备无关像素（dp/dip）。
     * 转换公式：dp = px / density，结果进行四舍五入处理。
     * </p>
     *
     * @param context 上下文对象，用于获取屏幕密度信息
     * @param pxValue px 值
     * @return 转换后的 dp/dip 值
     */
    public static int px2dip(@NonNull Context context, float pxValue) {
        return (int) (0.5F + pxValue / context.getResources().getDisplayMetrics().density);
    }

    /**
     * 将 sp 值转换为 px 值
     * <p>
     * 根据设备的字体缩放比例，将缩放无关像素（sp）转换为实际像素（px）。
     * sp 是用于字体大小的单位，会根据用户的字体大小设置进行缩放。
     * 转换公式：px = sp * scaledDensity，结果进行四舍五入处理。
     * </p>
     *
     * @param context 上下文对象，用于获取字体缩放比例信息
     * @param spValue sp 值
     * @return 转换后的 px 值
     */
    public static int sp2px(@NonNull Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
