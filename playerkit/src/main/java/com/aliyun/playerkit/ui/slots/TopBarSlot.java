package com.aliyun.playerkit.ui.slots;

import android.app.Activity;
import android.content.Context;
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
        return R.layout.layout_top_bar_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 初始化 UI 组件
        mTvTitle = findViewByIdCompat(R.id.tv_title);

        // 设置返回按钮点击事件
        mIvBack = findViewByIdCompat(R.id.iv_back);
        mIvBack.setOnClickListener(v -> {
            // 处理返回事件
            handleBackEvent();
            notifyInteraction();
        });

        // 设置设置按钮点击事件
        mIvSettings = findViewByIdCompat(R.id.iv_settings);
        mIvSettings.setOnClickListener(v -> {
            postEvent(new ControlBarEvents.ShowSettings(mPlayerId));
            notifyInteraction();
        });

        // 设置截图按钮点击事件
        mIvSnapshot = findViewByIdCompat(R.id.iv_snapshot);
        mIvSnapshot.setOnClickListener(v -> {
            snapshot();
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
        } else if (event instanceof PlayerEvents.SnapshotCompleted) {
            onSnapshotCompleted((PlayerEvents.SnapshotCompleted) event);
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
     * 处理截图完成事件
     * <p>
     * 当截图操作完成时调用，显示截图结果提示。
     * </p>
     *
     * @param event 截图完成事件，不能为 null
     */
    private void onSnapshotCompleted(@NonNull PlayerEvents.SnapshotCompleted event) {
        if (event.result) {
            String toastText = "Snapshot Success: " + event.width + "x" + event.height + ", " + event.snapshotPath;
            ToastUtils.showToast(toastText);
        } else {
            ToastUtils.showToast("Snapshot Failed");
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
