package com.app.diary.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.app.diary.R;

/**
 * 主页面
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}