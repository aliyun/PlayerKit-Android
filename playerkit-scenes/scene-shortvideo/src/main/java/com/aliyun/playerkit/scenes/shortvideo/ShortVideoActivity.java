package com.aliyun.playerkit.scenes.shortvideo;

import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.aliyun.playerkit.utils.ClipboardUtils;
import com.aliyun.playerkit.utils.ToastUtils;

/**
 * 列表播放场景示例 Activity
 *
 * <p>
 * 理论上，AliPlayerKit 在架构层面已完整支持 VIDEO_LIST（列表播放）场景。
 * 在该场景下，播放器的核心能力与单视频播放保持一致，包括但不限于：
 * - 手势控制（Gesture Control）
 * - 全屏 / 竖屏切换（Fullscreen / Orientation）
 * - 预渲染（Pre-render）
 * - 预加载（Preload）
 * - 播放器池复用（Player Pool）
 * </p>
 *
 * <p>
 * 因此，从播放器能力角度来看，VIDEO_LIST 场景并不存在功能缺失或能力限制。
 * 列表播放场景的复杂性主要体现在业务编排、数据组织以及前后端协同上，
 * 而非播放器本身的技术实现。
 * </p>
 *
 * <p>
 * List Playback Scene Demo Activity
 * </p>
 *
 * <p>
 * Theoretically, AliPlayerKit provides full architectural support for the
 * VIDEO_LIST (list playback) scenario. In this scenario, the player's core
 * capabilities remain consistent with those of single-video playback,
 * including but not limited to:
 * - Gesture control
 * - Fullscreen / orientation handling
 * - Pre-rendering
 * - Preloading
 * - Player pool reuse
 * </p>
 *
 * <p>
 * From a player capability perspective, the VIDEO_LIST scenario does not
 * introduce functional gaps or capability limitations. The complexity of
 * list playback mainly lies in business orchestration, data organization,
 * and front-end/back-end collaboration rather than in the player
 * implementation itself.
 * </p>
 *
 * @author keria
 * @date 2026/1/5
 */
public class ShortVideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_video);
        initHint();
    }

    /**
     * 初始化提示信息
     */
    private void initHint() {
        TextView hintText = findViewById(R.id.tv_short_drama_hint);
        hintText.setText(HtmlCompat.fromHtml(getString(R.string.short_video_hint), HtmlCompat.FROM_HTML_MODE_LEGACY));

        // 必须设置，否则 <a href> 不可点击
        hintText.setMovementMethod(LinkMovementMethod.getInstance());
        hintText.setLinksClickable(true);

        // 长按：直接复制全部文本（不进入可选复制模式）
        hintText.setOnLongClickListener(v -> {
            copyHintWithLinks(hintText);
            return true; // 消费长按事件，避免进入系统选择模式
        });
    }

    /**
     * 复制提示信息（包含链接）
     *
     * @param hintText 提示信息
     */
    private void copyHintWithLinks(TextView hintText) {
        CharSequence cs = hintText.getText(); // 包含可见文字（链接会以文字形式呈现）

        String plain = cs.toString().trim();
        StringBuilder sb = new StringBuilder(plain);

        if (cs instanceof Spanned) {
            Spanned sp = (Spanned) cs;
            URLSpan[] spans = sp.getSpans(0, sp.length(), URLSpan.class);
            if (spans != null && spans.length > 0) {
                sb.append("\n\n");
                for (URLSpan s : spans) {
                    sb.append(s.getURL()).append('\n');
                }
            }
        }

        // 复制文本到剪贴板
        ClipboardUtils.copyText(this, "short_drama_hint", sb.toString().trim());

        ToastUtils.showToast(R.string.copy_success);
    }
}
