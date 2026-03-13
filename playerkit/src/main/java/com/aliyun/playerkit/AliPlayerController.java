package com.aliyun.playerkit;

import android.content.Context;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.aliyun.player.videoview.AliDisplayView;
import com.aliyun.playerkit.core.IPlayerController;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.player.DefaultPlayerFactory;
import com.aliyun.playerkit.player.IMediaPlayer;
import com.aliyun.playerkit.player.IPlayerFactory;
import com.aliyun.playerkit.player.IPlayerLifecycleStrategy;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.player.DefaultLifecycleStrategy;
import com.aliyun.playerkit.strategy.StrategyRegistry;
import com.aliyun.playerkit.strategy.StrategyContext;
import com.aliyun.playerkit.strategy.StrategyManager;
import com.aliyun.playerkit.utils.StringUtil;

/**
 * AliPlayerKit 播放控制器
 * <p>
 * 负责管理播放器的完整生命周期，包括初始化、配置、播放控制、状态管理等。
 * 实现了 {@link IPlayerController} 接口，提供标准的播放器控制能力。
 * 同时实现了 {@link LifecycleEventObserver} 接口，支持生命周期感知。
 * </p>
 * <p>
 * AliPlayerKit Player Controller Implementation
 * <p>
 * Manages the complete lifecycle of the player, including initialization, configuration, playback control, state management, etc.
 * Implements the {@link IPlayerController} interface, providing standard player control capabilities.
 * Also implements the {@link LifecycleEventObserver} interface, supporting lifecycle awareness.
 * </p>
 *
 * @author keria
 * @date 2025/11/21
 */
public class AliPlayerController implements IPlayerController, LifecycleEventObserver {

    private static final String TAG = "AliPlayerController";

    // ==================== 依赖注入 ====================

    /**
     * 应用上下文
     * <p>
     * Android Context
     * </p>
     */
    @NonNull
    private final Context context;

    /**
     * 播放器生命周期策略
     * <p>
     * 负责管理播放器实例的获取和释放。
     * </p>
     */
    @NonNull
    private final IPlayerLifecycleStrategy lifecycleStrategy;

    /**
     * 播放器工厂接口
     */
    @NonNull
    private final IPlayerFactory playerFactory;

    // ==================== 播放器实例 ====================

    /**
     * 播放器接口实例
     */
    @Nullable
    private IMediaPlayer player;

    /**
     * 当前播放数据配置
     */
    @Nullable
    private AliPlayerModel model;

    /**
     * 是否已初始化
     * <p>
     * 用于防止重复初始化和确保方法调用的正确顺序。
     * </p>
     * <p>
     * Whether Player is Initialized
     * <p>
     * Used to prevent duplicate initialization and ensure correct method call order.
     * </p>
     */
    private boolean initialized = false;

    /**
     * 记录暂停前的播放状态
     * <p>
     * 用于在 onResume 时恢复播放状态。
     * 如果在 onPause 时是播放状态，则设为 true，onResume 时会自动恢复播放。
     * </p>
     */
    private boolean wasPlayingBeforePause = false;

    /**
     * 策略管理器
     * <p>
     * 管理所有策略的生命周期，包括注册、启动、停止、重置等。
     * </p>
     */
    @NonNull
    private final StrategyManager strategyManager = new StrategyManager();

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * <p>
     * Constructor
     * </p>
     *
     * @param context 应用上下文
     */
    public AliPlayerController(@NonNull Context context) {
        this(context, new DefaultLifecycleStrategy());
    }

