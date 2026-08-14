package com.aliyun.playerkit.ui.chapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.utils.FormatUtil;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节面板列表适配器
 * <p>
 * Adapter for chapter panel list
 * </p>
 *
 * @author keria
 * @date 2026/07/13
 */
public class ChapterPanelListAdapter extends RecyclerView.Adapter<ChapterPanelListAdapter.ViewHolder> {

    private final List<ChapterInfo> mChapters = new ArrayList<>();
    private int mCurrentIndex = -1;

    @Nullable
    private OnChapterItemClickListener mListener;

    /**
     * 章节项点击回调
     * <p>
     * Chapter item click callback
     * </p>
     */
    public interface OnChapterItemClickListener {
        void onChapterItemClick(int position, ChapterInfo chapter);
    }

    public void setOnChapterItemClickListener(@Nullable OnChapterItemClickListener listener) {
        mListener = listener;
    }

    /**
     * 设置章节数据
     * <p>
     * Set chapter data
     * </p>
     */
    public void setChapters(@NonNull List<ChapterInfo> chapters) {
        mChapters.clear();
        mChapters.addAll(chapters);
        mCurrentIndex = -1;
        notifyDataSetChanged();
    }

    /**
     * 更新当前播放章节索引（增量更新）
     * <p>
     * Update current playing chapter index (incremental update)
     * </p>
     */
    public void updateCurrentIndex(int newIndex) {
        if (newIndex == mCurrentIndex) return;
        int oldIndex = mCurrentIndex;
        mCurrentIndex = newIndex;
        if (oldIndex >= 0 && oldIndex < mChapters.size()) {
            notifyItemChanged(oldIndex);
        }
        if (newIndex >= 0 && newIndex < mChapters.size()) {
            notifyItemChanged(newIndex);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter_panel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterInfo chapter = mChapters.get(position);
        boolean isCurrent = position == mCurrentIndex;
        Context context = holder.itemView.getContext();

        // 标题
        holder.tvTitle.setText(chapter.getTitle());
        holder.tvTitle.setTextColor(ContextCompat.getColor(context,
                isCurrent ? R.color.common_text_white : R.color.chapter_title_unselected));

        // 时间
        holder.tvTime.setText(FormatUtil.formatDuration(chapter.getStartMs()));
        holder.tvTime.setTextColor(ContextCompat.getColor(context,
                isCurrent ? R.color.chapter_time_selected : R.color.chapter_secondary_text));

        // 选中态背景
        holder.itemView.setBackgroundColor(isCurrent
                ? ContextCompat.getColor(context, R.color.chapter_item_selected_bg)
                : Color.TRANSPARENT);

        // 箭头透明度
        holder.ivPlayingIndicator.setAlpha(isCurrent ? 1.0f : 0.5f);

        // 缩略图
        String thumbnailUrl = chapter.getThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(holder.ivThumbnail.getContext())
                    .load(thumbnailUrl)
                    .centerCrop()
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageDrawable(null);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && mListener != null) {
                mListener.onChapterItemClick(adapterPosition, mChapters.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChapters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumbnail;
        final TextView tvTitle;
        final TextView tvTime;
        final ImageView ivPlayingIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            tvTime = itemView.findViewById(R.id.tv_chapter_time);
            ivPlayingIndicator = itemView.findViewById(R.id.iv_playing_indicator);
        }
    }
}
