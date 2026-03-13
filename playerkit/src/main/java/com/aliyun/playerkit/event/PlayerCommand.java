package com.aliyun.playerkit.event;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.player.IMediaPlayer;

/**
 * AliPlayerKit 播放命令定义
 * <p>
 * 定义了所有播放控制相关的命令，所有命令都继承自 {@link PlayerCommand}。
 * 命令用于解耦 UI (Slot) 和 Controller，UI 只负责发送命令，Controller 负责执行。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 发送播放命令
 * PlayerEventBus.getInstance().post(new PlayerCommand.Play(playerId));
 *
 * // 发送跳转命令
 * PlayerEventBus.getInstance().post(new PlayerCommand.Seek(playerId, 5000));
 * </pre>
 * </p>
 *
 * @author keria
 * @date 2025/12/08
 * @see PlayerEvent 事件基类
 * @see PlayerEventBus 事件总线
 */
public abstract class PlayerCommand extends PlayerEvent {

    /**
     * 所有播放命令类列表
     * <p>
     * 用于批量订阅和取消订阅命令。
     * </p>
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends PlayerCommand>[] ALL_COMMANDS = new Class[]{
            Play.class,
            Pause.class,
            Toggle.class,
            Stop.class,
            Replay.class,
            Seek.class,
            SetSpeed.class,
            Snapshot.class,
            SetLoop.class,
            SetMute.class,
            SetScaleType.class,
            SetMirrorType.class,
            SetRotation.class,
            SelectTrack.class
    };

    /**
     * 构造函数
     * <p>
     * 创建播放命令实例，所有子类命令都需要指定播放器 ID。
     * </p>
     *
     * @param playerId 播放器 ID，用于标识命令的目标播放器，不能为 null
     */
    protected PlayerCommand(@NonNull String playerId) {
        super(playerId);
    }

    /**
     * 播放命令
     * <p>
     * 用于控制播放器开始播放。当播放器处于暂停或停止状态时，执行此命令将开始播放。
     * 如果播放器已经在播放中，此命令通常不会产生副作用。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的播放操作。
     * </p>
     */
    public static final class Play extends PlayerCommand {

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public Play(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "Play{playerId='" + playerId + "'}";
        }
    }

    /**
     * 暂停命令
     * <p>
     * 用于控制播放器暂停播放。当播放器正在播放时，执行此命令将暂停播放。
     * 暂停后可以通过 {@link Play} 命令恢复播放。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的暂停操作。
     * </p>
     */
    public static final class Pause extends PlayerCommand {

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public Pause(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "Pause{playerId='" + playerId + "'}";
        }
    }

    /**
     * 切换播放命令
     * <p>
     * 用于控制播放器开始或暂停播放。当播放器处于播放状态时，执行此命令将暂停播放。
     * 当播放器处于暂停状态时，执行此命令将开始播放。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的切换播放操作。
     * </p>
     */
    public static final class Toggle extends PlayerCommand {
        /**
         * 构造函数
         * <p>
         * 创建播放命令实例，所有子类命令都需要指定播放器 ID。
         * </p>
         *
         * @param playerId 播放器 ID，用于标识命令的目标播放器，不能为 null
         */
        public Toggle(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "Toggle{playerId='" + playerId + "'}";
        }
    }

    /**
     * 停止命令
     * <p>
     * 用于控制播放器停止播放。执行此命令后，播放器将停止播放并释放相关资源。
     * 停止后需要重新设置数据源才能再次播放。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的停止操作。
     * </p>
     */
    public static final class Stop extends PlayerCommand {

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public Stop(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "Stop{playerId='" + playerId + "'}";
        }
    }

    /**
     * 重播命令
     * <p>
     * 用于控制播放器重新开始播放。执行此命令后，播放器将从视频开头重新开始播放。
     * 重播会重置播放位置到起始位置（通常是 0）。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的重播操作。
     * </p>
     */
    public static final class Replay extends PlayerCommand {

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public Replay(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "Replay{playerId='" + playerId + "'}";
        }
    }

    /**
     * 跳转命令
     * <p>
     * 用于控制播放器跳转到指定的播放位置。执行此命令后，播放器将跳转到指定的时间点继续播放。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的跳转操作。
     * </p>
     * <p>
     * 注意：跳转位置应该大于等于 0，且不应超过视频总时长。
     * </p>
     */
    public static final class Seek extends PlayerCommand {

        /**
         * 目标播放位置（单位：毫秒）
         * <p>
         * 播放器将跳转到此位置继续播放。该值应该大于等于 0。
         * </p>
         */
        public final long position;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         * @param position 目标播放位置（单位：毫秒），应该大于等于 0
         */
        public Seek(@NonNull String playerId, long position) {
            super(playerId);
            this.position = position;
        }

        @NonNull
        @Override
        public String toString() {
            return "Seek{playerId='" + playerId + "', position=" + position + "ms}";
        }
    }

