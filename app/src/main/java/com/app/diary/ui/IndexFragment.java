package com.app.diary.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.app.diary.R;
import com.app.diary.utils.AppUtils;

/**
 * 首页
 */
public class IndexFragment extends BaseFragment {

    private Toolbar toolbar;//标题栏控件
    private Button browseButton;//查看日记按钮控件
    private Button createButton;//创建日记按钮控件
    private TextView versionTextView;//当前版本文本控件

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_index, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setView();
    }

    /**
     * 初始化控件
     */
    private void initView(@NonNull View view) {
        toolbar = view.findViewById(R.id.toolbar);
        browseButton = view.findViewById(R.id.browse_button);
        createButton = view.findViewById(R.id.create_button);

    }

    /**
     * 设置控件
     */
    private void setView() {
        //将标题栏关联到页面
        initSupportActionBar(toolbar, false);

        //设置查看日记按钮的点击事件
        browseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getNavController().navigate(IndexFragmentDirections.diaryListAction());
            }

        });

        //设置创建日记按钮的点击事件
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getNavController().navigate(IndexFragmentDirections.diaryEditAction(0));
            }

        });

    }

}