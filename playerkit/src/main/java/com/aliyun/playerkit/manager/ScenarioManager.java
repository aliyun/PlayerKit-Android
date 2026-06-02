package com.aliyun.playerkit.manager;

import androidx.annotation.NonNull;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.data.SceneType;
import com.aliyun.playerkit.data.SourceType;
import com.aliyun.playerkit.logging.LogHub;

/**
 * 播放器能力识别管理
 * <p>
 * 框架内部管理类，统一负责能力识别数据生成，用于播放体验优化和问题诊断。
 * 提供两个层级的能力标识：
 * <ul>
 *     <li>全局组件身份（{@link #buildGlobalExtraData()}）：标识「这是哪个组件」，
 *         由 {@link GlobalManager} 在初始化阶段
 *         写入 AliPlayerGlobalSettings.SET_EXTRA_DATA。</li>
 *     <li>实例级能力标识（{@link #buildScenarioFlag(int, int)}）：标识「这个播放器实例
 *         播什么、用什么源」，由 {@link com.aliyun.playerkit.player.MediaPlayer} 在
 *         {@code setDataSource} 内部写入 AliPlayer.SCENARIO_FLAG。</li>
 * </ul>
 * </p>
 * <p>
 * Player Capability Recognition Manager
 * <p>
 * Internal management class responsible for generating capability recognition data,
 * used for playback experience optimization and issue diagnosis.
 * </p>
 *
 * @author keria
 * @date 2026/05/12
 */
public final class ScenarioManager {

    private static final String TAG = "ScenarioManager";

    // ==================== 数据协议常量 ====================

    /**
     * 组件身份场景标识
     * <p>
     * 用于全局识别本组件的来源
     * </p>
     */
    private static final String SCENE_PLAYER_KIT = "player-kit";

    /**
     * 平台标识
     * <p>
     * Android 端在全局能力识别中加入 platform 字段，便于跨端聚合统计区分。
     * </p>
     */
    private static final String PLATFORM_ANDROID = "android";

    /**
     * 未知枚举值降级协议值
     * <p>
     * 当 sceneType / sourceType 超出已知枚举范围时（通常是新增枚举值后未同步更新 PROTOCOL_VALUES 数组），
     * 返回本值作为安全降级，同时打出 ERROR 日志供 QA / 灰度阶段捕获。
     * </p>
     */
    private static final String UNKNOWN_PROTOCOL_VALUE = "unknown";

    // ---- 枚举协议值映射 ----

    /**
     * {@link SceneType} 协议值映射
     * <p>
     * 索引与 {@link SceneType} int 常量严格一一对应（VOD=0, LIVE=1, VIDEO_LIST=2, RESTRICTED=3, MINIMAL=4）。
     * </p>
     * <p>
     * <strong>维护约束</strong>：新增 {@link SceneType} 枚举值时，<strong>必须</strong>在本数组末尾
     * 同步追加协议值，且 int 常量需从 0 开始连续递增。
     * </p>
     */
    private static final String[] SCENE_PROTOCOL_VALUES = {
            "vod",          // VOD
            "live",         // LIVE
            "list-player",  // VIDEO_LIST
            "restricted",   // RESTRICTED
            "minimal",      // MINIMAL
    };

    /**
     * {@link SourceType} 协议值映射
     * <p>
     * 索引与 {@link SourceType} int 常量严格一一对应（VID_AUTH=0, VID_STS=1, URL=2）。
     * </p>
     * <p>
     * <strong>维护约束</strong>：新增 {@link SourceType} 枚举值时，<strong>必须</strong>在本数组末尾
     * 同步追加协议值，且 int 常量需从 0 开始连续递增。
     * </p>
     */
    private static final String[] SOURCE_PROTOCOL_VALUES = {
            "vid-auth",  // VID_AUTH
            "vid-sts",   // VID_STS
            "url",       // URL
    };

    // ==================== 全局能力识别 ====================

    /**
     * 生成全局能力识别数据（组件身份）
     * <p>
     * 在 SDK 全局初始化阶段写入 AliPlayer 的 SET_EXTRA_DATA，用于标识本组件。
     * </p>
     * <p>
     * <strong>请勿覆盖</strong>：该数据用于播放体验优化与问题诊断，覆盖后会丢失组件来源信息。
     * </p>
     * <p>
     * Build global capability recognition data (component identity).
     * </p>
     *
     * @return 形如 {@code {"scene":"player-kit","platform":"android","version":"x.y.z"}} 的 JSON 字符串
     */
    @NonNull
    public static String buildGlobalExtraData() {
        return "{\"scene\":\"" + SCENE_PLAYER_KIT + "\",\"platform\":\"" + PLATFORM_ANDROID + "\",\"version\":\"" + AliPlayerKit.PLAYER_KIT_VERSION + "\"}";
    }

    // ==================== 实例级能力识别 ====================

    /**
     * 生成实例级能力识别数据
     * <p>
     * 基于当前播放场景与视频资源类型生成识别 JSON，用于播放体验优化与问题诊断。
     * </p>
     * <p>
     * Build instance-level capability recognition data based on the current
     * scene and source type.
     * </p>
     *
     * @param sceneType  播放场景类型，参见 {@link SceneType}
     * @param sourceType 视频资源类型，参见 {@link SourceType}
     * @return 形如 {@code {"scene":"vod","source":"url"}} 的 JSON 字符串。如某一枚举值超出已知范围，
     * 对应字段会降级为 {@code "unknown"} 并记录 ERROR 日志，不会抛出异常。
     */
    @NonNull
    public static String buildScenarioFlag(@SceneType int sceneType, @SourceType int sourceType) {
        return "{\"scene\":\"" + sceneValue(sceneType) + "\",\"source\":\"" + sourceValue(sourceType) + "\"}";
    }

    // ==================== 私有辅助方法 ====================

    /**
     * SceneType 协议值查找
     * <p>
     * 以枚举 int 作为索引从 {@link #SCENE_PROTOCOL_VALUES} 取出协议值。越界时降级为
     * {@link #UNKNOWN_PROTOCOL_VALUE} 并记录 ERROR 日志，以避免能力识别路径影响播放主链路。
     * </p>
     */
    @NonNull
    private static String sceneValue(@SceneType int sceneType) {
        if (sceneType < 0 || sceneType >= SCENE_PROTOCOL_VALUES.length) {
            LogHub.e(TAG, "Unknown SceneType: " + sceneType + ", please update ScenarioManager.SCENE_PROTOCOL_VALUES");
            return UNKNOWN_PROTOCOL_VALUE;
        }
        return SCENE_PROTOCOL_VALUES[sceneType];
    }

    /**
     * SourceType 协议值查找
     * <p>
     * 以枚举 int 作为索引从 {@link #SOURCE_PROTOCOL_VALUES} 取出协议值。越界时降级为
     * {@link #UNKNOWN_PROTOCOL_VALUE} 并记录 ERROR 日志，以避免能力识别路径影响播放主链路。
     * </p>
     */
    @NonNull
    private static String sourceValue(@SourceType int sourceType) {
        if (sourceType < 0 || sourceType >= SOURCE_PROTOCOL_VALUES.length) {
            LogHub.e(TAG, "Unknown SourceType: " + sourceType + ", please update ScenarioManager.SOURCE_PROTOCOL_VALUES");
            return UNKNOWN_PROTOCOL_VALUE;
        }
        return SOURCE_PROTOCOL_VALUES[sourceType];
    }

    /**
     * 私有构造函数，防止实例化
     */
    private ScenarioManager() {
        throw new UnsupportedOperationException("Cannot instantiate ScenarioManager");
    }
}
