package com.aliyun.playerkit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.aliyun.playerkit.logging.LogHub;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Android文件工具类
 * <p>
 * 提供文件和目录的常用操作方法，包括创建、删除、读写、校验等功能。
 * </p>
 * <p>
 * Android File Utility Class
 * <p>
 * Provides common file and directory operations including create, delete, read, write, checksum, etc.
 * </p>
 *
 * @author baorunchen
 * @date 2022/08/09
 */
public final class FileUtil {

    private static final String TAG = "FileUtil";

    /**
     * 默认缓冲区大小
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 文件大小单位
     */
    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 私有构造函数，防止实例化
     */
    private FileUtil() {
        throw new UnsupportedOperationException("Cannot instantiate FileUtil");
    }

    // ========== 文件和目录存在性检查 ==========

    /**
     * 检查文件是否存在
     * <p>
     * Check if file exists
     * </p>
     *
     * @param path 文件路径
     * @return true 如果文件存在，false 否则
     */
    public static boolean fileExists(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * 检查目录是否存在
     * <p>
     * Check if folder exists
     * </p>
     *
     * @param path 目录路径
     * @return true 如果目录存在，false 否则
     */
    public static boolean folderExists(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * 检查路径是否存在（文件或目录）
     * <p>
     * Check if path exists (file or directory)
     * </p>
     *
     * @param path 路径
     * @return true 如果路径存在，false 否则
     */
    public static boolean pathExists(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        return new File(path).exists();
    }

    // ========== 路径操作 ==========

    /**
     * 获取父目录的绝对路径
     * <p>
     * Get parent folder absolute path
     * </p>
     *
     * @param path 文件路径
     * @return 父目录的绝对路径，如果无法获取则返回空字符串
     */
    public static String getFolderPath(String path) {
        if (StringUtil.isEmpty(path)) {
            return StringUtil.EMPTY;
        }
        File file = new File(path);
        String parent = file.getAbsoluteFile().getParent();
        return StringUtil.nullToEmpty(parent);
    }

    /**
     * 获取文件名（不包含路径）
     * <p>
     * Get file name without path
     * </p>
     *
     * @param path 文件路径
     * @return 文件名
     */
    public static String getFileName(String path) {
        if (StringUtil.isEmpty(path)) {
            return StringUtil.EMPTY;
        }
        return new File(path).getName();
    }

    /**
     * 获取文件扩展名
     * <p>
     * Get file extension
     * </p>
     *
     * @param path 文件路径
     * @return 文件扩展名（不包含点号），如果没有扩展名则返回空字符串
     */
    public static String getFileExtension(String path) {
        if (StringUtil.isEmpty(path)) {
            return StringUtil.EMPTY;
        }

        String fileName = getFileName(path);
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return StringUtil.EMPTY;
    }

    /**
     * 获取不包含扩展名的文件名
     * <p>
     * Get file name without extension
     * </p>
     *
     * @param path 文件路径
     * @return 不包含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String path) {
        if (StringUtil.isEmpty(path)) {
            return StringUtil.EMPTY;
        }

        String fileName = getFileName(path);
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    // ========== 文件和目录创建 ==========

    /**
     * 安全创建目录
     * <p>
     * 如果目标是文件，则删除后创建目录
     * </p>
     * <p>
     * Create folder if not exists
     * <p>
     * If the target is a file, remove it and then create folder.
     * </p>
     *
     * @param path 目录路径
     * @return true 如果创建成功或目录已存在，false 否则
     */
    public static boolean safeCreateFolder(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        if (file.isFile()) {
            if (!file.delete()) {
                LogHub.w(TAG, "Failed to delete existing file: " + path);
                return false;
            }
        }

        if (!file.exists()) {
            return file.mkdirs();
        }

        return file.isDirectory();
    }

    /**
     * 安全创建文件
     * <p>
     * 如果目标是目录，则删除后创建文件
     * </p>
     * <p>
     * Create file if not exists
     * <p>
     * If the target is a folder, remove it and then create file.
     * </p>
     *
     * @param path 文件路径
     * @return true 如果创建成功或文件已存在，false 否则
     */
    public static boolean safeCreateFile(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }

        File file = new File(path);

        // 确保父目录存在
        String parentPath = getFolderPath(path);
        if (StringUtil.isNotEmpty(parentPath) && !safeCreateFolder(parentPath)) {
            return false;
        }

        if (file.isDirectory()) {
            if (!deleteRecursively(file)) {
                LogHub.w(TAG, "Failed to delete existing directory: " + path);
                return false;
            }
        }

        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                LogHub.e(TAG, "Failed to create file: " + path, e);
                return false;
            }
        }

