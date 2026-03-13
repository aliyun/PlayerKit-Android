package com.aliyun.playerkit.example.settings.storage;

/**
 * SharedPreferences 业务键名常量类
 * <p>
 * 职责：集中定义所有用于持久化存储的 Key 值，确保在整个模块中引用一致。
 * </p>
 * <p>
 * SharedPreferences Business Key Constants Class
 * <p>
 * Responsibility: Centralizes the definition of all Key values used for persistent storage, ensuring consistent references throughout the module.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public final class SettingKeys {

    private SettingKeys() {
        throw new UnsupportedOperationException("Cannot instantiate SettingKeys");
    }

    /**
     * 日志面板开关
     * <p>
     * Log panel switch
     * </p>
     */
    public static final String KEY_ENABLE_LOG_PANEL = "key_enable_log_panel";

    /**
     * 日志等级
     * <p>
     * Log level
     * </p>
     */
    public static final String KEY_LOG_LEVEL = "key_log_level";

    /**
     * 播放器视图类型
     * <p>
     * Player view type
     * </p>
     */
    public static final String KEY_PLAYER_VIEW_TYPE = "key_player_view_type";

    /**
     * Debug 模式开关
     * <p>
     * Debug mode switch
     * </p>
     */
    public static final String KEY_DEBUG_MODE = "key_debug_mode";

    /**
     * 禁止截屏开关
     * <p>
     * Disable screenshot switch
     * </p>
     */
    public static final String KEY_DISABLE_SCREENSHOT = "key_disable_screenshot";

}
