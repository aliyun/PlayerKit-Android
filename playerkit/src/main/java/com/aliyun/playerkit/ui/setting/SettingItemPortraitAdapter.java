package com.aliyun.playerkit.ui.setting;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.R;

import java.util.List;

/**
 * 竖屏设置项适配器。
 * <p>
 * 继承 {@link BaseSettingItemAdapter}，提供竖屏模式下的列表项装饰：
 * 同组设置项共享圆角卡片背景，组间添加间距，组内末尾项隐藏分割线。
 * </p>
 *
 * <p>
 * Portrait setting item adapter.
 * Extends {@link BaseSettingItemAdapter} with portrait-specific item decoration:
 * items in the same group share a rounded-corner card background,
 * spacing is added between groups, and divider is hidden for the last item in each group.
 * </p>
 *
 * @author junHuiYe
 * @date 2026/05/19
 */
public class SettingItemPortraitAdapter extends BaseSettingItemAdapter {

    /**
     * 分组卡片圆角半径（dp）。
     * <p>
     * Corner radius for group card background (in dp).
     * </p>
     */
    private static final float CARD_CORNER_RADIUS_DP = 12f;

    /**
     * 分组间距（dp）。
     * <p>
     * Spacing between groups (in dp).
     * </p>
     */
    private static final float GROUP_SPACING_DP = 10f;

    /**
     * 分组卡片背景色。
     * <p>
     * Background color for group cards.
     * </p>
     */
    private final int mCardBgColor;

    /**
     * 圆角半径（px）。
     * <p>
     * Corner radius in pixels.
     * </p>
     */
    private final int mCornerRadiusPx;

    private final float[] mRadiiAll;
    private final float[] mRadiiTop;
    private final float[] mRadiiBottom;
    private static final float[] RADII_NONE = {0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * 构造竖屏设置项适配器。
     * <p>
     * Construct the portrait setting item adapter.
     * </p>
     *
     * @param items   设置项列表 / Setting item list
     * @param context 上下文 / Context
     */
    public SettingItemPortraitAdapter(@NonNull List<SettingItem<?>> items, @NonNull Context context) {
        super(items, context);
        this.mCardBgColor = ContextCompat.getColor(context, R.color.setting_panel_item_bg);
        this.mCornerRadiusPx = dp2px(CARD_CORNER_RADIUS_DP);
        float r = mCornerRadiusPx;
        this.mRadiiAll = new float[]{r, r, r, r, r, r, r, r};
        this.mRadiiTop = new float[]{r, r, r, r, 0, 0, 0, 0};
        this.mRadiiBottom = new float[]{0, 0, 0, 0, r, r, r, r};
    }

    /**
     * 竖屏装饰：根据分组位置应用圆角背景、分组间距和分割线。
     * <p>
     * Portrait decoration: apply rounded-corner background, group spacing,
     * and divider visibility based on group position.
     * </p>
     */
    @Override
    protected void applyItemDecoration(@NonNull RecyclerView.ViewHolder holder, int position) {
        int currentGroup = mItems.get(position).group;
        boolean isFirst = (position == 0) || mItems.get(position - 1).group != currentGroup;
        boolean isLast = (position == mItems.size() - 1) || mItems.get(position + 1).group != currentGroup;

        // 圆角背景：复用当前itemView的背景Drawable
        float[] radii = resolveCornerRadii(isFirst, isLast);
        GradientDrawable bg;
        if (holder.itemView.getBackground() instanceof GradientDrawable) {
            bg = (GradientDrawable) holder.itemView.getBackground();
        } else {
            bg = new GradientDrawable();
            holder.itemView.setBackground(bg);
        }
        bg.setColor(mCardBgColor);
        bg.setCornerRadii(radii);

        // 分组间距
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (lp != null) {
            lp.topMargin = isFirst && position != 0 ? dp2px(GROUP_SPACING_DP) : 0;
            holder.itemView.setLayoutParams(lp);
        }

        // 分割线：组内最后一项隐藏
        View divider = holder.itemView.findViewById(R.id.view_divider);
        if (divider != null) {
            divider.setVisibility(isLast ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 根据列表项在分组中的位置，计算圆角半径数组。
     * <p>
     * 规则：
     * <ul>
     *   <li>独立项（首+末）：四角圆角</li>
     *   <li>组首项：上方两角圆角</li>
     *   <li>组末项：下方两角圆角</li>
     *   <li>组中间项：无圆角</li>
     * </ul>
     * </p>
     *
     * <p>
     * Resolve corner radii based on item position within its group.
     * </p>
     *
     * @param isFirst 是否为组首项 / Whether it is the first item in the group
     * @param isLast  是否为组末项 / Whether it is the last item in the group
     * @return 8 个浮点值的圆角半径数组 / Array of 8 float values for corner radii
     */
    private float[] resolveCornerRadii(boolean isFirst, boolean isLast) {
        if (isFirst && isLast) {
            return mRadiiAll;
        } else if (isFirst) {
            return mRadiiTop;
        } else if (isLast) {
            return mRadiiBottom;
        } else {
            return RADII_NONE;
        }
    }
}
