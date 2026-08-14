package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.BasePanelSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.ui.chapter.ChapterPanelListAdapter;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 章节面板插槽
 * <p>
 * 从右往左滑入的章节面板，展示所有章节列表，支持点击跳转。
 * 使用 translationX 动画 + 蒙层实现。
 * </p>
 * <p>
 * Chapter panel slot.
 * Slides in from right, displays all chapters with thumbnails.
 * </p>
 *
 * @author keria
 * @date 2026/07/13
 */
public class ChapterPanelSlot extends BasePanelSlot {

    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            ControlBarEvents.ShowChapterPanel.class,
            ControlBarEvents.Show.class,
            PlayerEvents.Info.class
    );

    @Nullable
    private String mPlayerId;
    @Nullable
    private List<ChapterInfo> mChapters;
    private int mCurrentChapterIndex = -1;

    private View mOverlay;
    private View mPanelContainer;
    private TextView mTvChapterCount;
    private RecyclerView mRvChapterList;
    private ChapterPanelListAdapter mAdapter;

    public ChapterPanelSlot(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_chapter_panel_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        mOverlay = findViewById(R.id.overlay);
        mPanelContainer = findViewById(R.id.panel_container);
        mTvChapterCount = findViewById(R.id.tv_chapter_count);
        mRvChapterList = findViewById(R.id.rv_chapter_list);
        ImageView ivClose = findViewById(R.id.iv_close);

        // 蒙层点击关闭
        if (mOverlay != null) {
            mOverlay.setOnClickListener(v -> hidePanel());
        }

        // 关闭按钮
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> hidePanel());
        }

        // 初始化列表
        mAdapter = new ChapterPanelListAdapter();
        if (mRvChapterList != null) {
            mRvChapterList.setLayoutManager(new LinearLayoutManager(getContext()));
            mRvChapterList.setHasFixedSize(true);
            mRvChapterList.setAdapter(mAdapter);
        }

        mAdapter.setOnChapterItemClickListener((position, chapter) -> {
            if (mPlayerId != null) {
                postEvent(new PlayerCommand.Seek(mPlayerId, chapter.getStartMs()));
            }
            hidePanel();
        });

        // 初始隐藏
        setVisibility(View.GONE);
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        mPlayerId = getPlayerId();
        mChapters = model.getChapters();
    }

    @Override
    public void onUnbindData() {
        mPlayerId = null;
        mChapters = null;
        mCurrentChapterIndex = -1;
        super.onUnbindData();
    }

    @Override
    public void onDetach() {
        mPlayerId = null;
        super.onDetach();
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (StringUtil.notEquals(mPlayerId, event.playerId)) {
            return;
        }

        if (event instanceof ControlBarEvents.ShowChapterPanel) {
            showChapterPanel();
        } else if (event instanceof ControlBarEvents.Show) {
            hidePanel();
        } else if (event instanceof PlayerEvents.Info) {
            updateCurrentChapter(((PlayerEvents.Info) event).currentPosition);
        }
    }

    private void showChapterPanel() {
        if (mChapters == null || mChapters.isEmpty()) return;

        // 更新头部
        if (mTvChapterCount != null) {
            Context context = getContext();
            if (context != null) {
                String text = context.getString(R.string.chapter_panel_title_format, mChapters.size());
                mTvChapterCount.setText(text);
            }
        }

        // 更新列表
        mAdapter.setChapters(mChapters);
        if (mCurrentChapterIndex >= 0) {
            mAdapter.updateCurrentIndex(mCurrentChapterIndex);
            // 滚动到当前章节
            if (mRvChapterList != null) {
                mRvChapterList.scrollToPosition(mCurrentChapterIndex);
            }
        }

        showPanel();
    }

    private void updateCurrentChapter(long positionMs) {
        if (mChapters == null || mChapters.isEmpty()) return;
        int newIndex = ChapterInfo.findIndexByPosition(mChapters, positionMs);
        if (newIndex != mCurrentChapterIndex) {
            mCurrentChapterIndex = newIndex;
            if (isShowing()) {
                mAdapter.updateCurrentIndex(mCurrentChapterIndex);
            }
        }
    }

    // ==================== BasePanelSlot ====================

    @Override
    protected void onPerformShowAnimation() {
        setVisibility(View.VISIBLE);
        bringToFront();

        if (mOverlay != null) {
            animateOverlayIn(mOverlay);
        }
        if (mPanelContainer != null) {
            float panelWidth = getContext().getResources().getDimension(R.dimen.chapter_panel_width);
            animateTranslationXIn(mPanelContainer, panelWidth, this::onPanelShown);
        } else {
            onPanelShown();
        }
    }

    @Override
    protected void onPerformHideAnimation() {
        if (mOverlay != null) {
            animateOverlayOut(mOverlay);
        }
        if (mPanelContainer != null) {
            float panelWidth = getContext().getResources().getDimension(R.dimen.chapter_panel_width);
            animateTranslationXOut(mPanelContainer, panelWidth, () -> {
                setVisibility(View.GONE);
                onPanelHidden();
            });
        } else {
            setVisibility(View.GONE);
            onPanelHidden();
        }
    }
}
