package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.os.Looper;
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
import java.util.Collections;
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
            PlayerEvents.Info.class,
            PlayerEvents.ChapterListUpdated.class
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
        mChapters = readChaptersFromChannel();
        mCurrentChapterIndex = -1;
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
        } else if (event instanceof PlayerEvents.ChapterListUpdated) {
            onChaptersUpdated();
        }
    }

    /**
     * 从章节数据通道读取章节
     *
     * @return 章节列表（无数据时为空列表）
     */
    @NonNull
    private List<ChapterInfo> readChaptersFromChannel() {
        SlotHost host = getHost();
        if (host == null) return Collections.emptyList();
        try {
            return host.getPlayerStateStore().getChapters();
        } catch (IllegalStateException e) {
            // 播放器尚未就绪，视为无章节
            return Collections.emptyList();
        }
    }

    /**
     * 处理章节数据变更
     * <p>
     * 事件总线为同步派发，业务可能在工作线程写入章节，统一切回主线程刷新 UI。
     * 刷新时重新读取数据通道而非使用事件载荷：排队中的旧载荷可能晚于主线程上的新写入执行，
     * 覆盖最新的章节数据。
     * </p>
     */
    private void onChaptersUpdated() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            applyChaptersFromChannel();
        } else {
            post(this::applyChaptersFromChannel);
        }
    }

    /**
     * 按数据通道最新章节刷新面板状态
     * <p>
     * 面板为「打开时快照」，但展示期间收到的数据变化必须同步刷新列表快照：
     * 否则随后的 {@link PlayerEvents.Info} 会用新列表算出的索引去高亮旧快照的行，
     * 两份数据条目数/顺序不同时高亮会落到错误章节。
     * </p>
     */
    private void applyChaptersFromChannel() {
        List<ChapterInfo> latest = readChaptersFromChannel();
        mChapters = latest.isEmpty() ? null : latest;

        // 数据变空：收起面板，避免残留可点击但无意义的入口
        if (mChapters == null) {
            mCurrentChapterIndex = -1;
            if (isShowing()) {
                hidePanel();
            }
            return;
        }

        if (isShowing()) {
            renderChapterListSnapshot();
        } else {
            // 未展示时仅记录最新数据与索引，下次打开时由 showChapterPanel 重算快照
            mCurrentChapterIndex = ChapterInfo.findIndexByPosition(mChapters, getCurrentPositionMs());
        }
    }

    /**
     * 按当前章节数据重建列表快照与高亮索引
     * <p>
     * 面板头部计数、列表数据和高亮索引必须同源刷新。
     * </p>
     */
    private void renderChapterListSnapshot() {
        if (mAdapter == null) return;
        updateChapterCountHeader();
        mCurrentChapterIndex = ChapterInfo.findIndexByPosition(mChapters, getCurrentPositionMs());
        mAdapter.setChapters(mChapters);
        if (mCurrentChapterIndex >= 0) {
            mAdapter.updateCurrentIndex(mCurrentChapterIndex);
            // 滚动到当前章节
            if (mRvChapterList != null) {
                mRvChapterList.scrollToPosition(mCurrentChapterIndex);
            }
        }
    }

    /**
     * 更新面板头部的章节数量
     */
    private void updateChapterCountHeader() {
        if (mTvChapterCount == null || mChapters == null) return;
        Context context = getContext();
        if (context == null) return;
        mTvChapterCount.setText(context.getString(R.string.chapter_panel_title_format, mChapters.size()));
    }

    /**
     * 从状态存储读取当前播放位置
     *
     * @return 当前播放位置（毫秒），不可用时返回 0
     */
    private long getCurrentPositionMs() {
        SlotHost host = getHost();
        if (host == null) return 0;
        try {
            return host.getPlayerStateStore().getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    private void showChapterPanel() {
        if (mChapters == null || mChapters.isEmpty()) return;

        // 每次打开都按最新章节数据与实时播放位置重算快照（头部计数、列表数据、高亮索引）
        renderChapterListSnapshot();

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
