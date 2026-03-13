package com.aliyun.playerkit;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.aliyun.playerkit.slot.ISlotManager;
import com.aliyun.playerkit.slot.SlotHostLayout;
import com.aliyun.playerkit.slot.SlotRegistry;
import com.aliyun.playerkit.slot.SlotType;
import com.aliyun.playerkit.ui.DefaultSlotRegistryFactory;
import com.aliyun.playerkit.ui.slots.FullscreenSlot;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.ContextUtil;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

/**
 * AliPlayerKit 播放组件
 * <p>
 * 这是播放组件的根视图，负责整合控制器、插槽系统和播放器 UI。
 * 它是播放组件的入口点，您通过此视图来使用播放器功能。
 * </p>
 * <p>
 * AliPlayerKit
 * <p>
 * This is the root view of the playback component, responsible for integrating the controller, slot system, and player UI.
 * It is the entry point for the playback component, and you can use the player function through this view.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class AliPlayerView extends FrameLayout {

    private static final String TAG = "AliPlayerView";

    /**
     * 插槽宿主布局，管理所有插槽视图
     */
    @NonNull
    private final SlotHostLayout slotHostLayout;

    /**
     * 播放控制器
     */
    @Nullable
    private AliPlayerController controller;

    /**
     * 是否已绑定控制器
     */
    private boolean isAttached = false;

    /**
     * 已绑定的 LifecycleOwner，用于避免重复绑定
     */
    @Nullable
    private LifecycleOwner boundLifecycleOwner;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public AliPlayerView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public AliPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造函数
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public AliPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        slotHostLayout = new SlotHostLayout(context);
        addView(slotHostLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        LogHub.i(TAG, "View created");
    }

    // ==================== 控制器绑定 ====================

    /**
     * 绑定控制器（使用默认插槽注册表）
     *
     * @param controller 播放控制器
     * @param model      播放数据
     */
    public void attach(@NonNull AliPlayerController controller, @NonNull AliPlayerModel model) {
        attach(controller, model, null);
    }

    /**
     * 绑定控制器（可自定义插槽注册表）
     * <p>
     * 将播放控制器、数据和插槽注册表绑定到视图。
     * 绑定后会自动初始化播放器并配置数据。
     * </p>
     *
     * @param controller 播放控制器，不能为 null（由外部创建和管理）
     * @param model      播放器数据配置，不能为 null
     * @param registry   插槽注册表，如果为空则使用默认实现
     * @throws IllegalStateException 如果已经绑定过控制器
     */
    public void attach(@NonNull AliPlayerController controller, @NonNull AliPlayerModel model, @Nullable SlotRegistry registry) {
        if (isAttached) {
            LogHub.w(TAG, "Controller already attached, detach first");
            throw new IllegalStateException("Controller already attached. Call detach() first.");
        }

        this.controller = controller;
        SlotRegistry actualRegistry = registry != null ? registry : DefaultSlotRegistryFactory.create();

        try {
            // 配置播放数据
            controller.configure(model);

            // 注册默认插槽配置
            DefaultSlotRegistryFactory.registerDefaultConfigs(slotHostLayout);

            // 绑定插槽系统
            slotHostLayout.bind(controller, model.getSceneType(), actualRegistry);

            // 设置屏幕常亮（根据数据配置，在数据绑定前设置）
            updateKeepScreenOnState(model);

            // 应用禁止截屏设置
            updateScreenshotDisabledState();

            // 通知 Slot 数据已配置（触发 onBindData 回调）
            slotHostLayout.bindData(model);

            isAttached = true;
            LogHub.i(TAG, "Controller attached successfully");

            // 自动绑定生命周期
            if (isAttachedToWindow()) {
                tryAutoBindLifecycle();
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 参数或状态错误，重新抛出
            LogHub.e(TAG, "Failed to attach controller: " + e.getMessage(), e);
            cleanupOnError();
            throw e;
        } catch (Exception e) {
            // 其他异常包装为 RuntimeException
            LogHub.e(TAG, "Unexpected error during controller attachment", e);
            cleanupOnError();
            throw new RuntimeException("Failed to attach controller", e);
        }
    }

    /**
     * 错误时清理状态
     */
    private void cleanupOnError() {
        this.controller = null;
        isAttached = false;
    }

    // ==================== 生命周期处理 ====================

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogHub.i(TAG, "View attached to window");

        // 应用禁止截屏设置（View 附加到窗口时）
        updateScreenshotDisabledState();

        // 如果控制器已绑定，尝试自动绑定生命周期
        if (isAttached && controller != null) {
            tryAutoBindLifecycle();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // 确保释放屏幕常亮（View 生命周期结束时必须释放）
        setKeepScreenOn(false);

        LogHub.i(TAG, "View detached from window");
    }

    /**
     * 尝试自动绑定生命周期
     */
    private void tryAutoBindLifecycle() {
        if (controller == null) {
            LogHub.w(TAG, "tryAutoBindLifecycle: controller is null, skip.");
            return;
        }

        LifecycleOwner lifecycleOwner = findLifecycleOwner();
        if (lifecycleOwner == null) {
            LogHub.i(TAG, "No LifecycleOwner found in Context hierarchy, lifecycle binding skipped. " + "You can manually call bindLifecycle() if needed.");
            return;
        }

        // 避免重复绑定同一个 LifecycleOwner
        if (lifecycleOwner == boundLifecycleOwner) {
            LogHub.i(TAG, "tryAutoBindLifecycle: LifecycleOwner already bound, skip.");
            return;
        }

        boolean success = bindLifecycle(lifecycleOwner);
        if (success) {
            boundLifecycleOwner = lifecycleOwner;
            LogHub.i(TAG, "Lifecycle auto-bound successfully");
        }
    }

    /**
     * 查找 LifecycleOwner
     * <p>
     * 从 Context 层级中查找实现了 LifecycleOwner 的 Activity 或 ContextWrapper
     */
    @Nullable
    private LifecycleOwner findLifecycleOwner() {
        Context current = getContext();

        while (current != null) {
            if (current instanceof LifecycleOwner) {
                return (LifecycleOwner) current;
            }

            if (!(current instanceof ContextWrapper)) {
                return null;
            }

            Context base = ((ContextWrapper) current).getBaseContext();
            if (base == current) {
                return null; // 防止死循环
            }

            current = base;
        }
        return null;
    }

    /**
     * 解绑控制器
     * <p>
     * 释放所有资源，包括播放器实例、插槽视图等。
     * 建议在 Activity/Fragment 的 onDestroy/onDestroyView 中调用。
     * </p>
     */
    public void detach() {
        if (!isAttached) {
            LogHub.w(TAG, "Controller not attached, nothing to detach");
            return;
        }

        LogHub.i(TAG, "Detaching");

        // 先设置标志位，确保状态一致性
        isAttached = false;
        boundLifecycleOwner = null;

        // 解绑插槽系统
        slotHostLayout.unbind();

        // 销毁控制器
        if (controller != null) {
            controller.destroy();
        }

        // 清理引用
        controller = null;

        // 释放屏幕常亮（解绑时必须释放）
        setKeepScreenOn(false);

        LogHub.i(TAG, "Controller detached");
    }

    /**
     * 更新屏幕常亮状态
     * <p>
     * 根据播放数据配置决定是否保持屏幕常亮。
     * 当业务不允许屏幕休眠时（allowedScreenSleep = false），需要保持屏幕常亮。
     * 当业务允许屏幕休眠时（allowedScreenSleep = true），不保持常亮，交给系统处理。
     * </p>
     * <p>
     * Update Screen Keep-On State
     * <p>
     * Determines whether to keep the screen on based on playback model configuration.
     * When business does not allow screen sleep (allowedScreenSleep = false), screen should be kept on.
     * When business allows screen sleep (allowedScreenSleep = true), do not keep on, let system handle it.
     * </p>
     *
     * @param model 播放数据配置，不能为 null
     */
    private void updateKeepScreenOnState(@NonNull AliPlayerModel model) {
        boolean keepOn = !model.isAllowedScreenSleep();
        setKeepScreenOn(keepOn);
        LogHub.i(TAG, "Keep screen on: " + keepOn + " (allowedScreenSleep=" + model.isAllowedScreenSleep() + ")");
    }

    /**
     * 更新禁止截屏状态
     * <p>
     * 根据 AliPlayerKit 的全局设置决定是否禁止截屏。
     * 当禁止截屏时，会在 Activity 窗口上设置 FLAG_SECURE 标志。
     * </p>
     * <p>
     * Update Screenshot Disabled State
     * <p>
     * Determines whether to disable screenshot based on AliPlayerKit global settings.
     * When screenshot is disabled, sets FLAG_SECURE flag on the Activity window.
     * </p>
     */
    private void updateScreenshotDisabledState() {
        boolean disable = AliPlayerKit.isDisableScreenshot();
        setScreenshotDisabled(disable);
        LogHub.i(TAG, "Disable screenshot: " + disable);
    }

    /**
     * 设置是否禁止截屏
     * <p>
     * 在 Activity 窗口上设置或清除 FLAG_SECURE 标志。
     * </p>
     * <p>
     * Set whether to disable screenshot
     * <p>
     * Sets or clears FLAG_SECURE flag on the Activity window.
     * </p>
     *
     * @param disable true 表示禁止截屏，false 表示允许截屏
     */
    private void setScreenshotDisabled(boolean disable) {
        Activity activity = ContextUtil.getActivity(getContext());
        if (activity == null) {
            LogHub.w(TAG, "Cannot set screenshot disabled, Activity not found in Context");
            return;
        }

        Window window = activity.getWindow();
        if (window == null) {
            LogHub.w(TAG, "Cannot set screenshot disabled, Window is null");
            return;
        }

        if (disable) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    /**
     * 绑定生命周期（可选，通常不需要手动调用）
     * <p>
     * 将控制器注册到生命周期观察者，自动处理生命周期事件。
     * </p>
     *
     * @param lifecycleOwner 生命周期所有者（通常是 Activity 或 Fragment）
     * @return true 如果绑定成功，false 如果绑定失败（例如不支持 LifecycleOwner）
     */
    public boolean bindLifecycle(@NonNull LifecycleOwner lifecycleOwner) {
        if (controller == null) {
            LogHub.w(TAG, "Controller is null, cannot bind lifecycle");
            return false;
        }

        try {
            lifecycleOwner.getLifecycle().addObserver(controller);
            LogHub.i(TAG, "Lifecycle bound successfully");
            return true;
        } catch (Exception e) {
            LogHub.w(TAG, "Failed to bind lifecycle. You may need to handle lifecycle manually.", e);
            return false;
        }
    }

    // ==================== 插槽系统访问 ====================

    /**
     * 处理返回键按下事件
     * <p>
     * 在 Activity 的 onBackPressed 中调用此方法。
     * 如果当前处于全屏状态，则退出全屏并返回 true（表示已处理）。
     * 否则返回 false（表示未处理，由 Activity 自行处理）。
     * </p>
     * <p>
     * 使用示例：
     * <pre>
     * {@code
     * @Override
     * public void onBackPressed() {
     *     if (playerKit != null && playerKit.onBackPressed()) {
     *         // 已处理返回键（退出全屏），不需要执行默认行为
     *         return;
     *     }
     *     // 未处理，执行默认行为（关闭 Activity）
     *     super.onBackPressed();
     * }
     * }
     * </pre>
     * </p>
     *
     * @return true 如果已处理返回键（退出全屏），false 否则
     */
    public boolean onBackPressed() {
        View fullscreenSlotView = slotHostLayout.getSlotView(SlotType.FULLSCREEN);
        if (fullscreenSlotView instanceof FullscreenSlot) {
            return ((FullscreenSlot) fullscreenSlotView).onBackPressed();
        }
        return false;
    }

    /**
     * 获取插槽管理器
     * <p>
     * 返回插槽管理接口，用于动态管理插槽系统。
     * 通过此接口，可以在运行时动态切换插槽、访问插槽视图、管理数据绑定等，而无需重建整个播放器组件。
     * </p>
     * <p>
     * <b>架构设计说明</b>：
     * <ul>
     *     <li><b>接口隔离原则</b>：ISlotManager 接口专注于插槽管理能力，与 SlotHost 接口（依赖访问）职责分离</li>
     *     <li><b>依赖倒置原则</b>：外部代码依赖 ISlotManager 接口，而非具体实现类，提高可维护性和可扩展性</li>
     *     <li><b>封装性</b>：不暴露 SlotHostLayout 的具体实现细节，保持内部实现的灵活性</li>
     * </ul>
     * </p>
     * <p>
     * <b>使用场景</b>：
     * <ul>
     *     <li>动态切换插槽：修改 SlotRegistry 后调用 {@link ISlotManager#rebuildSlots()}</li>
     *     <li>访问插槽视图：通过 {@link ISlotManager#getSlotView(SlotType)} 获取特定插槽</li>
     *     <li>数据生命周期管理：通过 {@link ISlotManager#bindData(AliPlayerModel)} 和 {@link ISlotManager#unbindData()} 管理数据绑定</li>
     *     <li>场景切换：通过 {@link ISlotManager#updateSceneType(int)} 切换播放场景</li>
     * </ul>
     * </p>
     * <p>
     * <b>注意事项</b>：
     * <ul>
     *     <li>建议在 {@link #attach(AliPlayerController, AliPlayerModel, SlotRegistry)} 之后调用</li>
     *     <li>虽然 SlotHostLayout 在构造时已创建，但只有在 attach 之后才真正初始化</li>
     *     <li>在 detach 之后，ISlotManager 仍然可用，但插槽可能已被清理</li>
     * </ul>
     * </p>
     * <p>
     * Get Slot Manager
     * <p>
     * Returns the slot management interface for dynamically managing the slot system.
     * Through this interface, slots can be dynamically switched at runtime, slot views can be accessed, data binding can be managed, etc., without rebuilding the entire player component.
     * </p>
     * <p>
     * <b>Architecture Design Notes</b>:
     * <ul>
     *     <li><b>Interface Segregation Principle</b>: ISlotManager interface focuses on slot management capabilities, separated from SlotHost interface (dependency access) responsibilities</li>
     *     <li><b>Dependency Inversion Principle</b>: External code depends on ISlotManager interface rather than concrete implementation classes, improving maintainability and extensibility</li>
     *     <li><b>Encapsulation</b>: Does not expose SlotHostLayout's concrete implementation details, maintaining flexibility of internal implementation</li>
     * </ul>
     * </p>
     *
     * @return 插槽管理器接口，不会为 null
     */
    @NonNull
    public ISlotManager getSlotManager() {
        return slotHostLayout;
    }
}
