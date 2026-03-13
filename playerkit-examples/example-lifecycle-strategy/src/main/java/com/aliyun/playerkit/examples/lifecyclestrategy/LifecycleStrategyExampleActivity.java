package com.aliyun.playerkit.examples.lifecyclestrategy;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.event.PlayerLifecycleEvents;
import com.aliyun.playerkit.player.DefaultLifecycleStrategy;
import com.aliyun.playerkit.player.IPlayerLifecycleStrategy;
import com.aliyun.playerkit.player.IdScopedPoolLifecycleStrategy;
import com.aliyun.playerkit.player.ReusePoolLifecycleStrategy;
import com.aliyun.playerkit.player.SingletonLifecycleStrategy;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.utils.FormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放器生命周期策略使用示例 Activity
 * <p>
 * 演示如何通过 {@link AliPlayerController} 注入不同的生命周期管理策略：
 * <ul>
 *     <li>默认策略 (Default)：直接创建和销毁，不复用。</li>
 *     <li>复用池策略 (ReusePool)：基于池化机制，复用空闲的播放器实例。</li>
 *     <li>ID 作用域池策略 (IdScopedPool)：基于 LRU 机制，为特定 ID 维护特定的播放器实例。</li>
 *     <li>全局单例策略 (Singleton)：始终使用唯一的全局播放器实例。</li>
 * </ul>
 * </p>
 * <p>
 * Player Lifecycle Strategy Example Activity
 * <p>
 * Demonstrates how to inject different lifecycle management strategies via {@link AliPlayerController}:
 * <ul>
 *     <li>Default Strategy (Default): Direct creation and destruction, no reuse.</li>
 *     <li>Reuse Pool Strategy (ReusePool): Based on a pooling mechanism, reuses idle player instances.</li>
 *     <li>ID Scoped Pool Strategy (IdScopedPool): Based on LRU mechanism, maintains specific instances for specific IDs.</li>
 *     <li>Singleton Strategy (Singleton): Always uses a unique global player instance.</li>
 * </ul>
 * </p>
 *
 * @author keria
 * @date 2026/01/12
 */
public class LifecycleStrategyExampleActivity extends AppCompatActivity {

    // 演示用的池容量限制
    private static final int PLAYER_POOL_MAX_SIZE = 2;

    // 播放器组件视图
    private AliPlayerView mPlayerView;

    // 当前使用的播放器生命周期策略
    private IPlayerLifecycleStrategy mLifecycleStrategy;

    // 策略选择器
    private Spinner mStrategySpinner;
    // 状态覆盖层
    private TextView mTvStatusOverlay;
    // 日志输出
    private TextView mTvLogs;

    // 视频源列表
    private final List<VideoSource> mVideoSources = new ArrayList<>();
    // 待处理的状态描述
    private String mStatusPending;

    // 日志缓存
    private final StringBuilder mLogs = new StringBuilder();

