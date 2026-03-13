package com.aliyun.playerkit.examples.mobileops;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.SourceType;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.example.settings.link.LinkActivity;
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.logging.logger.LoggerCallback;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.logging.LogLevel;
import com.aliyun.playerkit.slot.SlotRegistry;
import com.aliyun.playerkit.slot.SlotType;
import com.aliyun.playerkit.ui.DefaultSlotRegistryFactory;
import com.aliyun.playerkit.ui.slots.LogPanelSlot;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;

/**
 * 掌上运维 Activity
 * <p>
 * 用于分析端侧日志的运维工具。选择视频源类型后开始播放，播放过程中的日志将自动注入到日志面板。
 * </p>
 * <p>
 * Mobile Operations Activity
 * <p>
 * Operations tool for analyzing client-side logs. Select a video source type to start playback.
 * Logs during playback will be automatically injected into the log panel.
 * </p>
 *
 * @author keria
 * @date 2026/01/13
 */
public class MobileOpsActivity extends AppCompatActivity {

    private static final String TAG = "MobileOpsActivity";

    // 播放器 SDK 日志标签
    private static final String PLAYER_SDK_LOG_TAG = "AliFrameWork";

    // 播放器组件视图
    private AliPlayerView mPlayerView;

    private LinearLayout mLlSourceSelection;
    private Button mBtnUrlSource;
    private Button mBtnVidAuthSource;
    private Button mBtnVidStsSource;

    // 日志回调，用于将播放器日志注入到 LogHub
    private final LoggerCallback mLogCallback = new LoggerCallback() {
        @Override
        public void onLog(@LogLevel int level, @NonNull String message) {
            // 将播放器日志注入到 LogHub，这样可以在日志面板上显示
            LogHub.log(level, "", message);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_ops);

        initViews();
        setupButtons();
        setupLogger();
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
     * 设置日志回调，将播放器日志注入到 LogHub
     */
    private void setupLogger() {
        // 设置日志回调
        AliPlayerKit.getLogger().setLogCallback(mLogCallback);
        LogHub.i(TAG, "setupLogger", "Logger callback set, player logs will be injected to LogHub");
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
        String title = getString(R.string.mobile_ops_config_title);
        String message = getString(R.string.mobile_ops_config_message);
        String positiveButton = getString(R.string.mobile_ops_config_go_scan);

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

        // 创建自定义插槽注册表，重新定制日志面板插槽
        SlotRegistry registry = DefaultSlotRegistryFactory.create();
        registry.register(SlotType.LOG_PANEL, parent -> new MobileOpsLogPanelSlot(parent.getContext()));

        // 构建播放数据
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle("Mobile Ops Demo")
                .build();

        // 绑定控制器、数据和自定义注册表到视图
        mPlayerView.attach(playerController, playerModel, registry);

        // 隐藏选择区域，显示播放器
        mLlSourceSelection.setVisibility(View.GONE);
        mPlayerView.setVisibility(View.VISIBLE);

        // 记录开始播放的日志
        LogHub.i(TAG, "playVideo", "Video playback started, logs will be injected to LogHub");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 移除日志回调
        AliPlayerKit.getLogger().setLogCallback(null);
        LogHub.i(TAG, "onDestroy", "Logger callback removed");

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

    /**
     * 自定义日志面板插槽
     * <p>
     * 仅展示特定标签（AliFrameWork）的日志。
     * </p>
     */
    private static class MobileOpsLogPanelSlot extends LogPanelSlot {

        public MobileOpsLogPanelSlot(@NonNull android.content.Context context) {
            super(context);
        }

        @Override
        protected boolean acceptLog(@NonNull com.aliyun.playerkit.logging.LogInfo logInfo) {
            // 先执行基类过滤逻辑（级别过滤等）
            if (!super.acceptLog(logInfo)) {
                return false;
            }

            // 仅接受含 AliFrameWork 的日志
            return StringUtil.contains(logInfo.getMessage(), PLAYER_SDK_LOG_TAG);
        }
    }
}
