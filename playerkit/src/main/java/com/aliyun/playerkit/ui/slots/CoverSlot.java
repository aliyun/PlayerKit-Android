package com.aliyun.playerkit.ui.slots;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.R;
import com.aliyun.playerkit.converter.PlayerTypeConverter;
import com.aliyun.playerkit.event.PlayerEvent;
import com.aliyun.playerkit.event.PlayerEvents;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.player.IMediaPlayer;
import com.aliyun.playerkit.slot.BaseSlot;
import com.aliyun.playerkit.slot.SlotHost;
import com.aliyun.playerkit.utils.StringUtil;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

/**
 * 封面图插槽
 * <p>
 * 负责显示视频封面图，覆盖在播放器渲染视图之上，作为首帧渲染前的兜底画面。
 * 首帧渲染完成后立即隐藏（无动画），实现封面到视频画面的无缝切换，不产生黑屏或闪烁。
 * </p>
 *
 * <h3>封面图的两种来源</h3>
 * <p>
 * <b>来源一：外部赋值（{@link AliPlayerModel#getCoverUrl()}）</b>：绑定这一刻地址就已就绪。
 * 再配合图片预加载（提前把封面下进本地缓存，如 Glide 的 {@code preload()} / {@code downloadOnly()}），
 * 封面几乎与页面同时出现，从加载到渲染全程没有黑屏，秒开效果最好。
 * </p>
 * <p>
 * <b>来源二：SDK 自动提取（Vid 播放方式，{@link PlayerEvents.CoverUrlReady}）</b>：
 * SDK 在 prepare 完成后从 MediaInfo 拿到地址再回调。接入方只传一个 Vid，不用另外维护封面地址，
 * 且 Vid 比明文 URL 更安全，是极致 All-in-one 的用法。代价是取地址这一步天然后置，
 * 从绑定到封面出现之间<b>仍可能有一段短暂黑屏 —— 这是该来源的固有时序，不是缺陷</b>。
 * </p>
 * <p>
 * 两者同时存在时以外部赋值为准。追求秒开选来源一，追求接入简单选来源二。
 * </p>
 *
 * <h3>使用前提</h3>
 * <p>
 * <b>封面图的宽高比需与视频画面一致</b>（服务端通常同源生成）。基于这个前提，
 * {@link ImageView.ScaleType} 算出的矩形与播放器渲染填充模式逐像素一致，无需任何手工计算。
 * 若宽高比不一致，封面与画面的边界无法精确重合，切换时仍会有轻微跳动。
 * </p>
 *
 * <p>
 * Cover Image Slot
 * <p>
 * Displays the video cover image on top of the player rendering view as a fallback before the first
 * frame is rendered. Hides immediately (without animation) once the first frame arrives, so the
 * transition from cover to video introduces neither a black screen nor a flicker.
 * </p>
 * <p>
 * Two cover sources are supported: an externally assigned {@link AliPlayerModel#getCoverUrl()}
 * (ready at bind time, best for instant playback) and the URL extracted by the SDK from MediaInfo
 * during Vid playback (simplest integration, but inherently arrives after prepare). The external
 * one wins when both are present. The cover aspect ratio must match the video aspect ratio.
 * </p>
 *
 * @author keria
 * @date 2025/12/08
 */
public class CoverSlot extends BaseSlot {

    private static final String TAG = "CoverSlot";

    /**
     * 本插槽需要订阅的事件类型列表（静态常量，避免重复创建）
     */
    private static final List<Class<? extends PlayerEvent>> OBSERVED_EVENTS = Arrays.asList(
            PlayerEvents.FirstFrameRendered.class,
            PlayerEvents.CoverUrlReady.class,
            PlayerEvents.SetScaleTypeCompleted.class
    );

    /**
     * 封面图视图
     */
    private ImageView mCoverImageView;

    /**
     * 是否已使用外部传入的封面地址
     * <p>
     * 外部地址优先级高于 SDK 从 MediaInfo 提取的地址。置 true 后 MediaInfo 回调里的地址一律忽略，
     * 避免两个来源互相覆盖。
     * </p>
     */
    private boolean mExternalCoverUsed = false;

