package com.aliyun.playerkit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.player.AliPlayerFactory;
import com.aliyun.playerkit.global.GlobalInitializer;
import com.aliyun.playerkit.logging.logger.DefaultPlayerLogger;
import com.aliyun.playerkit.logging.logger.IPlayerLogger;
import com.aliyun.playerkit.preload.DefaultPlayerPreloader;
import com.aliyun.playerkit.preload.IPlayerPreloader;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.data.PlayerViewType;
import com.aliyun.playerkit.utils.ContextUtils;

/**
 * AliPlayerKit 全局设置类
 * <p>
 * 提供 AliPlayerKit 的全局配置和初始化入口。
 * 负责管理播放器的全局配置，包括业务来源信息、本地缓存配置等。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>全局上下文管理：提供 ApplicationContext 访问</li>
 *     <li>播放器全局配置：设置业务来源信息、缓存配置等</li>
 *     <li>缓存管理：提供缓存清除功能</li>
 *     <li>版本信息：提供版本号查询功能</li>
 *     <li>初始化检查：确保在使用前已正确初始化</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * public class MyApplication extends Application {
 *     {@literal @}Override
 *     public void onCreate() {
 *         super.onCreate();
 *         // 初始化全局设置（会自动配置播放器）
 *         AliPlayerKit.init(this);
 *     }
 * }
 * </pre>
 * </p>
 * <p>
 * AliPlayerKit Global Setting Class
 * <p>
 * Provides global configuration and initialization entry for AliPlayerKit.
 * Responsible for managing player global configuration, including business source information, local cache configuration, etc.
 * </p>
 * <p>
 * Main Functions:
 * <ul>
 *     <li>Global context management: Provides ApplicationContext access</li>
 *     <li>Player global configuration: Sets business source information, cache configuration, etc.</li>
 *     <li>Cache management: Provides cache clearing functionality</li>
 *     <li>Version information: Provides version number query functionality</li>
 *     <li>Initialization check: Ensures proper initialization before use</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2024/11/26
 */
public final class AliPlayerKit {

    private static final String TAG = "AliPlayerKit";

    /**
     * AliPlayerKit 版本号
     * <p>
     * AliPlayerKit version number
     * </p>
     */
    public static final String PLAYER_KIT_VERSION = "7.12.0";

    // ==================== 状态管理 ====================

    /**
     * 应用上下文（使用 ApplicationContext 避免内存泄漏）
     * <p>
     * Application context (using ApplicationContext to avoid memory leaks)
     * </p>
     */
    @Nullable
    private static Context applicationContext;

    /**
     * 是否已初始化
     * <p>
     * 使用 volatile 确保多线程环境下的可见性。
     * </p>
     * <p>
     * Whether initialized
     * <p>
     * Using volatile to ensure visibility in multi-threaded environments.
     * </p>
     */
    private static volatile boolean initialized = false;

    /**
     * 初始化同步锁对象
     * <p>
     * 用于确保初始化过程的线程安全。
     * </p>
     * <p>
     * Initialization synchronization lock object
     * <p>
     * Used to ensure thread safety during initialization.
     * </p>
     */
    private static final Object INIT_LOCK = new Object();

    // ==================== 全局 API 实例 ====================

    /**
     * 全局日志实例
     * <p>
     * Global logger instance
     * </p>
     */
    private static volatile IPlayerLogger sLogger = null;

    /**
     * 全局预加载实例
     * <p>
     * Global preloader instance
     * </p>
     */
    private static volatile IPlayerPreloader sPreloader = null;

    /**
     * 全局 API 同步锁对象
     * <p>
     * Global API synchronization lock object
     * </p>
     */
    private static final Object GLOBAL_API_LOCK = new Object();

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private AliPlayerKit() {
        throw new UnsupportedOperationException("Cannot instantiate AliPlayerKit");
    }

    // ==================== 初始化方法 ====================

