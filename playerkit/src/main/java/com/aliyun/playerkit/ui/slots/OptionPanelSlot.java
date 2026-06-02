package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.core.IPlayerStateStore;
import com.aliyun.playerkit.data.TrackQuality;
import com.aliyun.playerkit.event.ControlBarEvents;
import com.aliyun.playerkit.event.PlayerCommand;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.ui.setting.SettingConstants;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.TrackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 选项面板插槽
 * <p>
 * 横屏场景下用于倍速、清晰度选择的右侧独立面板。
 * 使用 RecyclerView 展示选项列表，选中项高亮，点击后执行对应操作并自动关闭。
 * </p>
 *
 * @author junhuiYe
 * @date 2026/05/28
 */
public class OptionPanelSlot extends BaseSlot {

    private static final long ANIM_DURATION_MS = 250L;

    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            ControlBarEvents.ShowSpeedPanel.class,
            ControlBarEvents.ShowQualityPanel.class,
            ControlBarEvents.Show.class
    );

    @Nullable
    private String mPlayerId;

    private RecyclerView mRecyclerView;
    private final List<String> mLabels = new ArrayList<>();
    private OptionPanelAdapter mAdapter;

    private boolean mIsAnimating;

    public OptionPanelSlot(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_option_panel_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        setOnClickListener(v -> hidePanel());

        mRecyclerView = findViewById(R.id.rv_option_list);
        mAdapter = new OptionPanelAdapter(mLabels, getContext());
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(mAdapter);
        }

        setVisibility(View.GONE);
        setAlpha(0f);
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);
        mPlayerId = getPlayerId();
    }

    @Override
    public void onUnbindData() {
        mPlayerId = null;
        mIsAnimating = false;
        mLabels.clear();
        super.onUnbindData();
    }

    @Override
    public void onDetach() {
        mPlayerId = null;
        mIsAnimating = false;
        super.onDetach();
    }

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

        if (event instanceof ControlBarEvents.ShowSpeedPanel) {
            showSpeedPanel();
        } else if (event instanceof ControlBarEvents.ShowQualityPanel) {
            showQualityPanel();
        } else if (event instanceof ControlBarEvents.Show) {
            hidePanel();
        }
    }

    // ----- Speed Panel -----

    private void showSpeedPanel() {
        SlotHost host = getHost();
        if (host == null) return;

        IPlayerStateStore store = host.getPlayerStateStore();
        float currentSpeed = store.getCurrentSpeed();

        mLabels.clear();
        List<Float> speedOptions = SettingConstants.SPEED_OPTIONS;
        int selectedIndex = -1;

        for (int i = 0; i < speedOptions.size(); i++) {
            float speed = speedOptions.get(i);
            mLabels.add(speed + "x");
            if (Float.compare(speed, currentSpeed) == 0) {
                selectedIndex = i;
            }
        }

        mAdapter.setSelectedIndex(selectedIndex);
        mAdapter.setOnItemClickListener(position -> {
            float selected = speedOptions.get(position);
            if (mPlayerId != null) {
                postEvent(new PlayerCommand.SetSpeed(mPlayerId, selected));
            }
            hidePanel();
        });
        mAdapter.notifyDataSetChanged();

        showPanel();
    }

    // ----- Quality Panel -----

    private void showQualityPanel() {
        SlotHost host = getHost();
        if (host == null) return;

        IPlayerStateStore store = host.getPlayerStateStore();
        List<TrackQuality> qualityList = store.getTrackQualityList();
        if (qualityList == null || qualityList.isEmpty()) return;

        int currentIndex = store.getCurrentTrackIndex();

        mLabels.clear();
        int selectedIndex = -1;

        for (int i = 0; i < qualityList.size(); i++) {
            TrackQuality quality = qualityList.get(i);
            mLabels.add(TrackUtil.findNearestResolution(quality));
            if (quality != null && quality.getIndex() == currentIndex) {
                selectedIndex = i;
            }
        }

        mAdapter.setSelectedIndex(selectedIndex);
        mAdapter.setOnItemClickListener(position -> {
            TrackQuality selected = qualityList.get(position);
            if (mPlayerId != null) {
                postEvent(new PlayerCommand.SelectTrack(mPlayerId, selected));
            }
            hidePanel();
        });
        mAdapter.notifyDataSetChanged();

        showPanel();
    }

    // ----- Show / Hide -----

    private void showPanel() {
        if (isShow() || mIsAnimating) return;
        mIsAnimating = true;

        bringToFront();
        setVisibility(View.VISIBLE);
        setAlpha(0f);
        animate()
                .alpha(1f)
                .setDuration(ANIM_DURATION_MS)
                .withEndAction(() -> mIsAnimating = false)
                .start();
    }

    private void hidePanel() {
        if (!isShow() || mIsAnimating) return;
        mIsAnimating = true;

        animate()
                .alpha(0f)
                .setDuration(ANIM_DURATION_MS)
                .withEndAction(() -> {
                    setVisibility(View.GONE);
                    mIsAnimating = false;
                })
                .start();
    }
}
