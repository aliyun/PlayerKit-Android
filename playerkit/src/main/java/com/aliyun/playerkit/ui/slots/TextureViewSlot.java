package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.slot.ISlot;
import com.aliyun.playerkit.slot.ISurfaceProvider;
import com.aliyun.playerkit.slot.SlotBehavior;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.logging.LogHub;

/**
 * 播放器 TextureView 插槽
 * <p>
 * 负责创建和管理播放器视频显示视图（TextureView）。
 * 此插槽是播放器视图的承载者，Controller 从此插槽获取视图并设置给播放器实例。
 * </p>
 * <p>
 * Player TextureView Slot
 * <p>
 * Responsible for creating and managing the player video display view (TextureView).
 * This slot is the carrier of the player view, and the Controller gets the view from this slot and sets it to the player instance.
 * </p>
 *
 * @author keria
 * @date 2025/11/23
 */
public class TextureViewSlot extends FrameLayout implements ISlot, ISurfaceProvider {

    private static final String TAG = "TextureViewSlot";

    /**
     * 插槽行为委托对象
     * <p>
     * 通过组合方式获得插槽的核心能力（生命周期管理、事件订阅）。
     * </p>
     */
    private final SlotBehavior slotBehavior = new SlotBehavior();

    /**
     * 插槽宿主
     * <p>
     * 用于在 Surface 回调中设置 Surface 到播放器。
     * </p>
     */
    @Nullable
    private SlotHost host;

    public TextureViewSlot(@NonNull Context context) {
        super(context);
        init();
    }

    public TextureViewSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextureViewSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化方法
     * <p>
     * 加载布局资源。
     * </p>
     */
    private void init() {
        View.inflate(getContext(), R.layout.layout_texture_view_slot, this);
    }

    /**
     * 插槽被附加到宿主时调用
     * <p>
     * 委托给 {@link SlotBehavior} 处理生命周期管理。
     * </p>
     *
     * @param host 插槽宿主，不能为 null
     */
    @Override
    public void onAttach(@NonNull SlotHost host) {
        LogHub.i(TAG, "onAttach");
        // 委托给 behavior 处理生命周期
        slotBehavior.attach(host);
        // 保存宿主引用
        this.host = host;
        // 设置 Surface 提供者
        setupSurfaceProvider(host);
    }

    @Override
    public void onDetach() {
        LogHub.i(TAG, "onDetach");
        if (host != null) {
            onSurfaceDestroyed(host);
        }
        // 执行解绑逻辑
        slotBehavior.detach();
        host = null;
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        // 空实现，不需要数据绑定
    }

    @Override
    public void onUnbindData() {
        // 空实现，不需要数据清理
    }

    @Override
    public void setupSurfaceProvider(@Nullable SlotHost host) {
        if (host == null) {
            return;
        }

        TextureView textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                LogHub.i(TAG, "Surface texture available: " + width + "x" + height);
                if (TextureViewSlot.this.host != null) {
                    onSurfaceAvailable(new Surface(surface), TextureViewSlot.this.host);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                LogHub.i(TAG, "Surface texture size changed: " + width + "x" + height);
                if (TextureViewSlot.this.host != null) {
                    onSurfaceChanged(TextureViewSlot.this.host);
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                LogHub.i(TAG, "Surface texture destroyed");
                if (TextureViewSlot.this.host != null) {
                    onSurfaceDestroyed(TextureViewSlot.this.host);
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
    }
}
