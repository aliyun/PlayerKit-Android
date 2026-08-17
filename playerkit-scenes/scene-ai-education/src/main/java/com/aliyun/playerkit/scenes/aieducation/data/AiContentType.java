package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * AI 内容类型
 * <p>
 * 每个 TAB 关联一个 AiContentType，由类型决定该 TAB 的渲染样式
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
@IntDef({AiContentType.CHAPTERS_ONLY, AiContentType.TEXT_ANALYSIS, AiContentType.GRAPHIC_ANALYSIS})
@Retention(RetentionPolicy.SOURCE)
public @interface AiContentType {
    /**
     * 章节列表：缩略图 + 标题 + 时间。
     */
    int CHAPTERS_ONLY = 0;

    /**
     * 文字解析：知识点格式化列表或 Summary markdown。
     */
    int TEXT_ANALYSIS = 1;

    /**
     * 图解分析：ParagraphSummary + Tags 徽章 + MarkdownContent。
     */
    int GRAPHIC_ANALYSIS = 2;
}