    /**
     * 初始化全局设置
     * <p>
     * 应该在 Application 的 onCreate 中调用，且只调用一次。
     * 如果多次调用，后续调用会被忽略（使用第一次传入的 Context）。
     * </p>
     * <p>
     * 初始化流程：
     * <ol>
     *     <li>保存 ApplicationContext</li>
     *     <li>配置播放器全局设置（业务来源信息、缓存配置等）</li>
     * </ol>
     * </p>
     * <p>
     * 线程安全：使用双重检查锁定模式确保线程安全。
     * </p>
     * <p>
     * Initialize global settings
     * <p>
     * Should be called in Application.onCreate() and only once.
     * If called multiple times, subsequent calls will be ignored (using the first Context passed).
     * </p>
     * <p>
     * Initialization process:
     * <ol>
     *     <li>Save ApplicationContext</li>
     *     <li>Configure player global settings (business source information, cache configuration, etc.)</li>
     * </ol>
     * </p>
     * <p>
     * Thread safety: Uses double-checked locking pattern to ensure thread safety.
     * </p>
     *
     * @param context 应用上下文，将使用 ApplicationContext
     * @throws IllegalArgumentException 如果 context 为 null
     * @throws RuntimeException         如果初始化失败
     */
    public static void init(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        // 双重检查锁定模式，确保线程安全
        if (initialized) {
            LogHub.w(TAG, "Already initialized, ignoring duplicate init call");
            return;
        }

        synchronized (INIT_LOCK) {
            if (initialized) {
                LogHub.w(TAG, "Already initialized, ignoring duplicate init call");
                return;
            }

            try {
                // 初始化 ContextUtils
                ContextUtils.setContext(context);

                applicationContext = context.getApplicationContext();

                // 初始化全局设置
                GlobalInitializer.initialize(applicationContext);

                initialized = true;
                LogHub.i(TAG, "AliPlayerKit initialized successfully");
            } catch (Exception e) {
                LogHub.e(TAG, "Failed to initialize AliPlayerKit", e);
                applicationContext = null;
                initialized = false;
                sLogger = null;
                sPreloader = null;
                throw new RuntimeException("Failed to initialize AliPlayerKit", e);
            }
        }
    }

    // ==================== 公开方法 ====================

    /**
     * 获取应用上下文
     * <p>
     * 返回 ApplicationContext，避免内存泄漏。
     * 在调用此方法前，必须先调用 {@link #init(Context)} 进行初始化。
     * </p>
     * <p>
     * Get application context
     * <p>
     * Returns ApplicationContext to avoid memory leaks.
     * Must call {@link #init(Context)} first before calling this method.
     * </p>
     *
     * @return 应用上下文
     * @throws IllegalStateException 如果未初始化
     */
    @NonNull
    public static Context getContext() {
        if (!initialized || applicationContext == null) {
            throw new IllegalStateException("AliPlayerKit not initialized. " + "Call AliPlayerKit.init(Context) in Application.onCreate() first.");
        }
        return applicationContext;
    }

    /**
     * 检查是否已初始化
     * <p>
     * Check if initialized
     * </p>
     *
     * @return true 如果已初始化，false 否则
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取 AliPlayerKit 版本号
     * <p>
     * Get AliPlayerKit version number
     * </p>
     *
     * @return AliPlayerKit 版本号
     */
    @NonNull
    public static String getPlayerKitVersion() {
        return PLAYER_KIT_VERSION;
    }

    /**
     * 获取底层 AliPlayer SDK 版本号
     * <p>
     * Get underlying AliPlayer SDK version number
     * </p>
     *
     * @return AliPlayer SDK 版本号
     */
    @NonNull
    public static String getSdkVersion() {
        return AliPlayerFactory.getSdkVersion();
    }

    /**
     * 获取设备 ID
     * <p>
     * Get device ID
     * </p>
     *
     * @return 设备 ID
     */
    @NonNull
    public static String getDeviceId() {
        try {
            // TODO keria: This API will crash if invoked before playback starts.
            return AliPlayerFactory.getDeviceUUID();
        } catch (Exception e) {
            return "";
        }
    }

    // ==================== 缓存管理 ====================

    /**
     * 清除播放器缓存
     * <p>
     * 清除所有播放器相关的缓存文件。
     * 注意：此操作会清除所有缓存，可能会影响播放性能。
     * 建议在用户主动清理缓存或应用卸载时调用。
     * </p>
     * <p>
     * Clear player cache
     * <p>
     * Clears all player-related cache files.
     * Note: This operation will clear all caches and may affect playback performance.
     * Recommended to call when user actively clears cache or when app is uninstalled.
     * </p>
     */
    public static void clearCaches() {
        if (!initialized) {
            LogHub.w(TAG, "Not initialized, cannot clear caches");
            return;
        }

        GlobalInitializer.clearCaches();
    }


