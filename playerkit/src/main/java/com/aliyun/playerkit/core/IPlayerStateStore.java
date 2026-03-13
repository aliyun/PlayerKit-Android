package com.aliyun.playerkit.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.data.VideoSize;
import com.aliyun.playerkit.player.IMediaPlayer;

import java.util.List;

/**
 * 播放器状态存储接口
 * <p>
 * 定义了播放器状态的统一访问接口。
 * 提供播放状态、视频尺寸、播放位置等信息的查询功能。
 * </p>
 * <p>
 * Player State Store Interface
 * <p>
 * Defines a unified access interface for player state.
 * Provides query functions for playback state, video size, playback position, and other information.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public interface IPlayerStateStore {

    // ==================== 状态访问方法 ====================

    /**
     * 获取当前播放状态
     * <p>
     * Get current playback state
     * </p>
     *
     * @return 当前播放状态
     */
    @NonNull
    PlayerState getPlayState();

    /**
     * 获取当前视频尺寸
     * <p>
     * Get current video size
     * </p>
     *
     * @return 当前视频尺寸，可能为 null（表示尺寸未知）
     */
    @Nullable
    VideoSize getVideoSize();

    /**
     * 获取视频总时长
     *
     * @return 视频总时长，单位：毫秒。如果未知，返回 0
     */
    long getDuration();

    /**
     * 获取当前播放位置
     *
     * @return 当前播放位置，单位：毫秒。如果未知，返回 0
     */
    long getCurrentPosition();

    /**
     * 获取当前播放速度
     *
     * @return 当前播放速度
     */
    float getCurrentSpeed();

    /**
     * 获取是否循环播放
     *
     * @return 是否循环播放
     */
    boolean isLoop();

    /**
     * 获取是否静音
     *
     * @return 是否静音
     */
    boolean isMute();

    /**
     * 获取当前视频旋转角度
     *
     * @return 当前视频旋转角度
     */
    @IMediaPlayer.Rotation
    int getCurrentRotation();

    /**
     * 获取当前画面缩放类型
     *
     * @return 当前画面缩放类型
     */
    @IMediaPlayer.ScaleType
    int getCurrentScaleType();

    /**
     * 获取当前画面镜像类型
     *
     * @return 当前画面镜像类型
     */
    @IMediaPlayer.MirrorType
    int getCurrentMirrorType();

    /**
     * 获取媒体轨道信息列表
     *
     * @return 媒体轨道信息列表
     */
    @Nullable
    List<TrackQuality> getTrackQualityList();

    /**
     * 获取当前选中的媒体轨道索引
     *
     * @return 当前选中的媒体轨道索引，如果没有选中则返回 -1
     */
    int getCurrentTrackIndex();

    /**
     * 获取是否处于全屏状态
     *
     * @return 是否处于全屏状态
     */
    boolean isFullscreen();
}
