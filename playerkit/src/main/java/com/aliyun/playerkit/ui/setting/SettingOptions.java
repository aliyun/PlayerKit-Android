package com.aliyun.playerkit.ui.setting;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 选择器选项源（只读）。
 * <p>
 * 用于为选择器类型设置项提供可选值集合（按索引访问），可由 {@link List}、对象数组或 {@code int[]} 构造。
 * </p>
 *
 * <p>
 * Read-only options provider.
 * Provides index-based access to available options for selector-type setting items.
 * Options can be backed by a {@link List}, an object array, or an {@code int[]} (with lazy boxing).
 * </p>
 *
 * @param <T> 选项值类型 / Option value type
 * @author keria
 * @date 2025/12/25
 */
public interface SettingOptions<T> {

    /**
     * 选项数量
     * <p>
     * Option count.
     */
    int size();

    /**
     * 获取指定索引的选项值
     * <p>
     * Returns the option value at the given index.
     */
    T get(int index);

    /**
     * 将选项转换为对话框展示用的字符串数组。
     *
     * @param formatter 值格式化器 / Value formatter
     * @return 展示文本数组 / Display text array
     */
    @NonNull
    default String[] toDisplayArray(@NonNull SettingItem.ValueFormatter<T> formatter) {
        int n = size();
        String[] arr = new String[n];
        for (int i = 0; i < n; i++) {
            arr[i] = formatter.format(get(i));
        }
        return arr;
    }

    /**
     * 由 List 创建选项源（只读视图）。
     * <p>
     * Creates options from a List (read-only view).
     * </p>
     */
    @NonNull
    static <T> SettingOptions<T> of(@NonNull List<T> list) {
        return new ListOptions<>(list);
    }

    /**
     * 由 int[] 创建选项源（惰性装箱）。
     * <p>
     * Creates options from an int array (lazy boxing).
     * </p>
     */
    @NonNull
    static SettingOptions<Integer> of(@NonNull int[] array) {
        return new IntArrayOptions(array);
    }

    /**
     * 由对象数组创建选项源（只读）。
     * <p>
     * Creates options from an object array (read-only).
     * </p>
     */
    @NonNull
    static <T> SettingOptions<T> of(@NonNull T[] array) {
        return new ObjectArrayOptions<>(array);
    }

    /**
     * 对象数组适配实现（只读）
     * <p>
     * Object-array backed implementation (read-only).
     */
    final class ObjectArrayOptions<T> implements SettingOptions<T> {
        private final T[] array;

        /**
         * 创建对象数组适配实现。
         * <p>
         * Creates an object-array backed implementation.
         * </p>
         */
        private ObjectArrayOptions(@NonNull T[] array) {
            this.array = array;
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public T get(int index) {
            return array[index];
        }
    }

    /**
     * List 适配实现（只读）。
     * <p>
     * List backed implementation (read-only).
     */
    final class ListOptions<T> implements SettingOptions<T> {
        private final List<T> list;

        /**
         * 创建 List 适配实现。
         * <p>
         * Creates a List-backed implementation.
         * </p>
         */
        private ListOptions(@NonNull List<T> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }
    }

    /**
     * int[] 适配实现（只读）。
     * <p>
     * int-array backed implementation (read-only).
     */
    final class IntArrayOptions implements SettingOptions<Integer> {
        private final int[] array;

        /**
         * 创建 int[] 适配实现。
         * <p>
         * Creates an int-array backed implementation.
         * </p>
         */
        private IntArrayOptions(@NonNull int[] array) {
            this.array = array;
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public Integer get(int index) {
            return array[index];
        } // lazy boxing
    }
}
