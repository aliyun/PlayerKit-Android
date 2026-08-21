package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.FormatUtil;
import com.aliyun.thumbnail.ThumbnailBitmapInfo;
import com.aliyun.thumbnail.ThumbnailHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Seek 缩略图插槽
 * <p>
 * 在用户拖动进度条（Seek）时显示缩略图浮层，包含视频缩略图和时间信息。
 * 通过监听 {@link ControlBarEvents.ShowSeekThumbnail}、{@link ControlBarEvents.UpdateSeekThumbnail}
 * 和 {@link ControlBarEvents.HideSeekThumbnail} 事件来控制显示和更新。
 * </p>
 * <p>
 * 自持 {@link ThumbnailHelper}，根据 seek 位置自行请求缩略图 Bitmap，
 * bitmap 生命周期完全本地化管理。
 * </p>
 * <p>
 * Seek Thumbnail Slot
 * <p>
 * Displays a thumbnail overlay when the user drags the progress bar (Seek), containing
 * video thumbnail and time information.
 * Controlled by listening to ShowSeekThumbnail, UpdateSeekThumbnail, and HideSeekThumbnail events.
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class SeekThumbnailSlot extends BaseSlot {

    private static final String TAG = "SeekThumbnailSlot";

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            ControlBarEvents.ShowSeekThumbnail.class,
            ControlBarEvents.UpdateSeekThumbnail.class,
            ControlBarEvents.HideSeekThumbnail.class,
            PlayerEvents.ThumbnailUrlReady.class
    );

    /**
     * 缩略图 ImageView
     */
    @Nullable
    private ImageView mIvThumbnail;
    /**
     * 时间 TextView
     */
    @Nullable
    private TextView mTvTime;

    /**
     * 缩略图辅助工具
     */
    @Nullable
    private ThumbnailHelper mThumbnailHelper;

    /**
     * ThumbnailHelper 是否准备就绪
     */
    private boolean mThumbnailReady = false;

    /**
     * 是否使用了外部 thumbnailUrl（优先级高于 vid 自动获取）
     */
    private boolean mExternalThumbnailUsed = false;

    public SeekThumbnailSlot(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_seek_thumbnail_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        mIvThumbnail = findViewByIdCompat(R.id.iv_seek_thumbnail);
        mTvTime = findViewByIdCompat(R.id.tv_seek_time);

        // 初始不可见
        setVisibility(View.GONE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        String thumbnailUrl = model.getThumbnailUrl();
        if (!TextUtils.isEmpty(thumbnailUrl)) {
            mExternalThumbnailUsed = true;
            initThumbnailHelper(thumbnailUrl);
        } else {
            mExternalThumbnailUsed = false;
        }
    }

    @Override
    public void onUnbindData() {
        mThumbnailHelper = null;
        mThumbnailReady = false;
        mExternalThumbnailUsed = false;
        if (mIvThumbnail != null) {
            mIvThumbnail.setImageDrawable(null);
        }
        if (mTvTime != null) {
            mTvTime.setText(null);
        }
        setVisibility(View.GONE);
        super.onUnbindData();
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        super.onEvent(event);
        if (event instanceof ControlBarEvents.ShowSeekThumbnail) {
            handleShowThumbnail();
        } else if (event instanceof ControlBarEvents.UpdateSeekThumbnail) {
            handleUpdateThumbnail((ControlBarEvents.UpdateSeekThumbnail) event);
        } else if (event instanceof ControlBarEvents.HideSeekThumbnail) {
            handleHideThumbnail();
        } else if (event instanceof PlayerEvents.ThumbnailUrlReady) {
            onThumbnailUrlReady((PlayerEvents.ThumbnailUrlReady) event);
        }
    }

    private void handleShowThumbnail() {
        LogHub.i(TAG, "handleShowThumbnail");
        setVisibility(View.VISIBLE);
    }

    private void handleUpdateThumbnail(@NonNull ControlBarEvents.UpdateSeekThumbnail event) {
        // 更新时间文本
        if (mTvTime != null) {
            String timeText = getContext().getString(R.string.seek_thumbnail_time_format,
                    FormatUtil.formatDuration(event.getPositionMs()),
                    FormatUtil.formatDuration(event.getDurationMs()));
            mTvTime.setText(timeText);
        }

        // 请求缩略图
        if (mThumbnailReady && mThumbnailHelper != null) {
            mThumbnailHelper.requestBitmapAtPosition(event.getPositionMs());
        }
    }

    private void handleHideThumbnail() {
        LogHub.i(TAG, "handleHideThumbnail");
        setVisibility(View.GONE);
    }

    /**
     * 处理 vid 缩略图 URL 就绪事件
     * <p>
     * 仅在未使用外部 thumbnailUrl 时生效，用于自动初始化缩略图能力。
     * </p>
     * <p>
     * Handle vid thumbnail URL ready event.
     * <p>
     * Only takes effect when no external thumbnailUrl is used, auto-initializes thumbnail capability.
     * </p>
     */
    private void onThumbnailUrlReady(@NonNull PlayerEvents.ThumbnailUrlReady event) {
        if (mExternalThumbnailUsed) return;
        if (mThumbnailHelper != null) return;
        // ThumbnailUrlReady 事件来自 AliPlayer 内部线程（PlayerEventBus 同步分发），
        // ThumbnailHelper 需要在主线程初始化，否则 prepare 回调无法投递
        if (mIvThumbnail != null) {
            final String url = event.thumbnailUrl;
            mIvThumbnail.post(() -> {
                if (mThumbnailHelper != null) return;
                initThumbnailHelper(url);
            });
        }
    }

    /**
     * 初始化缩略图帮助类
     * <p>
     * 通过 AliPlayer SDK 的 ThumbnailHelper 实现根据位置异步获取缩略图 Bitmap，
     * bitmap 生命周期完全在本插槽内管理。
     * </p>
     *
     * @param thumbnailUrl 缩略图 URL
     */
    private void initThumbnailHelper(@NonNull String thumbnailUrl) {
        if (mThumbnailHelper != null) return;
        LogHub.i(TAG, "initThumbnailHelper, thumbnailUrl: " + thumbnailUrl);
        mThumbnailReady = false;
        mThumbnailHelper = new ThumbnailHelper(thumbnailUrl);
        mThumbnailHelper.setOnPrepareListener(new ThumbnailHelper.OnPrepareListener() {
            @Override
            public void onPrepareSuccess() {
                LogHub.i(TAG, "Thumbnail prepare success");
                mThumbnailReady = true;
            }

            @Override
            public void onPrepareFail() {
                LogHub.w(TAG, "Thumbnail prepare fail");
                mThumbnailReady = false;
            }
        });
        mThumbnailHelper.setOnThumbnailGetListener(new ThumbnailHelper.OnThumbnailGetListener() {
            @Override
            public void onThumbnailGetSuccess(long positionMs, ThumbnailBitmapInfo info) {
                if (mIvThumbnail == null) return;
                Bitmap bitmap = (info != null) ? info.getThumbnailBitmap() : null;
                if (bitmap != null && !bitmap.isRecycled()) {
                    mIvThumbnail.post(() -> {
                        if (mIvThumbnail != null) {
                            mIvThumbnail.setImageBitmap(bitmap);
                        }
                    });
                }
            }

            @Override
            public void onThumbnailGetFail(long positionMs, String errorMsg) {
                // 获取失败不清空，保留上一帧有效缩略图
                LogHub.w(TAG, "Thumbnail get failed, positionMs: " + positionMs + ", errorMsg: " + errorMsg);
            }
        });
        mThumbnailHelper.prepare();
    }
}
