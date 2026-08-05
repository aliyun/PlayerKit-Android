package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * AI 内容类型
 * <p>
 * 定义 AI 分析的展示模式，决定面板如何渲染内容。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
@IntDef({AiContentType.CHAPTERS_ONLY, AiContentType.TEXT_ANALYSIS, AiContentType.GRAPHIC_ANALYSIS})
@Retention(RetentionPolicy.SOURCE)
public @interface AiContentType {
    /**
     * 仅展示章节列表
     */
    int CHAPTERS_ONLY = 0;

    /**
     * 文字解析模式：摘要 + 分段文字总结
     */
    int TEXT_ANALYSIS = 1;

    /**
     * 图解解析模式：摘要 + 分段 Markdown 图解
     */
    int GRAPHIC_ANALYSIS = 2;
}
