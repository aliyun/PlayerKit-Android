package com.aliyun.playerkit.examples.slotsystem;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.slot.CustomSlotType;
import com.aliyun.playerkit.slot.SlotManager;

import com.google.android.material.chip.ChipGroup;

/**
 * 插槽系统使用示例 Activity
 * <p>
 * 演示 PlayerKit 插槽系统的两大核心功能：
 * <ul>
 *     <li><b>细粒度控制</b>：通过 {@link SlotManager#hideElements} 声明式配置，
 *     按需隐藏默认插槽中的 UI 元素或禁用手势交互</li>
 *     <li><b>层级控制</b>：通过 {@link CustomSlotType} 在任意内置插槽之间插入自定义组件，
 *     演示如何定义自定义水印插槽并指定其层级</li>
 * </ul>
 * </p>
 * <p>
 * <b>演示场景</b>：
 * <ul>
 *     <li><b>默认</b>：展示完整的默认播放器 UI，所有元素正常显示</li>
 *     <li><b>精简 UI</b>：隐藏顶部栏/底部栏中的部分元素，展示 UI 元素级别的控制</li>
 *     <li><b>手势受限</b>：禁用部分手势交互，展示手势级别的控制</li>
 *     <li><b>细粒度控制</b>：综合控制，同时隐藏 UI 元素和禁用手势</li>
 * </ul>
 * </p>
 * <p>
 * Slot System Example Activity
 * <p>
 * Demonstrates two core features of the PlayerKit slot system:
 * <ul>
 *     <li><b>Fine-grained control</b>: Declaratively hide UI elements or disable gesture
 *     interactions within default slots via {@link SlotManager#hideElements}</li>
 *     <li><b>Layer control</b>: Insert custom components between any built-in slots via
 *     {@link CustomSlotType}, demonstrating how to define a custom watermark slot with specific layer order</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/05/10
 */
public class SlotSystemExampleActivity extends AppCompatActivity {

    // ============================================================
    // 层级控制：自定义插槽类型定义
    // Layer Control: Custom Slot Type Definition
    // ============================================================

    /**
     * 自定义水印插槽类型
     * <p>
     * order=25 使其位于 GESTURE_CONTROL(20) 和 LANDSCAPE_HINT(30) 之间。
     * </p>
     * <p>
     * Custom watermark slot type.
     * order=25 places it between GESTURE_CONTROL(20) and LANDSCAPE_HINT(30).
     * </p>
     */
    private static final CustomSlotType WATERMARK = new CustomSlotType("watermark", 25);

    // ============================================================
    // 播放器相关
    // ============================================================

    /**
     * 播放器组件视图
     */
    private AliPlayerView playerView;

    /**
     * 播放器控制器
     */
    private AliPlayerController mController;

    // ============================================================
    // UI 控件
    // ============================================================

    /**
     * 场景选择 ChipGroup
     */
    private ChipGroup mChipGroup;

    /**
     * 场景描述文本
     */
    private TextView mDescriptionTv;

    /**
     * 当前模式文本
     */
    private TextView mModeTv;

    // ============================================================
    // 状态
    // ============================================================

    /**
     * 当前演示场景
     */
    private SlotExampleScene mCurrentScene = SlotExampleScene.DEFAULT;

    // ============================================================
    // 生命周期
    // ============================================================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_system_example);

