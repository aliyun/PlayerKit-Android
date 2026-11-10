package com.aliyun.playerkit.ui.slots;

import android.annotation.SuppressLint;
import android.content.Context;

import android.os.Looper;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.data.PlayerState;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.FullscreenEvents;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.ui.chapter.ChapterChipAdapter;
import com.aliyun.playerkit.ui.chapter.ChapterMarkerView;
import com.aliyun.playerkit.utils.FormatUtil;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 底部控制栏插槽
 * <p>
 * 提供播放器底部控制栏功能，包括播放/暂停控制、进度条、时间显示和全屏按钮。
 * 实时更新播放进度和缓冲进度，响应用户的拖动操作进行视频跳转。
 * </p>
 * <p>
 * Bottom Control Bar Slot
 * <p>
 * Provides bottom control bar functionality for the player, including play/pause control, progress bar, time display, and fullscreen button.
 * Updates playback progress and buffering progress in real-time, and responds to user drag operations for video seeking.
 * </p>
 *
 * @author keria
 * @date 2025/12/24
 */
public class BottomBarSlot extends BaseControlBarSlot implements View.OnClickListener {

    // ==================== UI 组件 ====================

    /**
     * 播放/暂停按钮
     * <p>
     * 点击后切换播放和暂停状态。
     * </p>
     */
    private ImageView mIvPlayPause;

    /**
     * 刷新按钮
     * <p>
     * 点击后重新播放视频。
     * 仅在直播场景（LIVE）下显示。
     * </p>
     */
    private ImageView mIvReplay;

    /**
     * 进度条
     * <p>
     * 显示播放进度和缓冲进度，支持拖动跳转。
     * </p>
     */
    private SeekBar mSeekBar;

    /**
     * 时间显示文本视图
     * <p>
     * 显示当前播放时间和总时长，格式为 "当前时间/总时长"。
     * </p>
     */
    private TextView mTvTime;


    // ==================== 状态变量 ====================

    /**
     * 是否正在拖动进度条
     * <p>
     * 拖动时暂停进度更新，避免拖动操作被进度更新覆盖。
     * </p>
     */
    private boolean mIsDragging = false;

    // ==================== 横屏 UI 组件（ViewStub 懒加载） ====================

    /**
     * 横屏布局的 ViewStub
     * <p>
     * 首次进入全屏时 inflate，避免竖屏场景下承担不必要的布局开销。
     * </p>
     */
    private ViewStub mViewStub;

    /**
     * 横屏布局根视图
     * <p>
     * 由 {@link #mViewStub} inflate 生成，为 null 表示尚未进入过全屏。
     * </p>
     */
    private View mLandScapeView;

    /**
     * 竖屏布局根视图
     */
    private View mPortraitView;

    /**
     * 横屏 - 时间显示文本视图
     */
    private TextView mTvTimeLS;

    /**
     * 横屏 - 进度条
     */
    private SeekBar mSeekBarLS;

    /**
     * 横屏 - 播放/暂停按钮
     */
    private ImageView mIvPlayPauseLS;

    /**
     * 横屏 - 刷新按钮
     */
    private ImageView mIvReplayLS;

    /**
     * 章节标记 View（竖屏）
     */
    private ChapterMarkerView mChapterMarkerView;

    /**
     * 章节标记 View（横屏）
     */
    private ChapterMarkerView mChapterMarkerViewLS;

    /**
     * 横屏 - 章节按钮
     */
    private TextView mTvChapterLS;

    /**
     * 横屏 - 下一章按钮
     */
    private TextView mTvNextChapterLS;

    /**
     * 横屏 - 分隔符（·）
     */
    private View mTvSeparatorDotLS;

    /**
     * 横屏 - 分隔符（|）
     */
    private View mTvSeparatorPipeLS;

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(ControlBarEvents.Show.class,
            ControlBarEvents.Hide.class,
            ControlBarEvents.ResetTimer.class,
            PlayerEvents.StateChanged.class,
            PlayerEvents.Prepared.class,
            PlayerEvents.Info.class,
            PlayerEvents.ChapterListUpdated.class,
            FullscreenEvents.FullScreenChanged.class
    );

    /**
     * 视频总时长（毫秒）
     * <p>
     * 用于计算进度百分比和显示总时长。
     * </p>
     */
    private long mDuration = 0;

    /**
     * 当前场景类型
     * <p>
     * 用于判断是否允许进度条拖拽（RESTRICTED 场景下禁用）。
     * </p>
     */
    @SceneType
    private int mSceneType = SceneType.VOD;

    // ==================== 章节相关 ====================

    /**
     * 章节信息列表（章节能力可用时有值，判断规则见 {@link #isChapterAvailable()}）
     */
    @Nullable
    private List<ChapterInfo> mChapters;

    /**
     * 章节导航 RecyclerView（竖屏）
     */
    @Nullable
    private RecyclerView mRvChapterChips;

