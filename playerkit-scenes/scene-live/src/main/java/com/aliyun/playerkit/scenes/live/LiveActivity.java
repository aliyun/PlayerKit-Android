package com.aliyun.playerkit.scenes.live;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.example.settings.link.LinkActivity;
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * 直播播放场景
 * <p>
 * 演示如何在直播场景下使用 AliPlayerKit 播放视频。
 * </p>
 * <p>
 * 集成FAQ (Integration FAQ)
 * 本示例依赖 demo-settings 和 scene-common 模块，集成时请：
 * 1. 移除 demo-settings 依赖（仅用于 Demo 演示）
 * 2. 如自行实现视频源，可移除 scene-common 依赖
 * 3. 修改 getLiveUrl() 方法，替换为您的直播源获取逻辑
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class LiveActivity extends AppCompatActivity {

    // 播放器组件视图
    private AliPlayerView playerView;

    // 播放器组件控制器
    private AliPlayerController playerController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

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
        String liveUrl = getLiveUrl();
        if (StringUtil.isEmpty(liveUrl)) {
            // 获取直播地址（必须从设置中读取，如果没有则提示跳转扫码）
            showScanDialog();
            return;
        }

        VideoSource.UrlSource videoSource = VideoSourceFactory.createUrlSource(liveUrl);
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle("Live Stream")
                .sceneType(SceneType.LIVE)
                .build();

        // 步骤 4：绑定控制器和数据到视图
        playerView.attach(playerController, playerModel);
    }

    /**
     * 获取直播地址
     * <p>
     * Demo 演示：从设置中读取直播地址，如果没有设置则返回 null。
     * </p>
     * <p>
     * 集成FAQ (Integration FAQ)
     * 请替换为您的直播源获取逻辑，例如：
     * <pre>
     * private String getLiveUrl() {
     *     return "您的直播地址";  // 或从您的业务接口获取
     * }
     * </pre>
     * </p>
     *
     * @return 直播地址，如果没有设置则返回 null
     */
    @Nullable
    private String getLiveUrl() {
        // Demo 演示：从 demo-settings 读取，集成时请替换
        String savedUrl = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_LIVE_URL);
        return StringUtil.isNotEmpty(savedUrl) ? savedUrl : null;
    }

    /**
     * 显示扫码提示对话框
     * <p>
     * 提示用户未设置直播地址，引导用户跳转到扫码页面进行配置。
     * </p>
     */
    private void showScanDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.setting_live_url_not_set_title)
                .setMessage(R.string.setting_live_url_not_set_message)
                .setPositiveButton(R.string.setting_live_url_go_scan, (dialog, which) -> {
                    Intent intent = new Intent(this, LinkActivity.class);
                    // 只显示直播地址配置项
                    ArrayList<String> filterKeys = new ArrayList<>();
                    filterKeys.add(LinkConstants.KEY_VIDEO_LIVE_URL);
                    intent.putStringArrayListExtra(LinkActivity.EXTRA_FILTER_KEYS, filterKeys);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
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
}
