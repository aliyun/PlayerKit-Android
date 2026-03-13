package com.aliyun.playerkit.preload;

import androidx.annotation.NonNull;

/**
 * 播放器全局预加载接口
 * <p>
 * 封装播放器 SDK 的全局预加载功能,提供统一的预加载任务管理。
 * </p>
 * <p>
 * Player Global Preloader Interface
 * </p>
 * <p>
 * Encapsulates the player SDK's global preload functionality, providing unified preload task management.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public interface IPlayerPreloader {

    /**
     * 添加预加载任务
     * <p>
     * 添加一个预加载任务,并指定该任务的状态回调监听器。
     * </p>
     * <p>
     * Add a preload task with a status callback listener.
     * </p>
     *
     * @param task     预加载任务对象 {@link PlayerPreloadTask}
     * @param listener 任务状态回调监听器 {@link PreloadCallback}
     * @return 分配的唯一任务 ID(taskId),用于后续控制与查询
     */
    @NonNull
    String addTask(@NonNull PlayerPreloadTask task, @NonNull PreloadCallback listener);

    /**
     * 取消指定任务 ID 的加载
     * <p>
     * 注意:不会删除已经下载的文件,仅取消未完成部分的加载。
     * </p>
     * <p>
     * Cancel the loading of the specified task ID.
     * <p>
     * Note: Does not delete already downloaded files, only cancels the unfinished part of the loading.
     * </p>
     *
     * @param taskId 要取消加载的任务 ID
     */
    void cancelTask(@NonNull String taskId);

    /**
     * 暂停指定任务 ID 的加载
     * <p>
     * Pause the loading of the specified task ID.
     * </p>
     *
     * @param taskId 要暂停的任务 ID
     */
    void pauseTask(@NonNull String taskId);

    /**
     * 继续(恢复)指定任务 ID 的加载
     * <p>
     * Resume the loading of the specified task ID.
     * </p>
     *
     * @param taskId 要恢复的任务 ID
     */
    void resumeTask(@NonNull String taskId);
}
