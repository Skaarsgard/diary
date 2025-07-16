package com.app.diary.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;

import com.app.diary.R;
import com.app.diary.bean.Constant;
import com.app.diary.bean.Diary;
import com.app.diary.utils.ToastUtils;
import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * 编辑日志
 */
public class DiaryEditFragment extends BaseFragment {

    private static final String[] WEATHERS = new String[]{"晴天", "雨天", "雪天"};
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1001;
    private Toolbar toolbar;//标题栏控件
    private TextView dateTextView;//日期文本控件
    private TextView weatherTextView;//天气文本控件
    private EditText titleEditText;//标题输入框控件
    private EditText contentEditText;//内容输入框控件
    private Button selectImageButton; // 选择图片按钮
    private ImageView diaryImageView; // 显示图片的 ImageView

    private DatePickerDialog datePickerDialog;//日期选择对话框
    private AlertDialog weatherPickerDialog;//天气选择对话框

    private long diaryId;//日记主键
    private DiaryEditViewModel diaryEditViewModel;
    private SavedStateHandle savedStateHandle;

    private String selectedImagePath; // 选中图片的路径

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        diaryEditViewModel = new ViewModelProvider(this).get(DiaryEditViewModel.class);
        savedStateHandle = getNavController().getPreviousBackStackEntry().getSavedStateHandle();
        initView(view);
        setView();
        if (diaryId > 0) {
            diaryEditViewModel.loadData(diaryId, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //在页面销毁前，关闭日期选择对话框，防止对话框未关闭报错
        if (datePickerDialog != null && datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
        }
        if (weatherPickerDialog != null && weatherPickerDialog.isShowing()) {
            weatherPickerDialog.dismiss();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        diaryId = DiaryEditFragmentArgs.fromBundle(getArguments()).getDiaryId();
    }

    /**
     * 初始化控件
     */
    private void initView(@NonNull View view) {
        toolbar = view.findViewById(R.id.toolbar);
        dateTextView = view.findViewById(R.id.date_textView);
        weatherTextView = view.findViewById(R.id.weather_textView);
        titleEditText = view.findViewById(R.id.title_editText);
        contentEditText = view.findViewById(R.id.content_editText);
        selectImageButton = view.findViewById(R.id.select_image_button);
        diaryImageView = view.findViewById(R.id.diary_image_view);
    }

    /**
     * 设置控件
     */
    private void setView() {
        //将标题栏关联到页面
        initSupportActionBar(toolbar, true);

        //添加菜单
        requireActivity().addMenuProvider(new MenuProvider() {

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_diary_create, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_save) {//保存日记
                    saveDiary();
                }
                return false;
            }

        }, getViewLifecycleOwner());

        //设置日期文本的点击事件
        dateTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }

        });
        //设置天气文本的点击事件
        weatherTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showWeatherPickerDialog();
            }

        });

        // 设置选择图片按钮的点击事件
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        if (diaryId > 0) {//修改日记
            diaryEditViewModel.getDiaryLiveData().observe(getViewLifecycleOwner(), new Observer<Diary>() {

                @Override
                public void onChanged(Diary diary) {
                    if (diary == null) {
                        ToastUtils.showShort("未找到该日记");
                        getNavController().navigateUp();

                    } else {
                        //设置该日记数据
                        toolbar.setTitle("修改日记");
                        dateTextView.setText(new SimpleDateFormat("yyyy年MM月dd日").format(diary.getDate()));
                        weatherTextView.setText(diary.getWeather());
                        titleEditText.setText(diary.getTitle());
                        contentEditText.setText(diary.getContent());

                        // 显示已有的图片
                        String imagePath = diary.getImagePath();
                        if (imagePath != null && !imagePath.isEmpty()) {
                            diaryImageView.setVisibility(View.VISIBLE);
                            Glide.with(requireContext()).load(imagePath).into(diaryImageView);
                            selectedImagePath = imagePath;
                        }
                    }
                }

            });

        } else {//创建日记
            //设置默认值
            toolbar.setTitle("创建日记");
            dateTextView.setText(new SimpleDateFormat("yyyy年MM月dd日").format(new Date()));
            weatherTextView.setText(WEATHERS[0]);
        }
    }

    /**
     * 显示日期选择对话框
     */
    private void showDatePickerDialog() {
        //懒加载创建日期选择对话框，取当前日期为默认日期
        if (datePickerDialog == null) {
            Calendar calendar = Calendar.getInstance();
            try {
                String dateStr = dateTextView.getText().toString().trim();
                Date date = new SimpleDateFormat("yyyy年MM月dd日").parse(dateStr);
                calendar.setTime(date);
            } catch (ParseException e) {
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    //将选择的年月日组合成文字，显示在日期文本上
                    Date date = getDate(year, monthOfYear, dayOfMonth);
                    String dateStr = new SimpleDateFormat("yyyy年MM月dd日").format(date);
                    dateTextView.setText(dateStr);
                }

            }, year, month, day);
        }
        if (!datePickerDialog.isShowing()) {
            datePickerDialog.show();
        }
    }

    /**
     * 显示天气选择对话框
     */
    private void showWeatherPickerDialog() {
        if (weatherPickerDialog == null) {
            String weather = weatherTextView.getText().toString().trim();
            int position = Arrays.asList(WEATHERS).indexOf(weather);
            if (position < 0) {
                position = 0;
            }
            weatherPickerDialog = new AlertDialog.Builder(getContext()).setTitle("选择天气").setSingleChoiceItems(WEATHERS, position, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    weatherTextView.setText(WEATHERS[which]);
                    dialog.dismiss();
                }

            }).create();
        }
        if (!weatherPickerDialog.isShowing()) {
            weatherPickerDialog.show();
        }
    }

    /**
     * 根据年月日获取日期函数
     */
    private Date getDate(int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return calendar.getTime();
    }

    /**
     * 保存日记
     */
    private void saveDiary() {
        //检查输入情况
        String date = dateTextView.getText().toString().trim();
        if (date.isEmpty()) {
            ToastUtils.showShort("未选择日期");
            return;
        }
        String weather = weatherTextView.getText().toString().trim();
        if (weather.isEmpty()) {
            ToastUtils.showShort("未选择天气");
            return;
        }
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            ToastUtils.showShort("未输入标题");
            return;
        }
        String content = contentEditText.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtils.showShort("未输入内容");
            return;
        }

        Date diaryDate;
        try {
            diaryDate = new SimpleDateFormat("yyyy年MM月dd日").parse(date);
        } catch (ParseException e) {
            ToastUtils.showShort("保存失败，时间转换错误");
            return;
        }

        if (diaryId > 0) {//修改日记
            diaryEditViewModel.updateDiary(diaryId, diaryDate, weather, title, content, selectedImagePath).observe(getViewLifecycleOwner(), new Observer<Boolean>() {

                @Override
                public void onChanged(Boolean success) {
                    if (success) {
                        savedStateHandle.set(Constant.DATA_CHANGE, true);
                        getNavController().navigateUp();
                    }
                }

            });

        } else {//创建日记
            diaryEditViewModel.insertDiary(diaryDate, weather, title, content, selectedImagePath).observe(getViewLifecycleOwner(), new Observer<Boolean>() {

                @Override
                public void onChanged(Boolean success) {
                    if (success) {
                        savedStateHandle.set(Constant.DATA_CHANGE, true);
                        getNavController().navigateUp();
                    }
                }

            });
        }
    }

    /**
     * 选择图片
     */
    private void selectImage() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        } else {
            requestStoragePermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = requireActivity().getContentResolver().query(selectedImageUri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                selectedImagePath = cursor.getString(columnIndex);
                cursor.close();

                // 显示选中的图片
                diaryImageView.setVisibility(View.VISIBLE);
                Glide.with(requireContext()).load(selectedImagePath).into(diaryImageView);
            }
        }
    }
    // 在 DiaryEditFragment 类中添加以下方法
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                ToastUtils.showShort("未授予读取存储权限，无法选择图片");
            }
        }
    }
}