package com.aliyun.playerkit.converter;

import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.playerkit.data.TrackQuality;

/**
 * 清晰度信息转换器
 * <p>
 * 负责将底层播放器 SDK 的 {@link TrackInfo} 转换为通用的 {@link TrackQuality} 数据模型。
 * </p>
 * <p>
 * Track Information Converter
 * <p>
 * Responsible for converting {@link TrackInfo} from the underlying player SDK to the generic {@link TrackQuality} data model.
 * </p>
 *
 * @author keria
 * @date 2025/12/26
 */
public class PlayerTrackConverter {

    /**
     * 将底层播放器 SDK 的 TrackInfo 转换为通用的 TrackQuality
     *
     * @param trackInfo 底层轨道信息
     * @return 通用轨道质量信息
     */
    public static TrackQuality convert(TrackInfo trackInfo) {
        if (trackInfo == null) {
            return null;
        }

        final @TrackQuality.TrackType int trackType;
        switch (trackInfo.getType()) {
            case TYPE_VIDEO:
                trackType = TrackQuality.TrackType.VIDEO;
                break;
            case TYPE_AUDIO:
                trackType = TrackQuality.TrackType.AUDIO;
                break;
            case TYPE_SUBTITLE:
                trackType = TrackQuality.TrackType.SUBTITLE;
                break;
            case TYPE_VOD:
                trackType = TrackQuality.TrackType.VOD;
                break;
            default:
                trackType = TrackQuality.TrackType.UNKNOWN;
                break;
        }

        return new TrackQuality(
                trackInfo.getIndex(),
                trackType,
                trackInfo.getVideoWidth(),
                trackInfo.getVideoHeight()
        );
    }
}