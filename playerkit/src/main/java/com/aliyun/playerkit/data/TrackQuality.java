package com.aliyun.playerkit.data;

import static java.lang.annotation.ElementType.TYPE_USE;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用清晰度数据模型
 * <p>
 * 用于屏蔽底层播放器 SDK 的具体实现，提供统一的清晰度信息描述。
 * </p>
 * <p>
 * Generic Track Quality Data Model
 * <p>
 * Used to shield the specific implementation of the underlying player SDK and provide a unified description of clarity information.
 * </p>
 *
 * @author keria
 * @date 2025/12/26
 */
public class TrackQuality {

    /**
     * TrackType 用于限定轨道（Track）的类型取值范围。
     * <p>
     * 该注解主要用于方法参数、返回值或变量声明，
     * 用于标识媒体轨道的具体类型。
     * </p>
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target(TYPE_USE)
    @IntDef(open = true, value = {
            TrackType.UNKNOWN,
            TrackType.VIDEO,
            TrackType.AUDIO,
            TrackType.SUBTITLE,
            TrackType.VOD,
    })
    public @interface TrackType {

        /**
         * 未知类型
         */
        int UNKNOWN = 0;

        /**
         * 视频轨道
         */
        int VIDEO = 1;

        /**
         * 音频轨道
         */
        int AUDIO = 2;

        /**
         * 字幕轨道
         */
        int SUBTITLE = 3;

        /**
         * 点播（VOD）轨道
         */
        int VOD = 4;
    }

    /**
     * 轨道索引。
     * <p>
     * 通常与底层播放器 SDK 中的轨道索引保持一致。
     * </p>
     */
    private final int index;

    /**
     * 轨道类型。
     * <p>
     * 取值范围受 {@link TrackType} 限定。
     * </p>
     */
    private final @TrackType int type;

    /**
     * 视频宽度（像素）。
     * <p>
     * 对于非视频轨道（如音频、字幕），该值可能为 0。
     * </p>
     */
    private final int width;

    /**
     * 视频高度（像素）。
     * <p>
     * 对于非视频轨道（如音频、字幕），该值可能为 0。
     * </p>
     */
    private final int height;

    /**
     * 构造一个 TrackQuality 实例。
     *
     * @param index  轨道索引
     * @param type   轨道类型，取值范围由 {@link TrackType} 限定
     * @param width  视频宽度（像素）
     * @param height 视频高度（像素）
     */
    public TrackQuality(int index, @TrackType int type, int width, int height) {
        this.index = index;
        this.type = type;
        this.width = width;
        this.height = height;
    }

    /**
     * 获取轨道索引。
     */
    public int getIndex() {
        return index;
    }

    /**
     * 获取轨道类型。
     *
     * @return 轨道类型，取值范围由 {@link TrackType} 限定
     */
    public @TrackType int getType() {
        return type;
    }

    /**
     * 获取视频宽度（像素）。
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取视频高度（像素）。
     */
    public int getHeight() {
        return height;
    }

    /**
     * 返回 TrackQuality 的字符串表示
     * <p>
     * 主要用于日志输出与调试。
     */
    @NonNull
    @Override
    public String toString() {
        return "TrackQuality{" +
                "index=" + index +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
