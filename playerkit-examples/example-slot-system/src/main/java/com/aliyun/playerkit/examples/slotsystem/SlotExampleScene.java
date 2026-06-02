package com.aliyun.playerkit.examples.slotsystem;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.slot.SlotManager;
import com.aliyun.playerkit.slot.SlotType;

/**
 * 演示场景枚举
 * <p>
 * 每个场景对应一组不同的隐藏元素配置，展示细粒度控制的不同使用方式。
 * </p>
 * <p>
 * Demo Scene Enumeration
 * <p>
 * Each scene corresponds to a different hidden elements configuration,
 * demonstrating various ways of fine-grained control.
 * </p>
 */
public enum SlotExampleScene {

    /**
     * 默认场景：所有元素正常显示，所有手势已启用
     */
    DEFAULT(R.string.scene_default_name, R.string.scene_default_desc) {
        @Override
        public void applyHiddenElements(@NonNull SlotManager slotManager) {
            // 不隐藏任何元素
        }
    },

    /**
     * 精简 UI：隐藏顶部栏和底部栏中的部分元素
     * <p>
     * - TopBar：隐藏下载按钮、截图按钮
     * - BottomBar：隐藏进度条
     * </p>
     */
    SIMPLIFIED_UI(R.string.scene_simplified_name, R.string.scene_simplified_desc) {
        @Override
        public void applyHiddenElements(@NonNull SlotManager slotManager) {
            slotManager.hideElements(SlotType.TOP_BAR,
                    SlotElements.TopBar.DOWNLOAD,
                    SlotElements.TopBar.SNAPSHOT);
            slotManager.hideElements(SlotType.BOTTOM_BAR,
                    SlotElements.BottomBar.PROGRESS);
        }
    },

    /**
     * 手势受限：禁用部分手势交互
     * <p>
     * - GestureControl：禁用双击、左侧垂直拖动、右侧垂直拖动手势
     * </p>
     */
    GESTURE_RESTRICTED(R.string.scene_gesture_name, R.string.scene_gesture_desc) {
        @Override
        public void applyHiddenElements(@NonNull SlotManager slotManager) {
            slotManager.hideElements(SlotType.GESTURE_CONTROL,
                    SlotElements.GestureControl.DOUBLE_TAP,
                    SlotElements.GestureControl.LEFT_VERTICAL_DRAG,
                    SlotElements.GestureControl.RIGHT_VERTICAL_DRAG);
        }
    },

    /**
     * 细粒度控制：综合控制，同时隐藏 UI 元素和禁用手势
     * <p>
     * - TopBar：隐藏下载按钮、截图按钮
     * - BottomBar：隐藏进度条
     * - SettingMenu：隐藏倍速、静音
     * - GestureControl：禁用双击、左侧垂直拖动、右侧垂直拖动手势
     * </p>
     */
    FINE_GRAINED(R.string.scene_fine_grained_name, R.string.scene_fine_grained_desc) {
        @Override
        public void applyHiddenElements(@NonNull SlotManager slotManager) {
            slotManager.hideElements(SlotType.TOP_BAR,
                    SlotElements.TopBar.DOWNLOAD,
                    SlotElements.TopBar.SNAPSHOT);
            slotManager.hideElements(SlotType.BOTTOM_BAR,
                    SlotElements.BottomBar.PROGRESS);
            slotManager.hideElements(SlotType.SETTING_MENU,
                    SlotElements.SettingMenu.SPEED,
                    SlotElements.SettingMenu.MUTE);
            slotManager.hideElements(SlotType.GESTURE_CONTROL,
                    SlotElements.GestureControl.DOUBLE_TAP,
                    SlotElements.GestureControl.LEFT_VERTICAL_DRAG,
                    SlotElements.GestureControl.RIGHT_VERTICAL_DRAG);
        }
    };

    /**
     * 场景名称资源 ID
     */
    private final int nameResId;
    /**
     * 场景描述资源 ID
     */
    private final int descResId;

    SlotExampleScene(int nameResId, int descResId) {
        this.nameResId = nameResId;
        this.descResId = descResId;
    }

    /**
     * 获取场景名称
     */
    @NonNull
    public String getName(@NonNull Context context) {
        return context.getString(nameResId);
    }

    /**
     * 获取场景描述
     */
    @NonNull
    public String getDescription(@NonNull Context context) {
        return context.getString(descResId);
    }

    /**
     * 将该场景对应的隐藏元素配置应用到 SlotManager
     *
     * @param slotManager 插槽管理器
     */
    public abstract void applyHiddenElements(@NonNull SlotManager slotManager);
}
