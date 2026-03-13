package com.aliyun.playerkit.examples.videosource;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.SourceType;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.example.settings.link.LinkActivity;
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;

/**
 * 视频源使用示例 Activity
 * <p>
 * 演示如何使用不同类型的视频源（URL、VidAuth、VidSts）进行视频播放。
 * 先选择播放类型，如果 link 中没有缓存，跳转 link 页面先扫码。
 * </p>
 * <p>
 * Video Source Example Activity
 * <p>
 * Demonstrates how to use different types of video sources (URL, VidAuth, VidSts) for video playback.
 * First select the playback type, if there is no cache in link, jump to link page to scan code first.
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class VideoSourceExampleActivity extends AppCompatActivity {

    // 播放器组件视图
    private AliPlayerView mPlayerView;

    private LinearLayout mLlSourceSelection;
    private Button mBtnUrlSource;
    private Button mBtnVidAuthSource;
    private Button mBtnVidStsSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_source_example);

        initViews();
        setupButtons();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mPlayerView = findViewById(R.id.v_player_kit);
        mLlSourceSelection = findViewById(R.id.ll_source_selection);
        mBtnUrlSource = findViewById(R.id.btn_url_source);
        mBtnVidAuthSource = findViewById(R.id.btn_vid_auth_source);
        mBtnVidStsSource = findViewById(R.id.btn_vid_sts_source);
    }

    /**
     * 设置按钮点击事件监听器
     */
    private void setupButtons() {
        mBtnUrlSource.setOnClickListener(v -> {
            checkAndPlay(SourceType.URL);
        });

        mBtnVidAuthSource.setOnClickListener(v -> {
            checkAndPlay(SourceType.VID_AUTH);
        });

        mBtnVidStsSource.setOnClickListener(v -> {
            checkAndPlay(SourceType.VID_STS);
        });
    }

    /**
     * 检查配置并播放
     */
    private void checkAndPlay(@SourceType int sourceType) {
        VideoSource videoSource = createVideoSource(sourceType);
        if (videoSource == null || !videoSource.isValid()) {
            // 配置不完整，显示提示对话框并跳转到 link 页面配置
            showConfigDialog(sourceType);
            return;
        }

        // 配置完整，开始播放
        playVideo(videoSource);
    }

    /**
     * 显示配置提示对话框
     */
    private void showConfigDialog(@SourceType int sourceType) {
        String title = getString(R.string.video_source_example_config_title);
        String message = getString(R.string.video_source_example_config_message);
        String positiveButton = getString(R.string.video_source_example_config_go_scan);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, which) -> {
                    Intent intent = new Intent(this, LinkActivity.class);
                    // 根据视频源类型过滤配置项
                    ArrayList<String> filterKeys = getFilterKeysForSourceType(sourceType);
                    if (filterKeys != null && !filterKeys.isEmpty()) {
                        intent.putStringArrayListExtra(LinkActivity.EXTRA_FILTER_KEYS, filterKeys);
                    }
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    // 取消，不做任何操作
                })
                .setCancelable(true)
                .show();
    }

    /**
     * 获取视频源类型对应的过滤 key 列表
     */
    @Nullable
    private ArrayList<String> getFilterKeysForSourceType(@SourceType int sourceType) {
        ArrayList<String> keys = new ArrayList<>();
        switch (sourceType) {
            case SourceType.URL:
                keys.add(LinkConstants.KEY_VIDEO_URL);
                return keys;
            case SourceType.VID_AUTH:
                // VidAuth 需要两个参数（vid, playAuth）
                keys.add(LinkConstants.KEY_VIDEO_VID);
                keys.add(LinkConstants.KEY_VIDEO_PLAY_AUTH);
                return keys;
            case SourceType.VID_STS:
                // VidSts 需要四个必需参数（vid, accessKeyId, accessKeySecret, securityToken）
                keys.add(LinkConstants.KEY_VIDEO_VID_STS_VID);
                keys.add(LinkConstants.KEY_VIDEO_VID_STS_ACCESS_KEY_ID);
                keys.add(LinkConstants.KEY_VIDEO_VID_STS_ACCESS_KEY_SECRET);
                keys.add(LinkConstants.KEY_VIDEO_VID_STS_SECURITY_TOKEN);
                // region 是可选的，但也可以显示让用户配置
                keys.add(LinkConstants.KEY_VIDEO_VID_STS_REGION);
                return keys;
            default:
                return null;
        }
    }

    /**
     * 创建视频源
     */
    @Nullable
    private VideoSource createVideoSource(@SourceType int sourceType) {
        switch (sourceType) {
            case SourceType.URL: {
                String url = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_URL);
                if (StringUtil.isNotEmpty(url)) {
                    return VideoSourceFactory.createUrlSource(url);
                }
                return null;
            }

            case SourceType.VID_AUTH: {
                String vid = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID);
                String playAuth = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_PLAY_AUTH);
                return VideoSourceFactory.createVidAuthSource(vid, playAuth);
            }

            case SourceType.VID_STS: {
                String vid = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID_STS_VID);
                String accessKeyId = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID_STS_ACCESS_KEY_ID);
                String accessKeySecret = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID_STS_ACCESS_KEY_SECRET);
                String securityToken = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID_STS_SECURITY_TOKEN);
                String region = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID_STS_REGION);
                return VideoSourceFactory.createVidStsSource(vid, accessKeyId, accessKeySecret, securityToken, region);
            }

            default:
                return null;
        }
    }

    /**
     * 播放视频
     */
    private void playVideo(VideoSource videoSource) {
        // 创建播放控制器
        AliPlayerController playerController = new AliPlayerController(this);

        // 构建播放数据
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle("Video Source Example")
                .build();

        // 绑定控制器和数据到视图
        mPlayerView.attach(playerController, playerModel);

        // 隐藏选择区域，显示播放器
        mLlSourceSelection.setVisibility(View.GONE);
        mPlayerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 解绑播放器组件，释放资源
        if (mPlayerView != null) {
            mPlayerView.detach();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView != null && mPlayerView.onBackPressed()) {
            // 已处理返回键（退出全屏），不需要执行默认行为
            return;
        }
        super.onBackPressed();
    }
}
