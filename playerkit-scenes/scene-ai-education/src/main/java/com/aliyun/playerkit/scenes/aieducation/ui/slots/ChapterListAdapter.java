package com.aliyun.playerkit.scenes.aieducation.ui.slots;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.scenes.aieducation.R;
import com.aliyun.playerkit.utils.FormatUtil;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * 章节列表适配器
 * <p>
 * 将 {@link ChapterInfo} 数据绑定到 item_chapter_list 布局，
 * 支持高亮当前播放章节，点击项触发回调。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder> {

    private final List<ChapterInfo> mChapters;
    private final OnChapterClickListener mListener;
    private int mCurrentIndex = -1;

    public ChapterListAdapter(@NonNull List<ChapterInfo> chapters, @NonNull OnChapterClickListener listener) {
        this.mChapters = chapters;
        this.mListener = listener;
    }

    /**
     * 更新当前高亮章节索引（仅刷新变化的项）
     *
     * @param oldIndex 旧索引
     * @param newIndex 新索引
     */
    public void updateCurrentIndex(int oldIndex, int newIndex) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterInfo chapter = mChapters.get(position);
        holder.tvTitle.setText(chapter.getTitle());
        holder.tvTime.setText(FormatUtil.formatDuration(chapter.getStartMs()));

        // Highlight current chapter
        boolean isCurrent = (position == mCurrentIndex);
        Context context = holder.itemView.getContext();
        int color = ContextCompat.getColor(context, isCurrent
                ? R.color.ai_education_highlight_red
                : R.color.ai_education_text_primary);
        holder.tvTitle.setTextColor(color);

        // Thumbnail placeholder
        String thumbnailUrl = chapter.getThumbnailUrl();
        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Glide.with(holder.ivThumbnail.getContext())
                    .load(thumbnailUrl)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setBackgroundColor(0xFFE0E0E0);
            holder.ivThumbnail.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> mListener.onChapterClick(chapter));
    }

    @Override
    public int getItemCount() {
        return mChapters.size();
    }

    /**
     * 章节点击回调
     */
    public interface OnChapterClickListener {
        void onChapterClick(@NonNull ChapterInfo chapter);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumbnail;
        final TextView tvTitle;
        final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            tvTime = itemView.findViewById(R.id.tv_chapter_time);
        }
    }
}
