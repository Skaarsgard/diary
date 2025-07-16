package com.app.diary.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;

import com.app.diary.R;
import com.app.diary.bean.Constant;
import com.app.diary.bean.Diary;
import com.app.diary.utils.ToastUtils;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;

/**
 * 日志浏览
 */
public class DiaryBrowseFragment extends BaseFragment {

    private Toolbar toolbar;//标题栏控件
    private NestedScrollView scrollView;//滑动控件
    private TextView dateTextView;//日期文本控件
    private TextView weekTextView;//星期文本控件
    private TextView weatherTextView;//天气文本控件
    private TextView titleTextView;//标题文本控件
    private TextView contentTextView;//内容文本控件
    private TextView errorTextView;//错误文本控件
    private ImageView diaryImageView; // 显示图片的 ImageView

    private long diaryId;//日记主键
    private DiaryBrowseViewModel diaryBrowseViewModel;
    private SavedStateHandle savedStateHandle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavBackStackEntry navBackStackEntry = getNavController().getCurrentBackStackEntry();
        navBackStackEntry.getSavedStateHandle().getLiveData(Constant.DATA_CHANGE).observe(navBackStackEntry, new Observer<Object>() {

            @Override
            public void onChanged(Object dataChanged) {
                if ((boolean) dataChanged) {
                    if (savedStateHandle != null) {
                        savedStateHandle.set(Constant.DATA_CHANGE, true);
                    }
                    if (diaryBrowseViewModel != null) {
                        diaryBrowseViewModel.loadData(diaryId, false);
                    }
                }
            }

        });

        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        diaryBrowseViewModel = new ViewModelProvider(this).get(DiaryBrowseViewModel.class);
        savedStateHandle = getNavController().getPreviousBackStackEntry().getSavedStateHandle();
        initView(view);
        setView();
        diaryBrowseViewModel.loadData(diaryId, true);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        diaryId = DiaryBrowseFragmentArgs.fromBundle(getArguments()).getDiaryId();
    }

    /**
     * 初始化控件
     */
    private void initView(@NonNull View view) {
        toolbar = view.findViewById(R.id.toolbar);
        scrollView = view.findViewById(R.id.scrollView);
        dateTextView = view.findViewById(R.id.date_textView);
        weekTextView = view.findViewById(R.id.week_textView);
        weatherTextView = view.findViewById(R.id.weather_textView);
        titleTextView = view.findViewById(R.id.title_textView);
        contentTextView = view.findViewById(R.id.content_textView);
        errorTextView = view.findViewById(R.id.error_textView);
        diaryImageView = view.findViewById(R.id.diary_image_view);
    }

    /**
     * 设置控件
     */
    private void setView() {
        //将标题栏关联到页面
        initSupportActionBar(toolbar, true);

        //设置菜单
        requireActivity().addMenuProvider(new MenuProvider() {

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_diary_browse, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_update) {//跳转到日记编辑页面
                    getNavController().navigate(DiaryBrowseFragmentDirections.diaryEditAction(diaryId));

                } else if (menuItem.getItemId() == R.id.action_delete) {//弹窗提示是否删除日记
                    //弹窗提示是否删除日记
                    new AlertDialog.Builder(getContext()).setTitle("提示").setMessage("确定要删除日记吗?").setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            diaryBrowseViewModel.deleteDiary(diaryId).observe(getViewLifecycleOwner(), new Observer<Boolean>() {

                                @Override
                                public void onChanged(Boolean success) {
                                    if (success) {
                                        savedStateHandle.set(Constant.DATA_CHANGE, true);
                                        getNavController().navigateUp();
                                    }
                                }

                            });
                        }

                    }).setNegativeButton("取消", null).create().show();
                }
                return false;
            }

        }, getViewLifecycleOwner());

        //观察日记数据并设置日记控件
        diaryBrowseViewModel.getDiaryLiveData().observe(getViewLifecycleOwner(), new Observer<Diary>() {

            @Override
            public void onChanged(Diary diary) {
                setDiaryView(diary);
            }

        });
    }

    /**
     * 设置日记控件
     */
    private void setDiaryView(@Nullable Diary diary) {
        if (diary == null) {
            errorTextView.setText("未找到该日记");
            errorTextView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            diaryImageView.setVisibility(View.GONE);

        } else {
            errorTextView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            dateTextView.setText(new SimpleDateFormat("yyyy年MM月dd日").format(diary.getDate()));
            weekTextView.setText(new SimpleDateFormat("EEEE").format(diary.getDate()));
            weatherTextView.setText(diary.getWeather());
            titleTextView.setText(diary.getTitle());
            //处理内容格式，开头空两格，换行空两格
            String content = "\t\t\t\t" + diary.getContent().replace("\n", "\n\t\t\t\t");
            contentTextView.setText(content);

            // 显示图片
            String imagePath = diary.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                diaryImageView.setVisibility(View.VISIBLE);
                Glide.with(requireContext()).load(imagePath).into(diaryImageView);
            } else {
                diaryImageView.setVisibility(View.GONE);
            }
        }
    }
}