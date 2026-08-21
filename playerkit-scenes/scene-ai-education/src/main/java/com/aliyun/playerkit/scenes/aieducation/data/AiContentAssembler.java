package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.ChapterInfo;
import com.aliyun.playerkit.scenes.aieducation.AiContentConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 内容数据组装器
 * <p>
 * 唯一职责：将服务端 DTO ({@link AiAnalysisResponse}) 转换为客户端展示模型。
 * 纯静态、无状态，所有组装逻辑集中于此。
 */
public final class AiContentAssembler {

    private AiContentAssembler() {
    }

    /**
     * 从服务端响应中组装章节列表（ChaptersOnly 样式）
     */
    @NonNull
    public static List<ChapterInfo> buildChapters(@NonNull AiAnalysisResponse response) {
        AiAnalysisResponse.ResultItem chapterResult = findResultByType(response, AiContentConstants.RESULT_TYPE_CHAPTER);
        if (chapterResult == null || chapterResult.getChapterContentItems() == null) {
            return Collections.emptyList();
        }
        List<ChapterInfo> chapters = new ArrayList<>();
        for (AiAnalysisResponse.ChapterContentItem item : chapterResult.getChapterContentItems()) {
            if (item == null) {
                continue;
            }
            ChapterInfo chapter = new ChapterInfo(
                    item.getChapterId() != null ? String.valueOf(item.getChapterId()) : null,
                    item.getStartTime() != null ? item.getStartTime() : 0L,
                    item.getEndTime() != null ? item.getEndTime() : 0L,
                    item.getChapterTitle() != null ? item.getChapterTitle() : "",
                    item.getThumbnailUrl()
            );
            chapters.add(chapter);
        }
        return chapters;
    }

    /**
     * 从服务端响应中组装知识点列表（TextAnalysis 样式）
     * <p>
     * 对每个 ChapterContentItem 执行三级降级：
     * 1. knowledgePoints 非空 → 拼接 markdown
     * 2. summary 非空 → 直接用作 markdown
     * 3. 都为空 → contentMarkdown 为 null（只显示标题）
     */
    @NonNull
    public static List<KnowledgeInfo> buildKnowledgeItems(@NonNull AiAnalysisResponse response) {
        AiAnalysisResponse.ResultItem chapterResult = findResultByType(response, AiContentConstants.RESULT_TYPE_CHAPTER);
        if (chapterResult == null || chapterResult.getChapterContentItems() == null) {
            return Collections.emptyList();
        }
        List<KnowledgeInfo> items = new ArrayList<>();
        for (AiAnalysisResponse.ChapterContentItem item : chapterResult.getChapterContentItems()) {
            if (item == null) {
                continue;
            }
            String markdown = buildContentMarkdown(item);
            KnowledgeInfo info = new KnowledgeInfo(
                    item.getChapterTitle() != null ? item.getChapterTitle() : "",
                    item.getStartTime() != null ? item.getStartTime() : 0L,
                    item.getEndTime() != null ? item.getEndTime() : 0L,
                    markdown
            );
            items.add(info);
        }
        return items;
    }

    /**
     * 从服务端响应中组装摘要信息（GraphicAnalysis 样式）
     *
     * @return SummaryInfo，如果无 summaryResult 则返回 null
     */
    @Nullable
    public static SummaryInfo buildSummaryInfo(@NonNull AiAnalysisResponse response) {
        AiAnalysisResponse.ResultItem summaryResult = findResultByType(response, AiContentConstants.RESULT_TYPE_SUMMARY);
        if (summaryResult == null || summaryResult.getSummaryContent() == null) {
            return null;
        }
        AiAnalysisResponse.SummaryContent content = summaryResult.getSummaryContent();
        List<String> mindMapTitles = null;
        if (content.getMindMapSummary() != null && !content.getMindMapSummary().isEmpty()) {
            mindMapTitles = new ArrayList<>();
            for (AiAnalysisResponse.MindMapItem mapItem : content.getMindMapSummary()) {
                if (mapItem == null) {
                    continue;
                }
                String title = mapItem.getTitle();
                if (title != null) {
                    mindMapTitles.add(title);
                }
            }
            if (mindMapTitles.isEmpty()) {
                mindMapTitles = null;
            }
        }
        return new SummaryInfo(
                content.getParagraphSummary(),
                mindMapTitles,
                content.getMarkdownContent()
        );
    }

    /**
     * 判断 AI 分析结果是否包含有效内容（章节或摘要）。
     */
    public static boolean hasAnyContent(@Nullable AiAnalysisResponse response) {
        return findResultByType(response, AiContentConstants.RESULT_TYPE_CHAPTER) != null
                || findResultByType(response, AiContentConstants.RESULT_TYPE_SUMMARY) != null;
    }

    /**
     * 一次性组装完整 ViewModel
     */
    @NonNull
    public static AiContentViewModel buildViewModel(@NonNull AiAnalysisResponse response) {
        List<ChapterInfo> chapters = buildChapters(response);
        List<KnowledgeInfo> knowledgeItems = buildKnowledgeItems(response);
        SummaryInfo summaryInfo = buildSummaryInfo(response);
        return new AiContentViewModel(chapters, knowledgeItems, summaryInfo);
    }

    // ---- Private ----

    /**
     * 按 resultType 筛选单个 AI 分析结果项
     * <p>
     * 将“按类型选取结果项”的数据处理逻辑从 DTO 下沉到组装器，
     * DTO ({@link AiAnalysisResponse}) 仅保留 JSON 字段映射。
     * 历史 NPE 教训：服务端数组元素可能为 null，需逐项防御。
     */
    @Nullable
    private static AiAnalysisResponse.ResultItem findResultByType(@Nullable AiAnalysisResponse response, @NonNull String resultType) {
        if (response == null) {
            return null;
        }
        List<AiAnalysisResponse.ResultItem> results = response.getAiAnalysisResult();
        if (results == null) {
            return null;
        }
        for (AiAnalysisResponse.ResultItem item : results) {
            if (item == null) {
                continue;
            }
            if (resultType.equals(item.getResultType())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 三级降级生成 markdown 内容
     */
    @Nullable
    private static String buildContentMarkdown(@NonNull AiAnalysisResponse.ChapterContentItem item) {
        // 1. knowledgePoints 非空 → 拼接 markdown
        List<AiAnalysisResponse.KnowledgePointItem> points = item.getKnowledgePoints();
        if (points != null && !points.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AiAnalysisResponse.KnowledgePointItem point : points) {
                if (point == null) {
                    continue;
                }
                String pointName = point.getPoint();
                String description = point.getDescription();
                if (pointName != null && description != null) {
                    sb.append("- **").append(pointName).append("**\uff1a").append(description).append("\n");
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
        // 2. summary 非空 → 直接用作 markdown
        String summary = item.getSummary();
        if (summary != null && !summary.isEmpty()) {
            return summary;
        }
        // 3. 都为空 → null
        return null;
    }
}
