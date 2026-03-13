package com.aliyun.playerkit.example.settings.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * SharedPreferences 管理工具类（单例模式）
 * <p>
 * 职责：提供统一的持久化存储能力，支持多种数据类型的存储与读取。
 * 采用懒加载方式，内部自动获取全局 Context，无需显式初始化。
 * </p>
 * <p>
 * SharedPreferences Management Utility (Singleton Pattern)
 * <p>
 * Responsibility: Provides unified persistent storage capabilities, supporting storage and retrieval of various data types.
 * Uses lazy initialization, automatically obtaining global Context internally, requiring no explicit initialization.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public final class SPManager {

    private static final String PREF_NAME = "PlayerKit-AppSettings_Prefs";

    private static Context sContext;
    private SharedPreferences mPrefs;

    private SPManager() {
        // 私有构造函数，防止外部实例化
    }

    /**
     * 初始化工具类
     *
     * @param context 应用上下文
     */
    public static void init(@NonNull Context context) {
        sContext = context.getApplicationContext();
    }

    /**
     * 获取单例实例
     * <p>
     * 线程安全、高效、可销毁
     *
     * @return SPManager 实例
     */
    public static SPManager getInstance() {
        return Inner.instance;
    }

    private static class Inner {
        private static final SPManager instance = new SPManager();
    }

    /**
     * 确保 SharedPreferences 实例已初始化。
     */
    private synchronized SharedPreferences getPrefs() {
        if (mPrefs == null) {
            if (sContext == null) {
                throw new IllegalStateException("SPManager not initialized. Call SPManager.init(Context) first.");
            }
            mPrefs = sContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        return mPrefs;
    }

    /**
     * 保存 String 类型的值
     *
     * @param key   键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean saveString(String key, String value) {
        getPrefs().edit().putString(key, value).apply();
        return true;
    }

    /**
     * 获取 String 类型的值
     *
     * @param key 键
     * @return 值，若不存在则返回 null
     */
    @Nullable
    public String getString(String key) {
        return getPrefs().getString(key, null);
    }

    /**
     * 保存 int 类型的值
     *
     * @param key   键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean saveInt(String key, int value) {
        getPrefs().edit().putInt(key, value).apply();
        return true;
    }

    /**
     * 获取 int 类型的值
     *
     * @param key      键
     * @param defValue 默认值
     * @return 值，若不存在则返回默认值
     */
    public int getInt(String key, int defValue) {
        return getPrefs().getInt(key, defValue);
    }

    /**
     * 保存 boolean 类型的值
     *
     * @param key   键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean saveBool(String key, boolean value) {
        getPrefs().edit().putBoolean(key, value).apply();
        return true;
    }

    /**
     * 获取 boolean 类型的值
     *
     * @param key      键
     * @param defValue 默认值
     * @return 值，若不存在则返回默认值
     */
    public boolean getBool(String key, boolean defValue) {
        return getPrefs().getBoolean(key, defValue);
    }

    /**
     * 保存 long 类型的值
     *
     * @param key   键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean saveLong(String key, long value) {
        getPrefs().edit().putLong(key, value).apply();
        return true;
    }

    /**
     * 获取 long 类型的值
     *
     * @param key      键
     * @param defValue 默认值
     * @return 值，若不存在则返回默认值
     */
    public long getLong(String key, long defValue) {
        return getPrefs().getLong(key, defValue);
    }

    /**
     * 保存 float 类型的值
     *
     * @param key   键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean saveFloat(String key, float value) {
        getPrefs().edit().putFloat(key, value).apply();
        return true;
    }

    /**
     * 获取 float 类型的值
     *
     * @param key      键
     * @param defValue 默认值
     * @return 值，若不存在则返回默认值
     */
    public float getFloat(String key, float defValue) {
        return getPrefs().getFloat(key, defValue);
    }

    /**
     * 删除指定 key 的值
     *
     * @param key 键
     * @return 是否操作成功
     */
    public boolean remove(String key) {
        getPrefs().edit().remove(key).apply();
        return true;
    }

    /**
     * 清空所有存储的数据
     *
     * @return 是否操作成功
     */
    public boolean clearAll() {
        getPrefs().edit().clear().apply();
        return true;
    }
}
