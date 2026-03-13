package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * 默认播放器工厂
 * <p>
 * 使用 AliPlayerFactory 创建标准的 AliPlayer 实例。
 * </p>
 * <p>
 * Default Player Factory
 * <p>
 * Creates standard AliPlayer instances using AliPlayerFactory.
 * </p>
 *
 * @author keria
 * @date 2025/11/26
 */
public class DefaultPlayerFactory implements IPlayerFactory {

    private static final String TAG = "DefaultPlayerFactory";

    /**
     * 创建播放器实例
     * <p>
     * 使用 AliPlayerFactory 创建标准的 AliPlayer 实例，并为其生成唯一标识。
     * 每次调用都会创建一个新的播放器实例。
     * </p>
     * <p>
     * Create Player Instance
     * <p>
     * Creates a standard AliPlayer instance using AliPlayerFactory and generates a unique identifier for it.
     * Each call creates a new player instance.
     * </p>
     *
     * @param context 应用上下文，用于创建播放器，不能为 null
     * @return 新创建的播放器实例（包装为 IMediaPlayer），不会为 null
     */
    @NonNull
    @Override
    public IMediaPlayer create(@NonNull Context context) {
        AliPlayer player = AliPlayerFactory.createAliPlayer(context);
        // 生成播放器唯一标识
        String playerId = StringUtil.getSimpleNameIdentity(player);
        LogHub.i(TAG, "Player created", playerId);
        return new MediaPlayer(player, playerId);
    }

    /**
     * 销毁播放器实例
     * <p>
     * 释放播放器占用的所有资源。调用此方法后，播放器实例将无法继续使用。
     * 如果传入的 player 为 null，则只记录警告日志，不执行任何操作。
     * </p>
     * <p>
     * Destroy Player Instance
     * <p>
     * Releases all resources occupied by the player. After calling this method, the player instance cannot be used anymore.
     * If the passed player is null, only logs a warning without performing any operation.
     * </p>
     *
     * @param player 要销毁的播放器实例，可以为 null（null 时只记录警告日志）
     */
    @Override
    public void destroy(@Nullable IMediaPlayer player) {
        if (player == null) {
            LogHub.w(TAG, "Player is null");
            return;
        }

        String playerId = player.getPlayerId();
        player.release();
        LogHub.i(TAG, "Player destroyed", playerId);
    }
}
