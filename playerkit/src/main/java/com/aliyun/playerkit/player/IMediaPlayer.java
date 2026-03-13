package com.aliyun.playerkit.player;

import static java.lang.annotation.ElementType.TYPE_USE;

import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.player.videoview.AliDisplayView;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.TrackQuality;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 媒体播放器接口
 * <p>
 * 定义了播放器的核心操作接口，屏蔽底层播放器的具体实现细节。
 * 通过该接口，上层业务代码可以统一操作不同实现的播放器，提高代码的可维护性和可扩展性。
 * </p>
 * <p>
 * Media Player Interface
 * <p>
 * Defines the core operation interface for media players, abstracting away the implementation details of underlying players.
 * Through this interface, upper-level business code can uniformly operate players with different implementations, improving code maintainability and extensibility.
 * </p>
 *
 * @author keria
 * @date 2025/11/28
 */
public interface IMediaPlayer {

    /**
     * 渲染填充模式
     */
    @IntDef({
            ScaleType.FIT_XY,
            ScaleType.FIT_CENTER,
            ScaleType.CENTER_CROP,
    })
    @Retention(RetentionPolicy.SOURCE)
    @Target(TYPE_USE)
    public @interface ScaleType {

        /**
         * 拉伸填充视图
         */
        int FIT_XY = 0;

        /**
         * 等比完整显示（可能留黑边）
         */
        int FIT_CENTER = 1;

        /**
         * 等比填充（可能裁剪）
         */
        int CENTER_CROP = 2;
    }

    /**
     * 镜像模式
     */
    @IntDef({
            MirrorType.NONE,
            MirrorType.HORIZONTAL,
            MirrorType.VERTICAL,
    })
    @Retention(RetentionPolicy.SOURCE)
    @Target(TYPE_USE)
    public @interface MirrorType {

        /**
         * 无镜像
         */
        int NONE = 0;

        /**
         * 水平镜像
         */
        int HORIZONTAL = 1;

        /**
         * 垂直镜像
         */
        int VERTICAL = 2;
    }

    /**
     * 视频旋转角度（单位：度）
     */
    @IntDef({
            Rotation.DEGREE_0,
            Rotation.DEGREE_90,
            Rotation.DEGREE_180,
            Rotation.DEGREE_270,
    })
    @Retention(RetentionPolicy.SOURCE)
    @Target(TYPE_USE)
    public @interface Rotation {

        /**
         * 0 度
         */
        int DEGREE_0 = 0;

        /**
         * 90 度
         */
        int DEGREE_90 = 90;

        /**
         * 180 度
         */
        int DEGREE_180 = 180;

        /**
         * 270 度
         */
        int DEGREE_270 = 270;
    }

    /**
     * 设置视频数据源
     * <p>
     * 配置播放器的视频源、播放参数等信息。调用此方法后，播放器会开始准备播放，
     * 但不会自动开始播放（除非配置中设置了自动播放）。
     * </p>
     * <p>
     * Set Video Data Source
     * <p>
     * Configures the player's video source, playback parameters, etc. After calling this method,
     * the player will start preparing for playback, but will not automatically start playing
     * (unless auto-play is configured).
     * </p>
     *
     * @param model 播放器数据配置，包含视频源、场景类型等信息，不能为 null
     */
    void setDataSource(@NonNull AliPlayerModel model);

    /**
     * 开始播放
     * <p>
     * 从当前播放位置开始播放视频。如果视频尚未准备完成，此方法可能不会立即生效。
     * 建议监听播放器状态变化事件，在 {@link com.aliyun.playerkit.data.PlayerState#PREPARED}
     * 状态后再调用此方法。
     * </p>
     * <p>
     * Start Playback
     * <p>
     * Starts playing video from the current position. If the video is not yet prepared,
     * this method may not take effect immediately. It is recommended to listen to player state
     * change events and call this method after the state becomes {@link com.aliyun.playerkit.data.PlayerState#PREPARED}.
     * </p>
     */
    void start();

    /**
     * 暂停播放
     * <p>
     * 暂停当前播放，保持当前播放位置。可以通过调用 {@link #start()} 恢复播放。
     * </p>
     * <p>
     * Pause Playback
     * <p>
     * Pauses current playback while maintaining the current position. Can be resumed by calling {@link #start()}.
     * </p>
     */
    void pause();

