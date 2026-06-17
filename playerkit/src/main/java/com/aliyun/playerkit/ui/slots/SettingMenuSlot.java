package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
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
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotElements;
import com.aliyun.playerkit.ui.setting.SettingConstants;
import com.aliyun.playerkit.ui.setting.SettingItem;
import com.aliyun.playerkit.ui.setting.SettingMenuDialogFragment;
import com.aliyun.playerkit.ui.setting.SettingOptions;
import com.aliyun.playerkit.utils.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 设置菜单插槽（Stub）。
 * <p>
 * 无 UI 的事件桥接层，仍注册为 SETTING_MENU 参与 element 体系。
 * 接收 ShowSettings 事件后，构建过滤后的设置项并弹出 {@link SettingMenuDialogFragment}。
 * </p>
 *
 * @author keria
 */
public class SettingMenuSlot extends BaseSlot {

    private static final String TAG = "SettingMenuSlot";
    private static final String DIALOG_TAG = "SettingMenuDialog";

    @Nullable
    private String mPlayerId;

    @SceneType
    private int mSceneType = SceneType.VOD;

    private final List<SettingItem<?>> mItems = new ArrayList<>();

    @Nullable
    private WeakReference<SettingMenuDialogFragment> mDialogRef;

    public SettingMenuSlot(@NonNull Context context) {
        super(context);
    }

    public SettingMenuSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingMenuSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

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
        dismissDialog();
        mPlayerId = null;
        mSceneType = SceneType.VOD;
        mItems.clear();
        super.onUnbindData();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismissDialog();
    }

    @Nullable
    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return Arrays.asList(
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
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (StringUtil.notEquals(mPlayerId, event.playerId)) {
            return;
        }

        if (event instanceof ControlBarEvents.ShowSettings) {
            toggleDialog();
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

    // ---------------------------------------------------------------------
    // Dialog management
    // ---------------------------------------------------------------------

    private void toggleDialog() {
        if (isDialogShowing()) {
            dismissDialog();
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        FragmentManager fm = getFragmentManager();
        if (fm == null || fm.isStateSaved()) return;
        if (mPlayerId == null) return;

        postEvent(new ControlBarEvents.Hide(mPlayerId));

        buildItems();
        syncWithPlayerState();

        SettingMenuDialogFragment dialog = SettingMenuDialogFragment.newInstance();
        dialog.setItems(mItems);
        dialog.show(fm, DIALOG_TAG);
        mDialogRef = new WeakReference<>(dialog);
    }

    private void dismissDialog() {
        if (!isDialogShowing()) {
            mDialogRef = null;
            return;
        }
        SettingMenuDialogFragment dialog = getDialog();
        if (dialog != null) {
            dialog.dismissImmediately();
        }
        mDialogRef = null;
    }

    private boolean isDialogShowing() {
        SettingMenuDialogFragment dialog = getDialog();
        return dialog != null && dialog.isAdded() && !dialog.isRemoving();
    }

    @Nullable
    private SettingMenuDialogFragment getDialog() {
        return mDialogRef != null ? mDialogRef.get() : null;
    }

    @Nullable
    private FragmentManager getFragmentManager() {
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                return activity.getSupportFragmentManager();
            }
        }
        return null;
    }

    @Override
    public boolean isShow() {
        return isDialogShowing();
    }

    @Override
    public void show() {
        if (!isDialogShowing()) {
            showDialog();
        }
    }

    @Override
    public void gone() {
        dismissDialog();
    }

    // ---------------------------------------------------------------------
    // Data
    // ---------------------------------------------------------------------

    private void buildItems() {
        mItems.clear();
        for (SettingItem<?> item : SettingConstants.createDefaultItems(this)) {
            if ((mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED)
                    && SettingConstants.KEY_SPEED.equals(item.key)) {
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

    private void notifyDialog() {
        SettingMenuDialogFragment dialog = getDialog();
        if (dialog != null) {
            dialog.notifyItemsChanged();
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
            notifyDialog();
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
            notifyDialog();
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
            notifyDialog();
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
}
