package com.aliyun.playerkit.scenes.aieducation.event;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.event.PlayerEvent;

/**
 * 章节相关事件定义
 * <p>
 * 用于 ChapterButtonSlot 和 ChapterContentPanelSlot 之间的通信。
 * </p>
 *
 * @author keria
 * @date 2026/07/03
 */
public final class ChapterEvents {

    private ChapterEvents() {
        throw new UnsupportedOperationException("Cannot instantiate ChapterEvents");
    }

    /**
     * 显示章节面板事件
     * <p>
     * 由 ChapterButtonSlot 发送，通知 ChapterContentPanelSlot 展开底部面板。
     * </p>
     */
    public static final class ShowPanel extends PlayerEvent {
        public ShowPanel(@NonNull String playerId) {
            super(playerId);
        }
    }

    /**
     * 隐藏章节面板事件
     * <p>
     * 由面板关闭操作发送，通知 ChapterButtonSlot 更新按钮状态。
     * </p>
     */
    public static final class HidePanel extends PlayerEvent {
        public HidePanel(@NonNull String playerId) {
            super(playerId);
        }
    }
}
