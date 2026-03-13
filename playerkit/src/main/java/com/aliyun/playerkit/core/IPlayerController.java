package com.aliyun.playerkit.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.player.IMediaPlayer;

/**
 * AliPlayerKit 播放控制器接口
 * <p>
 * 定义了播控的核心能力，包括播放控制、状态管理、配置等。
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public interface IPlayerController {

    /**
     * 配置播放数据
     * <p>
     * 设置视频源、场景类型、封面图等播放相关配置。
     * 配置完成后，播放器将准备播放，但不会自动开始播放（除非 autoPlay 为 true）。
     * </p>
     *
     * @param model 播放器数据配置，包含视频源、场景类型等信息
     * @throws IllegalArgumentException 如果 model 为 null 或配置无效
     * @throws IllegalStateException    如果播放器未初始化
     */
    void configure(@NonNull AliPlayerModel model);

    /**
     * 获取播放器实例
     * <p>
     * 提供对底层播放器的直接访问。
     * </p>
     *
     * @return 播放器实例，如果未初始化可能为 null
     */
    @Nullable
    IMediaPlayer getPlayer();

    /**
     * 获取当前播放数据
     *
     * @return 播放数据配置
     */
    @Nullable
    AliPlayerModel getModel();

    /**
     * 获取播放器状态存储
     *
     * @return 播放器状态存储
     */
    @NonNull
    IPlayerStateStore getStateStore();

    /**
     * 释放资源
     * <p>
     * 释放播放器占用的所有资源，包括底层播放器实例、监听器等。
     * 调用此方法后，控制器将不再可用，需要重新创建。
     * </p>
     */
    void destroy();
}
