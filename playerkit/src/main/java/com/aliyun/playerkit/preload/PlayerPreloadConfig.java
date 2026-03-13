package com.aliyun.playerkit.preload;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.utils.StringUtil;

/**
 * 播放器预加载配置类
 *
 * <p>
 * 用于定义播放器预加载行为参数，目前支持：
 * <ul>
 *     <li>预加载时长（毫秒）</li>
 *     <li>默认清晰度</li>
 * </ul>
 * </p>
 *
 * <p>
 * Player Preload Configuration Class
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class PlayerPreloadConfig {

    /**
     * 默认预加载时长（毫秒）
     */
    private static final int DEFAULT_PRELOAD_DURATION = 3 * 1000;

    /**
     * 默认清晰度
     */
    private static final String DEFAULT_QUALITY = "AUTO";

    /**
     * 预加载时长（毫秒）
     */
    private int preloadDuration;

    /**
     * 默认清晰度
     */
    private String defaultQuality;

    /**
     * 默认构造函数
     */
    public PlayerPreloadConfig() {
        this(DEFAULT_PRELOAD_DURATION, DEFAULT_QUALITY);
    }

    /**
     * 指定预加载时长
     *
     * @param preloadDuration 预加载时长（毫秒）
     */
    public PlayerPreloadConfig(int preloadDuration) {
        this(preloadDuration, DEFAULT_QUALITY);
    }

    /**
     * 指定默认清晰度
     *
     * @param defaultQuality 默认清晰度
     */
    public PlayerPreloadConfig(String defaultQuality) {
        this(DEFAULT_PRELOAD_DURATION, defaultQuality);
    }

    /**
     * 指定预加载时长和默认清晰度
     *
     * @param preloadDuration 预加载时长（毫秒）
     * @param defaultQuality  默认清晰度
     */
    public PlayerPreloadConfig(int preloadDuration, String defaultQuality) {
        setPreloadDuration(preloadDuration);
        setDefaultQuality(defaultQuality);
    }

    /**
     * 获取预加载时长（毫秒）
     */
    public int getPreloadDuration() {
        return preloadDuration;
    }

    /**
     * 设置预加载时长（毫秒）
     *
     * @param preloadDuration 时长，必须 >= 0
     */
    public void setPreloadDuration(int preloadDuration) {
        this.preloadDuration = Math.max(0, preloadDuration);
    }

    /**
     * 获取默认清晰度
     */
    public String getDefaultQuality() {
        return defaultQuality;
    }

    /**
     * 设置默认清晰度
     *
     * @param defaultQuality 清晰度，不能为空
     */
    public void setDefaultQuality(String defaultQuality) {
        if (StringUtil.isEmpty(defaultQuality)) {
            this.defaultQuality = DEFAULT_QUALITY;
        } else {
            this.defaultQuality = defaultQuality;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayerPreloadConfig{" +
                "preloadDuration=" + preloadDuration +
                ", defaultQuality='" + defaultQuality + '\'' +
                '}';
    }
}
