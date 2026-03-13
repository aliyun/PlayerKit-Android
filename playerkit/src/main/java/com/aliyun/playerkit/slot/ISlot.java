package com.aliyun.playerkit.slot;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;

/**
 * AliPlayerKit 插槽接口
 * <p>
 * 定义了插槽的最小生命周期契约，采用最小接口设计原则。
 * 所有插槽视图都应该实现此接口，以便宿主（{@link SlotHost}）能够统一管理插槽的生命周期。
 * </p>
 * <p>
 * <strong>双生命周期系统</strong>：
 * <ul>
 *     <li><strong>View 生命周期</strong>：{@link #onAttach(SlotHost)} → {@link #onDetach()}</li>
 *     <li><strong>Data 生命周期</strong>：{@link #onBindData(AliPlayerModel)} → {@link #onUnbindData()}</li>
 * </ul>
 * </p>
 * <p>
 * <strong>生命周期顺序</strong>：
 * <ol>
 *     <li>{@link #onAttach(SlotHost)} - 插槽被添加到宿主时调用，此时可以访问宿主</li>
 *     <li>{@link #onBindData(AliPlayerModel)} - 播放数据配置后调用，此时可以访问播放数据</li>
 *     <li>{@link #onUnbindData()} - 播放数据被清除前调用，用于清理数据相关资源</li>
 *     <li>{@link #onDetach()} - 插槽从宿主移除时调用，此时应该清理所有资源</li>
 * </ol>
 * </p>
 * <p>
 * <strong>实现方式</strong>：
 * <ul>
 *     <li><strong>继承方式</strong>：推荐继承 {@link BaseSlot} 来实现自定义插槽（适用于 View 类型的插槽）</li>
 *     <li><strong>组合方式</strong>：使用 {@link SlotBehavior} 通过组合方式实现（适用于非 View 类型的插槽）</li>
 * </ul>
 * </p>
 * <p>
 * AliPlayerKit Slot Interface
 * <p>
 * Defines the minimal lifecycle contract for slots, following the minimal interface design principle.
 * All slot views should implement this interface so that the host ({@link SlotHost}) can uniformly manage the slot lifecycle.
 * </p>
 *
 * @author keria
 * @date 2025/11/22
 */
public interface ISlot {

    /**
     * 插槽被附加到宿主时调用
     * <p>
     * 当插槽被添加到宿主布局（{@link SlotHostLayout}）时，宿主会调用此方法通知插槽。
     * 此时插槽已经添加到宿主布局中，可以安全地访问宿主。
     * </p>
     * <p>
     * <strong>注意事项</strong>：
     * <ul>
     *     <li>此方法在 UI 线程中调用，可以安全地进行 UI 操作</li>
     *     <li>如果使用 {@link BaseSlot}，事件订阅会自动处理，无需手动管理</li>
     *     <li>避免在此方法中执行耗时操作，以免阻塞 UI</li>
     *     <li>如果需要设置 Surface 或 DisplayView，应通过 {@link SlotHost#getSurfaceManager()} 获取 Surface 管理器</li>
     * </ul>
     * </p>
     * <p>
     * Called when the slot is attached to the host
     * <p>
     * When a slot is added to the host layout ({@link SlotHostLayout}), the host will call this method to notify the slot.
     * At this point, the slot has been added to the host layout and can safely access the host.
     * </p>
     *
     * @param host 插槽宿主，不能为 null。可通过 {@link SlotHost#getSurfaceManager()} 获取 Surface 管理器来设置 Surface 或 DisplayView
     */
    void onAttach(@NonNull SlotHost host);

    /**
     * 插槽从宿主分离时调用
     * <p>
     * 当插槽从宿主布局中移除时，宿主会调用此方法通知插槽进行清理。
     * 此时插槽即将从宿主布局中移除，应该立即清理所有资源。
     * </p>
     * <p>
     * <strong>注意事项</strong>：
     * <ul>
     *     <li>此方法在 UI 线程中调用</li>
     *     <li>必须确保所有资源都被正确清理，避免内存泄漏</li>
     *     <li>在此方法调用后，插槽不应再访问宿主或控制器</li>
     * </ul>
     * </p>
     * <p>
     * Called when the slot is detached from the host
     * <p>
     * When a slot is removed from the host layout, the host will call this method to notify the slot to clean up.
     * At this point, the slot is about to be removed from the host layout and should immediately clean up all resources.
     * </p>
     */
    void onDetach();

    /**
     * 数据绑定回调
     * <p>
     * 当播放数据通过 {@link AliPlayerController#configure(AliPlayerModel)} 设置后调用。
     * 用于执行依赖播放数据的初始化逻辑（如加载封面图、设置标题等）。
     * </p>
     * <p>
     * Data Binding Callback
     * <p>
     * Called after playback model is set via {@link AliPlayerController#configure(AliPlayerModel)}.
     * Used to perform initialization logic that depends on playback model (such as loading cover images, setting titles, etc.).
     * </p>
     *
     * @param model 播放数据，不会为 null / Playback model, not null
     */
    void onBindData(@NonNull AliPlayerModel model);

    /**
     * 数据解绑回调
     * <p>
     * 当播放数据被清除或更新时调用。
     * 用于清理依赖旧数据的资源（如清除图片缓存、重置 UI 状态等）。
     * </p>
     * <p>
     * Data Unbinding Callback
     * <p>
     * Called when playback data is cleared or updated.
     * Used to clean up resources dependent on old data (such as clearing image cache, resetting UI state, etc.).
     * </p>
     */
    void onUnbindData();
}
