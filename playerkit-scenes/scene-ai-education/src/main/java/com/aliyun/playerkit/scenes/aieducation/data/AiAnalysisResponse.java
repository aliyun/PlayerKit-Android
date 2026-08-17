package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * App Server AI 内容分析响应 DTO
 * <p>
 * 对应 App Server 返回的 JSON 结构，使用嵌套静态内部类组织层级关系。
 * 通过 Gson {@link SerializedName} 注解进行 JSON 字段反序列化映射，
 * 由 {@link AiContentAssembler} 组装为客户端展示模型。
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
     * 获取 AI 分析结果列表
     *
     * @return AI 分析结果项列表，可能为 null
     */
    @Nullable
    public List<ResultItem> getAiAnalysisResult() {
        return aiAnalysisResult;
    }

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
         * 获取结果类型
         *
         * @return 结果类型字符串，如 "Chapter"、"Summary"，可能为 null
         */
        @Nullable
        public String getResultType() {
            return resultType;
        }

        /**
         * 获取视频标题
         *
         * @return 视频标题，可能为 null
         */
        @Nullable
        public String getTitle() {
            return title;
        }

        /**
         * 获取额外信息
         *
         * @return 额外信息 JSON 字符串，可能为 null
         */
        @Nullable
        public String getExtra() {
            return extra;
        }

        /**
         * 获取摘要内容
         *
         * @return 摘要内容对象，可能为 null
         */
        @Nullable
        public SummaryContent getSummaryContent() {
            return summaryContent;
        }

        /**
         * 获取章节内容列表
         *
         * @return 章节内容项列表，可能为 null
         */
        @Nullable
        public List<ChapterContentItem> getChapterContentItems() {
            return chapterContentItems;
        }

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
     * 对应 JSON 中的 "summaryContent" 字段，包含段落摘要、MindMap 和 Markdown 内容。
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
         * 思维导图摘要列表
         */
        @Nullable
        @SerializedName("MindMapSummary")
        public List<MindMapItem> mindMapSummary;

        /**
         * 获取段落摘要文本
         *
         * @return 段落摘要文本，可能为 null
         */
        @Nullable
        public String getParagraphSummary() {
            return paragraphSummary;
        }

        /**
         * 获取 Markdown 格式内容
         *
         * @return Markdown 内容字符串，可能为 null
         */
        @Nullable
        public String getMarkdownContent() {
            return markdownContent;
        }

        /**
         * 获取思维导图摘要列表
         *
         * @return 思维导图项列表，可能为 null
         */
        @Nullable
        public List<MindMapItem> getMindMapSummary() {
            return mindMapSummary;
        }

        /**
         * 自定义字符串表示，方便日志打印和调试
         */
        @NonNull
        @Override
        public String toString() {
            return "SummaryContent{" +
                    "paragraphSummary='" + paragraphSummary + '\'' +
                    ", markdownContent='" + markdownContent + '\'' +
                    ", mindMapSummary=" + mindMapSummary +
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
         * 获取章节 ID
         *
         * @return 章节 ID，可能为 null
         */
        @Nullable
        public Integer getChapterId() {
            return chapterId;
        }

        /**
         * 获取章节开始时间
         *
         * @return 开始时间（毫秒），可能为 null
         */
        @Nullable
        public Long getStartTime() {
            return startTime;
        }

        /**
         * 获取章节结束时间
         *
         * @return 结束时间（毫秒），可能为 null
         */
        @Nullable
        public Long getEndTime() {
            return endTime;
        }

        /**
         * 获取章节标题
         *
         * @return 章节标题字符串，可能为 null
         */
        @Nullable
        public String getChapterTitle() {
            return chapterTitle;
        }

        /**
         * 获取章节摘要
         *
         * @return 章节摘要文本，可能为 null
         */
        @Nullable
        public String getSummary() {
            return summary;
        }

        /**
         * 获取章节缩略图 URL
         *
         * @return 缩略图 URL，可能为 null
         */
        @Nullable
        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        /**
         * 获取知识点列表
         *
         * @return 知识点项列表，可能为 null
         */
        @Nullable
        public List<KnowledgePointItem> getKnowledgePoints() {
            return knowledgePoints;
        }

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
         * 获取知识点 ID
         *
         * @return 知识点 ID，可能为 null
         */
        @Nullable
        public Integer getPointId() {
            return pointId;
        }

        /**
         * 获取知识点名称
         *
         * @return 知识点名称字符串，可能为 null
         */
        @Nullable
        public String getPoint() {
            return point;
        }

        /**
         * 获取知识点描述
         *
         * @return 知识点描述文本，可能为 null
         */
        @Nullable
        public String getDescription() {
            return description;
        }

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

    /**
     * 思维导图项
     * <p>
     * 对应 JSON 中 "MindMapSummary" 数组中的每一项，
     * 包含标题和子话题列表（递归结构）。
     * </p>
     */
    public static class MindMapItem {

        /**
         * 思维导图节点标题
         */
        @Nullable
        @SerializedName("Title")
        public String title;

        /**
         * 子话题列表（递归结构，每个子话题也是 MindMapItem）
         */
        @Nullable
        @SerializedName("Topics")
        public List<MindMapItem> topics;

        /**
         * 获取思维导图节点标题
         *
         * @return 节点标题，可能为 null
         */
        @Nullable
        public String getTitle() {
            return title;
        }

        /**
         * 获取子话题列表
         *
         * @return 子话题项列表（递归结构），可能为 null
         */
        @Nullable
        public List<MindMapItem> getTopics() {
            return topics;
        }

        /**
         * 自定义字符串表示，方便日志打印和调试
         */
        @NonNull
        @Override
        public String toString() {
            return "MindMapItem{" +
                    "title='" + title + '\'' +
                    ", topics=" + topics +
                    '}';
        }
    }
}
