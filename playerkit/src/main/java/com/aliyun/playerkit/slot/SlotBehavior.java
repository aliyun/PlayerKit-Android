package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.logging.LogHub;

import java.util.ArrayList;
import java.util.List;

/**
 * AliPlayerKit 插槽行为委托类
 * <p>
 * 封装了插槽的核心逻辑（生命周期管理、事件订阅），采用组合模式设计。
 * 通过组合此行为类，任何类都可以获得插槽的能力，而无需继承 {@link BaseSlot}。
 * </p>
 * <p>
 * AliPlayerKit Slot Behavior Delegate Class
 * <p>
 * Encapsulates the core logic of slots (lifecycle management, event subscription), using composition pattern design.
 * By composing this behavior class, any class can gain slot capabilities without inheriting from {@link BaseSlot}.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public class SlotBehavior {

    private static final String TAG = "SlotBehavior";

    @Nullable
    private SlotHost mHost;

    private final List<EventSubscription<?>> eventSubscriptions = new ArrayList<>();

    /**
     * 附加到宿主
     * <p>
     * 当插槽被添加到宿主时调用，用于初始化插槽的依赖关系。
     * 会保存宿主引用，供后续使用。
     * </p>
     * <p>
     * Attach to host
     * <p>
     * Called when the slot is added to the host, used to initialize the slot's dependencies.
     * Saves host reference for later use.
     * </p>
     *
     * @param host 插槽宿主，不能为 null
     */
    public void attach(@NonNull SlotHost host) {
        LogHub.i(TAG, "attach");
        this.mHost = host;
    }

    /**
     * 从宿主分离
     * <p>
     * 当插槽从宿主移除时调用，用于清理所有资源。
     * 会自动取消所有事件订阅，确保不会造成内存泄漏。
     * </p>
     * <p>
     * Detach from host
     * <p>
     * Called when the slot is removed from the host, used to clean up all resources.
     * Automatically cancels all event subscriptions to prevent memory leaks.
     * </p>
     */
    public void detach() {
        LogHub.i(TAG, "detach");
        unsubscribeAllEvents();
        this.mHost = null;
    }

    /**
     * 获取插槽宿主
     * <p>
     * 在插槽 attach 后可以获取宿主，用于访问宿主提供的能力。
     * 在插槽 detach 后返回 null。
     * </p>
     * <p>
     * Get slot host
     * <p>
     * Can get the host after the slot is attached, used to access capabilities provided by the host.
     * Returns null after the slot is detached.
     * </p>
     *
     * @return 插槽宿主，如果未 attach 或已 detach 则返回 null
     */
    @Nullable
    public SlotHost getHost() {
        return mHost;
    }

    // ==================== 事件订阅（统一 API）====================

    /**
     * 订阅事件
     * <p>
     * 统一的事件订阅方法，支持所有类型的事件。
     * 订阅会在 detach 时自动取消，无需手动管理，确保不会造成内存泄漏。
     * </p>
     * <p>
     * Subscribe to event
     * <p>
     * Unified event subscription method, supports all types of events.
     * Subscription will be automatically cancelled on detach, no manual management needed, ensuring no memory leaks.
     * </p>
     *
     * @param eventType 事件类型，必须是 {@link PlayerEvent} 的子类，不能为 null
     * @param listener  事件监听器，不能为 null
     * @param <T>       事件类型，必须是 {@link PlayerEvent} 的子类
     */
    public <T extends PlayerEvent> void subscribe(@NonNull Class<T> eventType, @NonNull PlayerEventBus.EventListener<T> listener) {
        PlayerEventBus.getInstance().subscribe(eventType, listener);
        eventSubscriptions.add(new EventSubscription<>(eventType, listener));
    }

    /**
     * 取消所有事件订阅
     * <p>
     * 在 detach 时自动调用，确保所有订阅都被正确清理。
     * </p>
     * <p>
     * Unsubscribe all events
     * <p>
     * Automatically called on detach to ensure all subscriptions are properly cleaned up.
     * </p>
     */
    private void unsubscribeAllEvents() {
        for (EventSubscription<?> subscription : eventSubscriptions) {
            unsubscribeEvent(subscription);
        }
        eventSubscriptions.clear();
    }

    /**
     * 取消单个事件订阅
     * <p>
     * Unsubscribe single event
     * </p>
     *
     * @param subscription 事件订阅对象
     * @param <T>          事件类型
     */
    private <T extends PlayerEvent> void unsubscribeEvent(EventSubscription<T> subscription) {
        PlayerEventBus.getInstance().unsubscribe(subscription.eventType, subscription.listener);
    }

    /**
     * 事件订阅记录
     * <p>
     * 用于跟踪和管理事件订阅，确保在 detach 时能够正确取消订阅。
     * </p>
     * <p>
     * Event Subscription Record
     * <p>
     * Used to track and manage event subscriptions, ensuring subscriptions can be properly cancelled on detach.
     * </p>
     *
     * @param <T> 事件类型
     */
    private static class EventSubscription<T extends PlayerEvent> {
        /**
         * 事件类型
         */
        final Class<T> eventType;
        /**
         * 事件监听器
         */
        final PlayerEventBus.EventListener<T> listener;

        /**
         * 构造函数
         *
         * @param eventType 事件类型
         * @param listener  事件监听器
         */
        EventSubscription(Class<T> eventType, PlayerEventBus.EventListener<T> listener) {
            this.eventType = eventType;
            this.listener = listener;
        }
    }
}
