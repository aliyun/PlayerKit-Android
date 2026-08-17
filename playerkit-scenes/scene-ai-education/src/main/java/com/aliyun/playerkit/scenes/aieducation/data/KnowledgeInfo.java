package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * 知识点信息模型，对应 TextAnalysis 模式下的单个知识点条目。
 * <p>
 * 由 {@link AiContentAssembler#buildKnowledgeItems} 从服务端 DTO 组装而来，
 * 用于 {@link com.aliyun.playerkit.scenes.aieducation.data.AiContentType#TEXT_ANALYSIS} 场景，
 * UI 层直接消费，无需二次处理。
 *
 * @author keria
 * @date 2026/08/17
 * @see AiContentAssembler#buildKnowledgeItems
 * @see AiContentType#TEXT_ANALYSIS
 */
public class KnowledgeInfo {

    /**
     * 知识点标题
     */
    private final String title;

    /**
     * 知识点对应视频片段的起始时间，单位：毫秒
     */
    private final long startMs;

    /**
     * 知识点对应视频片段的结束时间，单位：毫秒
     */
    private final long endMs;

    /**
     * 知识点详情内容，Markdown 格式，可能为 null
     */
    @Nullable
    private final String contentMarkdown;

    /**
     * 构造知识点信息实例。
     *
     * @param title           知识点标题
     * @param startMs         对应视频片段的起始时间（毫秒）
     * @param endMs           对应视频片段的结束时间（毫秒）
     * @param contentMarkdown 知识点详情，Markdown 格式，可为 null
     */
    public KnowledgeInfo(String title, long startMs, long endMs, @Nullable String contentMarkdown) {
        this.title = title;
        this.startMs = startMs;
        this.endMs = endMs;
        this.contentMarkdown = contentMarkdown;
    }

    /**
     * 获取知识点标题。
     *
     * @return 知识点标题文本
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取知识点对应视频片段的起始时间。
     *
     * @return 起始时间，单位：毫秒
     */
    public long getStartMs() {
        return startMs;
    }

    /**
     * 获取知识点对应视频片段的结束时间。
     *
     * @return 结束时间，单位：毫秒
     */
    public long getEndMs() {
        return endMs;
    }

    /**
     * 获取知识点详情内容。
     *
     * @return Markdown 格式的详情文本，可能为 null
     */
    @Nullable
    public String getContentMarkdown() {
        return contentMarkdown;
    }

    /**
     * 根据播放位置查找当前知识点索引（二分查找，O(log n)）
     * <p>
     * 假定知识点列表按时间范围有序排列且区间不重叠。
     * </p>
     *
     * @param items      知识点列表
     * @param positionMs 当前播放位置（毫秒）
     * @return 当前知识点索引，未找到返回 -1
     */
    public static int findIndexByPosition(@Nullable List<KnowledgeInfo> items, long positionMs) {
        if (items == null || items.isEmpty()) return -1;

        int low = 0;
        int high = items.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            KnowledgeInfo item = items.get(mid);
            if (positionMs < item.getStartMs()) {
                high = mid - 1;
            } else if (positionMs >= item.getEndMs()) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public String toString() {
        return "KnowledgeInfo{" +
                "title='" + title + '\'' +
                ", startMs=" + startMs +
                ", endMs=" + endMs +
                ", contentMarkdown='" + contentMarkdown + '\'' +
                '}';
    }
}
