package com.aliyun.playerkit.scenes.aieducation.ui.slots;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.scenes.aieducation.data.AiContentRepository;
import com.aliyun.playerkit.scenes.aieducation.data.AiSegment;
import com.aliyun.playerkit.scenes.aieducation.data.AiVideoContent;
import com.aliyun.playerkit.scenes.aieducation.event.ChapterEvents;
import com.aliyun.playerkit.scenes.aieducation.ui.MarkdownBinder;
import com.aliyun.playerkit.slot.BasePanelSlot;
import com.aliyun.playerkit.slot.CustomSlotType;
import com.aliyun.playerkit.slot.SlotHost;

import java.util.Arrays;
import java.util.List;

/**
 * 章节面板插槽（控制器角色）。
 * <p>
 * 继承 {@link BasePanelSlot}，保持统一 API、状态管理和防重入能力。
 * 自身不渲染面板内容，仅作为控制器，将面板的实际渲染委托给
 * {@link ChapterPanelDialogFragment}（拥有独立 Window，从屏幕边界弹出）。
 * </p>
 * <ul>
 *     <li>接收 {@link ChapterEvents.ShowPanel} 展开面板</li>
 *     <li>接收 {@link ChapterEvents.HidePanel} 收起面板</li>
 *     <li>订阅 {@link PlayerEvents.Info} 实时高亮当前章节</li>
 *     <li>点击章节项发送 {@link PlayerCommand.Seek} 跳转到对应位置</li>
 * </ul>
 *
 * @author keria
 * @date 2026/07/03
 */
@SuppressLint("ViewConstructor")
public class ChapterPanelSlot extends BasePanelSlot {

    /**
     * 插槽类型定义
     * <p>
     * order=105：位于 SETTING_MENU 之上，确保面板在最顶层
     * </p>
     */
    public static final CustomSlotType TYPE = new CustomSlotType("chapter_panel", 105);

