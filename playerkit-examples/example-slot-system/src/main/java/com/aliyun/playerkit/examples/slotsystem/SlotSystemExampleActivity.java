package com.aliyun.playerkit.examples.slotsystem;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.slot.ISlotManager;
import com.aliyun.playerkit.slot.SlotRegistry;
import com.aliyun.playerkit.slot.SlotType;
import com.aliyun.playerkit.ui.slots.SurfaceViewSlot;
import com.aliyun.playerkit.ui.slots.TextureViewSlot;
import com.aliyun.playerkit.utils.ToastUtils;

/**
 * 插槽系统使用示例 Activity
 * <p>
 * 演示 PlayerKit 插槽系统的 Surface 插槽动态切换功能。
 * </p>
 * <p>
 * <b>核心功能</b>：
 * <ul>
 *     <li><b>动态切换 Surface 插槽</b>：运行时无缝切换 SurfaceViewSlot/TextureViewSlot/空插槽</li>
 *     <li><b>插槽特性展示</b>：清晰展示每种插槽的特点、优势和使用场景</li>
 *     <li><b>插槽系统优势</b>：体现模块化、动态化、易扩展的设计理念</li>
 * </ul>
 * </p>
 * <p>
 * <b>Surface 插槽类型说明</b>：
 * <ul>
 *     <li><b>SurfaceViewSlot</b>：传统 SurfaceView 实现，独立渲染线程，适合需要独立渲染层的场景</li>
 *     <li><b>TextureViewSlot</b>：基于 TextureView，支持动画和变换，适合需要视图动画的场景</li>
 *     <li><b>空插槽</b>：移除 Surface 插槽，可用于特殊场景（如纯音频播放、后台播放等）</li>
 * </ul>
 * </p>
 * <p>
 * Slot System Example Activity
 * <p>
 * Demonstrates the dynamic switching functionality of Surface slots in the PlayerKit slot system.
 * </p>
 * <p>
 * <b>Core Features</b>:
 * <ul>
 *     <li><b>Dynamic Surface Slot Switching</b>: Seamlessly switch between SurfaceViewSlot/TextureViewSlot/Empty slot at runtime</li>
 *     <li><b>Slot Feature Display</b>: Clearly shows the characteristics, advantages, and use cases of each slot type</li>
 *     <li><b>Slot System Advantages</b>: Demonstrates modular, dynamic, and easily extensible design principles</li>
 * </ul>
 * </p>
 * <p>
 * <b>Surface Slot Type Explanation</b>:
 * <ul>
 *     <li><b>SurfaceViewSlot</b>：Traditional SurfaceView implementation, independent rendering thread, suitable for layers that need to be rendered independently</li>
 *     <li><b>TextureViewSlot</b>：Based on TextureView, supporting animations and transformations, suitable for scenarios that need view animations</li>
 *     <li><b>EmptySlot</b>：Remove Surface slot, suitable for special scenarios (such as pure audio playback, background playback, etc.)</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2025/12/11
 */
public class SlotSystemExampleActivity extends AppCompatActivity {

    private AliPlayerView playerView;
    private SlotRegistry slotRegistry;
    private ISlotManager slotManager;

    private TextView mStatusTv;
    private TextView mFeatureTv;
    private Button[] mSlotButtons;
    private SurfaceSlotType mCurrentSlot = SurfaceSlotType.SURFACE_VIEW;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加载布局文件
        setContentView(R.layout.activity_slot_system_example);