    /**
     * 首帧是否已渲染
     * <p>
     * 单向标记。置 true 后任何路径都不再显示封面，只有重新绑定 / 解绑才复位。
     * 仅在主线程读写。
     * </p>
     */
    private boolean mFirstFrameRendered = false;

    public CoverSlot(@NonNull Context context) {
        super(context);
    }

    public CoverSlot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverSlot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_cover_slot;
    }

    @Override
    public void onAttach(@NonNull SlotHost host) {
        super.onAttach(host);

        // 获取视图引用
        mCoverImageView = findViewById(R.id.cover_image);

        // 初始为不可见，等封面地址就绪后再显示
        hide();
    }

    @Override
    public void onBindData(@NonNull AliPlayerModel model) {
        super.onBindData(model);

        // 1. 复位首帧标记：新的一轮播放，封面重新获得显示权
        mFirstFrameRendered = false;

        // 2. 清理轮次边界：取消上一轮在途加载 + 清图 + 回到隐藏态
        //    必须在决定封面来源之前，否则有 coverUrl 的分支会立即露出上一个视频的封面，
        //    没有 coverUrl 的分支则会带着上一轮的图继续显示
        clearCover();
        hide();

        // 3. 同步当前渲染填充模式，只在初始化时设一次是不够的
        applyCurrentScaleType();

        // 4. 决定封面来源
        String coverUrl = model.getCoverUrl();
        if (StringUtil.isNotEmpty(coverUrl)) {
            mExternalCoverUsed = true;
            loadCover(coverUrl);
            show();
        } else {
            mExternalCoverUsed = false;
            LogHub.i(TAG, "No external cover url, waiting for MediaInfo");
        }
    }

    @Override
    public void onUnbindData() {
        super.onUnbindData();

        // 清图 + 复位状态 + 隐藏
        //
        // 这里不调 Glide.clear()：解绑只由 AliPlayerView#detach() 触发，其主要来源是 Controller 销毁回调，
        // 此时宿主 Activity 可能已 destroy，而 Glide.with() 对已销毁的 Activity 会抛 IllegalArgumentException。
        // 在途请求无需操心 —— Glide 的 RequestManager 绑定在宿主生命周期上，
        // 宿主销毁时会自行取消所有请求；若是"解绑后重新绑定"的路径，取消也已由 onBindData 的清理覆盖。
        mCoverImageView.setImageDrawable(null);
        mExternalCoverUsed = false;
        mFirstFrameRendered = false;
        hide();

        LogHub.i(TAG, "Data unbound, cover cleared");
    }

    @Override
    protected List<Class<? extends PlayerEvent>> observedEvents() {
        return OBSERVED_EVENTS;
    }

    @Override
    protected void onEvent(@NonNull PlayerEvent event) {
        if (event instanceof PlayerEvents.FirstFrameRendered) {
            onFirstFrameRendered();
        } else if (event instanceof PlayerEvents.CoverUrlReady) {
            onCoverUrlReady((PlayerEvents.CoverUrlReady) event);
        } else if (event instanceof PlayerEvents.SetScaleTypeCompleted) {
            applyScaleType(((PlayerEvents.SetScaleTypeCompleted) event).scaleType);
        }
    }

    /**
     * 处理 MediaInfo 封面地址就绪事件
     * <p>
     * 仅在未配置外部封面地址、且首帧尚未渲染时生效。
     * </p>
     * <p>
     * Handle cover URL ready event from MediaInfo.
     * <p>
     * Only takes effect when no external cover URL is configured and the first frame has not arrived.
     * </p>
     */
    private void onCoverUrlReady(@NonNull PlayerEvents.CoverUrlReady event) {
        if (mExternalCoverUsed) {
            LogHub.i(TAG, "External cover already used, ignore MediaInfo cover");
            return;
        }
        if (StringUtil.isEmpty(event.coverUrl)) {
            return;
        }
        if (mFirstFrameRendered) {
            // 弱网下 MediaInfo 回调可能晚于首帧，此时封面绝不能再显示
            LogHub.i(TAG, "First frame already rendered, ignore MediaInfo cover");
            return;
        }

        loadCover(event.coverUrl);
        show();
    }

    /**
     * 处理首帧渲染完成事件
     * <p>
     * 立即隐藏封面（<b>无动画</b>）：封面与画面的显示范围已经一致，硬切让人看不出来；
     * 淡出反而会让静态封面叠在已经在运动的画面上，产生重影。
     * </p>
     * <p>
     * <b>不取消在途加载</b>：插槽已经隐藏，图片晚点回来也贴不到画面上；
     * 让它下载完还能进本地磁盘缓存，下次播同一个视频直接命中，取消只会浪费已经花掉的流量。
     * </p>
     * <p>
     * Handle first frame rendered event: hide the cover immediately without animation,
     * and keep the in-flight image request alive so it can still populate the disk cache.
     * </p>
     */
    private void onFirstFrameRendered() {
        LogHub.i(TAG, "First frame rendered, hide cover");
        mFirstFrameRendered = true;
        hide();
    }

    /**
     * 同步当前渲染填充模式
     * <p>
     * 从状态存储读取播放器当前的填充模式，读不到时兜底 {@link IMediaPlayer.ScaleType#DEFAULT}
     * （与状态存储自身的初始值同源，不在此处另立默认值）。
     * </p>
     */
    private void applyCurrentScaleType() {
        SlotHost host = getHost();
        applyScaleType(host != null
                ? host.getPlayerStateStore().getCurrentScaleType()
                : IMediaPlayer.ScaleType.DEFAULT);
    }

    /**
     * 应用渲染填充模式到封面图
     * <p>
     * 封面的缩放必须由 {@link ImageView#setScaleType} 承担，与播放器渲染行为一对一映射。
     * 在封面与视频宽高比一致的前提下，两者切换时不会有跳变。
     * </p>
     *
     * @param scaleType 播放器渲染填充模式
     */
    private void applyScaleType(@IMediaPlayer.ScaleType int scaleType) {
        ImageView.ScaleType target = PlayerTypeConverter.convertToImageScaleType(scaleType);
        if (mCoverImageView.getScaleType() != target) {
            LogHub.i(TAG, "Apply scale type", target);
            mCoverImageView.setScaleType(target);
        }
    }

    /**
     * 加载封面图
     * <p>
     * 不设 placeholder / error 占位图，不开启 crossFade：占位图会是一张明确错误的图盖在视频上，
     * crossFade 会推迟封面达到全不透明的时间，等于延长黑屏窗口。
     * 缩放交由 {@link ImageView#setScaleType} 承担，不使用 Glide 变换。
     * </p>
     * <p>
     * 也不注册加载回调：可见性只由绑定 / MediaInfo 回调 / 首帧三条路径决定，加载结果不参与其中。
     * 加载失败也无需处理 —— 封面层透明且不拦截触摸，一个显示着但没有图的封面层
     * 对画面和交互都没有影响。
     * </p>
     *
     * @param url 封面图 URL
     */
    private void loadCover(String url) {
        LogHub.i(TAG, "Load cover", url);

        Glide.with(this)
                .load(url)
                .dontAnimate()
                .into(mCoverImageView);
    }

    /**
     * 清理封面图
     * <p>
     * 取消在途加载并清空已显示的图。<b>只在 {@link #onBindData(AliPlayerModel)} 调用</b>：
     * 那是轮次边界且宿主必然存活，上一轮的图不该落到新一轮的 target 上。
     * 首帧时不要调用，否则会 abort 正在进行的网络下载（见 {@link #onFirstFrameRendered()}）；
     * 解绑时也不要调用（见 {@link #onUnbindData()}）。
     * </p>
     */
    private void clearCover() {
        Glide.with(this).clear(mCoverImageView);
        mCoverImageView.setImageDrawable(null);
    }
}