    // 事件监听器
    private PlayerEventBus.EventListener<PlayerEvents.Prepared> mPreparedListener;
    private PlayerEventBus.EventListener<PlayerLifecycleEvents.PlayerCreated> mCreatedListener;
    private PlayerEventBus.EventListener<PlayerLifecycleEvents.PlayerDestroyed> mDestroyedListener;
    private PlayerEventBus.EventListener<PlayerLifecycleEvents.PlayerReused> mReusedListener;
    private PlayerEventBus.EventListener<PlayerLifecycleEvents.PlayerHit> mHitListener;
    private PlayerEventBus.EventListener<PlayerLifecycleEvents.PlayerEvicted> mEvictedListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_lifecycle_strategy_example);

        initVideoSources();
        initViews();
        initStrategySelector();
        setupEventBus();
    }

    /**
     * 初始化视频源列表
     */
    private void initVideoSources() {
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
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        mPlayerView = findViewById(R.id.v_player_kit);
        mStrategySpinner = findViewById(R.id.strategy_spinner);
        mTvStatusOverlay = findViewById(R.id.tv_status_overlay);
        mTvLogs = findViewById(R.id.tv_logs);

        findViewById(R.id.btn_video_1).setOnClickListener(v -> playVideo(0));
        findViewById(R.id.btn_video_2).setOnClickListener(v -> playVideo(1));
        findViewById(R.id.btn_video_3).setOnClickListener(v -> playVideo(2));
    }

    /**
     * 初始化策略选择器
     */
    private void initStrategySelector() {
        String[] strategies = {
                getString(R.string.strategy_default),
                getString(R.string.strategy_reuse_pool),
                getString(R.string.strategy_id_scoped),
                getString(R.string.strategy_singleton)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strategies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStrategySpinner.setAdapter(adapter);

        mStrategySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchStrategy(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 设置事件总线监听
     * <p>
     * 通过监听 StrategyEvents 来洞察策略内部的创建、复用、置换等动作。
     * </p>
     */
    private void setupEventBus() {
        PlayerEventBus eventBus = PlayerEventBus.getInstance();

        // 1. 监听 Prepared 事件，此时播放器实例已就绪，同步更新 UI 状态
        mPreparedListener = event -> {
            String playerId = event.playerId;
            runOnUiThread(() -> {
                updateUI(mStatusPending);
                addLog("Event: Prepared | PlayerID: " + playerId + " | Status: " + mStatusPending);
            });
        };
        eventBus.subscribe(PlayerEvents.Prepared.class, mPreparedListener);

        // 2. 监听策略内部动作事件
        mCreatedListener = event -> runOnUiThread(() -> {
            mStatusPending = getString(R.string.strategy_status_new);
            addLog("Strategy: [CREATED] " + event.playerId);
        });
        eventBus.subscribe(PlayerLifecycleEvents.PlayerCreated.class, mCreatedListener);

        mDestroyedListener = event -> runOnUiThread(() -> addLog("Strategy: [DESTROYED] " + event.playerId));
        eventBus.subscribe(PlayerLifecycleEvents.PlayerDestroyed.class, mDestroyedListener);

        mReusedListener = event -> runOnUiThread(() -> {
            mStatusPending = getString(R.string.strategy_status_reused);
            addLog("Strategy: [REUSED] " + event.playerId);
        });
        eventBus.subscribe(PlayerLifecycleEvents.PlayerReused.class, mReusedListener);

        mHitListener = event -> runOnUiThread(() -> {
            mStatusPending = getString(R.string.strategy_status_hit);
            addLog("Strategy: [HIT] " + event.playerId);
        });
        eventBus.subscribe(PlayerLifecycleEvents.PlayerHit.class, mHitListener);

        mEvictedListener = event -> runOnUiThread(() -> addLog("Strategy: [EVICTED] " + event.playerId));
        eventBus.subscribe(PlayerLifecycleEvents.PlayerEvicted.class, mEvictedListener);
    }

    /**
     * 切换生命周期管理策略
     *
     * @param position 策略索引
     */
    private void switchStrategy(int position) {
        // 核心步骤：彻底清理旧资源（解绑 View、销毁 Controller、清理旧策略）
        cleanup();

        // 根据选择实例化新策略
        switch (position) {
            case 0: // Default
                mLifecycleStrategy = new DefaultLifecycleStrategy();
                break;
            case 1: // Reuse Pool
                ReusePoolLifecycleStrategy reusePool = ReusePoolLifecycleStrategy.getInstance();
                reusePool.setMaxPoolSize(PLAYER_POOL_MAX_SIZE);
                mLifecycleStrategy = reusePool;
                break;
            case 2: // Id Scoped Pool
                IdScopedPoolLifecycleStrategy idScopedPool = IdScopedPoolLifecycleStrategy.getInstance();
                idScopedPool.setMaxPoolSize(PLAYER_POOL_MAX_SIZE);
                mLifecycleStrategy = idScopedPool;
                break;
            case 3: // Global Singleton
                mLifecycleStrategy = SingletonLifecycleStrategy.getInstance();
                break;
        }

        // 重置环境状态
        mStatusPending = null;
        addLog("------------------------------------------");
        addLog("Switch: Strategy -> " + mLifecycleStrategy.getClass().getSimpleName());
        mTvStatusOverlay.setText(R.string.strategy_status_ready);
    }

    /**
     * 执行播放
     *
     * @param index 视频索引
     */
    private void playVideo(int index) {
        if (index < 0 || index >= mVideoSources.size()) return;
        VideoSource source = mVideoSources.get(index);

        // 处理视图层面的重置 (View 内部会调用旧控制器的 destroy)
        mPlayerView.detach();

        // 核心步骤：不再复用控制器实例，而是根据当前策略创建一个新的控制器
        // 既然旧的控制器已被 destroy，如果要继续播放新视频且保持 View 绑定，必须使用新的控制器实例
        AliPlayerController controller;
        if (mLifecycleStrategy != null) {
            controller = new AliPlayerController(this, mLifecycleStrategy);
        } else {
            controller = new AliPlayerController(this);
        }

        // 构造数据模型
        AliPlayerModel model = new AliPlayerModel.Builder()
                .videoSource(source)
                .build();

        // 核心 API: 绑定视图、控制器和数据模型
        mPlayerView.attach(controller, model);
    }

    /**
     * 内部清理逻辑
     */
    private void cleanup() {
        // 解绑播放器组件，停止渲染
        if (mPlayerView != null) {
            mPlayerView.detach();
        }
        // 销毁策略环境，清理策略内部持有的播放器实例
        if (mLifecycleStrategy != null) {
            mLifecycleStrategy.clear();
        }
    }

    /**
     * 更新 UI 状态覆盖层
     */
    private void updateUI(String status) {
        if (status == null) {
            mTvStatusOverlay.setText(R.string.strategy_status_idle);
        } else {
            mTvStatusOverlay.setText(status.toUpperCase());
        }
    }

    /**
     * 添加展示日志
     */
    private void addLog(String message) {
        String time = FormatUtil.formatCurrentTime();
        mLogs.insert(0, "[" + time + "] " + message + "\n");
        mTvLogs.setText(mLogs.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消订阅事件总线，避免内存泄漏
        PlayerEventBus eventBus = PlayerEventBus.getInstance();
        if (mPreparedListener != null) {
            eventBus.unsubscribe(PlayerEvents.Prepared.class, mPreparedListener);
        }
        if (mCreatedListener != null) {
            eventBus.unsubscribe(PlayerLifecycleEvents.PlayerCreated.class, mCreatedListener);
        }
        if (mDestroyedListener != null) {
            eventBus.unsubscribe(PlayerLifecycleEvents.PlayerDestroyed.class, mDestroyedListener);
        }
        if (mReusedListener != null) {
            eventBus.unsubscribe(PlayerLifecycleEvents.PlayerReused.class, mReusedListener);
        }
        if (mHitListener != null) {
            eventBus.unsubscribe(PlayerLifecycleEvents.PlayerHit.class, mHitListener);
        }
        if (mEvictedListener != null) {
            eventBus.unsubscribe(PlayerLifecycleEvents.PlayerEvicted.class, mEvictedListener);
        }

        // 执行终态清理
        cleanup();
    }
}
