package com.aliyun.playerkit.example.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.aliyun.playerkit.example.R;
import com.aliyun.playerkit.utils.ToastUtils;

/**
 * Schema-based navigation router
 * 基于Schema的导航路由器
 *
 * @author keria
 * @date 2025/5/31
 * @brief Router utility for schema-based navigation
 */
public class SchemaRouter {
    /**
     * 根据Schema进行页面跳转
     * 该方法解析传入的Schema并启动对应的Activity
     *
     * @param context 应用上下文
     * @param schema  要跳转的Schema路径
     * @return 跳转是否成功
     */
    @SuppressLint("QueryPermissionsNeeded")
    public static boolean navigate(Context context, String schema) {
        // 检查Schema是否为空
        if (TextUtils.isEmpty(schema)) {
            return false;
        }

        try {
            // 创建Intent并解析Schema
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(schema));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有Activity可以处理该Intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            } else {
                // 无法处理该Schema，显示提示信息
                showSchemaNotSupportedMessage(context, schema);
                return false;
            }
        } catch (Exception e) {
            // 发生异常时显示提示信息
            showSchemaNotSupportedMessage(context, schema);
            return false;
        }
    }

    /**
     * 显示Schema不支持的提示信息
     *
     * @param context 应用上下文
     * @param schema  不支持的Schema路径
     */
    private static void showSchemaNotSupportedMessage(Context context, String schema) {
        String message = context.getString(R.string.schema_not_supported, schema);
        ToastUtils.showToastLong(message);
    }
}