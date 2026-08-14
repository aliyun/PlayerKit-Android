package com.aliyun.playerkit.scenes.aieducation.ui.slots;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.scenes.aieducation.R;
import com.aliyun.playerkit.scenes.aieducation.data.AiContentType;
import com.aliyun.playerkit.scenes.aieducation.data.AiSummary;
import com.aliyun.playerkit.scenes.aieducation.data.AiVideoContent;
import com.aliyun.playerkit.scenes.aieducation.ui.MarkdownBinder;
import com.aliyun.playerkit.ui.dialog.BasePanelDialogFragment;
import com.aliyun.playerkit.utils.DensityUtil;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * 章节内容面板弹窗 Fragment。
 * <p>
 * 继承 {@link BasePanelDialogFragment}，复用全屏防护、入退场动画、蒙层等通用逻辑。
 * 面板从屏幕边界弹出（竖屏底部，横屏右侧）。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class ChapterContentDialogFragment extends BasePanelDialogFragment {

    /**
     * 横屏面板宽度（dp）
     */
    private static final float PANEL_WIDTH_DP = 390f;

    /**
     * 竖屏面板高度的回退值（dp），在首帧 layout 尚未完成时用于动画位移计算。
     */
    private static final float FALLBACK_PANEL_HEIGHT_DP = 600f;

    private static final int TAB_CHAPTERS = 0;
    private static final int TAB_AI_ANALYSIS = 1;

    // -- Views --
    private View mDialogRoot;
    private View mPanelContainer;
    @Nullable
    private TextView mTvPanelTitle;
    @Nullable
    private View mIvClose;
    @Nullable
    private TabLayout mTabLayout;
    @Nullable
    private RecyclerView mRvChapters;
    @Nullable
    private RecyclerView mRvAiAnalysis;

    // -- Adapters --
    @Nullable
    private ChapterListAdapter mChapterAdapter;
    @Nullable
    private SegmentListAdapter mSegmentAdapter;

    // -- Data (set before show) --
    @Nullable
    private AiVideoContent mContent;
    @Nullable
    private MarkdownBinder mMarkdownBinder;

    // -- Callbacks --
    @Nullable
    private ChapterListAdapter.OnChapterClickListener mChapterClickListener;
    @Nullable
    private SegmentListAdapter.OnSegmentClickListener mSegmentClickListener;

    public static ChapterContentDialogFragment newInstance() {
        return new ChapterContentDialogFragment();
    }

    // ==================== 配置 API（show 之前调用） ====================

    /**
     * 设置数据内容。必须在 show() 之前调用。
     */
    public void setContent(@Nullable AiVideoContent content) {
        mContent = content;
    }

    /**
     * 设置 Markdown 渲染器。必须在 show() 之前调用。
     */
    public void setMarkdownBinder(@Nullable MarkdownBinder binder) {
        mMarkdownBinder = binder;
    }

    public void setOnChapterClickListener(@Nullable ChapterListAdapter.OnChapterClickListener listener) {
        mChapterClickListener = listener;
    }

    public void setOnSegmentClickListener(@Nullable SegmentListAdapter.OnSegmentClickListener listener) {
        mSegmentClickListener = listener;
    }

    // ==================== BasePanelDialogFragment 抽象方法实现 ====================

    @Override
    protected int getLayoutResId() {
        return R.layout.layout_chapter_content_dialog;
    }

    @Nullable
    @Override
    protected View getDialogRootView() {
        return mDialogRoot;
    }

    @Nullable
    @Override
    protected View getCurrentPanelView() {
        return mPanelContainer;
    }

    @Override
    protected void onBindPanelViews(@NonNull View root) {
        mDialogRoot = root.findViewById(R.id.fl_dialog_root);
        mPanelContainer = root.findViewById(R.id.panel_container);
        mTvPanelTitle = root.findViewById(R.id.tv_panel_title);
        mIvClose = root.findViewById(R.id.iv_panel_close);
        mTabLayout = root.findViewById(R.id.tab_layout);
        mRvChapters = root.findViewById(R.id.rv_chapters);
        mRvAiAnalysis = root.findViewById(R.id.rv_ai_analysis);

        // Close button
        if (mIvClose != null) {
            mIvClose.setOnClickListener(v -> dismissWithAnimation());
        }

        // Setup RecyclerViews
        if (mRvChapters != null) {
            mRvChapters.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
        if (mRvAiAnalysis != null) {
            mRvAiAnalysis.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        // Setup tabs
        setupTabs();

        // Bind data
        bindContent();

        // 设置方向布局
        updatePanelLayoutForOrientation();
    }

    // ==================== Hook 覆写 ====================

    @Override
    protected float getFallbackPanelHeight() {
        return DensityUtil.dip2px(requireContext(), FALLBACK_PANEL_HEIGHT_DP);
    }

    @Override
    protected float getFallbackPanelWidth() {
        return DensityUtil.dip2px(requireContext(), PANEL_WIDTH_DP);
    }

    @Override
    protected void onOrientationChanged(boolean isLandscape) {
        updatePanelLayoutForOrientation();
    }

    // ==================== 公开 API ====================

    /**
     * 更新章节列表的高亮索引。
     * <p>
     * Update chapter list highlight index.
     * </p>
     *
     * @param oldIndex 之前的高亮索引，-1 表示无高亮
     * @param newIndex 新的高亮索引，-1 表示无高亮
     */
    public void updateChapterHighlight(int oldIndex, int newIndex) {
        if (mChapterAdapter != null) {
            mChapterAdapter.updateCurrentIndex(oldIndex, newIndex);
        }
    }

    /**
     * 更新分段列表的高亮索引。
     * <p>
     * Update segment list highlight index.
     * </p>
     *
     * @param oldIndex 之前的高亮索引，-1 表示无高亮
     * @param newIndex 新的高亮索引，-1 表示无高亮
     */
    public void updateSegmentHighlight(int oldIndex, int newIndex) {
        if (mSegmentAdapter != null) {
            mSegmentAdapter.updateCurrentSegmentIndex(oldIndex, newIndex);
        }
    }

    // ==================== 内部逻辑 ====================

    private void setupTabs() {
        if (mTabLayout == null) return;

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.ai_education_chapter));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.ai_education_ai_analysis));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void switchTab(int tabIndex) {
        if (mRvChapters != null) {
            mRvChapters.setVisibility(tabIndex == TAB_CHAPTERS ? View.VISIBLE : View.GONE);
        }
        if (mRvAiAnalysis != null) {
            mRvAiAnalysis.setVisibility(tabIndex == TAB_AI_ANALYSIS ? View.VISIBLE : View.GONE);
        }
    }

    private void bindContent() {
        if (mContent == null) return;

        int type = mContent.getType();
        List<ChapterInfo> chapters = mContent.getChapters();
        boolean hasChapters = chapters != null && !chapters.isEmpty();
        boolean hasAiAnalysis = (type == AiContentType.TEXT_ANALYSIS || type == AiContentType.GRAPHIC_ANALYSIS);

        // Update panel title
        if (mTvPanelTitle != null) {
            if (hasChapters) {
                String title = requireContext().getString(
                        R.string.ai_education_chapter_count, chapters.size());
                mTvPanelTitle.setText(title);
            } else {
                mTvPanelTitle.setText(R.string.ai_education_ai_analysis);
            }
        }

        // Setup chapter list
        if (hasChapters) {
            mChapterAdapter = new ChapterListAdapter(chapters, chapter -> {
                if (mChapterClickListener != null) {
                    mChapterClickListener.onChapterClick(chapter);
                }
            });
            if (mRvChapters != null) {
                mRvChapters.setAdapter(mChapterAdapter);
            }
        }

        // Setup AI analysis RecyclerView (summary header + segments)
        if (hasAiAnalysis) {
            AiSummary summary = mContent.getSummary();
            mSegmentAdapter = new SegmentListAdapter(summary, mContent.getSegments(), type,
                    mMarkdownBinder, segment -> {
                if (mSegmentClickListener != null) {
                    mSegmentClickListener.onSegmentClick(segment);
                }
            });
            if (mRvAiAnalysis != null) {
                mRvAiAnalysis.setAdapter(mSegmentAdapter);
            }
        }

        // Tab visibility logic
        configureTabVisibility(type, hasChapters, hasAiAnalysis);
    }

    private void configureTabVisibility(@AiContentType int type, boolean hasChapters, boolean hasAiAnalysis) {
        if (mTabLayout == null) return;

        if (type == AiContentType.CHAPTERS_ONLY) {
            mTabLayout.setVisibility(View.GONE);
            showOnlyChapters();
        } else if (hasAiAnalysis && !hasChapters) {
            mTabLayout.setVisibility(View.GONE);
            showOnlyAiAnalysis();
        } else {
            mTabLayout.setVisibility(View.VISIBLE);
            switchTab(TAB_CHAPTERS);
        }
    }

    private void showOnlyChapters() {
        if (mRvChapters != null) {
            mRvChapters.setVisibility(View.VISIBLE);
        }
        if (mRvAiAnalysis != null) {
            mRvAiAnalysis.setVisibility(View.GONE);
        }
    }

    private void showOnlyAiAnalysis() {
        if (mRvChapters != null) {
            mRvChapters.setVisibility(View.GONE);
        }
        if (mRvAiAnalysis != null) {
            mRvAiAnalysis.setVisibility(View.VISIBLE);
        }
    }

    // ==================== Orientation ====================

    private void updatePanelLayoutForOrientation() {
        if (mPanelContainer == null) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mPanelContainer.getLayoutParams();
        if (isLandscape()) {
            lp.width = DensityUtil.dip2px(requireContext(), PANEL_WIDTH_DP);
            lp.height = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.END;
            mPanelContainer.setBackgroundResource(R.drawable.bg_panel_round_all);
        } else {
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            mPanelContainer.setBackgroundResource(R.drawable.bg_panel_round_top);
        }
        mPanelContainer.setLayoutParams(lp);
    }
}
