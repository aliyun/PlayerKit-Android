package com.aliyun.playerkit.ui.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.R;

/**
 * 开关类型的设置项视图。
 * <p>
 * 用于展示 {@link SettingItemType#SWITCHER} 类型的设置项，
 * 显示标题与开关状态，并在状态变化时回调 {@link SettingItem.OnValueChangeListener}。
 * </p>
 *
 * <p>
 * View for switcher-type setting items.
 * Displays the title and switch state, and notifies
 * {@link SettingItem.OnValueChangeListener} when the state changes.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public class SettingSwitcherItemView extends LinearLayout implements ISettingItemView<Boolean> {

    // 设置项标题展示
    private TextView mTvTitle;

    // 开关控件
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mSwitchCompat;

    // Current bound item
    private SettingItem<Boolean> mItem;

    public SettingSwitcherItemView(Context context) {
        this(context, null);
    }

    public SettingSwitcherItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingSwitcherItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        LayoutInflater.from(getContext()).inflate(R.layout.item_setting_switcher, this, true);

        // 初始化视图
        mTvTitle = findViewById(R.id.tv_title);
        mSwitchCompat = findViewById(R.id.switch_compat);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    /**
     * 绑定设置项。
     * <p>
     * 该方法将设置项绑定到视图，并设置标题和当前值。
     * </p>
     *
     * <p>
     * Binds the setting item to the view.
     * Sets the title and current value.
     * </p>
     *
     * @param item 设置项 / Setting item
     */
    @Override
    public void bind(@NonNull SettingItem<Boolean> item) {
        mItem = item;

        mTvTitle.setText(item.title);

        // Avoid triggering callback when updating checked state programmatically.
        mSwitchCompat.setOnCheckedChangeListener(null);
        mSwitchCompat.setChecked(item.currentValue);
        mSwitchCompat.setOnCheckedChangeListener(newCheckedListener(item));

        // Support toggling by clicking the whole row.
        setOnClickListener(v -> mSwitchCompat.toggle());
    }

    /**
     * 创建一个新的切换状态监听器。
     *
     * @param item 设置项 / Setting item
     * @return 切换状态监听器 / Checked change listener
     */
    private CompoundButton.OnCheckedChangeListener newCheckedListener(@NonNull SettingItem<Boolean> item) {
        return (buttonView, isChecked) -> {
            item.currentValue = isChecked;
            if (item.listener != null) {
                item.listener.onValueChanged(item, isChecked);
            }
        };
    }

    /**
     * 更新开关状态，但不触发回调。
     * <p>
     * 该方法用于更新开关状态，但不触发回调。
     * </p>
     *
     * <p>
     * Update the switch state without triggering the callback.
     * </p>
     *
     * @param value 新的开关状态 / New switch state
     */
    @Override
    public void updateValueOnly(@NonNull Boolean value) {
        if (mItem != null) {
            mItem.currentValue = value;
            mSwitchCompat.setOnCheckedChangeListener(null);
            mSwitchCompat.setChecked(value);
            mSwitchCompat.setOnCheckedChangeListener(newCheckedListener(mItem));
        }
    }
}
