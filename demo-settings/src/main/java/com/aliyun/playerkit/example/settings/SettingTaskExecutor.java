package com.aliyun.playerkit.example.settings;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设置项异步任务执行器
 * <p>
 * 职责：提供统一的线程池用于处理设置项的异步加载任务。
 * </p>
 * <p>
 * Setting Task Executor
 * <p>
 * Responsibility: Provides a unified thread pool for handling asynchronous loading tasks for setting items.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public final class SettingTaskExecutor {

    private static final String THREAD_NAME_PREFIX = "PlayerKit-SettingEx-";

    private SettingTaskExecutor() {
        throw new UnsupportedOperationException("Cannot instantiate SettingTaskExecutor");
    }

    // 子线程池
    private static final ExecutorService sExecutor = new ThreadPoolExecutor(
            2, 2, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, THREAD_NAME_PREFIX + mCount.getAndIncrement());
                }
            }
    );

    // 主线程
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 在后台执行任务
     * <p>
     * Executes a task in the background.
     * </p>
     *
     * @param runnable 任务 / Task
     */
    public static void runOnBackground(Runnable runnable) {
        sExecutor.execute(runnable);
    }

    /**
     * 切换到主线程执行
     * <p>
     * Posts a task to the main thread.
     * </p>
     *
     * @param runnable 任务 / Task
     */
    public static void runOnMainThread(Runnable runnable) {
        sMainHandler.post(runnable);
    }
}
