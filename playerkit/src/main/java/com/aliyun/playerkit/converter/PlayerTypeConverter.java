package com.aliyun.playerkit.converter;

import android.widget.ImageView;

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
                // 非法入参退回 IMediaPlayer.ScaleType.DEFAULT 的映射结果。
                // @IntDef int 不是枚举，编译器无法证明穷尽，语法上必须保留 default 分支；
                // 这里不硬编码兜底值，而是递归到 DEFAULT —— 默认值改动时兜底自动跟随，
                // 不需要人工同步。DEFAULT 是上面三个取值之一的别名，递归必然命中具体 case，不会再落回本分支
                return convertScaleType(IMediaPlayer.ScaleType.DEFAULT);
        }
    }

    /**
     * 转换渲染填充模式（Android ImageView 侧）。
     * <p>
     * 将 {@link IMediaPlayer.ScaleType} 转换为 {@link ImageView.ScaleType}，供封面图等覆盖在渲染视图之上的
     * 图片层与播放器画面保持几何一致。在图片宽高比等于视频宽高比的前提下，
     * {@link ImageView.ScaleType} 算出的矩形与 SDK 渲染矩形逐像素重合，无需任何手工计算。
     * </p>
     *
     * <p>
     * Converts {@link IMediaPlayer.ScaleType} to {@link ImageView.ScaleType}, so image layers drawn on top of
     * the rendering view (e.g. the cover image) stay geometrically aligned with the video frame.
     * </p>
     *
     * @param scaleType 填充模式 / Scale mode
     * @return ImageView 缩放模式 / ImageView scale type
     */
    public static ImageView.ScaleType convertToImageScaleType(@IMediaPlayer.ScaleType int scaleType) {
        switch (scaleType) {
            case IMediaPlayer.ScaleType.FIT_XY:
                return ImageView.ScaleType.FIT_XY;
            case IMediaPlayer.ScaleType.FIT_CENTER:
                // 不用 CENTER_INSIDE：图片小于视图时 FIT_CENTER 会等比放大到贴边，
                // 与 SDK 的 SCALE_ASPECT_FIT 行为一致，CENTER_INSIDE 不放大会对不齐
                return ImageView.ScaleType.FIT_CENTER;
            case IMediaPlayer.ScaleType.CENTER_CROP:
                return ImageView.ScaleType.CENTER_CROP;
            default:
                // 兜底方式同 convertScaleType（见该方法注释）
                return convertToImageScaleType(IMediaPlayer.ScaleType.DEFAULT);
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
            case IMediaPlayer.MirrorType.HORIZONTAL:
                return IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL;
            case IMediaPlayer.MirrorType.VERTICAL:
                return IPlayer.MirrorMode.MIRROR_MODE_VERTICAL;
            case IMediaPlayer.MirrorType.NONE:
            default:
                // 非法入参退回不镜像
                return IPlayer.MirrorMode.MIRROR_MODE_NONE;
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
            case IMediaPlayer.Rotation.DEGREE_90:
                return IPlayer.RotateMode.ROTATE_90;
            case IMediaPlayer.Rotation.DEGREE_180:
                return IPlayer.RotateMode.ROTATE_180;
            case IMediaPlayer.Rotation.DEGREE_270:
                return IPlayer.RotateMode.ROTATE_270;
            case IMediaPlayer.Rotation.DEGREE_0:
            default:
                // 非法入参退回不旋转
                return IPlayer.RotateMode.ROTATE_0;
        }
    }
}