    /**
     * 播放/暂停切换
     * <p>
     * 如果当前正在播放，则暂停播放；如果当前暂停，则恢复播放。
     * </p>
     * <p>
     * Play/Pause Switch
     * <p>
     * If currently playing, pause playback; if currently paused, resume playback.
     * </p>
     */
    void toggle();

    /**
     * 重新播放
     * <p>
     * 从视频开头重新开始播放。此操作会重新准备视频源并开始播放。
     * </p>
     * <p>
     * Replay
     * <p>
     * Restarts playback from the beginning of the video. This operation will re-prepare the video source and start playing.
     * </p>
     */
    void replay();

    /**
     * 跳转到指定播放位置
     * <p>
     * 将播放位置跳转到指定的时间点。跳转操作是异步的，实际跳转完成时间取决于网络和视频格式。
     * 建议监听播放器状态变化事件以确认跳转完成。
     * </p>
     * <p>
     * Seek to Specified Position
     * <p>
     * Jumps the playback position to the specified time point. Seek operation is asynchronous,
     * and the actual seek completion time depends on network and video format. It is recommended
     * to listen to player state change events to confirm seek completion.
     * </p>
     *
     * @param positionMs 目标播放位置，单位：毫秒，必须大于等于 0
     */
    void seekTo(long positionMs);

    /**
     * 停止播放
     * <p>
     * 停止当前播放并重置播放位置。停止后需要重新调用 {@link #setDataSource(AliPlayerModel)}
     * 和 {@link #start()} 才能继续播放。
     * </p>
     * <p>
     * Stop Playback
     * <p>
     * Stops current playback and resets the playback position. After stopping, you need to call
     * {@link #setDataSource(AliPlayerModel)} and {@link #start()} again to continue playback.
     * </p>
     */
    void stop();

    /**
     * 释放播放器资源
     * <p>
     * 释放播放器占用的所有资源，包括内存、网络连接、Surface 等。
     * 调用此方法后，播放器将无法继续使用，需要重新创建实例。
     * </p>
     * <p>
     * <strong>注意</strong>：此方法可能会阻塞当前线程，直到资源完全释放。
     * 如果需要异步释放，请使用实现类提供的异步释放方法。
     * </p>
     * <p>
     * Release Player Resources
     * <p>
     * Releases all resources occupied by the player, including memory, network connections, Surface, etc.
     * After calling this method, the player cannot be used anymore and a new instance needs to be created.
     * </p>
     * <p>
     * <strong>Note</strong>: This method may block the current thread until resources are completely released.
     * If asynchronous release is needed, please use the asynchronous release method provided by the implementation class.
     * </p>
     */
    void release();

    /**
     * 设置播放器显示视图
     * <p>
     * 设置用于显示视频画面的视图。支持 {@link AliDisplayView} 类型的视图。
     * 如果传入 null，则清除当前显示视图。
     * </p>
     * <p>
     * Set Player Display View
     * <p>
     * Sets the view used to display the video frame. Supports views of type {@link AliDisplayView}.
     * If null is passed, the current display view will be cleared.
     * </p>
     *
     * @param displayView 显示视图，可以为 null（表示清除显示视图）
     */
    void setDisplayView(@Nullable AliDisplayView displayView);

    /**
     * 设置播放器渲染 Surface
     * <p>
     * 设置用于渲染视频画面的 Surface。通常用于 SurfaceView 或 TextureView 场景。
     * 如果传入 null，则清除当前 Surface。
     * </p>
     * <p>
     * Set Player Rendering Surface
     * <p>
     * Sets the Surface used to render the video frame. Usually used for SurfaceView or TextureView scenarios.
     * If null is passed, the current Surface will be cleared.
     * </p>
     *
     * @param surface Surface 实例，可以为 null（表示清除 Surface）
     */
    void setSurface(@Nullable Surface surface);

    /**
     * 通知播放器 Surface 已发生变化
     * <p>
     * 当 Surface 的尺寸、格式等属性发生变化时，需要调用此方法通知播放器更新渲染。
     * 通常在 SurfaceHolder.Callback 的 surfaceChanged 回调中调用。
     * </p>
     * <p>
     * Notify Player that Surface has Changed
     * <p>
     * When the Surface's size, format, or other attributes change, this method should be called
     * to notify the player to update rendering. Usually called in SurfaceHolder.Callback's surfaceChanged callback.
     * </p>
     */
    void surfaceChanged();

