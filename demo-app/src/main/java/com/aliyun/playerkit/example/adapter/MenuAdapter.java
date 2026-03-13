package com.aliyun.playerkit.example.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.example.R;
import com.aliyun.playerkit.example.model.MenuItem;
import com.aliyun.playerkit.example.router.SchemaRouter;

import java.util.List;

/**
 * Menu adapter for RecyclerView to display menu items with headers and clickable items
 * 菜单适配器，用于在RecyclerView中显示包含页眉和可点击项的菜单
 *
 * @author keria
 * @date 2025/5/31
 * @brief RecyclerView adapter for menu display with header and item support
 */
public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<MenuItem> menuItems;

    /**
     * 构造函数，创建菜单适配器
     *
     * @param menuItems 菜单项列表
     */
    public MenuAdapter(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * 创建ViewHolder实例
     *
     * @param parent   父容器视图
     * @param viewType 视图类型
     * @return ViewHolder实例
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_main_item, parent, false);
        return new ItemViewHolder(view);
    }

    /**
     * 绑定ViewHolder数据
     *
     * @param holder   ViewHolder实例
     * @param position 数据位置
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind(item);
        }
    }

    /**
     * 获取菜单项数量
     *
     * @return 菜单项总数
     */
    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    /**
     * 菜单项ViewHolder，用于显示单个菜单项
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivIcon;
        private TextView tvTitle;

        /**
         * 构造函数，创建菜单项ViewHolder
         *
         * @param itemView 项视图
         */
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        /**
         * 初始化视图组件
         */
        private void initViews() {
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        /**
         * 绑定菜单项数据到视图
         *
         * @param item 菜单项数据
         */
        @SuppressLint("ClickableViewAccessibility")
        public void bind(MenuItem item) {
            // 绑定数据
            ivIcon.setImageResource(item.getResId());
            tvTitle.setText(item.getTitle());

            itemView.setOnClickListener(v -> SchemaRouter.navigate(v.getContext(), item.getSchema()));
        }
    }
}
