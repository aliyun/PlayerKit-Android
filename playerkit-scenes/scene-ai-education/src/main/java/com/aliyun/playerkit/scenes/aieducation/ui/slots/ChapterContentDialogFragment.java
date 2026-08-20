package com.aliyun.playerkit.scenes.aieducation.ui.slots;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.scenes.aieducation.R;
import com.aliyun.playerkit.scenes.aieducation.data.AiContentType;
import com.aliyun.playerkit.scenes.aieducation.data.AiContentViewModel;
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

    /**
     * Android 31 时 使用 Android 提供的模糊路径进行渲染
     */
    private static final int BLUR_RADIUS = 20;

    // 每个 TAB 关联一个 AiContentType，由类型决定该 TAB 的渲染样式。
    // 约定 tab0 -> mRvChapters，tab1 -> mRvAiAnalysis（仅命名习惯，实际 Adapter 由类型装配）。
    @AiContentType
    private int mTab0Type = AiContentType.CHAPTERS_ONLY;
    @AiContentType
    private int mTab1Type = AiContentType.GRAPHIC_ANALYSIS;

    private boolean mShowTabs = true;

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
    private AiContentViewModel mContent;
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
    public void setContent(@Nullable AiContentViewModel content) {
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
            mRvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (mRvAiAnalysis != null) {
            mRvAiAnalysis.setLayoutManager(new LinearLayoutManager(getContext()));
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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // API 31+ 启用原生窗口背景模糊，作为毛玻璃效果的增强
        if (isBlurBehindSupported()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.setBlurBehindRadius(BLUR_RADIUS);
                window.setAttributes(attributes);
            }
        }
        return dialog;
    }

    /**
     * 当前设备是否支持窗口背景模糊（API 31+）。
     */
    private boolean isBlurBehindSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
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

        // Tab 结构在 bindContent -> configureTabs 中根据数据动态构建，此处仅注册切换监听
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
        // tab0 承载于 mRvChapters，tab1 承载于 mRvAiAnalysis（Adapter 已按类型装配）。
        if (mRvChapters != null) {
            mRvChapters.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        }
        if (mRvAiAnalysis != null) {
            mRvAiAnalysis.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void bindContent() {
        if (mContent == null) return;

        // 1. 推导 tab 类型，以及是否显示 TAB
        deriveTabTypes();

        boolean hasChapters = mContent.hasChapterData();

        // 2. 面板标题：有章节显示"共 N 集"，否则显示 AI 解析
        if (mTvPanelTitle != null) {
            if (hasChapters) {
                List<ChapterInfo> chapters = mContent.getChapters();
                String title = getString(R.string.ai_education_chapter_count, chapters.size());
                mTvPanelTitle.setText(title);
            } else {
                mTvPanelTitle.setText(R.string.ai_education_ai_analysis);
            }
        }

        // 3. 按 tab 类型装配 Adapter：tab0 -> mRvChapters，tab1 -> mRvAiAnalysis
        applyAdapterForTab(mTab0Type, mRvChapters);
        if (mShowTabs) {
            applyAdapterForTab(mTab1Type, mRvAiAnalysis);
        }

        // 4. 配置 TAB 结构与可见性
        configureTabs();
    }

    /**
     * 推导 tab0/tab1 类型与是否显示 TAB。
     * <p>
     * <ul>
     *     <li>章节 + 摘要 → tab0=CHAPTERS_ONLY, tab1=GRAPHIC_ANALYSIS, 显示 TAB</li>
     *     <li>仅章节 → tab0=CHAPTERS_ONLY, tab1=TEXT_ANALYSIS, 显示 TAB</li>
     *     <li>仅摘要 → tab0=GRAPHIC_ANALYSIS, 不显示 TAB</li>
     * </ul>
     */
    private void deriveTabTypes() {
        boolean hasChapter = mContent != null && mContent.hasChapterData();
        boolean hasSummary = mContent != null && mContent.hasSummaryData();

        if (hasChapter && hasSummary) {
            mTab0Type = AiContentType.CHAPTERS_ONLY;
            mTab1Type = AiContentType.GRAPHIC_ANALYSIS;
            mShowTabs = true;
        } else if (hasChapter) {
            mTab0Type = AiContentType.CHAPTERS_ONLY;
            mTab1Type = AiContentType.TEXT_ANALYSIS;
            mShowTabs = true;
        } else {
            mTab0Type = AiContentType.GRAPHIC_ANALYSIS;
            mTab1Type = AiContentType.GRAPHIC_ANALYSIS; // unused
            mShowTabs = false;
        }
    }

    /**
     * 根据 AiContentType 为指定 RecyclerView 装配对应的 Adapter。
     *
     * @param type 该 TAB 的内容类型
     * @param rv   承载该 TAB 内容的列表
     */
    private void applyAdapterForTab(@AiContentType int type, @Nullable RecyclerView rv) {
        if (rv == null || mContent == null) return;
        switch (type) {
            case AiContentType.CHAPTERS_ONLY:
                mChapterAdapter = new ChapterListAdapter(mContent.getChapters(), chapter -> {
                    if (mChapterClickListener != null) {
                        mChapterClickListener.onChapterClick(chapter);
                    }
                });
                rv.setAdapter(mChapterAdapter);
                break;
            case AiContentType.TEXT_ANALYSIS:
                mSegmentAdapter = SegmentListAdapter.forTextAnalysis(mContent.getKnowledgeItems(), mMarkdownBinder, knowledge -> {
                    if (mSegmentClickListener != null) {
                        mSegmentClickListener.onSegmentClick(knowledge);
                    }
                });
                rv.setAdapter(mSegmentAdapter);
                break;
            case AiContentType.GRAPHIC_ANALYSIS:
                mSegmentAdapter = SegmentListAdapter.forGraphicAnalysis(mContent.getSummaryInfo(), mMarkdownBinder);
                rv.setAdapter(mSegmentAdapter);
                break;
            default:
                break;
        }
    }

    /**
     * 配置 TAB 结构。
     * <p>
     * TAB 规则（三种情况）：
     * <ul>
     *     <li>仅有章节（无 summary）→ 双 TAB："章节"(CHAPTERS_ONLY) + "AI解析"(TEXT_ANALYSIS)</li>
     *     <li>仅有摘要（无 chapters）→ 无 TAB，直接渲染 GRAPHIC_ANALYSIS</li>
     *     <li>两者都有 → 双 TAB："章节"(CHAPTERS_ONLY) + "图解"(GRAPHIC_ANALYSIS)</li>
     * </ul>
     */
    private void configureTabs() {
        if (mTabLayout == null) return;

        if (!mShowTabs) {
            mTabLayout.setVisibility(View.GONE);
            // 无 TAB 情形（仅 summary）：tab0 的 Adapter 始终装配在 mRvChapters，
            // 故与 bindContent 装配约定保持一致——显示 mRvChapters、隐藏 mRvAiAnalysis。
            switchTab(0);
            return;
        }

        mTabLayout.setVisibility(View.VISIBLE);
        mTabLayout.removeAllTabs();
        mTabLayout.addTab(mTabLayout.newTab().setText(titleForType(mTab0Type)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titleForType(mTab1Type)));
        switchTab(0); // 默认选 tab0
    }

    /**
     * 返回指定内容类型对应的 TAB 标题资源。
     */
    @StringRes
    private int titleForType(@AiContentType int type) {
        switch (type) {
            case AiContentType.CHAPTERS_ONLY:
                return R.string.ai_education_chapter;
            case AiContentType.TEXT_ANALYSIS:
                return R.string.ai_education_ai_analysis;
            case AiContentType.GRAPHIC_ANALYSIS:
                return R.string.ai_education_ai_analysis;
            default:
                return R.string.ai_education_ai_analysis;
        }
    }

    // ==================== Orientation ====================

    private void updatePanelLayoutForOrientation() {
        if (mPanelContainer == null) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mPanelContainer.getLayoutParams();
        // 所有版本统一使用毛玻璃模拟背景，API 31+ 额外启用原生模糊增强
        if (isLandscape()) {
            lp.width = DensityUtil.dip2px(requireContext(), PANEL_WIDTH_DP);
            lp.height = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.END;
            mPanelContainer.setBackgroundResource(R.drawable.bg_panel_round_all_glass);
        } else {
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            mPanelContainer.setBackgroundResource(R.drawable.bg_panel_round_top_glass);
        }
        mPanelContainer.setLayoutParams(lp);
    }
}
