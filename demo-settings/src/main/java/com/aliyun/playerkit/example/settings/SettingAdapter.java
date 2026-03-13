package com.aliyun.playerkit.example.settings;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.example.settings.binder.IBinderContext;
import com.aliyun.playerkit.example.settings.binder.ButtonBinder;
import com.aliyun.playerkit.example.settings.binder.TextBinder;
import com.aliyun.playerkit.example.settings.binder.HeaderBinder;
import com.aliyun.playerkit.example.settings.binder.SelectorBinder;
import com.aliyun.playerkit.example.settings.binder.ISettingBinder;
import com.aliyun.playerkit.example.settings.binder.SwitchBinder;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 设置页适配器
 * <p>
 * 职责：
 * 1. 维护设置项数据的内存模型与索引映射。
 * 2. 根据设置项类型分发 UI 布局。
 * 3. 委派具体的 UI 绑定逻辑给对应的 {@link ISettingBinder}。
 * </p>
 * <p>
 * Settings Page Adapter
 * </p>
 * <p>
 * Responsibility:
 * 1. Maintain memory model and index mapping of setting item data.
 * 2. Dispatch UI layouts based on setting item types.
 * 3. Delegate specific UI binding logic to the corresponding {@link ISettingBinder}.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> implements IBinderContext {

    /**
     * 内部数据存储：ID -> Model，保持插入顺序
     */
    private final Map<String, SettingModel> mModelsMap = new LinkedHashMap<>();
    /**
     * 绑定器映射：Type -> Binder
     * <p>
     * Binder mapping: Type -> Binder.
     * </p>
     */
    private final Map<Integer, ISettingBinder> mBinders = new HashMap<>();

    /**
     * 构造函数
     * <p>
     * 注册各类视图类型的绑定器。
     * Register binders for various view types.
     * </p>
     */
    public SettingAdapter() {
        registerBinders();
    }

    @Override
    public void notifyItemChanged(@NonNull SettingModel model) {
        updateItemValue(model.getId(), model.getValue());
    }

    /**
     * 设置全量数据并刷新 UI
     * <p>
     * 触发全局列表刷新
     * Sets all data and refreshes the UI, triggering a global list refresh.
     * </p>
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(@NonNull List<SettingModel> models) {
        mModelsMap.clear();
        for (SettingModel model : models) {
            mModelsMap.put(model.getId(), model);
        }
        notifyDataSetChanged();
    }

    /**
     * 获取当前所有项
     * <p>
     * Gets all current setting items.
     * </p>
     *
     * @return 项列表
     */
    public List<SettingModel> getModels() {
        return new ArrayList<>(mModelsMap.values());
    }

    /**
     * 更新指定项摘要并局部刷新
     * <p>
     * 局部更新 UI，避免全局重刷
     * Updates the value of a specified item and performs a local refresh, avoiding a global refresh.
     * </p>
     *
     * @param id    项 ID
     * @param value 摘要文本
     */
    public void updateItemValue(@NonNull String id, @Nullable String value) {
        SettingModel model = mModelsMap.get(id);
        if (model != null) {
            model.setValue(value);
            int position = getPositionById(id);
            if (position != -1) {
                notifyItemChanged(position);
            }
        }
    }

    /**
     * 注册组件绑定器
     * <p>
     * Registers component binders.
     * </p>
     */
    private void registerBinders() {
        mBinders.put(SettingType.HEADER, new HeaderBinder());
        mBinders.put(SettingType.BUTTON, new ButtonBinder());
        mBinders.put(SettingType.TEXT, new TextBinder());
        mBinders.put(SettingType.SWITCH, new SwitchBinder());
        mBinders.put(SettingType.SELECTOR, new SelectorBinder());
    }

    /**
     * 根据 ID 获取项的索引
     * <p>
     * Gets the index of an item by its ID.
     * </p>
     */
    private int getPositionById(String id) {
        int index = 0;
        for (String key : mModelsMap.keySet()) {
            if (StringUtil.equals(key, id)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 创建 ViewHolder
     * <p>
     * Creates a ViewHolder.
     * </p>
     */
    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = getLayoutResByType(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new SettingViewHolder(view, mBinders.get(viewType));
    }

    /**
     * 绑定数据
     * <p>
     * Binds data to the ViewHolder.
     * </p>
     */
    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingModel model = getModels().get(position);
        holder.bind(model, this);
    }

    /**
     * 获取数据项数量
     * <p>
     * Gets the number of data items.
     * </p>
     */
    @Override
    public int getItemCount() {
        return mModelsMap.size();
    }

    /**
     * 获取数据项类型
     * <p>
     * Gets the type of the data item.
     * </p>
     */
    @Override
    public int getItemViewType(int position) {
        return getModels().get(position).getType();
    }

    /**
     * 根据类型返回布局资源
     */
    private int getLayoutResByType(@SettingType int type) {
        switch (type) {
            case SettingType.HEADER:
                return R.layout.layout_setting_header;
            case SettingType.BUTTON:
                return R.layout.layout_setting_button;
            case SettingType.TEXT:
                return R.layout.layout_setting_text;
            case SettingType.SWITCH:
                return R.layout.layout_setting_switch;
            case SettingType.SELECTOR:
                return R.layout.layout_setting_selector;
            default:
                return android.R.layout.simple_list_item_1;
        }
    }

    /**
     * 通用的设置项 ViewHolder
     */
    static class SettingViewHolder extends RecyclerView.ViewHolder {
        private final ISettingBinder binder;

        SettingViewHolder(@NonNull View itemView, ISettingBinder binder) {
            super(itemView);
            this.binder = binder;
        }

        /**
         * 绑定数据
         */
        void bind(SettingModel model, IBinderContext context) {
            if (binder != null) {
                binder.bind(itemView, model, context);
            }
            itemView.setEnabled(model.isEnabled());
        }
    }
}
