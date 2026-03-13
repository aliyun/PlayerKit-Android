package com.aliyun.playerkit.converter;

import androidx.annotation.NonNull;

import com.aliyun.player.source.SourceBase;
import com.aliyun.player.source.UrlSource;
import com.aliyun.player.source.VidAuth;
import com.aliyun.player.source.VidSts;
import com.aliyun.playerkit.data.VideoSource;

/**
 * 视频源转换工具类
 * <p>
 * 提供 {@link VideoSource} 与播放器 SDK 数据源（UrlSource、VidSts、VidAuth）之间的转换功能。
 * </p>
 * <p>
 * Video Source Converter Utility
 * <p>
 * Provides conversion between {@link VideoSource} and player SDK sources (UrlSource, VidSts, VidAuth).
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public final class VideoSourceConverter {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private VideoSourceConverter() {
        throw new UnsupportedOperationException("Cannot instantiate VideoSourceConverter");
    }

    /**
     * 将 {@link VideoSource} 转换为播放器 SDK 的数据源对象
     * <p>
     * 支持的转换类型：
     * <ul>
     *     <li>{@link VideoSource.UrlSource} -> {@link UrlSource}</li>
     *     <li>{@link VideoSource.VidStsSource} -> {@link VidSts}</li>
     *     <li>{@link VideoSource.VidAuthSource} -> {@link VidAuth}</li>
     * </ul>
     * </p>
     * <p>
     * Convert {@link VideoSource} to player SDK source object
     * </p>
     *
     * @param videoSource AliPlayerKit 的视频源对象，不能为 null
     * @return 播放器 SDK 的数据源对象（UrlSource、VidSts 或 VidAuth）
     * @throws IllegalArgumentException 如果 videoSource 为 null 或不支持的类型
     */
    @NonNull
    public static SourceBase convert(@NonNull VideoSource videoSource) {
        if (videoSource == null) {
            throw new IllegalArgumentException("videoSource cannot be null");
        }

        if (videoSource instanceof VideoSource.UrlSource) {
            return convertUrlSource((VideoSource.UrlSource) videoSource);
        } else if (videoSource instanceof VideoSource.VidStsSource) {
            return convertVidStsSource((VideoSource.VidStsSource) videoSource);
        } else if (videoSource instanceof VideoSource.VidAuthSource) {
            return convertVidAuthSource((VideoSource.VidAuthSource) videoSource);
        } else {
            throw new IllegalArgumentException("Unsupported video source type: " + videoSource.getClass().getSimpleName());
        }
    }

    /**
     * 转换 UrlSource
     *
     * @param urlSource AliPlayerKit 的 UrlSource
     * @return 播放器 SDK 的 UrlSource
     */
    @NonNull
    private static UrlSource convertUrlSource(@NonNull VideoSource.UrlSource urlSource) {
        UrlSource sdkUrlSource = new UrlSource();
        sdkUrlSource.setUri(urlSource.getUrl());
        return sdkUrlSource;
    }

    /**
     * 转换 VidStsSource
     *
     * @param stsSource AliPlayerKit 的 VidStsSource
     * @return 播放器 SDK 的 VidSts
     */
    @NonNull
    private static VidSts convertVidStsSource(@NonNull VideoSource.VidStsSource stsSource) {
        VidSts vidSts = new VidSts();
        vidSts.setVid(stsSource.getVid());
        vidSts.setAccessKeyId(stsSource.getAccessKeyId());
        vidSts.setAccessKeySecret(stsSource.getAccessKeySecret());
        vidSts.setSecurityToken(stsSource.getSecurityToken());
        if (stsSource.getRegion() != null) {
            vidSts.setRegion(stsSource.getRegion());
        }
        return vidSts;
    }

    /**
     * 转换 VidAuthSource
     *
     * @param authSource AliPlayerKit 的 VidAuthSource
     * @return 播放器 SDK 的 VidAuth
     */
    @NonNull
    private static VidAuth convertVidAuthSource(@NonNull VideoSource.VidAuthSource authSource) {
        VidAuth vidAuth = new VidAuth();
        vidAuth.setVid(authSource.getVid());
        vidAuth.setPlayAuth(authSource.getPlayAuth());
        return vidAuth;
    }
}
