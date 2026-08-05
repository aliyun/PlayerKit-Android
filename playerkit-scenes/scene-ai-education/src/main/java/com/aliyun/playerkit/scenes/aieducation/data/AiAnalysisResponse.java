package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * App Server AI 内容分析响应 DTO
 * <p>
 * 对应 App Server 返回的 JSON 结构，使用嵌套静态内部类组织层级关系。
 * 通过 Gson 反序列化后，由转换层映射为业务模型 {@link AiVideoContent}。
 * </p>
 * <p>
 * 对应 App Server 接口：{@link com.aliyun.playerkit.scenes.aieducation.AiContentConstants#API_GET_MEDIA_AI_ANALYSIS } 视频内容分析
 *
 * @author keria
 * @date 2026/07/03
 */
public class AiAnalysisResponse {

    /**
     * AI 分析结果列表
     */
    @Nullable
    @SerializedName("AiAnalysisResult")
    public List<ResultItem> aiAnalysisResult;

    /**
     * 单个 AI 分析结果项
     * <p>
     * 对应 JSON 中 "AiAnalysisResult" 数组中的每一项，
     * 包含结果类型（如 "Chapter"/"Summary"）、标题、额外信息和内容列表。
     * </p>
     */
    public static class ResultItem {

        /**
         * 结果类型（如 "Chapter"、"Summary"）
         */
        @Nullable
        @SerializedName("ResultType")
        public String resultType;

        /**
         * 视频标题
         */
        @Nullable
        @SerializedName("Title")
        public String title;

        /**
         * 额外信息（JSON 字符串，包含整体摘要等）
         */
        @Nullable
        @SerializedName("Extra")
        public String extra;

        /**
         * 摘要内容（ResultType 为 Summary 时存在）
         */
        @Nullable
        @SerializedName("summaryContent")
        public SummaryContent summaryContent;

        /**
         * 章节内容列表（ResultType 为 Chapter 时存在）
         */
        @Nullable
        @SerializedName("chapterContentItems")
        public List<ChapterContentItem> chapterContentItems;

        /**
         * 自定义字符串表示，方便日志打印和调试
         */
        @NonNull
        @Override
        public String toString() {
            return "ResultItem{" +
                    "resultType='" + resultType + '\'' +
                    ", title='" + title + '\'' +
                    ", extra='" + extra + '\'' +
                    ", summaryContent=" + summaryContent +
                    ", chapterContentItems=" + chapterContentItems +
                    '}';
        }
    }

    /**
     * 摘要内容
     * <p>
     * 对应 JSON 中的 "summaryContent" 字段，包含段落摘要和 Markdown 内容。
     * </p>
     */
    public static class SummaryContent {

        /**
         * 段落摘要文本
         */
        @Nullable
        @SerializedName("ParagraphSummary")
        public String paragraphSummary;

        /**
         * Markdown 格式内容
         */
        @Nullable
        @SerializedName("MarkdownContent")
        public String markdownContent;

        /**
         * 自定义字符串表示，方便日志打印和调试
         */
        @NonNull
        @Override
        public String toString() {
            return "SummaryContent{" +
                    "paragraphSummary='" + paragraphSummary + '\'' +
                    ", markdownContent='" + markdownContent + '\'' +
                    '}';
        }
    }

    /**
     * 章节内容项
     * <p>
     * 对应 JSON 中 "chapterContentItems" 数组中的每一项，
     * 包含章节的时间范围、标题、摘要、缩略图和知识点列表。
     * </p>
     */
    public static class ChapterContentItem {

        /**
         * 章节 ID
         */
        @Nullable
        @SerializedName("ChapterId")
        public Integer chapterId;

        /**
         * 章节开始时间（毫秒）
         */
        @Nullable
        @SerializedName("StartTime")
        public Long startTime;

        /**
         * 章节结束时间（毫秒）
         */
        @Nullable
        @SerializedName("EndTime")
        public Long endTime;

        /**
         * 章节标题
         */
        @Nullable
        @SerializedName("ChapterTitle")
        public String chapterTitle;

        /**
         * 章节摘要
         */
        @Nullable
        @SerializedName("Summary")
        public String summary;

        /**
         * 章节缩略图 URL
         */
        @Nullable
        @SerializedName("ThumbnailUrl")
        public String thumbnailUrl;

        /**
         * 知识点列表
         */
        @Nullable
        @SerializedName("KnowledgePoints")
        public List<KnowledgePointItem> knowledgePoints;

        /**
         * 自定义字符串表示，方便日志打印和调试
         */
        @NonNull
        @Override
        public String toString() {
            return "ChapterContentItem{" +
                    "chapterId=" + chapterId +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", chapterTitle='" + chapterTitle + '\'' +
                    ", summary='" + summary + '\'' +
                    ", thumbnailUrl='" + thumbnailUrl + '\'' +
                    ", knowledgePoints=" + knowledgePoints +
                    '}';
        }
    }

    /**
     * 知识点项
     * <p>
     * 对应 JSON 中 "KnowledgePoints" 数组中的每一项，
     * 包含知识点 ID、名称和描述。
     * </p>
     */
    public static class KnowledgePointItem {

        /**
         * 知识点 ID
         */
        @Nullable
        @SerializedName("PointId")
        public Integer pointId;

        /**
         * 知识点名称
         */
        @Nullable
        @SerializedName("Point")
        public String point;

        /**
         * 知识点描述
         */
        @Nullable
        @SerializedName("Description")
        public String description;

        /**
         * 自定义字符串表示，方便日志打印和调试
         */
        @NonNull
        @Override
        public String toString() {
            return "KnowledgePointItem{" +
                    "pointId=" + pointId +
                    ", point='" + point + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
