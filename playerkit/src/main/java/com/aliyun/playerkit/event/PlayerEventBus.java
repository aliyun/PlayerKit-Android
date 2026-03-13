package com.aliyun.playerkit.event;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.logging.LogHub;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AliPlayerKit 播放事件总线
 * <p>
 * 提供基于类型的事件订阅和发布机制，支持插槽和外部组件订阅播放器事件。
 * 支持线程安全的事件发布和订阅。
 * </p>
 * <p>
 * AliPlayerKit Player Event Bus
 * <p>
 * Provides type-based event subscription and publishing mechanism, supporting slots and external components
 * to subscribe to player events.
 * Supports thread-safe event publishing and subscription.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class PlayerEventBus {

    private static final String TAG = "PlayerEventBus";

    /**
     * 单例实例
     */
    private static volatile PlayerEventBus instance;

    /**
     * 事件订阅者映射表
     * Key: 事件类型（PlayerEvent 的子类），Value: 订阅者列表
     */
    @NonNull
    private final Map<Class<? extends PlayerEvent>, List<WeakReference<EventListener<? extends PlayerEvent>>>> subscribers = new ConcurrentHashMap<>();

    // ==================== 单例 ====================

    /**
     * 获取事件总线单例
     * <p>
     * Get event bus singleton instance
     * </p>
     *
     * @return 事件总线实例
     */
    @NonNull
    public static PlayerEventBus getInstance() {
        if (instance == null) {
            synchronized (PlayerEventBus.class) {
                if (instance == null) {
                    instance = new PlayerEventBus();
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造函数
     */
    private PlayerEventBus() {
    }

    // ==================== 事件订阅 ====================

    /**
     * 订阅事件
     * <p>
     * 订阅指定类型的事件，当该类型的事件发布时，会调用 listener。
     * 使用弱引用存储 listener，避免内存泄漏。
     * </p>
     * <p>
     * <strong>注意</strong>：
     * <ul>
     *     <li>插槽内部推荐使用 {@link com.aliyun.playerkit.slot.BaseSlot#subscribe(Class, PlayerEventBus.EventListener)}，会自动管理生命周期</li>
     *     <li>外部组件需要手动管理订阅生命周期，在适当时机调用 {@link #unsubscribe(Class, EventListener)}</li>
     * </ul>
     * </p>
     * <p>
     * Subscribe to event
     * <p>
     * Subscribes to events of the specified type. When an event of this type is posted, the listener will be called.
     * Uses weak references to store listeners, avoiding memory leaks.
     * </p>
     *
     * @param eventType 事件类型，必须是 {@link PlayerEvent} 的子类
     * @param listener  事件监听器
     * @param <T>       事件类型，必须是 {@link PlayerEvent} 的子类
     */
    public <T extends PlayerEvent> void subscribe(@NonNull Class<T> eventType, @NonNull EventListener<T> listener) {
        if (eventType == null || listener == null) {
            LogHub.w(TAG, "EventType or listener is null, cannot subscribe");
            return;
        }

        // 获取或创建订阅者列表
        List<WeakReference<EventListener<? extends PlayerEvent>>> listeners = subscribers.get(eventType);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            List<WeakReference<EventListener<? extends PlayerEvent>>> existing = subscribers.put(eventType, listeners);
            if (existing != null) {
                // 如果已存在，使用已存在的列表
                subscribers.put(eventType, existing);
                listeners = existing;
            }
        }

        // 检查是否已订阅
        for (WeakReference<EventListener<? extends PlayerEvent>> ref : listeners) {
            EventListener<? extends PlayerEvent> existing = ref.get();
            if (existing == listener) {
                LogHub.i(TAG, "Listener already subscribed for " + eventType.getSimpleName());
                return;
            }
        }

        listeners.add(new WeakReference<>(listener));
        LogHub.i(TAG, "Subscribed to " + eventType.getSimpleName());
    }

    /**
     * 取消订阅
     * <p>
     * 取消对指定事件类型的订阅。
     * </p>
     * <p>
     * <strong>注意</strong>：插槽内部不需要手动调用，插槽会自动管理订阅生命周期。
     * </p>
     * <p>
     * Unsubscribe from event
     * <p>
     * Unsubscribes from events of the specified type.
     * </p>
     *
     * @param eventType 事件类型，必须是 {@link PlayerEvent} 的子类
     * @param listener  事件监听器
     * @param <T>       事件类型，必须是 {@link PlayerEvent} 的子类
     */
    public <T extends PlayerEvent> void unsubscribe(@NonNull Class<T> eventType, @NonNull EventListener<T> listener) {
        if (eventType == null || listener == null) {
            return;
        }

        List<WeakReference<EventListener<? extends PlayerEvent>>> listeners = subscribers.get(eventType);
        if (listeners == null) {
            return;
        }

        removeListener(listeners, listener);

        // 如果列表为空，移除该事件类型
        if (listeners.isEmpty()) {
            subscribers.remove(eventType);
        }

        LogHub.i(TAG, "Unsubscribed from " + eventType.getSimpleName());
    }

    /**
     * 取消指定监听器的所有订阅
     * <p>
     * Unsubscribe all events for the specified listener
     * </p>
     *
     * @param listener 要取消订阅的监听器
     */
    public void unsubscribe(@NonNull EventListener<? extends PlayerEvent> listener) {
        if (listener == null) {
            return;
        }

        removeListenerFromAll(listener);

        LogHub.i(TAG, "Unsubscribed listener from all events");
    }

    /**
     * 取消所有订阅
     * <p>
     * 清除所有事件订阅。
     * <strong>注意：此方法会清除全局所有订阅，包括其他播放器实例的订阅，请谨慎使用。</strong>
     * 通常不需要手动调用，因为：
     * <ul>
     *     <li>插槽通过 {@link com.aliyun.playerkit.slot.SlotBehavior} 自动管理订阅生命周期</li>
     *     <li>事件总线使用弱引用，会自动清理已回收的订阅</li>
     * </ul>
     * </p>
     * <p>
     * Unsubscribe all
     * <p>
     * Clears all event subscriptions.
     * <strong>Note: This method will clear all global subscriptions, including subscriptions from other player instances. Use with caution.</strong>
     * </p>
     */
    public void unsubscribeAll() {
        subscribers.clear();
        LogHub.i(TAG, "All subscriptions cleared");
    }

    // ==================== 事件发布 ====================

    /**
     * 发布事件
     * <p>
     * 将事件发布给所有订阅该事件类型的监听器。
     * 事件会在当前线程同步分发。
     * </p>
     * <p>
     * Post event
     * <p>
     * Posts an event to all listeners subscribed to the event type.
     * Events are dispatched synchronously on the current thread.
     * </p>
     *
     * @param event 事件对象，必须是 {@link PlayerEvent} 或其子类
     */
    public void post(@NonNull PlayerEvent event) {
        if (event == null) {
            LogHub.w(TAG, "Event is null, cannot post");
            return;
        }

        // 使用 Set 去重，避免同一个监听器订阅了父类和子类时收到多次事件
        Set<EventListener<? extends PlayerEvent>> activeListeners = new LinkedHashSet<>();
        Class<?> currentType = event.getClass();

        // 遍历类层次结构，查找所有匹配的订阅者
        while (currentType != null && PlayerEvent.class.isAssignableFrom(currentType)) {
            //noinspection unchecked
            List<WeakReference<EventListener<? extends PlayerEvent>>> listeners = subscribers.get((Class<? extends PlayerEvent>) currentType);

            if (listeners != null && !listeners.isEmpty()) {
                // 临时列表用于清理和收集
                List<EventListener<? extends PlayerEvent>> currentTypeListeners = new ArrayList<>();
                cleanUpListeners(listeners, currentTypeListeners);
                activeListeners.addAll(currentTypeListeners);
            }

            currentType = currentType.getSuperclass();
        }

        // 如果没有活跃的监听器，则返回
        if (activeListeners.isEmpty()) {
            return;
        }

        // 分发事件给所有活跃的监听器
        for (EventListener<? extends PlayerEvent> listener : activeListeners) {
            try {
                dispatchEvent(event, listener);
            } catch (Exception e) {
                LogHub.e(TAG, "Error handling event: " + event.getClass().getSimpleName(), e);
            }
        }

//        LogHub.i(TAG, "Posted event: " + event.getClass().getSimpleName() + " to " + activeListeners.size() + " listeners");
    }

    /**
     * 类型安全的事件分发
     */
    @SuppressWarnings("unchecked")
    private <T extends PlayerEvent> void dispatchEvent(@NonNull T event, @NonNull EventListener<? extends PlayerEvent> listener) {
        // 由于订阅时已经确保了类型匹配，这里可以安全转换
        EventListener<T> typedListener = (EventListener<T>) listener;
        typedListener.onEvent(event);
    }

    /**
     * 获取指定事件类型的订阅者数量
     * <p>
     * Get subscriber count for specified event type
     * </p>
     *
     * @param eventType 事件类型
     * @return 订阅者数量
     */
    public int getSubscriberCount(@NonNull Class<? extends PlayerEvent> eventType) {
        List<WeakReference<EventListener<? extends PlayerEvent>>> listeners = subscribers.get(eventType);
        if (listeners == null) {
            return 0;
        }

        return countActiveListeners(listeners);
    }

    /**
     * 检查是否有订阅者
     * <p>
     * Check if there are any subscribers
     * </p>
     *
     * @param eventType 事件类型
     * @return 是否有订阅者
     */
    public boolean hasSubscribers(@NonNull Class<? extends PlayerEvent> eventType) {
        return getSubscriberCount(eventType) > 0;
    }

    // ==================== 事件监听回调 ====================

    /**
     * 事件监听器接口
     * <p>
     * Event Listener Interface
     * </p>
     *
     * @param <T> 事件类型，必须是 {@link PlayerEvent} 的子类
     */
    public interface EventListener<T extends PlayerEvent> {
        /**
         * 事件回调
         * <p>
         * Event callback
         * </p>
         *
         * @param event 事件对象
         */
        void onEvent(@NonNull T event);
    }

    // ==================== 辅助方法 ====================

    private void removeListener(@NonNull List<WeakReference<EventListener<? extends PlayerEvent>>> listeners, @NonNull EventListener<? extends PlayerEvent> target) {
        List<WeakReference<EventListener<? extends PlayerEvent>>> toRemove = new ArrayList<>();
        for (WeakReference<EventListener<? extends PlayerEvent>> ref : listeners) {
            EventListener<? extends PlayerEvent> existing = ref.get();
            if (existing == null || existing == target) {
                toRemove.add(ref);
            }
        }
        if (!toRemove.isEmpty()) {
            listeners.removeAll(toRemove);
        }
    }

    private void removeListenerFromAll(@NonNull EventListener<? extends PlayerEvent> target) {
        List<Class<? extends PlayerEvent>> emptyEventTypes = new ArrayList<>();

        for (Map.Entry<Class<? extends PlayerEvent>, List<WeakReference<EventListener<? extends PlayerEvent>>>> entry : subscribers.entrySet()) {
            List<WeakReference<EventListener<? extends PlayerEvent>>> listeners = entry.getValue();
            removeListener(listeners, target);
            if (listeners.isEmpty()) {
                emptyEventTypes.add(entry.getKey());
            }
        }

        for (Class<? extends PlayerEvent> eventType : emptyEventTypes) {
            subscribers.remove(eventType);
        }
    }

    private void cleanUpListeners(@NonNull List<WeakReference<EventListener<? extends PlayerEvent>>> listeners, @NonNull List<EventListener<? extends PlayerEvent>> activeListeners) {
        List<WeakReference<EventListener<? extends PlayerEvent>>> toRemove = new ArrayList<>();
        for (WeakReference<EventListener<? extends PlayerEvent>> ref : listeners) {
            EventListener<? extends PlayerEvent> listener = ref.get();
            if (listener != null) {
                activeListeners.add(listener);
            } else {
                toRemove.add(ref);
            }
        }
        if (!toRemove.isEmpty()) {
            listeners.removeAll(toRemove);
        }
    }

    private int countActiveListeners(@NonNull List<WeakReference<EventListener<? extends PlayerEvent>>> listeners) {
        int count = 0;
        List<WeakReference<EventListener<? extends PlayerEvent>>> toRemove = new ArrayList<>();
        for (WeakReference<EventListener<? extends PlayerEvent>> ref : listeners) {
            if (ref.get() != null) {
                count++;
            } else {
                toRemove.add(ref);
            }
        }
        if (!toRemove.isEmpty()) {
            listeners.removeAll(toRemove);
        }
        return count;
    }
}
