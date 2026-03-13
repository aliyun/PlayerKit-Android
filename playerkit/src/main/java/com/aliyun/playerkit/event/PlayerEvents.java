package com.aliyun.playerkit.event;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.player.IMediaPlayer;

import java.util.List;

/**
 * AliPlayerKit 播放事件定义
 * <p>
 * 定义了所有播放相关的事件类型，所有事件都继承自 {@link PlayerEvent}。
 * </p>
 * <p>
 * AliPlayerKit Player Events Definition
 * <p>
 * Defines all player-related event types, all events extend from {@link PlayerEvent}.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public final class PlayerEvents {

    private PlayerEvents() {
        throw new UnsupportedOperationException("Cannot instantiate PlayerEvents");
    }

    /**
     * 视频尺寸变化事件
     * <p>
     * 当视频尺寸发生变化时触发（例如分辨率变化等）。
     * </p>
     * <p>
     * Video Size Changed Event
     * </p>
     * <p>
     * Triggered when video size changes (e.g., resolution change).
     * </p>
     */
    public static final class VideoSizeChanged extends PlayerEvent {
        public final int width;
        public final int height;

        public VideoSizeChanged(@NonNull String playerId, int width, int height) {
            super(playerId);
            this.width = width;
            this.height = height;
        }
    }

    /**
     * 播放状态变化事件
     * <p>
     * 当播放状态发生变化时触发（例如从播放切换到暂停）。
     * </p>
     * <p>
     * Play State Changed Event
     * </p>
     * <p>
     * Triggered when playback state changes (e.g., from playing to paused).
     * </p>
     */
    public static final class StateChanged extends PlayerEvent {
        @NonNull
        public final PlayerState oldState;
        @NonNull
        public final PlayerState newState;

        public StateChanged(@NonNull String playerId, @NonNull PlayerState oldState, @NonNull PlayerState newState) {
            super(playerId);
            this.oldState = oldState;
            this.newState = newState;
        }
    }

    /**
     * 播放器准备完成事件
     * <p>
     * 当播放器准备完成，可以开始播放时触发。
     * 携带视频总时长信息。
     * </p>
     * <p>
     * Player Prepared Event
     * </p>
     * <p>
     * Triggered when the player is prepared and ready to start playback.
     * Carries video duration information.
     * </p>
     */
    public static final class Prepared extends PlayerEvent {
        public final long duration;

        public Prepared(@NonNull String playerId, long duration) {
            super(playerId);
            this.duration = duration;
        }
    }

    /**
     * 首帧渲染完成事件
     * <p>
     * 当视频首帧渲染完成时触发。
     * 此事件通常用于隐藏封面图、显示视频内容等场景。
     * </p>
     * <p>
     * First Frame Rendered Event
     * </p>
     * <p>
     * Triggered when the first frame of the video is rendered.
     * This event is typically used to hide cover images, show video content, etc.
     * </p>
     */
    public static final class FirstFrameRendered extends PlayerEvent {
        public FirstFrameRendered(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "FirstFrameRendered{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 播放信息更新事件
     * <p>
     * 定期触发，用于更新播放进度、缓冲进度等信息。
     * </p>
     * <p>
     * Player Info Update Event
     * </p>
     * <p>
     * Triggered periodically to update playback progress, buffering progress, etc.
     * </p>
     */
    public static final class Info extends PlayerEvent {
        public final long duration;
        public final long currentPosition;
        public final long bufferedPosition;

        public Info(@NonNull String playerId, long duration, long currentPosition, long bufferedPosition) {
            super(playerId);
            this.duration = duration;
            this.currentPosition = currentPosition;
            this.bufferedPosition = bufferedPosition;
        }
    }

    /**
     * 错误事件
     * <p>
     * 当播放器发生错误时触发。
     * </p>
     * <p>
     * Error Event
     * </p>
     * <p>
     * Triggered when an error occurs in the player.
     * </p>
     */
    public static final class Error extends PlayerEvent {
        public final int errorCode;
        public final String errorMsg;

        public Error(@NonNull String playerId, int errorCode, @Nullable String errorMsg) {
            super(playerId);
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }
    }

    /**
     * 设置播放速度完成事件
     * <p>
     * 当播放速度设置完成时触发。
     * </p>
     * <p>
     * Set Speed Completed Event
     * </p>
     * <p>
     * Triggered when playback speed is set.
     * </p>
     */
    public static final class SetSpeedCompleted extends PlayerEvent {
        /**
         * 播放速度
         * <p>
         * 播放速度，取值范围：0.3 ~ 3.0。
         * 建议优先使用 {@link com.aliyun.playerkit.ui.setting.SettingConstants#SPEED_OPTIONS} 中定义的常量进行设置。
         * </p>
         */
        @FloatRange(from = 0.3f, to = 3.0f)
        public final float speed;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID
         * @param speed    播放速度
         */
        public SetSpeedCompleted(@NonNull String playerId, float speed) {
            super(playerId);
            this.speed = speed;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetSpeedCompleted{" +
                    "speed=" + speed +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 截图完成事件
     * <p>
     * 当截图完成时触发。
     * </p>
     * <p>
     * Snapshot Completed Event
     * </p>
     * <p>
     * Triggered when a snapshot is completed.
     * </p>
     */
    public static final class SnapshotCompleted extends PlayerEvent {

        /**
         * 截图结果
         * <p>
         * 截图结果，true 表示截图成功，false 表示截图失败。
         */
        public final boolean result;

        /**
         * 截图文件路径
         * <p>
         * 截图文件路径，截图成功时返回截图文件路径，截图失败时返回 null。
         */
        @Nullable
        public final String snapshotPath;

        /**
         * 截图宽度
         * <p>
         * 截图宽度，截图成功时返回截图宽度，截图失败时返回 0。
         */
        public final int width;

        /**
         * 截图高度
         * <p>
         * 截图高度，截图成功时返回截图高度，截图失败时返回 0。
         */
        public final int height;

        /**
         * 构造方法
         *
         * @param playerId     播放器 ID
         * @param result       截图结果
         * @param snapshotPath 截图文件路径
         * @param width        截图宽度
         * @param height       截图高度
         */
        public SnapshotCompleted(@NonNull String playerId, boolean result, @Nullable String snapshotPath, int width, int height) {
            super(playerId);
            this.result = result;
            this.snapshotPath = snapshotPath;
            this.width = width;
            this.height = height;
        }

        @NonNull
        @Override
        public String toString() {
            return "SnapshotCompleted{" +
                    "result=" + result +
                    ", snapshotPath='" + snapshotPath + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置循环播放完成事件
     * <p>
     * 当循环播放设置完成时触发。
     * </p>
     * <p>
     * Set Loop Completed Event
     * </p>
     * <p>
     * Triggered when loop playback is set.
     * </p>
     */
    public static final class SetLoopCompleted extends PlayerEvent {

        /**
         * 是否循环播放
         * <p>
         * Loop
         */
        public final boolean loop;

        /**
         * 构造函数
         *
         * @param playerId 播放器ID
         * @param loop     是否循环播放
         */
        public SetLoopCompleted(@NonNull String playerId, boolean loop) {
            super(playerId);
            this.loop = loop;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetLoopCompleted{" +
                    "loop=" + loop +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置静音完成事件
     * <p>
     * 当静音设置完成时触发。
     * </p>
     * <p>
     * Set Mute Completed Event
     * </p>
     * <p>
     * Triggered when mute is set.
     * </p>
     */
    public static final class SetMuteCompleted extends PlayerEvent {

        /**
         * 是否静音
         * <p>
         * Mute
         */
        public final boolean mute;

        /**
         * 构造函数
         *
         * @param playerId 播放器ID
         * @param mute     是否静音
         */
        public SetMuteCompleted(@NonNull String playerId, boolean mute) {
            super(playerId);
            this.mute = mute;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetMuteCompleted{" +
                    "mute=" + mute +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置渲染填充模式完成事件
     * <p>
     * 当渲染填充模式设置完成时触发。
     * </p>
     * <p>
     * Set Scale Type Completed Event
     * </p>
     * <p>
     * Triggered when scale type is set.
     * </p>
     */
    public static final class SetScaleTypeCompleted extends PlayerEvent {

        /**
         * 渲染填充模式
         * <p>
         * Scale Type
         */
        public final @IMediaPlayer.ScaleType int scaleType;

        /**
         * 构造函数
         *
         * @param playerId  播放器ID
         * @param scaleType 渲染填充模式
         */
        public SetScaleTypeCompleted(@NonNull String playerId, @IMediaPlayer.ScaleType int scaleType) {
            super(playerId);
            this.scaleType = scaleType;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetScaleTypeCompleted{" +
                    "scaleType=" + scaleType +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置镜像模式完成事件
     * <p>
     * 当镜像模式设置完成时触发。
     * </p>
     * <p>
     * Set Mirror Type Completed Event
     * </p>
     * <p>
     * Triggered when mirror type is set.
     * </p>
     */
    public static final class SetMirrorTypeCompleted extends PlayerEvent {
        /**
         * 镜像模式
         * <p>
         * Mirror Type
         */
        public final @IMediaPlayer.MirrorType int mirrorType;

        /**
         * 构造函数
         *
         * @param playerId   播放器ID
         * @param mirrorType 镜像模式
         */
        public SetMirrorTypeCompleted(@NonNull String playerId, @IMediaPlayer.MirrorType int mirrorType) {
            super(playerId);
            this.mirrorType = mirrorType;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetMirrorTypeCompleted{" +
                    "mirrorType=" + mirrorType +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置旋转模式完成事件
     * <p>
     * 当旋转模式设置完成时触发。
     * </p>
     * <p>
     * Set Rotation Completed Event
     * </p>
     * <p>
     * Triggered when rotation is set.
     * </p>
     */
    public static final class SetRotationCompleted extends PlayerEvent {
        /**
         * 旋转模式
         * <p>
         * Rotation
         */
        public final @IMediaPlayer.Rotation int rotation;

        /**
         * 构造函数
         *
         * @param playerId 播放器ID
         * @param rotation 旋转模式
         */
        public SetRotationCompleted(@NonNull String playerId, @IMediaPlayer.Rotation int rotation) {
            super(playerId);
            this.rotation = rotation;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetRotationCompleted{" +
                    "rotation=" + rotation +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 清晰度列表更新事件
     * <p>
     * 当清晰度列表更新时触发。
     * </p>
     * <p>
     * Track Quality List Updated Event
     * </p>
     * <p>
     * Triggered when the track quality list is updated.
     * </p>
     */
    public static final class TrackQualityListUpdated extends PlayerEvent {
        /**
         * 清晰度列表
         * <p>
         * Track Quality List
         */
        @NonNull
        public final List<TrackQuality> trackQualityList;

        /**
         * 清晰度列表更新事件
         *
         * @param playerId         播放器ID
         * @param trackQualityList 清晰度列表
         */
        public TrackQualityListUpdated(@NonNull String playerId, @NonNull List<TrackQuality> trackQualityList) {
            super(playerId);
            this.trackQualityList = trackQualityList;
        }

        @NonNull
        @Override
        public String toString() {
            return "TrackQualityListUpdated{" +
                    "trackQualityList=" + trackQualityList +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 选择清晰度完成事件
     * <p>
     * 当清晰度选择完成时触发。
     * </p>
     * <p>
     * Select Track Completed Event
     * </p>
     * <p>
     * Triggered when track selection is completed.
     * </p>
     */
    public static final class TrackSelected extends PlayerEvent {
        /**
         * 清晰度索引
         * <p>
         * Track Index
         */
        public final int trackIndex;

        /**
         * 选择清晰度完成事件
         *
         * @param playerId   播放器ID
         * @param trackIndex 清晰度索引
         */
        public TrackSelected(@NonNull String playerId, int trackIndex) {
            super(playerId);
            this.trackIndex = trackIndex;
        }

        @NonNull
        @Override
        public String toString() {
            return "TrackSelected{" +
                    "trackIndex=" + trackIndex +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 开始加载事件
     * <p>
     * 当播放器开始加载数据时触发。通常表现为卡顿或缓冲开始。
     * </p>
     * <p>
     * Loading Begin Event
     * </p>
     * <p>
     * Triggered when the player starts loading data. Usually indicates buffering starts.
     * </p>
     */
    public static final class LoadingBegin extends PlayerEvent {
        public LoadingBegin(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "LoadingBegin{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 加载进度事件
     * <p>
     * 当加载进度更新时触发。
     * </p>
     * <p>
     * Loading Progress Event
     * </p>
     * <p>
     * Triggered when loading progress updates.
     * </p>
     */
    public static final class LoadingProgress extends PlayerEvent {
        /**
         * 加载百分比 (0-100)
         */
        public final int percent;

        /**
         * 网速 (kbps)
         */
        public final float netSpeed;

        public LoadingProgress(@NonNull String playerId, int percent, float netSpeed) {
            super(playerId);
            this.percent = percent;
            this.netSpeed = netSpeed;
        }

        @NonNull
        @Override
        public String toString() {
            return "LoadingProgress{" +
                    "percent=" + percent +
                    ", netSpeed=" + netSpeed +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 加载结束事件
     * <p>
     * 当播放器加载结束时触发。通常表现为卡顿或缓冲结束，恢复播放。
     * </p>
     * <p>
     * Loading End Event
     * </p>
     * <p>
     * Triggered when loading ends. Usually indicates buffering ends and playback resumes.
     * </p>
     */
    public static final class LoadingEnd extends PlayerEvent {
        public LoadingEnd(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "LoadingEnd{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }
}
