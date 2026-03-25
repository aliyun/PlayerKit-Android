package com.aliyun.playerkit.player;

import android.graphics.Bitmap;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerGlobalSettings;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.UrlSource;
import com.aliyun.player.source.VidAuth;
import com.aliyun.player.source.VidSts;
import com.aliyun.player.videoview.AliDisplayView;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.converter.PlayerStateConverter;
import com.aliyun.playerkit.converter.PlayerTrackConverter;
import com.aliyun.playerkit.converter.VideoSourceConverter;
import com.aliyun.playerkit.controller.PlayerStateStore;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.event.PlayerEventBus;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.converter.PlayerTypeConverter;
import com.aliyun.playerkit.utils.BitmapUtil;
import com.aliyun.playerkit.utils.FileUtil;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.TrackUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体播放器实现类
 * <p>
 * 封装阿里云播放器 SDK（AliPlayer），实现 {@link IMediaPlayer} 接口。
 * 负责将底层播放器的操作和事件转换为统一的接口调用和状态管理。
 * </p>
 * <p>
 * Media Player Implementation
 * <p>
 * Encapsulates Alibaba Cloud Player SDK (AliPlayer) and implements the {@link IMediaPlayer} interface.
 * Responsible for converting underlying player operations and events into unified interface calls and state management.
 * </p>
 *
 * @author keria
 * @date 2025/11/28
 */
public class MediaPlayer implements IMediaPlayer {

    private static final String TAG = "MediaPlayer";

    // 加载 RTS 低延迟直播组件动态库
    // 必须在使用播放器前完成加载
    static {
        try {
            System.loadLibrary("RtsSDK");
        } catch (UnsatisfiedLinkError e) {
            LogHub.e(TAG, "RtsSDK load failed: " + e.getMessage());
        } catch (SecurityException e) {
            LogHub.e(TAG, "RtsSDK load denied: " + e.getMessage());
        }
    }

    /**
     * RTS 流地址前缀
     */
    private static final String RTS_STREAM_URL_PREFIX = "artc://";

    ///  === RTS 低延迟播放参数配置（单位：毫秒）===

    /**
     * 播放器允许的最大延迟时间，单位：毫秒，SDK 已默认
     * <p>
     * 超过该延迟会触发追帧/丢帧以降低整体延迟
     */
    private static final int RTS_MAX_DELAY_TIME_MS = 1000;

    /**
     * 起播时的最小缓冲时长，单位：毫秒，SDK 已默认
     * <p>
     * 值越小，起播越快，但对网络抖动的容忍度越低
     */
    private static final int RTS_START_BUFFER_DURATION_MS = 10;

    /**
     * 卡顿恢复时的最大缓冲上限，单位：毫秒，SDK 已默认
     * <p>
     * 控制追帧时的缓冲大小，保证低延迟优先
     */
    private static final int RTS_HIGH_BUFFER_DURATION_MS = 10;

    /**
     * 是否启用精准 Seek 模式。
     *
     * <p>
     * 开启后，在切换清晰度轨道或执行 Seek 操作时，
     * 播放器会尽量从最精确的时间点开始播放，
     * 以保证音视频同步和播放连续性。
     * </p>
     *
     * <p>
     * 相比快速模式，该模式下切换过程可能会稍慢。
     * </p>
     */
    private static final boolean ENABLE_ACCURATE_SEEK = true;

    /**
     * Seek 模式
     * <p>
     * 播放器 Seek 模式，默认为精准模式。
     * </p>
     */
    private static final IPlayer.SeekMode DEFAULT_SEEK_MODE = ENABLE_ACCURATE_SEEK ? IPlayer.SeekMode.Accurate : IPlayer.SeekMode.Inaccurate;

    /**
     * 底层播放器实例
     * <p>
     * 阿里云播放器 SDK 的播放器实例，所有播放操作最终都委托给此实例。
     * </p>
     */
    @NonNull
    private final AliPlayer aliPlayer;

    /**
     * 播放器唯一标识
     * <p>
     * 用于区分不同的播放器实例，在事件发布和日志记录中使用。
     * </p>
     */
    @NonNull
    private final String playerId;

