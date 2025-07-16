package com.app.diary.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Fragment的基础类
 */
public class BaseFragment extends Fragment {

    /**
     * 获取导航控制器
     */
    protected NavController getNavController() {
        return NavHostFragment.findNavController(this);
    }

    /**
     * 初始化标题栏
     *
     * @param toolbar  标题栏
     * @param showBack 是否显示返回键
     */
    protected void initSupportActionBar(@NonNull Toolbar toolbar, boolean showBack) {
        if (getActivity() != null && (getActivity() instanceof AppCompatActivity)) {
            if (toolbar.getTitle() == null) {
                toolbar.setTitle("");
            }
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
        }
    }

}