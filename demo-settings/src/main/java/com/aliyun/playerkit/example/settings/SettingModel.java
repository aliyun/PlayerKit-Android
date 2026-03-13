package com.aliyun.playerkit.example.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.example.settings.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置项数据模型
 * <p>
 * 职责：定义设置项的核心数据结构，包括标题、副标题、选中状态、加载状态以及异步加载能力。
 * 采用了 Builder 模式进行构建。
 * </p>
 * <p>
 * Setting Item Data Model
 * <p>
 * Responsibility: Defines the core data structure of a setting item, including title, subtitle, checked state, loading state, and async loading capability.
 * Built using the Builder pattern.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class SettingModel {

    // 唯一标识符
    private final String id;
    // 标题文本
    private final String title;
    // 视图类型
    private final @SettingType int type;

    // 描述文本或当前值
    private String value;
    // 选中状态（仅用于 SWITCH）
    private boolean checked;
    // 可选项列表（仅用于 SELECTOR）
    private List<String> options;
    // 是否启用
    private boolean enabled = true;
    // 是否显示右侧箭头
    private boolean showArrow;

    // 点击回调
    private SettingListeners.OnClickListener onClickListener;
    // 值变更回调
    private SettingListeners.OnValueChangedListener<Object> onValueChangedListener;
    // 异步加载器
    private SettingListeners.AsyncLoader asyncLoader;
    // 值格式化器（用于选择器类型的设置项）
    private ValueFormatter<?> valueFormatter;

    /**
     * 内部构造函数，强制通过 {@link Builder} 或工厂方法创建。
     */
    private SettingModel(@NonNull String id, @NonNull String title, @SettingType int type) {
        this.id = id;
        this.title = title;
        this.type = type;
    }

    /**
     * 获取 ID
     *
     * @return 唯一标识符
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * 获取标题
     *
     * @return 标题文本
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * 获取视图类型
     *
     * @return {@link SettingType}
     */
    public @SettingType int getType() {
        return type;
    }

    /**
     * 是否显示箭头
     *
     * @return true 显示
     */
    public boolean isShowArrow() {
        return showArrow;
    }

    /**
     * 设置是否显示箭头
     *
     * @param showArrow true 显示
     */
    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
    }

    /**
     * 获取值
     *
     * @return 当前展示的摘要或结果
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * 设置值
     *
     * @param value 内容
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取选中状态
     *
     * @return 是否选中
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * 设置选中状态
     *
     * @param checked 是否选中
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    /**
     * 获取可选项
     *
     * @return 选项列表
     */
    @Nullable
    public List<String> getOptions() {
        return options;
    }

    /**
     * 获取启用状态
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取点击监听
     *
     * @return 点击监听器
     */
    @Nullable
    public SettingListeners.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    /**
     * 获取值变更监听
     *
     * @return 值变更监听器
     */
    @Nullable
    public SettingListeners.OnValueChangedListener<Object> getOnValueChangedListener() {
        return onValueChangedListener;
    }

    /**
     * 获取关联的异步加载器
     *
     * @return 异步加载器实例，若无则返回 null。
     */
    @Nullable
    public SettingListeners.AsyncLoader getAsyncLoader() {
        return asyncLoader;
    }

    /**
     * 获取值格式化器
     *
     * @return 值格式化器实例，若无则返回 null
     */
    @Nullable
    public ValueFormatter<?> getValueFormatter() {
        return valueFormatter;
    }

    /**
     * 触发异步加载流程
     * <p>
     * 逻辑内部使用 {@link SettingTaskExecutor} 在后台线程执行加载，完成后切回主线程更新内存模型并通知回调。
     *
     * @param callback 加载完成后的回调接口，可为空。
     */
    public void loadAsync(@Nullable SettingListeners.OnLoadedCallback callback) {
        if (asyncLoader != null) {
            SettingTaskExecutor.runOnBackground(() -> {
                final String result = asyncLoader.load();
                SettingTaskExecutor.runOnMainThread(() -> {
                    this.value = result;
                    if (callback != null) {
                        callback.onLoaded(result);
                    }
                });
            });
        }
    }

    // --- 语义化静态工厂方法 ---

    /**
     * 创建头布局项
     *
     * @param id    ID
     * @param title 标题
     * @return Builder
     */
    public static Builder header(@NonNull String id, @NonNull String title) {
        return new Builder(id, title, SettingType.HEADER);
    }

    /**
     * 创建普通按钮项（不带箭头）
     *
     * @param id    ID
     * @param title 标题
     * @return Builder
     */
    public static Builder button(@NonNull String id, @NonNull String title) {
        return new Builder(id, title, SettingType.BUTTON);
    }

    /**
     * 创建带箭头的按钮项
     *
     * @param id    ID
     * @param title 标题
     * @return Builder
     */
    public static Builder buttonArrow(@NonNull String id, @NonNull String title) {
        return new Builder(id, title, SettingType.BUTTON).showArrow(true);
    }

    /**
     * 创建纯文本项
     *
     * @param id    ID
     * @param title 标题
     * @param value 值
     * @return Builder
     */
    public static Builder text(@NonNull String id, @NonNull String title, @Nullable String value) {
        return new Builder(id, title, SettingType.TEXT).value(value);
    }

    /**
     * 创建开关项
     *
     * @param id      ID
     * @param title   标题
     * @param checked 初始选中状态
     * @return Builder
     */
    public static Builder switchItem(@NonNull String id, @NonNull String title, boolean checked) {
        return new Builder(id, title, SettingType.SWITCH).checked(checked);
    }

    /**
     * 创建选择器项
     *
     * @param id           ID
     * @param title        标题
     * @param currentValue 当前选中项显示
     * @param options      选项列表
     * @return Builder
     */
    public static Builder selector(@NonNull String id, @NonNull String title, @Nullable String currentValue, @NonNull List<String> options) {
        return new Builder(id, title, SettingType.SELECTOR).value(currentValue).options(options);
    }

    /**
     * 创建带格式化器的选择器项（使用值类型数组）
     * <p>
     * 用于需要将值类型（如枚举、整数）与显示字符串进行转换的场景。
     * 格式化器会自动处理值类型和显示字符串之间的双向转换。
     * 此方法接收值类型数组，内部会自动通过格式化器转换为显示字符串列表。
     * 如果格式化器为 null，则使用值的 toString() 方法转换为字符串。
     * </p>
     * <p>
     * Create selector item with formatter (using value type array)
     * <p>
     * Used for scenarios where value types (such as enums, integers) need to be converted to/from display strings.
     * The formatter automatically handles bidirectional conversion between value types and display strings.
     * This method accepts a value type array and automatically converts it to display string list via formatter.
     * If formatter is null, uses toString() method to convert values to strings.
     * </p>
     *
     * @param id           ID
     * @param title        标题
     * @param valueOptions 值类型选项数组（如 PlayerViewType[]）
     * @param formatter    值格式化器，可以为 null（如果为 null，则使用 toString() 转换）
     * @param <T>          值类型
     * @return Builder
     */
    public static <T> Builder selectorWithFormatter(@NonNull String id, @NonNull String title, @NonNull T[] valueOptions, @Nullable ValueFormatter<T> formatter) {
        // 将值类型数组转换为显示字符串列表
        List<String> displayOptions = new ArrayList<>();
        if (formatter != null) {
            // 使用格式化器转换
            for (T value : valueOptions) {
                displayOptions.add(formatter.format(value));
            }
        } else {
            // 无格式化器，使用 toString() 转换
            for (T value : valueOptions) {
                displayOptions.add(value != null ? value.toString() : "");
            }
        }
        return new Builder(id, title, SettingType.SELECTOR).options(displayOptions).valueFormatter(formatter);
    }

    /**
     * 创建带格式化器的选择器项（使用显示字符串列表）
     * <p>
     * 用于需要将值类型（如枚举、整数）与显示字符串进行转换的场景。
     * 格式化器会自动处理值类型和显示字符串之间的双向转换。
     * 如果不需要格式化器，可以传递 null，此时将直接使用字符串值。
     * </p>
     * <p>
     * Create selector item with formatter (using display string list)
     * <p>
     * Used for scenarios where value types (such as enums, integers) need to be converted to/from display strings.
     * The formatter automatically handles bidirectional conversion between value types and display strings.
     * If no formatter is needed, pass null, and string values will be used directly.
     * </p>
     *
     * @param id        ID
     * @param title     标题
     * @param options   选项列表（显示字符串）
     * @param formatter 值格式化器，可以为 null（如果为 null，则直接使用字符串值）
     * @param <T>       值类型
     * @return Builder
     */
    public static <T> Builder selectorWithFormatter(@NonNull String id, @NonNull String title, @NonNull List<String> options, @Nullable ValueFormatter<T> formatter) {
        return new Builder(id, title, SettingType.SELECTOR).options(options).valueFormatter(formatter);
    }

    /**
     * 设置项流式构建器
     * <p>
     * Builder for {@link SettingModel}.
     * </p>
     */
    public static class Builder {
        private final String id;
        private final String title;
        private final @SettingType int type;
        private String value;
        private boolean showArrow;
        private boolean checked;
        private List<String> options;
        private SettingListeners.OnClickListener onClickListener;
        private SettingListeners.OnValueChangedListener<Object> onValueChangedListener;
        private SettingListeners.AsyncLoader asyncLoader;
        private ValueFormatter<?> valueFormatter;

        /**
         * 构造器构造函数
         *
         * @param id    ID
         * @param title 标题
         * @param type  类型
         */
        public Builder(@NonNull String id, @NonNull String title, @SettingType int type) {
            this.id = id;
            this.title = title;
            this.type = type;
        }

        /**
         * 设置值
         *
         * @param value 值
         * @return Builder
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * 设置是否显示箭头
         *
         * @param showArrow 是否显示
         * @return Builder
         */
        public Builder showArrow(boolean showArrow) {
            this.showArrow = showArrow;
            return this;
        }

        /**
         * 设置选中
         *
         * @param checked 选中
         * @return Builder
         */
        public Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }

        /**
         * 设置可选项
         *
         * @param options 列表
         * @return Builder
         */
        public Builder options(List<String> options) {
            this.options = options;
            return this;
        }

        /**
         * 设置点击回调
         *
         * @param listener 监听
         * @return Builder
         */
        public Builder onClick(SettingListeners.OnClickListener listener) {
            this.onClickListener = listener;
            return this;
        }

        /**
         * 设置值变更回调
         *
         * @param listener 监听
         * @param <T>      值类型
         * @return Builder
         */
        @SuppressWarnings("unchecked")
        public <T> Builder onValueChanged(SettingListeners.OnValueChangedListener<T> listener) {
            this.onValueChangedListener = (SettingListeners.OnValueChangedListener<Object>) listener;
            return this;
        }

        /**
         * 设置异步加载逻辑
         *
         * @param loader 加载器
         * @return Builder
         */
        public Builder asyncLoader(SettingListeners.AsyncLoader loader) {
            this.asyncLoader = loader;
            return this;
        }

        /**
         * 设置值格式化器
         * <p>
         * 用于选择器类型的设置项，自动处理值类型和显示字符串之间的转换。
         * 如果为 null，则直接使用字符串值，不进行转换。
         * </p>
         * <p>
         * Set value formatter
         * <p>
         * Used for selector-type setting items to automatically handle conversion between value types and display strings.
         * If null, string values are used directly without conversion.
         * </p>
         *
         * @param formatter 值格式化器，可以为 null
         * @param <T>       值类型
         * @return Builder
         */
        public <T> Builder valueFormatter(@Nullable ValueFormatter<T> formatter) {
            this.valueFormatter = formatter;
            return this;
        }

        /**
         * 设置值变更回调（带格式化器）
         * <p>
         * 当设置了格式化器时，此方法会自动将显示字符串转换为值类型，然后调用回调。
         * 如果没有格式化器，则直接传递字符串值。
         * </p>
         * <p>
         * Set value changed callback (with formatter)
         * <p>
         * When a formatter is set, this method automatically converts display strings to value types, then calls the callback.
         * If no formatter is set, the string value is passed directly.
         * </p>
         *
         * @param listener 值变更监听器，接收值类型 T（如果有格式化器）或 String（如果没有格式化器）
         * @param <T>      值类型
         * @return Builder
         */
        @SuppressWarnings("unchecked")
        public <T> Builder onValueChangedWithFormatter(@NonNull SettingListeners.OnValueChangedListener<T> listener) {
            if (valueFormatter != null) {
                // 有格式化器：将显示字符串转换为值类型
                ValueFormatter<T> formatter = (ValueFormatter<T>) valueFormatter;
                this.onValueChangedListener = (model, newValue) -> {
                    T value = formatter.parse((String) newValue);
                    listener.onValueChanged(model, value);
                };
            } else {
                // 无格式化器：直接传递字符串值
                this.onValueChangedListener = (model, newValue) -> {
                    listener.onValueChanged(model, (T) newValue);
                };
            }
            return this;
        }

        /**
         * 设置异步加载器（带格式化器）
         * <p>
         * 当设置了格式化器时，此方法会自动将值类型转换为显示字符串。
         * 如果没有格式化器，则直接返回字符串值。
         * </p>
         * <p>
         * Set async loader (with formatter)
         * <p>
         * When a formatter is set, this method automatically converts value types to display strings.
         * If no formatter is set, the string value is returned directly.
         * </p>
         *
         * @param valueLoader 值加载器，返回值类型 T（如果有格式化器）或 String（如果没有格式化器）
         * @param <T>         值类型
         * @return Builder
         */
        @SuppressWarnings("unchecked")
        public <T> Builder asyncLoaderWithFormatter(@NonNull ValueLoader<T> valueLoader) {
            if (valueFormatter != null) {
                // 有格式化器：将值类型转换为显示字符串
                ValueFormatter<T> formatter = (ValueFormatter<T>) valueFormatter;
                this.asyncLoader = () -> {
                    T value = valueLoader.load();
                    return formatter.format(value);
                };
            } else {
                // 无格式化器：直接返回字符串值
                this.asyncLoader = () -> {
                    T value = valueLoader.load();
                    return value != null ? value.toString() : "";
                };
            }
            return this;
        }

        /**
         * 构建 {@link SettingModel} 实例
         *
         * @return SettingModel 实例
         */
        public SettingModel build() {
            SettingModel model = new SettingModel(id, title, type);
            model.value = value;
            model.showArrow = showArrow;
            model.checked = checked;
            model.options = options;
            model.onClickListener = onClickListener;
            model.onValueChangedListener = onValueChangedListener;
            model.asyncLoader = asyncLoader;
            model.valueFormatter = valueFormatter;
            return model;
        }
    }

    /**
     * 值加载器接口
     * <p>
     * 用于异步加载值类型（T），配合格式化器使用。
     * </p>
     * <p>
     * Value Loader Interface
     * <p>
     * Used for asynchronously loading value types (T), used in conjunction with formatters.
     * </p>
     *
     * @param <T> 值类型
     */
    @FunctionalInterface
    public interface ValueLoader<T> {
        /**
         * 加载值
         *
         * @return 值类型实例
         */
        T load();
    }

}
