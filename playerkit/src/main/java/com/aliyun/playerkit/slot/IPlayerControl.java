package com.aliyun.playerkit.slot;

import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.player.IMediaPlayer;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * 播放器控制接口
 * <p>
 * 定义了播放器控制的便捷方法，插槽（{@link BaseSlot}）通过实现此接口，自动获得播放控制能力，而无需在基类中堆砌代码。
 * </p>
 * <p>
 * Player Control Interface
 * <p>
 * Defines convenience methods for player control, slots ({@link BaseSlot}) automatically gain playback control capabilities by implementing this interface,
 * without cluttering the base class with code.
 * </p>
 *
 * @author keria
 * @date 2025/12/08
 */
public interface IPlayerControl {

    String TAG = "IPlayerControl";

    /**
     * 获取插槽宿主
     * <p>
     * 必须由实现类提供，用于获取上下文和发送事件。
     * </p>
     *
     * @return 插槽宿主
     */
    @Nullable
    SlotHost getHost();

    /**
     * 获取播放器 ID
     *
     * @return 播放器 ID，如果未 attach 或播放器未初始化则返回 null
     */
    @Nullable
    default String getPlayerId() {
        SlotHost host = getHost();
        if (host != null) {
            return host.getPlayerId();
        }
        return null;
    }

    /**
     * 开始播放
     */
    default void play() {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot play, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Play(playerId));
        }
    }

    /**
     * 暂停播放
     */
    default void pause() {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot pause, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Pause(playerId));
        }
    }

    default void toggle() {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot toggle, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Toggle(playerId));
        }
    }

    /**
     * 停止播放
     */
    default void stop() {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot stop, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Stop(playerId));
        }
    }

    /**
     * 重播
     */
    default void replay() {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot replay, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Replay(playerId));
        }
    }

    /**
     * 跳转到指定位置
     *
     * @param position 目标位置（毫秒）
     */
    default void seekTo(long position) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot seek, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Seek(playerId, position));
        }
    }

    /**
     * 设置播放速度
     *
     * @param speed 播放速度
     */
    default void setSpeed(float speed) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot set speed, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SetSpeed(playerId, speed));
        }
    }

    /**
     * 截图
     */
    default void snapshot() {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot snapshot, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.Snapshot(playerId));
        }
    }

    /**
     * 设置循环播放
     *
     * @param loop 是否循环
     */
    default void setLoop(boolean loop) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot set loop, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SetLoop(playerId, loop));
        }
    }

    /**
     * 设置静音播放
     *
     * @param mute 是否静音
     */
    default void setMute(boolean mute) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot set mute, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SetMute(playerId, mute));
        }
    }

    /**
     * 设置渲染填充模式
     *
     * @param scaleType 填充模式
     */
    default void setScaleType(@IMediaPlayer.ScaleType int scaleType) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot set scale type, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SetScaleType(playerId, scaleType));
        }
    }

    /**
     * 设置镜像模式
     *
     * @param mirrorType 镜像模式
     */
    default void setMirrorType(@IMediaPlayer.MirrorType int mirrorType) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot set mirror type, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SetMirrorType(playerId, mirrorType));
        }
    }

    /**
     * 设置旋转模式
     *
     * @param rotation 旋转模式
     */
    default void setRotation(@IMediaPlayer.Rotation int rotation) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot set rotation, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SetRotation(playerId, rotation));
        }
    }

    /**
     * 切换清晰度
     *
     * @param trackQuality 清晰度
     */
    default void selectTrack(TrackQuality trackQuality) {
        String playerId = getPlayerId();
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Cannot select track, playerId is null");
            return;
        }

        SlotHost host = getHost();
        if (host != null) {
            host.postEvent(new PlayerCommand.SelectTrack(playerId, trackQuality));
        }
    }
}
