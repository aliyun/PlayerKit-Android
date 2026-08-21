package com.aliyun.playerkit.ui.chapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.utils.FormatUtil;

import java.util.Collections;
import java.util.List;

/**
 * 章节 Chip 适配器
 * <p>
 * 用于在进度条上方和 Seek 预览浮层中显示横向可滑动的章节导航列表。
 * 每个 Chip 显示章节的开始时间和标题，支持高亮当前播放章节。
 * </p>
 * <p>
 * Chapter Chip Adapter
 * <p>
 * Displays a horizontally scrollable chapter navigation list above the progress bar
 * and in the seek preview overlay. Each chip shows the chapter start time and title,
 * with highlight support for the currently playing chapter.
 * </p>
 *
 * @author keria
 * @date 2026/07/07
 */
public class ChapterChipAdapter extends RecyclerView.Adapter<ChapterChipAdapter.ViewHolder> {

    private List<ChapterInfo> mChapters = Collections.emptyList();
    private int mCurrentIndex = -1;

    @Nullable
    private OnChipClickListener mOnChipClickListener;

    /**
     * 设置 Chip 点击回调
     * <p>
     * Set chip click listener
     * </p>
     *
     * @param listener 点击回调
     */
    public void setOnChipClickListener(@Nullable OnChipClickListener listener) {
        this.mOnChipClickListener = listener;
    }

    /**
     * 设置章节数据
     * <p>
     * Set chapter data
     * </p>
     *
     * @param chapters 章节列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setChapters(@NonNull List<ChapterInfo> chapters) {
        this.mChapters = chapters;
        mCurrentIndex = -1;
        notifyDataSetChanged();
    }

    /**
     * 更新高亮索引（仅刷新变化的 2 个 item）
     * <p>
     * Update the highlighted index, refreshing only the 2 changed items.
     * </p>
     *
     * @param newIndex 新的高亮索引
     */
    public void updateCurrentIndex(int newIndex) {
        if (newIndex == mCurrentIndex) {
            return;
        }
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TextView view = (TextView) inflater.inflate(R.layout.item_chapter_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterInfo chapter = mChapters.get(position);
        boolean isCurrent = (position == mCurrentIndex);

        String text = holder.itemView.getContext().getString(R.string.chapter_chip_text_format,
                FormatUtil.formatDuration(chapter.getStartMs()),
                chapter.getTitle());
        holder.tvChip.setText(text);

        holder.itemView.setSelected(isCurrent);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            if (adapterPosition < 0 || adapterPosition >= mChapters.size()) return;
            if (mOnChipClickListener != null) {
                mOnChipClickListener.onChipClick(adapterPosition, mChapters.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChapters.size();
    }

    /**
     * Chip 点击回调
     * <p>
     * Chip click listener
     * </p>
     */
    public interface OnChipClickListener {
        void onChipClick(int position, ChapterInfo chapter);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvChip;

        ViewHolder(@NonNull TextView itemView) {
            super(itemView);
            this.tvChip = itemView;
        }
    }
}
