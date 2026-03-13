package com.aliyun.playerkit.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogHub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Bitmap 工具类
 * 用于将 Bitmap 保存为本地图片文件
 *
 * @author keria
 * @date 2025/12/23
 */
public final class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    private static final String DEFAULT_IMAGE_PREFIX = "IMG_";

    private static final int MIN_QUALITY = 0;
    private static final int MAX_QUALITY = 100;
    private static final int DEFAULT_QUALITY = 100;

    /**
     * 私有构造函数，防止实例化
     * <p>
     * Private constructor to prevent instantiation
     * </p>
     */
    private BitmapUtil() {
        throw new UnsupportedOperationException("Cannot instantiate BitmapUtil");
    }

    /**
     * 保存 Bitmap 到指定文件路径
     * 注意：Android 10+ 可能需要额外的存储权限
     *
     * @param bitmap   要保存的 Bitmap
     * @param filePath 文件路径
     * @return 保存是否成功
     */
    public static boolean saveBitmapToFile(@NonNull Bitmap bitmap, @NonNull String filePath) {
        return saveBitmapToFile(bitmap, filePath, Bitmap.CompressFormat.JPEG, DEFAULT_QUALITY);
    }

    /**
     * 保存 Bitmap 到指定文件路径
     *
     * @param bitmap   要保存的 Bitmap
     * @param filePath 文件路径
     * @param format   压缩格式
     * @param quality  压缩质量 (0-100)
     * @return 保存是否成功
     */
    public static boolean saveBitmapToFile(@NonNull Bitmap bitmap, @NonNull String filePath, @NonNull Bitmap.CompressFormat format, int quality) {
        // 参数安全检查
        if (!isValidBitmap(bitmap)) {
            LogHub.e(TAG, "Invalid bitmap: bitmap is null, recycled, or has invalid dimensions");
            return false;
        }

        if (TextUtils.isEmpty(filePath)) {
            LogHub.e(TAG, "File path cannot be null or empty");
            return false;
        }

        if (format == null) {
            LogHub.e(TAG, "Bitmap.CompressFormat cannot be null");
            return false;
        }

        // 质量参数校验
        quality = validateQuality(quality);

        try {
            File file = new File(filePath);

            // 检查文件路径是否有效
            if (!isValidFilePath(file)) {
                LogHub.e(TAG, "Invalid file path: " + filePath);
                return false;
            }

            // 创建父目录
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    LogHub.e(TAG, "Failed to create parent directories: " + parentDir.getAbsolutePath());
                    return false;
                }
            }

            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                // 再次检查 bitmap 是否仍然有效
                if (!isValidBitmap(bitmap)) {
                    LogHub.e(TAG, "Bitmap became invalid during save operation");
                    return false;
                }

                boolean success = bitmap.compress(format, quality, outputStream);
                outputStream.flush();

                if (success) {
                    LogHub.d(TAG, "Bitmap saved successfully to: " + filePath);
                } else {
                    LogHub.e(TAG, "Failed to compress bitmap");
                    // 删除失败的文件
                    if (file.exists() && !file.delete()) {
                        LogHub.w(TAG, "Failed to delete incomplete file: " + filePath);
                    }
                }

                return success;

            } catch (IOException e) {
                LogHub.e(TAG, "Error writing bitmap to file: " + filePath, e);
                // 删除失败的文件
                if (file.exists() && !file.delete()) {
                    LogHub.w(TAG, "Failed to delete incomplete file: " + filePath);
                }
                return false;
            }

        } catch (SecurityException e) {
            LogHub.e(TAG, "Security exception: insufficient permissions for file: " + filePath, e);
            return false;
        } catch (Exception e) {
            LogHub.e(TAG, "Error saving bitmap to file", e);
            return false;
        }
    }

    /**
     * 生成默认文件名
     */
    @NonNull
    public static String generateFileName(@NonNull Bitmap.CompressFormat format) {
        if (format == null) {
            format = Bitmap.CompressFormat.JPEG; // 默认格式
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String suffix = getFileSuffix(format);
        return DEFAULT_IMAGE_PREFIX + timestamp + suffix;
    }

    /**
     * 获取文件后缀
     */
    @NonNull
    private static String getFileSuffix(@NonNull Bitmap.CompressFormat format) {
        switch (format) {
            case PNG:
                return ".png";
            case WEBP:
                return ".webp";
            case JPEG:
            default:
                return ".jpg";
        }
    }

    /**
     * 检查 Bitmap 是否有效
     */
    public static boolean isValidBitmap(@Nullable Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 0 && bitmap.getHeight() > 0;
    }

    /**
     * 验证并修正质量参数
     */
    private static int validateQuality(int quality) {
        if (quality < MIN_QUALITY) {
            LogHub.w(TAG, "Quality " + quality + " is below minimum, using " + MIN_QUALITY);
            return MIN_QUALITY;
        }
        if (quality > MAX_QUALITY) {
            LogHub.w(TAG, "Quality " + quality + " is above maximum, using " + MAX_QUALITY);
            return MAX_QUALITY;
        }
        return quality;
    }

    /**
     * 检查文件路径是否有效
     */
    private static boolean isValidFilePath(@NonNull File file) {
        try {
            // 检查路径是否为绝对路径
            if (!file.isAbsolute()) {
                LogHub.e(TAG, "File path must be absolute");
                return false;
            }

            // 检查父目录是否可写（如果存在）
            File parentDir = file.getParentFile();
            if (parentDir != null && parentDir.exists() && !parentDir.canWrite()) {
                LogHub.e(TAG, "Parent directory is not writable: " + parentDir.getAbsolutePath());
                return false;
            }

            return true;
        } catch (Exception e) {
            LogHub.e(TAG, "Error validating file path", e);
            return false;
        }
    }
}
