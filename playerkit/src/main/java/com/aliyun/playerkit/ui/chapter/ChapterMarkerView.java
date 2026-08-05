package com.aliyun.playerkit.ui.chapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aliyun.playerkit.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 章节标记 View
 * <p>
 * 在进度条轨道上绘制章节分割标记点。
 * 每个标记点表示一个章节的起始位置，以竖线形式绘制。
 * 作为 SeekBar 的兄弟 View 叠加显示，通过 GONE/VISIBLE 控制显隐。
 * </p>
 * <p>
 * Chapter Marker View
 * <p>
 * Draws chapter separator markers on the progress bar track.
 * Each marker represents the start position of a chapter, drawn as a vertical line.
 * Placed as a sibling overlay of the SeekBar, controlled via GONE/VISIBLE.
 * </p>
 *
 * @author keria
 * @date 2026/07/07
 */
public class ChapterMarkerView extends View {

    /**
     * 标记点点击回调
     * <p>
     * Marker click listener
     * </p>
     */
    public interface OnMarkerClickListener {
        /**
         * 标记点被点击
         *
         * @param markerIndex        标记点索引
         * @param normalizedPosition 归一化位置 (0.0~1.0)
         */
        void onMarkerClicked(int markerIndex, float normalizedPosition);
    }

    private List<Float> mMarkerPositions = Collections.emptyList();
    private final Paint mPaint;
    @Nullable
    private OnMarkerClickListener mOnMarkerClickListener;
    private final int mTouchRadiusPx;

    public ChapterMarkerView(@NonNull Context context) {
        this(context, null);
    }

    public ChapterMarkerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChapterMarkerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(context, R.color.chapter_marker_color));
        mPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.chapter_marker_width));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mTouchRadiusPx = (int) context.getResources().getDimension(R.dimen.chapter_marker_touch_radius);
    }

    /**
     * 设置章节标记位置
     * <p>
     * Set chapter marker positions (normalized 0.0~1.0)
     * </p>
     *
     * @param positions 归一化位置列表
     */
    public void setMarkerPositions(@NonNull List<Float> positions) {
        mMarkerPositions = new ArrayList<>(positions);
        invalidate();
    }

    /**
     * 设置标记点点击回调
     * <p>
     * Set marker click listener
     * </p>
     */
    public void setOnMarkerClickListener(@Nullable OnMarkerClickListener listener) {
        mOnMarkerClickListener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mOnMarkerClickListener != null) {
            float touchX = event.getX();
            int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            if (contentWidth <= 0) return super.onTouchEvent(event);

            for (int i = 0; i < mMarkerPositions.size(); i++) {
                float position = mMarkerPositions.get(i);
                if (position <= 0f || position >= 1f) continue;
                float markerX = getPaddingLeft() + position * contentWidth;
                if (Math.abs(touchX - markerX) <= mTouchRadiusPx) {
                    mOnMarkerClickListener.onMarkerClicked(i, position);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mMarkerPositions.isEmpty()) return;

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int width = getWidth() - paddingLeft - paddingRight;
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        for (int i = 0; i < mMarkerPositions.size(); i++) {
            float position = mMarkerPositions.get(i);
            if (position <= 0f || position >= 1f) continue;
            float x = paddingLeft + position * width;
            canvas.drawLine(x, 0, x, height, mPaint);
        }
    }
}
