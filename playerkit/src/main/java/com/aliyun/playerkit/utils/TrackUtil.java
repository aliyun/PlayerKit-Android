package com.aliyun.playerkit.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.data.TrackQuality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 视频轨道工具类
 * <p>
 * 提供视频清晰度轨道相关的工具方法，包括过滤视频轨道和查找最接近的分辨率。
 * 支持多视频轨播放（可变清晰度）场景，用于从音视频轨道列表中筛选出有效的视频轨道，
 * 并根据视频尺寸匹配最接近的标准分辨率。
 * </p>
 * <p>
 * Video Track Utility Class
 * <p>
 * Provides utility methods for video track quality, including filtering video tracks and finding the nearest resolution.
 * Supports multi-track video playback (variable quality) scenarios, used to filter valid video tracks from audio/video track lists,
 * and match the nearest standard resolution based on video dimensions.
 * </p>
 *
 * @author keria
 * @date 2024/9/12
 */
public final class TrackUtil {

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private TrackUtil() {
        throw new UnsupportedOperationException("Cannot instantiate TrackUtil");
    }

    /**
     * 未知分辨率标识
     * <p>
     * 当无法匹配到标准分辨率时返回此值。
     * </p>
     * <p>
     * Unknown Resolution Identifier
     * <p>
     * Returned when no standard resolution can be matched.
     * </p>
     */
    private static final String UNKNOWN_RESOLUTION = "Unknown";

    /**
     * 有效的视频轨道类型集合
     * <p>
     * 用于过滤出有效的视频轨道，包括 VIDEO 和 VOD 类型。
     * </p>
     * <p>
     * Valid Video Track Type Set
     * <p>
     * Used to filter valid video tracks, including VIDEO and VOD types.
     * </p>
     */
    private static final Set<Integer> VALID_TRACK_TYPES = Set.of(
            TrackQuality.TrackType.VIDEO,
            TrackQuality.TrackType.VOD
    );

    /**
     * 标准分辨率与对应宽高的映射表
     * <p>
     * Key: 分辨率标识（如 "720P"），Value: 宽高数组 [宽度, 高度]。
     * 支持从 144P 到 4320P 的标准分辨率。
     * </p>
     * <p>
     * Standard Resolution to Width/Height Mapping
     * <p>
     * Key: Resolution identifier (e.g., "720P"), Value: Width/Height array [width, height].
     * Supports standard resolutions from 144P to 4320P.
     * </p>
     */
    private static final Map<String, int[]> TRACK_QUALITY_RESOLUTIONS = new HashMap<>();

    static {
        TRACK_QUALITY_RESOLUTIONS.put("144P", new int[]{144, 256});
        TRACK_QUALITY_RESOLUTIONS.put("240P", new int[]{240, 426});
        TRACK_QUALITY_RESOLUTIONS.put("360P", new int[]{360, 640});
        TRACK_QUALITY_RESOLUTIONS.put("480P", new int[]{480, 854});
        TRACK_QUALITY_RESOLUTIONS.put("540P", new int[]{540, 960});
        TRACK_QUALITY_RESOLUTIONS.put("720P", new int[]{720, 1280});
        TRACK_QUALITY_RESOLUTIONS.put("1080P", new int[]{1080, 1920});
        TRACK_QUALITY_RESOLUTIONS.put("1440P", new int[]{1440, 2560});
        TRACK_QUALITY_RESOLUTIONS.put("2160P", new int[]{2160, 3840});
        TRACK_QUALITY_RESOLUTIONS.put("4320P", new int[]{4320, 7680});
    }

    /**
     * 过滤视频清晰度轨道列表
     * <p>
     * 从音视频轨道列表中筛选出有效的视频轨道。
     * </p>
     * <p>
     * Filter Video Track Quality List
     * <p>
     * Filters valid video tracks from the audio/video track list.
     * </p>
     *
     * @param trackQualityList 音视频轨道列表，可能为 null 或空列表
     * @return 过滤后的视频清晰度轨道列表，不会为 null，如果输入为空则返回空列表
     */
    @NonNull
    public static List<TrackQuality> filterVideoTrackQualityList(@Nullable List<TrackQuality> trackQualityList) {
        List<TrackQuality> videoTrackQualityList = new ArrayList<>(4);
        if (trackQualityList == null || trackQualityList.isEmpty()) {
            return videoTrackQualityList;
        }

        for (TrackQuality trackQuality : trackQualityList) {
            if (trackQuality == null) {
                continue;
            }

            int width = trackQuality.getWidth();
            int height = trackQuality.getHeight();
            if (width <= 0 || height <= 0) {
                continue;
            }

            // 过滤掉非视频轨
            if (!VALID_TRACK_TYPES.contains(trackQuality.getType())) {
                continue;
            }

            videoTrackQualityList.add(trackQuality);
        }

        return videoTrackQualityList;
    }

    /**
     * 查找最接近的标准分辨率
     * <p>
     * 根据视频轨道的宽度和高度，从标准分辨率列表中查找最接近的分辨率。
     * 使用曼哈顿距离（宽度差绝对值 + 高度差绝对值）来计算最接近的分辨率。
     * </p>
     * <p>
     * Find Nearest Resolution
     * <p>
     * Finds the nearest standard resolution from the standard resolution list based on the video track's width and height.
     * Uses Manhattan distance (absolute width difference + absolute height difference) to calculate the nearest resolution.
     * </p>
     *
     * @param trackQuality 视频轨道，可能为 null
     * @return 最接近的标准分辨率标识（如 "720P"、"1080P"），如果无法匹配则返回 "Unknown"
     */
    @NonNull
    public static String findNearestResolution(@Nullable TrackQuality trackQuality) {
        // 如果不是视频轨，则返回默认值
        if (trackQuality == null || !VALID_TRACK_TYPES.contains(trackQuality.getType())) {
            return UNKNOWN_RESOLUTION;
        }

        int width = trackQuality.getWidth();
        int height = trackQuality.getHeight();
        if (width <= 0 || height <= 0) {
            return UNKNOWN_RESOLUTION;
        }

        String nearestResolution = UNKNOWN_RESOLUTION;
        int minDifference = Integer.MAX_VALUE;

        for (Map.Entry<String, int[]> entry : TRACK_QUALITY_RESOLUTIONS.entrySet()) {
            int[] resolution = entry.getValue();
            int resWidth = resolution[0];
            int resHeight = resolution[1];

            // 计算曼哈顿距离（宽度差 + 高度差）
            int d1 = Math.abs(resWidth - width) + Math.abs(resHeight - height);
            int d2 = Math.abs(resWidth - height) + Math.abs(resHeight - width);
            int difference = Math.min(d1, d2);

            if (difference < minDifference) {
                minDifference = difference;
                nearestResolution = entry.getKey();
            }
        }

        return nearestResolution;
    }
}
