package com.aliyun.playerkit.locale;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.aliyun.playerkit.AliPlayerKit;

import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 播放器多语言管理器。
 *
 * <p>内置翻译通过 Android 标准 strings.xml 资源提供（playerkit 模块的 res/values/ 和 res/values-en/）。
 * 本管理器提供以下额外能力：</p>
 * <ul>
 *   <li>指定应用语言（不跟随系统）</li>
 *   <li>监听语言变更事件</li>
 * </ul>
 *
 * @author keria
 * @date 2026/05/11
 */
public final class PlayerLocale {

    private static final CopyOnWriteArrayList<OnLanguageChangedListener> sListeners = new CopyOnWriteArrayList<>();

    // 封装 context 和 language tag 的不可变缓存对象，保证复合读写的原子性
    private static final class LocalizedCache {
        final Context context;
        final String language;

        LocalizedCache(Context context, String language) {
            this.context = context;
            this.language = language;
        }
    }

    private static volatile LocalizedCache sCache;

    private PlayerLocale() {
    }

    // ==================== 获取文案 ====================

    /**
     * 获取指定资源 ID 对应的多语言文案。
     *
     * @param resId Android 字符串资源 ID（如 R.string.xxx）
     * @return 当前语言对应的文案
     */
    @NonNull
    public static String get(@StringRes int resId) {
        return getLocalizedContext().getString(resId);
    }

    /**
     * 获取指定资源 ID 对应的多语言文案，并进行格式化。
     *
     * @param resId      Android 字符串资源 ID
     * @param formatArgs 格式化参数（对应 strings.xml 中的 %s、%d 等占位符）
     * @return 格式化后的文案
     */
    @NonNull
    public static String get(@StringRes int resId, Object... formatArgs) {
        return getLocalizedContext().getString(resId, formatArgs);
    }

    // ==================== 语言切换 ====================

    /**
     * 设置应用语言（不跟随系统）。
     *
     * <p>内部使用 {@link AppCompatDelegate#setApplicationLocales(LocaleListCompat)} 实现，
     * 调用后会触发当前 Activity 重建，以使新语言生效。</p>
     *
     * <p>如果新语言与当前语言不同，会触发 {@link OnLanguageChangedListener} 回调。</p>
     *
     * @param languageCode 目标语言代码，如 {@code "zh"}、{@code "en"}、{@code "ja"}
     * @see #getLanguage()
     * @see #addOnLanguageChangedListener(OnLanguageChangedListener)
     */
    public static void setLanguage(@NonNull String languageCode) {
        String oldLanguage = getLanguage();
        LocaleListCompat locales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(locales);
        sCache = null;
        String newLanguage = getLanguage();
        if (!oldLanguage.equals(newLanguage)) {
            notifyLanguageChanged(oldLanguage, newLanguage);
        }
    }

    /**
     * 获取当前应用语言代码。
     *
     * <p><b>获取优先级：</b></p>
     * <ol>
     *   <li>通过 {@link AppCompatDelegate#getApplicationLocales()} 获取用户显式设置的语言</li>
     *   <li>如未设置，回退到系统默认语言 {@link Locale#getDefault()}</li>
     * </ol>
     *
     * @return 非空的语言代码字符串，如 {@code "zh"}、{@code "en"}
     * @see #setLanguage(String)
     */
    @NonNull
    public static String getLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty()) {
            Locale locale = locales.get(0);
            if (locale != null) {
                return locale.getLanguage();
            }
        }
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取已应用当前语言的 Context。
     * Android 13+ 直接返回 Application Context（系统自动同步）；
     * Android 12 及以下通过 createConfigurationContext 创建。
     */
    private static Context getLocalizedContext() {
        Context context = AliPlayerKit.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 系统原生支持，Application Context 会自动同步 locale
            return context;
        }
        // Android 12 及以下：需手动创建带正确 locale 的 Context
        String currentLang = getLanguage();
        LocalizedCache cache = sCache;  // 单次读取，保证原子性
        if (cache != null && currentLang.equals(cache.language)) {
            return cache.context;
        }
        // 创建新的 localized context
        Locale locale = Locale.forLanguageTag(currentLang);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        Context localizedCtx = context.createConfigurationContext(config);
        sCache = new LocalizedCache(localizedCtx, currentLang);  // 原子写入
        return localizedCtx;
    }

    // ==================== 语言变更监听 ====================

    /**
     * 添加语言变更监听器。
     *
     * <p>当调用 {@link #setLanguage(String)} 切换语言时，已注册的监听器将收到回调。
     * 重复添加同一个监听器实例不会导致重复回调。</p>
     *
     * @param listener 语言变更监听器，非空
     * @see #removeOnLanguageChangedListener(OnLanguageChangedListener)
     */
    public static void addOnLanguageChangedListener(@NonNull OnLanguageChangedListener listener) {
        sListeners.addIfAbsent(listener);
    }

    /**
     * 移除已注册的语言变更监听器。
     *
     * @param listener 要移除的监听器实例，非空；如果未注册过则无操作
     * @see #addOnLanguageChangedListener(OnLanguageChangedListener)
     */
    public static void removeOnLanguageChangedListener(@NonNull OnLanguageChangedListener listener) {
        sListeners.remove(listener);
    }

    // 通知所有监听器语言已变更
    private static void notifyLanguageChanged(@NonNull String oldLanguage, @NonNull String newLanguage) {
        for (OnLanguageChangedListener listener : sListeners) {
            listener.onLanguageChanged(oldLanguage, newLanguage);
        }
    }

    /**
     * 语言变更监听器接口。
     *
     * <p>当应用语言通过 {@link PlayerLocale#setLanguage(String)} 发生变更时，
     * 已注册的监听器将收到 {@link #onLanguageChanged(String, String)} 回调。</p>
     *
     * @see PlayerLocale#addOnLanguageChangedListener(OnLanguageChangedListener)
     */
    public interface OnLanguageChangedListener {
        /**
         * 语言变更时回调。
         *
         * <p>在 {@link PlayerLocale#setLanguage(String)} 调用后、Activity 重建之前触发。</p>
         *
         * @param oldLanguage 变更前的语言代码
         * @param newLanguage 变更后的语言代码
         */
        void onLanguageChanged(@NonNull String oldLanguage, @NonNull String newLanguage);
    }
}
