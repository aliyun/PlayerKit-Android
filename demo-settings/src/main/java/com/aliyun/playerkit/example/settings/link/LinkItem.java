package com.aliyun.playerkit.example.settings.link;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * 链接配置项数据模型
 * <p>
 * 用于存储单个链接配置项的数据，包括显示名称、存储键名以及链接内容。
 * 支持视频 URL、Vid、PlayAuth、直播地址等多种类型的链接配置。
 * </p>
 * <p>
 * Link Configuration Item Data Model
 * <p>
 * Used to store data for a single link configuration item, including display name,
 * storage key name, and link content. Supports various types of link configurations
 * such as video URL, Vid, PlayAuth, live stream address, etc.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class LinkItem {

    /**
     * 配置项名称资源 ID（用于显示在 UI 上）
     */
    @StringRes
    private final int nameResId;

    /**
     * 存储键名（对应 SharedPreferences 中的 Key，用于数据持久化）
     */
    private final String key;

    /**
     * 当前配置的链接或 ID 内容（用户输入或扫码获取）
     */
    private String link;

    /**
     * 构造函数
     * <p>
     * 使用默认空字符串作为初始链接内容。
     * </p>
     *
     * @param nameResId 名称资源 ID
     * @param key       存储键名
     */
    public LinkItem(int nameResId, String key) {
        this(nameResId, key, "");
    }

    /**
     * 构造函数
     *
     * @param nameResId 名称资源 ID
     * @param key       存储键名
     * @param link      初始链接内容
     */
    public LinkItem(int nameResId, String key, String link) {
        this.nameResId = nameResId;
        this.key = key;
        this.link = link;
    }

    /**
     * 获取名称资源 ID
     *
     * @return 字符串资源 ID
     */
    public int getNameResId() {
        return nameResId;
    }

    /**
     * 获取存储键名
     *
     * @return 键名字符串
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取链接内容
     *
     * @return 链接字符串
     */
    public String getLink() {
        return link;
    }

    /**
     * 设置链接内容
     * <p>
     * 更新链接内容，通常在用户输入或扫码后调用。
     * </p>
     *
     * @param link 新的链接内容
     */
    public void setLink(String link) {
        this.link = link;
    }

    @NonNull
    @Override
    public String toString() {
        return "LinkItem{" +
                "nameResId=" + nameResId +
                ", key='" + key + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
