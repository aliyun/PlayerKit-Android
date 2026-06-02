package com.aliyun.playerkit.ui.slots;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.FullscreenEvents;
import com.aliyun.playerkit.event.GestureEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.ContextUtil;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 顶部控制栏插槽
 * <p>
 * 提供播放器顶部控制栏功能，包括标题显示、返回按钮、设置按钮和截图按钮。
 * 作为控制栏的主控制器（Master Controller），负责处理单击手势来切换控制栏的显示/隐藏状态。
 * </p>
 * <p>
 * Top Control Bar Slot
 * <p>
 * Provides top control bar functionality for the player, including title display, back button, settings button, and snapshot button.
 * Acts as the master controller for the control bar, handling single tap gestures to toggle the control bar's show/hide state.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public class TopBarSlot extends BaseControlBarSlot {

    // ==================== UI 组件 ====================

    /**
     * 标题文本视图
     * <p>
     * 显示当前播放视频的标题。
     * </p>
     */
    private TextView mTvTitle;

    /**
     * 返回按钮
     * <p>
     * 点击后关闭当前 Activity。
     * </p>
     */
    private ImageView mIvBack;

    /**
     * 下载按钮
     * <p>
     * 点击后下载当前播放。
     * </p>
     */
    private ImageView mIvDownload;

    /**
     * 设置按钮
     * <p>
     * 点击后显示设置界面。
     * </p>
     */
    private ImageView mIvSettings;

    /**
     * 截图按钮
     * <p>
     * 点击后截取当前播放画面。
     * </p>
     */
    private ImageView mIvSnapshot;

    // ==================== 构造函数 ====================

    public TopBarSlot(@NonNull Context context) {
        super(context);
    }

    // ==================== 生命周期方法 ====================

    @Override
    protected int getLayoutId() {
        return isLandscape() ? R.layout.layout_landscape_top_bar_slot : R.layout.layout_portrait_top_bar_slot;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadLayout();
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void reloadLayout() {
        removeAllViews();
        int layoutId = isLandscape() ? R.layout.layout_landscape_top_bar_slot : R.layout.layout_portrait_top_bar_slot;
        View.inflate(getContext(), layoutId, this);
        bindViews();
        SlotHost host = getHost();
        if (host != null && host.getModel() != null) {
            onBindData(host.getModel());
        }
    }

    @Override
    protected void onRegisterElements() {
        registerElement(SlotElements.TopBar.BACK, mIvBack);
        registerElement(SlotElements.TopBar.TITLE, mTvTitle);
        registerElement(SlotElements.TopBar.SNAPSHOT, mIvSnapshot);
        // DOWNLOAD: 功能未实现，不注册到框架，保持 XML 中的 gone 状态
        // registerElement(SlotElements.TopBar.DOWNLOAD,mIvDownload)
        registerElement(SlotElements.TopBar.SETTINGS, mIvSettings);
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        bindViews();
    }

    private void bindViews() {
        mTvTitle = findViewByIdCompat(R.id.tv_top_bar_title);

        mIvBack = findViewByIdCompat(R.id.iv_back);
        mIvBack.setOnClickListener(v -> {
            handleBackEvent();
            notifyInteraction();
        });

        mIvSettings = findViewByIdCompat(R.id.iv_settings);
        mIvSettings.setOnClickListener(v -> {
            postEvent(new ControlBarEvents.ShowSettings(mPlayerId));
            notifyInteraction();
        });

        mIvSnapshot = findViewByIdCompat(R.id.iv_snapshot);
        mIvSnapshot.setOnClickListener(v -> {
            snapshot();
            notifyInteraction();
        });

        mIvDownload = findViewByIdCompat(R.id.iv_download);
        mIvDownload.setOnClickListener(v -> {
            // TODO: 添加下载功能
            notifyInteraction();
        });
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);

        // 设置视频标题
        String title = model.getVideoTitle();
        if (StringUtil.isNotEmpty(title)) {
            mTvTitle.setText(title);
        }
    }

    // ==================== 事件处理 ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Arrays.asList(
                GestureEvents.SingleTapEvent.class,
                ControlBarEvents.Show.class,
                ControlBarEvents.Hide.class,
                ControlBarEvents.ResetTimer.class,
                PlayerEvents.SnapshotCompleted.class
        );
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        super.onEvent(event);

        if (event instanceof GestureEvents.SingleTapEvent) {
            onSingleTap((GestureEvents.SingleTapEvent) event);
        }
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理单击手势事件
     * <p>
     * 作为控制栏的主控制器，负责切换控制栏的显示/隐藏状态。
     * 如果控制栏当前显示，则隐藏；如果隐藏，则显示。
     * </p>
     *
     * @param event 单击事件，不能为 null
     */
    private void onSingleTap(@NonNull GestureEvents.SingleTapEvent event) {
        if (isShow()) {
            postEvent(new ControlBarEvents.Hide(event.playerId));
        } else {
            postEvent(new ControlBarEvents.Show(event.playerId));
        }
    }

    /**
     * 处理返回事件
     * <p>
     * 当点击返回按钮时调用，根据当前播放器状态决定如何处理返回事件。
     * </p>
     */
    private void handleBackEvent() {
        // 如果处于全屏状态，退出全屏；否则关闭 Activity
        SlotHost slotHost = getHost();
        if (slotHost != null && slotHost.getPlayerStateStore().isFullscreen()) {
            postEvent(new FullscreenEvents.Toggle(mPlayerId));
        } else {
            Activity activity = ContextUtil.getActivity(getContext());
            if (activity != null) {
                activity.finish();
            }
        }
    }
}
