package com.aliyun.playerkit.scenes.aieducation.ui.slots;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.FullscreenEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.CustomSlotType;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.scenes.aieducation.R;
import com.aliyun.playerkit.scenes.aieducation.event.ChapterEvents;

import java.util.Arrays;
import java.util.List;

/**
 * 章节按钮插槽
 * <p>
 * 自定义插槽，在播放器右上角区域显示"章节"按钮。
 * 点击按钮通过事件总线通知 {@link ChapterContentPanelSlot} 展开/收起面板。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class ChapterButtonSlot extends BaseSlot {

    /**
     * 插槽类型定义
     * <p>
     * order=85：位于 TOP_BAR(80) 和 BOTTOM_BAR(90) 之间
     * </p>
     */
    public static final CustomSlotType TYPE = new CustomSlotType("chapter_button", 85);

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            ChapterEvents.ShowPanel.class,
            ChapterEvents.HidePanel.class,
            ControlBarEvents.Show.class,
            ControlBarEvents.Hide.class,
            FullscreenEvents.FullScreenChanged.class
    );

    @Nullable
    private View mBtnChapter;

    /**
     * 章节面板是否正在显示
     */
    private boolean mIsPanelShowing = false;

    /**
     * 当前是否处于全屏（横屏）状态
     */
    private boolean mIsFullscreen = false;

    /**
     * 章节内容是否可用，默认不可用
     */
    private boolean mChapterAvailable = false;

    /**
     * 控制栏当前是否可见，默认可见
     */
    private boolean mControlBarVisible = false;

    public ChapterButtonSlot(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_chapter_button_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        mBtnChapter = findViewByIdCompat(R.id.btn_chapter);
        if (mBtnChapter != null) {
            mBtnChapter.setOnClickListener(v -> onChapterButtonClicked());
        }
        // 初始按钮显隐：此时 mChapterAvailable=false，按钮隐藏
        updateButtonVisibility();
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof ChapterEvents.ShowPanel) {
            mIsPanelShowing = true;
            updateButtonState();
        } else if (event instanceof ChapterEvents.HidePanel) {
            mIsPanelShowing = false;
            updateButtonState();
        } else if (event instanceof FullscreenEvents.FullScreenChanged) {
            mIsFullscreen = ((FullscreenEvents.FullScreenChanged) event).isFullscreen;
            updateButtonVisibility();
        } else if (event instanceof ControlBarEvents.Show) {
            mControlBarVisible = true;
            updateButtonVisibility();
        } else if (event instanceof ControlBarEvents.Hide) {
            mControlBarVisible = false;
            updateButtonVisibility();
        }
    }

    private void onChapterButtonClicked() {
        SlotHost host = getHost();
        if (host == null) return;

        String playerId = host.getPlayerId();
        if (playerId == null) return;

        if (mIsPanelShowing) {
            postEvent(new ChapterEvents.HidePanel(playerId));
        } else {
            postEvent(new ChapterEvents.ShowPanel(playerId));
        }
    }

    private void updateButtonState() {
        if (mBtnChapter != null) {
            mBtnChapter.setSelected(mIsPanelShowing);
        }
    }

    /**
     * 更新章节内容可用状态
     * <p>
     * 仅当章节/摘要数据可用时按钮才有机会显示。
     * </p>
     *
     * @param available 章节内容是否可用
     */
    public void updateChapterAvailable(boolean available) {
        mChapterAvailable = available;
        updateButtonVisibility();
    }

    /**
     * 更新按钮显隐
     * <p>
     * 仅当章节内容可用（mChapterAvailable）、
     * 控制栏可见（mControlBarVisible）且非全屏（!mIsFullscreen）时才显示按钮。
     * </p>
     */
    private void updateButtonVisibility() {
        if (mBtnChapter != null) {
            boolean shouldShow = !mIsFullscreen && mControlBarVisible && mChapterAvailable;
            mBtnChapter.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDetach() {
        if (mBtnChapter != null) {
            mBtnChapter.setOnClickListener(null);
        }
        mBtnChapter = null;
        super.onDetach();
    }
}
