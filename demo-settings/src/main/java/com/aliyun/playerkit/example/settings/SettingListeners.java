package com.aliyun.playerkit.example.settings;

/**
 * 设置项相关的回调和监听器接口集合
 * <p>
 * Collection of callback and listener interfaces related to setting items.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public final class SettingListeners {

    private SettingListeners() {
        throw new UnsupportedOperationException("Cannot instantiate SettingListeners");
    }

    /**
     * 设置项点击监听器
     */
    public interface OnClickListener {
        /**
         * 点击回调
         *
         * @param model 设置项
         */
        void onClick(SettingModel model);
    }

    /**
     * 设置值变更监听器
     *
     * @param <T> 值类型
     */
    public interface OnValueChangedListener<T> {
        /**
         * 值变更回调
         *
         * @param model    设置项
         * @param newValue 新值
         */
        void onValueChanged(SettingModel model, T newValue);
    }

    /**
     * 异步数据加载逻辑接口
     */
    @FunctionalInterface
    public interface AsyncLoader {
        /**
         * 加载数据
         *
         * @return 加载到的字符串结果
         */
        String load();
    }

    /**
     * 异步加载完成回调接口
     */
    public interface OnLoadedCallback {
        /**
         * 加载完成后调用
         *
         * @param result 加载得到的结果字符串
         */
        void onLoaded(String result);
    }
}
