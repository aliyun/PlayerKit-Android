package com.aliyun.playerkit.scenes.aieducation.data;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.scenes.aieducation.AiContentConstants;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AI 内容加载器
 * <p>
 * 封装 App Server HTTP 请求和 JSON 响应解析逻辑，
 * 将 App Server 返回的 AI 分析数据以 DTO ({@link AiAnalysisResponse}) 形式回调给消费侧。
 * 采用异步回调模式，在后台线程执行网络请求，通过主线程回调返回结果。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class AiContentLoader {

    private static final String TAG = "AiContentLoader";

    private static final String THREAD_NAME_PREFIX = "PlayerKit-AiContentLoader-";

    /**
     * HTTP 请求超时时间（秒）
     */
    private static final int TIMEOUT_SECONDS = 5;

    /**
     * HTTP 客户端，用于执行同步请求
     */
    private final OkHttpClient mHttpClient = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    // 子线程池（单线程，实例内复用，线程命名便于问题排查）
    private final ExecutorService mExecutor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, THREAD_NAME_PREFIX + mCount.getAndIncrement());
                }
            }
    );

    /**
     * 取消标记，取消后不再触发回调
     */
    private volatile boolean mCanceled = false;

    /**
     * 加载结果回调
     */
    public interface AiAnalysisCallback {
        /**
         * 加载完成（主线程回调）
         *
         * @param response 服务端 DTO，失败或无有效内容时为 null
         */
        void onLoaded(@Nullable AiAnalysisResponse response);
    }

    /**
     * PlayAuth 加载结果回调
     */
    public interface PlayAuthCallback {
        /**
         * 加载完成（主线程回调）
         *
         * @param playAuth PlayAuth 凭证，失败时为 null
         */
        void onLoaded(@Nullable String playAuth);
    }

    /**
     * 异步加载 AI 内容（主线程安全）
     *
     * @param mediaId  媒体 ID（即视频 Vid）
     * @param callback 加载结果回调
     */
    public void loadAiAnalysis(@NonNull String mediaId, @NonNull AiAnalysisCallback callback) {
        mExecutor.execute(() -> {
            AiAnalysisResponse result = doLoadAiAnalysisSync(mediaId);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!mCanceled) {
                    callback.onLoaded(result);
                }
            });
        });
    }

    /**
     * 异步加载 PlayAuth（主线程安全）
     *
     * @param videoId  视频 ID
     * @param callback 加载结果回调
     */
    public void loadPlayAuth(@NonNull String videoId, @NonNull PlayAuthCallback callback) {
        mExecutor.execute(() -> {
            String playAuth = doLoadPlayAuthSync(videoId);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!mCanceled) {
                    callback.onLoaded(playAuth);
                }
            });
        });
    }

    /**
     * 取消所有进行中的加载任务
     * <p>
     * 调用后：
     * <ul>
     *     <li>已提交但未完成的任务将被中断，进行中的 HTTP 请求会被取消；</li>
     *     <li>所有回调（{@link AiAnalysisCallback} / {@link PlayAuthCallback}）不再触发；</li>
     *     <li>本实例不可复用，后续调用 {@link #loadAiAnalysis} / {@link #loadPlayAuth} 将不会执行。</li>
     * </ul>
     * 通常在页面销毁（如 Activity#onDestroy）时调用，避免内存泄漏和无效回调。
     */
    public void cancel() {
        mCanceled = true;
        mExecutor.shutdownNow();
        // 取消本实例私有 HTTP 客户端上进行中的请求
        mHttpClient.dispatcher().cancelAll();
    }

    /**
     * 同步执行 PlayAuth HTTP 请求
     *
     * @param videoId 视频 ID
     * @return PlayAuth 凭证，加载失败返回 null
     */
    @Nullable
    private String doLoadPlayAuthSync(@NonNull String videoId) {
        try {
            String url = AiContentConstants.buildPlayAuthUrl(videoId);
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = mHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    LogHub.w(TAG, "PlayAuth request failed: " + response.code());
                    return null;
                }
                String json = response.body().string();
                LogHub.i(TAG, "PlayAuth response: " + json);
                PlayAuthResponse authResponse = new Gson().fromJson(json, PlayAuthResponse.class);
                if (authResponse != null && authResponse.data != null) {
                    return authResponse.data.playAuth;
                }
                return null;
            }
        } catch (Exception e) {
            LogHub.w(TAG, "loadPlayAuth failed", e);
            return null;
        }
    }

    /**
     * 同步执行 HTTP 请求 + JSON 解析
     *
     * @param mediaId 媒体 ID（即视频 Vid）
     * @return 服务端 DTO，加载失败或无有效内容时返回 null
     */
    @Nullable
    private AiAnalysisResponse doLoadAiAnalysisSync(@NonNull String mediaId) {
        try {
            // 构建请求 URL（不传 ResultType，默认返回所有类型数据）
            String url = AiContentConstants.buildApiUrl(mediaId, null);

            // 执行同步 HTTP GET 请求
            Request request = new Request.Builder().url(url).get().build();

            try (Response response = mHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    LogHub.w(TAG, "App Server request failed: " + response.code());
                    return null;
                }
                String json = response.body().string();
                LogHub.i(TAG, "App Server response: " + json);
                AiAnalysisResponse apiResponse = new Gson().fromJson(json, AiAnalysisResponse.class);
                if (apiResponse == null || apiResponse.getAiAnalysisResult() == null) {
                    return null;
                }
                // 空结果兜底：Chapter 与 Summary 都为 null 时视为无有效内容，让上层走加载失败提示
                if (!AiContentAssembler.hasAnyContent(apiResponse)) {
                    return null;
                }
                return apiResponse;
            }
        } catch (Exception e) {
            LogHub.w(TAG, "load failed", e);
            return null;
        }
    }
}
