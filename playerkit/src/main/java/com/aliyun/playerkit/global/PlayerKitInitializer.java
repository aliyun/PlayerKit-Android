package com.aliyun.playerkit.global;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.logging.LogHub;

/**
 * PlayerKit 自动初始化器
 * <p>
 * 通过 ContentProvider 机制实现无侵入式自动初始化。
 * Android 系统会在应用启动时自动创建并调用此 Provider，无需用户手动调用 AliPlayerKit.init()。
 * </p>
 * <p>
 * 这是 Firebase、LeakCanary 等主流 SDK 采用的初始化方案。
 * </p>
 * <p>
 * 注意：此 Provider 仅用于自动设置 setExtraData 等全局配置，
 * 完整的 AliPlayerKit 初始化仍建议用户手动调用 AliPlayerKit.init(Context)。
 * </p>
 *
 * @author keria
 * @date 2026/04/21
 */
public class PlayerKitInitializer extends ContentProvider {

    private static final String TAG = "PlayerKitInitializer";

    /**
     * 标记是否已通过 ContentProvider 自动初始化
     */
    private static volatile boolean autoInitialized = false;

    /**
     * 同步锁
     */
    private static final Object LOCK = new Object();

    /**
     * ContentProvider 的 onCreate 方法，在应用启动时自动调用
     * <p>
     * 此方法在主线程执行，应保持轻量。
     * </p>
     *
     * @return 始终返回 true
     */
    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context == null) {
            LogHub.w(TAG, "Context is null, cannot auto-initialize");
            return true;
        }

        // 获取 ApplicationContext 避免内存泄漏
        Context appContext = context.getApplicationContext();
        if (appContext == null) {
            LogHub.w(TAG, "ApplicationContext is null, using context");
            appContext = context;
        }

        performAutoInitialization(appContext);

        return true;
    }

    /**
     * 执行自动初始化
     * <p>
     * 仅设置 setExtraData，不执行完整的 AliPlayerKit 初始化。
     * 如果用户后续调用 AliPlayerKit.init()，会执行完整的初始化流程。
     * </p>
     *
     * @param context 应用上下文
     */
    private void performAutoInitialization(@NonNull Context context) {
        if (autoInitialized) {
            return;
        }

        synchronized (LOCK) {
            if (autoInitialized) {
                return;
            }

            try {
                LogHub.i(TAG, "Auto-initializing PlayerKit...");

                // 检查 AliPlayerKit 是否已经初始化
                if (!AliPlayerKit.isInitialized()) {
                    // 仅设置 setExtraData，这是最重要的埋点数据
                    // 不执行完整的初始化，避免重复配置
                    GlobalInitializer.setExtraDataOnly(context);
                    LogHub.i(TAG, "Auto-initialization completed: setExtraData called");
                } else {
                    LogHub.i(TAG, "AliPlayerKit already initialized, skipping auto-initialization");
                }

                autoInitialized = true;
            } catch (Exception e) {
                LogHub.e(TAG, "Failed to auto-initialize PlayerKit", e);
            }
        }
    }

    /**
     * 检查是否已通过 ContentProvider 自动初始化
     *
     * @return true 如果已自动初始化
     */
    public static boolean isAutoInitialized() {
        return autoInitialized;
    }

    // ==================== ContentProvider 必需方法（无实际操作）====================

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