    /**
     * 播放器状态存储
     * <p>
     * 负责维护播放器的状态信息，并自动发布状态变化事件。
     * </p>
     */
    @NonNull
    private final PlayerStateStore stateStore;

    /**
     * 构造函数
     * <p>
     * 创建播放器实例并初始化状态管理。会自动设置播放器回调监听，
     * 并将底层播放器的事件转换为状态更新和事件发布。
     * </p>
     * <p>
     * Constructor
     * <p>
     * Creates a player instance and initializes state management. Automatically sets up player
     * callback listeners and converts underlying player events into state updates and event publishing.
     * </p>
     *
     * @param aliPlayer 底层播放器实例，不能为 null
     * @param playerId  播放器唯一标识，不能为 null
     * @throws NullPointerException 如果 aliPlayer 或 playerId 为 null
     */
    public MediaPlayer(@NonNull AliPlayer aliPlayer, @NonNull String playerId) {
        if (aliPlayer == null) {
            throw new NullPointerException("AliPlayer cannot be null");
        }
        if (playerId == null) {
            throw new NullPointerException("PlayerId cannot be null");
        }

        this.aliPlayer = aliPlayer;
        this.playerId = playerId;
        this.stateStore = new PlayerStateStore();

        // 启用状态变化事件发布
        stateStore.enableEventPublishing(playerId);
        stateStore.updatePlayState(PlayerState.INITIALIZING);

        // 设置播放器内部回调
        setupPlayerCallbacks();

        LogHub.i(TAG, "MediaPlayer created", playerId);
    }

