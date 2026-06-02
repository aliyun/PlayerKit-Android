package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
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
import com.aliyun.playerkit.ui.setting.SettingItemLandscapeAdapter;
import com.aliyun.playerkit.ui.setting.SettingItemPortraitAdapter;
import com.aliyun.playerkit.ui.setting.SettingOptions;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 设置菜单插槽。
 * <p>
 * 提供倍速、清晰度、循环播放等设置项，并与播放器状态保持同步。
 * 根据屏幕方向自动切换横屏/竖屏布局。
 * </p>
 *
 * <p>
 * Settings menu slot.
 * Provides setting items (speed/quality/loop, etc.) and keeps them in sync with player state.
 * Automatically switches between landscape/portrait layout based on screen orientation.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public class SettingMenuSlot extends BaseSlot {

    private static final String TAG = "SettingMenuSlot";

    // 显隐动画时长
    private static final long ANIM_DURATION_MS = 300L;
    // 横屏默认面板宽度
    private static final float FALLBACK_PANEL_WIDTH_DP = 345f;
    // 竖屏默认面板高度
    private static final float FALLBACK_PANEL_HEIGHT_DP = 400f;
    // 横屏/竖屏容器
    private View mLandscapeContainer;
    private View mPortraitContainer;

    // 适配器
    private SettingItemLandscapeAdapter mLandscapeAdapter;
    private SettingItemPortraitAdapter mPortraitAdapter;

    // 全量设置项（竖屏使用）
    private final List<SettingItem<?>> mItems = new ArrayList<>();
    // 横屏设置项（过滤掉倍速和清晰度）
    private final List<SettingItem<?>> mLandscapeItems = new ArrayList<>();

    // 横屏不显示的设置项
    private static final List<String> LANDSCAPE_EXCLUDED_KEYS = Arrays.asList(
            SettingConstants.KEY_SPEED,
            SettingConstants.KEY_QUALITY
    );

    // 是否正在执行动画
    private boolean mIsAnimating;

    /**
     * 当前绑定的播放器 ID
     * <p>
     * 用于过滤事件，确保只处理当前播放器的事件。
     * </p>
     */
    @Nullable
    private String mPlayerId;

    /**
     * 当前场景类型
     * <p>
     * 用于判断是否允许某些设置项：
     * - 倍速设置：LIVE 和 RESTRICTED 场景下禁用
     * - 循环播放设置：LIVE 场景下禁用
     * </p>
     */
    @SceneType
    private int mSceneType = SceneType.VOD;

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
    protected int getLayoutId() {
        return R.layout.layout_setting_menu_slot;
    }

    @Override
    public void onAttach(@NonNull com.aliyun.playerkit.slot.SlotHost host) {
        super.onAttach(host);

        // 设置背景蒙层和点击关闭
        setBackgroundColor(0x80000000);
        setClickable(true);
        setFocusable(true);
        setOnClickListener(v -> gone());

        // 查找横竖屏容器
        mPortraitContainer = findViewById(R.id.ll_portrait_container);
        mLandscapeContainer = findViewById(R.id.ll_landscape_container);

        // 初始化设置项
        initSettingItems();

        // 设置横屏 RecyclerView
        mLandscapeAdapter = new SettingItemLandscapeAdapter(mLandscapeItems, getContext());
        RecyclerView landscapeRecycler = findViewById(R.id.rc_landscape_setting_recycler);
        if (landscapeRecycler != null) {
            landscapeRecycler.setAdapter(mLandscapeAdapter);
            landscapeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // 设置竖屏 RecyclerView
        mPortraitAdapter = new SettingItemPortraitAdapter(mItems, getContext());
        RecyclerView portraitRecycler = findViewById(R.id.rc_portrait_setting_recycler);
        if (portraitRecycler != null) {
            portraitRecycler.setAdapter(mPortraitAdapter);
            portraitRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // 根据当前方向显示对应容器
        updateOrientation();

        // 默认隐藏
        setVisibility(View.GONE);
        setAlpha(0f);

        // Initial sync
        syncWithPlayerState();
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        mPlayerId = getPlayerId();
        mSceneType = model.getSceneType();
        // 根据场景类型更新设置项（禁用倍速等）
        updateItemsForScene();
    }

    @Override
    public void onUnbindData() {
        mPlayerId = null;
        mSceneType = SceneType.VOD;
        mIsAnimating = false;

        mItems.clear();
        mLandscapeItems.clear();

        super.onUnbindData();
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
        // 过滤 playerId
        if (StringUtil.notEquals(mPlayerId, event.playerId)) {
            return;
        }

        if (event instanceof ControlBarEvents.ShowSettings) {
            toggleVisibility();
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
    // Orientation
    // ---------------------------------------------------------------------

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 方向变化时，若菜单正在显示或动画中则取消动画并立即隐藏
        if (isShow() || mIsAnimating) {
            cancelAnimations();
            setVisibility(View.GONE);
            setAlpha(0f);
            mIsAnimating = false;
            resetTranslations();
        }
        updateOrientation();
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void updateOrientation() {
        boolean landscape = isLandscape();
        if (mLandscapeContainer != null) {
            mLandscapeContainer.setVisibility(landscape ? View.VISIBLE : View.GONE);
        }
        if (mPortraitContainer != null) {
            mPortraitContainer.setVisibility(landscape ? View.GONE : View.VISIBLE);
        }
    }

    @Nullable
    private View getActiveContentView() {
        return isLandscape() ? mLandscapeContainer : mPortraitContainer;
    }

    private void resetTranslations() {
        if (mLandscapeContainer != null) {
            mLandscapeContainer.setTranslationX(0);
        }
        if (mPortraitContainer != null) {
            mPortraitContainer.setTranslationY(0);
        }
    }

    private void cancelAnimations() {
        animate().cancel();
        if (mLandscapeContainer != null) {
            mLandscapeContainer.animate().cancel();
        }
        if (mPortraitContainer != null) {
            mPortraitContainer.animate().cancel();
        }
    }

    // ---------------------------------------------------------------------
    // Visibility & Animation
    // ---------------------------------------------------------------------

    @Override
    public void show() {
        if (isShow() || mIsAnimating) {
            return;
        }
        mIsAnimating = true;

        // Ensure state is up-to-date before showing.
        syncWithPlayerState();

        // 隐藏控制栏（顶部栏和底部栏），避免遮挡设置面板
        postEvent(new ControlBarEvents.Hide(mPlayerId));

        bringToFront();
        setVisibility(View.VISIBLE);

        View contentView = getActiveContentView();
        if (contentView == null) {
            setVisibility(View.GONE);
            setAlpha(0f);
            mIsAnimating = false;
            return;
        }

        setAlpha(0f);
        animate().alpha(1f).setDuration(ANIM_DURATION_MS).start();

        if (isLandscape()) {
            float width = contentView.getWidth() > 0
                    ? contentView.getWidth()
                    : FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density;
            contentView.setTranslationX(width);
            contentView.animate()
                    .translationX(0f)
                    .setDuration(ANIM_DURATION_MS)
                    .withEndAction(() -> mIsAnimating = false)
                    .start();
        } else {
            float height = contentView.getHeight() > 0
                    ? contentView.getHeight()
                    : FALLBACK_PANEL_HEIGHT_DP * getResources().getDisplayMetrics().density;
            contentView.setTranslationY(height);
            contentView.animate()
                    .translationY(0f)
                    .setDuration(ANIM_DURATION_MS)
                    .withEndAction(() -> mIsAnimating = false)
                    .start();
        }
    }

    @Override
    public void gone() {
        if (!isShow() || mIsAnimating) {
            return;
        }
        mIsAnimating = true;

        View contentView = getActiveContentView();
        if (contentView == null) {
            setVisibility(View.GONE);
            mIsAnimating = false;
            return;
        }

        animate().alpha(0f).setDuration(ANIM_DURATION_MS).start();

        if (isLandscape()) {
            float width = contentView.getWidth() > 0
                    ? contentView.getWidth()
                    : FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density;
            contentView.animate()
                    .translationX(width)
                    .setDuration(ANIM_DURATION_MS)
                    .withEndAction(() -> {
                        setVisibility(View.GONE);
                        mIsAnimating = false;
                    })
                    .start();
        } else {
            float height = contentView.getHeight() > 0
                    ? contentView.getHeight()
                    : FALLBACK_PANEL_HEIGHT_DP * getResources().getDisplayMetrics().density;
            contentView.animate()
                    .translationY(height)
                    .setDuration(ANIM_DURATION_MS)
                    .withEndAction(() -> {
                        setVisibility(View.GONE);
                        mIsAnimating = false;
                    })
                    .start();
        }
    }

    /**
     * 切换设置菜单的可见性
     * <p>
     * 如果设置菜单当前可见，则将其隐藏。
     * </p>
     *
     * <p>
     * Toggle the visibility of the setting menu.
     * If the setting menu is currently visible, it will be hidden.
     * </p>
     */
    private void toggleVisibility() {
        if (isShow()) {
            gone();
        } else {
            show();
        }
    }

    // ---------------------------------------------------------------------
    // Data & Rendering
    // ---------------------------------------------------------------------

    /**
     * 初始化设置项
     * <p>
     * 根据场景类型和元素可见性配置过滤设置项。
     * </p>
     *
     * <p>
     * Initialize and filter setting items based on scene type and element visibility.
     * </p>
     */
    private void initSettingItems() {
        mItems.clear();
        mLandscapeItems.clear();
        for (SettingItem<?> item : SettingConstants.createDefaultItems(this)) {
            // 特定场景下隐藏倍速设置项
            if ((mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED)
                    && SettingConstants.KEY_SPEED.equals(item.key)) {
                continue;
            }
            // 直播场景下隐藏循环播放设置项
            if (mSceneType == SceneType.LIVE && SettingConstants.KEY_LOOP.equals(item.key)) {
                continue;
            }
            // 根据 hiddenSlotElements 配置隐藏对应元素
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

    /**
     * 根据场景类型更新设置项
     * <p>
     * 根据场景类型隐藏/显示设置项：
     * - LIVE 和 RESTRICTED 场景下隐藏倍速设置项
     * - LIVE 场景下隐藏循环播放设置项
     * </p>
     */
    private void updateItemsForScene() {
        initSettingItems();
        notifyAdapters();
    }

    /**
     * 通知适配器数据已变化
     */
    private void notifyAdapters() {
        if (mLandscapeAdapter != null) {
            mLandscapeAdapter.notifyDataSetChanged();
        }
        if (mPortraitAdapter != null) {
            mPortraitAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 单一数据源：从 {@link IPlayerStateStore} 同步 UI
     * <p>
     * 批量更新所有设置项数据后统一通知适配器刷新，避免多次无效刷新。
     * </p>
     *
     * <p>
     * Single source of truth: sync UI from {@link IPlayerStateStore}.
     * Batch-updates all item data, then notifies adapters once.
     * </p>
     */
    private void syncWithPlayerState() {
        IPlayerStateStore store = getHost() != null ? getHost().getPlayerStateStore() : null;
        if (store == null) {
            return;
        }

        // Track quality.
        List<TrackQuality> qualityList = store.getTrackQualityList();
        if (qualityList != null && !qualityList.isEmpty()) {
            applyClarityOptions(qualityList);
            applySelectedTrack(store.getCurrentTrackIndex());
        }

        // Common settings.
        applyItemValue(SettingConstants.KEY_SPEED, store.getCurrentSpeed());
        applyItemValue(SettingConstants.KEY_LOOP, store.isLoop());
        applyItemValue(SettingConstants.KEY_MUTE, store.isMute());
        applyItemValue(SettingConstants.KEY_SCALE, store.getCurrentScaleType());
        applyItemValue(SettingConstants.KEY_MIRROR, store.getCurrentMirrorType());
        applyItemValue(SettingConstants.KEY_ROTATE, store.getCurrentRotation());

        notifyAdapters();
    }

    /**
     * 更新设置项的值并通知适配器刷新（事件驱动调用）。
     *
     * @param key      设置项的键
     * @param newValue 要设置的新值
     */
    private void updateItemValue(@NonNull String key, @Nullable Object newValue) {
        if (applyItemValue(key, newValue)) {
            notifyAdapters();
        }
    }

    /**
     * 仅更新设置项数据，不通知适配器。
     *
     * @return true 如果数据被更新
     */
    @SuppressWarnings("unchecked")
    private boolean applyItemValue(@NonNull String key, @Nullable Object newValue) {
        if (newValue == null) {
            return false;
        }
        SettingItem<Object> item = findItem(key);
        if (item == null) {
            return false;
        }
        item.currentValue = newValue;
        return true;
    }

    /**
     * 更新清晰度选项并通知适配器刷新（事件驱动调用）。
     *
     * @param qualityList 轨道质量列表
     */
    private void updateClarityOptions(@Nullable List<TrackQuality> qualityList) {
        if (applyClarityOptions(qualityList)) {
            notifyAdapters();
        }
    }

    /**
     * 仅更新清晰度选项数据，不通知适配器。
     *
     * @return true 如果数据被更新
     */
    private boolean applyClarityOptions(@Nullable List<TrackQuality> qualityList) {
        if (qualityList == null || qualityList.isEmpty()) {
            return false;
        }

        SettingItem<TrackQuality> item = findItem(SettingConstants.KEY_QUALITY);
        if (item == null) {
            return false;
        }

        TrackQuality[] arr = qualityList.toArray(new TrackQuality[0]);
        item.options = SettingOptions.of(arr);

        if (item.currentValue == null) {
            item.currentValue = arr[0];
        }
        return true;
    }

    /**
     * 更新已选轨道并通知适配器刷新（事件驱动调用）。
     *
     * @param trackIndex 已选轨道的索引
     */
    private void updateSelectedTrack(int trackIndex) {
        if (applySelectedTrack(trackIndex)) {
            notifyAdapters();
        }
    }

    /**
     * 仅更新已选轨道数据，不通知适配器。
     *
     * @return true 如果数据被更新
     */
    private boolean applySelectedTrack(int trackIndex) {
        SettingItem<TrackQuality> item = findItem(SettingConstants.KEY_QUALITY);
        if (item == null) {
            return false;
        }

        SettingOptions<TrackQuality> options = item.options;
        if (options == null) {
            return false;
        }

        for (int i = 0; i < options.size(); i++) {
            TrackQuality q = options.get(i);
            if (q.getIndex() == trackIndex) {
                item.currentValue = q;
                return true;
            }
        }
        return false;
    }

    /**
     * 根据键查找设置项
     *
     * @param key 设置项的键
     * @param key item key
     * @return 找到的设置项，如果未找到则返回 null
     */
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

    /**
     * 将设置项的 key 映射为插槽元素 key
     *
     * @param itemKey 设置项的 key
     * @return 对应的插槽元素 key，如果没有映射则返回 null
     */
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
