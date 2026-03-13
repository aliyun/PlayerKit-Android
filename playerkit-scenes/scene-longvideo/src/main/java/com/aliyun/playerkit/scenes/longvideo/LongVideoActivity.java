package com.aliyun.playerkit.scenes.longvideo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * 中长视频播放示例 Activity
 * <p>
 * 演示如何在中长视频场景下使用 AliPlayerKit 进行视频播放。
 * </p>
 * <p>
 * 集成FAQ (Integration FAQ)
 * 本示例依赖 demo-settings 和 scene-common 模块，集成时请：
 * 1. 移除 demo-settings 依赖（仅用于 Demo 演示）
 * 2. 如自行实现视频源，可移除 scene-common 依赖
 * 3. 修改 getVideoVid()、getVideoPlayAuth() 方法，替换为您的视频源获取逻辑
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class LongVideoActivity extends AppCompatActivity {

    // 播放器组件视图
    private AliPlayerView playerView;

    // 播放器组件控制器
    private AliPlayerController playerController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_video);

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

        // 步骤 3：配置播放器组件数据
        // 优先使用设置的 Vid 和 PlayAuth，如果没有设置则使用默认值
        String vid = getVideoVid();
        String playAuth = getVideoPlayAuth();
        VideoSource.VidAuthSource videoSource = VideoSourceFactory.createVidAuthSource(vid, playAuth);
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle("Long Video")
                .build();

        // 步骤 4：绑定控制器和数据到视图
        playerView.attach(playerController, playerModel);
    }

    /**
     * 获取视频 Vid
     * <p>
     * 优先从设置中读取，如果没有设置则使用默认值。
     * </p>
     * <p>
     * 集成FAQ (Integration FAQ)
     * 请替换为您的视频源获取逻辑，例如：
     * <pre>
     * private String getVideoVid() {
     *     return "您的视频 Vid";  // 或从您的业务接口获取
     * }
     * </pre>
     * </p>
     *
     * @return 视频 Vid
     */
    private String getVideoVid() {
        // Demo 演示：从 demo-settings 读取，集成时请替换
        String savedVid = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID);
        return StringUtil.isNotEmpty(savedVid) ? savedVid : SceneConstants.LANDSCAPE_SAMPLE_VID;
    }

    /**
     * 获取视频 PlayAuth
     * <p>
     * 优先从设置中读取，如果没有设置则使用默认值。
     * </p>
     * <p>
     * 集成FAQ (Integration FAQ)
     * 请替换为您的视频源获取逻辑，例如：
     * <pre>
     * private String getVideoPlayAuth() {
     *     return "您的 PlayAuth";  // 或从您的业务接口获取
     * }
     * </pre>
     * </p>
     *
     * @return 视频 PlayAuth
     */
    private String getVideoPlayAuth() {
        // Demo 演示：从 demo-settings 读取，集成时请替换
        String savedPlayAuth = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_PLAY_AUTH);
        return StringUtil.isNotEmpty(savedPlayAuth) ? savedPlayAuth : SceneConstants.LANDSCAPE_SAMPLE_PLAY_AUTH;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 解绑播放器组件，释放资源
        // 注意：Activity 销毁时必须释放播放器组件资源，避免内存泄露
        if (playerView != null) {
            playerView.detach();
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
