package com.aliyun.playerkit.scenes.aieducation.data;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.logging.LogHub;
import com.aliyun.playerkit.scenes.aieducation.AiContentConstants;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
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
 * 将 App Server 返回的 AI 分析数据转换为 {@link AiVideoContent} 业务模型。
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
    public interface Callback {
        /**
         * 加载完成（主线程回调）
         *
         * @param content 加载结果，失败时为 null
         */
        void onLoaded(@Nullable AiVideoContent content);
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
    public void load(@NonNull String mediaId, @NonNull Callback callback) {
        mExecutor.execute(() -> {
            AiVideoContent result = doLoadSync(mediaId);
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
     *     <li>所有回调（{@link Callback} / {@link PlayAuthCallback}）不再触发；</li>
     *     <li>本实例不可复用，后续调用 {@link #load} / {@link #loadPlayAuth} 将不会执行。</li>
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
     * 同步执行 HTTP 请求 + JSON 解析 + 模型转换
     *
     * @param mediaId 媒体 ID（即视频 Vid）
     * @return AI 内容数据，加载失败返回 null
     */
    @Nullable
    private AiVideoContent doLoadSync(@NonNull String mediaId) {
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
                return convertToAiVideoContent(mediaId, apiResponse);
            }
        } catch (Exception e) {
            LogHub.w(TAG, "load failed", e);
            return null;
        }
    }

    /**
     * 将 App Server 响应转换为业务模型
     *
     * @param mediaId  媒体 ID（即视频 Vid）
     * @param response App Server 响应 DTO
     * @return 业务模型，转换失败返回 null
     */
    @Nullable
    private AiVideoContent convertToAiVideoContent(@NonNull String mediaId, @Nullable AiAnalysisResponse response) {
        if (response == null || response.aiAnalysisResult == null) {
            return null;
        }

        // 分别查找 Chapter 和 Summary 结果项
        AiAnalysisResponse.ResultItem chapterResult = null;
        AiAnalysisResponse.ResultItem summaryResult = null;
        for (AiAnalysisResponse.ResultItem item : response.aiAnalysisResult) {
            if (item == null) {
                continue;
            }
            if (AiContentConstants.RESULT_TYPE_CHAPTER.equals(item.resultType)) {
                chapterResult = item;
            } else if (AiContentConstants.RESULT_TYPE_SUMMARY.equals(item.resultType)) {
                summaryResult = item;
            }
        }
        // 至少需要有一种结果
        if (chapterResult == null && summaryResult == null) {
            return null;
        }

        // 构建章节列表和分段列表（chapterResult 可能为 null）
        List<ChapterInfo> chapters = new ArrayList<>();
        List<AiSegment> segments = new ArrayList<>();
        boolean hasAnalysis = false;

        if (chapterResult != null && chapterResult.chapterContentItems != null) {
            for (AiAnalysisResponse.ChapterContentItem contentItem : chapterResult.chapterContentItems) {
                if (contentItem == null) {
                    continue;
                }
                long startTime = contentItem.startTime != null ? contentItem.startTime : 0;
                long endTime = contentItem.endTime != null ? contentItem.endTime : 0;
                String chapterTitle = contentItem.chapterTitle != null ? contentItem.chapterTitle : "";
                String chapterId = contentItem.chapterId != null ? String.valueOf(contentItem.chapterId) : "0";

                // 构建 ChapterInfo
                chapters.add(new ChapterInfo(chapterId, startTime, endTime, chapterTitle, contentItem.thumbnailUrl));

                // 构建 markdown（由 KnowledgePoints 拼接）
                StringBuilder markdown = new StringBuilder();
                if (contentItem.knowledgePoints != null && !contentItem.knowledgePoints.isEmpty()) {
                    hasAnalysis = true;
                    for (AiAnalysisResponse.KnowledgePointItem kp : contentItem.knowledgePoints) {
                        if (kp == null) {
                            continue;
                        }
                        String point = kp.point != null ? kp.point : "";
                        String description = kp.description != null ? kp.description : "";
                        markdown.append("- **").append(point).append("**: ").append(description).append("\n");
                    }
                }

                // 检查是否有摘要
                if (contentItem.summary != null && !contentItem.summary.isEmpty()) {
                    hasAnalysis = true;
                }

                // 构建 AiSegment
                String mdContent = markdown.length() > 0 ? markdown.toString() : null;
                segments.add(new AiSegment(chapterId, startTime, endTime, chapterTitle, contentItem.summary, mdContent));
            }
        }

        // 构建 AiSummary（优先从 Summary 结果的 summaryContent 获取，降级到 Chapter 结果的 Extra）
        AiSummary summary = null;
        if (summaryResult != null && summaryResult.summaryContent != null
                && summaryResult.summaryContent.paragraphSummary != null) {
            summary = new AiSummary(summaryResult.title, summaryResult.summaryContent.paragraphSummary, null);
        } else if (chapterResult != null && chapterResult.extra != null && !chapterResult.extra.isEmpty()) {
            try {
                JSONObject extraJson = new JSONObject(chapterResult.extra);
                String extraSummary = extraJson.optString("Summary");
                if (!TextUtils.isEmpty(extraSummary)) {
                    summary = new AiSummary(chapterResult.title, extraSummary, null);
                }
            } catch (Exception e) {
                LogHub.w(TAG, "parse extra failed", e);
            }
        }

        // 确定 AiContentType
        @AiContentType int type = hasAnalysis ? AiContentType.TEXT_ANALYSIS : AiContentType.CHAPTERS_ONLY;

        return new AiVideoContent(mediaId, type, summary, chapters, segments);
    }
}
