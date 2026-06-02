package com.aliyun.playerkit.ui.setting;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import java.util.Objects;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;

import java.util.List;

/**
 * 设置项列表适配器基类。
 * <p>
 * 封装 Selector（选择器）和 Switcher（开关）两种设置项类型的通用绑定逻辑，
 * 子类仅需实现 {@link #applyItemDecoration} 来定义不同布局模式下的外观装饰。
 * </p>
 *
 * <p>
 * Base adapter for setting item lists.
 * Encapsulates common binding logic for Selector and Switcher item types.
 * Subclasses only need to implement {@link #applyItemDecoration} for layout-specific decoration.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public abstract class BaseSettingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SWITCHER = 0;
    private static final int TYPE_SELECTOR = 1;

    /**
     * 设置项数据列表。
     * <p>
     * Setting item data list.
     * </p>
     */
    protected final List<SettingItem<?>> mItems;

    /**
     * 上下文引用。
     * <p>
     * Context reference.
     * </p>
     */
    protected final Context mContext;

    /**
     * 选中态文字颜色。
     * <p>
     * Text color for selected option.
     * </p>
     */
    protected final int mColorSelected;

    /**
     * 默认态文字颜色。
     * <p>
     * Text color for unselected option.
     * </p>
     */
    protected final int mColorNormal;

    private final float mDensity;

    /**
     * 构造适配器。
     * <p>
     * Construct the adapter.
     * </p>
     *
     * @param items   设置项列表 / Setting item list
     * @param context 上下文 / Context
     */
    public BaseSettingItemAdapter(@NonNull List<SettingItem<?>> items, @NonNull Context context) {
        this.mItems = items;
        this.mContext = context;
        this.mColorSelected = ContextCompat.getColor(context, R.color.setting_option_selected_color);
        this.mColorNormal = ContextCompat.getColor(context, R.color.setting_option_normal_color);
        this.mDensity = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getItemViewType(int position) {
        int type = mItems.get(position).type;
        if (type == SettingItemType.SELECTOR) {
            return TYPE_SELECTOR;
        } else if (type == SettingItemType.SWITCHER) {
            return TYPE_SWITCHER;
        }
        // 如果将来添加新的类型，这里可以返回默认值或抛出异常
        throw new IllegalArgumentException("Unknown item type: " + type);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SELECTOR) {
            View view = inflater.inflate(R.layout.item_setting_option, parent, false);
            return new SelectorViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_setting_switch, parent, false);
            return new SwitcherViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem<?> item = mItems.get(position);

        applyItemDecoration(holder, position);

        if (holder instanceof SelectorViewHolder) {
            bindSelector((SelectorViewHolder) holder, item);
        } else if (holder instanceof SwitcherViewHolder) {
            bindSwitcher((SwitcherViewHolder) holder, item);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // ---------------------------------------------------------------------
    // Template method
    // ---------------------------------------------------------------------

    /**
     * 应用列表项的外观装饰（模板方法）。
     * <p>
     * 由子类实现，用于处理分割线、分组背景、间距等与布局模式相关的视觉效果。
     * </p>
     *
     * <p>
     * Apply item decoration (template method).
     * Implemented by subclasses to handle dividers, group backgrounds, spacing, etc.
     * </p>
     *
     * @param holder   当前 ViewHolder / Current ViewHolder
     * @param position 当前位置 / Current position
     */
    protected abstract void applyItemDecoration(@NonNull RecyclerView.ViewHolder holder, int position);

    // ---------------------------------------------------------------------
    // Bind logic
    // ---------------------------------------------------------------------

    /**
     * 绑定 Selector（选择器）类型设置项。
     * <p>
     * 设置标题，构建选项视图列表，并处理选中状态与点击回调。
     * </p>
     *
     * <p>
     * Bind a Selector type setting item.
     * Sets title, builds option views, and handles selection state and click callbacks.
     * </p>
     *
     * @param holder  ViewHolder
     * @param rawItem 设置项数据 / Setting item data
     */
    @SuppressWarnings("unchecked")
    private <T> void bindSelector(SelectorViewHolder holder, SettingItem<?> rawItem) {
        SettingItem<T> item = (SettingItem<T>) rawItem;

        holder.tvTitle.setText(item.title);
        holder.llOptionsContainer.removeAllViews();

        SettingOptions<T> options = item.options;
        if (options == null || options.size() == 0) {
            return;
        }

        SettingItem.ValueFormatter<T> formatter = item.formatter;

        for (int i = 0; i < options.size(); i++) {
            T optionValue = options.get(i);

            TextView optionView = new TextView(mContext);
            String displayText = formatter != null ? formatter.format(optionValue) : String.valueOf(optionValue);
            optionView.setText(displayText);
            optionView.setTextSize(12);

            boolean isSelected = Objects.equals(optionValue, item.currentValue);
            applyOptionTextStyle(optionView, isSelected);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginStart(dp2px(16));
            optionView.setLayoutParams(lp);

            optionView.setOnClickListener(v -> {
                if (Objects.equals(optionValue, item.currentValue)) {
                    return;
                }
                item.currentValue = optionValue;
                if (item.listener != null) {
                    item.listener.onValueChanged(item, optionValue);
                }
                refreshOptionStyles(holder.llOptionsContainer, item);
            });

            holder.llOptionsContainer.addView(optionView);
        }
    }

    /**
     * 绑定 Switcher（开关）类型设置项。
     * <p>
     * 设置标题和开关状态，并处理切换回调。
     * </p>
     *
     * <p>
     * Bind a Switcher type setting item.
     * Sets title and switch state, and handles toggle callbacks.
     * </p>
     *
     * @param holder  ViewHolder
     * @param rawItem 设置项数据 / Setting item data
     */
    @SuppressWarnings("unchecked")
    private void bindSwitcher(SwitcherViewHolder holder, SettingItem<?> rawItem) {
        SettingItem<Boolean> item = (SettingItem<Boolean>) rawItem;

        holder.tvTitle.setText(item.title);
        holder.cbCheckBox.setOnCheckedChangeListener(null);
        holder.cbCheckBox.setChecked(Boolean.TRUE.equals(item.currentValue));

        holder.cbCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.currentValue = isChecked;
            if (item.listener != null) {
                item.listener.onValueChanged(item, isChecked);
            }
        });
    }

    // ---------------------------------------------------------------------
    // Style helpers
    // ---------------------------------------------------------------------

    /**
     * 刷新选项容器内所有选项的文字样式。
     * <p>
     * Refresh text styles of all options within the container.
     * </p>
     *
     * @param container 选项容器 / Options container
     * @param item      设置项数据 / Setting item data
     */
    private <T> void refreshOptionStyles(LinearLayout container, SettingItem<T> item) {
        SettingOptions<T> options = item.options;
        if (options == null) return;

        int count = Math.min(container.getChildCount(), options.size());
        for (int i = 0; i < count; i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                T optionValue = options.get(i);
                boolean isSelected = Objects.equals(optionValue, item.currentValue);
                applyOptionTextStyle((TextView) child, isSelected);
            }
        }
    }

    /**
     * 设置选项文字的选中/默认样式。
     * <p>
     * Apply selected or default style to option text.
     * </p>
     *
     * @param view     目标 TextView / Target TextView
     * @param selected 是否选中 / Whether selected
     */
    private void applyOptionTextStyle(TextView view, boolean selected) {
        if (selected) {
            view.setTextColor(mColorSelected);
            view.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            view.setTextColor(mColorNormal);
            view.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * dp 转 px 工具方法。
     * <p>
     * Utility method: dp to px conversion.
     * </p>
     *
     * @param dp dp 值 / Value in dp
     * @return px 值 / Value in px
     */
    protected int dp2px(float dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    // ---------------------------------------------------------------------
    // ViewHolders
    // ---------------------------------------------------------------------

    /**
     * Selector 类型设置项的 ViewHolder。
     * <p>
     * ViewHolder for Selector type setting items.
     * </p>
     */
    protected static class SelectorViewHolder extends RecyclerView.ViewHolder {

        final View container;
        final TextView tvTitle;
        final LinearLayout llOptionsContainer;

        SelectorViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.setting_option_container);
            tvTitle = itemView.findViewById(R.id.tv_option_title);
            llOptionsContainer = itemView.findViewById(R.id.setting_items_option);
        }
    }

    /**
     * Switcher 类型设置项的 ViewHolder。
     * <p>
     * ViewHolder for Switcher type setting items.
     * </p>
     */
    protected static class SwitcherViewHolder extends RecyclerView.ViewHolder {

        final View container;
        final TextView tvTitle;
        final CheckBox cbCheckBox;

        SwitcherViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.setting_switch_container);
            tvTitle = itemView.findViewById(R.id.tv_switcher_title);
            cbCheckBox = itemView.findViewById(R.id.cb_setting_checkbox);
        }
    }
}