        // 初始化视图和事件监听
        initViews();
        // 初始化播放器组件
        initPlayerKit();
    }

    /**
     * 初始化视图
     * <p>
     * 绑定 UI 控件，设置按钮点击事件监听器，并更新初始 UI 状态。
     * </p>
     * <p>
     * Initialize Views
     * <p>
     * Bind UI controls, set button click listeners, and update initial UI state.
     * </p>
     */
    private void initViews() {
        // 绑定状态显示控件
        mStatusTv = findViewById(R.id.status_text);
        mFeatureTv = findViewById(R.id.feature_text);

        // 绑定插槽切换按钮
        mSlotButtons = new Button[]{findViewById(R.id.btn_surface_view), findViewById(R.id.btn_texture_view), findViewById(R.id.btn_empty_slot)};

        // 为每个按钮设置点击事件监听器
        // 按钮顺序与 SurfaceSlotType 枚举顺序对应
        SurfaceSlotType[] types = SurfaceSlotType.values();
        for (int i = 0; i < mSlotButtons.length && i < types.length; i++) {
            final SurfaceSlotType type = types[i];
            mSlotButtons[i].setOnClickListener(v -> switchSurfaceSlot(type));
        }

        // 更新 UI 显示初始状态
        updateUI();
    }

    /**
     * 初始化播放器组件
     * <p>
     * 创建播放器实例、配置播放数据、注册插槽，并获取 SlotHostLayout 引用用于后续动态切换。
     * </p>
     * <p>
     * Initialize Player Kit
     * <p>
     * Create player instance, configure playback data, register slots, and get SlotHostLayout reference for dynamic switching.
     * </p>
     */
    private void initPlayerKit() {
        // 步骤 1：获取播放器组件视图
        playerView = findViewById(R.id.v_player_kit);

        // 步骤 2：创建插槽注册表并注册初始插槽
        slotRegistry = new SlotRegistry();
        registerSurfaceSlot(slotRegistry, mCurrentSlot);

        // 步骤 3：创建播放器控制器
        AliPlayerController controller = new AliPlayerController(this);

        // 步骤 4：创建视频源
        VideoSource.UrlSource videoSource = VideoSourceFactory.createUrlSource(SceneConstants.SAMPLE_VIDEO_URL);

        // 步骤 5：构建播放数据配置
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .build();

        // 步骤 6：绑定控制器、数据和插槽注册表到播放器视图
        // 这会触发插槽的创建和初始化
        playerView.attach(controller, playerModel, slotRegistry);

        // 步骤 7：获取插槽管理器，用于后续动态切换插槽
        slotManager = playerView.getSlotManager();
    }

    /**
     * 注册 Surface 插槽到注册表
     * <p>
     * 根据插槽类型，向 SlotRegistry 注册对应的插槽构建器。
     * 这是插槽系统动态切换的核心实现：通过修改注册表中的构建器，可以实现运行时切换不同的插槽实现。
     * </p>
     * <p>
     * Register Surface Slot to Registry
     * <p>
     * Register the corresponding slot builder to SlotRegistry based on the slot type.
     * This is the core implementation of dynamic slot switching: by modifying the builder in the registry,
     * different slot implementations can be switched at runtime.
     * </p>
     *
     * @param registry 插槽注册表，不能为 null
     * @param type     Surface 插槽类型，不能为 null
     */
    private void registerSurfaceSlot(@NonNull SlotRegistry registry, @NonNull SurfaceSlotType type) {
        switch (type) {
            case SURFACE_VIEW:
                // 注册 SurfaceViewSlot 构建器
                // SurfaceView 使用独立渲染线程，适合需要独立渲染层的场景
                registry.register(SlotType.PLAYER_SURFACE, parent -> new SurfaceViewSlot(parent.getContext()));
                break;
            case TEXTURE_VIEW:
                // 注册 TextureViewSlot 构建器
                // TextureView 支持动画和变换，适合需要视图动画的场景
                registry.register(SlotType.PLAYER_SURFACE, parent -> new TextureViewSlot(parent.getContext()));
                break;
            case EMPTY:
                // 移除 Surface 插槽注册
                // 适用于纯音频播放、后台播放等不需要视频显示的场景
                registry.unregister(SlotType.PLAYER_SURFACE);
                break;
        }
    }

    /**
     * 切换 Surface 插槽
     * <p>
     * 这是插槽系统的核心功能：运行时动态切换不同的 Surface 实现。
     * </p>
     * <p>
     * Switch Surface Slot
     * <p>
     * This is the core functionality of the slot system: dynamically switch between different Surface implementations at runtime.
     * </p>
     *
     * @param type 目标 Surface 插槽类型，不能为 null
     */
    private void switchSurfaceSlot(@NonNull SurfaceSlotType type) {
        // 步骤 1：检查是否已经是目标插槽类型
        if (mCurrentSlot == type) {
            ToastUtils.showToast(getString(R.string.toast_already_current, type.getDisplayName(this)));
            return;
        }

        // 步骤 2：检查插槽管理器是否已初始化
        // 注意：虽然 getSlotManager() 返回 @NonNull，但为了防御性编程，仍保留检查
        if (slotManager == null || playerView == null) {
            ToastUtils.showToast(R.string.toast_slot_host_layout_not_initialized);
            return;
        }

        // 步骤 3：重新注册目标插槽类型到 SlotRegistry
        // 这会覆盖之前的构建器，实现动态切换
        registerSurfaceSlot(slotRegistry, type);
        mCurrentSlot = type;

        // 步骤 4：调用 rebuildSlots() 重建插槽
        // rebuildSlots() 会：
        // - 调用旧插槽的 onDetach() 进行清理（会清除旧的 Surface）
        // - 根据 SlotRegistry 中的新构建器创建新插槽
        // - 调用新插槽的 onAttach() 进行初始化（会设置新的 Surface）
        slotManager.rebuildSlots();

        // 步骤 5：更新 UI 状态显示
        updateUI();

        // 步骤 6：显示切换成功提示
        ToastUtils.showToast(getString(R.string.toast_switched_to, type.getDisplayName(this)));
    }

    /**
     * 更新 UI 状态
     * <p>
     * 更新状态文本、特性说明和按钮选中状态，提供清晰的视觉反馈。
     * </p>
     * <p>
     * Update UI State
     * <p>
     * Update status text, feature description, and button selected state to provide clear visual feedback.
     * </p>
     */
    private void updateUI() {
        // 更新状态文本：显示当前插槽类型
        mStatusTv.setText(getString(R.string.slot_system_example_current_status, mCurrentSlot.getDisplayName(this)));

        // 更新特性说明：显示当前插槽的特性
        mFeatureTv.setText(getString(R.string.slot_system_example_features, mCurrentSlot.getFeatures(this)));

        // 更新按钮状态：高亮当前选中的按钮
        SurfaceSlotType[] types = SurfaceSlotType.values();
        Resources res = getResources();
        // 从资源文件获取透明度值（整数，需要除以 100 转换为 float）
        float alphaSelected = res.getInteger(R.integer.slot_button_alpha_selected) / 100f;
        float alphaUnselected = res.getInteger(R.integer.slot_button_alpha_unselected) / 100f;
        int colorSelected = ContextCompat.getColor(this, R.color.slot_button_selected);
        int colorUnselected = ContextCompat.getColor(this, R.color.slot_button_unselected);

        for (int i = 0; i < mSlotButtons.length && i < types.length; i++) {
            boolean isSelected = types[i] == mCurrentSlot;
            mSlotButtons[i].setSelected(isSelected);
            mSlotButtons[i].setAlpha(isSelected ? alphaSelected : alphaUnselected);
            mSlotButtons[i].setBackgroundColor(isSelected ? colorSelected : colorUnselected);
        }
    }

    /**
     * Activity 销毁时调用
     * <p>
     * 解绑播放器组件，释放资源。插槽系统会自动处理所有插槽的清理工作。
     * </p>
     * <p>
     * Called when Activity is destroyed
     * <p>
     * Detach player component and release resources. The slot system will automatically handle cleanup of all slots.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑播放器组件，释放资源
        // 插槽系统会自动处理所有插槽的清理工作
        if (playerView != null) {
            playerView.detach();
        }
    }
}
