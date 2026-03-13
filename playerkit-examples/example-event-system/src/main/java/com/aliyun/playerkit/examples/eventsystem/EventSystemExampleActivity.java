package com.aliyun.playerkit.examples.eventsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.example.settings.link.LinkConstants;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 事件系统使用示例 Activity
 * <p>
 * 演示如何通过事件总线发送命令和接收事件：
 * <ul>
 *     <li>发送命令：通过 {@link PlayerEventBus#post(PlayerEvent)} 发送 {@link PlayerCommand.Toggle} 命令控制播放/暂停</li>
 *     <li>接收事件：通过 {@link PlayerEventBus#subscribe(Class, PlayerEventBus.EventListener)} 订阅 {@link PlayerEvents.Info} 事件，实时接收播放信息</li>
 * </ul>
 * </p>
 * <p>
 * Event System Example Activity
 * <p>
 * Demonstrates how to send commands and receive events through the event bus:
 * <ul>
 *     <li>Send Command: Send {@link PlayerCommand.Toggle} command via {@link PlayerEventBus#post(PlayerEvent)} to control play/pause</li>
 *     <li>Receive Event: Subscribe to {@link PlayerEvents.Info} event via {@link PlayerEventBus#subscribe(Class, PlayerEventBus.EventListener)} to receive playback information in real-time</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class EventSystemExampleActivity extends AppCompatActivity {

    // UI 最多展示的事件行数（可按需调整）
    private static final int MAX_EVENT_LINES = 20;

    // 播放器组件视图
    private AliPlayerView mPlayerView;
    // 播放器组件控制器
    private AliPlayerController mPlayerController;

    private Button mBtnSendToggle;
    private ScrollView mSvInfoOutput;
    private TextView mTvInfoOutput;

    // 事件总线实例
    private PlayerEventBus mEventBus;
    // Info 事件监听器
    private PlayerEventBus.EventListener<PlayerEvents.Info> mInfoListener;

    private final Deque<String> mEventLines = new ArrayDeque<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_system_example);

        initViews();
        initPlayerKit();
        setupEventBus();
        setupButtons();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mPlayerView = findViewById(R.id.v_player_kit);
        mBtnSendToggle = findViewById(R.id.btn_send_toggle);
        mSvInfoOutput = findViewById(R.id.sv_info_output);
        mTvInfoOutput = findViewById(R.id.tv_info_output);
    }

    /**
     * 初始化 AliPlayerKit 播放组件
     */
    private void initPlayerKit() {
        // 1. 创建播放器组件控制器（传入 Activity 上下文）
        mPlayerController = new AliPlayerController(this);

        // 2. 配置播放器组件数据
        // 优先使用设置的 Vid 和 PlayAuth，如果没有设置则使用默认值
        String vid = getVideoVid();
        String playAuth = getVideoPlayAuth();
        VideoSource.VidAuthSource videoSource = VideoSourceFactory.createVidAuthSource(vid, playAuth);
        AliPlayerModel playerModel = new AliPlayerModel.Builder().videoSource(videoSource).videoTitle("Event System Example").build();

        // 3. 绑定控制器和数据到视图
        mPlayerView.attach(mPlayerController, playerModel);
    }

    /**
     * 获取视频 Vid
     */
    private String getVideoVid() {
        String savedVid = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_VID);
        return StringUtil.isNotEmpty(savedVid) ? savedVid : SceneConstants.LANDSCAPE_SAMPLE_VID;
    }

    /**
     * 获取视频 PlayAuth
     */
    private String getVideoPlayAuth() {
        String savedPlayAuth = SPManager.getInstance().getString(LinkConstants.KEY_VIDEO_PLAY_AUTH);
        return StringUtil.isNotEmpty(savedPlayAuth) ? savedPlayAuth : SceneConstants.LANDSCAPE_SAMPLE_PLAY_AUTH;
    }

    /**
     * 设置事件总线：订阅 Info 事件，接收播放信息
     */
    private void setupEventBus() {
        // 获取事件总线单例
        mEventBus = PlayerEventBus.getInstance();

        // 自动订阅 Info 事件
        mInfoListener = event -> runOnUiThread(() -> {
            String message = getString(R.string.event_info, formatTime(event.currentPosition), formatTime(event.duration), formatTime(event.bufferedPosition));
            appendEventLine(message);
            mTvInfoOutput.setText(buildEventText());
            scrollToBottom();
        });
        // 接收事件示例：通过事件总线订阅 Info 事件，实时接收播放器状态信息
        mEventBus.subscribe(PlayerEvents.Info.class, mInfoListener);
    }

    /**
     * 设置按钮：发送 Toggle 命令，控制播放/暂停
     */
    private void setupButtons() {
        // 发送 Toggle 命令
        mBtnSendToggle.setOnClickListener(v -> {
            // 实时获取 playerId
            String playerId = null;
            if (mPlayerController != null && mPlayerController.getPlayer() != null) {
                playerId = mPlayerController.getPlayer().getPlayerId();
            }
            if (StringUtil.isNotEmpty(playerId)) {
                PlayerCommand.Toggle toggleCommand = new PlayerCommand.Toggle(playerId);
                // 发送命令示例：通过事件总线发送 Toggle 命令，控制播放器播放/暂停
                mEventBus.post(toggleCommand);
                ToastUtils.showToast(getString(R.string.toast_toggle_sent));
            }
        });
    }

    /**
     * 追加一行事件，并限制最大行数（仅保留最近 MAX_EVENT_LINES 行）
     */
    private void appendEventLine(String line) {
        mEventLines.addLast(line);
        while (mEventLines.size() > MAX_EVENT_LINES) {
            mEventLines.removeFirst();
        }
    }

    /**
     * 拼接事件文本用于显示
     */
    private String buildEventText() {
        StringBuilder sb = new StringBuilder();
        for (String line : mEventLines) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /**
     * 格式化时间（毫秒转秒）
     */
    private String formatTime(long milliseconds) {
        String format = getString(R.string.time_format_seconds);
        return String.format(format, milliseconds / 1000.0);
    }

    /**
     * 滚动 ScrollView 到底部
     */
    private void scrollToBottom() {
        if (mSvInfoOutput != null) {
            mSvInfoOutput.post(() -> mSvInfoOutput.fullScroll(View.FOCUS_DOWN));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消订阅，避免内存泄漏
        if (mInfoListener != null && mEventBus != null) {
            mEventBus.unsubscribe(PlayerEvents.Info.class, mInfoListener);
        }

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
        // 未处理，执行默认行为（关闭 Activity）
        super.onBackPressed();
    }
}