    /**
     * 章节 Chip 适配器（竖屏）
     */
    @Nullable
    private ChapterChipAdapter mChapterChipAdapter;

    /**
     * 章节 Chip 适配器（横屏）
     */
    @Nullable
    private ChapterChipAdapter mChapterChipAdapterLS;

    /**
     * 上一次显示的章节索引，用于避免重复更新
     */
    private int mLastChapterIndex = -1;

    /**
     * 章节 Chip 点击回调（竖屏/横屏共用）
     * <p>
     * Chapter chip click listener (shared by portrait and landscape)
     * </p>
     */
    private final ChapterChipAdapter.OnChipClickListener mChipClickListener = (position, chapter) -> {
        // RESTRICTED 等禁跳场景：章节仅作展示，点击不发起跳转、不更新高亮
        if (!isChapterSeekEnabled()) return;
        if (mPlayerId != null) {
            seekTo(chapter.getStartMs());
            updateChapterHighlight(chapter.getStartMs());
        }
    };

    /**
     * 章节标记点点击回调（竖屏/横屏共用）
     * <p>
     * Chapter marker click listener (shared by portrait and landscape)
     * </p>
     */
    private final ChapterMarkerView.OnMarkerClickListener mMarkerClickListener = (markerIndex, normalizedPosition) -> {
        // RESTRICTED 等禁跳场景：标记仅作展示，点击不发起跳转、不更新高亮
        if (!isChapterSeekEnabled()) return;
        if (mPlayerId != null && mDuration > 0) {
            long targetPosition = (long) (normalizedPosition * mDuration);
            seekTo(targetPosition);
            updateChapterHighlight(targetPosition);
        }
    };

    // ==================== 构造函数 ====================

    public BottomBarSlot(@NonNull Context context) {
        super(context);
    }

    // ==================== 生命周期方法 ====================

    @Override
    protected int getLayoutId() {
        return R.layout.layout_bottom_bar_slot;
    }

    @Override
    protected void onRegisterElements() {
        registerElement(SlotElements.BottomBar.PLAY, visible -> setViewVisible(visible, mIvPlayPause, mIvPlayPauseLS));
        registerElement(SlotElements.BottomBar.REFRESH, visible -> setViewVisible(visible, mIvReplay, mIvReplayLS));
        // PROGRESS 控制多个 View，保持 SlotElementHandle 方式
        registerElement(SlotElements.BottomBar.PROGRESS, visible -> setViewVisible(visible, mSeekBar, mTvTime, mSeekBarLS, mTvTimeLS));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 竖屏
        mPortraitView = findViewByIdCompat(R.id.ll_view_portrait);

        // 初始化播放/暂停按钮
        mIvPlayPause = findViewByIdCompat(R.id.iv_play_pause);

        // 初始化刷新按钮（可选：直播场景）
        mIvReplay = findViewByIdCompat(R.id.iv_replay);

        // 初始化时间显示
        mTvTime = findViewByIdCompat(R.id.tv_time);

        setViewClickListener(this, mIvPlayPause, mIvReplay);

        // 初始化进度条并设置监听器
        mSeekBar = findViewByIdCompat(R.id.seek_bar);
        setSeekbarListener(mSeekBar);

        // 初始化章节标记 View
        mChapterMarkerView = findViewByIdCompat(R.id.chapter_marker_view);
        if (mChapterMarkerView != null) {
            mChapterMarkerView.setOnMarkerClickListener(mMarkerClickListener);
        }

        // 初始化章节导航 RecyclerView
        mRvChapterChips = findViewByIdCompat(R.id.rv_chapter_chips);
        if (mRvChapterChips != null) {
            mRvChapterChips.setLayoutManager(
                    new LinearLayoutManager(mRvChapterChips.getContext(), LinearLayoutManager.HORIZONTAL, false));
            mRvChapterChips.setHasFixedSize(true);
        }

        mViewStub = findViewByIdCompat(R.id.stub_bottom_bar_landscape);
    }


