package com.aliyun.playerkit.scenes.aieducation.ui.slots;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.scenes.aieducation.R;
import com.aliyun.playerkit.scenes.aieducation.data.KnowledgeInfo;
import com.aliyun.playerkit.scenes.aieducation.data.SummaryInfo;
import com.aliyun.playerkit.scenes.aieducation.ui.MarkdownBinder;
import com.aliyun.playerkit.utils.DensityUtil;
import com.aliyun.playerkit.utils.FormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 解析列表适配器（多模式 / 多 ViewType）。
 * <p>
 * 直接消费新数据模型，支持两种展示模式：
 * <ul>
 *     <li>{@link #MODE_TEXT}（文字分析）：数据源 {@code List<KnowledgeInfo>}，
 *     每项展示标题 + 时间 +（可选）Markdown 内容；无 Markdown 时仅显示标题。</li>
 *     <li>{@link #MODE_GRAPHIC}（图解分析）：数据源 {@link SummaryInfo}，
 *     自上而下展示段落摘要（含思维导图徽章标签）+ Markdown 内容。</li>
 * </ul>
 * 通过静态工厂方法 {@link #forTextAnalysis} / {@link #forGraphicAnalysis} 创建，
 * 复用现有 item 布局（item_summary_section / item_ai_summary_header）。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class SegmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 文字分析模式：渲染知识点列表
     */
    private static final int MODE_TEXT = 0;
    /**
     * 图解分析模式：渲染摘要（段落 + 徽章 + Markdown）
     */
    private static final int MODE_GRAPHIC = 1;

    /**
     * ViewType：知识点条目（标题 + 时间 + Markdown）
     */
    private static final int TYPE_KNOWLEDGE = 0;
    /**
     * ViewType：图解头部（段落摘要 + 思维导图徽章）
     */
    private static final int TYPE_GRAPHIC_HEADER = 1;
    /**
     * ViewType：图解 Markdown 正文
     */
    private static final int TYPE_GRAPHIC_MARKDOWN = 2;

    private final int mMode;

    /**
     * 文字分析模式的数据源
     */
    @NonNull
    private final List<KnowledgeInfo> mKnowledgeItems;

    /**
     * 图解分析模式的数据源
     */
    @Nullable
    private final SummaryInfo mSummaryInfo;

    /**
     * 图解模式下按顺序排列的行 ViewType（构造时确定）
     */
    @NonNull
    private final List<Integer> mGraphicRows = new ArrayList<>(2);

    @Nullable
    private final MarkdownBinder mMarkdownBinder;
    @Nullable
    private final OnSegmentClickListener mListener;

    private int mCurrentSegmentIndex = -1;

    /**
     * 创建文字分析（TextAnalysis）适配器。
     *
     * @param items          知识点列表
     * @param markdownBinder Markdown 渲染器
     * @param listener       条目时间点击回调（跳转播放位置）
     */
    @NonNull
    public static SegmentListAdapter forTextAnalysis(@NonNull List<KnowledgeInfo> items,
                                                     @Nullable MarkdownBinder markdownBinder,
                                                     @Nullable OnSegmentClickListener listener) {
        return new SegmentListAdapter(MODE_TEXT, items, null, markdownBinder, listener);
    }

    /**
     * 创建图解分析（GraphicAnalysis）适配器。
     *
     * @param summaryInfo    摘要数据
     * @param markdownBinder Markdown 渲染器
     */
    @NonNull
    public static SegmentListAdapter forGraphicAnalysis(@Nullable SummaryInfo summaryInfo,
                                                        @Nullable MarkdownBinder markdownBinder) {
        return new SegmentListAdapter(MODE_GRAPHIC, Collections.emptyList(), summaryInfo, markdownBinder, null);
    }

    private SegmentListAdapter(int mode,
                               @NonNull List<KnowledgeInfo> knowledgeItems,
                               @Nullable SummaryInfo summaryInfo,
                               @Nullable MarkdownBinder markdownBinder,
                               @Nullable OnSegmentClickListener listener) {
        this.mMode = mode;
        this.mKnowledgeItems = knowledgeItems;
        this.mSummaryInfo = summaryInfo;
        this.mMarkdownBinder = markdownBinder;
        this.mListener = listener;

        // 图解模式下预计算行结构：段落/徽章存在则有头部，Markdown 存在则有正文行
        if (mode == MODE_GRAPHIC && summaryInfo != null) {
            boolean hasHeader = !TextUtils.isEmpty(summaryInfo.getParagraphSummary())
                    || (summaryInfo.getMindMapTitles() != null && !summaryInfo.getMindMapTitles().isEmpty());
            if (hasHeader) {
                mGraphicRows.add(TYPE_GRAPHIC_HEADER);
            }
            if (!TextUtils.isEmpty(summaryInfo.getMarkdownContent())) {
                mGraphicRows.add(TYPE_GRAPHIC_MARKDOWN);
            }
        }
    }

    /**
     * 更新当前高亮分段索引（仅刷新变化的项）。
     * <p>
     * 仅文字分析模式生效，图解模式不参与高亮。
     * </p>
     *
     * @param oldIndex 旧索引
     * @param newIndex 新索引
     */
    public void updateCurrentSegmentIndex(int oldIndex, int newIndex) {
        if (mMode != MODE_TEXT) {
            return;
        }
        mCurrentSegmentIndex = newIndex;
        if (oldIndex >= 0 && oldIndex < mKnowledgeItems.size()) {
            notifyItemChanged(oldIndex);
        }
        if (newIndex >= 0 && newIndex < mKnowledgeItems.size()) {
            notifyItemChanged(newIndex);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMode == MODE_TEXT) {
            return TYPE_KNOWLEDGE;
        }
        return mGraphicRows.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_GRAPHIC_HEADER) {
            View view = inflater.inflate(R.layout.item_ai_summary_header, parent, false);
            return new HeaderViewHolder(view);
        }
        // TYPE_KNOWLEDGE / TYPE_GRAPHIC_MARKDOWN 复用同一 section 布局
        View view = inflater.inflate(R.layout.item_summary_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_GRAPHIC_HEADER) {
            bindGraphicHeader((HeaderViewHolder) holder);
        } else if (viewType == TYPE_GRAPHIC_MARKDOWN) {
            bindGraphicMarkdown((SectionViewHolder) holder);
        } else {
            bindKnowledge((SectionViewHolder) holder, position);
        }
    }

    /**
     * 绑定知识点条目（文字分析模式）。
     */
    private void bindKnowledge(@NonNull SectionViewHolder holder, int position) {
        KnowledgeInfo item = mKnowledgeItems.get(position);

        holder.tvTitle.setVisibility(View.VISIBLE);
        holder.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "");

        holder.tvTime.setVisibility(View.VISIBLE);
        holder.tvTime.setText(FormatUtil.formatDuration(item.getStartMs()));

        // 高亮当前分段
        boolean isCurrent = (position == mCurrentSegmentIndex);
        int highlightColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.ai_education_highlight_white);
        int normalColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.ai_education_text_secondary);
        holder.tvTime.setTextColor(isCurrent ? highlightColor : normalColor);

        // 点击时间跳转播放位置
        holder.tvTime.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSegmentClick(item);
            }
        });

        // 内容：有 Markdown 则渲染，否则仅显示标题（隐藏内容区）
        String markdown = item.getContentMarkdown();
        if (!TextUtils.isEmpty(markdown)) {
            holder.tvContent.setVisibility(View.VISIBLE);
            if (mMarkdownBinder != null) {
                mMarkdownBinder.bind(holder.tvContent, markdown);
            } else {
                holder.tvContent.setText(markdown);
            }
        } else {
            holder.tvContent.setText("");
            holder.tvContent.setVisibility(View.GONE);
        }
    }

    /**
     * 绑定图解头部：段落摘要 + 思维导图徽章标签。
     */
    private void bindGraphicHeader(@NonNull HeaderViewHolder holder) {
        if (mSummaryInfo == null) {
            return;
        }

        if (holder.tvDescription != null) {
            String paragraph = mSummaryInfo.getParagraphSummary();
            if (!TextUtils.isEmpty(paragraph)) {
                holder.tvDescription.setVisibility(View.VISIBLE);
                holder.tvDescription.setText(paragraph);
            } else {
                holder.tvDescription.setVisibility(View.GONE);
            }
        }

        if (holder.layoutTags != null) {
            holder.layoutTags.removeAllViews();
            List<String> titles = mSummaryInfo.getMindMapTitles();
            if (titles != null && !titles.isEmpty()) {
                for (String title : titles) {
                    holder.layoutTags.addView(createTagView(holder.itemView, title));
                }
            }
        }
    }

    /**
     * 绑定图解 Markdown 正文（复用 section 布局，隐藏标题与时间行）。
     */
    private void bindGraphicMarkdown(@NonNull SectionViewHolder holder) {
        holder.tvTitle.setVisibility(View.GONE);
        holder.tvTime.setVisibility(View.GONE);
        holder.tvTime.setOnClickListener(null);

        holder.tvContent.setVisibility(View.VISIBLE);
        String markdown = mSummaryInfo != null ? mSummaryInfo.getMarkdownContent() : null;
        if (mMarkdownBinder != null) {
            mMarkdownBinder.bind(holder.tvContent, markdown);
        } else {
            holder.tvContent.setText(markdown != null ? markdown : "");
        }
    }

    /**
     * 创建单个徽章标签视图。
     */
    @NonNull
    private static TextView createTagView(@NonNull View parent, @NonNull String text) {
        TextView tvTag = new TextView(parent.getContext());
        tvTag.setText(text);
        tvTag.setTextSize(11);
        tvTag.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.common_text_white));
        tvTag.setBackgroundResource(R.drawable.bg_tag);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(DensityUtil.dip2px(parent.getContext(), 8));
        tvTag.setLayoutParams(params);
        return tvTag;
    }

    @Override
    public int getItemCount() {
        return mMode == MODE_TEXT ? mKnowledgeItems.size() : mGraphicRows.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        // 清空 Markdown 内容，避免复用时短暂显示旧内容
        if (holder instanceof SectionViewHolder && mMarkdownBinder != null) {
            mMarkdownBinder.clear(((SectionViewHolder) holder).tvContent);
        }
    }

    // ==================== ViewHolders ====================

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        final TextView tvDescription;
        @Nullable
        final LinearLayout layoutTags;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_summary_description);
            layoutTags = itemView.findViewById(R.id.layout_tags);
        }
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvTime;
        final TextView tvContent;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_segment_title);
            tvTime = itemView.findViewById(R.id.tv_segment_time);
            tvContent = itemView.findViewById(R.id.tv_segment_content);
        }
    }

    /**
     * 知识点条目点击回调（文字分析模式）
     */
    public interface OnSegmentClickListener {
        void onSegmentClick(@NonNull KnowledgeInfo knowledge);
    }
}
