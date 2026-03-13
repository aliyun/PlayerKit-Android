package com.aliyun.playerkit.slot;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEventBus;

/**
 * AliPlayerKit 插槽宿主接口
 * <p>
 * 定义了插槽宿主的核心能力，为插槽提供统一的依赖访问接口。
 * 插槽通过此接口可以访问宿主提供的各种能力，而无需直接依赖具体实现类。
 * </p>
 * <p>
 * Slot Host Interface
 * <p>
 * Defines the core capabilities of the slot host, providing a unified dependency access interface for slots.
 * Slots can access various capabilities provided by the host through this interface without directly depending on concrete implementation classes.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public interface SlotHost {

    /**
     * 获取宿主上下文
     * <p>
     * 用于创建视图、访问资源、获取系统服务等。
     * </p>
     * <p>
     * Get host context
     * <p>
     * Used for creating views, accessing resources, getting system services, etc.
     * </p>
     *
     * @return 宿主上下文，不会为 null
     */
    @NonNull
    Context getContext();

    /**
     * 获取宿主布局视图
     * <p>
     * 插槽可以通过此视图访问布局信息，如宽高、位置、父视图等。
     * 通常用于需要根据宿主布局调整自身布局的场景。
     * </p>
     * <p>
     * Get host layout view
     * <p>
     * Slots can access layout information through this view, such as width, height, position, parent view, etc.
     * Usually used for scenarios where the slot needs to adjust its own layout based on the host layout.
     * </p>
     *
     * @return 宿主布局视图，不会为 null
     */
    @NonNull
    View getHostView();

    /**
     * 获取播放器状态存储
     * <p>
     * 提供对播放器状态（如播放状态、时长、当前位置等）的只读访问。
     * 插槽可以通过此接口主动查询当前状态，而无需等待事件通知。
     * </p>
     * <p>
     * Get Player State Store
     * <p>
     * Provides read-only access to player state (such as playback state, duration, current position, etc.).
     * Slots can actively query the current state through this interface without waiting for event notifications.
     * </p>
     *
     * @return 播放器状态存储，不会为 null
     */
    @NonNull
    IPlayerStateStore getPlayerStateStore();

    /**
     * 获取 Surface 管理器
     * <p>
     * 返回用于管理播放器显示视图和 Surface 的统一接口。
     * 插槽应通过此接口设置 DisplayView 或 Surface，而不是直接访问 Controller。
     * </p>
     * <p>
     * Get Surface Manager
     * <p>
     * Returns a unified interface for managing player display view and Surface.
     * Slots should set DisplayView or Surface through this interface instead of directly accessing Controller.
     * </p>
     *
     * @return Surface 管理器，不会为 null
     */
    @NonNull
    ISurfaceManager getSurfaceManager();

    /**
     * 获取播放器 ID
     * <p>
     * 用于构建播放器命令和事件。
     * </p>
     * <p>
     * Get Player ID
     * <p>
     * Used to construct player commands and events.
     * </p>
     *
     * @return 播放器 ID，如果未绑定或未初始化可能为 null
     */
    @Nullable
    String getPlayerId();

    /**
     * 获取当前播放数据配置
     * <p>
     * 用于获取封面图 URL 等信息。
     * </p>
     * <p>
     * Get current playback data configuration
     * <p>
     * Used to get cover image URL and other information.
     * </p>
     *
     * @return 播放数据配置，可能为 null
     */
    @Nullable
    AliPlayerModel getModel();

    /**
     * 发布事件到事件总线
     * <p>
     * 插槽可以通过此方法发布自定义事件，其他插槽可以订阅这些事件。
     * 这是插槽间通信的推荐方式，避免直接依赖，提高代码的可维护性和可测试性。
     * </p>
     * <p>
     * <strong>注意事项</strong>：
     * <ul>
     *     <li>事件必须是 {@link PlayerEvent} 或其子类</li>
     *     <li>事件会通过 {@link PlayerEventBus} 发布，所有订阅者都会收到</li>
     *     <li>事件发布是异步的，不会阻塞当前线程</li>
     * </ul>
     * </p>
     * <p>
     * Post event to event bus
     * <p>
     * Slots can publish custom events through this method, and other slots can subscribe to these events.
     * This is the recommended way for inter-slot communication, avoiding direct dependencies, improving code maintainability and testability.
     * </p>
     *
     * @param event 事件对象，必须是 {@link PlayerEvent} 或其子类，不能为 null
     */
    void postEvent(@NonNull PlayerEvent event);
}
