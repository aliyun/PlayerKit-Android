package com.aliyun.playerkit.strategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 策略基类
 * <p>
 * 提供事件订阅管理和通用工具方法。
 * 子类只需关注业务逻辑，事件订阅和取消由基类自动管理。
 * </p>
 * <p>
 * Base Strategy Class
 * <p>
 * Provides event subscription management and common utility methods.
 * Subclasses only need to focus on business logic, event subscription and unsubscription
 * are automatically managed by the base class.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public abstract class BaseStrategy implements IStrategy, PlayerEventBus.EventListener<PlayerEvent> {

    private static final String TAG = "BaseStrategy";

    /**
     * 策略上下文
     */
    @Nullable
    protected StrategyContext mContext;

    /**
     * 已订阅的事件类型列表
     */
    private final List<Class<? extends PlayerEvent>> mSubscribedEvents = new CopyOnWriteArrayList<>();

    @Override
    public void onStart(@NonNull StrategyContext context) {
        mContext = context;
        LogHub.i(TAG, buildLogData("onStart"));
        subscribeEvents();
    }

    @Override
    public void onStop() {
        LogHub.i(TAG, buildLogData("onStop"));
        mContext = null;  // 先置空，防止onEvent中使用
        unsubscribeEvents();
    }

    @Override
    public void onReset() {
        LogHub.i(TAG, buildLogData("onReset"));
    }

    // ==================== Abstract Methods ====================

    /**
     * 指定需要订阅的事件类型
     * <p>
     * 子类重写此方法，返回需要监听的事件类型列表。
     * 如果返回 null 或空列表，则不订阅任何事件。
     * </p>
     * <p>
     * Specify event types to subscribe
     * <p>
     * Subclasses override this method to return a list of event types to listen.
     * If null or empty list is returned, no events will be subscribed.
     * </p>
     *
     * @return 要订阅的事件类型列表，null 表示不订阅任何事件
     */
    @Nullable
    protected abstract List<Class<? extends PlayerEvent>> observedEvents();

    /**
     * 事件回调
     * <p>
     * 子类实现此方法来处理订阅的事件。
     * </p>
     * <p>
     * Event callback
     * <p>
     * Subclasses implement this method to handle subscribed events.
     * </p>
     *
     * @param event 播放器事件
     */
    @Override
    public abstract void onEvent(@NonNull PlayerEvent event);

    // ==================== Helper Methods ====================

    /**
     * 检查当前播放器 ID 是否匹配
     * <p>
     * 用于过滤来自其他播放器实例的事件。
     * 在多播放器场景下，确保策略只处理当前播放器的事件。
     * </p>
     *
     * @param event 播放器事件
     * @return 如果事件来自当前播放器，返回 true；否则返回 false
     */
    protected boolean isCurrentPlayer(@NonNull PlayerEvent event) {
        return StringUtil.equals(event.playerId, getPlayerId());
    }

    /**
     * 获取播放器 ID
     * <p>
     * 从策略上下文中获取当前播放器的唯一标识。
     * </p>
     *
     * @return 播放器 ID，如果上下文为 null 则返回空字符串
     */
    @NonNull
    protected String getPlayerId() {
        return mContext != null ? mContext.getPlayerId() : "";
    }

    /**
     * 订阅事件
     * <p>
     * 根据子类返回的事件类型列表，自动订阅相应的事件。
     * 订阅的事件会被记录，以便在策略停止时自动取消订阅。
     * </p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void subscribeEvents() {
        List<Class<? extends PlayerEvent>> events = observedEvents();
        if (events == null || events.isEmpty()) {
            return;
        }

        // 订阅所有需要监听的事件类型
        for (Class<? extends PlayerEvent> eventType : events) {
            PlayerEventBus.getInstance().subscribe(eventType, (PlayerEventBus.EventListener) this);
            mSubscribedEvents.add(eventType);
        }

        LogHub.i(TAG, buildLogData("Subscribed to " + mSubscribedEvents.size() + " event types"));
    }

    /**
     * 取消事件订阅
     * <p>
     * 取消所有已订阅的事件，清理订阅记录。
     * 在策略停止时自动调用，确保不会产生内存泄漏。
     * </p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void unsubscribeEvents() {
        if (mSubscribedEvents.isEmpty()) {
            return;
        }

        // 取消所有已订阅的事件
        for (Class<? extends PlayerEvent> eventType : mSubscribedEvents) {
            PlayerEventBus.getInstance().unsubscribe(eventType, (PlayerEventBus.EventListener) this);
        }

        LogHub.i(TAG, buildLogData("Unsubscribed from " + mSubscribedEvents.size() + " event types"));
        mSubscribedEvents.clear();
    }

    // ==================== Private Methods ====================

    /**
     * 构建日志数据
     * <p>
     * 为日志添加策略名称和播放器 ID 信息，便于调试和问题定位。
     * </p>
     *
     * @param msg 日志消息
     * @return 格式化后的日志消息
     */
    @NonNull
    private String buildLogData(@NonNull String msg) {
        String pid = getPlayerId();
        String name = getName();

        if (StringUtil.isEmpty(pid)) {
            return "[" + name + "] " + msg;
        }
        return "[" + name + "][" + pid + "] " + msg;
    }
}
