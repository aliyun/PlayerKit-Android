package com.aliyun.playerkit.preload;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.data.VideoSource;

/**
 * 播放器预加载回调接口
 * <p>
 * Player Preload Callback Interface
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public interface PreloadCallback {
    /**
     * 预加载任务完成时调用
     *
     * @param taskId 对应任务的唯一 ID
     * @param source 此任务对应的数据源 {@link VideoSource}
     */
    void onCompleted(@NonNull String taskId, @NonNull VideoSource source);

    /**
     * 预加载发生错误时调用
     *
     * @param taskId  对应任务的唯一 ID
     * @param source  此任务对应的数据源 {@link VideoSource}
     * @param code    错误码
     * @param message 错误描述信息
     */
    void onError(@NonNull String taskId, @NonNull VideoSource source, int code, @NonNull String message);

    /**
     * 预加载任务被取消时调用
     *
     * @param taskId 对应任务的唯一 ID
     * @param source 此任务对应的数据源 {@link VideoSource}
     */
    void onCanceled(@NonNull String taskId, @NonNull VideoSource source);
}
