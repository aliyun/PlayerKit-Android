package com.aliyun.playerkit.scenes.aieducation.ui.slots;

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
import com.aliyun.playerkit.scenes.aieducation.data.AiContentType;
import com.aliyun.playerkit.scenes.aieducation.data.AiSegment;
import com.aliyun.playerkit.scenes.aieducation.data.AiSummary;

import com.aliyun.playerkit.scenes.aieducation.ui.MarkdownBinder;
import com.aliyun.playerkit.utils.DensityUtil;
import com.aliyun.playerkit.utils.FormatUtil;

import java.util.List;

/**
 * AI 解析列表适配器（多 ViewType）
 * <p>
 * 支持两种 ViewType：
 * <ul>
 *     <li>{@link #TYPE_HEADER}：摘要卡片（description + tags），仅第一项</li>
 *     <li>{@link #TYPE_SEGMENT}：分段内容（标题 + 时间 + 内容文本/Markdown）</li>
 * </ul>
 * 根据 {@link AiContentType} 决定 segment 内容渲染方式：
 * <ul>
 *     <li>{@link AiContentType#TEXT_ANALYSIS}：纯文本展示 segment.summary</li>
 *     <li>{@link AiContentType#GRAPHIC_ANALYSIS}：使用 Markwon 渲染 segment.markdown</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class SegmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SEGMENT = 1;

    @Nullable
    private final AiSummary mSummary;
    private final List<AiSegment> mSegments;
    @AiContentType
    private final int mContentType;
    @Nullable
    private final MarkdownBinder mMarkdownBinder;
    @Nullable
    private final OnSegmentClickListener mListener;

    private final boolean mHasHeader;
    private int mCurrentSegmentIndex = -1;

    public SegmentListAdapter(@Nullable AiSummary summary, @NonNull List<AiSegment> segments, @AiContentType int contentType, @Nullable MarkdownBinder markdownBinder, @Nullable OnSegmentClickListener listener) {
        this.mSummary = summary;
        this.mSegments = segments;
        this.mContentType = contentType;
        this.mMarkdownBinder = markdownBinder;
        this.mListener = listener;
        this.mHasHeader = (summary != null);
    }

    /**
     * 更新当前高亮分段索引（仅刷新变化的项）
     *
     * @param oldIndex 旧索引
     * @param newIndex 新索引
     */
    public void updateCurrentSegmentIndex(int oldIndex, int newIndex) {
        mCurrentSegmentIndex = newIndex;
        int offset = mHasHeader ? 1 : 0;
        if (oldIndex >= 0 && oldIndex < mSegments.size()) {
            notifyItemChanged(oldIndex + offset);
        }
        if (newIndex >= 0 && newIndex < mSegments.size()) {
            notifyItemChanged(newIndex + offset);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasHeader && position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_SEGMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_ai_summary_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_summary_section, parent, false);
            return new SegmentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeader((HeaderViewHolder) holder);
        } else if (holder instanceof SegmentViewHolder) {
            int segmentIndex = mHasHeader ? position - 1 : position;
            bindSegment((SegmentViewHolder) holder, segmentIndex);
        }
    }

    private void bindHeader(@NonNull HeaderViewHolder holder) {
        if (mSummary == null) return;

        if (holder.tvDescription != null && mSummary.getDescription() != null) {
            holder.tvDescription.setText(mSummary.getDescription());
        }

        if (holder.layoutTags != null) {
            holder.layoutTags.removeAllViews();
            List<String> tags = mSummary.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    TextView tvTag = new TextView(holder.itemView.getContext());
                    tvTag.setText(tag);
                    tvTag.setTextSize(11);
                    tvTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ai_education_tag_text));
                    tvTag.setBackgroundResource(R.drawable.bg_tag);
                    int paddingH = DensityUtil.dip2px(holder.itemView.getContext(), 8);
                    int paddingV = DensityUtil.dip2px(holder.itemView.getContext(), 4);
                    tvTag.setPadding(paddingH, paddingV, paddingH, paddingV);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMarginEnd(DensityUtil.dip2px(holder.itemView.getContext(), 8));
                    tvTag.setLayoutParams(params);
                    holder.layoutTags.addView(tvTag);
                }
            }
        }
    }

    private void bindSegment(@NonNull SegmentViewHolder holder, int segmentIndex) {
        AiSegment segment = mSegments.get(segmentIndex);
        holder.tvTitle.setText(segment.getTitle());
        holder.tvTime.setText(FormatUtil.formatDuration(segment.getStartMs()));

        // Highlight state
        boolean isCurrent = (segmentIndex == mCurrentSegmentIndex);
        int highlightColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.ai_education_highlight_white);
        int normalColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.ai_education_text_secondary);
        holder.tvTime.setTextColor(isCurrent ? highlightColor : normalColor);

        // Click to seek
        holder.tvTime.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSegmentClick(segment);
            }
        });

        // Render content based on type
        if (mContentType == AiContentType.GRAPHIC_ANALYSIS && segment.getMarkdown() != null && mMarkdownBinder != null) {
            mMarkdownBinder.bind(holder.tvContent, segment.getMarkdown());
        } else {
            String text = segment.getSummary() != null ? segment.getSummary() : "";
            holder.tvContent.setText(text);
        }
    }

    @Override
    public int getItemCount() {
        int count = mSegments.size();
        if (mHasHeader) {
            count += 1;
        }
        return count;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        // 清空 Markdown 内容，避免复用时短暂显示旧内容
        if (holder instanceof SegmentViewHolder && mMarkdownBinder != null) {
            mMarkdownBinder.clear(((SegmentViewHolder) holder).tvContent);
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

    static class SegmentViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvTime;
        final TextView tvContent;

        SegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_segment_title);
            tvTime = itemView.findViewById(R.id.tv_segment_time);
            tvContent = itemView.findViewById(R.id.tv_segment_content);
        }
    }

    /**
     * Segment 点击回调
     */
    public interface OnSegmentClickListener {
        void onSegmentClick(@NonNull AiSegment segment);
    }
}
