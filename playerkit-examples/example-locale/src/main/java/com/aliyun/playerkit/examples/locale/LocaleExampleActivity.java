package com.aliyun.playerkit.examples.locale;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.playerkit.locale.PlayerLocale;

/**
 * 多语言示例 Activity。
 *
 * <p>
 * 演示 PlayerKit 多语言能力：通过 values-xx/strings.xml 实现全量语言替换，
 * 切换到对应语言后所有文案自动切换。
 * </p>
 *
 * @author keria
 * @date 2026/05/11
 */
public class LocaleExampleActivity extends AppCompatActivity {

    private static final String TAG = "LocaleExample";

    private TextView mTvCurrentLanguage;
    private TextView mTvTranslations;

    private final PlayerLocale.OnLanguageChangedListener languageChangedListener = new PlayerLocale.OnLanguageChangedListener() {
        @Override
        public void onLanguageChanged(@NonNull String oldLanguage, @NonNull String newLanguage) {
            Log.i(TAG, "Language changed: " + oldLanguage + " → " + newLanguage);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locale_example);
        setTitle(R.string.locale_example_title);

        initViews();
        setupButtons();
        refreshDisplay();

        // 注册语言变更监听
        PlayerLocale.addOnLanguageChangedListener(languageChangedListener);
    }

    private void initViews() {
        mTvCurrentLanguage = findViewById(R.id.tv_current_language);
        mTvTranslations = findViewById(R.id.tv_translations);
    }

    private void setupButtons() {
        // 1. 切换到中文
        findViewById(R.id.btn_switch_zh).setOnClickListener(v -> {
            PlayerLocale.setLanguage("zh");
        });

        // 2. 切换到英文
        findViewById(R.id.btn_switch_en).setOnClickListener(v -> {
            PlayerLocale.setLanguage("en");
        });

        // 3. 切换到日语（全量语言替换，通过 values-ja/strings.xml 实现）
        findViewById(R.id.btn_switch_ja).setOnClickListener(v -> {
            PlayerLocale.setLanguage("ja");
        });
    }

    /**
     * 刷新翻译结果展示区。
     * 由于 setLanguage() 使用 AppCompatDelegate 会触发 Activity 重建，
     * 在 onCreate 中调用即可保持 UI 与语言一致。
     */
    private void refreshDisplay() {
        // 顶部状态
        String currentLang = PlayerLocale.getLanguage();
        mTvCurrentLanguage.setText(getString(R.string.locale_current_language, currentLang));

        // 翻译展示区（精选几个不同分类的 key 说明翻译能力）
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.locale_translation_header)).append("\n\n");

        appendTranslation(sb, "setting_item_speed", R.string.setting_item_speed);
        appendTranslation(sb, "setting_item_quality", R.string.setting_item_quality);
        appendTranslation(sb, "setting_item_loop", R.string.setting_item_loop);
        appendTranslation(sb, "setting_item_mute", R.string.setting_item_mute);

        appendTranslation(sb, "player_brightness", R.string.player_brightness);
        appendTranslation(sb, "player_volume", R.string.player_volume);

        mTvTranslations.setText(sb.toString());
    }

    private void appendTranslation(StringBuilder sb, String key, int resId) {
        String value = PlayerLocale.get(resId);
        sb.append("  ").append(key).append("\n    → ").append(value).append("\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayerLocale.removeOnLanguageChangedListener(languageChangedListener);
    }
}