    /**
     * 设置播放速度命令
     * <p>
     * 用于控制播放器设置播放速度。执行此命令后，播放器将以指定的速度播放。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的速度设置操作。
     * </p>
     * <p>
     * 注意：播放速度通常范围在 0.3 到 3.0 之间，1.0 表示正常速度。
     * 建议优先使用 {@link com.aliyun.playerkit.ui.setting.SettingConstants#SPEED_OPTIONS} 中定义的常量进行设置。
     * </p>
     */
    public static final class SetSpeed extends PlayerCommand {

        /**
         * 目标播放速度
         * <p>
         * 播放器将以此速度播放。通常范围为 0.3 到 3.0，1.0 表示正常速度。
         * </p>
         * <p>
         * 推荐取值范围为 {@code 0.3f} ～ {@code 3.0f}
         * </p>
         */
        @FloatRange(from = 0.3f, to = 3.0f)
        public final float speed;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         * @param speed    目标播放速度，通常范围为 0.3 到 3.0
         */
        public SetSpeed(@NonNull String playerId, float speed) {
            super(playerId);
            this.speed = speed;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetSpeed{" +
                    "speed=" + speed +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 截图命令
     * <p>
     * 用于控制播放器进行截图。执行此命令后，播放器将截取当前播放位置的画面并保存为图片。
     * </p>
     * <p>
     * 该命令通过 {@link PlayerEventBus} 发送，由 {@link AliPlayerController}
     * 接收并执行相应的截图操作。
     * </p>
     */
    public static final class Snapshot extends PlayerCommand {
        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         */
        public Snapshot(@NonNull String playerId) {
            super(playerId);
        }

        @NonNull
        @Override
        public String toString() {
            return "Snapshot{" +
                    "playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置循环播放命令
     */
    public static final class SetLoop extends PlayerCommand {

        /**
         * 是否循环播放
         */
        public final boolean loop;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         * @param loop     是否循环播放
         */
        public SetLoop(@NonNull String playerId, boolean loop) {
            super(playerId);
            this.loop = loop;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetLoop{" +
                    "loop=" + loop +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置静音播放命令
     */
    public static final class SetMute extends PlayerCommand {

        /**
         * 是否静音播放
         */
        public final boolean mute;

        /**
         * 构造函数
         *
         * @param playerId 播放器 ID，不能为 null
         * @param mute     是否静音播放
         */
        public SetMute(@NonNull String playerId, boolean mute) {
            super(playerId);
            this.mute = mute;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetMute{" +
                    "mute=" + mute +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置渲染填充模式命令
     */
    public static final class SetScaleType extends PlayerCommand {

        /**
         * 渲染填充模式
         */
        public final @IMediaPlayer.ScaleType int scaleType;

        /**
         * 构造函数
         *
         * @param playerId  播放器 ID，不能为 null
         * @param scaleType 渲染填充模式
         */
        public SetScaleType(@NonNull String playerId, @IMediaPlayer.ScaleType int scaleType) {
            super(playerId);
            this.scaleType = scaleType;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetScaleType{" +
                    "scaleType=" + scaleType +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置镜像模式命令
     */
    public static final class SetMirrorType extends PlayerCommand {

        /**
         * 镜像模式
         */
        public final @IMediaPlayer.MirrorType int mirrorType;

        /**
         * 构造函数
         *
         * @param playerId   播放器 ID，不能为 null
         * @param mirrorType 镜像模式
         */
        public SetMirrorType(@NonNull String playerId, @IMediaPlayer.MirrorType int mirrorType) {
            super(playerId);
            this.mirrorType = mirrorType;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetMirrorType{" +
                    "mirrorType=" + mirrorType +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 设置旋转模式命令
     */
    public static final class SetRotation extends PlayerCommand {

        /**
         * 旋转模式
         */
        public final @IMediaPlayer.Rotation int rotationMode;

        /**
         * 构造函数
         *
         * @param playerId     播放器 ID，不能为 null
         * @param rotationMode 旋转模式
         */
        public SetRotation(@NonNull String playerId, @IMediaPlayer.Rotation int rotationMode) {
            super(playerId);
            this.rotationMode = rotationMode;
        }

        @NonNull
        @Override
        public String toString() {
            return "SetRotation{" +
                    "rotationMode=" + rotationMode +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    /**
     * 切换清晰度命令
     */
    public static final class SelectTrack extends PlayerCommand {

        /**
         * 目标清晰度
         */
        public final TrackQuality trackQuality;

        /**
         * 构造函数
         *
         * @param playerId     播放器 ID，不能为 null
         * @param trackQuality 目标清晰度
         */
        public SelectTrack(@NonNull String playerId, @NonNull TrackQuality trackQuality) {
            super(playerId);
            this.trackQuality = trackQuality;
        }

        @NonNull
        @Override
        public String toString() {
            return "SelectTrack{" +
                    "trackQuality=" + trackQuality +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }
}
