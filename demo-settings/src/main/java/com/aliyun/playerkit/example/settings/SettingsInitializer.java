package com.aliyun.playerkit.example.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.data.PlayerViewType;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.example.settings.storage.SettingKeys;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * 设置初始化工具类
 * <p>
 * 负责在应用启动时从持久化存储中恢复设置并应用到相应的组件。
 * 将设置恢复逻辑封装在 demo-settings 模块内，保持职责清晰。
 * </p>
 * <p>
 * Settings Initializer Utility Class
 * <p>
 * Responsible for restoring settings from persistent storage and applying them to corresponding components at application startup.
 * Encapsulates settings restoration logic within the demo-settings module to maintain clear responsibilities.
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public final class SettingsInitializer {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private SettingsInitializer() {
        throw new UnsupportedOperationException("Cannot instantiate SettingsInitializer");
    }

    /**
     * 初始化设置系统并恢复设置
     * <p>
     * 初始化 SPManager，然后从持久化存储中恢复日志设置（日志面板开关、日志等级），并应用到 AliPlayerKit 和 LogHub。
     * 如果 SPManager 中没有保存的值，则使用默认值。
     * </p>
     * <p>
     * Initialize settings system and restore settings
     * <p>
     * Initializes SPManager, then restores log settings (log panel switch, log level) from persistent storage
     * and applies them to AliPlayerKit and LogHub. If no saved value exists in SPManager, uses default values.
     * </p>
     *
     * @param context 应用上下文，不能为 null
     */
    public static void init(@NonNull Context context) {
        // 初始化 SPManager
        SPManager.init(context);

        // 恢复播放器视图类型设置
        restorePlayerViewTypeSettings();

        // 恢复 Debug 模式设置
        restoreDebugModeSettings();

        // 恢复禁止截屏设置
        restoreDisableScreenshotSettings();

        // 恢复日志相关设置
        restoreLogSettings();
    }

    /**
     * 恢复播放器视图类型设置
     * <p>
     * 从 SPManager 读取保存的播放器视图类型设置，并应用到 AliPlayerKit。
     * 如果 SPManager 中没有保存的值，则使用默认值（DISPLAY_VIEW）。
     * </p>
     * <p>
     * Restore player view type settings
     * <p>
     * Reads saved player view type settings from SPManager and applies them to AliPlayerKit.
     * If no saved value exists in SPManager, uses default value (DISPLAY_VIEW).
     * </p>
     */
    private static void restorePlayerViewTypeSettings() {
        // 恢复播放器视图类型设置
        // 默认值：PlayerViewType.DISPLAY_VIEW（推荐）
        String savedType = SPManager.getInstance().getString(SettingKeys.KEY_PLAYER_VIEW_TYPE);
        PlayerViewType viewType;
        if (StringUtil.isNotEmpty(savedType)) {
            try {
                viewType = PlayerViewType.valueOf(savedType);
            } catch (IllegalArgumentException e) {
                // 如果保存的值无效，使用默认值
                viewType = PlayerViewType.DISPLAY_VIEW;
            }
        } else {
            viewType = PlayerViewType.DISPLAY_VIEW;
        }
        AliPlayerKit.setPlayerViewType(viewType);
    }

    /**
     * 恢复 Debug 模式设置
     * <p>
     * 从 SPManager 读取保存的 Debug 模式设置，并应用到 AliPlayerKit。
     * 如果 SPManager 中没有保存的值，则使用默认值（BuildConfig.DEBUG）。
     * </p>
     * <p>
     * Restore Debug mode settings
     * </p>
     * <p>
     * Reads saved Debug mode settings from SPManager and applies them to AliPlayerKit.
     * If no saved value exists in SPManager, uses default value (BuildConfig.DEBUG).
     * </p>
     */
    private static void restoreDebugModeSettings() {
        // 获取 SPManager 实例
        SPManager spManager = SPManager.getInstance();

        // 恢复 Debug 模式开关设置
        // 默认值：AliPlayerKit.isDebugModeEnabled()（如果未初始化则使用 BuildConfig.DEBUG）
        boolean defaultDebugMode = AliPlayerKit.isDebugModeEnabled();
        boolean enableDebugMode = spManager.getBool(SettingKeys.KEY_DEBUG_MODE, defaultDebugMode);
        AliPlayerKit.setDebugModeEnabled(enableDebugMode);
    }

    /**
     * 恢复禁止截屏设置
     * <p>
     * 从 SPManager 读取保存的禁止截屏设置，并应用到 AliPlayerKit。
     * 如果 SPManager 中没有保存的值，则使用默认值（false）。
     * </p>
     * <p>
     * Restore disable screenshot settings
     * </p>
     * <p>
     * Reads saved disable screenshot settings from SPManager and applies them to AliPlayerKit.
     * If no saved value exists in SPManager, uses default value (false).
     * </p>
     */
    private static void restoreDisableScreenshotSettings() {
        // 获取 SPManager 实例
        SPManager spManager = SPManager.getInstance();

        // 恢复禁止截屏开关设置
        boolean disableScreenshot = spManager.getBool(SettingKeys.KEY_DISABLE_SCREENSHOT, AliPlayerKit.isDisableScreenshot());
        AliPlayerKit.setDisableScreenshot(disableScreenshot);
    }

    /**
     * 恢复日志相关设置
     * <p>
     * 从 SPManager 读取保存的日志设置（日志面板开关、日志等级），并应用到 AliPlayerKit 和 LogHub。
     * 如果 SPManager 中没有保存的值，则使用默认值。
     * </p>
     * <p>
     * Restore log-related settings
     * </p>
     * <p>
     * Reads saved log settings (log panel switch, log level) from SPManager and applies them to AliPlayerKit and LogHub.
     * If no saved value exists in SPManager, uses default values.
     * </p>
     */
    private static void restoreLogSettings() {
        // 获取 SPManager 实例
        SPManager spManager = SPManager.getInstance();

        // 恢复日志面板开关设置
        boolean defaultEnableLogPanel = AliPlayerKit.isLogPanelEnabled();
        boolean enableLogPanel = spManager.getBool(SettingKeys.KEY_ENABLE_LOG_PANEL, defaultEnableLogPanel);
        AliPlayerKit.setLogPanelEnabled(enableLogPanel);

        // 恢复日志等级设置
        int logLevel = spManager.getInt(SettingKeys.KEY_LOG_LEVEL, LogHub.getLogLevel());
        LogHub.setLogLevel(logLevel);
    }
}