        initViews();
        initPlayerKit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mController != null) {
            mController.destroy();
        }
    }

    // ============================================================
    // 初始化
    // ============================================================

    /**
     * 初始化视图
     * <p>
     * 绑定 UI 控件，设置 ChipGroup 选择监听器，并更新初始 UI 状态。
     * </p>
     */
    private void initViews() {
        // 绑定控件
        mChipGroup = findViewById(R.id.chip_group_scenes);
        mDescriptionTv = findViewById(R.id.tv_scene_description);
        mModeTv = findViewById(R.id.tv_current_mode);

        // 设置 ChipGroup 选择监听
        mChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            SlotExampleScene scene = getSceneFromChipId(checkedId);
            if (scene != null && scene != mCurrentScene) {
                switchScene(scene);
            }
        });

        // 设置默认选中
        mChipGroup.check(R.id.chip_default);

        // 更新初始 UI 状态
        updateUI();
    }

    /**
     * 初始化播放器组件
     * <p>
     * 创建播放器实例，构建播放数据配置，注册自定义插槽，并绑定到 AliPlayerView。
     * </p>
     * <p>
     * Initialize player component.
     * Creates player instance, builds playback data, registers custom slots, and attaches to AliPlayerView.
     * </p>
     */
    private void initPlayerKit() {
        playerView = findViewById(R.id.v_player_kit);

        mController = new AliPlayerController(this);
        AliPlayerModel model = buildPlayerModel();

        // 配置数据并绑定控制器到播放器视图
        // Configure data and attach controller to player view
        mController.configure(model);

        // 注册自定义插槽并应用隐藏元素配置
        SlotManager slotManager = playerView.getSlotManager();
        slotManager.register(WATERMARK, parent -> new WatermarkSlot(parent.getContext()));
        mCurrentScene.applyHiddenElements(slotManager);

        playerView.attach(mController);
    }

    // ============================================================
    // 播放器数据构建
    // ============================================================

    /**
     * 构建播放器数据模型
     */
    private AliPlayerModel buildPlayerModel() {
        VideoSource.VidAuthSource videoSource = VideoSourceFactory.createVidAuthSource(
                SceneConstants.LANDSCAPE_SAMPLE_VID,
                SceneConstants.LANDSCAPE_SAMPLE_PLAY_AUTH
        );

        AliPlayerModel.Builder builder = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle(getString(R.string.slot_demo_video_title));

        return builder.build();
    }

    // ============================================================
    // 场景切换
    // ============================================================

    /**
     * 切换演示场景
     * <p>
     * 切换场景时需要重建播放器，并通过 SlotManager 应用新的隐藏元素配置。
     * </p>
     *
     * @param scene 目标演示场景
     */
    private void switchScene(@NonNull SlotExampleScene scene) {
        if (playerView == null) {
            return;
        }

        mCurrentScene = scene;

        // 销毁旧控制器
        if (mController != null) {
            mController.destroy();
        }

        // 使用新的场景配置重新构建并 attach
        mController = new AliPlayerController(this);
        AliPlayerModel model = buildPlayerModel();
        mController.configure(model);

        // 清空旧配置，注册自定义插槽并应用隐藏元素
        SlotManager slotManager = playerView.getSlotManager();
        slotManager.clearAllHiddenElements();
        slotManager.register(WATERMARK, parent -> new WatermarkSlot(parent.getContext()));
        mCurrentScene.applyHiddenElements(slotManager);

        playerView.attach(mController);

        // 更新 UI
        updateUI();
    }

    // ============================================================
    // UI 更新
    // ============================================================

    /**
     * 更新 UI 状态
     * <p>
     * 根据当前场景更新场景描述和当前模式文本。
     * </p>
     */
    private void updateUI() {
        String description = mCurrentScene.getDescription(this);
        mDescriptionTv.setText(description);

        String name = mCurrentScene.getName(this);
        mModeTv.setText(getString(R.string.slot_demo_current_mode, name));
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 根据 Chip ID 获取对应的演示场景
     *
     * @param chipId Chip 的资源 ID
     * @return 对应的 SlotExampleScene，如果没有匹配则返回 null
     */
    @Nullable
    private static SlotExampleScene getSceneFromChipId(int chipId) {
        if (chipId == R.id.chip_default) {
            return SlotExampleScene.DEFAULT;
        }
        if (chipId == R.id.chip_simplified) {
            return SlotExampleScene.SIMPLIFIED_UI;
        }
        if (chipId == R.id.chip_gesture_restricted) {
            return SlotExampleScene.GESTURE_RESTRICTED;
        }
        if (chipId == R.id.chip_fine_grained) {
            return SlotExampleScene.FINE_GRAINED;
        }
        return null;
    }
}