    /**
     * 处理全屏切换
     * <p>
     * 进入全屏时懒加载横屏布局并同步状态；退出全屏时切回竖屏布局。
     * </p>
     *
     * @param isFullScreen 是否进入全屏
     */
    private void onToggleFullscreen(boolean isFullScreen) {
        if (isFullScreen) {
            if (mLandScapeView == null) {
                buildLandScapeView();
                // 首次 inflate，同步当前状态
                syncLandscapeState();
            }
            mPortraitView.setVisibility(View.GONE);
            if (mRvChapterChips != null) {
                mRvChapterChips.setVisibility(View.GONE);
            }
            mLandScapeView.setVisibility(View.VISIBLE);
        } else {
            if (mLandScapeView != null) {
                mLandScapeView.setVisibility(View.GONE);
            }
            mPortraitView.setVisibility(View.VISIBLE);
            if (mRvChapterChips != null && isChapterAvailable()) {
                mRvChapterChips.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 横屏控制栏是否正在展示
     * <p>
     * 竖屏章节胶囊行与竖屏容器是兄弟节点，进入全屏时由 {@link #onToggleFullscreen(boolean)}
     * 单独隐藏；运行期刷新章节数据时需要据同一状态维持可见性，避免竖屏 UI 叠加到横屏控制条上。
     * </p>
     *
     * @return 横屏控制栏可见返回 true
     */
    private boolean isLandscapeLayoutActive() {
        return mLandScapeView != null && mLandScapeView.getVisibility() == View.VISIBLE;
    }

    /**
     * 同步竖屏状态到横屏
     * <p>
     * 首次 inflate 横屏布局后调用，将播放状态、进度条、时间文本同步到横屏视图。
     * </p>
     */
    private void syncLandscapeState() {
        if (mIvPlayPauseLS != null && mIvPlayPause != null) {
            mIvPlayPauseLS.setSelected(mIvPlayPause.isSelected());
        }
        if (mSeekBarLS != null && mSeekBar != null) {
            mSeekBarLS.setProgress(mSeekBar.getProgress());
            mSeekBarLS.setSecondaryProgress(mSeekBar.getSecondaryProgress());
        }
        if (mTvTimeLS != null && mTvTime != null) {
            mTvTimeLS.setText(mTvTime.getText());
        }
    }


    /**
     * 构建横屏布局
     * <p>
     * 通过 ViewStub 懒加载横屏布局，绑定 UI 组件引用并设置监听器。
     * 仅在首次进入全屏时调用一次。
     * </p>
     */
    private void buildLandScapeView() {
        mLandScapeView = mViewStub.inflate();

        mTvTimeLS = mLandScapeView.findViewById(R.id.tv_time_landscape);

        mSeekBarLS = mLandScapeView.findViewById(R.id.seek_bar_landscape);
        mChapterMarkerViewLS = mLandScapeView.findViewById(R.id.chapter_marker_view_landscape);
        if (mChapterMarkerViewLS != null) {
            mChapterMarkerViewLS.setOnMarkerClickListener(mMarkerClickListener);
        }

        // 横屏初始化后，如果章节数据已加载并已拿到时长，立即应用标记
        if (isChapterAvailable() && mDuration > 0) {
            applyChapterMarkers(mDuration);
        }

        // 横屏初始化后，如果章节数据已加载，立即绑定到横屏 adapter
        if (isChapterAvailable()) {
            if (mChapterChipAdapterLS == null) {
                mChapterChipAdapterLS = new ChapterChipAdapter();
                mChapterChipAdapterLS.setOnChipClickListener(mChipClickListener);
            }
            mChapterChipAdapterLS.setChapters(mChapters);
            mChapterChipAdapterLS.updateCurrentIndex(mLastChapterIndex);
        }

        mIvReplayLS = mLandScapeView.findViewById(R.id.iv_replay_landscape);

        mIvPlayPauseLS = mLandScapeView.findViewById(R.id.iv_play_pause_landscape);

        TextView tvQuality = mLandScapeView.findViewById(R.id.tv_quality);

        TextView tvSpeed = mLandScapeView.findViewById(R.id.tv_speed);

        setSeekbarListener(mSeekBarLS);

        setViewClickListener(this, mIvPlayPauseLS, mIvReplayLS, tvQuality, tvSpeed);

        // 初始化章节按钮
        mTvChapterLS = mLandScapeView.findViewById(R.id.tv_chapter_landscape);
        mTvNextChapterLS = mLandScapeView.findViewById(R.id.tv_next_chapter_landscape);
        mTvSeparatorDotLS = mLandScapeView.findViewById(R.id.tv_separator_dot);
        mTvSeparatorPipeLS = mLandScapeView.findViewById(R.id.tv_separator_pipe);

        setViewClickListener(this, mTvChapterLS, mTvNextChapterLS);

        // 如果章节数据已加载，同步按钮显示状态
        if (isChapterAvailable()) {
            setChapterButtonsVisible(true);
            updateChapterButtonText();
        }
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        // 存储场景类型，用于判断是否允许进度条拖拽
        mSceneType = model.getSceneType();
        // 重置播放进度状态，待新视频进度事件更新；
        // mDuration 置 0：防止新视频 Prepared 前切全屏时，buildLandScapeView 用旧时长错误重建章节标记
        mDuration = 0;
        // 刷新按钮（可选：直播场景）
        updateReplayButtonVisibility();

        // 章节数据初始化：统一从章节数据通道取值（配置期由 configure 写入，运行期由 setChapters 写入）
        // 注意：Slot 可能重复绑定数据，切换数据源时需重置旧章节状态，避免残留 UI 或旧索引错位
        applyChapters(readChaptersFromChannel());

    }

    /**
     * 更新刷新按钮可见性
     * <p>
     * 仅在直播场景（LIVE）下显示刷新按钮，其他场景隐藏。
     * </p>
     */
    private void updateReplayButtonVisibility() {
        setViewVisible((mSceneType == SceneType.LIVE), mIvReplay, mIvReplayLS);
    }


    @Override
    public void onDetach() {
        // 清理状态
        mIsDragging = false;
        mDuration = 0;
        mSceneType = SceneType.VOD;

        // 清理章节相关状态
        mChapters = null;
        mRvChapterChips = null;
        mChapterChipAdapter = null;
        mChapterChipAdapterLS = null;
        mLastChapterIndex = -1;

        // 清理横屏视图引用，防止重新 attach 时持有已脱离视图树的旧引用
        mViewStub = null;
        mLandScapeView = null;
        mTvTimeLS = null;
        mSeekBarLS = null;
        mIvPlayPauseLS = null;
        mIvReplayLS = null;
        mTvChapterLS = null;
        mTvNextChapterLS = null;
        mChapterMarkerViewLS = null;
        mTvSeparatorDotLS = null;
        mTvSeparatorPipeLS = null;

        // 取消自动隐藏任务
        cancelAutoHide();

        super.onDetach();
    }

    // ==================== 事件处理 ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        super.onEvent(event);

        if (event instanceof PlayerEvents.StateChanged) {
            onStateChanged((PlayerEvents.StateChanged) event);
        } else if (event instanceof PlayerEvents.Prepared) {
            onPrepared((PlayerEvents.Prepared) event);
        } else if (event instanceof PlayerEvents.Info) {
            onInfo((PlayerEvents.Info) event);
        } else if (event instanceof PlayerEvents.ChapterListUpdated) {
            onChaptersUpdated();
        } else if (event instanceof FullscreenEvents.FullScreenChanged) {
            onToggleFullscreen(((FullscreenEvents.FullScreenChanged) event).isFullscreen);
        }
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理播放器状态变化事件
     * <p>
     * 根据新的播放状态更新播放/暂停按钮图标。
     * </p>
     *
     * @param event 状态变化事件，不能为 null
     */
    private void onStateChanged(@NonNull PlayerEvents.StateChanged event) {
        updatePlayPauseIcon(event.newState);
    }

    /**
     * 处理播放器准备完成事件
     * <p>
     * 当视频准备完成时，获取视频总时长并初始化时间显示。
     * </p>
     *
     * @param event 准备完成事件，不能为 null
     */
    private void onPrepared(@NonNull PlayerEvents.Prepared event) {
        mDuration = event.duration;
        updateTimeText(0, mDuration);

        if (isChapterAvailable() && mDuration > 0) {
            applyChapterMarkers(mDuration);
        }
    }

    /**
     * 处理播放器信息更新事件
     * <p>
     * 当不在拖动状态时，更新播放进度、缓冲进度和时间显示。
     * 拖动时暂停更新，避免拖动操作被进度更新覆盖。
     * </p>
     *
     * @param event 信息更新事件，不能为 null
     */
    private void onInfo(@NonNull PlayerEvents.Info event) {
        // 不在拖动状态时更新
        if (mIsDragging) {
            return;
        }
        mDuration = event.duration;
        updateProgress(event.currentPosition, event.bufferedPosition, event.duration);

        // 章节可用时更新章节高亮
        if (isChapterAvailable()) {
            updateChapterHighlight(event.currentPosition);
        }
    }


    // ==================== UI 更新方法 ====================

    /**
     * 更新播放/暂停按钮图标
     * <p>
     * 根据播放状态显示对应的图标（播放中显示暂停图标，其他状态显示播放图标）。
     * </p>
     *
     * @param state 播放器状态，不能为 null
     */
    private void updatePlayPauseIcon(@NonNull PlayerState state) {
        if (null != mIvPlayPause) {
            mIvPlayPause.setSelected(state == PlayerState.PLAYING);
        }

        if (null != mIvPlayPauseLS) {
            mIvPlayPauseLS.setSelected(state == PlayerState.PLAYING);
        }
    }

    /**
     * 更新进度条和时间显示
     * <p>
     * 根据当前播放位置、缓冲位置和总时长更新进度条和时间显示。
     * </p>
     *
     * @param current  当前播放位置（毫秒）
     * @param buffered 缓冲位置（毫秒）
     * @param duration 总时长（毫秒）
     */
    private void updateProgress(long current, long buffered, long duration) {
        if (duration <= 0) {
            return;
        }
        int progress = Math.round((float) current * 100 / duration);
        int secondaryProgress = Math.round((float) buffered * 100 / duration);

        if (null != mSeekBar) {
            mSeekBar.setProgress(progress);
            mSeekBar.setSecondaryProgress(secondaryProgress);
        }

        if (null != mSeekBarLS) {
            mSeekBarLS.setProgress(progress);
            mSeekBarLS.setSecondaryProgress(secondaryProgress);
        }

        updateTimeText(current, duration);
    }

    /**
     * 更新时间显示文本
     * <p>
     * 格式化并显示当前播放时间和总时长，格式为 "当前时间/总时长"。
     * </p>
     *
     * @param current  当前播放时间（毫秒）
     * @param duration 总时长（毫秒）
     */
    private void updateTimeText(long current, long duration) {
        String text = FormatUtil.formatDuration(current) + "/" + FormatUtil.formatDuration(duration);
        if (null != mTvTime) {
            mTvTime.setText(text);
        }
        if (null != mTvTimeLS) {
            mTvTimeLS.setText(text);
        }
    }

    /**
     * 恢复进度条位置
     * <p>
     * 根据当前播放位置更新进度条位置。
     * </p>
     *
     * @param seekBar 进度条，不能为 null
     */
    private void restoreSeekBarPosition(SeekBar seekBar) {
        SlotHost host = getHost();
        if (host != null && seekBar != null && mDuration > 0) {
            long currentPosition = host.getPlayerStateStore().getCurrentPosition();
            seekBar.setProgress(Math.round((float) currentPosition * 100 / mDuration));
        }
    }


    // ==================== 工具方法 ====================

    /**
     * 批量设置视图可见性
     * <p>
     * 安全处理 null 视图（横屏视图在 inflate 前为 null）。
     * </p>
     *
     * @param visible 是否可见
     * @param views   目标视图列表
     */
    private void setViewVisible(boolean visible, View... views) {
        int visibleValue = visible ? View.VISIBLE : View.GONE;
        if (null != views) {
            for (View v : views) {
                if (null != v) {
                    v.setVisibility(visibleValue);
                }
            }
        }
    }

    /**
     * 批量设置点击监听器
     * <p>
     * 安全处理 null 视图。
     * </p>
     *
     * @param listener 点击监听器
     * @param views    目标视图列表
     */
    private void setViewClickListener(View.OnClickListener listener, View... views) {
        if (null != views) {
            for (View v : views) {
                if (null != v) {
                    v.setOnClickListener(listener);
                }
            }
        }
    }

    /**
     * 为进度条设置触摸拦截和拖动监听
     * <p>
     * 统一竖屏和横屏 SeekBar 的行为：特定场景下禁止拖动，正常场景下支持拖动跳转。
     * </p>
     *
     * @param bar 目标进度条
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setSeekbarListener(SeekBar bar) {
        if (null == bar) return;

        // 特定场景下，进度条直接不允许触摸（仅展示进度，不允许用户拖拽跳转）
        // 说明：
        // 1) 通过拦截触摸事件避免"拖一下又回弹"的体验和不同 ROM 的行为差异；
        // 2) 不影响播放器 Info 更新进度（setProgress 仍然生效）。
        bar.setOnTouchListener((v, event) -> {
            if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                return true; // 消费事件，阻止进一步处理
            }
            return false; // 不消费事件，允许正常处理
        });

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 特定场景下禁用进度拖拽
                    // 说明：通常不会走到这里（因为上面拦截了触摸），保留判断作为防御
                    if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                        // 恢复进度条到当前播放位置
                        restoreSeekBarPosition(seekBar);
                        return;
                    }

                    // 用户拖动时，实时更新时间显示
                    long targetPosition = (long) ((progress / 100.0f) * mDuration);
                    updateTimeText(targetPosition, mDuration);
                    // 章节可用时更新章节高亮
                    updateChapterHighlight(targetPosition);
                    // 发布 Seek 缩略图更新事件
                    if (mPlayerId != null) {
                        postEvent(new ControlBarEvents.UpdateSeekThumbnail(mPlayerId, targetPosition, mDuration));
                    }
                    notifyInteraction();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 特定场景下禁用进度拖拽
                // 说明：通常不会走到这里（因为上面拦截了触摸），保留判断作为防御
                if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                    // 取消拖动操作
                    seekBar.setPressed(false);
                    return;
                }

                // 开始拖动时，标记拖动状态并重置自动隐藏计时器
                mIsDragging = true;
                // 发布 Seek 缩略图显示事件
                if (mPlayerId != null) {
                    postEvent(new ControlBarEvents.ShowSeekThumbnail(mPlayerId));
                }
                notifyInteraction();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 特定场景下禁用进度拖拽
                // 说明：通常不会走到这里（因为上面拦截了触摸），保留判断作为防御
                if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) {
                    // 恢复进度条到当前播放位置
                    restoreSeekBarPosition(seekBar);
                    return;
                }

                // 结束拖动时，执行跳转操作
                mIsDragging = false;
                // 发布 Seek 缩略图隐藏事件
                if (mPlayerId != null) {
                    postEvent(new ControlBarEvents.HideSeekThumbnail(mPlayerId));
                }
                long targetPosition = (long) ((seekBar.getProgress() / 100.0f) * mDuration);
                seekTo(targetPosition);

                notifyInteraction();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (null == mPlayerId) return;

        // 播放/暂停
        if (id == R.id.iv_play_pause || id == R.id.iv_play_pause_landscape) {
            notifyInteraction();
            postEvent(new PlayerCommand.Toggle(mPlayerId));
        }
        // 直播场景: 刷新
        else if (id == R.id.iv_replay || id == R.id.iv_replay_landscape) {
            notifyInteraction();
            postEvent(new PlayerCommand.Replay(mPlayerId));
        }
        // 清晰度
        else if (id == R.id.tv_quality) {
            showQualityDialog();
        }
        // 倍速
        else if (id == R.id.tv_speed) {
            showSpeedDialog();
        }
        // 章节面板
        else if (id == R.id.tv_chapter_landscape) {
            notifyInteraction();
            showChapterPanel();
        }
        // 下一章
        else if (id == R.id.tv_next_chapter_landscape) {
            // 单章数据不存在"下一章"，不响应点击
            if (mChapters == null || mChapters.size() <= 1) return;
            notifyInteraction();
            seekToNextChapter();
        }
    }

    private void showQualityDialog() {
        if (mPlayerId == null) return;
        postEvent(new ControlBarEvents.Hide(mPlayerId));
        postEvent(new ControlBarEvents.ShowQualityPanel(mPlayerId));
    }

    private void showSpeedDialog() {
        if (mPlayerId == null) return;
        postEvent(new ControlBarEvents.Hide(mPlayerId));
        postEvent(new ControlBarEvents.ShowSpeedPanel(mPlayerId));
    }

    /**
     * 显示章节面板
     * <p>
     * Show chapter panel
     * </p>
     */
    private void showChapterPanel() {
        if (mPlayerId == null) return;
        postEvent(new ControlBarEvents.Hide(mPlayerId));
        postEvent(new ControlBarEvents.ShowChapterPanel(mPlayerId));
    }

    /**
     * 定位到下一章节
     * <p>
     * Seek to next chapter
     * </p>
     */
    private void seekToNextChapter() {
        // RESTRICTED 等禁跳场景：不发起跳转，也不提示“已跳转”
        if (!isChapterSeekEnabled()) return;
        if (!isChapterAvailable() || mPlayerId == null) return;
        Context context = getContext();
        if (context == null) return;

        // 尚未定位到任何章节（如播放位置处于首个章节之前）时，下一章即第一章；
        // 播放位置处于章节空隙时 mLastChapterIndex 保留最近的有效章节索引，顺延取其下一章
        int nextIndex = mLastChapterIndex < 0 ? 0 : mLastChapterIndex + 1;
        if (nextIndex >= mChapters.size()) {
            ToastUtils.showToast(R.string.toast_last_chapter);
            return;
        }
        ChapterInfo next = mChapters.get(nextIndex);
        seekTo(next.getStartMs());
        updateChapterHighlight(next.getStartMs());
        String time = FormatUtil.formatDuration(next.getStartMs());
        ToastUtils.showToast(context.getString(R.string.toast_seek_to_chapter, time));
    }

    /**
     * 更新横屏章节按钮文本
     * <p>
     * Update landscape chapter button text
     * </p>
     */
    private void updateChapterButtonText() {
        if (mTvChapterLS == null || !isChapterAvailable() || mLastChapterIndex < 0) return;
        if (mLastChapterIndex >= mChapters.size()) return;
        Context context = getContext();
        if (context == null) return;
        ChapterInfo current = mChapters.get(mLastChapterIndex);
        String text = context.getString(R.string.chapter_button_format,
                mLastChapterIndex + 1, current.getTitle());
        mTvChapterLS.setText(text);
    }

    /**
     * 设置横屏章节按钮的可见性
     * <p>
     * Set landscape chapter buttons visibility
     * </p>
     */
    private void setChapterButtonsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (mTvSeparatorDotLS != null) mTvSeparatorDotLS.setVisibility(visibility);
        if (mTvChapterLS != null) mTvChapterLS.setVisibility(visibility);
        if (mTvSeparatorPipeLS != null) mTvSeparatorPipeLS.setVisibility(visibility);
        if (mTvNextChapterLS != null) mTvNextChapterLS.setVisibility(visibility);
    }

    // ==================== 章节方法 ====================

    /**
     * 章节能力是否可用
     * <p>
     * 章节功能显隐/启用的统一判断入口。
     * 判断规则：非 LIVE/MINIMAL 场景且章节数据非空。
     * </p>
     * <p>
     * Whether chapter feature is available.
     * <p>
     * Rules: the scene is not LIVE/MINIMAL and the chapter list is not empty.
     * </p>
     *
     * @return 章节能力是否可用
     */
    private boolean isChapterAvailable() {
        if (mSceneType == SceneType.LIVE || mSceneType == SceneType.MINIMAL) {
            return false;
        }
        return mChapters != null && !mChapters.isEmpty();
    }

    /**
     * 当前场景是否允许通过章节控件发起跳转
     * <p>
     * RESTRICTED 防跳播：章节 UI 仅作展示，点击不发起跳转、不更新高亮、不提示已跳转；
     * 外部/自定义 UI 发起的 Seek 命令由播放控制器统一兜底拦截。
     * </p>
     * <p>
     * Whether seeking via chapter controls is enabled in the current scene
     * </p>
     *
     * @return 允许跳转返回 true
     */
    private boolean isChapterSeekEnabled() {
        return mSceneType != SceneType.LIVE && mSceneType != SceneType.RESTRICTED;
    }

    /**
     * 清理并隐藏章节相关 UI
     * <p>
     * 切换到无章节数据（或章节能力不可用场景）时调用，防止上一份数据的
     * 章节 Chip、横屏章节按钮和进度条标记残留显示——其点击监听器仍会向新视频发起 Seek。
     * </p>
     * <p>
     * Clear and hide chapter related UI
     * </p>
     */
    private void clearChapterState() {
        mLastChapterIndex = -1;
        // 竖屏章节 Chip 与进度条章节标记（横竖屏）
        setViewVisible(false, mRvChapterChips, mChapterMarkerView, mChapterMarkerViewLS);
        // 横屏章节按钮组（横屏视图未 inflate 时内部判空保护）
        setChapterButtonsVisible(false);
        // 横屏章节标题残留文本
        if (mTvChapterLS != null) {
            mTvChapterLS.setText("");
        }
    }

    /**
     * 从章节数据通道读取章节
     * <p>
     * Read chapters from the chapter data channel
     * </p>
     *
     * @return 章节列表（无数据时为空列表）
     */
    @NonNull
    private List<ChapterInfo> readChaptersFromChannel() {
        SlotHost host = getHost();
        if (host == null) return Collections.emptyList();
        try {
            return host.getPlayerStateStore().getChapters();
        } catch (IllegalStateException e) {
            // 播放器尚未就绪（如 attach 早期），视为无章节
            return Collections.emptyList();
        }
    }

    /**
     * 处理章节数据变更
     * <p>
     * 事件总线为同步派发，业务可能在工作线程写入章节，统一切回主线程刷新 UI。
     * 刷新时重新读取数据通道而非使用事件载荷：排队中的旧载荷可能晚于主线程上的新写入执行，
     * 覆盖最新的章节数据。
     * </p>
     */
    private void onChaptersUpdated() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            applyChaptersFromChannel();
        } else {
            post(this::applyChaptersFromChannel);
        }
    }

    /**
     * 从章节数据通道读取并渲染最新章节
     */
    private void applyChaptersFromChannel() {
        applyChapters(readChaptersFromChannel());
    }

    /**
     * 应用章节数据
     * <p>
     * 章节数据的唯一渲染入口：配置绑定与运行期写入走同一条路径。
     * 数据为空时全部章节入口不渲染（进度条无标记点、横屏信息行回退时间显示、
     * 竖屏无胶囊行、面板入口不存在），界面等同于普通播放器。
     * </p>
     * <p>
     * Apply chapter data, the single rendering entry for both configuration binding
     * and runtime updates.
     * </p>
     *
     * @param chapters 章节列表，可为 null 或空列表（表示无章节）
     */
    private void applyChapters(@Nullable List<ChapterInfo> chapters) {
        // 数据整体替换：旧索引一并失效，后续按实时播放位置重算
        mChapters = (chapters == null || chapters.isEmpty()) ? null : chapters;
        mLastChapterIndex = -1;
        if (mTvChapterLS != null) {
            mTvChapterLS.setText("");
        }

        // 无章节（或 LIVE/MINIMAL 场景）：全部章节入口不渲染
        if (!isChapterAvailable()) {
            clearChapterState();
            return;
        }

        // 竖屏章节胶囊
        if (mChapterChipAdapter == null) {
            mChapterChipAdapter = new ChapterChipAdapter();
            mChapterChipAdapter.setOnChipClickListener(mChipClickListener);
        }
        mChapterChipAdapter.setChapters(mChapters);
        if (mRvChapterChips != null) {
            mRvChapterChips.setAdapter(mChapterChipAdapter);
            // 竖屏胶囊行是竖屏容器的兄弟节点，全屏时需保持隐藏，避免叠加在横屏控制条之上
            mRvChapterChips.setVisibility(isLandscapeLayoutActive() ? View.GONE : View.VISIBLE);
        }

        // 横屏章节胶囊
        if (mChapterChipAdapterLS == null) {
            mChapterChipAdapterLS = new ChapterChipAdapter();
            mChapterChipAdapterLS.setOnChipClickListener(mChipClickListener);
        }
        mChapterChipAdapterLS.setChapters(mChapters);

        // 横屏章节按钮组
        setChapterButtonsVisible(true);

        // 进度条标记点需要时长信息：时长未就绪时先清除旧标记，待 Prepared 后再应用
        if (mDuration > 0) {
            applyChapterMarkers(mDuration);
        } else {
            setViewVisible(false, mChapterMarkerView, mChapterMarkerViewLS);
        }

        // 按实时播放位置重算当前章节（数据替换后旧索引已失效，必须强制刷新）
        updateChapterHighlight(getCurrentPositionMs(), true);
        // 竖屏列表定位：命中时跟随当前章节，无归属时归零
        scrollChapterChipsToCurrent();
    }

    /**
     * 从状态存储读取当前播放位置
     *
     * @return 当前播放位置（毫秒），不可用时返回 0
     */
    private long getCurrentPositionMs() {
        SlotHost host = getHost();
        if (host == null) return 0;
        try {
            return host.getPlayerStateStore().getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    /**
     * 竖屏章节列表定位
     * <p>
     * 跟随当前章节；当前播放位置不属于任何章节时回到列表开头，
     * 避免章节数据整体替换后残留旧偏移。
     * </p>
     * <p>
     * Scroll the portrait chapter list to the current chapter, or back to the
     * beginning when the current position does not belong to any chapter.
     * </p>
     */
    private void scrollChapterChipsToCurrent() {
        int index = mLastChapterIndex;
        boolean valid = index >= 0 && mChapters != null && index < mChapters.size();

        if (!valid) {
            // 无归属：清除高亮并归零，避免残留旧偏移
            if (mChapterChipAdapter != null) {
                mChapterChipAdapter.updateCurrentIndex(-1);
            }
            if (mChapterChipAdapterLS != null) {
                mChapterChipAdapterLS.updateCurrentIndex(-1);
            }
        }

        if (mRvChapterChips != null && mRvChapterChips.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager lm = (LinearLayoutManager) mRvChapterChips.getLayoutManager();
            lm.scrollToPositionWithOffset(valid ? index : 0, 0);
        }
    }

    /**
     * 将章节标记应用到 ChapterMarkerView 上
     * <p>
     * Apply chapter markers to ChapterMarkerView
     * </p>
     */
    private void applyChapterMarkers(long durationMs) {
        if (!isChapterAvailable() || durationMs <= 0) return;

        // 计算归一化位置
        List<Float> positions = new ArrayList<>();
        for (ChapterInfo chapter : mChapters) {
            float pos = (float) chapter.getStartMs() / durationMs;
            if (pos > 0f && pos < 1f) {
                positions.add(pos);
            }
        }

        if (positions.isEmpty()) {
            // 无有效标记（如单章且 startMs=0）时清除上一份数据的残留标记并保持隐藏
            setViewVisible(false, mChapterMarkerView, mChapterMarkerViewLS);
            return;
        }

        // 应用到竖屏标记 View
        if (mChapterMarkerView != null) {
            mChapterMarkerView.setMarkerPositions(positions);
            mChapterMarkerView.setVisibility(View.VISIBLE);
        }
        // 应用到横屏标记 View
        if (mChapterMarkerViewLS != null) {
            mChapterMarkerViewLS.setMarkerPositions(positions);
            mChapterMarkerViewLS.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新章节导航高亮
     * <p>
     * 根据当前播放位置更新 ChapterChipAdapter 的高亮索引，并滚动到当前章节。
     * </p>
     *
     * @param positionMs 当前播放位置（毫秒）
     */
    private void updateChapterHighlight(long positionMs) {
        updateChapterHighlight(positionMs, false);
    }

    /**
     * 更新章节高亮
     * <p>
     * Update the highlighted chapter
     * </p>
     *
     * @param positionMs 当前播放位置（毫秒）
     * @param force      是否强制刷新（章节数据整体替换后旧索引已失效，必须强制重算）
     */
    private void updateChapterHighlight(long positionMs, boolean force) {
        if (!isChapterAvailable()) return;

        int index = ChapterInfo.findIndexByPosition(mChapters, positionMs);
        if (!force && index == mLastChapterIndex) return; // 避免重复更新
        // 播放位置处于章节空隙（未命中任何章节区间）时仅清除展示态，
        // mLastChapterIndex 保留最近的有效章节索引，供「下一章」顺延计算目标
        if (index >= 0) {
            mLastChapterIndex = index;
        }

        if (index >= 0) {
            if (mChapterChipAdapter != null) {
                mChapterChipAdapter.updateCurrentIndex(index);
            }
            if (mChapterChipAdapterLS != null) {
                mChapterChipAdapterLS.updateCurrentIndex(index);
            }
            // 滚动到当前章节使其在最左侧
            if (mRvChapterChips != null) {
                LinearLayoutManager lm = (LinearLayoutManager) mRvChapterChips.getLayoutManager();
                if (lm != null) {
                    lm.scrollToPositionWithOffset(index, 0);
                }
            }
        }

        updateChapterButtonText();
    }
}
