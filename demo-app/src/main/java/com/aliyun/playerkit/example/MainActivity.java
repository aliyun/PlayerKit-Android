package com.aliyun.playerkit.example;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.playerkit.example.adapter.MenuAdapter;
import com.aliyun.playerkit.example.config.MenuConfig;
import com.aliyun.playerkit.example.model.MenuItem;

import java.util.List;

/**
 * @author keria
 * @date 2025/6/10
 * @brief PlayerKit Example 首页
 */
public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadMenuData();
    }

    // 初始化界面视图组件
    private void initViews() {
        mRecyclerView = findViewById(R.id.rv_menu);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // 加载菜单数据并设置适配器
    private void loadMenuData() {
        List<MenuItem> menuItems = MenuConfig.getMenuItems(this);
        MenuAdapter adapter = new MenuAdapter(menuItems);
        mRecyclerView.setAdapter(adapter);
    }
}