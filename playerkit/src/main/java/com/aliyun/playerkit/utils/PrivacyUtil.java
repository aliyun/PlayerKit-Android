package com.aliyun.playerkit.utils;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 隐私保护工具类
 * <p>
 * 提供隐私信息处理相关的工具方法，包括 URL 鉴权参数移除、字符串模糊化等功能。
 * 用于在日志输出、错误上报等场景中保护敏感信息。
 * </p>
 * <p>
 * Privacy Protection Utility Class
 * <p>
 * Provides utility methods for privacy information processing, including URL authentication parameter removal,
 * string blurring, etc. Used to protect sensitive information in scenarios such as log output and error reporting.
 * </p>
 *
 * @author keria
 * @date 2026/1/6
 */
public final class PrivacyUtil {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private PrivacyUtil() {
        throw new UnsupportedOperationException("Cannot instantiate PrivacyUtil");
    }

    // ==================== 常量定义 ====================

    /**
     * URL 中的鉴权参数名
     * <p>
     * Authentication parameter name in URL
     * </p>
     */
    private static final String AUTH_KEY = "auth_key";

    /**
     * 模糊占位符
     * <p>
     * Mask placeholder
     * </p>
     */
    private static final String MASK = "***";

    /**
     * 最小可模糊长度
     * <p>
     * Minimum length for blurring
     * </p>
     */
    private static final int MIN_BLUR_LENGTH = 4;

    // ==================== URL 处理 ====================

    /**
     * 从 URL 中移除鉴权参数
     * <p>
     * 移除 URL 中的 auth_key 参数，保留其他所有 query 参数。
     * 如果 URL 中不存在 auth_key 参数，则返回原始 URL。
     * </p>
     * <p>
     * Remove authentication parameter from URL
     * <p>
     * Removes the auth_key parameter from the URL while preserving all other query parameters.
     * If the auth_key parameter does not exist in the URL, returns the original URL.
     * </p>
     *
     * @param url 原始 URL，可能为 null
     * @return 移除 auth_key 后的 URL，如果输入为 null 或处理失败则返回原始 URL
     */
    @Nullable
    public static String removeAuthKeyFromUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        try {
            Uri uri = Uri.parse(url);
            if (uri.getQueryParameter(AUTH_KEY) == null) {
                return url;
            }

            Uri.Builder builder = uri.buildUpon().clearQuery();
            for (String key : uri.getQueryParameterNames()) {
                if (!AUTH_KEY.equals(key)) {
                    for (String value : uri.getQueryParameters(key)) {
                        builder.appendQueryParameter(key, value);
                    }
                }
            }
            return builder.build().toString();
        } catch (Throwable ignore) {
            return url;
        }
    }

    // ==================== 字符串模糊 ====================

    /**
     * 模糊化字符串
     * <p>
     * 根据字符串长度采用不同的模糊策略：
     * <ul>
     *     <li>长度 <= 4：直接返回 "***"</li>
     *     <li>长度 5~8：保留首字符和尾字符，中间用 "***" 替代</li>
     *     <li>长度 > 8：保留前三位和后两位，中间用 "***" 替代</li>
     * </ul>
     * </p>
     * <p>
     * Blur string
     * <p>
     * Uses different blurring strategies based on string length:
     * <ul>
     *     <li>Length <= 4: Returns "***" directly</li>
     *     <li>Length 5~8: Keeps first and last character, replaces middle with "***"</li>
     *     <li>Length > 8: Keeps first 3 and last 2 characters, replaces middle with "***"</li>
     * </ul>
     * </p>
     *
     * @param value 待模糊化的字符串，可能为 null
     * @return 模糊化后的字符串，如果输入为 null 则返回 "null"
     */
    @NonNull
    public static String blur(@Nullable String value) {
        if (value == null) {
            return "null";
        }

        int len = value.length();
        if (len <= MIN_BLUR_LENGTH) {
            return MASK;
        }

        if (len <= 8) {
            return value.charAt(0) + MASK + value.charAt(len - 1);
        }

        return value.substring(0, 3) + MASK + value.substring(len - 2);
    }
}
