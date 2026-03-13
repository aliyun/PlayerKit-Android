package com.aliyun.playerkit.example.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.data.PlayerViewType;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.example.settings.formatter.LogLevelFormatter;
import com.aliyun.playerkit.example.settings.formatter.PlayerViewTypeFormatter;
import com.aliyun.playerkit.example.settings.link.LinkActivity;
import com.aliyun.playerkit.example.settings.storage.SettingKeys;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置页面 Activity
 * <p>
 * 职责：作为设置模块的入口和声明式控制器。负责定义所有的设置项模板、处理异步数据加载触发、以及分发全局的用户交互事件。
 * </p>
 * <p>
 * Setting Activity
 * <p>
 * Responsibility:
 * Acts as the entry point and declarative controller for the settings module.
 * Responsible for defining all setting item templates, handling asynchronous data loading triggers, and dispatching global user interaction events.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class SettingActivity extends AppCompatActivity {

    // 通用设置项 ID
    private static final String ID_HEADER_GENERAL = "header_general";
    private static final String ID_LINK_SETTING = "link_setting";
    private static final String ID_PLAYER_VIEW_TYPE = "player_view_type";
    private static final String ID_DEBUG_MODE = "debug_mode";
    private static final String ID_DISABLE_SCREENSHOT = "disable_screenshot";

    // 日志设置项 ID
    private static final String ID_HEADER_LOGS = "header_logs";
    private static final String ID_LOG_PANEL_SWITCH = "log_panel_switch";
    private static final String ID_LOG_LEVEL_SELECTOR = "log_level_selector";

    // 工具设置项 ID
    private static final String ID_HEADER_TOOLS = "header_tools";
    private static final String ID_MOBILE_OPS = "mobile_ops";

    // 版本设置项 ID
    private static final String ID_HEADER_VERSIONS = "header_versions";
    private static final String ID_DEVICE_ID = "device_id";
    private static final String ID_SDK_VERSION = "sdk_version";
    private static final String ID_PLAYER_KIT_VERSION = "player_kit_version";
    private static final String ID_PLAYER_KIT_BUILD = "player_kit_build";

    // 其他设置项 ID
    private static final String ID_HEADER_OTHERS = "header_others";
    private static final String ID_CLEAR_CACHE = "clear_cache";
    private static final String ID_RESET_DEFAULT = "reset_default";

    // 掌上运维页面 URI
    private static final String URI_MOBILE_OPS = "playerkit://examples/mobileops";

    // 全局静态 Handler，绑定主线程 Looper，只创建一次
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    // 应用退出延迟时间
    private static final long KILL_APP_AFTER_TIME_MS = 2 * 1000;

    // 设置项适配器
    private SettingAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setupUI();
        loadSettings();
    }

    private void setupUI() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SettingAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 加载所有设置项
     */
    private void loadSettings() {
        List<SettingModel> models = new ArrayList<>();

        // 分段构建设置项，保证结构清晰
        models.addAll(buildGeneralSettings());
        models.addAll(buildLogSettings());
        models.addAll(buildToolSettings());
        models.addAll(buildVersionSettings());
        models.addAll(buildOtherSettings());

        mAdapter.setItems(models);

        // 自动触发异步加载
        for (SettingModel model : models) {
            if (model.getAsyncLoader() != null) {
                model.loadAsync(result -> mAdapter.updateItemValue(model.getId(), result));
            }
        }
    }

    /**
     * 构建通用设置相关的项（清缓存、链接设置等）
     */
    @NonNull
    private List<SettingModel> buildGeneralSettings() {
        List<SettingModel> models = new ArrayList<>();

        // 通用设置
        models.add(SettingModel.header(ID_HEADER_GENERAL, getString(R.string.setting_header_general)).build());

        models.add(SettingModel.buttonArrow(ID_LINK_SETTING, getString(R.string.setting_link_setting))
                .onClick(model -> {
                    Intent intent = new Intent(this, LinkActivity.class);
                    startActivity(intent);
                })
                .build());

        // Debug 模式开关
        boolean enableDebugMode = SPManager.getInstance().getBool(SettingKeys.KEY_DEBUG_MODE, AliPlayerKit.isDebugModeEnabled());
        models.add(SettingModel.switchItem(ID_DEBUG_MODE, getString(R.string.setting_debug_mode_title), enableDebugMode)
                .onValueChanged((SettingModel m, Boolean checked) -> {
                    // 保存到 SPManager
                    SPManager.getInstance().saveBool(SettingKeys.KEY_DEBUG_MODE, checked);
                    // 应用到 AliPlayerKit
                    AliPlayerKit.setDebugModeEnabled(checked);
                })
                .build());

        // 禁止截屏开关
        boolean disableScreenshot = SPManager.getInstance().getBool(SettingKeys.KEY_DISABLE_SCREENSHOT, AliPlayerKit.isDisableScreenshot());
        models.add(SettingModel.switchItem(ID_DISABLE_SCREENSHOT, getString(R.string.setting_disable_screenshot_title), disableScreenshot)
                .onValueChanged((SettingModel m, Boolean checked) -> {
                    // 保存到 SPManager
                    SPManager.getInstance().saveBool(SettingKeys.KEY_DISABLE_SCREENSHOT, checked);
                    // 应用到 AliPlayerKit
                    AliPlayerKit.setDisableScreenshot(checked);
                })
                .build());

        // 播放器视图类型选择器（使用格式化器，直接传入值类型数组）
        PlayerViewTypeFormatter viewTypeFormatter = new PlayerViewTypeFormatter();
        PlayerViewType[] viewTypeOptions = {
                PlayerViewType.DISPLAY_VIEW,
                PlayerViewType.SURFACE_VIEW,
                PlayerViewType.TEXTURE_VIEW,
        };
        models.add(SettingModel.selectorWithFormatter(ID_PLAYER_VIEW_TYPE,
                        getString(R.string.setting_view_type_title),
                        viewTypeOptions,
                        viewTypeFormatter)
                .onValueChangedWithFormatter((SettingModel m, PlayerViewType viewType) -> {
                    // 保存到 SPManager
                    SPManager.getInstance().saveString(SettingKeys.KEY_PLAYER_VIEW_TYPE, viewType.name());
                    // 应用到 AliPlayerKit
                    AliPlayerKit.setPlayerViewType(viewType);
                })
                .asyncLoaderWithFormatter(() -> {
                    // 从 SPManager 读取，如果没有则使用当前值
                    String savedType = SPManager.getInstance().getString(SettingKeys.KEY_PLAYER_VIEW_TYPE);
                    if (savedType != null) {
                        try {
                            return PlayerViewType.valueOf(savedType);
                        } catch (IllegalArgumentException e) {
                            return AliPlayerKit.getPlayerViewType();
                        }
                    } else {
                        return AliPlayerKit.getPlayerViewType();
                    }
                })
                .build());

        return models;
    }

    /**
     * 构建日志相关的设置项（Log Panel 开关 + 日志等级）
     */
    @NonNull
    private List<SettingModel> buildLogSettings() {
        List<SettingModel> models = new ArrayList<>();

        // 日志设置
        models.add(SettingModel.header(ID_HEADER_LOGS, getString(R.string.setting_header_log)).build());

        // 日志面板开关：控制 LogPanelSlot 是否显示
        boolean defaultEnableLogPanel = AliPlayerKit.isLogPanelEnabled();
        boolean enableLogPanel = SPManager.getInstance().getBool(SettingKeys.KEY_ENABLE_LOG_PANEL, defaultEnableLogPanel);
        models.add(SettingModel.switchItem(ID_LOG_PANEL_SWITCH, getString(R.string.setting_log_panel_title), enableLogPanel)
                .onValueChanged((SettingModel m, Boolean checked) -> {
                    // 保存到 SPManager
                    SPManager.getInstance().saveBool(SettingKeys.KEY_ENABLE_LOG_PANEL, checked);
                    // 应用到 AliPlayerKit
                    AliPlayerKit.setLogPanelEnabled(checked);
                })
                .build());

        // 日志等级选择器（使用格式化器，直接传入值类型数组）
        LogLevelFormatter logLevelFormatter = new LogLevelFormatter();
        int[] logLevelValues = LogHub.getLogLevelValues();
        // 需要装箱
        Integer[] levelValueOptions = new Integer[logLevelValues.length];
        for (int i = 0; i < logLevelValues.length; i++) {
            levelValueOptions[i] = logLevelValues[i];
        }
        models.add(SettingModel.selectorWithFormatter(ID_LOG_LEVEL_SELECTOR,
                        getString(R.string.setting_log_level_title),
                        levelValueOptions,
                        logLevelFormatter)
                .onValueChangedWithFormatter((SettingModel m, Integer logLevel) -> {
                    // 保存到 SPManager
                    SPManager.getInstance().saveInt(SettingKeys.KEY_LOG_LEVEL, logLevel);
                    // 应用到 LogHub
                    LogHub.setLogLevel(logLevel);
                })
                .asyncLoaderWithFormatter(() -> {
                    // 从 SPManager 读取，如果没有则使用当前值
                    return SPManager.getInstance().getInt(SettingKeys.KEY_LOG_LEVEL, LogHub.getLogLevel());
                })
                .build());

        return models;
    }

    /**
     * 构建工具相关的设置项
     */
    @NonNull
    private List<SettingModel> buildToolSettings() {
        List<SettingModel> models = new ArrayList<>();

        // 工具设置
        models.add(SettingModel.header(ID_HEADER_TOOLS, getString(R.string.setting_header_tools)).build());

        // 掌上运维
        models.add(SettingModel.buttonArrow(ID_MOBILE_OPS, getString(R.string.setting_mobile_ops))
                .onClick(model -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(URI_MOBILE_OPS));
                    startActivity(intent);
                })
                .build());

        return models;
    }

    /**
     * 构建版本信息相关的设置项
     */
    @NonNull
    private List<SettingModel> buildVersionSettings() {
        List<SettingModel> models = new ArrayList<>();

        // 版本信息
        models.add(SettingModel.header(ID_HEADER_VERSIONS, getString(R.string.setting_header_version)).build());

        models.add(SettingModel.text(ID_DEVICE_ID, getString(R.string.setting_device_id), null)
                .asyncLoader(AliPlayerKit::getDeviceId)
                .build());

        models.add(SettingModel.text(ID_SDK_VERSION, getString(R.string.setting_sdk_version), null)
                .asyncLoader(AliPlayerKit::getSdkVersion)
                .build());

        models.add(SettingModel.text(ID_PLAYER_KIT_VERSION, getString(R.string.setting_playerkit_version), null)
                .asyncLoader(AliPlayerKit::getPlayerKitVersion)
                .build());

        models.add(SettingModel.text(ID_PLAYER_KIT_BUILD, getString(R.string.setting_playerkit_build_info), null)
                .asyncLoader(SettingActivity::getBuildInfo)
                .build());

        return models;
    }

    /**
     * 构建其它设置项（清除缓存、恢复默认配置）
     */
    @NonNull
    private List<SettingModel> buildOtherSettings() {
        List<SettingModel> models = new ArrayList<>();

        // 其它
        models.add(SettingModel.header(ID_HEADER_OTHERS, getString(R.string.setting_header_others)).build());

        // 恢复默认配置
        models.add(SettingModel.button(ID_RESET_DEFAULT, getString(R.string.setting_reset_default)).onClick(model -> {
            // 恢复所有设置
            SPManager.getInstance().clearAll();
            ToastUtils.showToast(R.string.setting_toast_reset_default);
            // 2s 后杀掉 App
            sMainHandler.postDelayed(SettingActivity::killApp, KILL_APP_AFTER_TIME_MS);
        }).build());

        models.add(SettingModel.button(ID_CLEAR_CACHE, getString(R.string.setting_clear_cache))
                .onClick(model -> {
                    // 清除缓存
                    AliPlayerKit.clearCaches();
                    ToastUtils.showToast(R.string.setting_toast_clear_cache);
                })
                .build());

        return models;
    }

    /**
     * 获取 App 构建信息。
     * <p>
     * 构建信息由构建标识和构建时间组成，使用 '/' 作为分隔符，
     * 便于日志打印、问题定位以及人工快速识别。
     * </p>
     *
     * <pre>
     * 格式：buildId/buildTimestamp
     * 示例：localbuild/260115101902
     * </pre>
     *
     * @return 构建信息字符串，格式为 buildId/buildTimestamp
     */
    @NonNull
    private static String getBuildInfo() {
        String[] supportedAbis = Build.SUPPORTED_ABIS;
        String abi = (supportedAbis != null && supportedAbis.length > 0) ? supportedAbis[0] : "unknown";
        return BuildConfig.MTL_BUILD_ID + "/" + BuildConfig.MTL_BUILD_TIMESTAMP + "/" + abi;
    }

    /**
     * 杀掉 App
     */
    private static void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
