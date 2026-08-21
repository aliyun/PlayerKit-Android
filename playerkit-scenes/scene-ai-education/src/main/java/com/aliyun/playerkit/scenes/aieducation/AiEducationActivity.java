package com.aliyun.playerkit.scenes.aieducation;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.scenes.aieducation.data.AiAnalysisResponse;
import com.aliyun.playerkit.scenes.aieducation.data.AiContentAssembler;
import com.aliyun.playerkit.scenes.aieducation.data.AiContentLoader;
import com.aliyun.playerkit.scenes.aieducation.ui.slots.ChapterButtonSlot;
import com.aliyun.playerkit.scenes.aieducation.ui.slots.ChapterContentPanelSlot;
import com.aliyun.playerkit.slot.SlotManager;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.List;

/**
 * AI 教育场景播放示例 Activity
 * <p>
 * 演示如何在 AI 教育场景下使用 AliPlayerKit 的 AI 章节和摘要功能。
 * 通过自定义插槽（ChapterButtonSlot + ChapterContentPanelSlot）实现章节导航和 AI 内容解析。
 * </p>
 * <p>
 * 集成FAQ (Integration FAQ)
 * 本示例依赖 demo-settings 和 scene-common 模块，集成时请：
 * 1. 移除 demo-settings 依赖（仅用于 Demo 演示）
 * 2. 如自行实现视频源，可移除 scene-common 依赖
 * 3. 修改 getVideoVid() 方法和 AiContentLoader 中的 PlayAuth 获取逻辑，替换为您的视频源获取方式
 * 4. 替换 AiContentLoader 实现，对接您的 AI 内容服务
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class AiEducationActivity extends AppCompatActivity {

    private static final String TAG = "AiEducationActivity";

    // 播放器组件视图
    private AliPlayerView playerView;

    // 播放器组件控制器
    private AliPlayerController playerController;

    /**
     * 章节内容面板插槽实例，注册时捕获，用于在 AI 内容就绪后传入数据
     */
    @Nullable
    private ChapterContentPanelSlot mChapterContentPanelSlot;

    /**
     * 章节按钮插槽实例，注册时捕获，用于根据 AI 内容可用性控制按钮显隐
     */
    @Nullable
    private ChapterButtonSlot mChapterButtonSlot;

    /**
     * AI 内容加载器，页面销毁时需取消进行中的任务
     */
    @Nullable
    private AiContentLoader mContentLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_education);

        initPlayerKit();
    }

    /**
     * 初始化 AliPlayerKit 播放组件
     */
    private void initPlayerKit() {
        // 步骤 1：获取播放器组件视图
        playerView = findViewById(R.id.v_player_kit);

        // 步骤 2：创建播放器组件控制器（传入 Activity 上下文）
        playerController = new AliPlayerController(this);

        // 步骤 3：注册自定义插槽（章节按钮 + 章节面板）
        registerAiSlot();

        String vid = AiContentConstants.DEFAULT_MEDIA_ID;

        // 步骤 4：异步链式加载（PlayAuth → AI 内容 → 配置播放器）
        mContentLoader = new AiContentLoader();
        mContentLoader.loadPlayAuth(vid, playAuth -> {
            // 回调在主线程执行，页面已销毁时不再处理
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (TextUtils.isEmpty(playAuth)) {
                LogHub.w(TAG, "Failed to get PlayAuth");
                // PlayAuth 获取失败，提示用户并退出页面，避免停留在黑屏页面
                ToastUtils.showToast(R.string.ai_education_error_server_unreachable);
                finish();
                return;
            }
            mContentLoader.loadAiAnalysis(vid, response -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (response == null) {
                    // AI 内容加载失败不影响播放，仅提示用户知情
                    ToastUtils.showToast(R.string.ai_education_error_ai_content_load_failed);
                }
                configurePlayer(vid, playAuth, response);
            });
        });
    }

    /**
     * 注册 AI 相关插槽
     */
    private void registerAiSlot() {
        SlotManager slotManager = playerView.getSlotManager();
        slotManager.register(ChapterButtonSlot.TYPE, parent -> {
            // 保存插槽实例，attach 后由本页面根据 AI 内容可用性控制按钮显隐
            mChapterButtonSlot = new ChapterButtonSlot(parent.getContext());
            return mChapterButtonSlot;
        });
        slotManager.register(ChapterContentPanelSlot.TYPE, parent -> {
            // 保存插槽实例，attach 后由本页面把 AI 内容传入面板
            mChapterContentPanelSlot = new ChapterContentPanelSlot(parent.getContext());
            return mChapterContentPanelSlot;
        });
    }

    /**
     * 配置播放器（在 PlayAuth 和 AI 内容加载完成后调用）
     */
    private void configurePlayer(@NonNull String vid, @NonNull String playAuth, @Nullable AiAnalysisResponse response) {
        // 消费侧显式调用 Assembler 构建章节列表
        List<ChapterInfo> chapters = response != null ? AiContentAssembler.buildChapters(response) : null;
        // 仅当章节非空且非空列表时才下发章节数据
        boolean hasChapters = chapters != null && !chapters.isEmpty();

        VideoSource.VidAuthSource videoSource = VideoSourceFactory.createVidAuthSource(vid, playAuth);
        AliPlayerModel.Builder builder = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .sceneType(SceneType.AI_VOD);
        if (hasChapters) {
            builder.chapters(chapters);
        }
        AliPlayerModel playerModel = builder.build();

        // 配置播放源并挂载控制器（挂载后插槽才会创建，自动开始播放）
        playerController.configure(playerModel);
        playerView.attach(playerController);

        // attach 后显式把 AI DTO 推给面板插槽（插槽之间互不感知）
        if (mChapterContentPanelSlot != null) {
            mChapterContentPanelSlot.updateAiContent(response);
        }

        // 根据 Chapter/Summary 结果是否存在控制章节按钮显隐
        boolean chapterAvailable = AiContentAssembler.hasAnyContent(response);
        if (mChapterButtonSlot != null) {
            mChapterButtonSlot.updateChapterAvailable(chapterAvailable);
        }
    }

    // ==================== Lifecycle ====================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel in-flight AI content loading tasks to avoid invalid callbacks.
        if (mContentLoader != null) {
            mContentLoader.cancel();
        }

        // Destroy the player controller to release resources.
        // Note: Must destroy when Activity is destroyed to avoid memory leaks.
        if (playerController != null) {
            playerController.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (playerView != null && playerView.onBackPressed()) {
            // 已处理返回键（退出全屏），不需要执行默认行为
            return;
        }
        // 未处理，执行默认行为（关闭 Activity）
        super.onBackPressed();
    }
}
