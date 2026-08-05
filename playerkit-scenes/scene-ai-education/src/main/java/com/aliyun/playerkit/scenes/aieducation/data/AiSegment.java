package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * AI 分段解析
 * <p>
 * 表示视频中的一个分段的 AI 分析结果。
 * 根据 {@link AiContentType}，可以包含纯文字摘要或 Markdown 图解。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class AiSegment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分段 ID
     */
    @NonNull
    private String id;

    /**
     * 分段开始时间，单位毫秒
     */
    private long startMs;

    /**
     * 分段结束时间，单位毫秒
     */
    private long endMs;

    /**
     * 分段标题
     */
    @NonNull
    private String title;

    /**
     * 纯文字摘要（TEXT_ANALYSIS 模式使用）
     */
    @Nullable
    private String summary;

    /**
     * Markdown 格式图解内容（GRAPHIC_ANALYSIS 模式使用）
     */
    @Nullable
    private String markdown;

    /**
     * Gson 反序列化专用
     */
    @SuppressWarnings("unused")
    private AiSegment() {
        this.id = "";
        this.title = "";
    }

    /**
     * 构造函数
     *
     * @param id       分段 ID
     * @param startMs  分段开始时间，单位毫秒
     * @param endMs    分段结束时间，单位毫秒
     * @param title    分段标题
     * @param summary  纯文字摘要（TEXT_ANALYSIS 模式使用）
     * @param markdown Markdown 格式图解内容（GRAPHIC_ANALYSIS 模式使用）
     */
    public AiSegment(@NonNull String id, long startMs, long endMs, @NonNull String title,
                     @Nullable String summary, @Nullable String markdown) {
        this.id = id;
        this.startMs = startMs;
        this.endMs = endMs;
        this.title = title;
        this.summary = summary;
        this.markdown = markdown;
    }

    /**
     * 获取分段 ID
     *
     * @return 分段 ID
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * 获取分段开始时间，单位毫秒
     *
     * @return 分段开始时间，单位毫秒
     */
    public long getStartMs() {
        return startMs;
    }

    /**
     * 获取分段结束时间，单位毫秒
     *
     * @return 分段结束时间，单位毫秒
     */
    public long getEndMs() {
        return endMs;
    }

    /**
     * 获取分段标题
     *
     * @return 分段标题
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * 获取纯文字摘要（TEXT_ANALYSIS 模式使用）
     *
     * @return 纯文字摘要（TEXT_ANALYSIS 模式使用）
     */
    @Nullable
    public String getSummary() {
        return summary;
    }

    /**
     * 获取 Markdown 格式图解内容（GRAPHIC_ANALYSIS 模式使用）
     *
     * @return Markdown 格式图解内容（GRAPHIC_ANALYSIS 模式使用）
     */
    @Nullable
    public String getMarkdown() {
        return markdown;
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    @NonNull
    @Override
    public String toString() {
        return "AiSegment{" +
                "id='" + id + '\'' +
                ", startMs=" + startMs +
                ", endMs=" + endMs +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", markdown='" + markdown + '\'' +
                '}';
    }
}
