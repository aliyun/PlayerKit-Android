package com.aliyun.playerkit.slot;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.SceneType;

/**
 * 插槽管理接口
 * <p>
 * 定义插槽系统的管理能力，供外部代码使用。
 * 与 {@link SlotHost} 接口分离，遵循接口隔离原则（ISP）。
 * </p>
 * <p>
 * <b>使用场景</b>：
 * <ul>
 *     <li>动态切换插槽：修改 SlotRegistry 后调用 {@link #rebuildSlots()}</li>
 *     <li>访问插槽视图：通过 {@link #getSlotView(SlotType)} 获取特定插槽</li>
 *     <li>数据生命周期管理：通过 {@link #bindData(AliPlayerModel)} 和 {@link #unbindData()} 管理数据绑定</li>
 *     <li>场景切换：通过 {@link #updateSceneType(int)} 切换播放场景</li>
 * </ul>
 * </p>
 * <p>
 * Slot Management Interface
 * <p>
 * Defines the management capabilities of the slot system for external code use.
 * Separated from the {@link SlotHost} interface, following the Interface Segregation Principle (ISP).
 * </p>
 * <p>
 * <b>Use cases</b>:
 * <ul>
 *     <li>Dynamic switching slots: Modify SlotRegistry and call {@link #rebuildSlots()}</li>
 *     <li>Accessing slot views: Use {@link #getSlotView(SlotType)} to get a specific slot</li>
 *     <li>Data lifecycle management: Use {@link #bindData(AliPlayerModel)} and {@link #unbindData()} to manage data binding</li>
 *     <li>Scene switching: Use {@link #updateSceneType(int)} to switch playback scenes</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2025/12/11
 */
public interface ISlotManager {

    /**
     * 重建所有插槽
     * <p>
     * 根据当前场景类型和插槽注册表，重新构建所有插槽视图。
     * 会先调用旧插槽的 {@link ISlot#onDetach()}，然后创建新插槽并调用 {@link ISlot#onAttach(SlotHost)}。
     * </p>
     * <p>
     * <b>使用场景</b>：
     * <ul>
     *     <li>动态切换插槽：修改 SlotRegistry 后调用此方法重建插槽</li>
     *     <li>场景切换：切换播放场景后重建插槽</li>
     *     <li>插槽配置变更：插槽注册表变更后重建插槽</li>
     * </ul>
     * </p>
     * <p>
     * Rebuild all slots
     * <p>
     * Rebuild all slot views based on current scene type and slot registry.
     * Will first call {@link ISlot#onDetach()} for old slots, then create new slots and call {@link ISlot#onAttach(SlotHost)}.
     * </p>
     */
    void rebuildSlots();

    /**
     * 获取指定类型的插槽视图
     * <p>
     * 返回当前已创建的插槽视图。如果插槽尚未创建或已被移除，则返回 null。
     * </p>
     * <p>
     * Get slot view of specified type
     * <p>
     * Returns the currently created slot view. Returns null if the slot has not been created or has been removed.
     * </p>
     *
     * @param slotType 插槽类型，不能为 null
     * @return 插槽视图，如果不存在则返回 null
     */
    @Nullable
    View getSlotView(@NonNull SlotType slotType);

    /**
     * 绑定播放数据
     * <p>
     * 将播放数据绑定到插槽系统，并通知所有插槽数据已配置。
     * 每个插槽的 {@link ISlot#onBindData(AliPlayerModel)} 方法会被调用。
     * </p>
     * <p>
     * Bind playback model
     * <p>
     * Binds playback model to the slot system and notifies all slots that model is configured.
     * Each slot's {@link ISlot#onBindData(AliPlayerModel)} method will be called.
     * </p>
     *
     * @param model 播放数据，不能为 null
     */
    void bindData(@NonNull AliPlayerModel model);

    /**
     * 解绑播放数据
     * <p>
     * 清除插槽系统的数据，并通知所有插槽数据已清除。
     * 每个插槽的 {@link ISlot#onUnbindData()} 方法会被调用。
     * </p>
     * <p>
     * Unbind playback data
     * <p>
     * Clears the data in the slot system and notifies all slots that data is cleared.
     * Each slot's {@link ISlot#onUnbindData()} method will be called.
     * </p>
     */
    void unbindData();

    /**
     * 更新场景类型
     * <p>
     * 当播放场景发生变化时（例如从 VOD 切换到 LIVE），调用此方法更新场景类型并重建插槽。
     * 采用增量更新策略，只更新变化的插槽。
     * </p>
     * <p>
     * Update scene type
     * <p>
     * When the playback scene changes (e.g., switching from VOD to LIVE), call this method to update the scene type and rebuild slots.
     * Uses incremental update strategy to only update changed slots.
     * </p>
     *
     * @param sceneType 新的场景类型
     */
    void updateSceneType(@SceneType int sceneType);
}
