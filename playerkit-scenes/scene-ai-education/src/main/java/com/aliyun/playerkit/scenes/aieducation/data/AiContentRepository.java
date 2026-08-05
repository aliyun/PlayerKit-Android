package com.aliyun.playerkit.scenes.aieducation.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerModel;

/**
 * AI 内容数据仓库接口
 * <p>
 * 抽象 AI 内容的数据获取方式，由 Activity 实现并注入到 Slot 中。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public interface AiContentRepository {

    /**
     * 加载视频 AI 内容
     *
     * @param model 播放数据模型
     * @return AI 内容数据，如果无法加载返回 null
     */
    @Nullable
    AiVideoContent load(@NonNull AliPlayerModel model);
}
