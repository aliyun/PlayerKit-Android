package com.aliyun.playerkit.examples.strategysystem;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.AliPlayerController;
import com.aliyun.playerkit.AliPlayerModel;
import com.aliyun.playerkit.AliPlayerView;
import com.aliyun.playerkit.data.VideoSource;
import com.aliyun.playerkit.data.VideoSourceFactory;
import com.aliyun.playerkit.scenes.common.SceneConstants;
import com.aliyun.playerkit.strategy.StrategyManager;
import com.aliyun.playerkit.utils.ToastUtils;

/**
 * 策略使用演示 Activity
 * <p>
 * 演示 AliPlayerKit 策略功能的使用方式，包括如何创建控制器、注册策略并绑定到视图组件。
 * </p>
 * <p>
 * Strategy System Example Activity
 * <p>
 * Demonstrates how to use AliPlayerKit strategies: create controller, register strategies
 * and attach them to the player view.
 * </p>
 *
 * @author keria
 * @date 2026/01/05
 */
public class StrategySystemExampleActivity extends AppCompatActivity {

    /**
     * 播放器视图组件
     */
    private AliPlayerView playerView;

    /**
     * 播放器控制器
     */
    private AliPlayerController playerController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strategy_system_example);

        initPlayerKit();
    }

    /**
     * 初始化播放器组件
     * <p>
     * 完成播放器的创建、策略注册和数据绑定。
     * </p>
     * <p>
     * Initialize AliPlayerKit components, including controller creation,
     * strategy registration and data binding.
     * </p>
     */
    private void initPlayerKit() {
        // 1. 获取播放器视图
        playerView = findViewById(R.id.v_player_kit);

        // 2. 创建播放器控制器
        playerController = new AliPlayerController(this);

        // 3. 获取策略管理器并注册策略
        StrategyManager strategyManager = playerController.getStrategyManager();

        // 注册记忆播放策略：自动记录播放进度，下次播放时恢复
        strategyManager.register(new ResumePlayStrategy());

        // 4. 准备播放数据
        VideoSource.VidAuthSource videoSource = VideoSourceFactory.createVidAuthSource(
                SceneConstants.LANDSCAPE_SAMPLE_VID,
                SceneConstants.LANDSCAPE_SAMPLE_PLAY_AUTH
        );
        // 在起播前，通过 ResumePlayStrategy 读取续播进度，并通过 startTime 设置起播时间
        long startTime = ResumePlayStrategy.getResumePosition(videoSource);

        AliPlayerModel model = new AliPlayerModel.Builder()
                .videoSource(videoSource)
                .videoTitle("Strategy Demo Video")
                .startTime(startTime) // 使用记忆播放位置作为起播时间
                .build();

        // 5. 配置播放器组件数据
        playerController.configure(model);

        // 6. 将播放控制器附加到播放器视图
        playerView.attach(playerController);

        // 显示提示
        ToastUtils.showToast(getString(R.string.resume_play_restored_toast));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 销毁播放器控制器，释放资源
        // 注意：Activity 销毁时必须销毁控制器，避免内存泄露
        // StrategyManager 会在控制器销毁时自动停止所有策略
        // Destroy player controller and release resources to avoid memory leaks.
        if (playerController != null) {
            playerController.destroy();
        }
    }
}
