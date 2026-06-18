package com.aliyun.playerkit.ui.setting;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 设置菜单弹窗。
 * <p>
 * 从 slot 架构中解耦的设置菜单 UI，使用系统 DialogFragment 实现全屏弹窗。
 * 竖屏从底部滑入，横屏从右侧滑入。
 * </p>
 * <p>
 * 数据通过 {@link #setItems(List)} 在 {@code show()} 之前注入，不做序列化持久化。
 * 当系统重建（如屏幕旋转）导致 {@code mItems} 丢失时，在 {@link #onCreate(Bundle)} 中
 * 调用 {@code dismissAllowingStateLoss()} 安全关闭自身。
 * </p>
 *
 * <p>
 * Setting menu dialog.
 * <p>
 * A decoupled setting menu UI implemented with system DialogFragment as a fullscreen dialog.
 * Portrait mode slides in from the bottom, landscape mode slides in from the right.
 * </p>
 * <p>
 * Data is injected via {@link #setItems(List)} before {@code show()}, without serialization.
 * When the system recreates the fragment (e.g. rotation) and {@code mItems} is lost,
 * {@code dismissAllowingStateLoss()} is called in {@link #onCreate(Bundle)} to safely close itself.
 * </p>
 *
 * @author wyq
 */
public class SettingMenuDialogFragment extends DialogFragment {

    private static final String TAG = "SettingMenuDialog";

    /**
     * 入场/退场动画时长（毫秒）。
     * <p>
     * Enter/exit animation duration in milliseconds.
     * </p>
     */
    private static final long ANIM_DURATION_MS = 300L;

    /**
     * 横屏面板宽度的回退值（dp），在首帧 layout 尚未完成时用于动画位移计算。
     * <p>
     * Fallback panel width (dp) for landscape animation when the first layout pass is not yet complete.
     * </p>
     */
    private static final float FALLBACK_PANEL_WIDTH_DP = 345f;

    /**
     * 竖屏面板高度的回退值（dp），在首帧 layout 尚未完成时用于动画位移计算。
     * <p>
     * Fallback panel height (dp) for portrait animation when the first layout pass is not yet complete.
     * </p>
     */
    private static final float FALLBACK_PANEL_HEIGHT_DP = 400f;

    /**
     * 横屏模式下需要排除的设置项 key 列表。
     * <p>
     * 横屏时倍速和清晰度由独立的 Slot 控制，无需在设置菜单中重复展示。
     * </p>
     * <p>
     * Setting item keys excluded in landscape mode.
     * Speed and quality are controlled by dedicated Slots in landscape, so they are excluded here.
     * </p>
     */
    private static final List<String> LANDSCAPE_EXCLUDED_KEYS = Arrays.asList(
            SettingConstants.KEY_SPEED,
            SettingConstants.KEY_QUALITY
    );

    // -- Views --

    /** 竖屏设置面板容器 / Portrait setting panel container */
    private View mPortraitContainer;

    /** 横屏设置面板容器 / Landscape setting panel container */
    private View mLandscapeContainer;

    /** 竖屏列表适配器 / Portrait list adapter */
    private SettingItemPortraitAdapter mPortraitAdapter;

    /** 横屏列表适配器 / Landscape list adapter */
    private SettingItemLandscapeAdapter mLandscapeAdapter;

    // -- Data --

    /**
     * 全量设置项列表（竖屏使用）。
     * <p>
     * Full setting item list used in portrait mode.
     * </p>
     */
    private final List<SettingItem<?>> mItems = new ArrayList<>();

    /**
     * 横屏设置项列表（排除了 {@link #LANDSCAPE_EXCLUDED_KEYS} 中的项）。
     * <p>
     * Landscape setting item list with items in {@link #LANDSCAPE_EXCLUDED_KEYS} excluded.
     * </p>
     */
    private final List<SettingItem<?>> mLandscapeItems = new ArrayList<>();

    // -- State flags --

    /**
     * 退场动画/dismiss 是否已触发，防止重复执行。
     * <p>
     * Whether the exit animation or dismiss has been triggered, to prevent duplicate execution.
     * </p>
     */
    private boolean mDismissing;

    // ==================== 工厂方法 / Factory ====================

    /**
     * 创建新的设置菜单弹窗实例。
     * <p>
     * Create a new setting menu dialog instance.
     * </p>
     *
     * @return 新的 {@link SettingMenuDialogFragment} 实例 / New instance
     */
    public static SettingMenuDialogFragment newInstance() {
        return new SettingMenuDialogFragment();
    }

    // ==================== 公开方法 / Public API ====================

    /**
     * 注入设置项数据，必须在 {@code show()} 之前调用。
     * <p>
     * 内部会根据 {@link #LANDSCAPE_EXCLUDED_KEYS} 同步生成横屏列表。
     * </p>
     * <p>
     * Inject setting item data. Must be called before {@code show()}.
     * Internally generates the landscape list by filtering out {@link #LANDSCAPE_EXCLUDED_KEYS}.
     * </p>
     *
     * @param items 设置项列表 / Setting item list
     */
    public void setItems(@NonNull List<SettingItem<?>> items) {
        mItems.clear();
        mLandscapeItems.clear();
        mItems.addAll(items);
        for (SettingItem<?> item : items) {
            if (!LANDSCAPE_EXCLUDED_KEYS.contains(item.key)) {
                mLandscapeItems.add(item);
            }
        }
    }

    /**
     * 通知适配器数据已变更，刷新列表。
     * <p>
     * 由 {@link com.aliyun.playerkit.ui.slots.SettingMenuSlot} 在收到播放器状态事件后调用。
     * </p>
     * <p>
     * Notify adapters that data has changed to refresh the list.
     * Called by {@link com.aliyun.playerkit.ui.slots.SettingMenuSlot} upon receiving player state events.
     * </p>
     */
    public void notifyItemsChanged() {
        if (mPortraitAdapter != null) {
            mPortraitAdapter.notifyDataSetChanged();
        }
        if (mLandscapeAdapter != null) {
            mLandscapeAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 立即关闭弹窗，跳过退场动画。
     * <p>
     * 取消所有正在运行的动画，然后立即执行 {@code dismissAllowingStateLoss()}。
     * </p>
     * <p>
     * Dismiss the dialog immediately without exit animation.
     * Cancels all running animations before calling {@code dismissAllowingStateLoss()}.
     * </p>
     */
    public void dismissImmediately() {
        mDismissing = true;
        cancelRunningAnimations();
        dismissAllowingStateLoss();
    }

    // ==================== 生命周期 / Lifecycle ====================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PlayerKit_SettingMenuDialog);
        // mItems 通过 setItems() 注入，不做序列化。系统重建时 mItems 为空，直接关闭。
        if (savedInstanceState != null && mItems.isEmpty()) {
            dismissAllowingStateLoss();
            return;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart() {
        super.onStart();
        // 同步 Activity 窗口的系统 UI 状态，确保全屏/沉浸式模式不被打断
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;
        Window activityWindow = requireActivity().getWindow();
        Window dialogWindow = dialog.getWindow();
        dialogWindow.getDecorView().setSystemUiVisibility(
                activityWindow.getDecorView().getSystemUiVisibility()
        );
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialogWindow.setStatusBarColor(Color.TRANSPARENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_setting_menu_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPortraitContainer = view.findViewById(R.id.ll_portrait_container);
        mLandscapeContainer = view.findViewById(R.id.ll_landscape_container);

        // 点击遮罩区域关闭弹窗
        View root = view.findViewById(R.id.fl_dialog_root);
        if (root != null) {
            root.setOnClickListener(v -> dismissWithAnimation());
        }

        // 拦截面板区域点击事件，防止穿透到遮罩层触发关闭
        if (mPortraitContainer != null) {
            mPortraitContainer.setOnClickListener(v -> { /* consume click */ });
        }
        if (mLandscapeContainer != null) {
            mLandscapeContainer.setOnClickListener(v -> { /* consume click */ });
        }

        setupPortraitRecycler(view);
        setupLandscapeRecycler(view);
        updateOrientation();
        playEnterAnimation();
    }

    @Override
    public void onDestroyView() {
        mPortraitAdapter = null;
        mLandscapeAdapter = null;
        mPortraitContainer = null;
        mLandscapeContainer = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        for (SettingItem<?> item : mItems) {
            item.listener = null;
        }
        mItems.clear();
        mLandscapeItems.clear();
        super.onDestroy();
    }

    // ==================== RecyclerView 初始化 / RecyclerView Setup ====================

    /**
     * 初始化竖屏设置列表。
     * <p>
     * Setup portrait setting RecyclerView.
     * </p>
     */
    private void setupPortraitRecycler(@NonNull View view) {
        RecyclerView recycler = view.findViewById(R.id.rc_portrait_setting_recycler);
        if (recycler == null) return;
        mPortraitAdapter = new SettingItemPortraitAdapter(mItems, requireContext());
        recycler.setAdapter(mPortraitAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * 初始化横屏设置列表。
     * <p>
     * Setup landscape setting RecyclerView.
     * </p>
     */
    private void setupLandscapeRecycler(@NonNull View view) {
        RecyclerView recycler = view.findViewById(R.id.rc_landscape_setting_recycler);
        if (recycler == null) return;
        mLandscapeAdapter = new SettingItemLandscapeAdapter(mLandscapeItems, requireContext());
        recycler.setAdapter(mLandscapeAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    // ==================== 方向切换 / Orientation ====================

    /**
     * 判断当前是否为横屏模式。
     * <p>
     * Check whether the current orientation is landscape.
     * </p>
     *
     * @return {@code true} 为横屏 / {@code true} if landscape
     */
    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 根据当前屏幕方向切换竖屏/横屏面板的可见性。
     * <p>
     * Toggle visibility of portrait/landscape panels based on current orientation.
     * </p>
     */
    private void updateOrientation() {
        boolean landscape = isLandscape();
        if (mLandscapeContainer != null) {
            mLandscapeContainer.setVisibility(landscape ? View.VISIBLE : View.GONE);
        }
        if (mPortraitContainer != null) {
            mPortraitContainer.setVisibility(landscape ? View.GONE : View.VISIBLE);
        }
    }

    // ==================== 动画 / Animation ====================

    /**
     * 播放入场动画。
     * <p>
     * 遮罩层渐显，内容面板从屏幕边缘滑入（竖屏从底部，横屏从右侧）。
     * </p>
     * <p>
     * Play enter animation.
     * Overlay fades in, content panel slides in from screen edge (bottom in portrait, right in landscape).
     * </p>
     */
    private void playEnterAnimation() {
        View root = getView();
        if (root == null) return;

        View overlay = root.findViewById(R.id.fl_dialog_root);
        if (overlay != null) {
            overlay.setAlpha(0f);
            overlay.animate().alpha(1f).setDuration(ANIM_DURATION_MS).start();
        }

        View contentView = isLandscape() ? mLandscapeContainer : mPortraitContainer;
        if (contentView == null) return;

        if (isLandscape()) {
            float width = contentView.getWidth() > 0
                    ? contentView.getWidth()
                    : FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density;
            contentView.setTranslationX(width);
            contentView.animate()
                    .translationX(0f)
                    .setDuration(ANIM_DURATION_MS)
                    .start();
        } else {
            float height = contentView.getHeight() > 0
                    ? contentView.getHeight()
                    : FALLBACK_PANEL_HEIGHT_DP * getResources().getDisplayMetrics().density;
            contentView.setTranslationY(height);
            contentView.animate()
                    .translationY(0f)
                    .setDuration(ANIM_DURATION_MS)
                    .start();
        }
    }

    /**
     * 取消所有正在运行的动画。
     * <p>
     * Cancel all running animations on overlay and content panels.
     * </p>
     */
    private void cancelRunningAnimations() {
        View root = getView();
        if (root == null) return;
        View overlay = root.findViewById(R.id.fl_dialog_root);
        if (overlay != null) overlay.animate().cancel();
        if (mPortraitContainer != null) mPortraitContainer.animate().cancel();
        if (mLandscapeContainer != null) mLandscapeContainer.animate().cancel();
    }

    /**
     * 播放退场动画后关闭弹窗。
     * <p>
     * 遮罩层渐隐，内容面板滑出屏幕（竖屏向下，横屏向右），动画结束后执行 dismiss。
     * 使用 {@link #mDismissing} 标志防止重复触发。
     * </p>
     * <p>
     * Play exit animation then dismiss.
     * Overlay fades out, content panel slides out of screen (down in portrait, right in landscape).
     * Uses {@link #mDismissing} flag to prevent duplicate triggers.
     * </p>
     */
    private void dismissWithAnimation() {
        if (mDismissing) return;
        mDismissing = true;

        View root = getView();
        if (root == null) {
            dismissAllowingStateLoss();
            return;
        }

        View overlay = root.findViewById(R.id.fl_dialog_root);
        if (overlay != null) {
            overlay.animate().alpha(0f).setDuration(ANIM_DURATION_MS).start();
        }

        View contentView = isLandscape() ? mLandscapeContainer : mPortraitContainer;
        if (contentView == null) {
            dismissAllowingStateLoss();
            return;
        }

        if (isLandscape()) {
            float width = contentView.getWidth() > 0
                    ? contentView.getWidth()
                    : FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density;
            contentView.animate()
                    .translationX(width)
                    .setDuration(ANIM_DURATION_MS)
                    .withEndAction(this::safeDismiss)
                    .start();
        } else {
            float height = contentView.getHeight() > 0
                    ? contentView.getHeight()
                    : FALLBACK_PANEL_HEIGHT_DP * getResources().getDisplayMetrics().density;
            contentView.animate()
                    .translationY(height)
                    .setDuration(ANIM_DURATION_MS)
                    .withEndAction(this::safeDismiss)
                    .start();
        }
    }

    private void safeDismiss() {
        if (isAdded() && !isRemoving()) {
            dismissAllowingStateLoss();
        }
    }
}
