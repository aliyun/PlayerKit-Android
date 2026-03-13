package com.aliyun.playerkit.example.settings.link;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.example.settings.R;

import com.aliyun.playerkit.utils.StringUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * 链接配置列表适配器
 * <p>
 * 负责管理链接配置项的列表展示，处理 ViewHolder 的创建和数据绑定。
 * 支持实时监听输入框内容变化并同步到数据模型，处理扫码按钮点击事件。
 * </p>
 * <p>
 * Link Configuration List Adapter
 * <p>
 * Manages the list display of link configuration items, handles ViewHolder creation and data binding.
 * Supports real-time monitoring of input field content changes and synchronization to the data model,
 * handles QR code scan button click events.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.LinkViewHolder> {

    /**
     * 链接配置项列表
     */
    private final List<LinkItem> mItems;
    /**
     * 扫码点击监听器
     */
    private OnScanClickListener mOnScanClickListener;

    /**
     * 扫码点击监听器接口
     * <p>
     * 当用户点击某个配置项的扫码按钮时触发回调。
     * </p>
     */
    public interface OnScanClickListener {
        /**
         * 扫码按钮点击回调
         *
         * @param position 配置项位置
         * @param item     配置项数据
         */
        void onScanClick(int position, LinkItem item);
    }

    /**
     * 构造函数
     *
     * @param items 链接配置项列表
     */
    public LinkAdapter(List<LinkItem> items) {
        this.mItems = items;
    }

    /**
     * 设置扫码点击监听器
     *
     * @param listener 监听器实例
     */
    public void setOnScanClickListener(OnScanClickListener listener) {
        this.mOnScanClickListener = listener;
    }

    /**
     * 创建 ViewHolder
     * <p>
     * 加载布局文件并创建对应的 ViewHolder 实例。
     * </p>
     *
     * @param parent   父容器
     * @param viewType 视图类型
     * @return ViewHolder 实例
     */
    @NonNull
    @Override
    public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_link, parent, false);
        return new LinkViewHolder(view);
    }

    /**
     * 绑定数据到 ViewHolder
     * <p>
     * 将指定位置的数据绑定到 ViewHolder，更新 UI 显示。
     * </p>
     *
     * @param holder   ViewHolder 实例
     * @param position 数据位置
     */
    @Override
    public void onBindViewHolder(@NonNull LinkViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    /**
     * 获取列表项数量
     *
     * @return 配置项数量
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * 链接配置项 ViewHolder
     * <p>
     * 负责单个配置项的视图管理和数据绑定，处理输入框文本变化和扫码按钮点击。
     * </p>
     */
    public class LinkViewHolder extends RecyclerView.ViewHolder {
        /**
         * 配置项名称 TextView
         */
        private final TextView tvName;
        /**
         * 链接输入框
         */
        private final TextInputEditText etLink;
        /**
         * 扫码按钮
         */
        private final MaterialButton btnScan;
        /**
         * 文本变化监听器
         */
        private TextWatcher mTextWatcher;

        /**
         * 构造函数
         *
         * @param itemView 根视图
         */
        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            etLink = itemView.findViewById(R.id.et_link);
            btnScan = itemView.findViewById(R.id.btn_scan);
        }

        /**
         * 绑定数据到视图
         * <p>
         * 设置配置项名称和链接内容，注册文本变化监听器以实时同步数据，
         * 设置扫码按钮点击监听器。
         * </p>
         *
         * @param item 链接配置项数据
         */
        public void bind(final LinkItem item) {
            tvName.setText(item.getNameResId());

            // 移除旧监听器，防止 ViewHolder 复用导致数据错乱
            if (mTextWatcher != null) {
                etLink.removeTextChangedListener(mTextWatcher);
            }

            // 获取链接内容
            String itemLink = item.getLink();
            etLink.setText(StringUtil.isNotEmpty(itemLink) ? itemLink : "");

            // 创建新的文本变化监听器，实时同步输入内容到数据模型
            mTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // 文本变化前，无需处理
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 文本变化中，无需处理
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // 文本变化后，同步到数据模型
                    item.setLink(s.toString());
                }
            };
            etLink.addTextChangedListener(mTextWatcher);

            // 设置扫码按钮点击监听
            btnScan.setOnClickListener(v -> {
                if (mOnScanClickListener != null) {
                    mOnScanClickListener.onScanClick(getAdapterPosition(), item);
                }
            });
        }
    }
}
