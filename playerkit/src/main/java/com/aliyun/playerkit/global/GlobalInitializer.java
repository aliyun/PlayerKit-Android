package com.aliyun.playerkit.global;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliyun.player.AliPlayerGlobalSettings;
import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.FileUtil;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * 全局初始化器
 * <p>
 * 负责初始化播放器 SDK 的全局设置，包括业务来源信息、本地缓存配置等。
 * </p>
 * <p>
 * Global Initializer
 * <p>
 * Responsible for initializing player SDK global settings, including business source information, local cache configuration, etc.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class GlobalInitializer {

    private static final String TAG = "GlobalInitializer";

    // ==================== 常量配置 ====================

    /**
     * 播放组件名称
     * <p>
     * Player component name
     * </p>
     */
    private static final String PLAYER_KIT = "AliPlayerKit";

    /**
     * 业务来源信息
     * <p>
     * 用于标识播放器来源，便于统计和问题排查。
     * 包含场景类型、平台信息和版本号。
     * </p>
     * <p>
     * Business source information
     * <p>
     * Used to identify the player source for statistics and troubleshooting.
     * Contains scene type, platform information, and version number.
     * </p>
     */
    private static final String EXTRA_DATA_ANDROID_PLAYER_KIT = "{\"scene\":\"player-kit\",\"platform\":\"android\",\"version\":\"" + AliPlayerKit.PLAYER_KIT_VERSION + "\"}";

    // ==================== 缓存配置常量 ====================

    /**
     * 是否开启本地缓存
     * <p>
     * 默认值：true
     * </p>
     * <p>
     * Whether to enable local cache
     * <p>
     * Default: true
     * </p>
     */
    private static final boolean ENABLE_LOCAL_CACHE_FLAG = true;

    /**
     * 本地缓存最大缓冲区内存（KB）
     * <p>
     * 默认值：10MB (10 * 1024 KB)
     * </p>
     * <p>
     * 注意：5.4.7.1 及以后版本已废弃，暂无作用。
     * 保留此常量以保持兼容性。
     * </p>
     * <p>
     * Local cache max buffer memory (KB)
     * <p>
     * Default: 10MB (10 * 1024 KB)
     * </p>
     * <p>
     * Note: Deprecated since version 5.4.7.1, no longer effective.
     * Kept for compatibility.
     * </p>
     */
    private static final int LOCAL_CACHE_MAX_BUFFER_MEMORY_KB = 10 * 1024;

    /**
     * 本地缓存过期时间（分钟）
     * <p>
     * 默认值：30 天 (30 * 24 * 60 分钟)
     * </p>
     * <p>
     * 注意：5.4.7.1 及以后版本已废弃，暂无作用。
     * 保留此常量以保持兼容性。
     * </p>
     * <p>
     * Local cache expire time (minutes)
     * <p>
     * Default: 30 days (30 * 24 * 60 minutes)
     * </p>
     * <p>
     * Note: Deprecated since version 5.4.7.1, no longer effective.
     * Kept for compatibility.
     * </p>
     */
    private static final int LOCAL_CACHE_EXPIRE_MIN = 30 * 24 * 60;

    /**
     * 最大缓存容量（MB）
     * <p>
     * 默认值：20GB (20 * 1024 MB)
     * </p>
     * <p>
     * 在清理时，如果缓存总容量超过此大小，则会以 cacheItem 为粒度，
     * 按缓存的最后时间排序，一个一个的删除最旧的缓存文件，直到小于等于最大缓存容量。
     * </p>
     * <p>
     * Maximum cache capacity (MB)
     * <p>
     * Default: 20GB (20 * 1024 MB)
     * </p>
     * <p>
     * During cleanup, if the total cache capacity exceeds this size, cache items will be deleted
     * one by one in order of last access time, starting with the oldest, until the total capacity
     * is less than or equal to the maximum cache capacity.
     * </p>
     */
    private static final int LOCAL_CACHE_MAX_CAPACITY_MB = 20 * 1024;

    /**
     * 磁盘最小空余容量（MB）
     * <p>
     * 默认值：0 MB
     * </p>
     * <p>
     * 在清理时，同最大缓存容量，如果当前磁盘容量小于该值，也会按规则一个一个的删除缓存文件，
     * 直到 freeStorage 大于等于该值或者所有缓存都被清理掉。
     * </p>
     * <p>
     * Minimum free storage (MB)
     * <p>
     * Default: 0 MB
     * </p>
     * <p>
     * During cleanup, similar to maximum cache capacity, if the current disk capacity is less than
     * this value, cache files will be deleted one by one according to the rules until freeStorage
     * is greater than or equal to this value or all caches are cleared.
     * </p>
     */
    private static final int LOCAL_CACHE_FREE_STORAGE_MB = 0;

    /**
     * 缓存目录名称
     * <p>
     * 统一约定在 cache 路径下的 Preload 目录。
     * </p>
     * <p>
     * Cache directory name
     * <p>
     * Standardize on the Preload directory under the cache path.
     * </p>
     */
    private static final String CACHE_DIR_NAME = "Preload";

    /**
     * 私有构造函数，防止实例化
     */
    private GlobalInitializer() {
        throw new UnsupportedOperationException("Cannot instantiate GlobalInitializer");
    }

    // ==================== 公开方法 ====================

    /**
     * 初始化全局设置
     * <p>
     * 配置播放器 SDK 的全局设置，包括：
     * <ul>
     *     <li>设置业务来源信息</li>
     *     <li>配置本地缓存</li>
     *     <li>设置缓存清除策略</li>
     * </ul>
     * </p>
     *
     * @param context 应用上下文，不能为 null
     * @throws Exception 如果配置失败
     */
    public static void initialize(@NonNull Context context) {
        try {
            setExtraData();
            setupLocalCache(context);
            setCacheClearConfig();
            LogHub.i(TAG, "Global settings initialized successfully");
        } catch (Exception e) {
            LogHub.e(TAG, "Failed to initialize global settings", e);
            throw e;
        }
    }

    /**
     * 清除所有缓存
     * <p>
     * Clear all caches
     * </p>
     */
    public static void clearCaches() {
        try {
            AliPlayerGlobalSettings.clearCaches();
            LogHub.i(TAG, "Caches cleared successfully");
        } catch (Exception e) {
            LogHub.e(TAG, "Failed to clear caches", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 设置业务来源信息
     */
    private static void setExtraData() {
        AliPlayerGlobalSettings.setOption(AliPlayerGlobalSettings.SET_EXTRA_DATA, EXTRA_DATA_ANDROID_PLAYER_KIT);
        LogHub.i(TAG, "Set extra data", EXTRA_DATA_ANDROID_PLAYER_KIT);
    }

    /**
     * 配置本地缓存
     *
     * @param context 应用上下文
     */
    private static void setupLocalCache(@NonNull Context context) {
        if (ENABLE_LOCAL_CACHE_FLAG) {
            String cacheDir = setupCacheDirectory(context);
            if (StringUtil.isNotEmpty(cacheDir)) {
                AliPlayerGlobalSettings.enableLocalCache(true, LOCAL_CACHE_MAX_BUFFER_MEMORY_KB, cacheDir);
                LogHub.i(TAG, "Local cache enabled", "cacheDir: " + cacheDir);
            } else {
                LogHub.e(TAG, "Cache directory setup failed, local cache not enabled");
            }
        } else {
            LogHub.i(TAG, "Local cache is disabled by configuration");
        }
    }

    /**
     * 设置缓存清除策略
     */
    private static void setCacheClearConfig() {
        AliPlayerGlobalSettings.setCacheFileClearConfig(
                LOCAL_CACHE_EXPIRE_MIN,
                LOCAL_CACHE_MAX_CAPACITY_MB,
                LOCAL_CACHE_FREE_STORAGE_MB
        );
        LogHub.i(TAG, "Cache clear config set",
                "expireMin=" + LOCAL_CACHE_EXPIRE_MIN,
                "maxCapacityMB=" + LOCAL_CACHE_MAX_CAPACITY_MB,
                "freeStorageMB=" + LOCAL_CACHE_FREE_STORAGE_MB);
    }

    /**
     * 设置缓存目录
     * <p>
     * 获取并创建缓存目录，优先使用外部缓存目录，如果不可用则使用内部缓存目录。
     * 缓存目录结构：{baseCacheDir}/AliPlayerKit/Preload
     * </p>
     *
     * @param context 应用上下文
     * @return 缓存目录路径，如果设置失败则返回空字符串
     */
    @NonNull
    private static String setupCacheDirectory(@NonNull Context context) {
        try {
            String baseCacheDir = FileUtil.getExternalCacheFolder(context);
            String finalCacheDir = FileUtil.combinePaths(baseCacheDir, PLAYER_KIT, CACHE_DIR_NAME);

            if (FileUtil.safeCreateFolder(finalCacheDir)) {
                LogHub.i(TAG, "Cache directory created", finalCacheDir);
                return finalCacheDir;
            } else {
                LogHub.w(TAG, "Failed to create cache directory", finalCacheDir);
                return StringUtil.EMPTY;
            }
        } catch (Exception e) {
            LogHub.e(TAG, "Exception while setting up cache directory", e);
            return StringUtil.EMPTY;
        }
    }
}