    /**
     * 构造函数（支持自定义生命周期策略）
     * <p>
     * 允许注入自定义的生命周期管理策略。
     * </p>
     * <p>
     * Constructor (with Custom Lifecycle Strategy)
     * <p>
     * Allows injection of custom lifecycle management strategy.
     * </p>
     *
     * @param context           应用上下文
     * @param lifecycleStrategy 生命周期管理策略
     */
    public AliPlayerController(@NonNull Context context, @NonNull IPlayerLifecycleStrategy lifecycleStrategy) {
        if (context == null) {
            throw new NullPointerException("Context cannot be null");
        }
        if (lifecycleStrategy == null) {
            throw new NullPointerException("Lifecycle strategy cannot be null");
        }
        this.context = context;

        this.lifecycleStrategy = lifecycleStrategy;
        this.playerFactory = new DefaultPlayerFactory();

        // 初始化生命周期策略（只初始化一次，策略内部应保持幂等）
        this.lifecycleStrategy.init(playerFactory);

        LogHub.i(TAG, "Controller created with lifecycle strategy: " + lifecycleStrategy.getClass().getSimpleName());

        // 订阅命令事件（不依赖具体 player 实例）
        subscribeCommands();

        // 应用全局默认策略
        StrategyRegistry.applyTo(strategyManager);
    }

    // ==================== IPlayerController 实现 ====================

    /**
     * 配置播放数据
     * <p>
     * 设置视频源、场景类型、封面图等播放相关配置。
     * 配置完成后，播放器将准备播放，但不会自动开始播放（除非 autoPlay 为 true）。
     * </p>
     * <p>
     * Configure Playback Data
     * <p>
     * Sets video source, scene type, cover image, and other playback-related configurations.
     * After configuration, the player will prepare for playback but will not automatically start (unless autoPlay is true).
     * </p>
     *
     * @param model 播放器数据配置，包含视频源、场景类型等信息，不能为 null
     * @throws IllegalStateException    如果播放器未初始化或播放器实例为 null
     * @throws IllegalArgumentException 如果 model 为 null 或配置无效（如不支持的视频源类型）
     * @throws RuntimeException         如果配置过程中发生其他异常
     */
    @Override
    public void configure(@NonNull AliPlayerModel model) {
        if (model == null) {
            throw new IllegalArgumentException("Player model cannot be null");
        }

        // 1. 保存配置数据，用于后续操作（如跳转到起始时间）
        boolean isFirstConfigure = (this.model == null);
        this.model = model;

        // 2. 基于业务模型生成唯一标识，用于生命周期策略管理
        String uniqueId = buildUniqueId(model);

        // 3. 如果还没有播放器实例，则通过生命周期策略按 uniqueId 获取
        if (player == null) {
            try {
                IMediaPlayer internalPlayer = lifecycleStrategy.acquire(context, uniqueId);
                if (internalPlayer == null) {
                    throw new IllegalStateException("LifecycleStrategy.acquire() returned null");
                }
                this.player = internalPlayer;
                initialized = true;
                LogHub.i(TAG, "Player acquired for uniqueId=" + uniqueId + ", playerId=" + player.getPlayerId());
            } catch (Exception e) {
                LogHub.e(TAG, "Failed to acquire player for uniqueId=" + uniqueId, e);
                throw new IllegalStateException("Failed to acquire player", e);
            }
        }

        if (!isPlayerReady()) {
            throw new IllegalStateException("Player instance is not ready");
        }

        LogHub.i(TAG, "Configure player, uniqueId=" + uniqueId + ", scene=" + model.getSceneType() + ", source=" + model.getVideoSource());

        // 4. 启动或更新策略上下文
        StrategyContext strategyContext = new StrategyContext(player.getPlayerId(), model, getStateStore());
        if (isFirstConfigure) {
            strategyManager.start(strategyContext);
        } else {
            strategyManager.updateContext(strategyContext);
        }

        // 5. 配置视频源
        player.setDataSource(model);
    }

    /**
     * 获取播放器实例
     * <p>
     * 提供对底层播放器的直接访问。
     * </p>
     *
     * @return 播放器实例，如果未初始化可能为 null
     */
    @Override
    @Nullable
    public IMediaPlayer getPlayer() {
        return player;
    }

