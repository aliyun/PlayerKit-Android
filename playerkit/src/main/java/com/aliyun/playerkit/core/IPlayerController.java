package com.aliyun.playerkit.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.player.IMediaPlayer;

import java.util.List;

/**
 * AliPlayerKit 播放控制器接口
 * <p>
 * 定义了播控的核心能力，包括播放控制、状态管理、配置等。
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public interface IPlayerController {

    /**
     * 配置播放数据
     * <p>
     * 设置视频源、场景类型、封面图等播放相关配置。
     * 配置完成后，播放器将准备播放，但不会自动开始播放（除非 autoPlay 为 true）。
     * </p>
     *
     * @param model 播放器数据配置，包含视频源、场景类型等信息
     * @throws IllegalArgumentException 如果 model 为 null 或配置无效
     * @throws IllegalStateException    如果播放器未初始化
     */
    void configure(@NonNull AliPlayerModel model);

    /**
     * 获取播放器实例
     * <p>
     * 提供对底层播放器的直接访问。
     * </p>
     *
     * @return 播放器实例，如果未初始化可能为 null
     */
    @Nullable
    IMediaPlayer getPlayer();

    /**
     * 获取当前播放数据
     *
     * @return 播放数据配置
     */
    @Nullable
    AliPlayerModel getModel();

    /**
     * 获取播放器状态存储
     *
     * @return 播放器状态存储
     */
    @NonNull
    IPlayerStateStore getStateStore();

    /**
     * 写入章节数据（运行期动态更新）
     * <p>
     * 章节数据不再只能随 {@link #configure(AliPlayerModel)} 一次性传入：
     * 业务方在配置完成后拿到数据写入即可生效，无需重新 configure、无需重建播放器。
     * 初始章节请通过 {@link AliPlayerModel.Builder#chapters(List)} 传入。
     * 写入后进度条章节标记、横屏章节信息行与「下一章」、竖屏章节胶囊、
     * 章节侧滑面板等入口会同步刷新；传入 null 或空列表表示清除章节，UI 随之降级。
     * 播放器未就绪（configure 之前或 destroy 之后）时忽略本次写入并输出警告日志。
     * </p>
     * <p>
     * 数据前提：章节需按起始时间升序且区间连续不重叠，这是章节模型的既有约束。
     * 写入是整体替换，后写覆盖先写。
     * </p>
     * <p>
     * Set Chapter Data at Runtime
     * <p>
     * Chapter data is no longer restricted to {@link #configure(AliPlayerModel)}: business code
     * can write it after configuration without re-configuring or recreating the player. Initial
     * chapters should be passed via {@link AliPlayerModel.Builder#chapters(List)}. All chapter UI
     * refreshes accordingly. Passing null or an empty list clears the chapters. Writes are ignored
     * with a warning log while the player is not ready (before configure or after destroy).
     * </p>
     *
     * @param chapters 章节列表，可为 null（等同于清空章节）
     */
    void setChapters(@Nullable List<ChapterInfo> chapters);

    /**
     * 获取当前章节数据
     * <p>
     * 与配置期写入、运行期 {@link #setChapters(List)} 写入的是同一份数据。
     * </p>
     *
     * @return 章节列表（不可变，无章节时为空列表，永不返回 null）
     */
    @NonNull
    List<ChapterInfo> getChapters();

    /**
     * 释放资源
     * <p>
     * 释放播放器占用的所有资源，包括底层播放器实例、监听器等。
     * 调用此方法后，控制器将不再可用，需要重新创建。
     * </p>
     */
    void destroy();
}
