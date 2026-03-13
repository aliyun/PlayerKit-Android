package com.aliyun.playerkit.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.data.VideoSize;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.player.IMediaPlayer;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * AliPlayerKit 播放状态存储
 * <p>
 * 负责状态的存储、通知和获取。
 * </p>
 * <p>
 * AliPlayerKit State Store
 * <p>
 * Responsible for state storage, notification, and retrieval.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class PlayerStateStore implements IPlayerStateStore {

    private static final String TAG = "PlayerStateStore";

    // ==================== 事件发布配置 ====================

    /**
     * 播放器唯一标识
     */
    @Nullable
    private String playerId;

    // ==================== 状态存储 ====================

    /**
     * 播放状态
     */
    private PlayerState playState = PlayerState.IDLE;

    /**
     * 视频尺寸
     */
    @Nullable
    private VideoSize videoSize = null;

    /**
     * 视频总时长
     */
    private long duration = 0;

    /**
     * 当前播放位置
     */
    private long currentPosition = 0;

    /**
     * 当前播放速度
     */
    private float currentSpeed = 1.0f;

    /**
     * 是否循环播放
     */
    private boolean isLoop = false;

    /**
     * 是否静音
     */
    private boolean isMute = false;

    /**
     * 当前视频旋转角度
     */
    private @IMediaPlayer.Rotation int currentRotation = IMediaPlayer.Rotation.DEGREE_0;

    /**
     * 画面缩放类型
     */
    private @IMediaPlayer.ScaleType int currentScaleType = IMediaPlayer.ScaleType.FIT_CENTER;

    /**
     * 画面镜像类型
     */
    private @IMediaPlayer.MirrorType int currentMirrorType = IMediaPlayer.MirrorType.NONE;

    /**
     * 媒体轨道信息列表
     */
    private List<TrackQuality> trackQualityList = new ArrayList<>();

    /**
     * 当前选中的媒体轨道索引
     */
    private int currentTrackIndex = -1;

    /**
     * 是否处于全屏状态
     */
    private boolean isFullscreen = false;

    // ==================== 状态获取 ====================

    /**
     * 获取当前播放状态
     * <p>
     * Get Current Play State
     * </p>
     *
     * @return 当前播放状态
     */
    @Override
    @NonNull
    public PlayerState getPlayState() {
        return playState;
    }

    /**
     * 获取当前视频尺寸
     * <p>
     * Get Current Video Size
     * </p>
     *
     * @return 当前视频尺寸，可能为 null（表示尺寸未知）
     */
    @Override
    @Nullable
    public VideoSize getVideoSize() {
        return videoSize;
    }

    /**
     * 获取视频总时长
     * <p>
     * Get Video Duration
     * </p>
     *
     * @return 视频总时长，单位：毫秒
     */
    @Override
    public long getDuration() {
        return duration;
    }

    /**
     * 获取当前播放位置
     * <p>
     * Get Current Position
     * </p>
     *
     * @return 当前播放位置，单位：毫秒
     */
    @Override
    public long getCurrentPosition() {
        return currentPosition;
    }

    /**
     * 获取当前播放速度
     * <p>
     * Get Current Speed
     * </p>
     *
     * @return 当前播放速度
     */
    public float getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * 获取是否循环播放
     * <p>
     * Get Whether to Loop Play
     * </p>
     *
     * @return 是否循环播放
     */
    public boolean isLoop() {
        return isLoop;
    }

    /**
     * 获取是否静音
     * <p>
     * Get Whether to Mute
     * </p>
     *
     * @return 是否静音
     */
    public boolean isMute() {
        return isMute;
    }

    /**
     * 获取当前视频旋转角度
     * <p>
     * Get Current Video Rotation
     * </p>
     *
     * @return 当前视频旋转角度
     */
    @Override
    public @IMediaPlayer.Rotation int getCurrentRotation() {
        return currentRotation;
    }

    /**
     * 获取当前画面缩放类型
     * <p>
     * Get Current Scale Type
     * </p>
     *
     * @return 当前画面缩放类型
     */
    @Override
    public @IMediaPlayer.ScaleType int getCurrentScaleType() {
        return currentScaleType;
    }

    /**
     * 获取当前画面镜像类型
     * <p>
     * Get Current Mirror Type
     * </p>
     *
     * @return 当前画面镜像类型
     */
    @Override
    public @IMediaPlayer.MirrorType int getCurrentMirrorType() {
        return currentMirrorType;
    }

    /**
     * 获取媒体轨道信息列表
     * <p>
     * Get Media Track Info List
     * </p>
     *
     * @return 媒体轨道信息列表
     */
    @Nullable
    public List<TrackQuality> getTrackQualityList() {
        return trackQualityList;
    }

    /**
     * 获取当前选中的媒体轨道索引
     *
     * @return 当前选中的媒体轨道索引，如果没有选中则返回 -1
     */
    @Override
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    /**
     * 获取是否处于全屏状态
     *
     * @return 是否处于全屏状态
     */
    @Override
    public boolean isFullscreen() {
        return isFullscreen;
    }


    // ==================== 状态更新 ====================

    /**
     * 更新播放状态
     * <p>
     * 会自动发布 {@link PlayerEvents.StateChanged} 事件（如果启用事件发布）。
     * </p>
     * <p>
     * Update Play State
     * <p>
     * Automatically publishes {@link PlayerEvents.StateChanged} event if event publishing is enabled.
     * </p>
     *
     * @param newState 新的播放状态
     */
    public void updatePlayState(@NonNull PlayerState newState) {
        PlayerState oldState = playState;
        if (oldState == newState) {
            return; // 状态未变化，无需更新和通知
        }

        playState = newState;

        LogHub.i(TAG, "updatePlayState", oldState + " -> " + newState, playerId);

        // 发布事件
        if (StringUtil.isNotEmpty(playerId)) {
            PlayerEventBus.getInstance().post(new PlayerEvents.StateChanged(playerId, oldState, newState));
        }
    }

    /**
     * 更新视频尺寸
     * <p>
     * 会自动发布 {@link PlayerEvents.VideoSizeChanged} 事件（如果启用事件发布）。
     * </p>
     * <p>
     * Update Video Size
     * <p>
     * Automatically publishes {@link PlayerEvents.VideoSizeChanged} event if event publishing is enabled.
     * </p>
     *
     * @param width  视频宽度
     * @param height 视频高度
     */
    public void updateVideoSize(int width, int height) {
        VideoSize newSize = new VideoSize(width, height);

        // 检查尺寸是否变化
        boolean sizeChanged = videoSize == null || videoSize.getWidth() != width || videoSize.getHeight() != height;

        videoSize = newSize;

        // 发布事件（尺寸变化时）
        if (sizeChanged && StringUtil.isNotEmpty(playerId)) {
            PlayerEventBus.getInstance().post(new PlayerEvents.VideoSizeChanged(playerId, width, height));
        }
    }

    /**
     * 更新视频时长
     *
     * @param duration 视频时长，单位：毫秒
     */
    public void updateDuration(long duration) {
        if (this.duration == duration) {
            return;
        }

        this.duration = duration;

        LogHub.i(TAG, "updateDuration", duration, playerId);
    }

    /**
     * 更新当前播放位置
     *
     * @param currentPosition 当前播放位置，单位：毫秒
     */
    public void updateCurrentPosition(long currentPosition) {
        if (this.currentPosition == currentPosition) {
            return;
        }

        this.currentPosition = currentPosition;

//        LogHub.i(TAG, "updateCurrentPosition", currentPosition + "ms", playerId);
    }

    /**
     * 更新播放速度
     *
     * @param speed 播放速度
     */
    public void updateSpeed(float speed) {
        if (this.currentSpeed == speed) {
            return;
        }

        LogHub.i(TAG, "updateSpeed", currentSpeed + " -> " + speed, playerId);
        this.currentSpeed = speed;
        PlayerEventBus.getInstance().post(new PlayerEvents.SetSpeedCompleted(playerId, speed));
    }

    /**
     * 更新循环播放状态
     *
     * @param loop 循环播放
     */
    public void updateLoop(boolean loop) {
        if (this.isLoop == loop) {
            return;
        }

        LogHub.i(TAG, "updateLoop", isLoop + " -> " + loop, playerId);
        this.isLoop = loop;
        PlayerEventBus.getInstance().post(new PlayerEvents.SetLoopCompleted(playerId, loop));
    }

    /**
     * 更新静音状态
     *
     * @param mute 静音
     */
    public void updateMute(boolean mute) {
        if (this.isMute == mute) {
            return;
        }

        LogHub.i(TAG, "updateMute", isMute + " -> " + mute, playerId);
        this.isMute = mute;
        PlayerEventBus.getInstance().post(new PlayerEvents.SetMuteCompleted(playerId, mute));
    }

    /**
     * 更新当前视频旋转角度
     *
     * @param rotation 旋转角度
     */
    public void updateRotation(@IMediaPlayer.Rotation int rotation) {
        if (currentRotation == rotation) {
            return;
        }

        LogHub.i(TAG, "updateRotation", currentRotation + " -> " + rotation, playerId);
        currentRotation = rotation;
        PlayerEventBus.getInstance().post(new PlayerEvents.SetRotationCompleted(playerId, rotation));
    }

    /**
     * 更新当前画面缩放类型
     *
     * @param scaleType 缩放类型
     */
    public void updateScaleType(@IMediaPlayer.ScaleType int scaleType) {
        if (currentScaleType == scaleType) {
            return;
        }

        LogHub.i(TAG, "updateScaleType", currentScaleType + " -> " + scaleType, playerId);
        currentScaleType = scaleType;
        PlayerEventBus.getInstance().post(new PlayerEvents.SetScaleTypeCompleted(playerId, scaleType));
    }

    /**
     * 更新当前画面镜像类型
     *
     * @param mirrorType 镜像类型
     */
    public void updateMirrorType(@IMediaPlayer.MirrorType int mirrorType) {
        if (currentMirrorType == mirrorType) {
            return;
        }

        LogHub.i(TAG, "updateMirrorType", currentMirrorType + " -> " + mirrorType, playerId);
        currentMirrorType = mirrorType;
        PlayerEventBus.getInstance().post(new PlayerEvents.SetMirrorTypeCompleted(playerId, mirrorType));
    }

    /**
     * 添加清晰度信息列表
     *
     * @param trackQualityList 清晰度信息列表
     */
    public void updateTrackQualityList(@NonNull List<TrackQuality> trackQualityList) {
        List<TrackQuality> copy = new ArrayList<>(trackQualityList);
        LogHub.i(TAG, "updateTrackQualityList", copy, playerId);
        this.trackQualityList = copy;
        PlayerEventBus.getInstance().post(new PlayerEvents.TrackQualityListUpdated(playerId, copy));
    }

    /**
     * 更新当前选中的媒体轨道索引
     *
     * @param index 媒体轨道索引
     */
    public void updateCurrentTrackIndex(int index) {
        if (this.currentTrackIndex == index) {
            return;
        }

        LogHub.i(TAG, "updateCurrentTrackIndex", currentTrackIndex + " -> " + index, playerId);
        this.currentTrackIndex = index;
        PlayerEventBus.getInstance().post(new PlayerEvents.TrackSelected(playerId, index));
    }

    /**
     * 更新全屏状态
     *
     * @param fullscreen 是否全屏
     */
    public void updateFullscreen(boolean fullscreen) {
        if (this.isFullscreen == fullscreen) {
            return;
        }

        LogHub.i(TAG, "updateFullscreen", isFullscreen + " -> " + fullscreen, playerId);
        this.isFullscreen = fullscreen;
    }

    // ==================== 事件发布 ====================

    /**
     * 启用事件发布
     * <p>
     * 设置 playerId 后，状态变化会自动发布到事件总线。
     * </p>
     * <p>
     * Enable Event Publishing
     * <p>
     * After setting playerId, state changes will automatically be published to the event bus.
     * </p>
     *
     * @param playerId 播放器 ID
     */
    public void enableEventPublishing(@NonNull String playerId) {
        this.playerId = playerId;
    }

    /**
     * 禁用事件发布
     * <p>
     * 清空 playerId 后，将不再发布事件。
     * </p>
     * <p>
     * Disable Event Publishing
     * <p>
     * After clearing playerId, events will no longer be published.
     * </p>
     */
    public void disableEventPublishing() {
        this.playerId = null;
    }

    /**
     * 重置所有状态
     * <p>
     * 将所有状态重置为初始值，通常在播放器销毁或重新配置时调用。
     * 注意：重置不会发布事件，因为这是内部状态清理。
     * </p>
     * <p>
     * Reset All States
     * <p>
     * Resets all states to initial values, typically called when player is destroyed or reconfigured.
     * Note: Reset does not publish events, as this is internal state cleanup.
     * </p>
     */
    public void reset() {
        playerId = null;

        playState = PlayerState.IDLE;
        videoSize = null;
        duration = 0;
        currentPosition = 0;

        currentSpeed = 1.0f;
        isLoop = false;
        isMute = false;
        currentRotation = IMediaPlayer.Rotation.DEGREE_0;
        currentMirrorType = IMediaPlayer.MirrorType.NONE;
        currentScaleType = IMediaPlayer.ScaleType.FIT_CENTER;

        trackQualityList = new ArrayList<>();
        currentTrackIndex = -1;
        isFullscreen = false;
    }
}
