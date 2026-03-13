package com.aliyun.playerkit.preload;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.data.VideoSource;

/**
 * 播放器预加载任务类
 * <p>
 * 用于封装一次预加载任务的输入,包括数据源和预加载配置。
 * </p>
 * <p>
 * Player Preload Task Class
 * <p>
 * Represents a preload task consisting of a data source and preload configuration.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class PlayerPreloadTask {

    /**
     * 数据源,支持不同类型的数据源
     * <p>
     * Data source, supports multiple types (UrlSource / VidAuthSource / VidStsSource)
     * </p>
     */
    private final VideoSource source;

    /**
     * 预加载配置
     * <p>
     * Preload configuration
     * </p>
     */
    private final PlayerPreloadConfig preloadConfig;

    /**
     * 使用 VidAuthSource 类型数据源构造预加载任务
     * <p>
     * Construct preload task with VidAuthSource
     * </p>
     *
     * @param source        VidAuthSource 类型数据源 {@link VideoSource.VidAuthSource}
     * @param preloadConfig 预加载配置
     */
    public PlayerPreloadTask(@NonNull VideoSource.VidAuthSource source, @NonNull PlayerPreloadConfig preloadConfig) {
        this.source = source;
        this.preloadConfig = preloadConfig;
    }

    /**
     * 使用 VidStsSource 类型数据源构造预加载任务
     * <p>
     * Construct preload task with VidStsSource
     * </p>
     *
     * @param source        VidStsSource 类型数据源 {@link VideoSource.VidStsSource}
     * @param preloadConfig 预加载配置
     */
    public PlayerPreloadTask(@NonNull VideoSource.VidStsSource source, @NonNull PlayerPreloadConfig preloadConfig) {
        this.source = source;
        this.preloadConfig = preloadConfig;
    }

    /**
     * 使用 UrlSource 类型数据源构造预加载任务
     * <p>
     * Construct preload task with UrlSource
     * </p>
     *
     * @param source        UrlSource 类型数据源 {@link VideoSource.UrlSource}
     * @param preloadConfig 预加载配置
     */
    public PlayerPreloadTask(@NonNull VideoSource.UrlSource source, @NonNull PlayerPreloadConfig preloadConfig) {
        this.source = source;
        this.preloadConfig = preloadConfig;
    }

    /**
     * 获取预加载配置
     * <p>
     * Get preload configuration
     * </p>
     *
     * @return PlayerPreloadConfig
     */
    @NonNull
    public PlayerPreloadConfig getPreloadConfig() {
        return preloadConfig;
    }

    /**
     * 获取数据源
     * <p>
     * Get data source
     * </p>
     *
     * @return VideoSource
     */
    @NonNull
    public VideoSource getSource() {
        return source;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayerPreloadTask{" +
                "source=" + source +
                ", preloadConfig=" + preloadConfig +
                '}';
    }

}
