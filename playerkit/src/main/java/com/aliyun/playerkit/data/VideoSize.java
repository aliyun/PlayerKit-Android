package com.aliyun.playerkit.data;

import androidx.annotation.NonNull;

/**
 * 视频尺寸
 * <p>
 * 表示视频的宽度和高度信息。
 * </p>
 * <p>
 * Video Size
 * <p>
 * Represents the width and height information of a video.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public final class VideoSize {

    /**
     * 视频宽度
     */
    private final int width;

    /**
     * 视频高度
     */
    private final int height;

    /**
     * 构造视频尺寸
     * <p>
     * Constructor
     * </p>
     *
     * @param width  视频宽度
     * @param height 视频高度
     */
    public VideoSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 获取视频宽度
     * <p>
     * Get video width
     * </p>
     *
     * @return 视频宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取视频高度
     * <p>
     * Get video height
     * </p>
     *
     * @return 视频高度
     */
    public int getHeight() {
        return height;
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoSize{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