    /**
     * 销毁播放器
     * <p>
     * 释放所有播放器资源，包括停止播放、释放播放器实例、清空所有引用等。
     * 调用此方法后，播放器将无法继续使用，需要重新创建控制器实例。
     * </p>
     * <p>
     * Destroy Player
     * <p>
     * Releases all player resources, including stopping playback, releasing player instance, clearing all references, etc.
     * After calling this method, the player cannot be used anymore, and a new controller instance needs to be created.
     * </p>
     */
    @Override
    public void destroy() {
        LogHub.i(TAG, "Destroy controller");

        // 释放播放器资源
        if (player != null) {
            try {
                // 回收播放器实例
                String uniqueId = (model != null) ? buildUniqueId(model) : "unknown";
                lifecycleStrategy.recycle(player, uniqueId, false);
                LogHub.i(TAG, "Player recycled");
            } catch (Exception e) {
                LogHub.e(TAG, "Failed to destroy", e);
            }
            player = null;
        }

        // 销毁策略管理器
        strategyManager.destroy();

        // 清空引用，避免内存泄漏
        model = null;
        initialized = false;
        wasPlayingBeforePause = false;

        // 取消订阅事件
        unsubscribeCommands();

        LogHub.i(TAG, "Controller destroyed");
    }

    // ==================== 公开方法 ====================

    /**
     * 获取当前播放数据配置
     * <p>
     * 返回最近一次调用 {@link #configure(AliPlayerModel)} 时传入的配置数据。
     * </p>
     * <p>
     * Get Current Playback Data Configuration
     * <p>
     * Returns the configuration data passed in the last call to {@link #configure(AliPlayerModel)}.
     * </p>
     *
     * @return 播放数据配置，如果未配置则返回 null
     */
    @Nullable
    public AliPlayerModel getModel() {
        return model;
    }

    /**
     * 获取策略管理器
     * <p>
     * 用于注册策略，监听播放器事件并执行相应的业务逻辑。
     * </p>
     * <p>
     * Get Strategy Manager
     * <p>
     * Used to register strategies that listen to player events and execute corresponding business logic.
     * </p>
     *
     * @return 策略管理器
     */
    @NonNull
    public StrategyManager getStrategyManager() {
        return strategyManager;
    }

    /**
     * 获取播放器状态存储
     *
     * @return 播放器状态存储，如果播放器未初始化可能为 null
     */
    @NonNull
    @Override
    public IPlayerStateStore getStateStore() {
        if (!isPlayerReady()) {
            throw new IllegalStateException("Player not ready, cannot get state store");
        }
        return player.getStateStore();
    }

    /**
     * 设置播放器显示视图
     * <p>
     * 从插槽获取视图并设置给播放器实例。
     * 如果 displayView 为 null，则设置为 null。
     * </p>
     *
     * @param displayView 播放器显示视图，从插槽获取。如果为 null，则设置为 null
     */
    public void setDisplayView(@Nullable AliDisplayView displayView) {
        if (!isPlayerReady()) {
            LogHub.w(TAG, "Player not ready, cannot set display view");
            return;
        }

        if (displayView != null) {
            player.setDisplayView(displayView);
            LogHub.i(TAG, "DisplayView set to player instance");
        } else {
            player.setDisplayView(null);
            LogHub.w(TAG, "DisplayView is null, player will not have display view");
        }
    }

    /**
     * 设置播放器 Surface
     * <p>
     * 用于支持 SurfaceView 和 TextureView。
     * 如果 surface 为 null，则清理播放器的 Surface。
     * </p>
     *
     * @param surface Surface 实例，如果为 null 则清理
     */
    public void setSurface(@Nullable Surface surface) {
        if (!isPlayerReady()) {
            LogHub.w(TAG, "Player not ready, cannot set surface");
            return;
        }

        if (surface != null) {
            player.setSurface(surface);
            LogHub.i(TAG, "Surface set to player instance");
        } else {
            player.setSurface(null);
            LogHub.i(TAG, "Surface cleared from player instance");
        }
    }

