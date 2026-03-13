package com.aliyun.playerkit.ui.setting;

import android.content.Context;

import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.player.IMediaPlayer;
import com.aliyun.playerkit.slot.IPlayerControl;
import com.aliyun.playerkit.utils.FormatUtil;
import com.aliyun.playerkit.utils.TrackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 设置菜单常量与默认项构建器。
 * <p>
 * 该类集中管理设置菜单中各项的 key、可选项列表、默认值，并提供创建默认设置项列表的方法。
 * </p>
 * <p>
 * Settings menu constants and default item builder.
 * <p>
 * This utility class holds menu keys, option lists and default values, and provides a factory method
 * to build default setting items.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public final class SettingConstants {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private SettingConstants() {
        throw new UnsupportedOperationException("Cannot instantiate SettingConstants");
    }

    // ------------------------------------------------------------------------
    // Speed
    // ------------------------------------------------------------------------

    /**
     * 播放速度设置项的 key。
     * <p>
     * Key for playback speed item.
     * </p>
     */
    public static final String KEY_SPEED = "speed";

    /**
     * 支持的播放速度取值列表。
     * <p>
     * 列表中的每个值表示播放速度的倍率，
     * {@code 1.0f} 表示正常播放速度。
     * </p>
     *
     * <p>
     * 该列表为不可变集合，仅用于读取，不应被修改。
     * </p>
     *
     * <p>
     * Supported playback speed values.
     * Each value represents a playback speed multiplier,
     * where {@code 1.0f} indicates normal playback speed.
     * </p>
     *
     * <p>
     * This list is immutable and should be treated as read-only.
     * </p>
     */
    public static final List<Float> SPEED_OPTIONS =
            Collections.unmodifiableList(Arrays.asList(
                    0.3f,
                    0.5f,
                    1.0f,
                    1.5f,
                    2.0f,
                    3.0f
            ));

    /**
     * 默认播放速度为 1.0x（正常）。
     * <p>
     * Default playback speed: 1.0x (normal).
     */
    private static final float DEFAULT_SPEED = 1.0f;

    // ------------------------------------------------------------------------
    // Quality
    // ------------------------------------------------------------------------

    /**
     * 视频质量设置项的 key。
     * <p>
     * Key for video quality item.
     * </p>
     */
    public static final String KEY_QUALITY = "quality";

    // ------------------------------------------------------------------------
    // Loop
    // ------------------------------------------------------------------------

    /**
     * 循环播放设置项的 key。
     * <p>
     * Key for loop playback item.
     * </p>
     */
    public static final String KEY_LOOP = "loop";

    /**
     * 默认循环播放：关闭。
     * <p>
     * Default loop playback: disabled.
     * </p>
     */
    public static final boolean DEFAULT_LOOP = false;

    // ------------------------------------------------------------------------
    // Mute
    // ------------------------------------------------------------------------

    /**
     * 静音设置项的 key。
     * <p>
     * Key for mute item.
     * </p>
     */
    public static final String KEY_MUTE = "mute";

    /**
     * 默认静音：关闭。
     * <p>
     * Default mute: disabled.
     * </p>
     */
    public static final boolean DEFAULT_MUTE = false;

    // ------------------------------------------------------------------------
    // Mirror
    // ------------------------------------------------------------------------

    /**
     * 镜像模式设置项的 key。
     * <p>
     * Key for mirror mode item.
     * </p>
     */
    public static final String KEY_MIRROR = "mirror";

    /**
     * 支持的视频镜像模式列表。
     * <p>
     * 镜像模式用于控制视频画面的渲染方向，
     * 可对画面进行水平或垂直翻转。
     * </p>
     *
     * <p>
     * 该列表为不可变集合，仅用于读取，不应被修改。
     * </p>
     *
     * <p>
     * Supported mirror mode values.
     * Mirror mode controls the rendering orientation of the video frame,
     * allowing horizontal or vertical flipping.
     * </p>
     *
     * <p>
     * This list is immutable and should be treated as read-only.
     * </p>
     */
    public static final @IMediaPlayer.MirrorType int[] MIRROR_OPTIONS = {
            IMediaPlayer.MirrorType.NONE,
            IMediaPlayer.MirrorType.HORIZONTAL,
            IMediaPlayer.MirrorType.VERTICAL,
    };

    /**
     * 默认镜像模式：无。
     * <p>
     * Default mirror mode: none.
     * </p>
     */
    public static final @IMediaPlayer.MirrorType int DEFAULT_MIRROR = IMediaPlayer.MirrorType.NONE;

    // ------------------------------------------------------------------------
    // Scale
    // ------------------------------------------------------------------------

    /**
     * 缩放模式设置项的 key。
     * <p>
     * Key for scale mode item.
     * </p>
     */
    public static final String KEY_SCALE = "scale";

    /**
     * 支持的视频缩放模式列表。
     * <p>
     * 缩放模式用于控制视频内容
     * 如何适配并渲染到显示视图中。
     * </p>
     *
     * <p>
     * 该列表为不可变集合，仅用于读取，不应被修改。
     * </p>
     *
     * <p>
     * Supported scale mode values.
     * Scale mode controls how the video content
     * is scaled and rendered into the target view.
     * </p>
     *
     * <p>
     * This list is immutable and should be treated as read-only.
     * </p>
     */
    public static final @IMediaPlayer.ScaleType int[] SCALE_OPTIONS = {
            IMediaPlayer.ScaleType.FIT_XY,
            IMediaPlayer.ScaleType.FIT_CENTER,
            IMediaPlayer.ScaleType.CENTER_CROP,
    };

    /**
     * 默认缩放模式：裁剪填充（CENTER_CROP）。
     * <p>
     * Default scale mode: center crop (CENTER_CROP).
     * </p>
     */
    public static final @IMediaPlayer.ScaleType int DEFAULT_SCALE = IMediaPlayer.ScaleType.CENTER_CROP;

    // ------------------------------------------------------------------------
    // Rotation
    // ------------------------------------------------------------------------

    /**
     * 旋转模式设置项的 key。
     * <p>
     * Key for rotation item.
     * </p>
     */
    public static final String KEY_ROTATE = "rotate";

    /**
     * 支持的视频旋转角度列表（单位：度）。
     * <p>
     * 旋转角度表示对视频画面
     * 按顺时针方向进行旋转。
     * </p>
     *
     * <p>
     * 该列表为不可变集合，仅用于读取，不应被修改。
     * </p>
     *
     * <p>
     * Supported video rotation angle values (in degrees).
     * Rotation angles are applied clockwise
     * to the rendered video frame.
     * </p>
     *
     * <p>
     * This list is immutable and should be treated as read-only.
     * </p>
     */
    public static final @IMediaPlayer.Rotation int[] ROTATE_OPTIONS = {
            IMediaPlayer.Rotation.DEGREE_0,
            IMediaPlayer.Rotation.DEGREE_90,
            IMediaPlayer.Rotation.DEGREE_180,
            IMediaPlayer.Rotation.DEGREE_270,
    };

    /**
     * 默认旋转角度：0 度。
     * <p>
     * Default rotation: 0 degree.
     * </p>
     */
    public static final @IMediaPlayer.Rotation int DEFAULT_ROTATE = IMediaPlayer.Rotation.DEGREE_0;

    // ------------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------------

    /**
     * 创建默认的设置项列表。
     * <p>
     * 根据预定义的 keys、选项列表与默认值创建设置项，并将用户操作回调绑定到 {@link IPlayerControl}。
     * </p>
     * <p>
     * Build default setting items.
     * <p>
     * Creates items with predefined keys/options/defaults and wires user actions to {@link IPlayerControl}.
     * </p>
     *
     * @param context Android context used to resolve string resources.
     * @param control Player control interface used to apply user settings.
     * @return A list of default {@link SettingItem} for the settings menu.
     */
    public static List<SettingItem<?>> createDefaultItems(Context context, IPlayerControl control) {
        List<SettingItem<?>> items = new ArrayList<>();

        // 1. 倍速
        SettingItem<Float> speedItem = new SettingItem<>(KEY_SPEED, context.getString(R.string.setting_item_speed), SettingItemType.SELECTOR, DEFAULT_SPEED);
        speedItem.options = SettingOptions.of(SPEED_OPTIONS);
        speedItem.formatter = value -> value + "x";
        speedItem.listener = (item, newValue) -> control.setSpeed(newValue);
        items.add(speedItem);

        // 2. 清晰度
        // 默认值为 null，因为初始状态下还没有获取到可用的清晰度选项；options 将在运行时动态设置
        SettingItem<TrackQuality> qualityItem = new SettingItem<>(KEY_QUALITY, context.getString(R.string.setting_item_quality), SettingItemType.SELECTOR, null);
        qualityItem.formatter = TrackUtil::findNearestResolution;
        qualityItem.listener = (SettingItem<TrackQuality> item, TrackQuality newValue) -> control.selectTrack(newValue);
        items.add(qualityItem);

        // 3. 循环播放
        SettingItem<Boolean> loopItem = new SettingItem<>(KEY_LOOP, context.getString(R.string.setting_item_loop), SettingItemType.SWITCHER, DEFAULT_LOOP);
        loopItem.listener = (item, newValue) -> control.setLoop(newValue);
        items.add(loopItem);

        // 4. 静音播放
        SettingItem<Boolean> muteItem = new SettingItem<>(KEY_MUTE, context.getString(R.string.setting_item_mute), SettingItemType.SWITCHER, DEFAULT_MUTE);
        muteItem.listener = (item, newValue) -> control.setMute(newValue);
        items.add(muteItem);

        // 5. 镜像模式
        SettingItem<Integer> mirrorItem = new SettingItem<>(KEY_MIRROR, context.getString(R.string.setting_item_mirror), SettingItemType.SELECTOR, DEFAULT_MIRROR);
        mirrorItem.options = SettingOptions.of(MIRROR_OPTIONS);
        mirrorItem.formatter = FormatUtil::formatMirrorType;
        mirrorItem.listener = (item, newValue) -> control.setMirrorType(newValue);
        items.add(mirrorItem);

        // 6. 渲染填充
        SettingItem<Integer> scaleItem = new SettingItem<>(KEY_SCALE, context.getString(R.string.setting_item_scale), SettingItemType.SELECTOR, DEFAULT_SCALE);
        scaleItem.options = SettingOptions.of(SCALE_OPTIONS);
        scaleItem.formatter = FormatUtil::formatScaleType;
        scaleItem.listener = (item, newValue) -> control.setScaleType(newValue);
        items.add(scaleItem);

        // 7. 旋转模式
        SettingItem<Integer> rotateItem = new SettingItem<>(KEY_ROTATE, context.getString(R.string.setting_item_rotate), SettingItemType.SELECTOR, DEFAULT_ROTATE);
        rotateItem.options = SettingOptions.of(ROTATE_OPTIONS);
        rotateItem.formatter = FormatUtil::formatRotation;
        rotateItem.listener = (item, newValue) -> control.setRotation(newValue);
        items.add(rotateItem);

        return items;
    }
}
