package com.aliyun.playerkit.scenes.playlist;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.player.BaseLifecycleStrategy;
import com.aliyun.playerkit.player.SingletonLifecycleStrategy;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表播放演示 Activity
 * <p>
 * 演示如何使用列表播放功能，实现多个视频的连续播放。
 * 当第一个视频播放完成后，会自动播放下一个视频。
 * </p>
 * <p>
 * 集成FAQ (Integration FAQ)
 * 本示例依赖 scene-common 模块，集成时请：
 * 1. 如自行实现视频源，可移除 scene-common 依赖
 * 2. 修改 initVideoSources() 方法，替换为您的视频源获取逻辑
 * </p>
 * <p>
 * 设计说明：
 * - mPlayerView 可以复用，整个生命周期只有一个实例
 * - 使用两个控制器：mCurrentController（当前播放）和 mNextController（下一个预加载）
 * - 当当前视频播放完成时，将 mNextController 切换为 mCurrentController，并创建新的 mNextController
 * - 通过这种方式可以实现无限后续播放，无需管理多个控制器
 * </p>
 * <p>
 * Playlist Demo Activity
 * <p>
 * Demonstrates how to use the playlist feature to achieve continuous playback of multiple videos.
 * When the first video finishes playing, the next video will automatically start playing.
 * </p>
 * <p>
 * Design Notes:
 * - mPlayerView can be reused, only one instance throughout the lifecycle
 * - Use two controllers: mCurrentController (current playback) and mNextController (next preload)
 * - When current video finishes, switch mNextController to mCurrentController and create a new mNextController
 * - This approach enables infinite subsequent playback without managing multiple controllers
 * </p>
 *
 * @author keria
 * @date 2026/01/06
 */
public class PlaylistActivity extends AppCompatActivity {

    /**
     * 播放器组件视图（复用，整个生命周期只有一个实例）
     */
    private AliPlayerView mPlayerView;

    /**
     * 播放器生命周期策略实例
     */
    private BaseLifecycleStrategy mLifecycleStrategy;

    /**
     * 当前播放的控制器
     */
    @Nullable
    private AliPlayerController mCurrentController;

    /**
     * 下一个预加载的控制器
     */
    @Nullable
    private AliPlayerController mNextController;

    /**
     * 事件总线实例
     */
    private PlayerEventBus mEventBus;

    /**
     * 状态变化事件监听器（全局唯一，通过 playerId 区分不同控制器的事件）
     */
    private PlayerEventBus.EventListener<PlayerEvents.StateChanged> mStateChangedListener;

    /**
     * 当前播放的视频索引
     */
    private int mCurrentVideoIndex = 0;