    /**
     * 设置播放器回调监听
     * <p>
     * 将底层播放器的事件回调转换为状态更新和回调通知。
     * </p>
     * <p>
     * Setup Player Callback Listeners
     * <p>
     * Converts underlying player event callbacks into state updates and event publishing.
     * </p>
     * <p>
     * <strong>线程安全说明</strong>：
     * 播放器回调在播放器内部线程执行，状态更新会触发事件发布。
     * 如需在主线程处理事件，请在事件订阅者中切换线程。
     * </p>
     */
    private void setupPlayerCallbacks() {
        // 播放器准备完成回调
        aliPlayer.setOnPreparedListener(() -> {
            LogHub.i(TAG, "onPrepared");

            long duration = aliPlayer.getDuration();
            stateStore.updateDuration(duration);
            stateStore.updatePlayState(PlayerState.PREPARED);

            // 发布 Prepared 事件
            PlayerEventBus.getInstance().post(new PlayerEvents.Prepared(playerId, duration));
        });

        // 首帧渲染完成回调
        aliPlayer.setOnRenderingStartListener(() -> {
            LogHub.i(TAG, "onRenderingStart");

            // 发布首帧渲染完成事件
            PlayerEventBus.getInstance().post(new PlayerEvents.FirstFrameRendered(playerId));
        });

        // 播放状态改变回调
        aliPlayer.setOnStateChangedListener(state -> {
            LogHub.i(TAG, "onStateChanged", state);

            // 更新状态存储
            PlayerState newState = PlayerStateConverter.convert(state);
            stateStore.updatePlayState(newState);
        });

        // 播放错误回调
        aliPlayer.setOnErrorListener(errorInfo -> {
            LogHub.e(TAG, "onError", "errorInfo=" + formatErrorInfo(errorInfo));

            if (errorInfo == null) {
                LogHub.e(TAG, "onError: unknown error");
                return;
            }
            stateStore.updatePlayState(PlayerState.ERROR);

            // 发布错误事件
            PlayerEventBus.getInstance().post(new PlayerEvents.Error(playerId, errorInfo.getCode().getValue(), errorInfo.getMsg()));
        });

        // 视频尺寸变化回调
        aliPlayer.setOnVideoSizeChangedListener((width, height) -> {
            LogHub.i(TAG, "onVideoSizeChanged", width + "x" + height);
            stateStore.updateVideoSize(width, height);
        });

        // 播放信息回调
        aliPlayer.setOnInfoListener(infoBean -> {
            if (infoBean.getCode() == InfoCode.CurrentPosition) {
                long currentPosition = infoBean.getExtraValue();
                stateStore.updateCurrentPosition(currentPosition);

                long duration = aliPlayer.getDuration();
                stateStore.updateDuration(duration); // 确保时长是最新的

                // 发布 Info 事件
                PlayerEventBus.getInstance().post(new PlayerEvents.Info(playerId, duration, currentPosition, aliPlayer.getBufferedPosition()));
            } else if (infoBean.getCode() == InfoCode.BufferedPosition) {
                // 缓冲进度更新，也可以触发 Info 事件
                long bufferedPosition = infoBean.getExtraValue();
                PlayerEventBus.getInstance().post(new PlayerEvents.Info(playerId, aliPlayer.getDuration(), aliPlayer.getCurrentPosition(), bufferedPosition));
            }
        });

        // 截图回调
        aliPlayer.setOnSnapShotListener((bitmap, width, height) -> {
            String externalFileFolder = FileUtil.getExternalFileFolder(AliPlayerKit.getContext());
            String fileName = BitmapUtil.generateFileName(Bitmap.CompressFormat.JPEG);
            String fullPath = FileUtil.combinePaths(externalFileFolder, "AliPlayerKit", fileName);

            // 将 Bitmap 保存为截图文件
            boolean result = BitmapUtil.saveBitmapToFile(bitmap, fullPath);

            // 截图完成，发布截图完成事件
            PlayerEventBus.getInstance().post(new PlayerEvents.SnapshotCompleted(playerId, result, fullPath, width, height));
            LogHub.i(TAG, "onSnapShot", width + "x" + height, "saveBitmapToFile", result, fullPath);
        });

        // Loading 状态回调
        aliPlayer.setOnLoadingStatusListener(new IPlayer.OnLoadingStatusListener() {
            @Override
            public void onLoadingBegin() {
                LogHub.i(TAG, "onLoadingBegin");
                PlayerEventBus.getInstance().post(new PlayerEvents.LoadingBegin(playerId));
            }

            @Override
            public void onLoadingProgress(int percent, float netSpeed) {
                // 频繁回调，仅在 Debug 模式或特定需求下记录日志
                // LogHub.d(TAG, "onLoadingProgress", percent, netSpeed);
                PlayerEventBus.getInstance().post(new PlayerEvents.LoadingProgress(playerId, percent, netSpeed));
            }

            @Override
            public void onLoadingEnd() {
                LogHub.i(TAG, "onLoadingEnd");
                PlayerEventBus.getInstance().post(new PlayerEvents.LoadingEnd(playerId));
            }
        });

        // 媒体信息回调
        aliPlayer.setOnTrackReadyListener(this::createTrackInfoList);

        // 清晰度切换回调
        aliPlayer.setOnTrackChangedListener(new IPlayer.OnTrackChangedListener() {
            /**
             * 切换成功
             *
             * @param trackInfo 流信息。见{@link TrackInfo}
             */
            /****
             * The stream is switched.
             *
             * @param trackInfo Stream information. See {@link TrackInfo}.
             */
            @Override
            public void onChangedSuccess(TrackInfo trackInfo) {
                LogHub.i(TAG, "onChangedSuccess", "trackInfo=" + formatTrackInfo(trackInfo));
                if (trackInfo == null) {
                    return;
                }

                TrackQuality quality = PlayerTrackConverter.convert(trackInfo);
                stateStore.updateCurrentTrackIndex(quality.getIndex());
            }

            /**
             * 切换失败
             *
             * @param trackInfo 流信息。见{@link TrackInfo}
             * @param errorInfo 错误信息。见{@link ErrorInfo}
             */
            /****
             * Failed to switch the stream.
             *
             * @param trackInfo Stream information. See {@link TrackInfo}.
             * @param errorInfo Error message. See {@link ErrorInfo}.
             */
            @Override
            public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
                LogHub.e(TAG, "onChangedFail", "trackInfo=" + formatTrackInfo(trackInfo), "errorInfo=" + formatErrorInfo(errorInfo));
            }
        });
    }

    @Override
    public void setDataSource(@NonNull AliPlayerModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model cannot be null");
        }

        VideoSource videoSource = model.getVideoSource();
        if (videoSource == null) {
            throw new IllegalArgumentException("videoSource cannot be null");
        }

        LogHub.i(TAG, "setDataSource", String.valueOf(model), playerId);

        // 可选：推荐使用`播放器单点追查`功能，当使用阿里云播放器 SDK 播放视频发生异常时，可借助单点追查功能针对具体某个用户或某次播放会话的异常播放行为进行全链路追踪，以便您能快速诊断问题原因，可有效改善播放体验治理效率。
        // traceId 值由您自行定义，需为您的用户或用户设备的唯一标识符，例如传入您业务的 userid 或者 IMEI、IDFA 等您业务用户的设备 ID。
        // 传入 traceId 后，埋点日志上报功能开启，后续可以使用播放质量监控、单点追查和视频播放统计功能。
        // 文档：https://help.aliyun.com/zh/vod/developer-reference/single-point-tracing
        String traceId = model.getTraceId();
        if (StringUtil.isNotEmpty(traceId)) {
            aliPlayer.setTraceId(traceId);
        }

        // 列表播放模式下，允许预渲染
        if (model.getSceneType() == SceneType.VIDEO_LIST) {
            aliPlayer.setOption(AliPlayer.ALLOW_PRE_RENDER, 1);
        }

        // RTS 超低延迟直播相关的特殊配置
        if (videoSource instanceof VideoSource.UrlSource && StringUtil.startsWith(((VideoSource.UrlSource) videoSource).getUrl(), RTS_STREAM_URL_PREFIX)) {
            // 若为 RTS 超低延迟直播，设置播放器配置以优化播放体验

            // 获取当前播放器配置
            PlayerConfig config = aliPlayer.getConfig();

            // 设置最大允许延迟
            config.mMaxDelayTime = RTS_MAX_DELAY_TIME_MS;
            // 起播时最小缓冲时长
            config.mStartBufferDuration = RTS_START_BUFFER_DURATION_MS;
            // 卡顿时最大缓冲上限
            config.mHighBufferDuration = RTS_HIGH_BUFFER_DURATION_MS;

            // 将修改后的配置应用到播放器
            aliPlayer.setConfig(config);

            // 全局设置：启用 RTS 自动降级功能（默认已开启）
            // 当网络不佳时，自动切换至普通直播流保障连续性
            AliPlayerGlobalSettings.setOption(AliPlayerGlobalSettings.ALLOW_RTS_DEGRADE, 1);

            // 可选功能：自定义降级流地址（当前未启用）
            // PlayerConfig config = mAliPlayer.getConfig();
            // UrlSource downgradeSource = new UrlSource();
            // downgradeSource.setUri(downgradeUrl);
            // mAliPlayer.enableDowngrade(downgradeSource, config);

            LogHub.i(TAG, "RTS low-latency live streaming optimization has been enabled.");
        }

        // 配置视频源
        if (videoSource instanceof VideoSource.UrlSource) {
            // URL 视频源
            UrlSource urlSource = (UrlSource) VideoSourceConverter.convert(videoSource);
            aliPlayer.setDataSource(urlSource);
        } else if (videoSource instanceof VideoSource.VidStsSource) {
            // VidSts 视频源
            VidSts vidSts = (VidSts) VideoSourceConverter.convert(videoSource);
            aliPlayer.setDataSource(vidSts);
        } else if (videoSource instanceof VideoSource.VidAuthSource) {
            // VidAuth 视频源
            VidAuth vidAuth = (VidAuth) VideoSourceConverter.convert(videoSource);
            aliPlayer.setDataSource(vidAuth);
        } else {
            throw new IllegalArgumentException("Unsupported video source type: " + videoSource.getClass().getSimpleName());
        }

        // 配置硬解码
        aliPlayer.enableHardwareDecoder(model.isHardWareDecode());

        // 设置播放开始时间
        long startTime = model.getStartTime();
        if (startTime > 0) {
            aliPlayer.setStartTime(startTime, DEFAULT_SEEK_MODE);
        }

        // 如果设置了自动播放，在准备完成后自动开始播放
        // 注意：prepare 以后可以同步调用 start 操作，onPrepared 回调完成后会自动起播
        aliPlayer.setAutoPlay(model.isAutoPlay());

        stateStore.updatePlayState(PlayerState.PREPARING);
        aliPlayer.prepare();
    }

    @Override
    public void start() {
        LogHub.i(TAG, "Start", playerId);
        stateStore.updatePlayState(PlayerState.PLAYING);
        aliPlayer.start();
    }

    @Override
    public void pause() {
        LogHub.i(TAG, "Pause", playerId);
        aliPlayer.pause();
    }

    @Override
    public void toggle() {
        PlayerState state = stateStore.getPlayState();
        if (state == PlayerState.PLAYING) {
            LogHub.i(TAG, "Toggle playback to pause state");
            pause();
        } else if (state == PlayerState.PAUSED || state == PlayerState.PREPARED) {
            LogHub.i(TAG, "Toggle playback to play state");
            start();
        } else if (state == PlayerState.COMPLETED || state == PlayerState.STOPPED) {
            LogHub.i(TAG, "Toggle playback to replay state");
            replay();
        } else {
            LogHub.w(TAG, "Toggle playback failed", "state=" + state);
        }
    }

    @Override
    public void replay() {
        LogHub.i(TAG, "Replay", playerId);
        stateStore.updatePlayState(PlayerState.PREPARING);
        aliPlayer.prepare();
        start();
    }

    @Override
    public void seekTo(long positionMs) {
        LogHub.i(TAG, "Seek", positionMs + "ms", playerId);
        aliPlayer.seekTo(positionMs, DEFAULT_SEEK_MODE);
    }

    @Override
    public void stop() {
        LogHub.i(TAG, "Stop");

        // 停止播放
        aliPlayer.stop();
        stateStore.updatePlayState(PlayerState.STOPPED);
    }

    @Override
    public void release() {
        LogHub.i(TAG, "Release", playerId);

        // 销毁播放器实例
        // 方案1：stop + release，适用于通用场景；释放操作有耗时，会阻塞当前线程，直到资源完全释放。
        aliPlayer.stop();
        aliPlayer.release();

        // 方案2：releaseAsync，无需手动 stop，适用于短剧等场景；异步释放资源，不阻塞线程，内部已自动调用 stop。
        // aliPlayer.releaseAsync();

        stateStore.updatePlayState(PlayerState.IDLE);

        // 禁用事件发布并重置状态
        stateStore.disableEventPublishing();
        stateStore.reset();
    }

    @Override
    public void setDisplayView(@Nullable AliDisplayView displayView) {
        LogHub.i(TAG, "setDisplayView", playerId);
        aliPlayer.setDisplayView(displayView);
    }

    @Override
    public void setSurface(@Nullable Surface surface) {
        LogHub.i(TAG, "setSurface", playerId);
        aliPlayer.setSurface(surface);
    }

    @Override
    public void surfaceChanged() {
        LogHub.i(TAG, "surfaceChanged", playerId);
        aliPlayer.surfaceChanged();
    }

    @Override
    public void setSpeed(float speed) {
        LogHub.i(TAG, "setSpeed", speed + "x", playerId);
        aliPlayer.setSpeed(speed);
        stateStore.updateSpeed(speed);
    }

    @Override
    public void setLoop(boolean loop) {
        LogHub.i(TAG, "setLoop", loop, playerId);
        aliPlayer.setLoop(loop);
        stateStore.updateLoop(loop);
    }

    @Override
    public void setMute(boolean mute) {
        LogHub.i(TAG, "setMute", mute, playerId);
        aliPlayer.setMute(mute);
        stateStore.updateMute(mute);
    }

    @Override
    public void snapshot() {
        LogHub.i(TAG, "snapshot", playerId);
        aliPlayer.snapshot();
    }

    @Override
    public void setScaleType(@ScaleType int scaleType) {
        LogHub.i(TAG, "setScaleType", scaleType, playerId);
        aliPlayer.setScaleMode(PlayerTypeConverter.convertScaleType(scaleType));
        stateStore.updateScaleType(scaleType);
    }

    @Override
    public void setMirrorType(@MirrorType int mirrorType) {
        LogHub.i(TAG, "setMirrorType", mirrorType, playerId);
        aliPlayer.setMirrorMode(PlayerTypeConverter.convertMirrorType(mirrorType));
        stateStore.updateMirrorType(mirrorType);
    }

    @Override
    public void setRotation(@Rotation int rotation) {
        LogHub.i(TAG, "setRotation", rotation, playerId);
        aliPlayer.setRotateMode(PlayerTypeConverter.convertRotation(rotation));
        stateStore.updateRotation(rotation);
    }

    @Override
    public void selectTrack(TrackQuality trackQuality) {
        if (trackQuality == null) {
            LogHub.w(TAG, "selectTrack ignored due to null trackQuality");
            return;
        }

        LogHub.i(TAG, "selectTrack: " + trackQuality, playerId);
        // 启用精准跳转模式。在此模式下，播放器在切换到新的清晰度轨道后，会尝试从最精确的时间点开始播放，以保证音视频同步和流畅性，但切换过程可能会稍慢。
        aliPlayer.selectTrack(trackQuality.getIndex(), ENABLE_ACCURATE_SEEK);
    }

    @NonNull
    @Override
    public String getPlayerId() {
        return playerId;
    }

    @NonNull
    @Override
    public IPlayerStateStore getStateStore() {
        return stateStore;
    }

    /**
     * 创建 TrackInfo 列表
     *
     * @param mediaInfo 媒体信息
     */
    private void createTrackInfoList(MediaInfo mediaInfo) {
        if (mediaInfo == null) {
            return;
        }

        List<TrackInfo> trackInfoList = mediaInfo.getTrackInfos();
        if (trackInfoList == null) {
            return;
        }

        // 创建 TrackQuality 列表
        List<TrackQuality> trackQualityList = new ArrayList<>();
        for (TrackInfo trackInfo : trackInfoList) {
            TrackQuality quality = PlayerTrackConverter.convert(trackInfo);
            trackQualityList.add(quality);
        }

        // 过滤出有效的视频轨道
        List<TrackQuality> filterVideoTrackQualityList = TrackUtil.filterVideoTrackQualityList(trackQualityList);
        stateStore.updateTrackQualityList(filterVideoTrackQualityList);

        // 如果当前没有选中的轨道，且存在视频轨道，默认认为选中了第一个视频轨道
        TrackQuality firstVideoTrack = filterVideoTrackQualityList.isEmpty() ? null : filterVideoTrackQualityList.get(0);
        if (firstVideoTrack != null) {
            stateStore.updateCurrentTrackIndex(firstVideoTrack.getIndex());
        }
    }

    /**
     * 将 ErrorInfo 对象格式化为可读字符串。
     *
     * @param errorInfo 错误信息对象，允许为 null
     * @return 格式化后的字符串，若 errorInfo 为 null 返回 "null"
     */
    @NonNull
    private static String formatErrorInfo(ErrorInfo errorInfo) {
        if (errorInfo == null) {
            return "null";
        }

        return "ErrorInfo{" + "code=" + errorInfo.getCode() + ", msg='" + errorInfo.getMsg() + '\'' + ", extra=" + errorInfo.getExtra() + "}";
    }

    /**
     * 将 TrackInfo 对象格式化为可读字符串。
     *
     * @param trackInfo TrackInfo 对象，允许为 null
     * @return 格式化后的字符串，若 trackInfo 为 null 返回 "null"
     */
    @NonNull
    private static String formatTrackInfo(TrackInfo trackInfo) {
        if (trackInfo == null) {
            return "null";
        }

        return "TrackInfo{" + "index=" + trackInfo.getIndex() + ", type=" + trackInfo.getType() + ", width=" + trackInfo.getVideoWidth() + ", height=" + trackInfo.getVideoHeight() + "}";
    }
}
