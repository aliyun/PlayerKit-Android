package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.slot.BasePanelSlot;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.ui.setting.SettingConstants;
import com.aliyun.playerkit.ui.setting.SettingItem;
import com.aliyun.playerkit.ui.setting.SettingMenuDialogFragment;
import com.aliyun.playerkit.ui.setting.SettingOptions;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 设置菜单插槽（控制器角色）。
 * <p>
 * 继承 {@link BasePanelSlot}，保持统一 API、状态管理和防重入能力。
 * 自身不渲染面板内容，仅作为控制器，将面板的实际渲染委托给
 * {@link SettingMenuDialogFragment}（拥有独立 Window，从屏幕边界弹出）。
 * </p>
 *
 * @author keria
 */
public class SettingMenuSlot extends BasePanelSlot {

    private static final String TAG = "SettingMenuSlot";

    private static final String FRAGMENT_TAG = "SettingMenuDialog";

    /**
     * 横屏模式下需要排除的设置项 key 列表。
     * <p>
     * 横屏时倍速和清晰度由独立的 Slot 控制，无需在设置菜单中重复展示。
     * </p>
     */
    private static final List<String> LANDSCAPE_EXCLUDED_KEYS = Arrays.asList(
            SettingConstants.KEY_SPEED, SettingConstants.KEY_QUALITY);

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            ControlBarEvents.ShowSettings.class,
            PlayerEvents.SetSpeedCompleted.class,
            PlayerEvents.SetLoopCompleted.class,
            PlayerEvents.SetMuteCompleted.class,
            PlayerEvents.SetScaleTypeCompleted.class,
            PlayerEvents.SetMirrorTypeCompleted.class,
            PlayerEvents.SetRotationCompleted.class,
            PlayerEvents.TrackQualityListUpdated.class,
            PlayerEvents.TrackSelected.class
    );

    // -- DialogFragment --

    @Nullable
    private SettingMenuDialogFragment mDialogFragment;

    // -- Data --

    @Nullable
    private String mPlayerId;

    @SceneType
    private int mSceneType = SceneType.VOD;

    /**
     * 全量设置项列表（竖屏使用）
     */
    private final List<SettingItem<?>> mItems = new ArrayList<>();

    /**
     * 横屏设置项列表（排除了 LANDSCAPE_EXCLUDED_KEYS 中的项）
     */
    private final List<SettingItem<?>> mLandscapeItems = new ArrayList<>();

    // ==================== 构造 ====================

    public SettingMenuSlot(@NonNull Context context) {
        super(context);
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

        mDialogFragment = SettingMenuDialogFragment.newInstance();
        mDialogFragment.setItems(mItems, mLandscapeItems);
        mDialogFragment.setOnDismissListener(() -> {
            // DialogFragment 被关闭时（用户操作或系统行为），同步 Slot 状态。
            // hidePanel() 内部有 mIsShowing 防重入守卫，如果是 Slot 主动调用的
            // hidePanel → onPerformHideAnimation 路径，此时 mIsShowing 已为 false，
            // 再次调用 hidePanel() 会被守卫拦截，不会产生循环。
            mDialogFragment = null;
            hidePanel();
        });
        mDialogFragment.show(fm, FRAGMENT_TAG);
    }

    @Override
    protected void onPerformHideAnimation() {
        SettingMenuDialogFragment fragment = mDialogFragment;
        mDialogFragment = null;
        if (fragment != null && fragment.isAdded()) {
            fragment.dismissWithAnimation();
        }
    }

    // ==================== Slot 生命周期 ====================

    @Override
    public void onAttach(@NonNull com.aliyun.playerkit.slot.SlotHost host) {
        super.onAttach(host);
        setVisibility(View.GONE);
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        mPlayerId = getPlayerId();
        mSceneType = model.getSceneType();
    }

    @Override
    public void onUnbindData() {
        if (isShowing()) {
            hidePanel();
        }
        mPlayerId = null;
        mSceneType = SceneType.VOD;
        clearItems();
        super.onUnbindData();
    }

    @Override
    public void onDetach() {
        if (mDialogFragment != null) {
            mDialogFragment.dismissAllowingStateLoss();
            mDialogFragment = null;
        }
        clearItems();
        super.onDetach();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isShowing()) {
            // 旋转时直接隐藏面板（同原实现行为）
            hidePanel();
        }
    }

    // ==================== 公开 API ====================

    @Override
    public boolean isShow() {
        return isShowing();
    }

    @Override
    public void show() {
        if (!isShowing()) {
            doShowPanel();
        }
    }

    @Override
    public void gone() {
        if (isShowing()) {
            hidePanel();
        }
    }

    // ==================== 事件处理 ====================

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (StringUtil.notEquals(mPlayerId, event.playerId)) {
            return;
        }

        if (event instanceof ControlBarEvents.ShowSettings) {
            togglePanel();
        } else if (event instanceof PlayerEvents.TrackQualityListUpdated) {
            updateClarityOptions(((PlayerEvents.TrackQualityListUpdated) event).trackQualityList);
        } else if (event instanceof PlayerEvents.TrackSelected) {
            updateSelectedTrack(((PlayerEvents.TrackSelected) event).trackIndex);
        } else if (event instanceof PlayerEvents.SetSpeedCompleted) {
            updateItemValue(SettingConstants.KEY_SPEED, ((PlayerEvents.SetSpeedCompleted) event).speed);
        } else if (event instanceof PlayerEvents.SetLoopCompleted) {
            updateItemValue(SettingConstants.KEY_LOOP, ((PlayerEvents.SetLoopCompleted) event).loop);
        } else if (event instanceof PlayerEvents.SetMuteCompleted) {
            updateItemValue(SettingConstants.KEY_MUTE, ((PlayerEvents.SetMuteCompleted) event).mute);
        } else if (event instanceof PlayerEvents.SetScaleTypeCompleted) {
            updateItemValue(SettingConstants.KEY_SCALE, ((PlayerEvents.SetScaleTypeCompleted) event).scaleType);
        } else if (event instanceof PlayerEvents.SetMirrorTypeCompleted) {
            updateItemValue(SettingConstants.KEY_MIRROR, ((PlayerEvents.SetMirrorTypeCompleted) event).mirrorType);
        } else if (event instanceof PlayerEvents.SetRotationCompleted) {
            updateItemValue(SettingConstants.KEY_ROTATE, ((PlayerEvents.SetRotationCompleted) event).rotation);
        }
    }

    // ==================== 面板显隐 ====================

    private void togglePanel() {
        if (isShowing()) {
            hidePanel();
        } else {
            doShowPanel();
        }
    }

    private void doShowPanel() {
        if (mPlayerId == null) return;

        postEvent(new ControlBarEvents.Hide(mPlayerId));

        buildItems();
        syncWithPlayerState();

        showPanel();
    }

    // ==================== 数据管理 ====================

    private void buildItems() {
        mItems.clear();
        mLandscapeItems.clear();
        for (SettingItem<?> item : SettingConstants.createDefaultItems(this)) {
            if ((mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) && SettingConstants.KEY_SPEED.equals(item.key)) {
                continue;
            }
            if (mSceneType == SceneType.LIVE && SettingConstants.KEY_LOOP.equals(item.key)) {
                continue;
            }
            String elementKey = mapToSlotElementKey(item.key);
            if (elementKey != null && !isElementVisible(elementKey)) {
                continue;
            }
            mItems.add(item);
            if (!LANDSCAPE_EXCLUDED_KEYS.contains(item.key)) {
                mLandscapeItems.add(item);
            }
        }
    }

    private void syncWithPlayerState() {
        IPlayerStateStore store = getHost() != null ? getHost().getPlayerStateStore() : null;
        if (store == null) return;

        List<TrackQuality> qualityList = store.getTrackQualityList();
        if (qualityList != null && !qualityList.isEmpty()) {
            applyClarityOptions(qualityList);
            applySelectedTrack(store.getCurrentTrackIndex());
        }

        applyItemValue(SettingConstants.KEY_SPEED, store.getCurrentSpeed());
        applyItemValue(SettingConstants.KEY_LOOP, store.isLoop());
        applyItemValue(SettingConstants.KEY_MUTE, store.isMute());
        applyItemValue(SettingConstants.KEY_SCALE, store.getCurrentScaleType());
        applyItemValue(SettingConstants.KEY_MIRROR, store.getCurrentMirrorType());
        applyItemValue(SettingConstants.KEY_ROTATE, store.getCurrentRotation());
    }

    private void notifyAdapters() {
        if (mDialogFragment != null) {
            mDialogFragment.notifyDataChanged();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean applyItemValue(@NonNull String key, @Nullable Object newValue) {
        if (newValue == null) return false;
        SettingItem<Object> item = findItem(key);
        if (item == null) return false;
        item.currentValue = newValue;
        return true;
    }

    private void updateItemValue(@NonNull String key, @Nullable Object newValue) {
        if (applyItemValue(key, newValue)) {
            notifyAdapters();
        }
    }

    private boolean applyClarityOptions(@Nullable List<TrackQuality> qualityList) {
        if (qualityList == null || qualityList.isEmpty()) return false;
        SettingItem<TrackQuality> item = findItem(SettingConstants.KEY_QUALITY);
        if (item == null) return false;
        TrackQuality[] arr = qualityList.toArray(new TrackQuality[0]);
        item.options = SettingOptions.of(arr);
        if (item.currentValue == null) {
            item.currentValue = arr[0];
        }
        return true;
    }

    private void updateClarityOptions(@Nullable List<TrackQuality> qualityList) {
        if (applyClarityOptions(qualityList)) {
            notifyAdapters();
        }
    }

    private boolean applySelectedTrack(int trackIndex) {
        SettingItem<TrackQuality> item = findItem(SettingConstants.KEY_QUALITY);
        if (item == null) return false;
        SettingOptions<TrackQuality> options = item.options;
        if (options == null) return false;
        for (int i = 0; i < options.size(); i++) {
            TrackQuality q = options.get(i);
            if (q.getIndex() == trackIndex) {
                item.currentValue = q;
                return true;
            }
        }
        return false;
    }

    private void updateSelectedTrack(int trackIndex) {
        if (applySelectedTrack(trackIndex)) {
            notifyAdapters();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> SettingItem<T> findItem(String key) {
        for (SettingItem<?> item : mItems) {
            if (item.key.equals(key)) {
                return (SettingItem<T>) item;
            }
        }
        return null;
    }

    private void clearItems() {
        for (SettingItem<?> item : mItems) {
            item.listener = null;
        }
        mItems.clear();
        mLandscapeItems.clear();
    }

    @Nullable
    private String mapToSlotElementKey(@NonNull String itemKey) {
        switch (itemKey) {
            case SettingConstants.KEY_SPEED:
                return SlotElements.SettingMenu.SPEED;
            case SettingConstants.KEY_QUALITY:
                return SlotElements.SettingMenu.TRACK_INFO;
            case SettingConstants.KEY_LOOP:
                return SlotElements.SettingMenu.LOOP;
            case SettingConstants.KEY_MUTE:
                return SlotElements.SettingMenu.MUTE;
            case SettingConstants.KEY_MIRROR:
                return SlotElements.SettingMenu.MIRROR_MODE;
            case SettingConstants.KEY_SCALE:
                return SlotElements.SettingMenu.SCALE_MODE;
            case SettingConstants.KEY_ROTATE:
                return SlotElements.SettingMenu.ROTATE_MODE;
            default:
                return null;
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
