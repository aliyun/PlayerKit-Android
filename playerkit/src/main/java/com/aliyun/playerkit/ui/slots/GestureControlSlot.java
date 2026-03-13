package com.aliyun.playerkit.ui.slots;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.event.GestureEvents;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.utils.BrightnessUtil;
import com.aliyun.playerkit.utils.ContextUtil;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.VibrationUtil;
import com.aliyun.playerkit.utils.VolumeUtil;

/**
 * 手势控制插槽
 * <p>
 * 负责检测播放器的所有手势并发送相应的手势事件。
 * 该插槽只负责手势检测，不包含任何业务逻辑。
 * </p>
 * <p>
 * Gesture Control Slot
 * <p>
 * Responsible for detecting all player gestures and sending corresponding gesture events.
 * This slot only handles gesture detection without any business logic.
 * </p>
 *
 * @author keria
 * @date 2025/12/09
 */
public class GestureControlSlot extends BaseSlot {

    private static final String TAG = "GestureControlSlot";

    // ==================== 常量定义 ====================

    /**
     * 手势状态类型定义
     * <p>
     * 定义了手势控制插槽支持的所有手势状态类型。
     * </p>
     * <p>
     * Gesture State Type Definition
     * <p>
     * Defines all gesture state types supported by the gesture control slot.
     * </p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            GestureState.NONE,
            GestureState.HORIZONTAL,
            GestureState.VERTICAL_LEFT,
            GestureState.VERTICAL_RIGHT,
            GestureState.LONG_PRESSING,
    })
    @interface GestureState {
        /**
         * 无手势
         */
        int NONE = 0;
        /**
         * 水平拖动
         */
        int HORIZONTAL = 1;
        /**
         * 左侧垂直拖动
         */
        int VERTICAL_LEFT = 2;
        /**
         * 右侧垂直拖动
         */
        int VERTICAL_RIGHT = 3;
        /**
         * 长按
         */
        int LONG_PRESSING = 4;
    }

    // 判断垂直拖动的最小距离（像素）
    private static final int MIN_VERTICAL_DISTANCE = 20;
    // 判断水平拖动的最小距离（像素）
    private static final int MIN_HORIZONTAL_DISTANCE = 20;

    /**
     * 更新事件触发的最小百分比阈值 (1%)
     * 避免微小变动频繁触发回调和日志
     */
    private static final float MIN_UPDATE_PERCENT = 0.01f;

    // 亮度调整灵敏度：垂直拖动全屏对应 1.0 的变化，可以调节此倍率修改灵敏度
    private static final float BRIGHTNESS_SENSITIVITY = 1.0f;
    // 音量调整灵敏度：垂直拖动全屏对应 1.0 的变化，可以调节此倍率修改灵敏度
    private static final float VOLUME_SENSITIVITY = 1.0f;

    // ==================== 成员变量 ====================

    // 手势检测器
    private GestureDetector mGestureDetector;

    // 拖动起始点 X 坐标
    private float mDragStartX = 0;
    // 拖动起始点 Y 坐标
    private float mDragStartY = 0;

    // 水平拖动追踪器
    private final DragTracker mHorizontalTracker = new DragTracker();
    // 垂直拖动追踪器
    private final DragTracker mVerticalTracker = new DragTracker();

    // 当前手势状态
    @GestureState
    private int mGestureState = GestureState.NONE;

    // 播放器 ID（在 onBindData 时存储，避免频繁调用 getPlayerId()）
    @Nullable
    private String mPlayerId;

    // 当前场景类型（在 onBindData 时存储，用于判断是否允许手势）
    @SceneType
    private int mSceneType = SceneType.VOD;

    // 当前亮度（0.0 - 1.0）
    private float mCurrentBrightness = 0.0f;

    // 当前音量百分比（0.0 - 1.0）
    private float mCurrentVolumePercent = 0.0f;

    // ==================== 构造函数 ====================

    public GestureControlSlot(@NonNull Context context) {
        super(context);
        init();
    }

    public GestureControlSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GestureControlSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化手势检测器
     * <p>
     * 创建 GestureDetector 实例，用于检测点击类手势（单击、双击、长按）。
     * 拖动类手势通过自定义逻辑在 onTouchEvent 中处理。
     * </p>
     * <p>
     * Initialize Gesture Detector
     * <p>
     * Creates a GestureDetector instance for detecting tap gestures (single tap, double tap, long press).
     * Drag gestures are handled by custom logic in onTouchEvent.
     * </p>
     */
    private void init() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    // ==================== 生命周期 ====================

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);
    }

    @Override
    public void onDetach() {
        // 清理资源
        mGestureDetector = null;

        super.onDetach();
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);

        // 存储播放器 ID，避免频繁调用 getPlayerId()
        mPlayerId = getPlayerId();
        // 存储场景类型，用于判断是否允许手势
        mSceneType = model.getSceneType();
    }

    @Override
    public void onUnbindData() {
        // 清理播放器 ID
        mPlayerId = null;
        // 重置场景类型
        mSceneType = SceneType.VOD;

        mDragStartX = 0;
        mDragStartY = 0;

        mGestureState = GestureState.NONE;

        mCurrentBrightness = 0.0f;
        mCurrentVolumePercent = 0.0f;

        super.onUnbindData();
    }

    // ==================== 触摸事件处理 ====================

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MINIMAL 场景下禁用所有手势
        if (mSceneType == SceneType.MINIMAL) {
            return false;
        }

        // 先交给手势检测器处理
        mGestureDetector.onTouchEvent(event);

        // 处理手势检测器未处理的事件
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleActionUp(event);
                break;
        }

        // 始终返回 true，表示消费所有触摸事件
        return true;
    }

    /**
     * 处理 ACTION_DOWN 事件
     * <p>
     * 记录触摸起始位置，初始化拖动追踪器，重置手势状态。
     * 这是所有手势识别的起点。
     * </p>
     * <p>
     * Handle ACTION_DOWN Event
     * <p>
     * Records touch start position, initializes drag trackers, and resets gesture state.
     * This is the starting point for all gesture recognition.
     * </p>
     *
     * @param event 触摸事件，包含触摸位置信息
     */
    private void handleActionDown(MotionEvent event) {
        mDragStartX = event.getX();
        mDragStartY = event.getY();

        mHorizontalTracker.start(mDragStartX);
        mVerticalTracker.start(mDragStartY);

        mGestureState = GestureState.NONE;
    }

    /**
     * 处理 ACTION_MOVE 事件
     * <p>
     * 当手势状态为 NONE 时，尝试识别拖动开始（水平或垂直）。
     * 如果已经识别出手势类型，则更新对应的拖动进度。
     * </p>
     * <p>
     * Handle ACTION_MOVE Event
     * <p>
     * When gesture state is NONE, attempts to recognize drag start (horizontal or vertical).
     * If a gesture type has been recognized, updates the corresponding drag progress.
     * </p>
     *
     * @param event 触摸事件，包含当前触摸位置信息
     */
    private void handleActionMove(MotionEvent event) {
        if (mGestureState == GestureState.NONE) {
            tryStartDrag(event);
        }

        switch (mGestureState) {
            case GestureState.HORIZONTAL:
                handleHorizontalDrag(event.getX());
                break;
            case GestureState.VERTICAL_LEFT:
            case GestureState.VERTICAL_RIGHT:
                handleVerticalDrag(event.getY());
                break;
            default:
                break;
        }
    }

    /**
     * 尝试识别并开始拖动
     * <p>
     * 根据触摸移动的距离判断是否为拖动操作。
     * 识别成功后，更新手势状态并发送对应的拖动开始事件。
     * </p>
     * <p>
     * Try to Recognize and Start Drag
     * <p>
     * Determines if it's a drag operation based on touch movement distance.
     * After recognition, updates gesture state and sends corresponding drag start event.
     * </p>
     *
     * @param event 触摸事件，包含当前触摸位置信息
     */
    private void tryStartDrag(MotionEvent event) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        float deltaX = event.getX() - mDragStartX;
        float deltaY = event.getY() - mDragStartY;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);

        // 优先判断移动距离更大的方向，避免误判
        if (absDeltaX > MIN_HORIZONTAL_DISTANCE && absDeltaX > absDeltaY) {
            // 特定场景下禁用水平拖动（进度拖拽）
            if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED || mSceneType == SceneType.MINIMAL) {
                return;
            }

            mGestureState = GestureState.HORIZONTAL;
            LogHub.i(TAG, "Start horizontal dragging");

            // 拖动开始时触发震动
            VibrationUtil.vibrateLight(getContext());

            postGestureEvent(new GestureEvents.HorizontalDragStartEvent(mPlayerId, mDragStartX, mDragStartY));
        } else if (absDeltaY > MIN_VERTICAL_DISTANCE && absDeltaY > absDeltaX) {
            // 列表播放场景下禁用垂直手势（音量/亮度调节），避免与列表滚动手势冲突
            if (mSceneType == SceneType.VIDEO_LIST) {
                return;
            }

            boolean isLeftSide = mDragStartX < getWidth() / 2.0f;
            mGestureState = isLeftSide ? GestureState.VERTICAL_LEFT : GestureState.VERTICAL_RIGHT;
            LogHub.i(TAG, "Start vertical dragging on " + (isLeftSide ? "left" : "right") + " side");

            // 拖动开始时触发震动
            VibrationUtil.vibrateLight(getContext());

            if (isLeftSide) {
                // 左侧调整亮度：初始化当前亮度
                Activity activity = ContextUtil.getActivity(getContext());
                if (activity != null) {
                    mCurrentBrightness = BrightnessUtil.getWindowBrightnessPercentage(activity);
                } else {
                    mCurrentBrightness = 0.5f; // 默认为中等亮度
                }
                postGestureEvent(new GestureEvents.LeftVerticalDragStartEvent(mPlayerId, mDragStartX, mDragStartY));
            } else {
                // 右侧调整音量：初始化当前音量
                mCurrentVolumePercent = VolumeUtil.getVolumePercentage(getContext());
                postGestureEvent(new GestureEvents.RightVerticalDragStartEvent(mPlayerId, mDragStartX, mDragStartY));
            }
        }
    }

    /**
     * 处理 ACTION_UP 事件
     * <p>
     * 当手指抬起或手势被取消时调用。
     * 根据当前手势状态发送对应的结束事件，然后重置手势状态。
     * </p>
     * <p>
     * Handle ACTION_UP Event
     * <p>
     * Called when finger is lifted or gesture is cancelled.
     * Sends corresponding end event based on current gesture state, then resets gesture state.
     * </p>
     *
     * @param event 触摸事件，可能为 ACTION_UP 或 ACTION_CANCEL
     */
    private void handleActionUp(MotionEvent event) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        // 根据手势状态发送结束事件
        switch (mGestureState) {
            case GestureState.HORIZONTAL:
                LogHub.i(TAG, "Horizontal drag ended");
                postGestureEvent(new GestureEvents.HorizontalDragEndEvent(mPlayerId));
                break;
            case GestureState.VERTICAL_LEFT:
                LogHub.i(TAG, "Left vertical drag ended");
                postGestureEvent(new GestureEvents.LeftVerticalDragEndEvent(mPlayerId));
                break;
            case GestureState.VERTICAL_RIGHT:
                LogHub.i(TAG, "Right vertical drag ended");
                postGestureEvent(new GestureEvents.RightVerticalDragEndEvent(mPlayerId));
                break;
            case GestureState.LONG_PRESSING:
                LogHub.i(TAG, "Long press ended");

                // 长按结束时触发震动
                VibrationUtil.vibrateLight(getContext());

                // 长按结束：恢复速度
                setSpeed(1.0f);

                // 发送长按结束事件
                postGestureEvent(new GestureEvents.LongPressEndEvent(mPlayerId));
                break;
        }

        // 重置状态
        mGestureState = GestureState.NONE;
    }

    /**
     * 处理水平拖动更新
     * <p>
     * 计算水平拖动的增量百分比（相对于 View 宽度），
     * 只有当变化超过阈值时才发送更新事件，避免频繁触发。
     * </p>
     * <p>
     * Handle Horizontal Drag Update
     * <p>
     * Calculates horizontal drag delta percentage (relative to View width),
     * only sends update event when change exceeds threshold to avoid frequent triggering.
     * </p>
     *
     * @param currentX 当前触摸点的 X 坐标
     */
    private void handleHorizontalDrag(float currentX) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        float deltaPercent = mHorizontalTracker.update(currentX, getWidth());
        if (deltaPercent != 0) {
            postGestureEvent(new GestureEvents.HorizontalDragUpdateEvent(mPlayerId, deltaPercent));
        }
    }

    /**
     * 处理垂直拖动更新
     * <p>
     * 计算垂直拖动的增量百分比（相对于 View 高度），
     * 根据手势状态（左侧或右侧）发送对应的更新事件。
     * 只有当变化超过阈值时才发送更新事件。
     * </p>
     * <p>
     * Handle Vertical Drag Update
     * <p>
     * Calculates vertical drag delta percentage (relative to View height),
     * sends corresponding update event based on gesture state (left or right side).
     * Only sends update event when change exceeds threshold.
     * </p>
     *
     * @param currentY 当前触摸点的 Y 坐标
     */
    private void handleVerticalDrag(float currentY) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        float deltaPercent = mVerticalTracker.update(currentY, getHeight());
        if (deltaPercent != 0) {
            if (mGestureState == GestureState.VERTICAL_LEFT) {
                // 亮度调整：上滑减少 y，deltaPercent 为负；上滑应增加亮度 -> 减去 deltaPercent (负数)
                // 注意：DragTracker 计算的 delta = current - last。
                // 上滑时 current < last, delta < 0, percent < 0。
                // 我们希望上滑增加亮度，所以应该是 current - deltaPercent * sensitivity
                // 或者是: new = current + (-percent * sensitivity)

                float change = -deltaPercent * BRIGHTNESS_SENSITIVITY;
                mCurrentBrightness += change;
                mCurrentBrightness = Math.max(0.0f, Math.min(1.0f, mCurrentBrightness));

                // 左侧垂直手势：上滑增加亮度，下滑减少亮度。
                BrightnessUtil.setWindowBrightness(getContext(), mCurrentBrightness);

                // 发送亮度更新事件
                postGestureEvent(new GestureEvents.LeftVerticalDragUpdateEvent(mPlayerId, deltaPercent, mCurrentBrightness));
            } else if (mGestureState == GestureState.VERTICAL_RIGHT) {
                // 音量调整
                float change = -deltaPercent * VOLUME_SENSITIVITY;
                mCurrentVolumePercent += change;
                mCurrentVolumePercent = Math.max(0.0f, Math.min(1.0f, mCurrentVolumePercent));

                // 右侧垂直手势：上滑增加音量，下滑减少音量。
                VolumeUtil.setVolumePercentage(getContext(), mCurrentVolumePercent);

                // 发送音量更新事件
                postGestureEvent(new GestureEvents.RightVerticalDragUpdateEvent(mPlayerId, deltaPercent, mCurrentVolumePercent));
            }
        }
    }

    // ==================== 事件发送 ====================

    /**
     * 发送手势事件并记录日志
     * <p>
     * 统一的事件发送入口，所有手势事件都通过此方法发送。
     * 会自动记录事件信息，方便调试和追踪。
     * </p>
     * <p>
     * Post Gesture Event and Log
     * <p>
     * Unified event posting entry point, all gesture events are sent through this method.
     * Automatically logs event information for debugging and tracking.
     * </p>
     *
     * @param event 手势事件，不能为 null
     */
    private void postGestureEvent(@NonNull PlayerEvent event) {
        LogHub.i(TAG, "Post gesture event: " + event);
        postEvent(event);
    }

    // ==================== 手势监听器 ====================

    /**
     * 手势监听器
     * <p>
     * 继承自 GestureDetector.SimpleOnGestureListener，用于处理点击类手势。
     * 包括单击、双击和长按事件的检测。
     * </p>
     * <p>
     * Gesture Listener
     * <p>
     * Extends GestureDetector.SimpleOnGestureListener for handling tap gestures.
     * Includes detection of single tap, double tap, and long press events.
     * </p>
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // 单击事件
            handleSingleTap(e);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 双击事件
            handleDoubleTap(e);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // 长按事件
            handleLongPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // 必须返回 true，否则后续事件不会被处理
            return true;
        }
    }

    /**
     * 处理单击事件
     * <p>
     * 当用户单击屏幕时触发，发送单击事件到事件总线。
     * 业务逻辑由事件订阅者（如 AliPlayerController）处理。
     * </p>
     * <p>
     * Handle Single Tap Event
     * <p>
     * Triggered when user taps the screen once, sends single tap event to event bus.
     * Business logic is handled by event subscribers (such as AliPlayerController).
     * </p>
     *
     * @param e 触摸事件，包含点击位置信息
     */
    private void handleSingleTap(MotionEvent e) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        LogHub.i(TAG, "Single tap detected");
        postGestureEvent(new GestureEvents.SingleTapEvent(mPlayerId, e.getX(), e.getY()));
    }

    /**
     * 处理双击事件
     * <p>
     * 当用户双击屏幕时触发，发送双击事件到事件总线。
     * 业务逻辑由事件订阅者处理（通常用于切换播放/暂停）。
     * </p>
     * <p>
     * Handle Double Tap Event
     * <p>
     * Triggered when user double-taps the screen, sends double tap event to event bus.
     * Business logic is handled by event subscribers (typically used for toggling play/pause).
     * </p>
     *
     * @param e 触摸事件，包含点击位置信息
     */
    private void handleDoubleTap(MotionEvent e) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        LogHub.i(TAG, "Double tap detected");

        // 切换播放/暂停
        toggle();

        // 发送双击事件
        postGestureEvent(new GestureEvents.DoubleTapEvent(mPlayerId, e.getX(), e.getY()));
    }

    /**
     * 处理长按事件
     * <p>
     * 当用户长按屏幕时触发。如果当前已有其他手势（如拖动），则不响应长按。
     * 长按识别成功后，更新手势状态，触发震动反馈，并发送长按事件到事件总线。
     * 业务逻辑由事件订阅者处理（通常用于倍速播放）。
     * </p>
     * <p>
     * Handle Long Press Event
     * <p>
     * Triggered when user long-presses the screen. If there's already another gesture (such as drag),
     * long press is ignored.
     * After successful recognition, updates gesture state, triggers vibration feedback,
     * and sends long press event to event bus.
     * Business logic is handled by event subscribers (typically used for speed playback).
     * </p>
     *
     * @param e 触摸事件，包含长按位置信息
     */
    private void handleLongPress(MotionEvent e) {
        // 播放器 ID 为空时，不处理
        if (StringUtil.isEmpty(mPlayerId)) {
            return;
        }

        // 特定场景下禁用长按（倍速播放）
        if (mSceneType == SceneType.LIVE || mSceneType == SceneType.RESTRICTED || mSceneType == SceneType.MINIMAL) {
            return;
        }

        // 如果当前已经有其他手势（如拖动），则不响应长按
        if (mGestureState != GestureState.NONE) {
            return;
        }

        LogHub.i(TAG, "Long press detected");

        mGestureState = GestureState.LONG_PRESSING;

        // 长按时触发震动
        VibrationUtil.vibrateMedium(getContext());

        // 长按：2倍速
        setSpeed(2.0f);

        postGestureEvent(new GestureEvents.LongPressEvent(mPlayerId, e.getX(), e.getY()));
    }

    /**
     * 拖动追踪器帮助类
     * <p>
     * 负责处理拖动过程中的位置追踪、增量计算和阈值过滤。
     * 通过累积增量并转换为百分比，避免频繁触发更新事件，提高性能。
     * </p>
     * <p>
     * Drag Tracker Helper Class
     * <p>
     * Handles position tracking, delta calculation, and threshold filtering during drag operations.
     * Converts accumulated deltas to percentages to avoid frequent update events and improve performance.
     * </p>
     */
    private static class DragTracker {
        /**
         * 上一次的位置
         */
        private float lastPos;

        /**
         * 累积的增量
         * <p>
         * 用于累积多次移动的增量，只有当累积量超过阈值时才上报。
         * </p>
         */
        private float accumulatedDelta;

        /**
         * 开始追踪
         * <p>
         * 初始化追踪器，记录起始位置，重置累积增量。
         * </p>
         * <p>
         * Start Tracking
         * <p>
         * Initializes tracker, records start position, resets accumulated delta.
         * </p>
         *
         * @param startPos 起始位置坐标
         */
        void start(float startPos) {
            lastPos = startPos;
            accumulatedDelta = 0;
        }

        /**
         * 更新追踪位置并计算增量百分比
         * <p>
         * 计算当前位置与上次位置的差值，累积增量。
         * 将累积增量转换为相对于总尺寸的百分比。
         * 只有当百分比变化超过阈值（MIN_UPDATE_PERCENT）时才返回非零值。
         * </p>
         * <p>
         * Update Tracking Position and Calculate Delta Percentage
         * <p>
         * Calculates difference between current and last position, accumulates delta.
         * Converts accumulated delta to percentage relative to total size.
         * Only returns non-zero value when percentage change exceeds threshold (MIN_UPDATE_PERCENT).
         * </p>
         *
         * @param currentPos 当前位置坐标
         * @param totalSize  总尺寸（宽度或高度），用于计算百分比
         * @return 增量百分比（0.0 - 1.0），如果变化未超过阈值则返回 0
         */
        float update(float currentPos, int totalSize) {
            if (totalSize <= 0) return 0;

            float delta = currentPos - lastPos;
            lastPos = currentPos;
            accumulatedDelta += delta;

            float percent = accumulatedDelta / totalSize;
            // 只有当变化超过阈值时才上报
            if (Math.abs(percent) >= MIN_UPDATE_PERCENT) {
                accumulatedDelta = 0; // 上报后重置累积量
                return percent;
            }
            return 0;
        }
    }
}