    // ==================== 插槽系统配置管理 ====================

    /**
     * Debug 模式开关
     * <p>
     * 控制是否启用 Debug 模式。当启用时，即使是在 Release 构建下，也会启用 Debug 相关的功能，
     * 例如：Debug Toast、日志面板等。
     * 默认值：BuildConfig.DEBUG（仅在 Debug 版本下默认为 true）
     * </p>
     * <p>
     * 注意：建议使用 {@link #setDebugModeEnabled(boolean)} 方法来设置，而不是直接修改此变量
     * </p>
     * <p>
     * Debug mode switch
     * <p>
     * Controls whether Debug mode is enabled. When enabled, Debug-related features will be enabled
     * even in Release builds, such as: Debug Toast, log panel, etc.
     * Default: BuildConfig.DEBUG (only defaults to true in Debug builds)
     * </p>
     */
    private static volatile boolean debugModeEnabled = BuildConfig.DEBUG;

    /**
     * 是否启用日志面板（Log Panel）显示
     * <p>
     * 控制日志面板插槽（LogPanelSlot）是否在 UI 中显示。
     * 默认值：BuildConfig.DEBUG（仅在 Debug 版本下默认启用）
     * </p>
     * <p>
     * 注意：建议使用 {@link #setLogPanelEnabled(boolean)} 方法来设置，而不是直接修改此变量
     * </p>
     * <p>
     * Whether to enable log panel display
     * <p>
     * Controls whether the log panel slot (LogPanelSlot) is displayed in the UI.
     * Default: BuildConfig.DEBUG (only enabled by default in Debug builds)
     * </p>
     */
    private static volatile boolean logPanelEnabled = BuildConfig.DEBUG;

    /**
     * 设置是否启用 Debug 模式
     * <p>
     * 控制是否启用 Debug 模式。当启用时，即使是在 Release 构建下，也会启用 Debug 相关的功能。
     * 此设置会影响 {@link com.aliyun.playerkit.utils.ToastUtils} 中的 Debug Toast 显示。
     * </p>
     * <p>
     * 注意：Debug 模式开关和日志面板开关是独立的，互不影响。
     * </p>
     * <p>
     * Set whether to enable Debug mode
     * <p>
     * Controls whether Debug mode is enabled. When enabled, Debug-related features will be enabled
     * even in Release builds. This setting affects Debug Toast display in {@link com.aliyun.playerkit.utils.ToastUtils}.
     * </p>
     * <p>
     * Note: Debug mode switch and log panel switch are independent and do not affect each other.
     * </p>
     *
     * @param enable true 表示启用 Debug 模式，false 表示禁用
     */
    public static void setDebugModeEnabled(boolean enable) {
        debugModeEnabled = enable;
        LogHub.i(TAG, "Debug mode enabled: " + enable);
    }

    /**
     * 检查 Debug 模式是否已启用
     * <p>
     * 如果 debugModeEnabled 为 true，则返回 true，即使是在 Release 构建下。
     * 否则返回 BuildConfig.DEBUG 的值。
     * </p>
     * <p>
     * Check if Debug mode is enabled
     * <p>
     * Returns true if debugModeEnabled is true, even in Release builds.
     * Otherwise returns the value of BuildConfig.DEBUG.
     * </p>
     *
     * @return true 表示 Debug 模式已启用，false 表示已禁用
     */
    public static boolean isDebugModeEnabled() {
        // 如果用户手动启用了 Debug 模式，则返回 true
        if (debugModeEnabled) {
            return true;
        }
        // 否则返回 BuildConfig.DEBUG 的值
        return BuildConfig.DEBUG;
    }

    /**
     * 设置是否启用日志面板显示
     * <p>
     * 控制日志面板插槽（LogPanelSlot）是否在 UI 中显示。
     * 此设置会影响 {@link com.aliyun.playerkit.slot.SlotConstants} 中日志面板插槽的可见性判断。
     * </p>
     * <p>
     * Set whether to enable log panel display
     * <p>
     * Controls whether the log panel slot (LogPanelSlot) is displayed in the UI.
     * This setting affects the visibility judgment of the log panel slot in {@link com.aliyun.playerkit.slot.SlotConstants}.
     * </p>
     *
     * @param enable true 表示启用日志面板，false 表示禁用
     */
    public static void setLogPanelEnabled(boolean enable) {
        logPanelEnabled = enable;
        LogHub.i(TAG, "Log panel enabled: " + enable);
    }

