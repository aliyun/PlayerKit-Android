package com.aliyun.playerkit.scenes.common;

/**
 * 场景常量定义（示例数据）
 * <p>
 * 用于 Scene 示例/演示模块中的固定常量，例如示例 Vid 与播放凭证（PlayAuth）。
 * </p>
 *
 * <p><b>注意：</b></p>
 * <ul>
 *   <li>本类中的常量仅用于 Demo/测试用途，请勿在生产环境硬编码敏感信息。</li>
 *   <li>PlayAuth 具有时效性，过期后需要重新生成。</li>
 *   <li>当使用本地签名播放凭证（JWTPlayAuth）播放时，客户端播放器 SDK 版本需要 ≥ 7.10.0。</li>
 * </ul>
 * <p>
 * Scene constants (sample data).
 * <p>
 * Holds fixed constants for Scene demo, such as a sample vid and sample PlayAuth token.
 * Do NOT hardcode sensitive values in production.
 * </p>
 *
 * @author keria
 * @date 2025/12/25
 */
public final class SceneConstants {

    // 私有构造函数，防止实例化
    private SceneConstants() {
        throw new UnsupportedOperationException("Cannot instantiate SceneConstants");
    }

    // =========================
    // 横屏示例（Landscape Sample）
    // =========================

    /**
     * 横屏示例视频 Vid（Video ID）
     * <p>
     * 用于获取横屏视频播放地址（仅示例用途）。
     * </p>
     * <p>
     * Sample landscape video id (Vid) used to fetch playback URL (demo only).
     * </p>
     */
    public static final String LANDSCAPE_SAMPLE_VID = "004fc90fd71d71f0bf184531958c0402";

    /**
     * 横屏示例播放凭证（PlayAuth / JWTPlayAuth）
     * <p>
     * 用于横屏视频播放鉴权（仅示例用途）。该凭证通常具有时效性，过期后需要重新获取。
     * </p>
     * <p>
     * SDK 版本要求：当使用本地签名播放凭证（JWTPlayAuth）播放时，
     * 客户端播放器 SDK 版本需要 ≥ 7.10.0，
     * 否则可能无法完成播放鉴权。
     * </p>
     * <p>
     * Sample PlayAuth token (PlayAuth / JWTPlayAuth) for landscape video (demo only).
     * JWTPlayAuth requires player SDK version >= 7.10.0.
     * </p>
     */
    public static final String LANDSCAPE_SAMPLE_PLAY_AUTH = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImFwcC0xMDAwMDAwIiwidmlkZW9JZCI6IjAwNGZjOTBmZDcxZDcxZjBiZjE4NDUzMTk1OGMwNDAyIiwiY3VycmVudFRpbWVTdGFtcCI6MTc2NjEzMTE5MTYxMywiZXhwaXJlVGltZVN0YW1wIjoxOTIzODExMTkxNjEzLCJyZWdpb25JZCI6ImNuLXNoYW5naGFpIiwicGxheUNvbnRlbnRJbmZvIjp7ImZvcm1hdHMiOiJtM3U4Iiwic3RyZWFtVHlwZSI6InZpZGVvIiwiYXV0aFRpbWVvdXQiOjE4MDB9fQ.CjqZA-6okJb2PxOZr0Jjai9gWwvaNdG-bk3LWBMzhdc";

    // =========================
    // 竖屏示例（Portrait Sample）
    // =========================

    /**
     * 竖屏示例视频 Vid（Video ID）
     * <p>
     * 用于获取竖屏视频播放地址（仅示例用途）。
     * </p>
     * <p>
     * Sample portrait video id (Vid) used to fetch playback URL (demo only).
     * </p>
     */
    public static final String PORTRAIT_SAMPLE_VID = "00e9b526d0d671f085715017f1e90402";

    /**
     * 竖屏示例播放凭证（PlayAuth / JWTPlayAuth）
     * <p>
     * 用于竖屏视频播放鉴权（仅示例用途）。该凭证通常具有时效性，过期后需要重新获取。
     * </p>
     * <p>
     * SDK 版本要求：当使用本地签名播放凭证（JWTPlayAuth）播放时，
     * 客户端播放器 SDK 版本需要 ≥ 7.10.0，
     * 否则可能无法完成播放鉴权。
     * </p>
     * <p>
     * Sample PlayAuth token (PlayAuth / JWTPlayAuth) for portrait video (demo only).
     * JWTPlayAuth requires player SDK version >= 7.10.0.
     * </p>
     */
    public static final String PORTRAIT_SAMPLE_PLAY_AUTH = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImFwcC0xMDAwMDAwIiwidmlkZW9JZCI6IjAwZTliNTI2ZDBkNjcxZjA4NTcxNTAxN2YxZTkwNDAyIiwiY3VycmVudFRpbWVTdGFtcCI6MTc2ODE4ODIzNzAwNiwiZXhwaXJlVGltZVN0YW1wIjoxOTI1ODY4MjM3MDA2LCJyZWdpb25JZCI6ImNuLXNoYW5naGFpIiwicGxheUNvbnRlbnRJbmZvIjp7ImZvcm1hdHMiOiJtM3U4Iiwic3RyZWFtVHlwZSI6InZpZGVvIiwiYXV0aFRpbWVvdXQiOjE4MDB9fQ.AwXM5c8EsLJbhTPoNbEiB3uFVkl7heukuFbntHmC7no";

    /**
     * 示例视频播放地址
     * <p>
     * 用于视频播放（仅示例用途）。
     * </p>
     * <p>
     * 注意：
     * 1. 该地址随时可能过期，请谨慎使用。
     * 2. 为了安全起见，并获得更强大的媒资管理能力，建议使用 Vid 进行播放。
     * </p>
     *
     * <p>
     * Sample video playback URL (demo only).
     * </p>
     * <p>
     * Notes:
     * 1. This URL may expire at any time; use it with caution.
     * 2. For security reasons and more powerful media asset management capabilities,
     * it is recommended to use Vid for playback.
     * </p>
     */
    public static final String SAMPLE_VIDEO_URL = "https://alivc-demo-vod.aliyuncs.com/6b357371ef3c45f4a06e2536fd534380/53733986bce75cfc367d7554a47638c0-fd.mp4";
}
