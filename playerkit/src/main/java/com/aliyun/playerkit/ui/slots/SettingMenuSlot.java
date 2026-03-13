package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.ui.setting.SettingConstants;
import com.aliyun.playerkit.ui.setting.SettingItem;
import com.aliyun.playerkit.ui.setting.SettingItemType;
import com.aliyun.playerkit.ui.setting.SettingOptions;
import com.aliyun.playerkit.ui.setting.ISettingItemView;
import com.aliyun.playerkit.ui.setting.SettingSelectorItemView;
import com.aliyun.playerkit.ui.setting.SettingSwitcherItemView;
import com.aliyun.playerkit.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设置菜单插槽。
 * <p>
 * 提供倍速、清晰度、循环播放等设置项，并与播放器状态保持同步。
 * </p>
 *
 * <p>
 * Settings menu slot.
 * Provides setting items (speed/quality/loop, etc.) and keeps them in sync with player state.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public class SettingMenuSlot extends BaseSlot {

    private static final String TAG = "SettingMenuSlot";

    // 显隐动画时长
    private static final long ANIM_DURATION_MS = 300L;
    // 默认面板宽度
    private static final float FALLBACK_PANEL_WIDTH_DP = 300f;

    // 菜单项
    private LinearLayout mLlItems;
    // 菜单内容
    private View mViewContent;

    // 设置项
    private final List<SettingItem<?>> mItems = new ArrayList<>();
    // 设置项视图
    private final Map<String, ISettingItemView<?>> mHolders = new HashMap<>();

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

        mLlItems = findViewById(R.id.ll_setting_items);
        mViewContent = findViewById(R.id.ll_menu_content);

        initSettingItems();
        renderItems();

        // 默认隐藏
        setVisibility(View.GONE);
        setAlpha(0f);

        // 点击背景隐藏
        findViewById(R.id.fl_root).setOnClickListener(v -> gone());

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
        mHolders.clear();

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

        final float width = resolvePanelWidth();
        mViewContent.setTranslationX(width);
        setAlpha(0f);

        ViewPropertyAnimator bgAnim = animate().alpha(1f).setDuration(ANIM_DURATION_MS);
        ViewPropertyAnimator panelAnim = mViewContent.animate().translationX(0f).setDuration(ANIM_DURATION_MS);

        bgAnim.start();
        panelAnim.withEndAction(() -> mIsAnimating = false).start();
    }

    @Override
    public void gone() {
        if (!isShow() || mIsAnimating) {
            return;
        }
        mIsAnimating = true;

        final float width = resolvePanelWidth();
        ViewPropertyAnimator bgAnim = animate().alpha(0f).setDuration(ANIM_DURATION_MS);
        ViewPropertyAnimator panelAnim = mViewContent.animate().translationX(width).setDuration(ANIM_DURATION_MS);

        bgAnim.start();
        panelAnim.withEndAction(() -> {
            setVisibility(View.GONE);
            mIsAnimating = false;
        }).start();
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

    /**
     * 解析设置菜单面板的宽度
     * <p>
     * 如果面板宽度已知，则返回该宽度。否则，将使用备用值。
     * </p>
     *
     * @return 设置菜单面板的宽度
     *
     * <p>
     * Resolve the width of the setting menu panel.
     * If the panel width is already known, it will be returned. Otherwise, a fallback value will be used.
     * </p>
     * @return The width of the setting menu panel.
     */
    private float resolvePanelWidth() {
        float width = mViewContent.getWidth();
        if (width > 0) {
            return width;
        }
        return FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density;
    }

    // ---------------------------------------------------------------------
    // Data & Rendering
    // ---------------------------------------------------------------------

    /**
     * 初始化设置项
     *
     * <p>
     * Initialize the setting items.
     * </p>
     */
    private void initSettingItems() {
        mItems.clear();
        mItems.addAll(SettingConstants.createDefaultItems(getContext(), this));
    }

    /**
     * 渲染设置项
     *
     * <p>
     * Render the setting items.
     * </p>
     */
    private void renderItems() {
        mLlItems.removeAllViews();
        mHolders.clear();

        for (SettingItem<?> item : mItems) {
            // 特定场景下隐藏倍速设置项
            if ((mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED) && SettingConstants.KEY_SPEED.equals(item.key)) {
                continue;
            }
            // 直播场景下隐藏循环播放设置项
            if (mSceneType == SceneType.LIVE && SettingConstants.KEY_LOOP.equals(item.key)) {
                continue;
            }

            ISettingItemView<?> itemView = createItemView(item);
            if (itemView == null) {
                continue;
            }
            mHolders.put(item.key, itemView);
            mLlItems.addView(itemView.getView());
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
        // 如果已经渲染过，需要重新渲染以应用场景限制
        if (!mHolders.isEmpty()) {
            renderItems();
        }
    }

    /**
     * 为指定的设置项创建视图
     *
     * @param item 要创建视图的设置项
     * @param item The setting item to create a view for.
     * @return 创建的设置项视图，如果设置项类型不支持则返回 null
     *
     * <p>
     * Create a setting item view for the given setting item.
     * </p>
     * @return The created setting item view, or null if the item type is not supported.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private ISettingItemView<?> createItemView(@NonNull SettingItem<?> item) {
        if (item.type == SettingItemType.SELECTOR) {
            SettingSelectorItemView view = new SettingSelectorItemView<>(getContext());
            view.bind(item);
            return view;
        } else if (item.type == SettingItemType.SWITCHER) {
            SettingSwitcherItemView view = new SettingSwitcherItemView(getContext());
            view.bind((SettingItem<Boolean>) item);
            return view;
        } else {
            LogHub.e(TAG, "Unsupported item type: " + item.type);
            return null;
        }
    }

    /**
     * 单一数据源：从 {@link IPlayerStateStore} 同步 UI
     *
     * <p>
     * Single source of truth: sync UI from {@link IPlayerStateStore}.
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
            updateClarityOptions(qualityList);
            updateSelectedTrack(store.getCurrentTrackIndex());
        }

        // Common settings.
        updateItemValue(SettingConstants.KEY_SPEED, store.getCurrentSpeed());
        updateItemValue(SettingConstants.KEY_LOOP, store.isLoop());
        updateItemValue(SettingConstants.KEY_MUTE, store.isMute());
        updateItemValue(SettingConstants.KEY_SCALE, store.getCurrentScaleType());
        updateItemValue(SettingConstants.KEY_MIRROR, store.getCurrentMirrorType());
        updateItemValue(SettingConstants.KEY_ROTATE, store.getCurrentRotation());
    }

    /**
     * 更新设置项的值
     *
     * @param key      设置项的键
     * @param newValue 要设置的新值
     */
    private void updateItemValue(@NonNull String key, @Nullable Object newValue) {
        if (newValue == null) {
            return;
        }
        ISettingItemView<?> view = mHolders.get(key);
        if (view == null) {
            return;
        }

        // Safe by construction: keys are bound to item types in SettingConstants#createDefaultItems.
        @SuppressWarnings("unchecked") ISettingItemView<Object> v = (ISettingItemView<Object>) view;
        v.updateValueOnly(newValue);
    }

    /**
     * 更新设置项的清晰度选项
     *
     * @param qualityList 轨道质量列表
     */
    private void updateClarityOptions(@Nullable List<TrackQuality> qualityList) {
        if (qualityList == null || qualityList.isEmpty()) {
            return;
        }

        ISettingItemView<?> view = mHolders.get(SettingConstants.KEY_QUALITY);
        if (!(view instanceof SettingSelectorItemView)) {
            return;
        }

        @SuppressWarnings("unchecked")
        SettingSelectorItemView<TrackQuality> selector = (SettingSelectorItemView<TrackQuality>) view;

        SettingItem<TrackQuality> item = findItem(SettingConstants.KEY_QUALITY);
        if (item == null) return;

        TrackQuality[] arr = qualityList.toArray(new TrackQuality[0]);
        item.options = SettingOptions.of(arr);

        // Only set default selection when current is empty.
        if (item.currentValue == null) {
            selector.updateValueOnly(arr[0]);
        } else {
            // Refresh value text if options changed but selection remains.
            selector.updateValueOnly(item.currentValue);
        }
    }

    /**
     * 更新设置项的已选轨道。
     *
     * @param trackIndex 已选轨道的索引
     */
    private void updateSelectedTrack(int trackIndex) {
        SettingItem<TrackQuality> item = findItem(SettingConstants.KEY_QUALITY);
        if (item == null) return;

        ISettingItemView<?> view = mHolders.get(SettingConstants.KEY_QUALITY);
        if (view == null) return;

        SettingOptions<TrackQuality> options = item.options;
        if (options == null) {
            return;
        }

        for (int i = 0; i < options.size(); i++) {
            TrackQuality q = options.get(i);
            if (q.getIndex() == trackIndex) {
                ((ISettingItemView<TrackQuality>) view).updateValueOnly(q);
                return;
            }
        }
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
}
