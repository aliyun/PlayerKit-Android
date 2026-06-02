package com.aliyun.playerkit.ui.setting;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;

import java.util.List;

/**
 * 横屏设置项适配器。
 * <p>
 * 继承 {@link BaseSettingItemAdapter}，提供横屏模式下的列表项装饰：
 * 仅在非末尾项显示分割线，并使用横屏专属的设置项高度。
 * </p>
 *
 * <p>
 * Landscape setting item adapter.
 * Extends {@link BaseSettingItemAdapter} with landscape-specific item decoration:
 * shows divider for all items except the last one, and applies landscape item height.
 * </p>
 *
 * @author junHuiYe
 * @date 2026/05/19
 */
public class SettingItemLandscapeAdapter extends BaseSettingItemAdapter {

    private final int mItemHeightLand;

    /**
     * 构造横屏设置项适配器。
     * <p>
     * Construct the landscape setting item adapter.
     * </p>
     *
     * @param items   设置项列表 / Setting item list
     * @param context 上下文 / Context
     */
    public SettingItemLandscapeAdapter(@NonNull List<SettingItem<?>> items, @NonNull Context context) {
        super(items, context);
        mItemHeightLand = context.getResources().getDimensionPixelSize(R.dimen.setting_item_height_land);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        View container = null;
        if (holder instanceof SelectorViewHolder) {
            container = ((SelectorViewHolder) holder).container;
        } else if (holder instanceof SwitcherViewHolder) {
            container = ((SwitcherViewHolder) holder).container;
        }
        if (container != null) {
            ViewGroup.LayoutParams lp = container.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mItemHeightLand);
            } else {
                lp.height = mItemHeightLand;
            }
            container.setLayoutParams(lp);
        }
        return holder;
    }

    /**
     * 横屏装饰：末尾项隐藏分割线，其余项显示。
     * <p>
     * Landscape decoration: hide divider for the last item, show for others.
     * </p>
     */
    @Override
    protected void applyItemDecoration(@NonNull RecyclerView.ViewHolder holder, int position) {
        boolean isLast = (position == mItems.size() - 1);
        View divider = holder.itemView.findViewById(R.id.view_divider);
        if (divider != null) {
            divider.setVisibility(isLast ? View.GONE : View.VISIBLE);
        }
    }
}