    /**
     * 通知播放器 Surface 已变化
     * <p>
     * 当 Surface 尺寸或格式发生变化时调用。
     * </p>
     */
    public void surfaceChanged() {
        if (!isPlayerReady()) {
            LogHub.w(TAG, "Player not ready, cannot notify surface changed");
            return;
        }

        player.surfaceChanged();
        LogHub.i(TAG, "Surface changed notified to player instance");
    }

    // ==================== 私有方法 ====================

    /**
     * 检查播放器是否已准备好（已初始化且实例不为 null）
     * <p>
     * 用于在执行播放器操作前验证播放器状态。
     * </p>
     * <p>
     * Check if Player is Ready
     * <p>
     * Used to validate player state before performing player operations.
     * </p>
     *
     * @return true 如果播放器已初始化且实例不为 null，否则 false
     */
    private boolean isPlayerReady() {
        return initialized && player != null;
    }

    /**
     * 基于业务模型构建唯一标识
     * <p>
     * 默认实现使用场景类型 + 视频源信息的组合，调用方可以在模型中扩展更精细的唯一键。
     * </p>
     *
     * @param model 播放器数据模型
     * @return 唯一标识字符串
     */
    @NonNull
    private String buildUniqueId(@NonNull AliPlayerModel model) {
        return model.getVideoSource().toString();
    }

    // ==================== LifecycleObserver 实现 ====================