    /**
     * 设置播放速度
     * <p>
     * 设置视频播放的速度倍率。例如：1.0 表示正常速度，2.0 表示 2 倍速播放，0.5 表示 0.5 倍速播放。
     * 播放速度通常范围为 0.3 到 3.0，1.0 表示正常速度。
     * 建议优先使用 {@link com.aliyun.playerkit.ui.setting.SettingConstants#SPEED_OPTIONS} 中定义的常量进行设置。
     * </p>
     * <p>
     * Set Playback Speed
     * <p>
     * Sets the playback speed multiplier. For example: 1.0 means normal speed, 2.0 means 2x speed, 0.5 means 0.5x speed.
     * Supported speed range is typically 0.3 to 3.0.
     * Suggest using the constants defined in {@link com.aliyun.playerkit.ui.setting.SettingConstants#SPEED_OPTIONS} for setting.
     * </p>
     *
     * @param speed 播放速度倍率，通常范围为 0.3 到 3.0，1.0 表示正常速度。
     */
    void setSpeed(float speed);

    /**
     * 设置循环播放
     * <p>
     * 循环播放设置
     * 默认为 false
     * </p>
     * <p>
     * Set loop playback.
     * <p>
     * Enables or disables loop playback.
     * Default value is false.
     * </p>
     *
     * @param loop true 开启循环播放，false 关闭
     *             true to enable loop playback, false to disable
     */
    void setLoop(boolean loop);

    /**
     * 设置静音
     * <p>
     * 静音设置
     * 默认为 false
     * </p>
     * <p>
     * Set mute state.
     * <p>
     * Enables or disables mute.
     * Default value is false.
     * </p>
     *
     * @param mute true 静音，false 取消静音
     *             true to mute, false to unmute
     */
    void setMute(boolean mute);

    /**
     * 截屏
     * <p>
     * 截取当前播放位置的画面并保存为图片。
     * </p>
     * <p>
     * Snapshot
     * <p>
     * Captures the current playback position and saves it as an image.
     * </p>
     */
    void snapshot();

    /**
     * 设置渲染填充模式
     * <p>
     * Set rendering fill mode.
     * </p>
     *
     * @param scaleType 填充模式
     */
    void setScaleType(@ScaleType int scaleType);

    /**
     * 设置镜像模式
     * <p>
     * Set mirror mode.
     * </p>
     *
     * @param mirrorType 镜像模式
     */
    void setMirrorType(@MirrorType int mirrorType);

    /**
     * 设置旋转模式
     * <p>
     * Set rotate mode.
     * </p>
     *
     * @param rotation 旋转角度（0, 90, 180, 270）
     */
    void setRotation(@Rotation int rotation);

    /**
     * 选择清晰度
     * <p>
     * 用于切换清晰度。
     * </p>
     * <p>
     * Select track.
     * </p>
     *
     * @param trackQuality 清晰度信息
     */
    void selectTrack(TrackQuality trackQuality);

    /**
     * 获取播放器唯一标识
     * <p>
     * 返回播放器的唯一标识符，用于区分不同的播放器实例。
     * 该标识符在播放器生命周期内保持不变。
     * </p>
     * <p>
     * Get Player Unique Identifier
     * <p>
     * Returns the unique identifier of the player, used to distinguish different player instances.
     * This identifier remains unchanged during the player's lifecycle.
     * </p>
     *
     * @return 播放器唯一标识，不会为 null
     */
    @NonNull
    String getPlayerId();

    /**
     * 获取播放器状态存储接口
     * <p>
     * 返回只读的状态存储接口，可以查询播放器的当前状态、视频尺寸等信息。
     * 状态变化会通过事件总线自动发布，也可以通过此接口主动查询。
     * </p>
     * <p>
     * Get Player State Store Interface
     * <p>
     * Returns a read-only state store interface that can query the player's current state, video size, etc.
     * State changes are automatically published through the event bus, or can be actively queried through this interface.
     * </p>
     *
     * @return 状态存储接口，不会为 null
     */
    @NonNull
    IPlayerStateStore getStateStore();
}
