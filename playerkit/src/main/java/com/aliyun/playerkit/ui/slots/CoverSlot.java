package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.StringUtil;
import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

/**
 * 封面图插槽
 * <p>
 * 负责显示视频封面图，覆盖在播放器 Surface 之上。
 * 当视频首帧渲染完成后，封面图会自动隐藏，显示视频内容。
 * </p>
 * <p>
 * Cover Image Slot
 * <p>
 * Responsible for displaying video cover image, overlaying on top of the player Surface.
 * When the first frame of the video is rendered, the cover image will automatically hide to show the video content.
 * </p>
 *
 * @author keria
 * @date 2025/12/08
 */
public class CoverSlot extends BaseSlot {

    private static final String TAG = "CoverSlot";

    // 封面图视图
    private ImageView mCoverImageView;

    public CoverSlot(@NonNull Context context) {
        super(context);
    }

    public CoverSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_cover_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 获取视图引用
        mCoverImageView = findViewById(R.id.cover_image);
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);

        // 检查封面图 URL
        String coverUrl = model.getCoverUrl();
        if (StringUtil.isNotEmpty(coverUrl)) {
            loadCover(coverUrl);
        } else {
            LogHub.i(TAG, "No cover url found in data, hide cover");
            gone();
        }
    }

    @Override
    public void onUnbindData() {
        super.onUnbindData();

        // 隐藏封面图
        // 注意：不需要手动调用 Glide.clear()，Glide 会在 ImageView detach 时自动清理资源
        // 手动调用可能导致 Activity 销毁时抛出 IllegalArgumentException
        gone();

        LogHub.i(TAG, "Data unbound, cover cleared");
    }

    /**
     * 加载封面图
     * <p>
     * 使用 Glide 库加载封面图 URL 并显示在 ImageView 中。
     * 加载前会先显示封面图视图，确保用户能看到封面图。
     * </p>
     *
     * @param url 封面图 URL
     */
    private void loadCover(String url) {
        LogHub.i(TAG, "Load cover", url);

        // 使用 Glide 加载
        Glide.with(this).load(url).into(mCoverImageView);
    }

    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Collections.singletonList(PlayerEvents.FirstFrameRendered.class);
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof PlayerEvents.FirstFrameRendered) {
            onFirstFrameRendered((PlayerEvents.FirstFrameRendered) event);
        }
    }

    /**
     * 处理首帧渲染完成事件
     * <p>
     * 当视频首帧已经渲染完成时调用，此时应该隐藏封面图，显示视频内容。
     * </p>
     * <p>
     * 这是封面图自动隐藏的触发机制，确保用户在视频开始播放时能看到视频内容而不是封面图。
     * </p>
     *
     * @param event 首帧渲染完成事件，不能为 null
     */
    private void onFirstFrameRendered(@NonNull PlayerEvents.FirstFrameRendered event) {
        // 首帧渲染后隐藏封面图，显示视频内容
        LogHub.i(TAG, "First frame rendered, hide cover");
        gone();
    }
}