    /**
     * 检查日志面板是否已启用
     * <p>
     * Check if log panel is enabled
     * </p>
     *
     * @return true 表示日志面板已启用，false 表示已禁用
     */
    public static boolean isLogPanelEnabled() {
        return logPanelEnabled;
    }

    // ==================== 禁止截屏配置 ====================

    /**
     * 是否禁止截屏
     * <p>
     * 控制是否禁止在播放器页面截屏。
     * 默认值：false（允许截屏）
     * </p>
     * <p>
     * Whether to disable screenshot
     * <p>
     * Controls whether to disable screenshot on player pages.
     * Default: false (screenshot allowed)
     * </p>
     */
    private static volatile boolean disableScreenshot = false;

    /**
     * 设置是否禁止截屏
     * <p>
     * 控制是否禁止在播放器页面截屏。当启用时，会在播放器所在的 Activity 窗口上设置 FLAG_SECURE 标志，
     * 从而禁止截屏和录屏。
     * </p>
     * <p>
     * Set whether to disable screenshot
     * <p>
     * Controls whether to disable screenshot on player pages. When enabled, sets FLAG_SECURE flag
     * on the Activity window where the player is located, thereby disabling screenshot and screen recording.
     * </p>
     *
     * @param disable true 表示禁止截屏，false 表示允许截屏
     */
    public static void setDisableScreenshot(boolean disable) {
        disableScreenshot = disable;
        LogHub.i(TAG, "Disable screenshot: " + disable);
    }

    /**
     * 检查是否禁止截屏
     * <p>
     * Check if screenshot is disabled
     * </p>
     *
     * @return true 表示禁止截屏，false 表示允许截屏
     */
    public static boolean isDisableScreenshot() {
        return disableScreenshot;
    }

    // ==================== 播放器视图类型配置 ====================

    /**
     * 播放器视图类型
     * <p>
     * 默认为 {@link PlayerViewType#DISPLAY_VIEW}（推荐）。
     * </p>
     * <p>
     * Player view type
     * <p>
     * Default is {@link PlayerViewType#DISPLAY_VIEW} (Recommended).
     * </p>
     */
    private static volatile PlayerViewType playerViewType = PlayerViewType.DISPLAY_VIEW;

    /**
     * 设置播放器视图类型
     * <p>
     * 设置全局播放器视图类型，影响所有新创建的播放器实例。
     * 支持的视图类型：
     * <ul>
     *     <li>{@link PlayerViewType#DISPLAY_VIEW}：AliDisplayView（推荐），官方提供的显示视图组件</li>
     *     <li>{@link PlayerViewType#SURFACE_VIEW}：SurfaceView，传统 SurfaceView 实现</li>
     *     <li>{@link PlayerViewType#TEXTURE_VIEW}：TextureView，支持动画和变换</li>
     * </ul>
     * </p>
     * <p>
     * Set player view type
     * <p>
     * Sets the global player view type, affecting all newly created player instances.
     * Supported view types:
     * <ul>
     *     <li>{@link PlayerViewType#DISPLAY_VIEW}: AliDisplayView (Recommended), official display view component</li>
     *     <li>{@link PlayerViewType#SURFACE_VIEW}: SurfaceView, traditional SurfaceView implementation</li>
     *     <li>{@link PlayerViewType#TEXTURE_VIEW}: TextureView, supports animation and transformation</li>
     * </ul>
     * </p>
     *
     * @param viewType 视图类型，不能为 null
     * @throws IllegalArgumentException 如果 viewType 为 null
     */
    public static void setPlayerViewType(@NonNull PlayerViewType viewType) {
        if (viewType == null) {
            throw new IllegalArgumentException("PlayerViewType cannot be null");
        }
        playerViewType = viewType;
        LogHub.i(TAG, "Player view type set to: " + viewType);
    }

