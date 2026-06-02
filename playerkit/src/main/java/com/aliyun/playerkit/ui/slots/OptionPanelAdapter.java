package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;

import java.util.List;

/**
 * 选项面板适配器
 * <p>
 * 用于横屏场景下倍速/清晰度选择面板的 RecyclerView 适配器。
 * 选中项白色加粗，未选中项灰色常规。
 * </p>
 *
 * @author junHuiYe
 * @date 2026/05/28
 */
class OptionPanelAdapter extends RecyclerView.Adapter<OptionPanelAdapter.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final List<String> mLabels;
    private final int mColorSelected;
    private final int mColorNormal;
    private int mSelectedIndex = -1;
    private OnItemClickListener mListener;

    OptionPanelAdapter(@NonNull List<String> labels, @NonNull Context context) {
        mLabels = labels;
        mColorSelected = ContextCompat.getColor(context, R.color.setting_option_selected_color);
        mColorNormal = ContextCompat.getColor(context, R.color.setting_option_normal_color);
    }

    void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option_panel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String label = mLabels.get(position);
        boolean selected = (position == mSelectedIndex);

        holder.tvLabel.setText(label);

        holder.tvLabel.setTextColor(selected ? mColorSelected : mColorNormal);
        holder.tvLabel.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                if (position == RecyclerView.NO_POSITION) return;
                mListener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLabels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tv_option_label);
        }
    }
}
