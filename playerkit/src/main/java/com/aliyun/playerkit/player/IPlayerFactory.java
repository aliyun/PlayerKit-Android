package com.aliyun.playerkit.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 播放器工厂接口
 * <p>
 * 定义创建 AliPlayer 实例的契约，允许不同的创建策略。
 * </p>
 * <p>
 * Player Factory Interface
 * <p>
 * Defines the contract for creating AliPlayer instances, allows for different creation strategies
 * </p>
 *
 * @author keria
 * @date 2025/11/26
 */
public interface IPlayerFactory {

    /**
     * 创建播放器实例
     * <p>
     * 根据传入的上下文创建新的播放器实例。每次调用都应该返回一个新的实例，
     * 除非实现类明确支持实例复用（如对象池模式）。
     * </p>
     * <p>
     * Create Player Instance
     * <p>
     * Creates a new player instance based on the provided context. Each call should return
     * a new instance unless the implementation class explicitly supports instance reuse (such as object pool pattern).
     * </p>
     *
     * @param context 应用上下文，用于创建播放器，不能为 null
     * @return 新创建的播放器实例，不会为 null
     */
    @NonNull
    IMediaPlayer create(@NonNull Context context);

    /**
     * 销毁播放器实例
     * <p>
     * 负责停止播放并释放播放器占用的所有资源。调用此方法后，播放器实例将无法继续使用。
     * </p>
     * <p>
     * Destroy Player Instance
     * <p>
     * Responsible for stopping playback and releasing all resources occupied by the player.
     * After calling this method, the player instance cannot be used anymore.
     * </p>
     *
     * @param player 要销毁的播放器实例，如果为 null 则忽略
     */
    void destroy(@Nullable IMediaPlayer player);
}
