package com.aliyun.playerkit.scenes.aieducation;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * AI 内容相关常量。
 * AI content related constants.
 *
 * <p>本类集中管理"AI 教育"场景所需的配置项、接口地址、请求参数及结果类型常量，
 * 主要用于：<br>
 * This class centralizes the configuration, API endpoints, request parameters and result type
 * constants required by the "AI education" scene. It is mainly used for:
 * <ul>
 *   <li>获取视频播放凭证（PlayAuth） / Fetching video play auth</li>
 *   <li>获取 AI 分析结果（章节、摘要等） / Fetching AI analysis results (chapters, summary, etc.)</li>
 * </ul>
 *
 * <p><b>集成前必读 / Before You Integrate</b><br>
 * 接入本模块前，请先完成以下两项必要配置：<br>
 * Before integrating this module, please complete the following two required configurations:
 * <ol>
 *   <li>{@link #APP_SERVER_BASE_URL} —— 替换为您自己部署的 App Server 地址；<br>
 *       Replace with the address of your own deployed App Server.</li>
 *   <li>{@link #DEFAULT_MEDIA_ID} —— 替换为您自己账号下的媒体 ID；<br>
 *       Replace with a media ID from your own account.</li>
 * </ol>
 * 详细说明请参见对应字段上的注释。<br>
 * See the comments on each field for details.
 *
 * @author keria
 * @date 2026/07/03
 */
public final class AiContentConstants {

    private AiContentConstants() {
    }

    /**
     * AI 内容分析服务的 App Server 基础地址。
     * App Server base URL for AI content analysis.
     *
     * <p><b>快速开始 / Quick Start</b><br>
     * 将此地址替换为您自己部署的 App Server 地址，即可完成端侧运行。集成时需要修改的配置共两项，
     * 除本地址外，还需要将 {@link #DEFAULT_MEDIA_ID} 替换为您自己账号下的媒体 ID。<br>
     * Replace this with the address of your own deployed App Server to get the client up and
     * running. There are two configurations you need to modify for integration: besides this
     * address, you also need to replace {@link #DEFAULT_MEDIA_ID} with a media ID from your own
     * account.
     *
     * <p><b>地址格式 / URL Format</b><br>
     * {@code http://<您的服务器IP或域名>:<端口>}，末尾不要带 {@code /}，例如 {@code http://192.168.1.100:9000}。<br>
     * {@code http://<your-server-ip-or-domain>:<port>}, without a trailing {@code /},
     * e.g. {@code http://192.168.1.100:9000}.
     *
     * <p><b>依赖接口 / Required APIs</b><br>
     * 本模块会请求该服务的以下两个接口：<br>
     * This module calls the following two APIs on the server:
     * <ul>
     *   <li>{@code /appServer/GetVideoPlayAuth} —— 获取播放凭证 / Get video play auth</li>
     *   <li>{@code /GetMediaAiAnalysisByResultType} —— 获取 AI 分析内容 / Get AI analysis content</li>
     * </ul>
     *
     * <p><b>App Server 开源地址 / Open Source</b><br>
     * App Server 已开源，项目地址：<br>
     * The App Server is open source and available at:<br>
     * <a href="https://github.com/MediaBox-Demos/VodAppServer">阿里云 · 视频点播（VOD）服务端 App Server</a>
     *
     * <p><b>运行时覆盖 / Runtime Override</b><br>
     * Demo 设置页（链接设置）中配置的 App Server 地址优先级高于此常量，仅用于调试，集成时无需关心。<br>
     * The App Server address configured in the demo settings page (Link Settings) takes
     * precedence over this constant. It is for debugging only and can be ignored for integration.
     */
    public static final String APP_SERVER_BASE_URL = "https://vodappserver-gwzwacvbyf.cn-shanghai.fcapp.run";

    // ==================== Default Values ====================

    /**
     * 默认播放媒体 ID。
     * Default media ID used for playback.
     *
     * <p><b>快速开始 / Quick Start</b><br>
     * 与 {@link #APP_SERVER_BASE_URL} 一样，这是集成时需要修改的两项配置之一。请将此值替换为您自己账号下
     * 已上传至阿里云视频点播（VOD）的媒体 ID，且该媒体必须能够被上面配置的 {@link #APP_SERVER_BASE_URL}
     * 对应的 App Server 查询到（即两者要属于同一套点播服务/同一个阿里云账号）。<br>
     * Just like {@link #APP_SERVER_BASE_URL}, this is one of the two configurations you need to
     * modify for integration. Replace this value with a media ID that has already been uploaded
     * to your own Alibaba Cloud VOD (Video on Demand) account, and make sure it can be queried by
     * the App Server configured via {@link #APP_SERVER_BASE_URL} (i.e. they must belong to the
     * same VOD service / same Alibaba Cloud account).
     *
     * <p><b>获取方式 / How to Obtain</b><br>
     * 登录阿里云视频点播控制台，在媒体管理中找到已上传视频对应的媒体 ID（MediaId），
     * 或调用点播上传/查询接口获取。<br>
     * Log in to the Alibaba Cloud VOD console, locate the MediaId of an uploaded video under
     * media management, or obtain it via the VOD upload/query APIs.
     *
     * <p><b>使用场景 / Usage</b><br>
     * 该 MediaId 会作为默认值传入播放器及 AI 内容分析接口
     * （{@code /appServer/GetVideoPlayAuth} 和 {@code /GetMediaAiAnalysisByResultType}），
     * 用于获取播放凭证与 AI 分析结果（章节、摘要等）。<br>
     * This MediaId is passed as the default value to the player and to the AI content analysis
     * APIs ({@code /appServer/GetVideoPlayAuth} and {@code /GetMediaAiAnalysisByResultType}) to
     * fetch the play auth and AI analysis results (chapters, summary, etc.).
     *
     * <p><b>运行时覆盖 / Runtime Override</b><br>
     * Demo 中若通过入口页面或设置页手动传入了具体的 MediaId，则优先使用传入值，此常量仅作为默认兜底值，
     * 集成时可根据实际业务场景动态传入真实 MediaId。<br>
     * If a specific MediaId is manually provided via the entry page or settings page in the demo,
     * that value takes precedence. This constant only serves as a default fallback value; in
     * real integration, you should dynamically pass the actual MediaId based on your business
     * scenario.
     */
    public static final String DEFAULT_MEDIA_ID = "30158cf1907c71f180176723a78f0102";

    // ==================== API Endpoints ====================

    /**
     * 视频内容分析接口
     * <p>
     * API endpoint for getting media AI analysis results by result type.
     */
    public static final String API_GET_MEDIA_AI_ANALYSIS = APP_SERVER_BASE_URL + "/GetMediaAiAnalysisByResultType";

    /**
     * 获取视频播放凭证接口
     * <p>
     * API endpoint for getting video play auth.
     */
    public static final String API_GET_VIDEO_PLAY_AUTH = APP_SERVER_BASE_URL + "/appServer/GetVideoPlayAuth";

    // ==================== Query Parameters ====================

    /**
     * 请求参数：媒体 ID
     * Query parameter: MediaId
     */
    public static final String PARAM_MEDIA_ID = "MediaId";

    /**
     * 请求参数：视频 ID
     * Query parameter: videoId
     */
    public static final String PARAM_VIDEO_ID = "videoId";

    /**
     * 请求参数：结果类型（可选，不传则返回所有类型）
     * Query parameter: ResultType (optional, returns all types if omitted)
     */
    public static final String PARAM_RESULT_TYPE = "ResultType";

    // ==================== Result Types ====================

    /**
     * AI 分析结果类型：章节
     * <p>
     * AI analysis result type: Chapter
     */
    public static final String RESULT_TYPE_CHAPTER = "Chapter";

    /**
     * AI 分析结果类型：摘要
     * <p>
     * AI analysis result type: Summary
     */
    public static final String RESULT_TYPE_SUMMARY = "Summary";

    // ==================== URL Builder ====================

    /**
     * 构建 AI 内容分析 API 请求 URL
     * <p>
     * Build API request URL for AI content analysis.
     *
     * @param mediaId    媒体 ID（必填）
     * @param resultType 结果类型（可选，传 null 则不带 ResultType 参数，返回所有类型）
     * @return 完整请求 URL
     */
    public static String buildApiUrl(@NonNull String mediaId, @Nullable String resultType) {
        StringBuilder url = new StringBuilder(API_GET_MEDIA_AI_ANALYSIS);
        url.append("?").append(PARAM_MEDIA_ID).append("=").append(mediaId);
        if (!TextUtils.isEmpty(resultType)) {
            url.append("&").append(PARAM_RESULT_TYPE).append("=").append(resultType);
        }
        return url.toString();
    }

    /**
     * 构建 PlayAuth 请求 URL
     */
    public static String buildPlayAuthUrl(@NonNull String videoId) {
        return API_GET_VIDEO_PLAY_AUTH + "?" + PARAM_VIDEO_ID + "=" + videoId;
    }
}
