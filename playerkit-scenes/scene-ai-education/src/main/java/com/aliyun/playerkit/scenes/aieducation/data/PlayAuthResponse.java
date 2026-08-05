package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.utils.PrivacyUtil;
import com.google.gson.annotations.SerializedName;

/**
 * 获取视频播放凭证（PlayAuth）接口的响应实体类。
 *
 * <p>
 * 该类用于解析从服务器获取的 JSON 响应数据，包含播放凭证相关数据。
 * </p>
 * 对应 App Server 接口：{@link com.aliyun.playerkit.scenes.aieducation.AiContentConstants#API_GET_VIDEO_PLAY_AUTH } 视频播放凭证获取
 *
 * @author keria
 * @date 2026/07/22
 */
public class PlayAuthResponse {

    /**
     * 业务状态码
     * <p>
     * 0 表示成功，非 0 表示失败。
     */
    @Nullable
    @SerializedName("code")
    public Integer code;

    /**
     * HTTP 状态码
     * <p>
     * 字符串类型，例如 "200"。
     */
    @Nullable
    @SerializedName("httpCode")
    public String httpCode;

    /**
     * 请求是否成功
     * <p>
     * true 表示成功，false 表示失败。
     */
    @Nullable
    @SerializedName("success")
    public Boolean success;

    /**
     * 提示信息
     * <p>
     * 成功时通常为 "success"，失败时为具体错误描述。
     */
    @Nullable
    @SerializedName("message")
    public String message;

    /**
     * 请求唯一标识
     * <p>
     * 用于问题排查追踪，可能为 null。
     */
    @Nullable
    @SerializedName("requestId")
    public String requestId;

    /**
     * 播放凭证相关数据
     * <p>
     * 仅在请求成功时返回。
     */
    @Nullable
    @SerializedName("data")
    public PlayAuthData data;

    /**
     * 播放凭证数据体
     * <p>
     * 包含视频 ID 与播放授权凭证。
     */
    public static class PlayAuthData {

        /**
         * 视频唯一标识 ID
         */
        @Nullable
        @SerializedName("videoId")
        public String videoId;

        /**
         * Base64 编码的播放授权凭证字符串，用于播放器鉴权获取视频播放地址，
         * <p>
         * 具有有效期，过期后需重新获取。
         */
        @Nullable
        @SerializedName("playAuth")
        public String playAuth;

        @NonNull
        @Override
        public String toString() {
            return "PlayAuthData{" +
                    "videoId='" + videoId + '\'' +
                    ", playAuth='" + PrivacyUtil.blur(playAuth) + '\'' +
                    '}';
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayAuthResponse{" +
                "code=" + code +
                ", httpCode='" + httpCode + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", requestId='" + requestId + '\'' +
                ", data=" + data +
                '}';
    }
}
