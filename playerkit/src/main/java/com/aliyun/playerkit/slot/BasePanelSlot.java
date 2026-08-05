package com.aliyun.playerkit.slot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 弹出面板插槽基类。
 * <p>
 * 封装所有弹出面板类插槽的通用逻辑：
 * <ul>
 *     <li>面板显隐的状态管理和防重入保护</li>
 *     <li>蒙层点击关闭的绑定机制</li>
 *     <li>生命周期清理（onDetach 时重置状态、取消动画）</li>
 * </ul>
 * <p>
 * 子类需实现 {@link #onPerformShowAnimation()} 和 {@link #onPerformHideAnimation()}
 * 来定义具体的动画行为（translationY、translationX、alpha 等）。
 * <p>
 * 基类还提供了一组便利的动画工具方法（如 {@link #animateOverlayIn}、{@link #animateTranslationYIn} 等），
 * 子类可组合使用以减少重复代码。
 *
 * @author keria
 * @date 2026/07/03
 */
public abstract class BasePanelSlot extends BaseSlot {

    private static final long DEFAULT_ANIMATION_DURATION = 300L;

    private boolean mIsShowing = false;

    public BasePanelSlot(@NonNull Context context) {
        super(context);
    }

    // ==================== 子类必须实现 ====================

    /**
     * 执行面板显示动画。
     * <p>
     * 子类在此方法中实现具体的入场动画逻辑。
     * 动画完成后应调用 {@link #onPanelShown()} 通知基类。
     */
    protected abstract void onPerformShowAnimation();

    /**
     * 执行面板隐藏动画。
     * <p>
     * 子类在此方法中实现具体的退场动画逻辑。
     * 动画完成后必须调用 {@link #onPanelHidden()} 通知基类。
     */
    protected abstract void onPerformHideAnimation();

    // ==================== 子类可选覆写 ====================

    /**
     * 获取蒙层 View。返回 null 表示无蒙层。
     */
    @Nullable
    protected View getOverlayView() {
        return null;
    }

    /**
     * 获取动画时长（毫秒）。子类可覆写自定义时长。
     */
    protected long getAnimationDuration() {
        return DEFAULT_ANIMATION_DURATION;
    }

    /**
     * 蒙层被点击时的回调。默认实现直接调用 {@link #hidePanel()}。
     * <p>
     * 子类可覆写以实现发送事件等业务行为（如通知按钮同步状态）。
     */
    protected void onOverlayClicked() {
        hidePanel();
    }

    /**
     * 面板完全显示后的回调。
     */
    protected void onPanelShown() {
        // 默认无操作
    }

    /**
     * 面板完全隐藏后的回调。
     */
    protected void onPanelHidden() {
        // 默认无操作
    }

    // ==================== 通用逻辑（final） ====================

    /**
     * 显示面板。执行状态检查后调用子类的 {@link #onPerformShowAnimation()}。
     */
    protected final void showPanel() {
        if (mIsShowing) return;
        mIsShowing = true;
        onPerformShowAnimation();
    }

    /**
     * 隐藏面板。执行状态检查后调用子类的 {@link #onPerformHideAnimation()}。
     */
    protected final void hidePanel() {
        if (!mIsShowing) return;
        mIsShowing = false;
        onPerformHideAnimation();
    }

    /**
     * 查询面板是否正在显示。
     */
    public final boolean isShowing() {
        return mIsShowing;
    }

    /**
     * 初始化蒙层的点击监听。子类应在初始化时调用此方法绑定蒙层。
     */
    protected void setupOverlayClickListener() {
        View overlay = getOverlayView();
        if (overlay != null) {
            overlay.setOnClickListener(v -> onOverlayClicked());
        }
    }

    @CallSuper
    @Override
    public void onDetach() {
        mIsShowing = false;
        View overlay = getOverlayView();
        if (overlay != null) {
            overlay.animate().cancel();
            overlay.setOnClickListener(null);
        }
        super.onDetach();
    }

    // ==================== 动画工具方法（子类可选用） ====================

    /**
     * 蒙层淡入动画。
     *
     * @param overlay 蒙层 View
     */
    protected void animateOverlayIn(@NonNull View overlay) {
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        overlay.animate().setListener(null).alpha(1f).setDuration(getAnimationDuration()).start();
    }

    /**
     * 蒙层淡出动画。
     *
     * @param overlay 蒙层 View
     */
    protected void animateOverlayOut(@NonNull final View overlay) {
        overlay.animate().alpha(0f).setDuration(getAnimationDuration()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        }).start();
    }

    /**
     * Y 轴位移入场动画（底部弹出）。
     *
     * @param panel       面板 View
     * @param fromY       起始 translationY 值（通常为面板高度）
     * @param onEndAction 动画结束后的回调（可为 null）
     */
    protected void animateTranslationYIn(@NonNull View panel, float fromY, @Nullable Runnable onEndAction) {
        panel.setVisibility(View.VISIBLE);
        panel.setTranslationY(fromY);
        panel.animate().setListener(null).translationY(0).setDuration(getAnimationDuration()).withEndAction(onEndAction).start();
    }

    /**
     * Y 轴位移退场动画（向下收回）。
     *
     * @param panel       面板 View
     * @param toY         目标 translationY 值（通常为面板高度）
     * @param onEndAction 动画结束后的回调（可为 null）
     */
    protected void animateTranslationYOut(@NonNull final View panel, float toY, @Nullable Runnable onEndAction) {
        panel.animate().translationY(toY).setDuration(getAnimationDuration()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                panel.setVisibility(View.GONE);
                if (onEndAction != null) onEndAction.run();
            }
        }).start();
    }

    /**
     * X 轴位移入场动画（侧边滑入）。
     *
     * @param panel       面板 View
     * @param fromX       起始 translationX 值（通常为面板宽度）
     * @param onEndAction 动画结束后的回调（可为 null）
     */
    protected void animateTranslationXIn(@NonNull View panel, float fromX, @Nullable Runnable onEndAction) {
        panel.setVisibility(View.VISIBLE);
        panel.setTranslationX(fromX);
        panel.animate().setListener(null).translationX(0).setDuration(getAnimationDuration()).withEndAction(onEndAction).start();
    }

    /**
     * X 轴位移退场动画（向侧边收回）。
     *
     * @param panel       面板 View
     * @param toX         目标 translationX 值（通常为面板宽度）
     * @param onEndAction 动画结束后的回调（可为 null）
     */
    protected void animateTranslationXOut(@NonNull final View panel, float toX, @Nullable Runnable onEndAction) {
        panel.animate().translationX(toX).setDuration(getAnimationDuration()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                panel.setVisibility(View.GONE);
                if (onEndAction != null) onEndAction.run();
            }
        }).start();
    }

    /**
     * Alpha 渐显入场动画。
     *
     * @param view        目标 View
     * @param onEndAction 动画结束后的回调（可为 null）
     */
    protected void animateAlphaIn(@NonNull View view, @Nullable Runnable onEndAction) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate().setListener(null).alpha(1f).setDuration(getAnimationDuration()).withEndAction(onEndAction).start();
    }

    /**
     * Alpha 渐隐退场动画。
     *
     * @param view        目标 View
     * @param onEndAction 动画结束后的回调（可为 null）
     */
    protected void animateAlphaOut(@NonNull final View view, @Nullable Runnable onEndAction) {
        view.animate().alpha(0f).setDuration(getAnimationDuration()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onEndAction != null) onEndAction.run();
            }
        }).start();
    }
}
