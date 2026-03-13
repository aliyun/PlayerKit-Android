package com.aliyun.playerkit.converter;

import com.aliyun.player.IPlayer;
import com.aliyun.playerkit.player.IMediaPlayer;

/**
 * 播放器类型转换工具类。
 * <p>
 * 提供 {@link IMediaPlayer} 的通用类型定义与阿里云播放器 SDK（{@link IPlayer}）内部常量之间的转换。
 * </p>
 *
 * <p>
 * Player type conversion utilities.
 * Provides conversions between {@link IMediaPlayer} types and {@link IPlayer} internal constants.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public final class PlayerTypeConverter {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private PlayerTypeConverter() {
        throw new UnsupportedOperationException("Cannot instantiate PlayerTypeConverter");
    }

    /**
     * 默认渲染填充模式
     * <p>
     * Default scale mode
     */
    private static final IPlayer.ScaleMode DEFAULT_SCALE_MODE = IPlayer.ScaleMode.SCALE_ASPECT_FIT;

    /**
     * 默认镜像模式
     * <p>
     * Default mirror mode
     */
    private static final IPlayer.MirrorMode DEFAULT_MIRROR_MODE = IPlayer.MirrorMode.MIRROR_MODE_NONE;

    /**
     * 默认旋转角度
     * <p>
     * Default rotate mode
     */
    private static final IPlayer.RotateMode DEFAULT_ROTATE_MODE = IPlayer.RotateMode.ROTATE_0;

    /**
     * 转换渲染填充模式。
     * <p>
     * 将 {@link IMediaPlayer.ScaleType} 转换为 {@link IPlayer.ScaleMode}。
     * </p>
     *
     * <p>
     * Converts {@link IMediaPlayer.ScaleType} to {@link IPlayer.ScaleMode}.
     * </p>
     *
     * @param scaleType 填充模式 / Scale mode
     * @return IPlayer 内部填充模式 / IPlayer scale mode
     */
    public static IPlayer.ScaleMode convertScaleType(@IMediaPlayer.ScaleType int scaleType) {
        switch (scaleType) {
            case IMediaPlayer.ScaleType.FIT_XY:
                return IPlayer.ScaleMode.SCALE_TO_FILL;
            case IMediaPlayer.ScaleType.FIT_CENTER:
                return IPlayer.ScaleMode.SCALE_ASPECT_FIT;
            case IMediaPlayer.ScaleType.CENTER_CROP:
                return IPlayer.ScaleMode.SCALE_ASPECT_FILL;
            default:
                return DEFAULT_SCALE_MODE;
        }
    }

    /**
     * 转换镜像模式。
     * <p>
     * 将 {@link IMediaPlayer.MirrorType} 转换为 {@link IPlayer.MirrorMode}。
     * </p>
     *
     * <p>
     * Converts {@link IMediaPlayer.MirrorType} to {@link IPlayer.MirrorMode}.
     * </p>
     *
     * @param mirrorType 镜像模式 / Mirror mode
     * @return IPlayer 内部镜像模式 / IPlayer mirror mode
     */
    public static IPlayer.MirrorMode convertMirrorType(@IMediaPlayer.MirrorType int mirrorType) {
        switch (mirrorType) {
            case IMediaPlayer.MirrorType.NONE:
                return IPlayer.MirrorMode.MIRROR_MODE_NONE;
            case IMediaPlayer.MirrorType.HORIZONTAL:
                return IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL;
            case IMediaPlayer.MirrorType.VERTICAL:
                return IPlayer.MirrorMode.MIRROR_MODE_VERTICAL;
            default:
                return DEFAULT_MIRROR_MODE;
        }
    }

    /**
     * 转换旋转角度。
     * <p>
     * 将 {@link IMediaPlayer.Rotation}（单位：度）转换为 {@link IPlayer.RotateMode}。
     * </p>
     *
     * <p>
     * Converts {@link IMediaPlayer.Rotation} (in degrees) to {@link IPlayer.RotateMode}.
     * </p>
     *
     * @param rotation 旋转角度（0/90/180/270）/ Rotation angle in degrees (0/90/180/270)
     * @return IPlayer 内部旋转模式 / IPlayer rotate mode
     */
    public static IPlayer.RotateMode convertRotation(@IMediaPlayer.Rotation int rotation) {
        switch (rotation) {
            case IMediaPlayer.Rotation.DEGREE_0:
                return IPlayer.RotateMode.ROTATE_0;
            case IMediaPlayer.Rotation.DEGREE_90:
                return IPlayer.RotateMode.ROTATE_90;
            case IMediaPlayer.Rotation.DEGREE_180:
                return IPlayer.RotateMode.ROTATE_180;
            case IMediaPlayer.Rotation.DEGREE_270:
                return IPlayer.RotateMode.ROTATE_270;
            default:
                return DEFAULT_ROTATE_MODE;
        }
    }
}
