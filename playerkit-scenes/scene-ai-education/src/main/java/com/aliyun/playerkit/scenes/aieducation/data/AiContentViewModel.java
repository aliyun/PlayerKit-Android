package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.ChapterInfo;

import java.util.List;

/**
 * AI 内容面板展示层的 ViewModel（UI 唯一数据入口模型）。
 * <p>
 * 由 {@link AiContentAssembler#buildViewModel} 一次性组装生成，
 * 生命周期绑定面板展示期：PanelSlot 展示时惰性构建，隐藏时释放。
 * <p>
 * 承载三种数据：
 * <ul>
 *   <li>章节列表（ChaptersOnly）—— 视频章节时间点信息</li>
 *   <li>知识点列表（TextAnalysis）—— AI 提取的文本知识点条目</li>
 *   <li>视频摘要（GraphicAnalysis）—— AI 生成的图文摘要信息</li>
 * </ul>
 * UI 层根据 {@link #hasChapterData()} / {@link #hasSummaryData()} 决定 TAB 组合布局。
 *
 * @author keria
 * @date 2026/08/17
 * @see AiContentAssembler
 */
public class AiContentViewModel {

    /**
     * 章节列表，空列表表示无章节数据
     */
    @NonNull
    private final List<ChapterInfo> chapters;

    /**
     * 知识点条目列表，空列表表示无知识点数据
     */
    @NonNull
    private final List<KnowledgeInfo> knowledgeItems;

    /**
     * 视频摘要信息，{@code null} 表示无摘要数据
     */
    @Nullable
    private final SummaryInfo summaryInfo;

    /**
     * 构造 AI 内容 ViewModel。
     *
     * @param chapters       章节列表，不可为 null（无数据时传空列表）
     * @param knowledgeItems 知识点列表，不可为 null（无数据时传空列表）
     * @param summaryInfo    视频摘要信息，可为 null
     */
    public AiContentViewModel(@NonNull List<ChapterInfo> chapters, @NonNull List<KnowledgeInfo> knowledgeItems, @Nullable SummaryInfo summaryInfo) {
        this.chapters = chapters;
        this.knowledgeItems = knowledgeItems;
        this.summaryInfo = summaryInfo;
    }

    /**
     * 获取章节列表。
     *
     * @return 章节信息列表，非 null，可能为空
     */
    @NonNull
    public List<ChapterInfo> getChapters() {
        return chapters;
    }

    /**
     * 获取知识点条目列表。
     *
     * @return 知识点列表，非 null，可能为空
     */
    @NonNull
    public List<KnowledgeInfo> getKnowledgeItems() {
        return knowledgeItems;
    }

    /**
     * 获取视频摘要信息。
     *
     * @return 摘要信息，无摘要时返回 {@code null}
     */
    @Nullable
    public SummaryInfo getSummaryInfo() {
        return summaryInfo;
    }

    /**
     * 判断是否包含章节数据。
     * <p>
     * 用于驱动 TAB 组合规则：当存在章节数据时展示「章节」TAB。
     *
     * @return {@code true} 表示章节列表非空
     */
    public boolean hasChapterData() {
        return !chapters.isEmpty();
    }

    /**
     * 判断是否包含视频摘要数据。
     * <p>
     * 用于驱动 TAB 组合规则：当存在摘要数据时展示「摘要」TAB。
     *
     * @return {@code true} 表示摘要信息不为 null
     */
    public boolean hasSummaryData() {
        return summaryInfo != null;
    }

    @NonNull
    @Override
    public String toString() {
        return "AiContentViewModel{" +
                "chapters=" + chapters +
                ", knowledgeItems=" + knowledgeItems +
                ", summaryInfo=" + summaryInfo +
                '}';
    }
}
