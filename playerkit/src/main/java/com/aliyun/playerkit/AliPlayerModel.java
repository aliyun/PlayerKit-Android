package com.aliyun.playerkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.VideoSource;

/**
 * AliPlayerKit 播放数据模型
 * <p>
 * 封装了播放器所需的所有配置数据，包括视频源、场景类型、封面图、标题等。
 * 采用 Builder 模式创建实例，确保数据配置的完整性和可读性。
 * </p>
 * <p>
 * AliPlayerKit Data Model
 * <p>
 * Encapsulates all configuration data required by the player, including video source, scene type, cover image, title, etc.
 * Uses Builder pattern to create instances, ensuring data configuration integrity and readability.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class AliPlayerModel {

    /**
     * 视频资源（必填）
     * <p>
     * 支持 Direct URL / VidSts / VidAuth 等类型，详见 {@link VideoSource}。
     * </p>
     */
    @NonNull
    private final VideoSource videoSource;

    /**
     * 播放场景（可选，默认 VOD）
     * <p>
     * 影响控制器及插槽策略，例如 VOD/LIVE 的 UI 与行为差异。
     * </p>
     */
    @SceneType
    private final int sceneType;

    /**
     * 封面图 URL（可选）
     * <p>
     * Cover image URL (optional).
     * </p>
     */
    @Nullable
    private final String coverUrl;

    /**
     * 视频标题（可选）
     * <p>
     * Video title (optional).
     * </p>
     */
    @Nullable
    private final String videoTitle;

    /**
     * 是否自动播放，默认 true。
     * <p>
     * Whether to start automatically.
     * </p>
     */
    private final boolean autoPlay;

    /**
     * traceId，用于排障或统计，可为空。
     * <p>
     * Trace id for tracking. Can be null or empty string.
     * </p>
     */
    @Nullable
    private final String traceId;

    /**
     * 起播时间（ms），默认 0。
     * <p>
     * Start position in milliseconds.
     * </p>
     */
    private final long startTime;

    /**
     * 是否硬解，默认 true。
     * <p>
     * Whether to use hardware decoding, default true.
     * </p>
     */
    private final boolean isHardWareDecode;

    /**
     * 是否允许屏幕休眠，默认为 false
     * <p>
     * Whether to allow screen sleep, default false
     * </p>
     */
    private final boolean allowedScreenSleep;

    /**
     * 私有构造函数，通过 Builder 创建实例
     * <p>
     * Private constructor, create instance through Builder
     * </p>
     *
     * @param builder 构建器实例，包含所有配置数据
     */
    private AliPlayerModel(Builder builder) {
        this.videoSource = builder.videoSource;
        this.sceneType = builder.sceneType;
        this.coverUrl = builder.coverUrl;
        this.videoTitle = builder.videoTitle;
        this.autoPlay = builder.autoPlay;
        this.traceId = builder.traceId;
        this.startTime = builder.startTime;
        this.isHardWareDecode = builder.isHardWareDecode;
        this.allowedScreenSleep = builder.allowedScreenSleep;
    }

    /**
     * 获取视频资源对象
     * <p>
     * Get video source object
     * </p>
     *
     * @return 视频资源对象，不会为 null
     */
    @NonNull
    public VideoSource getVideoSource() {
        return videoSource;
    }

    /**
     * 获取播放场景类型
     * <p>
     * 如果未设置场景类型，则返回默认值 VOD。
     * </p>
     * <p>
     * Get playback scene type
     * <p>
     * If scene type is not set, returns default value VOD.
     * </p>
     *
     * @return 播放场景类型
     */
    @SceneType
    public int getSceneType() {
        return sceneType;
    }

    /**
     * 获取封面图地址
     * <p>
     * Get cover image URL
     * </p>
     *
     * @return 封面图 URL，可能为 null
     */
    @Nullable
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * 获取视频标题
     * <p>
     * Get video title
     * </p>
     *
     * @return 视频标题，可能为 null
     */
    @Nullable
    public String getVideoTitle() {
        return videoTitle;
    }

    /**
     * 获取是否自动播放
     * <p>
     * Get whether to auto-play
     * </p>
     *
     * @return true 表示自动播放，false 表示手动播放
     */
    public boolean isAutoPlay() {
        return autoPlay;
    }

    /**
     * 获取trace ID
     * <p>
     * Get trace ID
     * </p>
     *
     * @return trace ID，可能为 null 或空字符串
     */
    @Nullable
    public String getTraceId() {
        return traceId;
    }

    /**
     * 获取视频起始播放时间
     * <p>
     * Get video start playback time
     * </p>
     *
     * @return 起始播放时间，单位：毫秒，非负数
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 获取是否硬解
     * <p>
     * Get whether to use hardware decoding
     * </p>
     *
     * @return true 表示使用硬解，false 表示使用软解
     */
    public boolean isHardWareDecode() {
        return isHardWareDecode;
    }

    /**
     * 获取是否允许屏幕休眠
     * <p>
     * Get whether to allow screen sleep
     * </p>
     *
     * @return true 允许屏幕休眠，false 不允许屏幕休眠
     */
    public boolean isAllowedScreenSleep() {
        return allowedScreenSleep;
    }

    /**
     * 播放器数据构建器
     * <p>
     * 采用 Builder 模式，方便创建 {@link AliPlayerModel} 实例。
     * 支持链式调用，提供默认值，确保数据配置的灵活性。
     * </p>
     * <p>
     * Player Data Builder
     * <p>
     * Uses Builder pattern to conveniently create {@link AliPlayerModel} instances.
     * Supports method chaining, provides default values, ensuring data configuration flexibility.
     * </p>
     */
    public static class Builder {

        /**
         * 视频资源对象，必须设置
         */
        private VideoSource videoSource;

        /**
         * 播放场景类型，默认为点播（VOD）
         */
        @SceneType
        private int sceneType = SceneType.VOD;

        /**
         * 封面图地址，可以为 null
         */
        private String coverUrl;

        /**
         * 视频标题，可以为 null
         */
        private String videoTitle;

        /**
         * 是否自动播放，默认为 true
         */
        private boolean autoPlay = true;

        /**
         * trace ID，默认为空字符串
         */
        private String traceId = "";

        /**
         * 视频起始播放时间，单位：毫秒，默认为 0
         */
        private long startTime = 0;

        /**
         * 是否硬解，默认为 true
         */
        private boolean isHardWareDecode = true;

        /**
         * 是否允许屏幕休眠，默认为 false
         */
        private boolean allowedScreenSleep = false;

        /**
         * 设置视频资源对象
         * <p>
         * 定义视频播放源，支持 URL、VidSts、VidAuth 等多种方式。
         * 必须设置，且必须有效（通过 {@link VideoSource#isValid()} 验证）。
         * </p>
         * <p>
         * Set video source object
         * <p>
         * Defines the video playback source, supporting URL, VidSts, VidAuth, etc.
         * Must be set and must be valid (validated by {@link VideoSource#isValid()}).
         * </p>
         *
         * @param value 视频资源对象，不能为 null
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder videoSource(@NonNull VideoSource value) {
            this.videoSource = value;
            return this;
        }

        /**
         * 设置播放场景类型
         * <p>
         * 定义播放器的使用场景，如点播（VOD）、直播（LIVE）等。
         * 不同场景会影响播放器的行为和 UI 显示。
         * </p>
         * <p>
         * Set playback scene type
         * <p>
         * Defines the usage scenario of the player, such as VOD, LIVE, etc.
         * Different scenarios affect player behavior and UI display.
         * </p>
         *
         * @param value 场景类型
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder sceneType(@SceneType int value) {
            this.sceneType = value;
            return this;
        }

        /**
         * 设置封面图地址
         * <p>
         * 视频播放前的封面图片 URL，用于在视频加载时显示。
         * 可以为 null，表示不使用封面图。
         * </p>
         * <p>
         * Set cover image URL
         * <p>
         * The URL of the cover image displayed before video playback, used to show during video loading.
         * Can be null, indicating no cover image.
         * </p>
         *
         * @param value 封面图 URL，可以为 null
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder coverUrl(@Nullable String value) {
            this.coverUrl = value;
            return this;
        }

        /**
         * 设置视频标题
         * <p>
         * 视频的标题文本，用于在 UI 中显示。
         * 可以为 null，表示不显示标题。
         * </p>
         * <p>
         * Set video title
         * <p>
         * The title text of the video, used for display in the UI.
         * Can be null, indicating no title.
         * </p>
         *
         * @param value 视频标题，可以为 null
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder videoTitle(@Nullable String value) {
            this.videoTitle = value;
            return this;
        }

        /**
         * 设置是否自动播放
         * <p>
         * true 表示视频配置完成后自动开始播放，false 表示需要手动调用播放方法。
         * 默认为 true。
         * </p>
         * <p>
         * Set whether to auto-play
         * <p>
         * true means the video will automatically start playing after configuration, false means manual play call is required.
         * Default is true.
         * </p>
         *
         * @param value true 为自动播放，false 为手动播放
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder autoPlay(boolean value) {
            this.autoPlay = value;
            return this;
        }

        /**
         * 设置 trace ID
         * <p>
         * 用于跟踪和统计视频播放的标识符，通常用于日志记录和数据分析。
         * 可以为 null 或空字符串。
         * </p>
         * <p>
         * Set trace ID
         * <p>
         * Identifier used for tracking and statistics of video playback, typically used for logging and data analysis.
         * Can be null or empty string.
         * </p>
         *
         * @param value trace ID，可以为 null
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder traceId(@Nullable String value) {
            this.traceId = value;
            return this;
        }

        /**
         * 设置视频起始播放时间
         * <p>
         * 视频开始播放的时间点，单位：毫秒。
         * 如果传入负数，会自动转换为 0（从视频开头播放）。
         * </p>
         * <p>
         * Set video start playback time
         * <p>
         * The time point at which video playback starts, unit: milliseconds.
         * If a negative number is passed, it will be automatically converted to 0 (play from the beginning).
         * </p>
         *
         * @param value 起始时间，单位毫秒，如果为负数则自动转换为 0
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder startTime(long value) {
            this.startTime = Math.max(0, value);
            return this;
        }

        /**
         * 设置硬件解码
         * <p>
         * Set whether to use hardware decoding
         * </p>
         *
         * @param value true 为使用硬件解码，false 为使用软件解码，默认为 true
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder isHardWareDecode(boolean value) {
            this.isHardWareDecode = value;
            return this;
        }

        /**
         * 设置屏幕休眠
         * <p>
         * Set whether to allow screen sleep
         * </p>
         *
         * @param value true 为允许屏幕休眠，false 为不允许屏幕休眠，默认为 false
         * @return 构建器实例，支持链式调用
         */
        @NonNull
        public Builder allowedScreenSleep(boolean value) {
            this.allowedScreenSleep = value;
            return this;
        }

        /**
         * 构建 {@link AliPlayerModel} 实例
         * <p>
         * Build {@link AliPlayerModel} instance
         * </p>
         *
         * @return {@link AliPlayerModel} 实例，不会为 null
         * @throws IllegalArgumentException 如果视频源未设置（为 null）
         * @throws IllegalStateException    如果视频源无效（通过 {@link VideoSource#isValid()} 验证失败）
         */
        @NonNull
        public AliPlayerModel build() {
            if (videoSource == null) {
                throw new IllegalArgumentException("VideoSource can not be null");
            }

            if (!videoSource.isValid()) {
                throw new IllegalStateException("VideoSource is invalid: " + videoSource);
            }

            return new AliPlayerModel(this);
        }
    }

    /**
     * 返回当前 {@link AliPlayerModel} 的字符串描述。
     * <p>
     * 主要用于日志输出、调试和问题排查，方便快速查看播放器模型的关键配置状态。
     * </p>
     *
     * <p><b>注意：</b></p>
     * <ul>
     *     <li>该方法不用于业务逻辑判断，仅用于调试和日志场景。</li>
     *     <li>输出内容包含视频源、场景类型、播放参数等关键信息。</li>
     *     <li>请避免在对性能或隐私敏感的场景中频繁调用。</li>
     * </ul>
     *
     * @return 包含当前模型关键信息的字符串表示，保证非空
     */
    @NonNull
    @Override
    public String toString() {
        return "AliPlayerModel{" +
                "videoSource=" + videoSource +
                ", sceneType=" + sceneType +
                ", coverUrl='" + coverUrl + '\'' +
                ", videoTitle='" + videoTitle + '\'' +
                ", autoPlay=" + autoPlay +
                ", traceId='" + traceId + '\'' +
                ", startTime=" + startTime +
                ", isHardWareDecode=" + isHardWareDecode +
                ", allowedScreenSleep=" + allowedScreenSleep +
                '}';
    }
}
