package com.aliyun.playerkit.data;

import static java.lang.annotation.ElementType.TYPE_USE;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AliPlayerKit 视频资源类型定义
 * <p>
 * 定义了播放视频时支持的不同资源类型。
 * 用于区分不同的视频源获取方式，包括 URL、VidSts、VidAuth 三种类型。
 * </p>
 * <p>
 * AliPlayerKit Playback Source Type Definition
 * <p>
 * Defines the different types of playback sources supported for video playback.
 * Used to distinguish different video source acquisition methods, including URL, VidSts, and VidAuth types.
 * </p>
 *
 * @author keria
 * @date 2024/11/26
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(TYPE_USE)
@IntDef({
        SourceType.VID_AUTH,
        SourceType.VID_STS,
        SourceType.URL,
})
public @interface SourceType {

    /**
     * VidAuth 类型
     * <p>
     * 表示通过 VID 和 Auth（授权认证）播放视频资源。
     * 使用播放授权码进行授权，适用于需要更简单授权机制的场景。
     * </p>
     * <p>
     * VidAuth Type
     * <p>
     * The video resource is played using a Video ID (VID) and Authorization (Auth).
     * Uses play authorization code for authorization, suitable for scenarios requiring simpler authorization mechanisms.
     * </p>
     */
    int VID_AUTH = 0;

    /**
     * VidSts 类型
     * <p>
     * 表示通过 VID（视频唯一标识符）和 STS（安全令牌服务）播放视频资源。
     * 使用阿里云 STS (Security Token Service) 进行授权，提供更高的安全性和访问控制。
     * 适用于需要临时访问凭证的场景。
     * </p>
     * <p>
     * VidSts Type
     * <p>
     * The video resource is played using a Video ID (VID) and Security Token Service (STS).
     * Uses Alibaba Cloud STS (Security Token Service) for authorization, providing higher security and access control.
     * Suitable for scenarios requiring temporary access credentials.
     * </p>
     */
    int VID_STS = 1;

    /**
     * URL 类型
     * <p>
     * 表示通过直接 URL 播放视频资源。
     * 适用于公开访问的视频资源，无需额外的授权验证。
     * </p>
     * <p>
     * URL Type
     * <p>
     * The video resource is played via a direct URL.
     * Suitable for publicly accessible video resources without additional authorization verification.
     * </p>
     */
    int URL = 2;
}
