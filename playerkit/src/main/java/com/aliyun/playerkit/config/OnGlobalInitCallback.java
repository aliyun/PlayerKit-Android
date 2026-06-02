package com.aliyun.playerkit.config;

/**
 * 全局初始化自定义配置回调。
 *
 * <p>在 {@code AliPlayerKit.init()} 完成内部初始化后触发，仅执行一次。
 * 适用于全局级 SDK 配置，如 setOption（全局日志级别）、mediaLoader 等。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * AliPlayerKit.setOnGlobalInit(() -> {
 *     // 设置全局 Option
 *     AliPlayerGlobalSettings.setOption(...);
 * });
 * AliPlayerKit.init(context);
 * </pre>
 */
@FunctionalInterface
public interface OnGlobalInitCallback {
    /**
     * 全局初始化完成后的配置回调。
     */
    void onGlobalInit();
}
