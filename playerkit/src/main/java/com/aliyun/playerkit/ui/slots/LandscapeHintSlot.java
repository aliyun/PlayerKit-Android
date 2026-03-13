package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.VideoSize;
import com.aliyun.playerkit.event.FullscreenEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.player.IMediaPlayer;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.DensityUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 横屏观看提示插槽
 * <p>
 * 当检测到视频为横屏（宽 > 高）且当前显示区域为竖屏（高 > 宽）时，
 * 在视频内容的下方固定位置显示提示按钮，点击可切换全屏。
 * 按钮位置会根据视频渲染区域动态计算，确保紧贴视频内容下方。
 * </p>
 * <p>
 * Landscape Hint Slot
 * <p>
 * Shows a hint button at the bottom when a landscape video (width > height)
 * is detected in a portrait display area (height > width).
 * The button position is dynamically calculated based on the video rendering area
 * to ensure it is placed just below the video content.
 * </p>
 *
 * @author keria
 * @date 2026/01/12
 */
public class LandscapeHintSlot extends BaseSlot {

    // 提示按钮与视频内容之间的间距
    private static final int HINT_MARGIN_DP = 20;

    // 播放器 ID（在 onBindData 时存储，避免频繁调用 getPlayerId()）
    @Nullable
    private String mPlayerId;

    private View mHintView;
    private int mVideoWidth;
    private int mVideoHeight;

    public LandscapeHintSlot(@NonNull Context context) {
        super(context);
    }

    public LandscapeHintSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LandscapeHintSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_landscape_hint_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        mHintView = findViewById(R.id.ll_landscape_hint);
        if (mHintView != null) {
            mHintView.setOnClickListener(v -> {
                // 点击切换全屏
                postEvent(new FullscreenEvents.Toggle(mPlayerId));
            });
        }
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);

        mPlayerId = getPlayerId();
    }

    @Override
    public void onUnbindData() {
        mPlayerId = null;
        mVideoWidth = 0;
        mVideoHeight = 0;

        super.onUnbindData();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 布局大小变化时重新计算位置和显示状态
        requestUpdate();
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Arrays.asList(
                PlayerEvents.VideoSizeChanged.class,
                PlayerEvents.Prepared.class,
                PlayerEvents.StateChanged.class,
                PlayerEvents.SetScaleTypeCompleted.class
        );
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        super.onEvent(event);
        if (event instanceof PlayerEvents.VideoSizeChanged) {
            PlayerEvents.VideoSizeChanged videoSizeChanged = (PlayerEvents.VideoSizeChanged) event;
            mVideoWidth = videoSizeChanged.width;
            mVideoHeight = videoSizeChanged.height;
            requestUpdate();
        } else if (event instanceof PlayerEvents.Prepared) {
            requestUpdate();
        } else if (event instanceof PlayerEvents.StateChanged) {
            requestUpdate();
        } else if (event instanceof PlayerEvents.SetScaleTypeCompleted) {
            requestUpdate();
        }
    }

    /**
     * 检查显示条件并更新提示按钮位置
     */
    private void requestUpdate() {
        if (mHintView == null) return;

        SlotHost host = getHost();
        if (host == null) return;
        IPlayerStateStore stateStore = host.getPlayerStateStore();

        // 1. 获取视频原始宽高
        VideoSize videoSize = stateStore.getVideoSize();
        int videoWidth = mVideoWidth > 0 ? mVideoWidth : (videoSize != null ? videoSize.getWidth() : 0);
        int videoHeight = mVideoHeight > 0 ? mVideoHeight : (videoSize != null ? videoSize.getHeight() : 0);
        int rotation = stateStore.getCurrentRotation();

        // 根据旋转角度调整宽高
        // 90度和270度时，宽高交换
        int displayWidth = videoWidth;
        int displayHeight = videoHeight;
        if (rotation == IMediaPlayer.Rotation.DEGREE_90 || rotation == IMediaPlayer.Rotation.DEGREE_270) {
            displayWidth = videoHeight;
            displayHeight = videoWidth;
        }

        if (displayWidth <= 0 || displayHeight <= 0) {
            hide();
            return;
        }

        // 2. 检查视频显示时是否为横屏（宽 > 高）
        boolean isVideoLandscape = displayWidth > displayHeight;

        // 3. 检查显示区域是否为竖屏
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) return;

        boolean isDisplayPortrait = viewHeight > viewWidth;

        // 4.1 检查渲染填充模式
        // 只有在 FIT_CENTER 模式下（会有黑边），才显示提示按钮
        // 其它模式（如 FIT_XY, CENTER_CROP）通常会填满屏幕，此时不显示按钮
        int scaleType = stateStore.getCurrentScaleType();
        boolean isFitCenter = scaleType == IMediaPlayer.ScaleType.FIT_CENTER;

        // 4.2 显示条件判断
        if (isVideoLandscape && isDisplayPortrait && isFitCenter) {
            // 计算位置
            updateHintPosition(viewWidth, viewHeight, displayWidth, displayHeight);
            show();
        } else {
            hide();
        }
    }

    /**
     * 更新提示按钮位置
     * <p>
     * 仅在 FIT_CENTER 模式下调用
     * </p>
     *
     * @param videoWidth  已处理旋转后的视频显示宽度
     * @param videoHeight 已处理旋转后的视频显示高度
     */
    private void updateHintPosition(int viewWidth, int viewHeight, int videoWidth, int videoHeight) {
        float videoRatio = (float) videoWidth / videoHeight;

        // FIT_CENTER: 等比完整显示
        // 横屏视频(W>H)在竖屏视图(H>W)中，一定是宽度撑满
        // renderedHeight 为实际渲染出的视频画面高度
        float renderedHeight = viewWidth / videoRatio;

        // 视频内容底部的 Y 坐标 (View坐标系)
        // 居中显示，top offset = (viewHeight - renderedHeight) / 2
        float topOffset = (viewHeight - renderedHeight) / 2;
        float videoBottomY = topOffset + renderedHeight;

        // 按钮高度
        int buttonHeight = mHintView.getHeight();
        if (buttonHeight == 0) {
            mHintView.measure(View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.AT_MOST));
            buttonHeight = mHintView.getMeasuredHeight();
        }

        // 设置 Margin
        int margin = DensityUtil.dip2px(getContext(), HINT_MARGIN_DP);

        // 目标 Y 坐标：视频底部 + Margin
        int targetTopMargin = (int) (videoBottomY + margin);

        // 边界限制：不能超出 View 底部太大，或者保证至少露出一点？
        // 逻辑：如果黑边足够大，按钮完全在黑边里。
        // 如果黑边很小，或者计算误差，导致按钮超出了 viewHeight，则限制在底部。
        int maxTopMargin = viewHeight - buttonHeight - margin;

        if (targetTopMargin > maxTopMargin) {
            targetTopMargin = maxTopMargin;
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mHintView.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.topMargin = targetTopMargin;
        params.bottomMargin = 0; // Prevent conflict

        mHintView.setLayoutParams(params);
    }
}
