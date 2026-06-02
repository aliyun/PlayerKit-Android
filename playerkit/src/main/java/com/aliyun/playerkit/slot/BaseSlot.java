package com.aliyun.playerkit.slot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.logging.LogHub;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AliPlayerKit 插槽基类
 * <p>
 * 轻量级 {@link FrameLayout} 包装器，实现了 {@link ISlot} 接口。
 * 核心逻辑已委托给 {@link SlotBehavior}
 * </p>
 * <p>
 * AliPlayerKit Base Slot Class
 * <p>
 * Lightweight {@link FrameLayout} wrapper that implements the {@link ISlot} interface.
 * Core logic is delegated to {@link SlotBehavior}.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public abstract class BaseSlot extends FrameLayout implements ISlot, IPlayerControl {

    private final String TAG = getClass().getSimpleName() + ".BaseSlot";

    /**
     * 插槽行为委托对象
     * <p>
     * 封装了插槽的核心逻辑（生命周期管理、事件订阅）。
     * 子类可以通过此对象访问宿主和控制器。
     * </p>
     */
    protected final SlotBehavior slotBehavior = new SlotBehavior();

    /**
     * 元素注册表
     * <p>
     * 存储已注册的插槽子元素及其控制句柄。
     * 框架会在 {@link #onBindData(AliPlayerModel)} 时自动根据隐藏配置控制已注册元素的可见性。
     * 元素在 {@link #onAttach(SlotHost)} 后通过 {@link #onRegisterElements()} 注册。
     * </p>
     * <p>
     * Element Registry
     * <p>
     * Stores registered slot sub-elements and their control handles.
     * The framework automatically controls the visibility of registered elements based on hidden configuration during {@link #onBindData(AliPlayerModel)}.
     * Elements are registered via {@link #onRegisterElements()} after {@link #onAttach(SlotHost)}.
     * </p>
     */
    private final Map<String, SlotElementHandle> mElementRegistry = new LinkedHashMap<>();

    /**
     * 隐藏元素配置缓存
     * <p>
     * 在 {@link #onBindData(AliPlayerModel)} 时从 Model 中提取并缓存，
     * 供 {@link #isElementVisible(String)} 进行运行时查询。
     * </p>
     * <p>
     * Hidden Elements Configuration Cache
     * <p>
     * Extracted and cached from the Model during {@link #onBindData(AliPlayerModel)},
     * used by {@link #isElementVisible(String)} for runtime queries.
     * </p>
     */
    @Nullable
    private Set<String> mHiddenElements;

    @Nullable
    private SlotType mSlotType;

    public BaseSlot(@NonNull Context context) {
        super(context);
        init();
    }

    public BaseSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            View.inflate(getContext(), layoutId, this);
        }
    }

    /**
     * 获取布局资源ID
     * <p>
     * 如果返回非 0 值，框架会自动加载对应的 XML 布局。
     * 布局的根视图会被添加到此 FrameLayout 中。
     * </p>
     * <p>
     * Get layout resource ID
     * <p>
     * If a non-zero value is returned, the framework will automatically load the corresponding XML layout.
     * The root view of the layout will be added to this FrameLayout.
     * </p>
     *
     * @return 布局资源ID，0 表示不使用 XML 布局
     */
    protected int getLayoutId() {
        return 0;
    }

    // ==================== 元素注册 ====================

    /**
     * Called by framework ({@link SlotHostLayout}) after
     * {@link ISlot#onAttach} returns. Injects the slot type
     * and triggers element registration.
     *
     * @param slotType the slot type assigned by the framework
     */
    void dispatchRegisterElements(@NonNull SlotType slotType) {
        mSlotType = slotType;
        onRegisterElements();
    }

    /**
     * 注册插槽元素（框架回调）
     * <p>
     * 子类覆写此方法，声明需要框架管理的插槽子元素。
     * 此方法由框架在 {@link #onAttach(SlotHost)} 返回后自动调用，
     * 此时 View 已初始化，子类不应手动调用此方法。
     * </p>
     * <p>
     * 使用示例：
     * <pre>
     * {@literal @}Override
     * protected void onRegisterElements() {
     *     registerElement(SlotElements.TopBar.BACK, mIvBack);
     *     registerElement(SlotElements.TopBar.TITLE, mTvTitle);
     * }
     * </pre>
     * </p>
     * <p>
     * Register Slot Elements (Framework Callback)
     * <p>
     * Subclasses override this method to declare sub-elements managed by the framework.
     * This method is automatically called by the framework after {@link #onAttach(SlotHost)} returns.
     * Views are initialized at this point. Subclasses should NOT call this method manually.
     * </p>
     */
    protected void onRegisterElements() {
        // 子类覆写，声明需要框架管理的元素
    }

    /**
     * 注册自定义控制的插槽元素
     * <p>
     * 将元素注册到框架的元素注册表中，使用自定义的 {@link SlotElementHandle} 控制逻辑。
     * 适用于非 View 类型的元素控制（如手势行为）或需要自定义显示/隐藏逻辑的场景。
     * </p>
     * <p>
     * Register custom-controlled slot element
     * <p>
     * Registers an element to the framework's element registry with a custom {@link SlotElementHandle} control logic.
     * Suitable for non-View type element control (e.g., gesture behaviors) or scenarios requiring custom show/hide logic.
     * </p>
     *
     * @param key    元素 key，对应 {@link SlotElements} 中的常量
     * @param handle 元素控制句柄，不能为 null
     */
    protected void registerElement(@NonNull String key, @NonNull SlotElementHandle handle) {
        mElementRegistry.put(key, handle);
    }

    /**
     * 注册一个 View 元素到框架。框架将自动管理该 View 的可见性
     * （{@link View#VISIBLE} / {@link View#GONE}）。
     *
     * <p>如果 view 为 null，则静默跳过注册，不会抛出异常。
     * 这在 View 可能因布局配置不同而不存在时是安全的。
     *
     * @param key  元素标识符，来自 {@link SlotElements}
     * @param view 要控制可见性的 View，可以为 null
     */
    protected void registerElement(@NonNull String key, @Nullable View view) {
        if (view == null) return;
        registerElement(key, new SlotElementHandle() {
            @Override
            public void setVisible(boolean visible) {
                view.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * 查询指定元素是否可见
     * <p>
     * 运行时查询方法，适用于行为型元素（如手势、事件驱动的显示逻辑）在决策点判断是否启用。
     * 如果框架未注入插槽类型或尚未调用 onBindData，默认返回 true（可见）。
     * </p>
     * <p>
     * Query whether the specified element is visible
     * <p>
     * Runtime query method, suitable for behavioral elements (e.g., gestures, event-driven display logic)
     * to determine whether to enable at decision points.
     * If the slot type is not injected by the framework or onBindData has not been called yet, returns true (visible) by default.
     * </p>
     *
     * @param key 元素 key，对应 {@link SlotElements} 中的常量
     * @return true 如果元素可见/启用，false 如果元素被隐藏/禁用
     */
    protected boolean isElementVisible(@NonNull String key) {
        if (mHiddenElements == null || mHiddenElements.isEmpty()) {
            return true;
        }
        return !mHiddenElements.contains(key);
    }

    /**
     * 查找子视图
     * <p>
     * 便捷方法，用于查找子视图，支持泛型自动转换。
     * </p>
     * <p>
     * Find child view
     * <p>
     * Convenience method for finding child views, supports generic automatic conversion.
     * </p>
     *
     * @param id  视图资源ID
     * @param <T> 视图类型
     * @return 找到的视图，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected <T extends View> T findViewByIdCompat(@IdRes int id) {
        return (T) findViewById(id);
    }

    /**
     * 插槽被附加到宿主时调用（视图生命周期）
     * <p>
     * 当插槽被添加到宿主布局（{@link SlotHostLayout}）时，宿主会调用此方法。
     * BaseSlot 在此方法中执行框架初始化（建立连接、事件订阅、Surface 设置）。
     * </p>
     * <p>
     * Slot Attached to Host Callback (View Lifecycle)
     * <p>
     * Called when the slot is added to the host layout. BaseSlot performs framework initialization
     * (establish connection, event subscription, Surface setup) in this method.
     * </p>
     *
     * @param host 插槽宿主，不能为 null
     */
    @CallSuper
    @Override
    public void onAttach(@NonNull SlotHost host) {
        LogHub.i(TAG, "onAttach");

        // 委托给 behavior 处理
        slotBehavior.attach(host);

        // 按需订阅事件
        subscribeSpecifiedEvents();

        // 设置显示视图
        setupDisplayViewIfNeeded();

        // 子类在 super 之后继续初始化视图
    }

    /**
     * 根据配置订阅事件
     * <p>
     * 按需订阅事件
     * <p>
     * 根据 {@link #observedEvents()} 返回的事件类型列表进行订阅。
     * </p>
     */
    private void subscribeSpecifiedEvents() {
        List<Class<? extends PlayerEvent>> eventTypes = observedEvents();

        if (eventTypes == null || eventTypes.isEmpty()) {
            LogHub.i(TAG, "No events to subscribe (null list)");
            return;
        }

        // 订阅指定的事件类型
        for (Class<? extends PlayerEvent> eventType : eventTypes) {
            slotBehavior.subscribe(eventType, this::onEvent);
        }
        LogHub.i(TAG, "Subscribed to " + eventTypes.size() + " custom event types");
    }

    /**
     * 指定要订阅的事件类型
     * <p>
     * 子类可以重写此方法，返回需要订阅的事件类型列表。
     * 如果返回 null 或空列表，则不订阅任何事件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>
     * // 方式 1：默认不订阅任何事件（返回 null）
     * {@literal @}Override
     * protected List&lt;Class&lt;? extends PlayerEvent&gt;&gt; observedEvents() {
     *     return null;  // 或者不重写此方法
     * }
     *
     * // 方式 2：只订阅指定事件
     * {@literal @}Override
     * protected List&lt;Class&lt;? extends PlayerEvent&gt;&gt; observedEvents() {
     *     return Arrays.asList(
     *         PlayerEvents.StateChanged.class,
     *         PlayerEvents.VideoSizeChanged.class
     *     );
     * }
     * </pre>
     * </p>
     * <p>
     * Specify Observed Event Types
     * <p>
     * Subclasses can override this method to return a list of event types to observe.
     * If null or empty list is returned, no events will be observed.
     * </p>
     *
     * @return 要订阅的事件类型列表，null 表示不订阅任何事件
     */
    @Nullable
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return null;  // 默认返回 null，不订阅任何事件
    }

    /**
     * 插槽从宿主分离时调用（视图生命周期）
     * <p>
     * 当插槽从宿主布局中移除时，宿主会调用此方法。
     * BaseSlot 在此方法中执行框架清理（断开连接、取消事件订阅）。
     * </p>
     * <p>
     * Slot Detached from Host Callback (View Lifecycle)
     * <p>
     * Called when the slot is removed from the host layout. BaseSlot performs framework cleanup
     * (disconnect, cancel event subscriptions) in this method.
     * </p>
     */
    @CallSuper
    @Override
    public void onDetach() {
        LogHub.i(TAG, "onDetach");

        // 子类在 super 之前清理视图资源

        // 清理元素注册表（确保 re-attach 时重新注册）
        mElementRegistry.clear();
        mSlotType = null;

        // 断开连接
        slotBehavior.detach();
    }

    /**
     * 事件处理回调
     * <p>
     * 所有播放器事件都会通过此方法统一处理。
     * 子类可以重写此方法，通过类型判断来过滤和处理需要的事件。
     * </p>
     * <p>
     * 使用示例：
     * <pre>
     * {@literal @}Override
     * protected void onEvent(@NonNull PlayerEvent event) {
     *     // 通过类型判断过滤和处理事件
     *     if (event instanceof PlayerEvents.StateChanged) {
     *         PlayerEvents.StateChanged stateEvent = (PlayerEvents.StateChanged) event;
     *         if (stateEvent.newState == PlayerState.PLAYING) {
     *             updatePlayButton(true);
     *         }
     *     // 可以继续处理其他事件类型...
     * }
     * </pre>
     * </p>
     * <p>
     * Event Handler Callback
     * <p>
     * All player events are handled through this method.
     * Subclasses can override this method to filter and handle needed events by type checking.
     * </p>
     *
     * @param event 播放器事件，不能为 null
     */
    protected void onEvent(@NonNull PlayerEvent event) {
        // 子类重写，通过类型判断处理需要的事件
    }

    /**
     * 数据绑定回调
     * <p>
     * 当播放数据配置后调用。
     * BaseSlot 提供空实现，子类可选择性重写以处理数据绑定逻辑。
     * </p>
     * <p>
     * Data Binding Callback
     * <p>
     * Called after playback data is configured.
     * BaseSlot provides empty implementation, subclasses can optionally override to handle data binding logic.
     * </p>
     *
     * @param model 播放数据，不会为 null
     */
    @CallSuper
    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        // 框架层：缓存元素隐藏配置并自动应用已注册元素的可见性
        applyElementRegistry();
    }

    /**
     * 数据解绑回调
     * <p>
     * 当播放数据被清除时调用。
     * BaseSlot 提供空实现，子类可选择性重写以清理数据相关资源。
     * </p>
     * <p>
     * Data Unbinding Callback
     * <p>
     * Called when playback data is cleared.
     * BaseSlot provides empty implementation, subclasses can optionally override to clean up data-related resources.
     * </p>
     */
    @CallSuper
    @Override
    public void onUnbindData() {
        // 框架层：重置所有已注册元素为可见状态，清空缓存
        resetElementRegistry();
    }

    /**
     * 应用元素注册表的可见性控制
     * <p>
     * 从 Model 中提取当前插槽的隐藏元素配置并缓存，
     * 然后遍历注册表中的所有元素，根据配置设置其可见性。
     * </p>
     */
    private void applyElementRegistry() {
        SlotType slotType = mSlotType;
        if (slotType == null) {
            return;
        }

        // 从 SlotManager 读取隐藏元素配置
        Set<String> elements = null;
        SlotHost host = getHost();
        if (host instanceof SlotHostLayout) {
            SlotManager slotManager = ((SlotHostLayout) host).getSlotManager();
            elements = slotManager.getHiddenElements(slotType);
        }

        mHiddenElements = elements;

        // 自动应用已注册元素的可见性
        if (mElementRegistry.isEmpty()) {
            return;
        }
        for (Map.Entry<String, SlotElementHandle> entry : mElementRegistry.entrySet()) {
            boolean visible = isElementVisible(entry.getKey());
            entry.getValue().setVisible(visible);
        }
    }

    /**
     * 重置元素注册表
     * <p>
     * 将所有已注册元素恢复为可见状态，清空隐藏配置缓存。
     * 确保切换视频源时元素状态被正确重置。
     * </p>
     */
    private void resetElementRegistry() {
        // 重置所有已注册元素为可见
        for (SlotElementHandle handle : mElementRegistry.values()) {
            handle.setVisible(true);
        }
        // 清空缓存
        mHiddenElements = null;
    }

    /**
     * 获取插槽宿主
     * <p>
     * 在插槽 attach 后可以获取宿主，用于访问宿主提供的能力。
     * 如果需要设置 Surface 或 DisplayView，应通过 {@link SlotHost#getSurfaceManager()} 获取 Surface 管理器。
     * </p>
     * <p>
     * Get slot host
     * <p>
     * Can get the host after the slot is attached, used to access capabilities provided by the host.
     * If you need to set Surface or DisplayView, get the Surface manager through {@link SlotHost#getSurfaceManager()}.
     * </p>
     *
     * @return 插槽宿主，如果未 attach 或已 detach 则返回 null
     */
    @Nullable
    public SlotHost getHost() {
        return slotBehavior.getHost();
    }

    /**
     * 发布事件到事件总线
     * <p>
     * 便捷方法，用于发布事件。如果宿主未 attach，会记录警告日志。
     * </p>
     * <p>
     * Post event to event bus
     * <p>
     * Convenience method for posting events. If the host is not attached, a warning log will be recorded.
     * </p>
     *
     * @param event 事件对象，不能为 null
     */
    protected void postEvent(@NonNull PlayerEvent event) {
        if (getHost() != null) {
            getHost().postEvent(event);
        } else {
            LogHub.w(TAG, "Cannot post event, host is null");
        }
    }

    // ==================== 事件订阅 =====================

    /**
     * 订阅特定类型的事件
     * <p>
     * 如果需要在 {@link #onEvent(PlayerEvent)} 之外单独订阅特定类型的事件，可以使用此方法。
     * </p>
     * <p>
     * 订阅会在 detach 时自动取消，无需手动管理。
     * </p>
     * <p>
     * Subscribe to specific event type (Advanced Usage)
     * <p>
     * If you need to subscribe to specific event types outside of {@link #onEvent(PlayerEvent)},
     * </p>
     *
     * @param eventType 事件类型，必须是 {@link PlayerEvent} 的子类
     * @param listener  事件监听器
     * @param <T>       事件类型，必须是 {@link PlayerEvent} 的子类
     */
    protected <T extends PlayerEvent> void subscribe(@NonNull Class<T> eventType, @NonNull PlayerEventBus.EventListener<T> listener) {
        slotBehavior.subscribe(eventType, listener);
    }

    /**
     * 根据需要设置显示视图
     * <p>
     * 如果插槽实现了 {@link ISurfaceProvider}，则会自动设置 Surface。
     * </p>
     */
    private void setupDisplayViewIfNeeded() {
        if (this instanceof ISurfaceProvider) {
            ISurfaceProvider provider = (ISurfaceProvider) this;
            SlotHost host = getHost();
            if (host != null) {
                provider.setupSurfaceProvider(host);
            }
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 显示插槽
     * <p>
     * 将插槽的可见性设置为 {@link View#VISIBLE}。
     * 如果插槽已经是可见状态，则不会重复设置。
     * </p>
     * <p>
     * Show Slot
     * <p>
     * Sets the slot's visibility to {@link View#VISIBLE}.
     * If the slot is already visible, this method does nothing.
     * </p>
     */
    public void show() {
        if (getVisibility() != View.VISIBLE) {
            LogHub.i(TAG, "Show slot");
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏插槽（占用布局空间）
     * <p>
     * 将插槽的可见性设置为 {@link View#INVISIBLE}。
     * 插槽将不可见，但仍占用布局空间。
     * 如果插槽已经是隐藏状态，则不会重复设置。
     * </p>
     * <p>
     * Hide Slot (Invisible)
     * <p>
     * Sets the slot's visibility to {@link View#INVISIBLE}.
     * The slot will be invisible but still take up layout space.
     * If the slot is already hidden, this method does nothing.
     * </p>
     */
    public void hide() {
        if (getVisibility() != View.INVISIBLE) {
            LogHub.i(TAG, "Hide slot (INVISIBLE)");
            setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 隐藏插槽（不占用布局空间）
     * <p>
     * 将插槽的可见性设置为 {@link View#GONE}，
     * 插槽将不可见且不占用布局空间。
     * 如果插槽已经是隐藏状态，则不会重复设置。
     * </p>
     * <p>
     * Hide Slot (Gone)
     * <p>
     * Sets the slot's visibility to {@link View#GONE},
     * making it invisible and not taking up layout space.
     * If the slot is already hidden, this method does nothing.
     * </p>
     */
    public void gone() {
        if (getVisibility() != View.GONE) {
            LogHub.i(TAG, "Hide slot (GONE)");
            setVisibility(View.GONE);
        }
    }

    /**
     * 检查插槽是否正在显示
     * <p>
     * 返回插槽是否满足以下两个条件：
     * <ul>
     *     <li>可见性为 {@link View#VISIBLE}</li>
     *     <li>尚未从父容器中移除（未被 dismiss）</li>
     * </ul>
     * </p>
     * <p>
     * Check if Slot is Showing
     * <p>
     * Returns whether the slot meets both conditions:
     * <ul>
     *     <li>Visibility is {@link View#VISIBLE}</li>
     *     <li>Not removed from parent (not dismissed)</li>
     * </ul>
     * </p>
     *
     * @return true 如果插槽正在显示，否则返回 false
     */
    public boolean isShow() {
        return getVisibility() == View.VISIBLE && getParent() != null;
    }

    /**
     * 销毁插槽
     * <p>
     * 将插槽从其父 ViewGroup 中移除。
     * 如果插槽没有父容器，此方法不会有任何效果。
     * </p>
     * <p>
     * <strong>注意</strong>：此方法只是从视图树中移除，不会触发 {@link #onDetach()} 回调。
     * 如果需要完整的生命周期清理，应该使用 SlotHostLayout 的相关方法。
     * </p>
     * <p>
     * Dismiss Slot
     * <p>
     * Removes the slot from its parent ViewGroup.
     * If the slot has no parent, this method has no effect.
     * </p>
     * <p>
     * <strong>Note</strong>: This only removes from the view tree and does not trigger {@link #onDetach()}.
     * For complete lifecycle cleanup, use SlotHostLayout's related methods.
     * </p>
     */
    public void dismiss() {
        if (getParent() instanceof ViewGroup) {
            LogHub.i(TAG, "Dismiss slot from parent");
            ((ViewGroup) getParent()).removeView(this);
        } else {
            LogHub.w(TAG, "Cannot dismiss slot: no parent found");
        }
    }
}
