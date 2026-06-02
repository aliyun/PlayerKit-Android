package com.aliyun.playerkit.slot;

/**
 * AliPlayerKit 插槽元素常量定义
 * <p>
 * 定义各插槽内部子元素的 key 常量，配合 {@link SlotManager#hideElements}
 * 使用，实现对默认插槽内单个 UI 元素的细粒度可见性控制。
 * </p>
 * <p>
 * Slot Element Constants
 * <p>
 * Defines key constants for sub-elements within each slot, used with
 * {@link SlotManager#hideElements} to achieve
 * fine-grained visibility control of individual UI elements within default slots.
 * </p>
 *
 * @author keria
 * @date 2026/05/10
 */
public final class SlotElements {

    private SlotElements() {
        throw new UnsupportedOperationException("Cannot instantiate SlotElements");
    }

    /**
     * 顶部栏元素 key 常量
     * <p>
     * Top bar element key constants.
     * </p>
     */
    public static final class TopBar {
        private TopBar() {
        }

        /**
         * 返回按钮
         */
        public static final String BACK = "back";
        /**
         * 标题
         */
        public static final String TITLE = "title";
        /**
         * 下载按钮
         */
        public static final String DOWNLOAD = "download";
        /**
         * 截图按钮
         */
        public static final String SNAPSHOT = "snapshot";
        /**
         * 设置按钮
         */
        public static final String SETTINGS = "settings";
    }

    /**
     * 底部栏元素 key 常量
     * <p>
     * Bottom bar element key constants.
     * </p>
     */
    public static final class BottomBar {
        private BottomBar() {
        }

        /**
         * 播放/暂停按钮
         */
        public static final String PLAY = "play";
        /**
         * 进度条
         */
        public static final String PROGRESS = "progress";
        /**
         * 直播刷新按钮
         */
        public static final String REFRESH = "refresh";
        /**
         * 全屏切换按钮
         */
        public static final String FULLSCREEN = "fullscreen";
    }

    /**
     * 设置菜单元素 key 常量
     * <p>
     * Setting menu element key constants.
     * </p>
     */
    public static final class SettingMenu {
        private SettingMenu() {
        }

        /**
         * 倍速
         */
        public static final String SPEED = "speed";
        /**
         * 清晰度
         */
        public static final String TRACK_INFO = "trackInfo";
        /**
         * 循环播放
         */
        public static final String LOOP = "loop";
        /**
         * 静音播放
         */
        public static final String MUTE = "mute";
        /**
         * 镜像模式
         */
        public static final String MIRROR_MODE = "mirrorMode";
        /**
         * 旋转模式
         */
        public static final String ROTATE_MODE = "rotateMode";
        /**
         * 渲染填充模式
         */
        public static final String SCALE_MODE = "scaleMode";
        /**
         * 字幕开关
         */
        public static final String SUBTITLE = "subtitle";
    }

    /**
     * 中心显示元素 key 常量
     * <p>
     * Center display element key constants.
     * </p>
     */
    public static final class CenterDisplay {
        private CenterDisplay() {
        }

        /**
         * 声音滑块
         */
        public static final String VOLUME = "volume";
        /**
         * 亮度滑块
         */
        public static final String BRIGHTNESS = "brightness";
        /**
         * 倍速显示
         */
        public static final String SPEED = "speed";
    }

    /**
     * 播放状态元素 key 常量
     * <p>
     * Play state element key constants.
     * </p>
     */
    public static final class PlayState {
        private PlayState() {
        }

        /**
         * 错误信息
         */
        public static final String ERROR_MESSAGE = "errorMessage";


        /**
         * 加载信息
         */
        public static final String LOAD_MESSAGE = "loadingMessage";
    }

    /**
     * 手势控制元素 key 常量
     * <p>
     * Gesture control element key constants.
     * </p>
     * <p>
     * 注意：这些元素用于控制手势交互的禁用，而非 UI 元素的隐藏。
     * 当某个手势被添加到 SlotManager 的隐藏元素配置中时，该手势将被禁用。
     * </p>
     * <p>
     * Note: These elements control gesture interaction disabling, not UI element hiding.
     * When a gesture is added to SlotManager's hidden elements, that gesture will be disabled.
     * </p>
     */
    public static final class GestureControl {
        private GestureControl() {
        }

        /**
         * 单击手势（显示/隐藏控制栏 + 切换播放状态）
         */
        public static final String SINGLE_TAP = "singleTap";
        /**
         * 双击手势（切换播放状态）
         */
        public static final String DOUBLE_TAP = "doubleTap";
        /**
         * 长按手势（倍速播放）
         */
        public static final String LONG_PRESS = "longPress";
        /**
         * 水平拖动手势（seek 调整进度）
         */
        public static final String HORIZONTAL_DRAG = "horizontalDrag";
        /**
         * 左侧垂直拖动手势（调整亮度）
         */
        public static final String LEFT_VERTICAL_DRAG = "leftVerticalDrag";
        /**
         * 右侧垂直拖动手势（调整音量）
         */
        public static final String RIGHT_VERTICAL_DRAG = "rightVerticalDrag";
    }
}
