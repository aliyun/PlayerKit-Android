package com.aliyun.playerkit.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * AliPlayerKit 视频源工厂类
 * <p>
 * 用于创建不同类型的视频源实例。
 * 提供便捷的静态方法，简化视频源的创建过程。
 * </p>
 * <p>
 * AliPlayerKit Video Source Factory
 * <p>
 * Used to create instances of different types of video sources.
 * Provides convenient static methods to simplify the video source creation process.
 * </p>
 *
 * @author keria
 * @date 2024/11/26
 */
public final class VideoSourceFactory {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private VideoSourceFactory() {
        throw new UnsupportedOperationException("Cannot instantiate VideoSourceFactory");
    }

    /**
     * 创建 VidAuth 类型的视频源
     * <p>
     * 通过 VID 和播放授权码播放视频，使用播放授权码进行授权。
     * 适用于需要更简单授权机制的场景。
     * </p>
     * <p>
     * Create a VidAuth-type video source
     * <p>
     * Play videos via VID and play authorization code, using play authorization code for authorization.
     * Suitable for scenarios requiring simpler authorization mechanisms.
     * </p>
     *
     * @param vid      视频唯一标识符，不能为 null 或空字符串
     * @param playAuth 播放授权码，不能为 null 或空字符串
     * @return VidAuth 类型的视频源实例
     * @throws IllegalArgumentException 如果 vid 或 playAuth 为 null 或空字符串
     */
    @NonNull
    public static VideoSource.VidAuthSource createVidAuthSource(@NonNull String vid, @NonNull String playAuth) {
        return new VideoSource.VidAuthSource(vid, playAuth);
    }

    /**
     * 创建 VidSts 类型的视频源
     * <p>
     * 通过 VID 和 STS 令牌播放视频，使用阿里云 STS 进行授权。
     * 适用于需要临时访问凭证的场景，提供更高的安全性和访问控制。
     * </p>
     * <p>
     * Create a VidSts-type video source
     * <p>
     * Play videos via VID and STS token, using Alibaba Cloud STS for authorization.
     * Suitable for scenarios requiring temporary access credentials, providing higher security and access control.
     * </p>
     *
     * @param vid             视频唯一标识符，不能为 null 或空字符串
     * @param accessKeyId     访问密钥 ID，不能为 null 或空字符串
     * @param accessKeySecret 访问密钥密文，不能为 null 或空字符串
     * @param securityToken   安全令牌，不能为 null 或空字符串
     * @param region          区域信息，可以为 null
     * @return VidSts 类型的视频源实例
     * @throws IllegalArgumentException 如果必需参数为 null 或空字符串
     */
    @NonNull
    public static VideoSource.VidStsSource createVidStsSource(@NonNull String vid, @NonNull String accessKeyId, @NonNull String accessKeySecret, @NonNull String securityToken, @Nullable String region) {
        return new VideoSource.VidStsSource(vid, accessKeyId, accessKeySecret, securityToken, region);
    }

    /**
     * 创建 URL 类型的视频源
     * <p>
     * 通过直接 URL 播放视频，适用于公开访问的视频资源。
     * </p>
     * <p>
     * Create a URL-type video source
     * <p>
     * Play videos via direct URL, suitable for publicly accessible video resources.
     * </p>
     *
     * @param url 视频 URL 地址，不能为 null 或空字符串
     * @return URL 类型的视频源实例
     * @throws IllegalArgumentException 如果 url 为 null 或空字符串
     */
    @NonNull
    public static VideoSource.UrlSource createUrlSource(@NonNull String url) {
        return new VideoSource.UrlSource(url);
    }
}
