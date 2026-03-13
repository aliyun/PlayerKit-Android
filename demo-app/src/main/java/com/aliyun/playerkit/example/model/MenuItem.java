package com.aliyun.playerkit.example.model;

/**
 * Data model for menu items
 * 菜单项的数据模型
 *
 * @author keria
 * @date 2025/5/31
 * @brief Menu item data model
 */
public class MenuItem {
    /**
     * 菜单项图标资源ID
     */
    private final int resId;

    /**
     * 菜单项标题
     */
    private final String title;

    /**
     * 菜单项对应的Schema路径，用于页面跳转
     */
    private final String schema;

    /**
     * 构造函数，创建一个新的菜单项
     *
     * @param resId  菜单项图标资源ID
     * @param title  菜单项标题
     * @param schema 菜单项对应的Schema路径
     */
    public MenuItem(int resId, String title, String schema) {
        this.resId = resId;
        this.title = title;
        this.schema = schema;
    }

    /**
     * 获取菜单项图标资源ID
     *
     * @return 图标资源ID
     */
    public int getResId() {
        return resId;
    }

    /**
     * 获取菜单项标题
     *
     * @return 菜单项标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取菜单项对应的Schema路径
     *
     * @return Schema路径字符串
     */
    public String getSchema() {
        return schema;
    }
}