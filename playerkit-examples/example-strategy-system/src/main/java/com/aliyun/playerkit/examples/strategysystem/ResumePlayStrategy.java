package com.aliyun.playerkit.examples.strategysystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.strategy.BaseStrategy;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆播放策略
 * <p>
 * 自动记录播放进度，下次播放同一视频时自动跳转到上次播放位置。
 * 使用场景：适用于长视频、课程视频等需要记忆播放位置的场景。
 * </p>
 * <p>
 * Resume Play Strategy
 * <p>
 * Automatically records playback position and restores it when the same video
 * is played again. Typical use cases are long-form videos and course videos
 * where users expect resume playback.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public class ResumePlayStrategy extends BaseStrategy {

    private static final String TAG = "ResumePlayStrategy";

    /**
     * 存储 key 前缀，用于区分不同的视频播放记录
     */
    private static final String KEY_PREFIX = "resume_play_";

    /**
     * 保存进度的时间间隔（毫秒），避免频繁写入
     */
    private static final long SAVE_INTERVAL = 2 * 1000;

    /**
     * 视频接近结束的阈值（毫秒），当剩余时长小于此值时清除记忆播放记录
     */
    private static final long NEAR_END_THRESHOLD_MS = 10 * 1000;

    /**
     * 上次保存进度的时间戳
     */
    private long mLastSaveTime = 0;
    /**
     * 当前播放的视频标识（URL 或 VID）
     */
    private String mMediaId = null;

    /**
     * 当前视频总时长（毫秒）
     */
    private long mDuration = 0;

    /**
     * 获取指定视频的续播起始时间（毫秒）
     * <p>
     * 这是一个静态工具方法，方便在「起播前」直接通过 {@link AliPlayerModel.Builder#startTime(long)}
     * 进行续播起点配置，而不是等 Prepared 之后再发 Seek 命令。
     * </p>
     *
     * @param source 视频源对象
     * @return 若有记忆播放记录则返回上次播放位置（毫秒），否则返回 0
     */
    public static long getResumePosition(@Nullable VideoSource source) {
        if (source == null) return 0;

        String mediaId = source.getMediaId();
        if (StringUtil.isEmpty(mediaId)) return 0;

        String key = KEY_PREFIX + mediaId;
        return SPManager.getInstance().getLong(key, 0);
    }

    @NonNull
    @Override
    public String getName() {
        return TAG;
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        List<Class<? extends PlayerEvent>> events = new ArrayList<>();
        events.add(PlayerEvents.Prepared.class);      // 准备完成时初始化媒体信息（mediaId、duration）
        events.add(PlayerEvents.Info.class);          // 定期更新播放进度
        events.add(PlayerEvents.StateChanged.class);   // 监听状态变化
        return events;
    }

    @Override
    public void onEvent(@NonNull PlayerEvent event) {
        if (!isCurrentPlayer(event)) return;

        if (event instanceof PlayerEvents.Prepared) {
            handlePrepared((PlayerEvents.Prepared) event);
        } else if (event instanceof PlayerEvents.Info) {
            handleInfo((PlayerEvents.Info) event);
        } else if (event instanceof PlayerEvents.StateChanged) {
            handleStateChanged((PlayerEvents.StateChanged) event);
        }
    }

    @Override
    public void onReset() {
        super.onReset();
        // 重置状态，准备处理新的视频
        mLastSaveTime = 0;
        mMediaId = null;
        mDuration = 0;
    }

    /**
     * 处理视频准备完成事件
     * <p>
     * 在视频准备完成时，检查是否有历史播放记录，如果有则自动跳转到上次播放位置。
     * </p>
     * <p>
     * Handle {@link PlayerEvents.Prepared} event.
     * When the video is prepared, check stored history and seek to the last
     * position if available.
     * </p>
     *
     * @param event 准备完成事件，包含视频时长等信息 / prepared event with duration info
     */
    private void handlePrepared(@NonNull PlayerEvents.Prepared event) {
        AliPlayerModel model = getModel();
        if (model == null) return;

        // 提取视频标识，用于作为存储 key
        VideoSource source = model.getVideoSource();
        mMediaId = source.getMediaId();

        // 记录当前视频总时长，用于判断“接近结束”逻辑
        mDuration = event.duration;

        // Note keria: 续播起点推荐由业务侧在起播前通过 AliPlayerModel.startTime 设置；
        // 这里不再主动发 Seek 命令，避免与业务逻辑产生冲突。
    }

    /**
     * 处理播放信息更新事件
     * <p>
     * 定期保存当前播放位置，使用节流机制避免频繁写入。
     * </p>
     * <p>
     * Handle {@link PlayerEvents.Info} event and persist current position
     * with throttling to avoid frequent disk writes.
     * </p>
     *
     * @param event 播放信息事件，包含当前播放位置 / info event with current position
     */
    private void handleInfo(@NonNull PlayerEvents.Info event) {
        if (StringUtil.isEmpty(mMediaId)) return;

        long now = System.currentTimeMillis();
        // 节流：固定间隔保存一次，避免频繁写入 SharedPreferences
        if (now - mLastSaveTime > SAVE_INTERVAL) {
            savePosition(event.currentPosition);
            mLastSaveTime = now;
        }
    }

    /**
     * 处理播放状态变化事件
     * <p>
     * 监听播放状态变化，在停止或完成时可以考虑保存进度。
     * 当前实现依赖 Info 事件的定期保存，这里仅作占位。
     * </p>
     * <p>
     * Handle {@link PlayerEvents.StateChanged} events.
     * This method can be extended to persist progress when playback stops
     * or completes.
     * </p>
     *
     * @param event 状态变化事件
     */
    private void handleStateChanged(@NonNull PlayerEvents.StateChanged event) {
        // 停止或播放完成时，也可以保存一次
        // 注意：STOPPED 时可能拿不到 currentPosition，这里简化处理，依赖 Info 的最后一次更新
        if (event.newState == PlayerState.STOPPED || event.newState == PlayerState.COMPLETED) {
            // Note keria: 可以在这里添加额外的保存逻辑
        }
    }

    /**
     * 保存播放位置
     * <p>
     * 将当前播放位置保存到 SPManager。
     * 如果播放接近结束，则清除记录，下次从头播放。
     * </p>
     * <p>
     * Persist current playback position into SPManager.
     * If the video is near the end, the stored record will be cleared so next playback starts from the beginning.
     * </p>
     *
     * @param position 当前播放位置（毫秒）
     */
    private void savePosition(long position) {
        if (StringUtil.isEmpty(mMediaId)) return;

        String key = getStorageKey(mMediaId);

        // 如果播放快结束了，清除记录，下次从头播放
        if (mDuration > 0 && position >= 0) {
            // Check if we're close enough to the end to clear the resume point
            if (mDuration - position <= NEAR_END_THRESHOLD_MS) {
                SPManager.getInstance().remove(key);
                LogHub.i(TAG, "Video near end, clearing resume position");
                return;
            }
        }

        // 保存播放位置
        SPManager.getInstance().saveLong(key, position);
    }

    /**
     * 获取存储 key
     * <p>
     * 使用前缀区分不同的视频播放记录。
     * </p>
     *
     * @param mediaId 视频标识
     * @return 存储 key
     */
    private String getStorageKey(String mediaId) {
        return KEY_PREFIX + mediaId;
    }

    /**
     * 获取播放器模型
     * <p>
     * 安全地获取播放器模型，避免空指针异常。
     * </p>
     * <p>
     * Safely get {@link AliPlayerModel} from strategy context, returning null
     * when the player is not ready.
     * </p>
     *
     * @return 播放器模型，如果不可用则返回 null
     */
    @Nullable
    private AliPlayerModel getModel() {
        return mContext == null ? null : mContext.getModel();
    }
}