    private static final String FRAGMENT_TAG = "ChapterPanelDialog";

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            ChapterEvents.ShowPanel.class,
            ChapterEvents.HidePanel.class,
            PlayerEvents.Info.class
    );

    private final AiContentRepository mRepository;

    @Nullable
    private AiVideoContent mContent;
    @Nullable
    private MarkdownBinder mMarkdownBinder;

    // -- DialogFragment --
    @Nullable
    private ChapterPanelDialogFragment mDialogFragment;

    // -- Highlight state --
    private int mCurrentChapterIndex = -1;
    private int mCurrentSegmentIndex = -1;

    public ChapterPanelSlot(@NonNull Context context, @NonNull AiContentRepository repository) {
        super(context);
        this.mRepository = repository;
        this.mMarkdownBinder = new MarkdownBinder(context.getApplicationContext());
        // 该 Slot 不渲染任何内容，始终不可见
        setVisibility(View.GONE);
    }

    /**
     * 不加载布局，该 Slot 仅作为控制器。
     */
    @Override
    protected int getLayoutId() {
        return 0;
    }

    // ==================== BasePanelSlot 实现 ====================

    @Override
    protected void onPerformShowAnimation() {
        FragmentActivity activity = getFragmentActivity();
        if (activity == null) return;
        if (activity.isFinishing() || activity.isDestroyed()) return;
        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.isStateSaved()) return;

        mDialogFragment = ChapterPanelDialogFragment.newInstance();
        mDialogFragment.setContent(mContent);
        mDialogFragment.setMarkdownBinder(mMarkdownBinder);
        mDialogFragment.setOnChapterClickListener(this::onChapterClicked);
        mDialogFragment.setOnSegmentClickListener(this::onSegmentClicked);
        mDialogFragment.setOnDismissListener(() -> {
            // DialogFragment 被关闭时（用户操作或系统行为），同步 Slot 状态。
            // hidePanel() 内部有 mIsShowing 防重入守卫，如果是 Slot 主动调用的
            // hidePanel → onPerformHideAnimation 路径，此时 mIsShowing 已为 false，
            // 再次调用 hidePanel() 会被守卫拦截，不会产生循环。
            mDialogFragment = null;
            boolean wasShowing = isShowing();
            hidePanel();
            // 仅当 dismiss 由用户操作触发（而非 HidePanel 事件路径）时，
            // 才发送 HidePanel 事件同步 ChapterButtonSlot 的状态。
            if (wasShowing) {
                SlotHost host = getHost();
                if (host != null) {
                    String playerId = host.getPlayerId();
                    if (playerId != null) {
                        postEvent(new ChapterEvents.HidePanel(playerId));
                    }
                }
            }
        });
        mDialogFragment.show(fm, FRAGMENT_TAG);
    }

    @Override
    protected void onPerformHideAnimation() {
        ChapterPanelDialogFragment fragment = mDialogFragment;
        mDialogFragment = null;
        if (fragment != null && fragment.isAdded()) {
            fragment.dismissWithAnimation();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isShowing()) {
            hidePanel();
        }
    }

    // ==================== Lifecycle ====================

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
        setVisibility(View.GONE);
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        loadContent(model);
    }

    @Override
    public void onUnbindData() {
        if (isShowing()) {
            hidePanel();
        }
        mContent = null;
        mCurrentChapterIndex = -1;
        mCurrentSegmentIndex = -1;
        super.onUnbindData();
    }

    @Override
    public void onDetach() {
        if (mDialogFragment != null) {
            mDialogFragment.dismissAllowingStateLoss();
            mDialogFragment = null;
        }
        mCurrentChapterIndex = -1;
        mCurrentSegmentIndex = -1;
        mContent = null;
        mMarkdownBinder = null;
        super.onDetach();
    }

    // ==================== Data binding ====================

    private void loadContent(@NonNull AliPlayerModel model) {
        mContent = mRepository.load(model);
    }

    // ==================== Click callbacks ====================

    private void onSegmentClicked(@NonNull AiSegment segment) {
        SlotHost host = getHost();
        if (host == null) return;

        String playerId = host.getPlayerId();
        if (playerId == null) return;

        postEvent(new PlayerCommand.Seek(playerId, segment.getStartMs()));
    }

    private void onChapterClicked(@NonNull ChapterInfo chapter) {
        SlotHost host = getHost();
        if (host == null) return;

        String playerId = host.getPlayerId();
        if (playerId == null) return;

        // Seek to chapter start
        postEvent(new PlayerCommand.Seek(playerId, chapter.getStartMs()));
    }

    // ==================== Event handling ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof ChapterEvents.ShowPanel) {
            showPanel();
        } else if (event instanceof ChapterEvents.HidePanel) {
            hidePanel();
        } else if (event instanceof PlayerEvents.Info) {
            onPlaybackInfo((PlayerEvents.Info) event);
        }
    }

    private void onPlaybackInfo(@NonNull PlayerEvents.Info info) {
        if (mContent == null) return;

        // Update chapter highlight
        int newChapterIndex = ChapterInfo.findIndexByPosition(mContent.getChapters(), info.currentPosition);
        if (newChapterIndex != mCurrentChapterIndex) {
            int oldIndex = mCurrentChapterIndex;
            mCurrentChapterIndex = newChapterIndex;
            if (mDialogFragment != null) {
                mDialogFragment.updateChapterHighlight(oldIndex, newChapterIndex);
            }
        }

        // Update segment highlight
        int newSegmentIndex = mContent.findSegmentIndexByPosition(info.currentPosition);
        if (newSegmentIndex != mCurrentSegmentIndex) {
            int oldIndex = mCurrentSegmentIndex;
            mCurrentSegmentIndex = newSegmentIndex;
            if (mDialogFragment != null) {
                mDialogFragment.updateSegmentHighlight(oldIndex, newSegmentIndex);
            }
        }
    }

    // ==================== 工具方法 ====================

    @Nullable
    private FragmentActivity getFragmentActivity() {
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        }
        return null;
    }
}
