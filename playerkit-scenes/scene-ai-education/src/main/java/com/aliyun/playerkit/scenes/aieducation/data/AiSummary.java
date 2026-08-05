package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * 视频级 AI 摘要
 * <p>
 * 包含 AI 生成的视频标题、整体描述和标签，对应 Summarize 能力的产出。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class AiSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频标题
     */
    @Nullable
    private String title;

    /**
     * 视频描述
     */
    @Nullable
    private String description;

    /**
     * 视频标签
     */
    @Nullable
    private List<String> tags;

    /**
     * Gson 反序列化专用
     */
    @SuppressWarnings("unused")
    private AiSummary() {
    }

    /**
     * 构造函数
     *
     * @param title       视频标题
     * @param description 视频描述
     * @param tags        视频标签
     */
    public AiSummary(@Nullable String title, @Nullable String description, @Nullable List<String> tags) {
        this.title = title;
        this.description = description;
        this.tags = tags;
    }

    /**
     * 获取视频标题
     *
     * @return 视频标题
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * 获取视频描述
     *
     * @return 视频描述
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * 获取视频标签
     *
     * @return 视频标签
     */
    @Nullable
    public List<String> getTags() {
        return tags;
    }

    /**
     * 获取字符串表示
     *
     * @return 字符串表示
     */
    @NonNull
    @Override
    public String toString() {
        return "AiSummary{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                '}';
    }
}
