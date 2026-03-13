package com.aliyun.playerkit.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.utils.PrivacyUtil;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AliPlayerKit 视频播放源基类
 * <p>
 * 定义了所有播放源类型的共同接口，支持不同类型的视频资源播放。
 * 所有视频源类型都应该继承此类并实现相应的接口方法。
 * </p>
 * <p>
 * AliPlayerKit Video Playback Source Base Class
 * <p>
 * Defines the common interface for all playback source types, supporting different types of video resources.
 * All video source types should extend this class and implement the corresponding interface methods.
 * </p>
 *
 * @author keria
 * @date 2024/11/26
 */
public abstract class VideoSource {

    /**
     * 获取视频源类型
     * <p>
     * 返回当前视频源的类型标识，用于区分不同的视频源类型。
     * </p>
     * <p>
     * Get the source type of the video
     * <p>
     * Returns the type identifier of the current video source, used to distinguish different video source types.
     * </p>
     *
     * @return 视频源类型
     */
    @SourceType
    public abstract int getSourceType();

    /**
     * 将当前配置转换为可下发给 AliPlayer 的键值对
     * <p>
     * 将视频源配置转换为 Map 格式，便于传递给底层播放器 SDK。
     * </p>
     * <p>
     * Convert to a configuration map that can be used by the player
     * <p>
     * Converts the video source configuration to Map format for passing to the underlying player SDK.
     * </p>
     *
     * @return 配置映射表，不可修改
     */
    @NonNull
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(new HashMap<String, Object>());
    }

    /**
     * 验证当前资源配置是否有效
     * <p>
     * 检查视频源的所有必需参数是否都已正确设置。
     * 在配置播放器之前应该调用此方法进行验证。
     * </p>
     * <p>
     * Validate if the current resource configuration is valid
     * <p>
     * Checks if all required parameters of the video source are correctly set.
     * This method should be called before configuring the player.
     * </p>
     *
     * @return true 如果配置有效，false 否则
     */
    public abstract boolean isValid();

    /**
     * 获取视频源的唯一标识符
     * <p>
     * 用于在播放器池中标识和复用播放器实例。
     * 相同 MediaId 的视频源会复用同一个播放器实例。
     * </p>
     * <p>
     * Get the unique identifier of the video source
     * <p>
     * Used to identify and reuse player instances in the player pool.
     * Video sources with the same MediaId will reuse the same player instance.
     * </p>
     *
     * @return 视频源的唯一标识符，不会为 null
     */
    @NonNull
    public abstract String getMediaId();

    /**
     * 重写 toString 方法
     * <p>
     * 返回当前视频源的描述信息，用于调试和日志记录。
     * </p>
     * <p>
     * Override the toString method
     * <p>
     * Returns a description of the current video source for debugging and logging.
     * </p>
     *
     * @return 视频源的描述信息
     */
    @Override
    @NonNull
    public abstract String toString();

    /**
     * URL 类型的视频源
     * <p>
     * 通过直接 URL 播放视频的资源类型。
     * 适用于公开访问的视频资源，无需额外的授权验证。
     * </p>
     * <p>
     * URL-based Video Source
     * <p>
     * Resource type for playing videos via direct URL.
     * Suitable for publicly accessible video resources without additional authorization verification.
     * </p>
     */
    public static final class UrlSource extends VideoSource {
        /**
         * 视频 URL 地址
         * <p>
         * Video URL address
         * </p>
         */
        @NonNull
        private final String url;

        /**
         * 构造函数
         * <p>
         * Constructor
         * </p>
         *
         * @param url 视频 URL 地址，不能为 null 或空字符串
         */
        public UrlSource(@NonNull String url) {
            this.url = url;
        }

        @Override
        @SourceType
        public int getSourceType() {
            return SourceType.URL;
        }

        @Override
        public boolean isValid() {
            return StringUtil.noneEmpty(url);
        }

        @NonNull
        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("url", url);
            return Collections.unmodifiableMap(map);
        }

        /**
         * 获取视频 URL 地址
         * <p>
         * Get the video URL address
         * </p>
         *
         * @return 视频 URL 地址
         */
        @NonNull
        public String getUrl() {
            return url;
        }

        @Override
        @NonNull
        public String getMediaId() {
            return "url:" + url;
        }

        @NonNull
        @Override
        public String toString() {
            return "UrlSource{" +
                    "url='" + PrivacyUtil.removeAuthKeyFromUrl(url) + '\'' +
                    '}';
        }
    }

    /**
     * VidSts 类型的视频源
     * <p>
     * 通过 VID 和 STS 令牌播放视频的资源类型。
     * 使用阿里云 STS (Security Token Service) 进行授权，提供更高的安全性和访问控制。
     * 适用于需要临时访问凭证的场景。
     * </p>
     * <p>
     * VidSts-based Video Source
     * <p>
     * Resource type for playing videos via VID and STS token.
     * Uses Alibaba Cloud STS (Security Token Service) for authorization, providing higher security and access control.
     * Suitable for scenarios requiring temporary access credentials.
     * </p>
     */
    public static final class VidStsSource extends VideoSource {
        /**
         * 视频唯一标识符
         * <p>
         * Video unique identifier
         * </p>
         */
        @NonNull
        private final String vid;

        /**
         * 访问密钥 ID
         * <p>
         * Access key ID
         * </p>
         */
        @NonNull
        private final String accessKeyId;

        /**
         * 访问密钥密文
         * <p>
         * Access key secret
         * </p>
         */
        @NonNull
        private final String accessKeySecret;

        /**
         * 安全令牌
         * <p>
         * Security token
         * </p>
         */
        @NonNull
        private final String securityToken;

        /**
         * 区域信息
         * <p>
         * Region information
         * </p>
         */
        @Nullable
        private final String region;

        /**
         * 构造函数
         * <p>
         * Constructor
         * </p>
         *
         * @param vid             视频唯一标识符，不能为 null 或空字符串
         * @param accessKeyId     访问密钥 ID，不能为 null 或空字符串
         * @param accessKeySecret 访问密钥密文，不能为 null 或空字符串
         * @param securityToken   安全令牌，不能为 null 或空字符串
         * @param region          区域信息，可以为 null
         */
        public VidStsSource(@NonNull String vid, @NonNull String accessKeyId, @NonNull String accessKeySecret, @NonNull String securityToken, @Nullable String region) {
            this.vid = vid;
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
            this.securityToken = securityToken;
            this.region = region;
        }

        @Override
        @SourceType
        public int getSourceType() {
            return SourceType.VID_STS;
        }

        @Override
        public boolean isValid() {
            return StringUtil.noneEmpty(vid, accessKeyId, accessKeySecret, securityToken);
        }

        @NonNull
        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("vid", vid);
            map.put("accessKeyId", accessKeyId);
            map.put("accessKeySecret", accessKeySecret);
            map.put("securityToken", securityToken);
            if (StringUtil.isNotEmpty(region)) {
                map.put("region", region);
            }
            return Collections.unmodifiableMap(map);
        }

        /**
         * 获取视频唯一标识符
         * <p>
         * Get the video unique identifier
         * </p>
         *
         * @return 视频唯一标识符
         */
        @NonNull
        public String getVid() {
            return vid;
        }

        /**
         * 获取访问密钥 ID
         * <p>
         * Get the access key ID
         * </p>
         *
         * @return 访问密钥 ID
         */
        @NonNull
        public String getAccessKeyId() {
            return accessKeyId;
        }

        /**
         * 获取访问密钥密文
         * <p>
         * Get the access key secret
         * </p>
         *
         * @return 访问密钥密文
         */
        @NonNull
        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        /**
         * 获取安全令牌
         * <p>
         * Get the security token
         * </p>
         *
         * @return 安全令牌
         */
        @NonNull
        public String getSecurityToken() {
            return securityToken;
        }

        /**
         * 获取区域信息
         * <p>
         * Get the region information
         * </p>
         *
         * @return 区域信息，可能为 null
         */
        @Nullable
        public String getRegion() {
            return region;
        }

        @Override
        @NonNull
        public String getMediaId() {
            return "vidsts:" + vid;
        }

        @NonNull
        @Override
        public String toString() {
            return "VidStsSource{" +
                    "vid='" + vid + '\'' +
                    ", accessKeyId='" + PrivacyUtil.blur(accessKeyId) + '\'' +
                    ", accessKeySecret='" + PrivacyUtil.blur(accessKeySecret) + '\'' +
                    ", securityToken='" + PrivacyUtil.blur(securityToken) + '\'' +
                    ", region='" + PrivacyUtil.blur(region) + '\'' +
                    '}';
        }
    }

    /**
     * VidAuth 类型的视频源
     * <p>
     * 通过 VID 和 Auth 授权播放视频的资源类型。
     * 使用播放授权码进行授权，适用于需要更简单授权机制的场景。
     * </p>
     * <p>
     * VidAuth-based Video Source
     * <p>
     * Resource type for playing videos via VID and Auth authentication.
     * Uses play authorization code for authorization, suitable for scenarios requiring simpler authorization mechanisms.
     * </p>
     */
    public static final class VidAuthSource extends VideoSource {
        /**
         * 视频唯一标识符
         * <p>
         * Video unique identifier
         * </p>
         */
        @NonNull
        private final String vid;

        /**
         * 播放授权码
         * <p>
         * Play authorization code
         * </p>
         */
        @NonNull
        private final String playAuth;

        /**
         * 构造函数
         * <p>
         * Constructor
         * </p>
         *
         * @param vid      视频唯一标识符，不能为 null 或空字符串
         * @param playAuth 播放授权码，不能为 null 或空字符串
         */
        public VidAuthSource(@NonNull String vid, @NonNull String playAuth) {
            this.vid = vid;
            this.playAuth = playAuth;
        }

        @Override
        @SourceType
        public int getSourceType() {
            return SourceType.VID_AUTH;
        }

        @Override
        public boolean isValid() {
            return StringUtil.noneEmpty(vid, playAuth);
        }

        @NonNull
        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("vid", vid);
            map.put("playAuth", playAuth);
            return Collections.unmodifiableMap(map);
        }

        /**
         * 获取视频唯一标识符
         * <p>
         * Get the video unique identifier
         * </p>
         *
         * @return 视频唯一标识符
         */
        @NonNull
        public String getVid() {
            return vid;
        }

        /**
         * 获取播放授权码
         * <p>
         * Get the play authorization code
         * </p>
         *
         * @return 播放授权码
         */
        @NonNull
        public String getPlayAuth() {
            return playAuth;
        }

        @Override
        @NonNull
        public String getMediaId() {
            return "vidauth:" + vid;
        }

        @NonNull
        @Override
        public String toString() {
            return "VidAuthSource{" +
                    "vid='" + vid + '\'' +
                    ", playAuth='" + PrivacyUtil.blur(playAuth) + '\'' +
                    '}';
        }
    }
}
