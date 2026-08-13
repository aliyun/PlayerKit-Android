package com.aliyun.playerkit.scenes.longvideo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
 * 演示如何在中长视频场景下使用 AliPlayerKit 进行视频播放，
 * 并演示返回键的分级处理：全屏状态下优先退出全屏，非全屏状态下二次确认后退出页面。
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

    // 退出播放确认弹窗（持有引用以便在页面销毁前关闭，避免窗口泄露）
    private AlertDialog exitDialog;

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
                .build();

        // 步骤 4：配置数据并绑定控制器到视图
        playerController.configure(playerModel);
        playerView.attach(playerController);
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

        // 关闭可能仍在显示的弹窗，避免窗口泄露
        if (exitDialog != null) {
            exitDialog.dismiss();
            exitDialog = null;
        }

        // 销毁播放器控制器，释放资源
        // 注意：Activity 销毁时必须销毁控制器，避免内存泄露
        if (playerController != null) {
            playerController.destroy();
        }
    }

    /**
     * 处理返回键事件
     * <p>
     * 返回键采用分级处理，优先级从高到低：
     * </p>
     * <ol>
     *   <li>交给播放器组件处理：如果当前处于全屏状态，则退出全屏，页面保持不变</li>
     *   <li>播放器组件未处理（非全屏状态）：弹窗二次确认，避免误触导致播放中断</li>
     * </ol>
     * <p>
     * 集成FAQ (Integration FAQ)
     * 第 1 步是全屏播放的必要处理，请务必保留；
     * 第 2 步的退出确认为示例交互，如需直接关闭页面，替换为 super.onBackPressed() 即可。
     * </p>
     */
    @Override
    public void onBackPressed() {
        // 步骤 1：优先交给播放器组件处理
        if (playerView != null && playerView.onBackPressed()) {
            // 已处理返回键（退出全屏），不需要执行默认行为
            return;
        }
        // 步骤 2：播放器组件未处理，弹窗确认是否退出播放
        showExitConfirmation();
    }

    /**
     * 弹窗确认是否退出播放
     * <p>
     * 用户点击「退出」时关闭页面，点击「取消」则关闭弹窗并继续播放。
     * </p>
     */
    private void showExitConfirmation() {
        // 连续按下返回键时避免重复弹窗
        if (exitDialog != null && exitDialog.isShowing()) {
            return;
        }

        exitDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.long_video_exit_confirm_title)
                .setMessage(R.string.long_video_exit_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.long_video_exit_confirm_exit, (dialog, which) -> finish())
                .show();
    }
}
