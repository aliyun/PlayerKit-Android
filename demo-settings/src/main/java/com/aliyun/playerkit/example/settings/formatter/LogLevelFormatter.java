package com.aliyun.playerkit.example.settings.formatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.playerkit.logging.LogHub;

/**
 * 日志等级格式化器
 * <p>
 * 负责日志等级（int）与显示字符串之间的转换。
 * 使用 LogHub 提供的转换方法。
 * </p>
 * <p>
 * Log Level Formatter
 * <p>
 * Responsible for conversion between log level (int) and display strings.
 * Uses conversion methods provided by LogHub.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class LogLevelFormatter implements ValueFormatter<Integer> {

    @NonNull
    @Override
    public String format(@Nullable Integer value) {
        if (value == null) {
            return LogHub.levelToString(getDefaultValue());
        }
        return LogHub.levelToString(value);
    }

    @Override
    public Integer parse(@NonNull String displayString) {
        return LogHub.stringToLevel(displayString);
    }

    @NonNull
    @Override
    public Integer getDefaultValue() {
        return LogHub.getLogLevel();
    }
}

