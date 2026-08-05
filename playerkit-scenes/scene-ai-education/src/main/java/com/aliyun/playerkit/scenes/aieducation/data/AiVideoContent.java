package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.ChapterInfo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 视频 AI 内容中心
 * <p>
 * 顶层容器，聚合一个视频的所有 AI 分析内容，包括摘要、章节目录和分段解析。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public class AiVideoContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频 ID
     */
    @NonNull
    private final String videoId;

    /**
     * 视频类型
     */
    @AiContentType
    private int type;

    /**
     * 视频摘要
     */
    @Nullable
    private AiSummary summary;

    /**
     * 视频章节目录
     */
    @NonNull
    private final List<ChapterInfo> chapters;

    /**
     * 视频分段解析
     */
    @NonNull
    private final List<AiSegment> segments;

    /**
     * Gson 反序列化专用
     */
    @SuppressWarnings("unused")
    private AiVideoContent() {
        this.videoId = "";
        this.chapters = Collections.emptyList();
        this.segments = Collections.emptyList();
    }

    /**
     * 构造函数
     *
     * @param videoId  视频 ID
     * @param type     视频类型
     * @param summary  视频摘要
     * @param chapters 视频章节目录
     * @param segments 视频分段解析
     */
    public AiVideoContent(@NonNull String videoId, @AiContentType int type, @Nullable AiSummary summary, @Nullable List<ChapterInfo> chapters, @Nullable List<AiSegment> segments) {
        this.videoId = videoId;
        this.type = type;
        this.summary = summary;
        this.chapters = chapters != null ? chapters : Collections.emptyList();
        this.segments = segments != null ? segments : Collections.emptyList();
    }

    /**
     * 获取视频 ID
     *
     * @return 视频 ID
     */
    @NonNull
    public String getVideoId() {
        return videoId;
    }

    /**
     * 获取视频类型
     *
     * @return 视频类型
     */
    @AiContentType
    public int getType() {
        return type;
    }

    /**
     * 获取视频摘要
     *
     * @return 视频摘要
     */
    @Nullable
    public AiSummary getSummary() {
        return summary;
    }

    /**
     * 获取视频章节目录
     *
     * @return 视频章节目录
     */
    @NonNull
    public List<ChapterInfo> getChapters() {
        return chapters;
    }

    /**
     * 获取视频分段解析
     *
     * @return 视频分段解析
     */
    @NonNull
    public List<AiSegment> getSegments() {
        return segments;
    }

    /**
     * 根据播放位置查找当前分段索引（二分查找，O(log n)）
     *
     * @param positionMs 当前播放位置（毫秒）
     * @return 当前分段索引，未找到返回 -1
     */
    public int findSegmentIndexByPosition(long positionMs) {
        List<AiSegment> list = segments;
        if (list.isEmpty()) return -1;

        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            AiSegment segment = list.get(mid);
            if (positionMs < segment.getStartMs()) {
                high = mid - 1;
            } else if (positionMs >= segment.getEndMs()) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    /**
     * 字符串表示
     *
     * @return 字符串表示
     */
    @NonNull
    @Override
    public String toString() {
        return "AiVideoContent{" +
                "videoId='" + videoId + '\'' +
                ", type=" + type +
                ", summary=" + summary +
                ", chapters=" + chapters +
                ", segments=" + segments +
                '}';
    }
}
