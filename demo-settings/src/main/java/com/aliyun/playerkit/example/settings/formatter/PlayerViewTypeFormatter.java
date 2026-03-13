package com.aliyun.playerkit.example.settings.formatter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.AliPlayerKit;
import com.aliyun.playerkit.data.PlayerViewType;
import com.aliyun.playerkit.example.settings.R;

/**
 * 播放器视图类型格式化器
 * <p>
 * 负责 PlayerViewType 枚举与显示字符串之间的转换。
 * </p>
 * <p>
 * Player View Type Formatter
 * <p>
 * Responsible for conversion between PlayerViewType enum and display strings.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class PlayerViewTypeFormatter implements ValueFormatter<PlayerViewType> {

    private final Context context;

    public PlayerViewTypeFormatter() {
        this.context = AliPlayerKit.getContext();
    }

    @NonNull
    @Override
    public String format(@Nullable PlayerViewType value) {
        switch (value) {
            case DISPLAY_VIEW:
                return context.getString(R.string.setting_view_type_display_view);
            case SURFACE_VIEW:
                return context.getString(R.string.setting_view_type_surface_view);
            case TEXTURE_VIEW:
                return context.getString(R.string.setting_view_type_texture_view);
            default:
                return context.getString(R.string.setting_view_type_display_view);
        }
    }

    @Override
    public PlayerViewType parse(@NonNull String displayString) {
        String displayView = context.getString(R.string.setting_view_type_display_view);
        String surfaceView = context.getString(R.string.setting_view_type_surface_view);
        String textureView = context.getString(R.string.setting_view_type_texture_view);

        if (displayString.equals(displayView)) {
            return PlayerViewType.DISPLAY_VIEW;
        } else if (displayString.equals(surfaceView)) {
            return PlayerViewType.SURFACE_VIEW;
        } else if (displayString.equals(textureView)) {
            return PlayerViewType.TEXTURE_VIEW;
        } else {
            return getDefaultValue();
        }
    }

    @NonNull
    @Override
    public PlayerViewType getDefaultValue() {
        return AliPlayerKit.getPlayerViewType();
    }
}
