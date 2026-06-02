package com.aliyun.playerkit.config;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.player.IMediaPlayer;

/**
 * 实例级播放器自定义配置回调。
 *
 * <p>在每次 {@code controller.configure(model)} 时，{@code prepare()} 前触发。
 * 适用于实例级 SDK 配置，如 setConfig（缓冲/Referer）、setOption 等。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * AliPlayerModel model = new AliPlayerModel.Builder()
 *     .videoSource(source)
 *     .onPlayerConfig(player -> {
 *         // 获取底层原生播放器实例
 *         AliPlayer aliPlayer = player.getInternalPlayer();
 *         // 设置播放配置
 *         PlayerConfig config = aliPlayer.getConfig();
 *         config.mMaxBufferDuration = 50000;
 *         config.mStartBufferDuration = 500;
 *         aliPlayer.setConfig(config);
 *     })
 *     .build();
 * </pre>
 */
@FunctionalInterface
public interface OnPlayerConfigCallback {
    /**
     * 播放器自定义配置回调，在 prepare() 前调用。
     *
     * @param player 播放器实例，可通过此实例访问底层 SDK 配置能力
     */
    void onPlayerConfig(@NonNull IMediaPlayer player);
}
