package com.aliyun.playerkit.ui.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.aliyun.playerkit.R;

/**
 * 选择器类型的设置项视图。
 * <p>
 * 用于展示 {@link SettingItemType#SELECTOR} 类型的 {@link SettingItem}：
 * 显示标题与当前值，并在点击时弹出选项列表供用户选择。
 * </p>
 *
 * <p>
 * View for selector-type setting items.
 * Displays the title and current value of a {@link SettingItemType#SELECTOR} item,
 * and shows an option dialog when clicked.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public class SettingSelectorItemView<T> extends LinearLayout implements ISettingItemView<T> {

    // 设置项标题展示
    private TextView mTvTitle;
    // 设置项当前值展示
    private TextView mTvValue;

    // Current bound item
    private SettingItem<T> mItem;

    public SettingSelectorItemView(Context context) {
        this(context, null);
    }

    public SettingSelectorItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingSelectorItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化视图。
     * <p>
     * 该方法将初始化视图，并设置点击监听器。
     * </p>
     *
     * <p>
     * Initializes the view.
     * This method will initialize the view and set the click listener.
     * </p>
     */
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_setting_selector, this, true);

        // 初始化视图
        mTvTitle = findViewById(R.id.tv_title);
        mTvValue = findViewById(R.id.tv_value);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    /**
     * 绑定设置项数据并刷新 UI。
     * <p>
     * 点击该视图将弹出选择对话框，供用户切换当前值。
     * </p>
     *
     * <p>
     * Binds the setting item and refreshes UI.
     * Clicking this view will show a selection dialog for changing the current value.
     * </p>
     *
     * @param item 设置项 / Setting item
     */
    @Override
    public void bind(@NonNull SettingItem<T> item) {
        mItem = item;

        mTvTitle.setText(item.title);
        mTvValue.setText(item.getDisplayValue());

        setOnClickListener(v -> showSelectionDialog(item));
    }

    /**
     * 显示选择对话框。
     * <p>
     * 该方法将显示一个选择对话框，供用户选择当前值。
     * </p>
     *
     * <p>
     * Shows a selection dialog.
     * This method will show a selection dialog for the user to choose the current value.
     * </p>
     *
     * @param item 待选择的设置项 / Setting item to select
     */
    private void showSelectionDialog(@NonNull SettingItem<T> item) {
        final SettingOptions<T> options = item.options;
        if (options == null || options.size() == 0) {
            return;
        }

        final String[] displayOptions = buildDisplayOptions(item, options);
        new AlertDialog.Builder(getContext())
                .setTitle(item.title)
                .setItems(displayOptions, (dialog, which) -> applySelection(item, options, which))
                .show();
    }

    /**
     * 构建选项显示文本。
     * <p>
     * 该方法将遍历选项列表，将每个选项转换为显示文本。
     * </p>
     *
     * <p>
     * Builds the display text for each option.
     * This method will iterate through the option list and convert each option to display text.
     * </p>
     *
     * @param item    待选择的设置项 / Setting item to select
     * @param options 选项列表 / Option list
     * @return 显示文本列表 / Display text list
     */
    private String[] buildDisplayOptions(@NonNull SettingItem<T> item, @NonNull SettingOptions<T> options) {
        final SettingItem.ValueFormatter<T> formatter = item.formatter != null ? item.formatter : String::valueOf;
        return options.toDisplayArray(formatter);
    }

    /**
     * 应用选择。
     * <p>
     * 该方法将更新当前值并通知监听器。
     * </p>
     *
     * <p>
     * Applies the selection.
     * This method will update the current value and notify the listener.
     * </p>
     *
     * @param item  待选择的设置项 / Setting item to select
     * @param which 选项索引 / Option index
     */
    private void applySelection(@NonNull SettingItem<T> item, @NonNull SettingOptions<T> options, int which) {
        if (which < 0 || which >= options.size()) {
            return;
        }

        final T newValue = options.get(which);
        item.currentValue = newValue;

        // Update UI first for responsiveness.
        mTvValue.setText(item.getDisplayValue());

        if (item.listener != null) {
            item.listener.onValueChanged(item, newValue);
        }
    }

    /**
     * 更新当前值（不触发监听器）。
     * <p>
     * 该方法将更新当前值，但不会触发监听器。
     * </p>
     *
     * <p>
     * Updates the current value (without triggering the listener).
     * This method will update the current value, but will not trigger the listener.
     * </p>
     *
     * @param value 新值 / New value
     */
    @Override
    public void updateValueOnly(@NonNull T value) {
        if (mItem != null) {
            mItem.currentValue = value;
            mTvValue.setText(mItem.getDisplayValue());
        }
    }
}