    /**
     * 视频源列表（支持无限视频源）
     */
    private final List<VideoSource> mVideoSources = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        initVideoSources();
        initPlayerKit();
        setupEventBus();
    }

    /**
     * 初始化视频源列表
     * <p>
     * 集成FAQ (Integration FAQ)
     * 请替换为您的视频源获取逻辑，例如：
     * <pre>
     * private void initVideoSources() {
     *     mVideoSources.add(VideoSourceFactory.createVidAuthSource("您的 Vid", "您的 PlayAuth"));
     *     mVideoSources.add(VideoSourceFactory.createUrlSource("您的视频 URL"));
     *     // 或从您的业务接口获取视频列表
     * }
     * </pre>
     * </p>
     */
    private void initVideoSources() {
        // Demo 演示：使用 scene-common 中的示例视频，集成时请替换
        // 第一个视频源：横屏 VidAuthSource
        mVideoSources.add(VideoSourceFactory.createVidAuthSource(
                SceneConstants.LANDSCAPE_SAMPLE_VID,
                SceneConstants.LANDSCAPE_SAMPLE_PLAY_AUTH
        ));

        // 第二个视频源：竖屏 VidAuthSource
        mVideoSources.add(VideoSourceFactory.createVidAuthSource(
                SceneConstants.PORTRAIT_SAMPLE_VID,
                SceneConstants.PORTRAIT_SAMPLE_PLAY_AUTH
        ));

        // 第三个视频源：UrlSource
        mVideoSources.add(VideoSourceFactory.createUrlSource(
                SceneConstants.SAMPLE_VIDEO_URL
        ));

        // Note keria: 可以继续添加更多视频源，实现无限列表播放
        // 但从性能上来讲，全局最多只有两个控制器实例，因此不会出现性能压力
    }

    /**
     * 初始化 AliPlayerKit 播放组件
     */
    private void initPlayerKit() {
        // 获取播放器视图（复用，整个生命周期只有一个实例）
        mPlayerView = findViewById(R.id.v_player_kit);

        // 初始化播放器生命周期策略
        // Note keria:
        // 当前示例使用的是单实例播放器生命周期策略（SingletonLifecycleStrategy），在应用进程内全局复用同一个 Player 实例。
        // 在该策略下，Player 实例不会随着 Activity 的退出而销毁，而是继续保留在内存中，供后续页面或场景复用，从而避免频繁创建和销毁 Player 实例带来的性能开销。
        // 注意：这只是一个示例，在实际业务中，您可以结合具体需求和业务场景，根据页面结构和资源管理策略，选择合适的播放器生命周期策略。
        // 不同策略的具体使用方式，可参考 example-lifecycle-strategy 中的播放器生命周期策略使用示例。
        mLifecycleStrategy = SingletonLifecycleStrategy.getInstance();

        // 创建并绑定当前控制器（第一个视频）
        createAndAttachCurrentController();

        // 预加载下一个控制器（如果存在下一个视频）
        preloadNextController();
    }

    /**
     * 创建并绑定当前控制器
     */
    private void createAndAttachCurrentController() {
        if (mCurrentVideoIndex < 0 || mCurrentVideoIndex >= mVideoSources.size()) {
            return;
        }

        VideoSource videoSource = mVideoSources.get(mCurrentVideoIndex);
        if (videoSource == null) {
            return;
        }

        // 1. 创建当前控制器
        mCurrentController = new AliPlayerController(this, mLifecycleStrategy);

        // 2. 准备播放数据
        String videoTitle = getString(R.string.playlist_video_title, mCurrentVideoIndex + 1);
        AliPlayerModel playerModel = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle(videoTitle)
                .build();

        // 3. 绑定控制器和数据到视图
        mPlayerView.attach(mCurrentController, playerModel);
    }

    /**
     * 预加载下一个控制器
     */
    private void preloadNextController() {
        int nextIndex = mCurrentVideoIndex + 1;
        if (nextIndex < 0 || nextIndex >= mVideoSources.size()) {
            // 没有下一个视频，不需要预加载
            return;
        }

        VideoSource nextVideoSource = mVideoSources.get(nextIndex);
        if (nextVideoSource == null) {
            return;
        }

        // 创建下一个控制器（但不绑定到视图，只是预创建）
        mNextController = new AliPlayerController(this, mLifecycleStrategy);

        // Note keria: 这地方还可以做一些其它优化，比如进行预加载、预渲染等操作。
    }

    /**
     * 设置事件总线：订阅 StateChanged 事件，监听播放完成
     * <p>
     * 使用全局唯一的事件监听器，通过 playerId 区分不同控制器的事件
     * </p>
     */
    private void setupEventBus() {
        // 获取事件总线单例
        mEventBus = PlayerEventBus.getInstance();

        // 创建全局唯一的状态变化监听器，通过 playerId 区分不同控制器的事件
        mStateChangedListener = event -> {
            // 检查事件是否来自当前控制器
            if (mCurrentController != null && mCurrentController.getPlayer() != null) {
                String playerId = mCurrentController.getPlayer().getPlayerId();
                if (event.newState == PlayerState.COMPLETED) {
                    if (StringUtil.equals(playerId, event.playerId)) {
                        runOnUiThread(() -> playNextVideo());
                    }
                }
            }
        };

        // 订阅事件
        mEventBus.subscribe(PlayerEvents.StateChanged.class, mStateChangedListener);
    }

    /**
     * 播放下一个视频
     * <p>
     * 通过将 mNextController 切换为 mCurrentController，并创建新的 mNextController 来实现无限后续播放
     * </p>
     */
    private void playNextVideo() {
        int nextIndex = mCurrentVideoIndex + 1;
        if (nextIndex < 0 || nextIndex >= mVideoSources.size()) {
            // 已经是最后一个视频，不再切换
            return;
        }

        // 解绑当前控制器
        if (mPlayerView != null) {
            mPlayerView.detach();
        }

        // 将下一个控制器切换为当前控制器
        mCurrentController = mNextController;
        mNextController = null;

        // 更新索引
        mCurrentVideoIndex = nextIndex;

        // 准备下一个视频的数据并绑定到视图
        VideoSource videoSource = mVideoSources.get(mCurrentVideoIndex);
        if (videoSource != null && mCurrentController != null) {
            String videoTitle = getString(R.string.playlist_video_title, mCurrentVideoIndex + 1);
            AliPlayerModel playerModel = new AliPlayerModel.Builder()
                    .videoSource(videoSource)
                    .videoTitle(videoTitle)
                    .build();

            // 绑定控制器和数据到视图
            mPlayerView.attach(mCurrentController, playerModel);
        }

        // 预加载下一个控制器（如果还有下一个视频）
        preloadNextController();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消事件监听器的订阅，避免内存泄漏
        if (mStateChangedListener != null && mEventBus != null) {
            mEventBus.unsubscribe(PlayerEvents.StateChanged.class, mStateChangedListener);
            mStateChangedListener = null;
        }

        // 解绑播放器组件，释放资源（会销毁当前绑定的控制器）
        if (mPlayerView != null) {
            mPlayerView.detach();
        }

        // 销毁生命周期管理策略，促使策略内部按需销毁播放器实例
        if (mLifecycleStrategy != null) {
            mLifecycleStrategy.clear();
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
