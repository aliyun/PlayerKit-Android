package com.aliyun.playerkit.example;

import android.app.Application;
import android.content.Context;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.example.settings.SettingsInitializer;

/**
 * @author keria
 * @date 2025/7/4
 * @brief 自定义 Application 类，用于全局初始化
 */
public class MainApplication extends Application {
    /**
     * attachBaseContext 可用于多语言、插件化等特殊场景的初始化
     * 这里保留父类实现，如需扩展可在此处添加
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 可在此处添加多语言或热修复等初始化代码
    }

    /**
     * 应用启动时调用，适合做全局初始化工作
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 AliPlayerKit 全局设置
        AliPlayerKit.init(this);

        // 初始化设置系统并恢复设置（初始化 SPManager 并恢复日志设置）
        SettingsInitializer.init(this);

        // Note keria: 其他全局初始化可在此扩展
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