    /**
     * Lifecycle 事件回调
     *
     * @param owner 生命周期所有者
     * @param event 生命周期事件
     */
    @Override
    public void onStateChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_RESUME:
                LogHub.w(TAG, "onResume (from LifecycleOwner)");
                handleResume();
                break;
            case ON_PAUSE:
                LogHub.w(TAG, "onPause (from LifecycleOwner)");
                handlePause();
                break;
            case ON_DESTROY:
                LogHub.w(TAG, "onDestroy (from LifecycleOwner)");
                destroy();
                break;
            default:
                // no-op
        }
    }

    // ==================== 手动生命周期方法（用于不支持 LifecycleOwner 的场景）====================

    /**
     * 手动处理 onResume 生命周期
     * <p>
     * 用于不支持 {@link LifecycleOwner} 的场景（如原生 Activity/Fragment）。
     * 如果未使用 {@link AliPlayerView#bindLifecycle(LifecycleOwner)}，可以在
     * Activity/Fragment 的 {@code onResume()} 方法中手动调用此方法。
     * </p>
     * <p>
     * <strong>使用示例</strong>：
     * <pre>
     * // 在原生 Activity 中
     * {@literal @}Override
     * protected void onResume() {
     *     super.onResume();
     *     AliPlayerController controller = playerKit.getController();
     *     if (controller != null) {
     *         controller.onResume();
     *     }
     * }
     * </pre>
     * </p>
     * <p>
     * Manually Handle onResume Lifecycle
     * <p>
     * For scenarios that do not support {@link LifecycleOwner} (such as native Activity/Fragment).
     * If {@link AliPlayerView#bindLifecycle(LifecycleOwner)} is not used, this method can be manually called
     * in the Activity/Fragment's {@code onResume()} method.
     * </p>
     */
    public void onResume() {
        LogHub.i(TAG, "onResume (manual)");
        handleResume();
    }

    /**
     * 手动处理 onPause 生命周期
     * <p>
     * 用于不支持 {@link LifecycleOwner} 的场景。
     * 在 Activity/Fragment 的 {@code onPause()} 方法中手动调用。
     * </p>
     * <p>
     * Manually Handle onPause Lifecycle
     * <p>
     * For scenarios that do not support {@link LifecycleOwner}.
     * Manually call in the Activity/Fragment's {@code onPause()} method.
     * </p>
     */
    public void onPause() {
        LogHub.i(TAG, "onPause (manual)");
        handlePause();
    }

    /**
     * 处理恢复逻辑（内部方法）
     * <p>
     * 当 Activity/Fragment 恢复时执行的操作。
     * 目前为空实现，可以根据需要添加恢复播放等逻辑。
     * </p>
     * <p>
     * <strong>可能的实现</strong>：
     * <ul>
     *     <li>如果之前是暂停状态，可以自动恢复播放</li>
     *     <li>重新连接网络资源</li>
     *     <li>更新 UI 状态</li>
     * </ul>
     * </p>
     * <p>
     * Handle Resume Logic (Internal Method)
     * <p>
     * Operations performed when Activity/Fragment resumes.
     * Currently empty implementation, can add resume playback logic as needed.
     * </p>
     */
    private void handleResume() {
        if (wasPlayingBeforePause) {
            LogHub.i(TAG, "Resuming playback from lifecycle");
            if (isPlayerReady()) {
                player.start();
            }
            wasPlayingBeforePause = false;
        }
    }

    /**
     * 处理暂停逻辑（内部方法）
     * <p>
     * 当 Activity/Fragment 暂停时执行的操作。
     * 目前为空实现，可以根据需要添加暂停播放等逻辑。
     * </p>
     * <p>
     * <strong>可能的实现</strong>：
     * <ul>
     *     <li>自动暂停播放以节省资源</li>
     *     <li>保存当前播放位置</li>
     *     <li>释放部分资源</li>
     * </ul>
     * </p>
     * <p>
     * Handle Pause Logic (Internal Method)
     * <p>
     * Operations performed when Activity/Fragment pauses.
     * Currently empty implementation, can add pause playback logic as needed.
     * </p>
     */
    private void handlePause() {
        if (!isPlayerReady()) {
            return;
        }
        PlayerState currentState = player.getStateStore().getPlayState();
        if (currentState == PlayerState.PLAYING) {
            LogHub.i(TAG, "Pausing playback from lifecycle");
            player.pause();
            wasPlayingBeforePause = true;
        } else {
            wasPlayingBeforePause = false;
        }
    }

    /**
     * 处理切换逻辑（内部方法）
     * <p>
     * 执行切换操作，如果当前正在播放，则暂停播放；如果当前暂停，则恢复播放。
     * 切换后，播放器状态会切换为 PLAYING 或 PAUSED。
     * </p>
     * Handle Switch Logic (Internal Method)
     * <p>
     * Performs switch operation, pauses playback if currently playing, resumes playback if currently paused.
     * After switching, the player state will be PLAYING or PAUSED.
     * </p>
     */
    private void handleToggle() {
        if (!isPlayerReady()) {
            return;
        }
        player.toggle();
    }

    // ==================== 命令处理 ====================

    /**
     * 处理播放控制命令
     * <p>
     * 根据命令类型执行相应的操作。
     * </p>
     * <p>
     * Handle Playback Control Commands
     * <p>
     * Executes operations based on command type.
     * </p>
     */
    private final PlayerEventBus.EventListener<PlayerCommand> commandListener = command -> {
        if (!checkCommandTarget(command)) {
            return;
        }

        if (command instanceof PlayerCommand.Play) {
            onPlayCommand((PlayerCommand.Play) command);
        } else if (command instanceof PlayerCommand.Pause) {
            onPauseCommand((PlayerCommand.Pause) command);
        } else if (command instanceof PlayerCommand.Toggle) {
            onToggleCommand((PlayerCommand.Toggle) command);
        } else if (command instanceof PlayerCommand.Stop) {
            onStopCommand((PlayerCommand.Stop) command);
        } else if (command instanceof PlayerCommand.Replay) {
            onReplayCommand((PlayerCommand.Replay) command);
        } else if (command instanceof PlayerCommand.Seek) {
            onSeekCommand((PlayerCommand.Seek) command);
        } else if (command instanceof PlayerCommand.SetSpeed) {
            onSetSpeedCommand((PlayerCommand.SetSpeed) command);
        } else if (command instanceof PlayerCommand.Snapshot) {
            onSnapshotCommand((PlayerCommand.Snapshot) command);
        } else if (command instanceof PlayerCommand.SetLoop) {
            onSetLoopCommand((PlayerCommand.SetLoop) command);
        } else if (command instanceof PlayerCommand.SetMute) {
            onSetMuteCommand((PlayerCommand.SetMute) command);
        } else if (command instanceof PlayerCommand.SetScaleType) {
            onSetScaleTypeCommand((PlayerCommand.SetScaleType) command);
        } else if (command instanceof PlayerCommand.SetMirrorType) {
            onSetMirrorTypeCommand((PlayerCommand.SetMirrorType) command);
        } else if (command instanceof PlayerCommand.SetRotation) {
            onSetRotationCommand((PlayerCommand.SetRotation) command);
        } else if (command instanceof PlayerCommand.SelectTrack) {
            onSelectTrackCommand((PlayerCommand.SelectTrack) command);
        } else {
            LogHub.w(TAG, "Unknown PlayerCommand: " + command);
        }
    };

    /**
     * 订阅播放控制命令
     * <p>
     * 订阅 {@link PlayerCommand} 事件，以便接收来自 UI 的播放控制请求。
     * </p>
     * <p>
     * Subscribe to Playback Control Commands
     * <p>
     * Subscribes to {@link PlayerCommand} events to receive playback control requests from the UI.
     * </p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void subscribeCommands() {
        for (Class<? extends PlayerCommand> commandClass : PlayerCommand.ALL_COMMANDS) {
            PlayerEventBus.getInstance().subscribe(commandClass, (PlayerEventBus.EventListener) commandListener);
        }
        LogHub.i(TAG, "Subscribed to " + PlayerCommand.ALL_COMMANDS.length + " PlayerCommand events");
    }

    /**
     * 取消订阅播放控制命令
     * <p>
     * 在停止使用播放器时调用，避免内存泄漏。
     * </p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void unsubscribeCommands() {
        for (Class<? extends PlayerCommand> commandClass : PlayerCommand.ALL_COMMANDS) {
            PlayerEventBus.getInstance().unsubscribe(commandClass, (PlayerEventBus.EventListener) commandListener);
        }
        LogHub.i(TAG, "Unsubscribed from PlayerCommand events");
    }

    /**
     * 检查命令目标是否匹配
     * <p>
     * 验证命令的目标播放器是否与当前控制器管理的播放器匹配。
     * 如果命令的 playerId 为空，则忽略命令。
     * </p>
     *
     * @param command 播放命令，不能为 null
     * @return true 表示命令目标匹配，可以处理该命令；false 表示命令目标不匹配，忽略该命令。
     */
    private boolean checkCommandTarget(@NonNull PlayerCommand command) {
        if (!isPlayerReady()) {
            LogHub.w(TAG, "Player is not ready, ignore " + command);
            return false;
        }

        String playerId = command.playerId;
        // 如果 playerId 为空，则忽略命令
        if (StringUtil.isEmpty(playerId)) {
            LogHub.w(TAG, "Command target playerId is empty, ignore " + command);
            return false;
        }

        // 检查命令的 playerId 是否与当前播放器 ID 匹配
        if (StringUtil.notEquals(playerId, player.getPlayerId())) {
            LogHub.w(TAG, "Command target playerId is not match, ignore " + command);
            return false;
        }

        LogHub.i(TAG, "Command target playerId is match, handle " + command);
        return true;
    }

    /**
     * 处理播放命令
     * <p>
     * 执行播放操作，调用播放器的 {@link IMediaPlayer#start()} 方法开始播放。
     * 如果播放器已经在播放中，此操作通常不会产生副作用。
     * </p>
     *
     * @param command 播放命令，不能为 null
     */
    private void onPlayCommand(@NonNull PlayerCommand.Play command) {
        if (player != null) {
            player.start();
        }
    }

    /**
     * 处理暂停命令
     * <p>
     * 执行暂停操作，调用播放器的 {@link IMediaPlayer#pause()} 方法暂停播放。
     * 暂停后可以通过播放命令恢复播放。
     * </p>
     *
     * @param command 暂停命令，不能为 null
     */
    private void onPauseCommand(@NonNull PlayerCommand.Pause command) {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * 处理切换命令
     * <p>
     * 执行切换操作，如果当前正在播放，则暂停播放；如果当前暂停，则恢复播放。
     * 切换后，播放器状态会切换为 PLAYING 或 PAUSED。
     * </p>
     *
     * @param command 切换命令，不能为 null
     */
    private void onToggleCommand(@NonNull PlayerCommand.Toggle command) {
        handleToggle();
    }

    /**
     * 处理停止命令
     * <p>
     * 执行停止操作，调用播放器的 {@link IMediaPlayer#stop()} 方法停止播放。
     * 停止后播放器会释放相关资源，需要重新设置数据源才能再次播放。
     * </p>
     *
     * @param command 停止命令，不能为 null
     */
    private void onStopCommand(@NonNull PlayerCommand.Stop command) {
        if (player != null) {
            player.stop();
        }
    }

    /**
     * 处理重播命令
     * <p>
     * 执行重播操作，调用播放器的 {@link IMediaPlayer#replay()} 方法重新开始播放。
     * 重播会将播放位置重置到起始位置（通常是 0），然后开始播放。
     * </p>
     *
     * @param command 重播命令，不能为 null
     */
    private void onReplayCommand(@NonNull PlayerCommand.Replay command) {
        if (player != null) {
            player.replay();
        }
    }

    /**
     * 处理跳转命令
     * <p>
     * 执行跳转操作，调用播放器的 {@link IMediaPlayer#seekTo(long)} 方法跳转到指定位置。
     * 跳转位置以毫秒为单位，应该大于等于 0，且不应超过视频总时长。
     * </p>
     *
     * @param command 跳转命令，不能为 null，包含目标播放位置（单位：毫秒）
     */
    private void onSeekCommand(@NonNull PlayerCommand.Seek command) {
        if (player != null) {
            player.seekTo(command.position);
        }
    }

    /**
     * 处理设置播放速度命令
     * <p>
     * 执行设置播放速度操作，调用播放器的 {@link IMediaPlayer#setSpeed(float)} 方法设置播放速度。
     * </p>
     * <p>
     * 播放速度通常范围为 0.3 到 3.0，1.0 表示正常速度。
     * 建议优先使用 {@link com.aliyun.playerkit.ui.setting.SettingConstants#SPEED_OPTIONS} 中定义的常量进行设置。
     * </p>
     *
     * @param command 设置速度命令，不能为 null，包含目标播放速度
     */
    private void onSetSpeedCommand(@NonNull PlayerCommand.SetSpeed command) {
        if (player != null) {
            LogHub.i(TAG, "Set playback speed to: " + command.speed + "x");
            player.setSpeed(command.speed);
        }
    }

    /**
     * 处理截图命令
     * <p>
     * 执行截图操作，调用播放器的 {@link IMediaPlayer#snapshot()} 方法进行截图。
     * 截图结果会保存为图片文件，并触发 {@link com.aliyun.playerkit.event.PlayerEvents.SnapshotCompleted} 事件。
     * </p>
     *
     * @param command 截图命令，不能为 null
     */
    private void onSnapshotCommand(@NonNull PlayerCommand.Snapshot command) {
        if (player != null) {
            LogHub.i(TAG, "Snapshot");
            player.snapshot();
        }
    }

    /**
     * 处理设置循环播放命令
     * <p>
     * 执行设置循环播放操作，调用播放器的 {@link IMediaPlayer#setLoop(boolean)} 方法设置循环播放模式。
     * 当 loop 为 true 时，视频播放结束后会自动重新开始播放；当 loop 为 false 时，视频播放结束后停止播放。
     * </p>
     *
     * @param command 设置循环播放命令，不能为 null，包含是否开启循环播放的布尔值
     */
    private void onSetLoopCommand(@NonNull PlayerCommand.SetLoop command) {
        if (player != null) {
            LogHub.i(TAG, "Set loop to: " + command.loop);
            player.setLoop(command.loop);
        }
    }

    /**
     * 处理设置静音命令
     * <p>
     * 执行设置静音操作，调用播放器的 {@link IMediaPlayer#setMute(boolean)} 方法设置静音模式。
     * 当 mute 为 true 时，播放器将静音播放；当 mute 为 false 时，播放器将正常播放声音。
     * </p>
     *
     * @param command 设置静音命令，不能为 null，包含是否开启静音的布尔值
     */
    private void onSetMuteCommand(@NonNull PlayerCommand.SetMute command) {
        if (player != null) {
            LogHub.i(TAG, "Set mute to: " + command.mute);
            player.setMute(command.mute);
        }
    }

    /**
     * 处理设置渲染填充模式命令
     * <p>
     * 执行设置渲染填充模式操作，调用播放器的 {@link IMediaPlayer#setScaleType(int)} 方法设置视频画面填充模式。
     * 填充模式决定了视频如何适应显示视图的尺寸，例如等比填充、拉伸填充等。
     * </p>
     *
     * @param command 设置填充模式命令，不能为 null，包含目标渲染填充模式
     */
    private void onSetScaleTypeCommand(@NonNull PlayerCommand.SetScaleType command) {
        if (player != null) {
            LogHub.i(TAG, "Set scale type to: " + command.scaleType);
            player.setScaleType(command.scaleType);
        }
    }

    /**
     * 处理设置镜像模式命令
     * <p>
     * 执行设置镜像模式操作，调用播放器的 {@link IMediaPlayer#setMirrorType(int)} 方法设置视频镜像模式。
     * 镜像模式包括无镜像、水平镜像、垂直镜像等效果。
     * </p>
     *
     * @param command 设置镜像模式命令，不能为 null，包含目标镜像模式
     */
    private void onSetMirrorTypeCommand(@NonNull PlayerCommand.SetMirrorType command) {
        if (player != null) {
            LogHub.i(TAG, "Set mirror type to: " + command.mirrorType);
            player.setMirrorType(command.mirrorType);
        }
    }

    /**
     * 处理设置旋转模式命令
     * <p>
     * 执行设置旋转模式操作，调用播放器的 {@link IMediaPlayer#setRotation(int)} 方法设置视频旋转角度。
     * 旋转角度包括 0度、90度、180度、270度等选项。
     * </p>
     *
     * @param command 设置旋转模式命令，不能为 null，包含目标旋转角度
     */
    private void onSetRotationCommand(@NonNull PlayerCommand.SetRotation command) {
        if (player != null) {
            LogHub.i(TAG, "Set rotation to: " + command.rotationMode);
            player.setRotation(command.rotationMode);
        }
    }

    /**
     * 处理切换清晰度命令
     * <p>
     * 执行切换清晰度操作，调用播放器的 {@link IMediaPlayer#selectTrack(TrackQuality)} 方法切换轨道（如清晰度切换）。
     * 通常用于切换不同清晰度的视频流或选择不同的音频轨道。
     * </p>
     *
     * @param command 切换清晰度命令，不能为 null，包含目标清晰度索引
     */
    private void onSelectTrackCommand(@NonNull PlayerCommand.SelectTrack command) {
        if (player != null) {
            LogHub.i(TAG, "Select track to: " + command.trackQuality);
            player.selectTrack(command.trackQuality);
        }
    }
}
