package com.aliyun.playerkit.utils;

import android.text.TextUtils;

import java.util.Collection;

/**
 * 字符串工具类
 * <p>
 * 提供常用的字符串操作和验证方法。
 * </p>
 * <p>
 * String Utility Class
 * <p>
 * Provides common string operations and validation methods.
 * </p>
 *
 * @author baorunchen
 * @date 2019/04/29
 */
public final class StringUtil {

    /**
     * 空字符串常量
     */
    public static final String EMPTY = "";

    /**
     * 默认分隔符
     */
    public static final String DEFAULT_DELIMITER = ",";

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private StringUtil() {
        throw new UnsupportedOperationException("Cannot instantiate StringUtil");
    }

    // ========== 基础判断方法 ==========

    /**
     * 检查字符串是否为空
     * <p>
     * Check if string is empty (null or empty string)
     * </p>
     *
     * @param str 待检查的字符串
     * @return true 如果字符串为空，false 否则
     */
    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    /**
     * 检查字符串是否不为空
     * <p>
     * Check if string is not empty
     * </p>
     *
     * @param str 待检查的字符串
     * @return true 如果字符串不为空，false 否则
     */
    public static boolean isNotEmpty(String str) {
        return !TextUtils.isEmpty(str);
    }

    /**
     * 检查字符串是否为空白（null、空字符串或只包含空白字符）
     * <p>
     * Check if string is blank (null, empty or contains only whitespace)
     * </p>
     *
     * @param str 待检查的字符串
     * @return true 如果字符串为空白，false 否则
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否不为空白
     * <p>
     * Check if string is not blank
     * </p>
     *
     * @param str 待检查的字符串
     * @return true 如果字符串不为空白，false 否则
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 检查多个字符串是否都不为空
     * <p>
     * 验证传入的所有字符串参数是否都不为空（不为 null 且不为空字符串）。
     * 如果所有字符串都不为空，返回 true；否则返回 false。
     * </p>
     * <p>
     * Check if multiple strings are all non-empty
     * <p>
     * Validates that all passed string parameters are non-empty (not null and not empty string).
     * Returns true if all strings are non-empty; otherwise returns false.
     * </p>
     *
     * @param values 待检查的字符串数组，可以为 null
     * @return true 如果所有字符串都不为空，false 否则（包括 values 为 null 的情况）
     */
    public static boolean noneEmpty(String... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (String value : values) {
            if (isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查多个字符串是否都不为空白
     * <p>
     * Check if multiple strings are all non-blank
     * </p>
     *
     * @param values 待检查的字符串数组
     * @return true 如果所有字符串都不为空白，false 否则
     */
    public static boolean noneBlank(String... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (String value : values) {
            if (isBlank(value)) {
                return false;
            }
        }
        return true;
    }

    // ========== 字符串处理方法 ==========

    /**
     * 安全地获取字符串，如果为 null 则返回空字符串
     * <p>
     * Safely get string, return empty string if null
     * </p>
     *
     * @param str 原字符串
     * @return 非 null 的字符串
     */
    public static String nullToEmpty(String str) {
        return str == null ? EMPTY : str;
    }

    /**
     * 如果字符串为空则返回默认值
     * <p>
     * Return default value if string is empty
     * </p>
     *
     * @param str          原字符串
     * @param defaultValue 默认值
     * @return 原字符串或默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * 如果字符串为空白则返回默认值
     * <p>
     * Return default value if string is blank
     * </p>
     *
     * @param str          原字符串
     * @param defaultValue 默认值
     * @return 原字符串或默认值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 安全地去除字符串两端空白
     * <p>
     * Safely trim string
     * </p>
     *
     * @param str 原字符串
     * @return 去除空白后的字符串，如果原字符串为 null 则返回 null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除字符串两端空白，如果为 null 则返回空字符串
     * <p>
     * Trim string and return empty if null
     * </p>
     *
     * @param str 原字符串
     * @return 去除空白后的字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }

    // ========== 字符串连接方法 ==========

    /**
     * 使用指定分隔符连接字符串数组
     * <p>
     * Join string array with specified delimiter
     * </p>
     *
     * @param delimiter 分隔符
     * @param elements  字符串数组
     * @return 连接后的字符串
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(nullToEmpty(elements[i]));
        }
        return sb.toString();
    }

    /**
     * 使用默认分隔符连接字符串数组
     * <p>
     * Join string array with default delimiter
     * </p>
     *
     * @param elements 字符串数组
     * @return 连接后的字符串
     */
    public static String join(String... elements) {
        return join(DEFAULT_DELIMITER, elements);
    }

    /**
     * 连接集合中的字符串
     * <p>
     * Join strings in collection
     * </p>
     *
     * @param delimiter  分隔符
     * @param collection 字符串集合
     * @return 连接后的字符串
     */
    public static String join(String delimiter, Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            return EMPTY;
        }
        return join(delimiter, collection.toArray(new String[0]));
    }

    // ========== 字符串比较方法 ==========

    /**
     * 安全地比较两个字符串是否相等
     * <p>
     * 比较两个字符串是否相等，正确处理 null 值。
     * 如果两个字符串都为 null，返回 true；如果只有一个为 null，返回 false。
     * 如果两个字符串都不为 null，则使用 {@link String#equals(Object)} 进行比较。
     * </p>
     * <p>
     * Safely compare two strings for equality
     * <p>
     * Compares two strings for equality, properly handling null values.
     * Returns true if both strings are null; returns false if only one is null.
     * If both strings are not null, uses {@link String#equals(Object)} for comparison.
     * </p>
     *
     * @param str1 第一个字符串，可以为 null
     * @param str2 第二个字符串，可以为 null
     * @return true 如果两个字符串相等，false 否则
     */
    public static boolean equals(String str1, String str2) {
        return TextUtils.equals(str1, str2);
    }

    /**
     * 安全地比较两个字符串是否不相等
     * <p>
     * 比较两个字符串是否不相等，正确处理 null 值。
     * </p>
     * <p>
     * Safely compare two strings for inequality
     * </p>
     *
     * @param str1 第一个字符串，可以为 null
     * @param str2 第二个字符串，可以为 null
     * @return true 如果两个字符串不相等，false 否则
     */
    public static boolean notEquals(String str1, String str2) {
        return !TextUtils.equals(str1, str2);
    }

    /**
     * 忽略大小写地比较两个字符串是否相等
     * <p>
     * 比较两个字符串是否相等（忽略大小写），正确处理 null 值。
     * 如果两个字符串都为 null，返回 true；如果只有一个为 null，返回 false。
     * 如果两个字符串都不为 null，则使用 {@link String#equalsIgnoreCase(String)} 进行比较。
     * </p>
     * <p>
     * Compare two strings for equality ignoring case
     * <p>
     * Compares two strings for equality (ignoring case), properly handling null values.
     * Returns true if both strings are null; returns false if only one is null.
     * If both strings are not null, uses {@link String#equalsIgnoreCase(String)} for comparison.
     * </p>
     *
     * @param str1 第一个字符串，可以为 null
     * @param str2 第二个字符串，可以为 null
     * @return true 如果两个字符串相等（忽略大小写），false 否则
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 检查字符串是否以指定的前缀开头
     * <p>
     * 安全地检查字符串是否以指定的前缀开头，正确处理 null 值。
     * 如果字符串或前缀为 null，返回 false。
     * </p>
     * <p>
     * Check if string starts with specified prefix
     * <p>
     * Safely checks if string starts with specified prefix, properly handling null values.
     * Returns false if string or prefix is null.
     * </p>
     *
     * @param str    待检查的字符串，可以为 null
     * @param prefix 前缀字符串，可以为 null
     * @return true 如果字符串以指定前缀开头，false 否则
     */
    public static boolean startsWith(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * 检查字符串是否以指定的后缀结尾
     * <p>
     * 安全地检查字符串是否以指定的后缀结尾，正确处理 null 值。
     * 如果字符串或后缀为 null，返回 false。
     * </p>
     * <p>
     * Check if string ends with specified suffix
     * <p>
     * Safely checks if string ends with specified suffix, properly handling null values.
     * Returns false if string or suffix is null.
     * </p>
     *
     * @param str    待检查的字符串，可以为 null
     * @param suffix 后缀字符串，可以为 null
     * @return true 如果字符串以指定后缀结尾，false 否则
     */
    public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * 检查字符串是否包含指定的子字符串
     * <p>
     * 安全地检查字符串是否包含指定的子字符串，正确处理 null 值。
     * 如果字符串或子字符串为 null，返回 false。
     * </p>
     * <p>
     * Check if string contains specified substring
     * <p>
     * Safely checks if string contains specified substring, properly handling null values.
     * Returns false if string or substring is null.
     * </p>
     *
     * @param str       待检查的字符串，可以为 null
     * @param substring 子字符串，可以为 null
     * @return true 如果字符串包含指定的子字符串，false 否则
     */
    public static boolean contains(String str, String substring) {
        if (str == null || substring == null) {
            return false;
        }
        return str.contains(substring);
    }

    /**
     * 忽略大小写地检查字符串是否包含指定的子字符串
     * <p>
     * 安全地检查字符串是否包含指定的子字符串（忽略大小写），正确处理 null 值。
     * 如果字符串或子字符串为 null，返回 false。
     * </p>
     * <p>
     * Check if string contains specified substring ignoring case
     * <p>
     * Safely checks if string contains specified substring (ignoring case), properly handling null values.
     * Returns false if string or substring is null.
     * </p>
     *
     * @param str       待检查的字符串，可以为 null
     * @param substring 子字符串，可以为 null
     * @return true 如果字符串包含指定的子字符串（忽略大小写），false 否则
     */
    public static boolean containsIgnoreCase(String str, String substring) {
        if (str == null || substring == null) {
            return false;
        }
        return str.toLowerCase().contains(substring.toLowerCase());
    }

    // ========== 格式验证方法 ==========

    /**
     * 验证字符串是否只包含数字
     * <p>
     * Validate if string contains only digits
     * </p>
     *
     * @param str 待验证的字符串
     * @return true 如果只包含数字，false 否则
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    // ========== 字符串截取和处理方法 ==========

    /**
     * 安全地截取字符串
     * <p>
     * Safely substring
     * </p>
     *
     * @param str   原字符串
     * @param start 开始位置
     * @param end   结束位置
     * @return 截取的字符串，如果参数无效则返回空字符串
     */
    public static String safeSubstring(String str, int start, int end) {
        if (isEmpty(str) || start < 0 || end < start || start >= str.length()) {
            return EMPTY;
        }

        int safeEnd = Math.min(end, str.length());
        return str.substring(start, safeEnd);
    }

    /**
     * 限制字符串长度，超出部分用省略号替代
     * <p>
     * Limit string length with ellipsis
     * </p>
     *
     * @param str       原字符串
     * @param maxLength 最大长度
     * @return 处理后的字符串
     */
    public static String ellipsis(String str, int maxLength) {
        if (isEmpty(str) || maxLength <= 0) {
            return EMPTY;
        }

        if (str.length() <= maxLength) {
            return str;
        }

        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 保留字符串尾部的最大长度
     *
     * @param str      原字符串
     * @param maxChars 最大长度
     */
    public static String tail(String str, int maxChars) {
        if (str == null || maxChars <= 0) {
            return "";
        }
        if (str.length() <= maxChars) {
            return str;
        }
        return str.substring(str.length() - maxChars);
    }

    /**
     * 首字母大写
     * <p>
     * Capitalize first letter
     * </p>
     *
     * @param str 原字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }

        if (str.length() == 1) {
            return str.toUpperCase();
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 将对象转换为 "SimpleClassName@hashCode" 格式（hashCode 为十六进制）
     *
     * @param obj 任意对象（可为 null）
     * @return 格式化后的字符串，如 "MyClass@1a2b3c4d"；若 obj 为 null，返回 "null"
     */
    public static String getSimpleNameIdentity(Object obj) {
        if (obj == null) {
            return "null";
        }
        // 获取简单类名（不含包名）
        String simpleName = obj.getClass().getSimpleName();
        // 获取 identity hash code 并转为十六进制（小写）
        String hexHash = Integer.toHexString(System.identityHashCode(obj));
        return simpleName + "@" + hexHash;
    }
}
