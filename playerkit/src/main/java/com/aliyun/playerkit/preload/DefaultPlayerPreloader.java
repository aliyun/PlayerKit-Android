package com.aliyun.playerkit.preload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.loader.MediaLoaderV2;
import com.aliyun.loader.OnPreloadListener;
import com.aliyun.loader.PreloadTask;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.nativeclass.PreloadConfig;
import com.aliyun.player.source.UrlSource;
import com.aliyun.player.source.VidAuth;
import com.aliyun.player.source.VidSts;
import com.aliyun.playerkit.converter.VideoSourceConverter;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.logging.LogHub;

/**
 * 默认播放器全局预加载实现
 * <p>
 * 基于播放器 SDK 的 MediaLoaderV2 实现全局预加载功能。
 * </p>
 * <p>
 * Default Player Global Preloader Implementation
 * <p>
 * Implements global preload functionality based on the player SDK's MediaLoaderV2.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class DefaultPlayerPreloader implements IPlayerPreloader {

    private static final String TAG = "DefaultPlayerPreloader";

    /**
     * 构造函数
     */
    public DefaultPlayerPreloader() {
    }

    @NonNull
    @Override
    public String addTask(@NonNull PlayerPreloadTask task, @NonNull PreloadCallback callback) {
        LogHub.i(TAG, "add task", "task: " + task, "callback: " + callback);

        PlayerPreloadConfig config = task.getPreloadConfig();

        // 创建 SDK 的 PreloadConfig
        PreloadConfig preloadConfig = createPreloadConfig(config);

        // 创建 SDK 的 PreloadTask
        PreloadTask preloadTask = null;

        // 配置视频源
        VideoSource videoSource = task.getSource();
        if (videoSource instanceof VideoSource.UrlSource) {
            // URL 视频源
            UrlSource urlSource = (UrlSource) VideoSourceConverter.convert(videoSource);
            preloadTask = new PreloadTask(urlSource, preloadConfig);
        } else if (videoSource instanceof VideoSource.VidStsSource) {
            // VidSts 视频源
            VidSts vidSts = (VidSts) VideoSourceConverter.convert(videoSource);
            preloadTask = new PreloadTask(vidSts, preloadConfig);
        } else if (videoSource instanceof VideoSource.VidAuthSource) {
            // VidAuth 视频源
            VidAuth vidAuth = (VidAuth) VideoSourceConverter.convert(videoSource);
            preloadTask = new PreloadTask(vidAuth, preloadConfig);
        }

        if (preloadTask == null) {
            throw new IllegalArgumentException("Unsupported video source type: " + videoSource.getClass().getSimpleName());
        }

        // 创建回调适配器
        PreloadListenerAdapter adapter = new PreloadListenerAdapter(callback, videoSource);

        // 调用 MediaLoaderV2 的 addTask 方法
        String taskId = MediaLoaderV2.getInstance().addTask(preloadTask, adapter);

        LogHub.i(TAG, "Task added", "taskId: " + taskId);
        return taskId;
    }

    /**
     * 创建 SDK 的 PreloadConfig
     *
     * @param config PlayerPreloadConfig 配置对象
     * @return SDK 的 PreloadConfig 对象
     */
    private PreloadConfig createPreloadConfig(PlayerPreloadConfig config) {
        PreloadConfig preloadConfig = new PreloadConfig();
        preloadConfig.setDuration(config.getPreloadDuration());
        preloadConfig.setDefaultQuality(config.getDefaultQuality());
        return preloadConfig;
    }

    @Override
    public void cancelTask(@NonNull String taskId) {
        MediaLoaderV2.getInstance().cancelTask(taskId);
        LogHub.i(TAG, "Task canceled", "taskId: " + taskId);
    }

    @Override
    public void pauseTask(@NonNull String taskId) {
        MediaLoaderV2.getInstance().pauseTask(taskId);
        LogHub.i(TAG, "Task paused", "taskId: " + taskId);
    }

    @Override
    public void resumeTask(@NonNull String taskId) {
        MediaLoaderV2.getInstance().resumeTask(taskId);
        LogHub.i(TAG, "Task resumed", "taskId: " + taskId);
    }

    /**
     * 预加载监听器适配器
     * <p>
     * 将 SDK 的 OnPreloadListener 回调转换为 AliPlayerKit 的 PreloadCallback
     * </p>
     */
    private static class PreloadListenerAdapter extends OnPreloadListener {
        @Nullable
        private final PreloadCallback mCallback;

        @NonNull
        private final VideoSource mSource;

        PreloadListenerAdapter(@Nullable PreloadCallback callback, @NonNull VideoSource source) {
            this.mCallback = callback;
            this.mSource = source;
        }

        @Override
        public void onError(@NonNull String taskId, @NonNull String urlOrVid, @NonNull ErrorInfo errorInfo) {
            if (errorInfo == null) {
                LogHub.e(TAG, "Preload task failed", "taskId: " + taskId, "urlOrVid: " + urlOrVid);
                return;
            }

            LogHub.e(TAG, "Preload task failed", "taskId: " + taskId, "urlOrVid: " + urlOrVid, "errorInfo: " + errorInfo);
            if (mCallback != null) {
                mCallback.onError(taskId, mSource, errorInfo.getCode().getValue(), errorInfo.getMsg());
            }
        }

        @Override
        public void onCompleted(@NonNull String taskId, @NonNull String urlOrVid) {
            LogHub.i(TAG, "Preload task completed", "taskId: " + taskId, "urlOrVid: " + urlOrVid);
            if (mCallback != null) {
                mCallback.onCompleted(taskId, mSource);
            }
        }

        @Override
        public void onCanceled(@NonNull String taskId, @NonNull String urlOrVid) {
            LogHub.i(TAG, "Preload task canceled", "taskId: " + taskId, "urlOrVid: " + urlOrVid);
            if (mCallback != null) {
                mCallback.onCanceled(taskId, mSource);
            }
        }
    }

}
