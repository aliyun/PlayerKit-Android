package com.aliyun.playerkit.ui.setting;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.ui.dialog.BasePanelDialogFragment;
import com.aliyun.playerkit.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置菜单弹窗 Fragment。
 * <p>
 * 拥有独立 Window，面板从屏幕边界弹出（竖屏底部，横屏右侧），
 * 解决了 SlotHostLayout 仅占播放器区域时面板弹出位置受限的问题。
 * </p>
 *
 * @author wyq
 */
public class SettingMenuDialogFragment extends BasePanelDialogFragment {

    /**
     * 横屏面板宽度的回退值（dp），在首帧 layout 尚未完成时用于动画位移计算。
     */
    private static final float FALLBACK_PANEL_WIDTH_DP = 345f;

    /**
     * 竖屏面板高度的回退值（dp），在首帧 layout 尚未完成时用于动画位移计算。
     */
    private static final float FALLBACK_PANEL_HEIGHT_DP = 400f;

    // -- Views --
    private View mDialogRoot;
    private View mPortraitContainer;
    private View mLandscapeContainer;
    private SettingItemPortraitAdapter mPortraitAdapter;
    private SettingItemLandscapeAdapter mLandscapeAdapter;

    // -- Data --
    private final List<SettingItem<?>> mItems = new ArrayList<>();
    private final List<SettingItem<?>> mLandscapeItems = new ArrayList<>();

    public static SettingMenuDialogFragment newInstance() {
        return new SettingMenuDialogFragment();
    }

    /**
     * 设置数据项。必须在 show() 之前调用。
     *
     * @param items          全量设置项列表
     * @param landscapeItems 横屏设置项列表（已排除横屏不需要的项）
     */
    public void setItems(@NonNull List<SettingItem<?>> items, @NonNull List<SettingItem<?>> landscapeItems) {
        mItems.clear();
        mItems.addAll(items);
        mLandscapeItems.clear();
        mLandscapeItems.addAll(landscapeItems);
    }

    // ==================== 公开 API ====================

    /**
     * 通知 Adapter 刷新数据。
     */
    public void notifyDataChanged() {
        if (mPortraitAdapter != null) {
            mPortraitAdapter.notifyDataSetChanged();
        }
        if (mLandscapeAdapter != null) {
            mLandscapeAdapter.notifyDataSetChanged();
        }
    }

    // ==================== BasePanelDialogFragment 抽象方法实现 ====================

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_setting_menu_dialog;
    }

    @Nullable
    @Override
    protected View getDialogRootView() {
        return mDialogRoot;
    }

    @Nullable
    @Override
    protected View getCurrentPanelView() {
        return isLandscape() ? mLandscapeContainer : mPortraitContainer;
    }

    @Override
    protected void onBindPanelViews(@NonNull View root) {
        mDialogRoot = root.findViewById(R.id.fl_dialog_root);
        mPortraitContainer = root.findViewById(R.id.ll_portrait_container);
        mLandscapeContainer = root.findViewById(R.id.ll_landscape_container);

        // 拦截面板区域点击事件（阻止穿透到蒙层）
        if (mPortraitContainer != null) {
            mPortraitContainer.setOnClickListener(v -> { /* consume click */ });
        }
        if (mLandscapeContainer != null) {
            mLandscapeContainer.setOnClickListener(v -> { /* consume click */ });
        }

        // 初始化 RecyclerView
        setupPortraitRecycler(root);
        setupLandscapeRecycler(root);

        // 设置方向
        updateOrientation();
    }

    // ==================== BasePanelDialogFragment 可覆写 Hook ====================

    @Override
    protected float getFallbackPanelHeight() {
        return DensityUtil.dip2px(requireContext(), FALLBACK_PANEL_HEIGHT_DP);
    }

    @Override
    protected float getFallbackPanelWidth() {
        return DensityUtil.dip2px(requireContext(), FALLBACK_PANEL_WIDTH_DP);
    }

    // ==================== 内部逻辑 ====================

    private void setupPortraitRecycler(@NonNull View root) {
        RecyclerView recycler = root.findViewById(R.id.rc_portrait_setting_recycler);
        if (recycler == null) return;
        mPortraitAdapter = new SettingItemPortraitAdapter(mItems, requireContext());
        recycler.setAdapter(mPortraitAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupLandscapeRecycler(@NonNull View root) {
        RecyclerView recycler = root.findViewById(R.id.rc_landscape_setting_recycler);
        if (recycler == null) return;
        mLandscapeAdapter = new SettingItemLandscapeAdapter(mLandscapeItems, requireContext());
        recycler.setAdapter(mLandscapeAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void updateOrientation() {
        boolean landscape = isLandscape();
        if (mLandscapeContainer != null) {
            mLandscapeContainer.setVisibility(landscape ? View.VISIBLE : View.GONE);
        }
        if (mPortraitContainer != null) {
            mPortraitContainer.setVisibility(landscape ? View.GONE : View.VISIBLE);
        }
    }
}