        return file.isFile();
    }

    // ========== 存储路径获取 ==========

    /**
     * 获取内部缓存目录
     * <p>
     * Get internal cache folder
     * </p>
     *
     * @param context Android上下文
     * @return 内部缓存目录路径
     */
    public static String getInternalCacheFolder(Context context) {
        if (context == null) {
            return StringUtil.EMPTY;
        }
        return context.getCacheDir().getAbsolutePath();
    }

    /**
     * 获取外部缓存目录
     * <p>
     * Get external cache folder
     * </p>
     *
     * @param context Android上下文
     * @return 外部缓存目录路径，如果外部存储不可用则返回内部缓存目录
     */
    public static String getExternalCacheFolder(Context context) {
        if (context == null) {
            return StringUtil.EMPTY;
        }

        if (isExternalStorageAvailable()) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                return externalCacheDir.getAbsolutePath();
            }
        }

        return getInternalCacheFolder(context);
    }

    /**
     * 获取内部文件目录
     * <p>
     * Get internal file folder
     * </p>
     *
     * @param context Android上下文
     * @return 内部文件目录路径
     */
    public static String getInternalFileFolder(Context context) {
        if (context == null) {
            return StringUtil.EMPTY;
        }
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * 获取外部文件目录
     * <p>
     * Get external file folder
     * </p>
     *
     * @param context Android上下文
     * @return 外部文件目录路径，如果外部存储不可用则返回内部文件目录
     */
    public static String getExternalFileFolder(Context context) {
        if (context == null) {
            return StringUtil.EMPTY;
        }

        if (isExternalStorageAvailable()) {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                return externalFilesDir.getAbsolutePath();
            }
        }

        return getInternalFileFolder(context);
    }

    /**
     * 检查外部存储是否可用
     * <p>
     * Check if external storage is available
     * </p>
     *
     * @return true 如果外部存储可用，false 否则
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // ========== 路径组合 ==========

    /**
     * 组合路径
     * <p>
     * Combine paths
     * </p>
     *
     * @param paths 路径列表
     * @return 组合后的绝对路径
     */
    public static String combinePaths(String... paths) {
        if (paths == null || paths.length == 0) {
            return StringUtil.EMPTY;
        }

        File result = new File(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            if (StringUtil.isNotEmpty(paths[i])) {
                result = new File(result, paths[i]);
            }
        }
        return result.getAbsolutePath();
    }

    // ========== 文件校验 ==========

    /**
     * 计算文件的MD5校验和
     * <p>
     * Calculate MD5 checksum of file
     * </p>
     *
     * @param filePath 文件路径
     * @return MD5校验和字符串，如果计算失败则返回空字符串
     */
    public static String getMD5Checksum(String filePath) {
        if (!fileExists(filePath)) {
            return StringUtil.EMPTY;
        }

        try {
            byte[] hash = createChecksum(filePath, "MD5");
            if (hash == null) {
                return StringUtil.EMPTY;
            }

            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            LogHub.e(TAG, "Failed to calculate MD5 checksum for: " + filePath, e);
            return StringUtil.EMPTY;
        }
    }

    /**
     * 计算文件的SHA-256校验和
     * <p>
     * Calculate SHA-256 checksum of file
     * </p>
     *
     * @param filePath 文件路径
     * @return SHA-256校验和字符串，如果计算失败则返回空字符串
     */
    public static String getSHA256Checksum(String filePath) {
        if (!fileExists(filePath)) {
            return StringUtil.EMPTY;
        }

        try {
            byte[] hash = createChecksum(filePath, "SHA-256");
            if (hash == null) {
                return StringUtil.EMPTY;
            }

            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            LogHub.e(TAG, "Failed to calculate SHA-256 checksum for: " + filePath, e);
            return StringUtil.EMPTY;
        }
    }

    /**
     * 创建文件校验和
     * <p>
     * Create file checksum
     * </p>
     *
     * @param filePath  文件路径
     * @param algorithm 算法名称（如 "MD5", "SHA-256"）
     * @return 校验和字节数组
     * @throws Exception 如果计算失败
     */
    private static byte[] createChecksum(String filePath, String algorithm) throws Exception {
        if (!fileExists(filePath)) {
            return null;
        }

        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            int numRead;

            while ((numRead = fis.read(buffer)) != -1) {
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }

            return digest.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new Exception("Failed to create checksum", e);
        }
    }

    // ========== 文件列表 ==========

    /**
     * 列出所有文件
     * <p>
     * List all files
     * </p>
     *
     * @param file          文件或目录
     * @param includeFolder 是否包含目录
     * @return 文件列表，如果没有文件则返回空列表
     */
    public static List<File> listAllFiles(File file, boolean includeFolder) {
        List<File> files = new ArrayList<>();

        if (file == null || !file.exists()) {
            return files;
        }

        if (file.isFile()) {
            files.add(file);
        } else if (file.isDirectory()) {
            if (includeFolder) {
                files.add(file);
            }

            File[] filesArray = file.listFiles();
            if (filesArray != null) {
                for (File subFile : filesArray) {
                    files.addAll(listAllFiles(subFile, includeFolder));
                }
            }
        }

        return files;
    }

    /**
     * 列出指定目录下的所有文件（不包含子目录）
     * <p>
     * List files in directory (not recursive)
     * </p>
     *
     * @param dirPath 目录路径
     * @return 文件列表
     */
    public static List<File> listFiles(String dirPath) {
        List<File> files = new ArrayList<>();

        if (!folderExists(dirPath)) {
            return files;
        }

        File dir = new File(dirPath);
        File[] filesArray = dir.listFiles();
        if (filesArray != null) {
            for (File file : filesArray) {
                if (file.isFile()) {
                    files.add(file);
                }
            }
        }

        return files;
    }

    // ========== 文件读取 ==========

    /**
     * 从文件读取所有行
     * <p>
     * Read lines from file
     * </p>
     *
     * @param file 文件对象
     * @return 文件行列表，如果读取失败则返回空列表
     */
    public static List<String> readLinesFromFile(File file) {
        List<String> lines = new ArrayList<>();

        if (file == null || !file.exists() || !file.canRead()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file), DEFAULT_BUFFER_SIZE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to read lines from file: " + file.getAbsolutePath(), e);
        }

        return lines;
    }

    /**
     * 从文件读取文本内容
     * <p>
     * Read text from file
     * </p>
     *
     * @param file 文件对象
     * @return 文件内容，如果读取失败则返回null
     */
    public static String readTextFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file), DEFAULT_BUFFER_SIZE)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    sb.append('\n');
                }
                sb.append(line);
                firstLine = false;
            }
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to read text from file: " + file.getAbsolutePath(), e);
            return null;
        }

        return sb.toString();
    }

    /**
     * 从文件路径读取文本内容
     * <p>
     * Read text from file path
     * </p>
     *
     * @param filePath 文件路径
     * @return 文件内容，如果读取失败则返回null
     */
    public static String readTextFromFile(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        return readTextFromFile(new File(filePath));
    }

    /**
     * 读取文件为字节数组
     * <p>
     * Read file as byte array
     * </p>
     *
     * @param filePath 文件路径
     * @return 字节数组，如果读取失败则返回null
     */
    public static byte[] readBytesFromFile(String filePath) {
        if (!fileExists(filePath)) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(filePath); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to read bytes from file: " + filePath, e);
            return null;
        }
    }

    // ========== 图片操作 ==========

    /**
     * 从本地文件读取位图
     * <p>
     * Read bitmap from local file
     * </p>
     *
     * @param path 图片文件路径
     * @return 位图对象，如果读取失败则返回null
     */
    public static Bitmap readBitmapFromLocalFile(String path) {
        if (!fileExists(path)) {
            return null;
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            LogHub.e(TAG, "Failed to read bitmap from file: " + path, e);
            return null;
        }
    }

    /**
     * 获取图片尺寸信息
     * <p>
     * Get image dimensions
     * </p>
     *
     * @param path 图片文件路径
     * @return 包含宽度和高度的数组 [width, height]，如果获取失败则返回null
     */
    public static int[] getImageDimensions(String path) {
        if (!fileExists(path)) {
            return null;
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            if (options.outWidth > 0 && options.outHeight > 0) {
                return new int[]{options.outWidth, options.outHeight};
            }
        } catch (Exception e) {
            LogHub.e(TAG, "Failed to get image dimensions: " + path, e);
        }

        return null;
    }

    // ========== 文件写入 ==========

    /**
     * 写入文本到文件
     * <p>
     * Write text to file
     * </p>
     *
     * @param file    文件对象
     * @param content 文本内容
     * @param append  是否追加模式
     * @return true 如果写入成功，false 否则
     */
    public static boolean writeTextToFile(File file, String content, boolean append) {
        if (file == null || content == null) {
            return false;
        }

        if (!append && !safeCreateFile(file.getAbsolutePath())) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to write text to file: " + file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * 写入文本到文件路径
     * <p>
     * Write text to file path
     * </p>
     *
     * @param filePath 文件路径
     * @param content  文本内容
     * @param append   是否追加模式
     * @return true 如果写入成功，false 否则
     */
    public static boolean writeTextToFile(String filePath, String content, boolean append) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        return writeTextToFile(new File(filePath), content, append);
    }

    /**
     * 写入字节数组到文件
     * <p>
     * Write bytes to file
     * </p>
     *
     * @param filePath 文件路径
     * @param data     字节数据
     * @return true 如果写入成功，false 否则
     */
    public static boolean writeBytesToFile(String filePath, byte[] data) {
        if (StringUtil.isEmpty(filePath) || data == null) {
            return false;
        }

        if (!safeCreateFile(filePath)) {
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
            return true;
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to write bytes to file: " + filePath, e);
            return false;
        }
    }

    // ========== 文件复制和移动 ==========

    /**
     * 复制文件
     * <p>
     * Copy file
     * </p>
     *
     * @param sourcePath 源文件路径
     * @param destPath   目标文件路径
     * @return true 如果复制成功，false 否则
     */
    public static boolean copyFile(String sourcePath, String destPath) {
        if (!fileExists(sourcePath) || StringUtil.isEmpty(destPath)) {
            return false;
        }

        if (!safeCreateFile(destPath)) {
            return false;
        }

        try (InputStream in = new FileInputStream(sourcePath); OutputStream out = new FileOutputStream(destPath)) {

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return true;
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to copy file from " + sourcePath + " to " + destPath, e);
            return false;
        }
    }

    /**
     * 移动文件
     * <p>
     * Move file
     * </p>
     *
     * @param sourcePath 源文件路径
     * @param destPath   目标文件路径
     * @return true 如果移动成功，false 否则
     */
    public static boolean moveFile(String sourcePath, String destPath) {
        if (copyFile(sourcePath, destPath)) {
            return deleteFile(sourcePath);
        }
        return false;
    }

    // ========== 文件删除 ==========

    /**
     * 删除文件
     * <p>
     * Delete file
     * </p>
     *
     * @param filePath 文件路径
     * @return true 如果删除成功或文件不存在，false 否则
     */
    public static boolean deleteFile(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        }

        return false;
    }

    /**
     * 递归删除文件或目录
     * <p>
     * Delete file or directory recursively
     * </p>
     *
     * @param file 文件或目录
     * @return true 如果删除成功，false 否则
     */
    public static boolean deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }

        return file.delete();
    }

    /**
     * 递归删除文件或目录
     * <p>
     * Delete file or directory recursively
     * </p>
     *
     * @param path 文件或目录路径
     * @return true 如果删除成功，false 否则
     */
    public static boolean deleteRecursively(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        return deleteRecursively(new File(path));
    }

    // ========== 文件信息 ==========

    /**
     * 获取文件大小
     * <p>
     * Get file size
     * </p>
     *
     * @param filePath 文件路径
     * @return 文件大小（字节），如果文件不存在则返回-1
     */
    public static long getFileSize(String filePath) {
        if (!fileExists(filePath)) {
            return -1;
        }
        return new File(filePath).length();
    }

    /**
     * 格式化文件大小
     * <p>
     * Format file size
     * </p>
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        int unitIndex = 0;
        double fileSize = size;

        while (fileSize >= 1024 && unitIndex < SIZE_UNITS.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(fileSize) + " " + SIZE_UNITS[unitIndex];
    }

    /**
     * 获取目录大小
     * <p>
     * Get directory size
     * </p>
     *
     * @param dirPath 目录路径
     * @return 目录大小（字节），如果目录不存在则返回-1
     */
    public static long getDirectorySize(String dirPath) {
        if (!folderExists(dirPath)) {
            return -1;
        }

        return getDirectorySize(new File(dirPath));
    }

    /**
     * 获取目录大小
     * <p>
     * Get directory size
     * </p>
     *
     * @param dir 目录对象
     * @return 目录大小（字节）
     */
    private static long getDirectorySize(File dir) {
        long size = 0;

        if (dir.isFile()) {
            return dir.length();
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += getDirectorySize(file);
            }
        }

        return size;
    }

    // ========== 特殊用途方法 ==========

    /**
     * 将字节数据转储为RGBA文件
     * <p>
     * Dump bytes to RGBA file
     * </p>
     *
     * @param data     字节数据
     * @param width    图像宽度
     * @param height   图像高度
     * @param filePath 输出文件路径
     * @return true 如果转储成功，false 否则
     */
    public static boolean dumpBytesToRGBAFile(byte[] data, int width, int height, String filePath) {
        if (data == null || width <= 0 || height <= 0 || StringUtil.isEmpty(filePath)) {
            return false;
        }

        int expectedSize = width * height * 4;
        if (data.length < expectedSize) {
            LogHub.w(TAG, "Data size is smaller than expected: " + data.length + " < " + expectedSize);
            return false;
        }

        if (!safeCreateFile(filePath)) {
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            for (int i = 0; i < expectedSize; i += 4) {
                fos.write(data[i]);     // R
                fos.write(data[i + 1]); // G
                fos.write(data[i + 2]); // B
                fos.write(data[i + 3]); // A
            }
            return true;
        } catch (IOException e) {
            LogHub.e(TAG, "Failed to dump bytes to RGBA file: " + filePath, e);
            return false;
        }
    }

    /**
     * 清空目录内容（保留目录本身）
     * <p>
     * Clear directory contents (keep directory itself)
     * </p>
     *
     * @param dirPath 目录路径
     * @return true 如果清空成功，false 否则
     */
    public static boolean clearDirectory(String dirPath) {
        if (!folderExists(dirPath)) {
            return false;
        }

        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!deleteRecursively(file)) {
                    return false;
                }
            }
        }

        return true;
    }
}
