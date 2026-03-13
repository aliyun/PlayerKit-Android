package com.aliyun.playerkit.data;

/**
 * 播放器状态类型定义
 * <p>
 * 定义了播放器支持的所有状态类型。
 * </p>
 * <p>
 * Player State Type Definition
 * <p>
 * Defines all state types supported by the player.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public enum PlayerState {

    /**
     * 未知状态
     * <p>
     * 表示播放器状态未知或未初始化。
     * </p>
     * <p>
     * Unknown State
     * <p>
     * Indicates that the player state is unknown or not initialized.
     * </p>
     */
    UNKNOWN,

    /**
     * 空闲状态
     * <p>
     * 播放器已创建但尚未初始化，或已释放资源。
     * </p>
     * <p>
     * Idle State
     * <p>
     * Player has been created but not yet initialized, or resources have been released.
     * </p>
     */
    IDLE,

    /**
     * 初始化中
     * <p>
     * 播放器正在初始化，准备加载视频资源。
     * </p>
     * <p>
     * Initializing State
     * <p>
     * Player is initializing and preparing to load video resources.
     * </p>
     */
    INITIALIZING,

    /**
     * 已初始化
     * <p>
     * 播放器已完成初始化，但尚未准备播放。
     * </p>
     * <p>
     * Initialized State
     * <p>
     * Player has completed initialization but is not yet prepared for playback.
     * </p>
     */
    INITIALIZED,

    /**
     * 准备中
     * <p>
     * 播放器正在准备视频资源，解析视频信息。
     * </p>
     * <p>
     * Preparing State
     * <p>
     * Player is preparing video resources and parsing video information.
     * </p>
     */
    PREPARING,

    /**
     * 已准备
     * <p>
     * 播放器已完成准备，可以开始播放。
     * </p>
     * <p>
     * Prepared State
     * <p>
     * Player has completed preparation and can start playback.
     * </p>
     */
    PREPARED,

    /**
     * 播放中
     * <p>
     * 播放器正在播放视频。
     * </p>
     * <p>
     * Playing State
     * <p>
     * Player is currently playing video.
     * </p>
     */
    PLAYING,

    /**
     * 暂停
     * <p>
     * 播放器已暂停播放。
     * </p>
     * <p>
     * Paused State
     * <p>
     * Player has paused playback.
     * </p>
     */
    PAUSED,

    /**
     * 播放完成
     * <p>
     * 视频播放已完成。
     * </p>
     * <p>
     * Completed State
     * <p>
     * Video playback has completed.
     * </p>
     */
    COMPLETED,

    /**
     * 停止
     * <p>
     * 播放器已停止播放。
     * </p>
     * <p>
     * Stopped State
     * <p>
     * Player has stopped playback.
     * </p>
     */
    STOPPED,

    /**
     * 错误
     * <p>
     * 播放器发生错误。
     * </p>
     * <p>
     * Error State
     * <p>
     * Player has encountered an error.
     * </p>
     */
    ERROR,

}
