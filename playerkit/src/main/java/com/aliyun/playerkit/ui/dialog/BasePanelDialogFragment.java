package com.aliyun.playerkit.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.utils.DensityUtil;

/**
 * 面板弹窗通用基类。
 * <p>
 * 封装全屏防护、入退场动画（蒙层淡入淡出 + 面板平移）、防重入 dismiss、
 * 方向检测等通用逻辑。子类只需实现少量抽象方法即可获得完整面板弹窗能力。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public abstract class BasePanelDialogFragment extends DialogFragment {

    // -- Animation state --
    @Nullable
    private ViewPropertyAnimator mOverlayAnimator;
    @Nullable
    private ViewPropertyAnimator mPanelAnimator;

    /**
     * 是否正在执行退出动画（防止重入）
     */
    private boolean mDismissing = false;

    // -- Callbacks --
    @Nullable
    private OnDismissListener mOnDismissListener;

    // ==================== 抽象方法（子类必须实现） ====================

    /**
     * 返回面板布局资源 ID。
     */
    protected abstract int getLayoutResId();

    /**
     * 返回蒙层根 View（用于蒙层点击关闭和淡入淡出动画）。
     */
    @Nullable
    protected abstract View getDialogRootView();

    /**
     * 返回当前方向的面板 View（用于平移动画计算）。
     */
    @Nullable
    protected abstract View getCurrentPanelView();

    /**
     * 子类初始化视图和数据绑定，在 onViewCreated 后调用。
     */
    protected abstract void onBindPanelViews(@NonNull View root);

    // ==================== 可覆写 Hook 方法 ====================

    /**
     * 动画时长（毫秒）。
     */
    protected long getAnimationDuration() {
        return 300L;
    }

    /**
     * 竖屏面板高度的回退值（首帧 layout 尚未完成时用于动画位移计算）。
     */
    protected float getFallbackPanelHeight() {
        return DensityUtil.dip2px(requireContext(), 400);
    }

    /**
     * 横屏面板宽度的回退值（首帧 layout 尚未完成时用于动画位移计算）。
     */
    protected float getFallbackPanelWidth() {
        return DensityUtil.dip2px(requireContext(), 345);
    }

    /**
     * 蒙层点击回调，默认执行带动画关闭。子类可覆写自定义行为。
     */
    protected void onOverlayClicked() {
        dismissWithAnimation();
    }

    /**
     * 方向变化回调（预留 Hook）。
     */
    protected void onOrientationChanged(boolean isLandscape) {
        // no-op, subclass may override
    }

    // ==================== 公开 API ====================

    /**
     * 设置面板弹窗的关闭回调。
     * <p>
     * 回调在退出动画完成并真正关闭 Dialog 后触发。
     * </p>
     * <p>
     * Set dismiss callback for this panel dialog.
     * Invoked after exit animation finishes and the dialog is dismissed.
     * </p>
     *
     * @param listener 回调接口，可为 null
     */
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    /**
     * 以带动画的方式关闭面板弹窗。
     * <p>
     * 播放退出动画后真正关闭 Dialog。重复调用会被忽略。
     * </p>
     * <p>
     * Dismiss this panel dialog with exit animation.
     * Subsequent calls during dismiss are ignored.
     * </p>
     */
    public void dismissWithAnimation() {
        if (mDismissing) return;
        mDismissing = true;
        playExitAnimation(() -> {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss();
            }
            if (isAdded()) {
                dismissAllowingStateLoss();
            }
        });
    }

    // ==================== Lifecycle ====================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PlayerKit_SettingMenuDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        Activity activity = getActivity();

        if (window != null && activity != null) {
            Window activityWindow = activity.getWindow();
            int activityFlags = activityWindow.getAttributes().flags;
            int activityUiVisibility = activityWindow.getDecorView().getSystemUiVisibility();

            // 仅在 Activity 处于全屏模式时应用防护
            boolean isFullscreen = (activityFlags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0
                    || (activityUiVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;

            if (isFullscreen) {
                // 1. 传播 immersive flags 到 Dialog Window
                window.getDecorView().setSystemUiVisibility(activityUiVisibility);
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // 2. 临时设置 FLAG_NOT_FOCUSABLE，阻止 Dialog 获焦时触发系统栏重算
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 子类绑定视图
        onBindPanelViews(view);

        // 蒙层点击关闭
        View dialogRoot = getDialogRootView();
        if (dialogRoot != null) {
            dialogRoot.setOnClickListener(v -> onOverlayClicked());
        }

        // 拦截面板区域点击事件（阻止穿透到蒙层）
        View panel = getCurrentPanelView();
        if (panel != null) {
            panel.setOnClickListener(v -> { /* consume click */ });
        }

        // 播放入场动画
        playEnterAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // 清除 FLAG_NOT_FOCUSABLE，恢复 Dialog 的触摸响应
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // cancel 所有动画防内存泄漏
        if (mOverlayAnimator != null) {
            mOverlayAnimator.cancel();
            mOverlayAnimator = null;
        }
        if (mPanelAnimator != null) {
            mPanelAnimator.cancel();
            mPanelAnimator = null;
        }
        if (isAdded()) {
            dismissAllowingStateLoss();
        }
    }

    // ==================== 方向检测 ====================

    /**
     * 判断当前是否为横屏。
     */
    protected boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // ==================== 动画 ====================

    private void playEnterAnimation() {
        long duration = getAnimationDuration();

        // 蒙层淡入
        View dialogRoot = getDialogRootView();
        if (dialogRoot != null) {
            dialogRoot.setAlpha(0f);
            mOverlayAnimator = dialogRoot.animate().alpha(1f).setDuration(duration);
            mOverlayAnimator.start();
        }

        // 面板滑入
        View panel = getCurrentPanelView();
        if (panel != null) {
            panel.setVisibility(View.VISIBLE);
            if (isLandscape()) {
                // 轴向隔离：横屏入场前清除竖屏残留的 translationY
                panel.setTranslationY(0);
                float width = panel.getWidth() > 0 ? panel.getWidth() : getFallbackPanelWidth();
                panel.setTranslationX(width);
                mPanelAnimator = panel.animate().translationX(0).setDuration(duration);
            } else {
                // 轴向隔离：竖屏入场前清除横屏残留的 translationX
                panel.setTranslationX(0);
                float height = panel.getHeight() > 0 ? panel.getHeight() : getFallbackPanelHeight();
                panel.setTranslationY(height);
                mPanelAnimator = panel.animate().translationY(0).setDuration(duration);
            }
            mPanelAnimator.start();
        }
    }

    private void playExitAnimation(@Nullable Runnable onEnd) {
        long duration = getAnimationDuration();

        // 蒙层淡出
        View dialogRoot = getDialogRootView();
        if (dialogRoot != null) {
            mOverlayAnimator = dialogRoot.animate().alpha(0f).setDuration(duration);
            mOverlayAnimator.start();
        }

        // 面板滑出
        View panel = getCurrentPanelView();
        if (panel != null) {
            if (isLandscape()) {
                float width = panel.getWidth() > 0 ? panel.getWidth() : getFallbackPanelWidth();
                mPanelAnimator = panel.animate().translationX(width)
                        .setDuration(duration).withEndAction(onEnd);
            } else {
                float height = panel.getHeight() > 0 ? panel.getHeight() : getFallbackPanelHeight();
                mPanelAnimator = panel.animate().translationY(height)
                        .setDuration(duration).withEndAction(onEnd);
            }
            mPanelAnimator.start();
        } else {
            if (onEnd != null) onEnd.run();
        }
    }

    // ==================== 回调接口 ====================

    public interface OnDismissListener {
        void onDismiss();
    }
}
