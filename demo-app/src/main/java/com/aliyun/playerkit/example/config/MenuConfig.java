package com.aliyun.playerkit.example.config;

import android.content.Context;

import com.aliyun.playerkit.example.R;
import com.aliyun.playerkit.example.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu configuration manager for loading and managing menu items
 * 菜单配置管理，用于加载和管理菜单项
 *
 * @author keria
 * @date 2025/5/31
 * @brief Menu configuration manager
 */
public final class MenuConfig {
    /**
     * 获取应用主菜单项列表
     * 该方法加载并返回所有主菜单项，包括中长视频、设置和调试页面
     *
     * @param context 应用上下文，用于获取字符串资源
     * @return 菜单项列表
     */
    public static List<MenuItem> getMenuItems(Context context) {
        List<MenuItem> items = new ArrayList<>();

        // 中长视频页面
        String longVideoTitle = context.getString(R.string.long_video_page_title);
        String longVideoSchema = context.getString(R.string.long_video_page_schema);
        items.add(new MenuItem(R.drawable.ic_long_video, longVideoTitle, longVideoSchema));

        // 直播页面
        String liveTitle = context.getString(R.string.live_page_title);
        String liveSchema = context.getString(R.string.live_page_schema);
        items.add(new MenuItem(R.drawable.ic_live, liveTitle, liveSchema));

        // 列表播放页面
        String playlistTitle = context.getString(R.string.playlist_page_title);
        String playlistSchema = context.getString(R.string.playlist_page_schema);
        items.add(new MenuItem(R.drawable.ic_playlist, playlistTitle, playlistSchema));

        // 短视频列表播放页面
        String shortVideoTitle = context.getString(R.string.short_video_page_title);
        String shortVideoSchema = context.getString(R.string.short_video_page_schema);
        items.add(new MenuItem(R.drawable.ic_short_video, shortVideoTitle, shortVideoSchema));

        // 视频源使用示例页面
        String videoSourceTitle = context.getString(R.string.video_source_example_page_title);
        String videoSourceSchema = context.getString(R.string.video_source_example_page_schema);
        items.add(new MenuItem(R.drawable.ic_video_source_example, videoSourceTitle, videoSourceSchema));

        // 插槽系统使用示例
        String slotSystemTitle = context.getString(R.string.slot_system_example_page_title);
        String slotSystemSchema = context.getString(R.string.slot_system_example_page_schema);
        items.add(new MenuItem(R.drawable.ic_slot_system_example, slotSystemTitle, slotSystemSchema));

        // 策略系统使用示例
        String strategySystemTitle = context.getString(R.string.strategy_system_example_page_title);
        String strategySystemSchema = context.getString(R.string.strategy_system_example_page_schema);
        items.add(new MenuItem(R.drawable.ic_strategy_system_example, strategySystemTitle, strategySystemSchema));

        // 日志系统使用示例页面
        String logSystemTitle = context.getString(R.string.log_system_example_page_title);
        String logSystemSchema = context.getString(R.string.log_system_example_page_schema);
        items.add(new MenuItem(R.drawable.ic_log_system_example, logSystemTitle, logSystemSchema));

        // 事件系统使用示例页面
        String eventSystemTitle = context.getString(R.string.event_system_example_page_title);
        String eventSystemSchema = context.getString(R.string.event_system_example_page_schema);
        items.add(new MenuItem(R.drawable.ic_event_system_example, eventSystemTitle, eventSystemSchema));

        // 播放器生命周期策略使用示例页面
        String lifecycleStrategyTitle = context.getString(R.string.lifecycle_strategy_example_page_title);
        String lifecycleStrategySchema = context.getString(R.string.lifecycle_strategy_example_page_schema);
        items.add(new MenuItem(R.drawable.ic_strategy_system_example, lifecycleStrategyTitle, lifecycleStrategySchema));

        // 预加载使用示例页面
        String preloadTitle = context.getString(R.string.preload_example_page_title);
        String preloadSchema = context.getString(R.string.preload_example_example_schema);
        items.add(new MenuItem(R.drawable.ic_preload_example, preloadTitle, preloadSchema));

        // 设置页面
        String settingsTitle = context.getString(R.string.settings_page_title);
        String settingsSchema = context.getString(R.string.settings_page_schema);
        items.add(new MenuItem(R.drawable.ic_settings, settingsTitle, settingsSchema));

        return items;
    }
}