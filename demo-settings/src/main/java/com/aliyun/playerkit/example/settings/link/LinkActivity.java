package com.aliyun.playerkit.example.settings.link;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.acker.simplezxing.activity.CaptureActivity;
import com.aliyun.playerkit.example.settings.R;
import com.aliyun.playerkit.example.settings.storage.SPManager;
import com.aliyun.playerkit.utils.StringUtil;
import com.aliyun.playerkit.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 链接配置页面 Activity
 * <p>
 * 负责管理播放器链接配置功能，提供链接输入的列表展示、扫码入口以及数据持久化功能。
 * 支持通过手动输入或扫描二维码的方式配置视频播放链接、Vid、PlayAuth 等参数。
 * </p>
 * <p>
 * Link Configuration Activity
 * <p>
 * Manages the player link configuration functionality, providing link input list display,
 * QR code scanning entry, and data persistence. Supports configuring video playback links,
 * Vid, PlayAuth and other parameters through manual input or QR code scanning.
 * </p>
 *
 * @author keria
 * @date 2026/01/04
 */
public class LinkActivity extends AppCompatActivity {

    /**
     * Intent 参数：过滤显示的配置项 key 列表
     * <p>
     * 如果传递此参数，则只显示指定 key 列表中的配置项。
     * 例如：显示 VidSts 相关的配置项，传递包含 vid, accessKeyId, accessKeySecret, securityToken 的列表
     * 例如：只显示直播地址配置项，传递包含 LinkConstants.KEY_VIDEO_LIVE_URL 的列表
     * </p>
     */
    public static final String EXTRA_FILTER_KEYS = "extra_filter_keys";

    /**
     * 扫码请求码
     */
    private static final int REQUEST_SCAN = 101;
    /**
     * 相机权限请求码
     */
    private static final int PERMISSION_REQUEST_CAMERA = 102;

    /**
     * 链接配置项列表
     */
    private List<LinkItem> mItems;
    /**
     * 列表适配器
     */
    private LinkAdapter mAdapter;
    /**
     * 当前扫码位置索引
     */
    private int mCurrentScanPosition = -1;

    /**
     * Activity 创建时初始化
     * <p>
     * 初始化数据、RecyclerView 和保存按钮。
     * </p>
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);

        initData();
        initRecyclerView();
        initSaveButton();
    }

    /**
     * 初始化保存按钮
     * <p>
     * 设置保存按钮的点击监听，点击后保存所有配置项并关闭页面。
     * </p>
     */
    private void initSaveButton() {
        findViewById(R.id.btn_save).setOnClickListener(v -> saveData());
    }

    /**
     * 初始化数据
     * <p>
     * 从 LinkConstants 构建默认配置项列表，并从 SharedPreferences 加载已保存的数据。
     * 如果 Intent 中传递了 EXTRA_FILTER_KEYS 参数，则只显示指定 key 列表中的配置项。
     * </p>
     */
    private void initData() {
        List<LinkItem> allItems = LinkConstants.buildDefaultItems();

        // 检查过滤 key 列表
        ArrayList<String> filterKeys = getIntent().getStringArrayListExtra(EXTRA_FILTER_KEYS);
        if (filterKeys != null && !filterKeys.isEmpty()) {
            mItems = new ArrayList<>();
            for (LinkItem item : allItems) {
                if (filterKeys.contains(item.getKey())) {
                    mItems.add(item);
                }
            }
        } else {
            // 当没有传递过滤参数时，则显示所有配置项
            mItems = allItems;
        }

        // 加载已保存的数据
        for (LinkItem item : mItems) {
            String savedValue = SPManager.getInstance().getString(item.getKey());
            if (savedValue != null) {
                item.setLink(savedValue);
            }
        }
    }

    /**
     * 初始化 RecyclerView
     * <p>
     * 设置布局管理器和适配器，并配置扫码点击监听器。
     * </p>
     */
    private void initRecyclerView() {
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new LinkAdapter(mItems);
        mAdapter.setOnScanClickListener((position, item) -> {
            mCurrentScanPosition = position;
            checkPermissionAndScan();
        });
        rv.setAdapter(mAdapter);
    }

    /**
     * 检查权限并启动扫码
     * <p>
     * 如果已有相机权限则直接启动扫码，否则请求相机权限。
     * </p>
     */
    private void checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            startScan();
        }
    }

    /**
     * 启动扫码 Activity
     * <p>
     * 打开二维码扫描页面，扫描结果通过 onActivityResult 回调返回。
     * </p>
     */
    private void startScan() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_SCAN);
    }

    /**
     * 权限请求结果回调
     * <p>
     * 处理相机权限请求结果，如果授权成功则启动扫码，否则提示用户。
     * </p>
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 授权结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                ToastUtils.showToast(R.string.setting_link_toast_camera_permission);
            }
        }
    }

    /**
     * 保存数据
     * <p>
     * 将所有配置项的链接内容保存到 SharedPreferences，保存成功后提示用户并关闭页面。
     * </p>
     */
    private void saveData() {
        for (LinkItem item : mItems) {
            SPManager.getInstance().saveString(item.getKey(), item.getLink());
        }
        ToastUtils.showToast(R.string.setting_link_toast_save_success);
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Activity 结果回调
     * <p>
     * 处理扫码结果，将扫描到的内容更新到对应的配置项并刷新列表显示。
     * </p>
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN && resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra("SCAN_RESULT");
            if (result != null && mCurrentScanPosition >= 0 && mCurrentScanPosition < mItems.size()) {
                mItems.get(mCurrentScanPosition).setLink(result);
                mAdapter.notifyItemChanged(mCurrentScanPosition);
            }
        }
    }

    /**
     * 分发触摸事件
     * <p>
     * 拦截触摸事件，当点击输入框外部区域时自动隐藏键盘，提升用户体验。
     * </p>
     *
     * @param ev 触摸事件
     * @return 是否消费事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocus = getCurrentFocus();
            // 如果当前焦点是输入框，检查点击位置是否在输入框外
            if (isInputView(currentFocus)) {
                if (!isTouchInsideView(currentFocus, ev)) {
                    hideKeyboard();
                }
            } else if (currentFocus != null) {
                // 如果焦点不是输入框，直接隐藏键盘
                hideKeyboard();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断是否是输入控件
     * <p>
     * 检查 View 是否为 EditText 或其子类，用于判断是否需要处理键盘隐藏逻辑。
     * </p>
     *
     * @param view 待检查的 View
     * @return 是否为输入控件
     */
    private boolean isInputView(View view) {
        return view instanceof EditText;
    }

    /**
     * 判断触摸事件是否在指定 View 内部
     * <p>
     * 计算触摸点的屏幕坐标，判断是否在指定 View 的边界范围内。
     * </p>
     *
     * @param view  目标 View
     * @param event 触摸事件
     * @return 触摸点是否在 View 内部
     */
    private boolean isTouchInsideView(View view, MotionEvent event) {
        int[] location = {0, 0};
        view.getLocationInWindow(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    /**
     * 隐藏键盘
     * <p>
     * 获取当前焦点 View，通过 InputMethodManager 隐藏键盘并清除焦点。
     * </p>
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
            }
        }
    }
}
