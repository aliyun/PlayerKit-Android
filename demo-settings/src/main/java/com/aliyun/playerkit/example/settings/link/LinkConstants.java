package com.aliyun.playerkit.example.settings.link;

import com.aliyun.playerkit.example.settings.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 链接配置页面常量与初始化数据
 * <p>
 * 集中定义所有可配置的业务项常量（存储键名），并提供默认配置项列表的构建方法。
 * 所有链接配置项的键名都在此统一管理，便于维护和扩展。
 * </p>
 * <p>
 * Link Configuration Constants and Initialization Data
 * <p>
 * Centralizes the definition of all configurable business item constants (storage key names),
 * and provides methods for building default configuration item lists. All link configuration
 * item key names are managed here uniformly for easy maintenance and extension.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public final class LinkConstants {

    /**
     * 视频 Direct URL 地址存储键
     * <p>
     * 用于存储视频的直链播放地址，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_URL = "key_video_url";

    /**
     * 视频 ID (Vid) 存储键
     * <p>
     * 用于存储阿里云视频点播服务的视频 ID，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_VID = "key_video_vid";

    /**
     * Vid 播放凭证 (PlayAuth) 存储键
     * <p>
     * 用于存储视频点播服务的播放凭证，配合 Vid 使用，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_PLAY_AUTH = "key_video_play_auth";

    /**
     * 直播地址存储键
     * <p>
     * 用于存储直播流的播放地址，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_LIVE_URL = "key_video_live_url";

    /**
     * VidSts 视频 ID 存储键
     * <p>
     * 用于存储 VidSts 类型的视频 ID，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_VID_STS_VID = "key_video_vid_sts_vid";

    /**
     * VidSts 访问密钥 ID 存储键
     * <p>
     * 用于存储 VidSts 类型的访问密钥 ID，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_VID_STS_ACCESS_KEY_ID = "key_video_vid_sts_access_key_id";

    /**
     * VidSts 访问密钥密文存储键
     * <p>
     * 用于存储 VidSts 类型的访问密钥密文，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_VID_STS_ACCESS_KEY_SECRET = "key_video_vid_sts_access_key_secret";

    /**
     * VidSts 安全令牌存储键
     * <p>
     * 用于存储 VidSts 类型的安全令牌，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_VID_STS_SECURITY_TOKEN = "key_video_vid_sts_security_token";

    /**
     * VidSts 区域信息存储键
     * <p>
     * 用于存储 VidSts 类型的区域信息，类型为 String。
     * </p>
     */
    public static final String KEY_VIDEO_VID_STS_REGION = "key_video_vid_sts_region";

    /**
     * 私有构造函数
     * <p>
     * 防止实例化，此类为工具类，只提供静态方法和常量。
     * </p>
     */
    private LinkConstants() {
        throw new UnsupportedOperationException("Cannot instantiate LinkConstants");
    }

    /**
     * 构建默认配置项列表
     * <p>
     * 创建包含所有可配置链接项的列表，每个配置项包含名称资源 ID 和存储键名。
     * 列表顺序决定了在 UI 中的显示顺序。
     * </p>
     *
     * @return 默认配置项列表
     */
    public static List<LinkItem> buildDefaultItems() {
        List<LinkItem> items = new ArrayList<>();
        items.add(new LinkItem(R.string.setting_link_video_url, KEY_VIDEO_URL));
        items.add(new LinkItem(R.string.setting_link_video_vid, KEY_VIDEO_VID));
        items.add(new LinkItem(R.string.setting_link_video_play_auth, KEY_VIDEO_PLAY_AUTH));
        items.add(new LinkItem(R.string.setting_link_video_live_url, KEY_VIDEO_LIVE_URL));
        items.add(new LinkItem(R.string.setting_link_video_vid_sts_vid, KEY_VIDEO_VID_STS_VID));
        items.add(new LinkItem(R.string.setting_link_video_vid_sts_access_key_id, KEY_VIDEO_VID_STS_ACCESS_KEY_ID));
        items.add(new LinkItem(R.string.setting_link_video_vid_sts_access_key_secret, KEY_VIDEO_VID_STS_ACCESS_KEY_SECRET));
        items.add(new LinkItem(R.string.setting_link_video_vid_sts_security_token, KEY_VIDEO_VID_STS_SECURITY_TOKEN));
        items.add(new LinkItem(R.string.setting_link_video_vid_sts_region, KEY_VIDEO_VID_STS_REGION));
        return items;
    }
}