    /**
     * 获取当前播放器视图类型
     * <p>
     * Get current player view type
     * </p>
     *
     * @return 当前视图类型，默认为 {@link PlayerViewType#DISPLAY_VIEW}
     */
    @NonNull
    public static PlayerViewType getPlayerViewType() {
        return playerViewType;
    }

    // ==================== 全局 API 访问方法 ====================

    /**
     * 获取全局日志实例
     * <p>
     * 在调用此方法前,必须先调用 {@link #init(Context)} 进行初始化。
     * </p>
     * <p>
     * Get global logger instance
     * <p>
     * Must call {@link #init(Context)} first before calling this method.
     * </p>
     *
     * @return 全局日志实例
     * @throws IllegalStateException 如果未初始化
     */
    @NonNull
    public static IPlayerLogger getLogger() {
        if (!initialized) {
            throw new IllegalStateException("AliPlayerKit not initialized. Call AliPlayerKit.init(Context) in Application.onCreate() first.");
        }

        if (sLogger == null) {
            synchronized (GLOBAL_API_LOCK) {
                if (sLogger == null) {
                    sLogger = new DefaultPlayerLogger(applicationContext);
                }
            }
        }
        return sLogger;
    }

    /**
     * 获取全局预加载实例
     * <p>
     * 在调用此方法前,必须先调用 {@link #init(Context)} 进行初始化。
     * </p>
     * <p>
     * Get global preloader instance
     * <p>
     * Must call {@link #init(Context)} first before calling this method.
     * </p>
     *
     * @return 全局预加载实例
     * @throws IllegalStateException 如果未初始化
     */
    @NonNull
    public static IPlayerPreloader getPreloader() {
        if (!initialized) {
            throw new IllegalStateException("AliPlayerKit not initialized. Call AliPlayerKit.init(Context) in Application.onCreate() first.");
        }

        if (sPreloader == null) {
            synchronized (GLOBAL_API_LOCK) {
                if (sPreloader == null) {
                    sPreloader = new DefaultPlayerPreloader();
                }
            }
        }
        return sPreloader;
    }

    /**
     * 设置自定义日志实现（可选）
     * <p>
     * 允许外部提供自定义的日志实现,替换默认实现。
     * 必须在 {@link #init(Context)} 之后、首次调用 {@link #getLogger()} 之前调用。
     * </p>
     * <p>
     * Set custom logger implementation (optional)
     * <p>
     * Allows external custom logger implementation to replace the default implementation.
     * Must be called after {@link #init(Context)} and before the first call to {@link #getLogger()}.
     * </p>
     *
     * @param logger 自定义日志实例
     * @throws IllegalStateException 如果未初始化或已经创建了默认实例
     */
    public static void setLogger(@NonNull IPlayerLogger logger) {
        if (!initialized) {
            throw new IllegalStateException("AliPlayerKit not initialized. Call AliPlayerKit.init(Context) in Application.onCreate() first.");
        }

        synchronized (GLOBAL_API_LOCK) {
            if (sLogger != null) {
                throw new IllegalStateException("Logger instance already created. Call setLogger() before the first call to getLogger().");
            }
            sLogger = logger;
            LogHub.i(TAG, "Custom logger set");
        }
    }

    /**
     * 设置自定义预加载实现（可选）
     * <p>
     * 允许外部提供自定义的预加载实现,替换默认实现。
     * 必须在 {@link #init(Context)} 之后、首次调用 {@link #getPreloader()} 之前调用。
     * </p>
     * <p>
     * Set custom preloader implementation (optional)
     * <p>
     * Allows external custom preloader implementation to replace the default implementation.
     * Must be called after {@link #init(Context)} and before the first call to {@link #getPreloader()}.
     * </p>
     *
     * @param preloader 自定义预加载实例
     * @throws IllegalStateException 如果未初始化或已经创建了默认实例
     */
    public static void setPreloader(@NonNull IPlayerPreloader preloader) {
        if (!initialized) {
            throw new IllegalStateException("AliPlayerKit not initialized. Call AliPlayerKit.init(Context) in Application.onCreate() first.");
        }

        synchronized (GLOBAL_API_LOCK) {
            if (sPreloader != null) {
                throw new IllegalStateException("Preloader instance already created. Call setPreloader() before the first call to getPreloader().");
            }
            sPreloader = preloader;
            LogHub.i(TAG, "Custom preloader set");
        }
    }
}
