package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * 视频摘要信息模型，对应 GraphicAnalysis 模式下的摘要数据。
 * <p>
 * 由 {@link AiContentAssembler#buildSummaryInfo} 从服务端 DTO 组装而来，
 * 用于 {@link com.aliyun.playerkit.scenes.aieducation.data.AiContentType#GRAPHIC_ANALYSIS} 场景。
 * <p>
 * 展示规则：ParagraphSummary + MindMapSummary 标题列表（徽章标签）+ MarkdownContent。
 *
 * @author keria
 * @date 2026/08/17
 * @see AiContentAssembler#buildSummaryInfo
 * @see AiContentType#GRAPHIC_ANALYSIS
 */
public class SummaryInfo {

    /**
     * 段落摘要文本，可能为 null
     */
    @Nullable
    private final String paragraphSummary;

    /**
     * 思维导图标题列表，用于徽章标签展示，可能为 null
     */
    @Nullable
    private final List<String> mindMapTitles;

    /**
     * Markdown 格式的详细内容，可能为 null
     */
    @Nullable
    private final String markdownContent;

    /**
     * 构造视频摘要信息实例。
     *
     * @param paragraphSummary 段落摘要文本，可为 null
     * @param mindMapTitles    思维导图标题列表，可为 null
     * @param markdownContent  Markdown 格式的详细内容，可为 null
     */
    public SummaryInfo(@Nullable String paragraphSummary, @Nullable List<String> mindMapTitles, @Nullable String markdownContent) {
        this.paragraphSummary = paragraphSummary;
        this.mindMapTitles = mindMapTitles;
        this.markdownContent = markdownContent;
    }

    /**
     * 获取段落摘要文本。
     *
     * @return 段落摘要，可能为 null
     */
    @Nullable
    public String getParagraphSummary() {
        return paragraphSummary;
    }

    /**
     * 获取思维导图标题列表，用于徽章标签展示。
     *
     * @return 标题字符串列表，可能为 null
     */
    @Nullable
    public List<String> getMindMapTitles() {
        return mindMapTitles;
    }

    /**
     * 获取 Markdown 格式的详细内容。
     *
     * @return Markdown 内容文本，可能为 null
     */
    @Nullable
    public String getMarkdownContent() {
        return markdownContent;
    }

    @NonNull
    @Override
    public String toString() {
        return "SummaryInfo{" +
                "paragraphSummary='" + paragraphSummary + '\'' +
                ", mindMapTitles=" + mindMapTitles +
                ", markdownContent='" + markdownContent + '\'' +
                '}';
    }
}
