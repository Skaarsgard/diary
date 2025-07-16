package com.app.diary.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.diary.Mapp;
import com.app.diary.R;
import com.app.diary.adapter.DiaryRecyclerAdapter;
import com.app.diary.bean.Constant;
import com.app.diary.bean.Diary;
import com.app.diary.data.DiaryDataSource;
import com.app.diary.utils.SizeUtils;
import com.app.diary.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import okhttp3.*;

/**
 * 日志列表
 */
public class DiaryListFragment extends BaseFragment {

    private Toolbar toolbar;//标题栏控件
    private RecyclerView recyclerView;//列表控件

    private DiaryRecyclerAdapter diaryRecyclerAdapter;
    private DiaryListViewModel diaryListViewModel;

    private static final String SERVER_URL = "http://192.168.1.2:8080/DiaryCloudServer/DiaryUpload.jsp";
    private static final String SERVER_DOWNLOAD_URL = "http://192.168.1.2:8080/DiaryCloudServer/DiaryDownload.jsp";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 启用菜单
        NavBackStackEntry navBackStackEntry = getNavController().getCurrentBackStackEntry();
        navBackStackEntry.getSavedStateHandle().getLiveData(Constant.DATA_CHANGE).observe(navBackStackEntry, new Observer<Object>() {

            @Override
            public void onChanged(Object dataChanged) {
                if ((boolean) dataChanged) {
                    if (diaryListViewModel != null) {
                        diaryListViewModel.loadData(false);
                    }
                }
            }

        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        diaryListViewModel = new ViewModelProvider(this).get(DiaryListViewModel.class);
        initView(view);
        setView();
        diaryListViewModel.loadData(true);
    }

    /**
     * 初始化控件
     */
    private void initView(@NonNull View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    /**
     * 设置控件
     */
    private void setView() {
        //将标题栏关联到页面
        initSupportActionBar(toolbar, true);

        //设置列表的布局样式
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        //设置列表的间隔距离
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int count = parent.getAdapter().getItemCount();
                int index = parent.getChildAdapterPosition(view);
                if (index < count - 1) {
                    outRect.set(0, 0, 0, SizeUtils.dp2px(30));
                }
            }

        });
        //设置列表的适配器
        diaryRecyclerAdapter = new DiaryRecyclerAdapter();
        diaryRecyclerAdapter.setOnItemClickListener(new DiaryRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(Diary diary, int position) {
                getNavController().navigate(DiaryListFragmentDirections.diaryBrowseAction(diary.getId()));
            }

        });
        recyclerView.setAdapter(diaryRecyclerAdapter);

        //观察日记列表数据并将数据加入适配器
        diaryListViewModel.getDiaryListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Diary>>() {

            @Override
            public void onChanged(List<Diary> list) {
                diaryRecyclerAdapter.setNewData(list);
            }

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_diary_list, menu);
        MenuItem syncFromServerItem = menu.add(Menu.NONE, R.id.action_sync_from_server, Menu.NONE, "从服务器同步");
        syncFromServerItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            syncDataToServer();
            return true;
        } else if (item.getItemId() == R.id.action_sync_from_server) {
            syncDataFromServer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncDataToServer() {
        DiaryDataSource diaryDataSource = ((Mapp) requireActivity().getApplication()).getDiaryDataSource();
        diaryDataSource.selectList().subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.rxjava3.observers.DisposableSingleObserver<List<Diary>>() {
                    @Override
                    public void onSuccess(List<Diary> diaryList) {
                        if (diaryList.isEmpty()) {
                            ToastUtils.showShort("没有可同步的日记");
                            return;
                        }

                        uploadDiariesToServer(diaryList);
                        ToastUtils.showShort("开始同步 " + diaryList.size() + " 篇日记");

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ToastUtils.showShort("获取本地日记数据失败: " + e.getMessage());
                    }
                });
    }


    private void syncDataFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_DOWNLOAD_URL)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showShort("从服务器同步数据失败: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            org.json.JSONObject jsonResponse = new org.json.JSONObject(responseData);

                            if (jsonResponse.getBoolean("success")) {
                                org.json.JSONArray diariesArray = jsonResponse.getJSONArray("diaries");
                                updateLocalDiaries(diariesArray);
                                ToastUtils.showShort("成功从服务器同步 " + diariesArray.length() + " 篇日记");
                                // 刷新日记列表
                                refreshDiaryList();
                            } else {
                                String error = jsonResponse.getString("error");
                                ToastUtils.showShort("从服务器同步数据失败: " + error);
                            }
                        } else {
                            ToastUtils.showShort("从服务器同步数据失败，服务器返回码: " + response.code());
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("解析服务器响应失败");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
    private void updateLocalDiaries(org.json.JSONArray diariesArray) {
        DiaryDataSource diaryDataSource = ((Mapp) requireActivity().getApplication()).getDiaryDataSource();
        List<Completable> completableList = new ArrayList<>();

        for (int i = 0; i < diariesArray.length(); i++) {
            try {
                org.json.JSONObject diaryJson = diariesArray.getJSONObject(i);
                long lastModified = diaryJson.getLong("lastModified");

                // 检查本地是否有该日记
                long diaryId = Long.parseLong(diaryJson.getString("id"));
                completableList.add(diaryDataSource.selectOne(diaryId)
                        .flatMapCompletable(localDiary -> {
                            // 比较时间戳，如果服务器上的日记更新，则更新本地日记
                            if (localDiary.getUpdateTime().getTime() < lastModified) {
                                return updateLocalDiaryFromJson(diaryJson, diaryDataSource);
                            }
                            return Completable.complete();
                        })
                        .onErrorResumeNext(throwable -> insertNewDiaryFromJson(diaryJson, diaryDataSource))
                        );
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        Completable.merge(completableList)
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // 所有更新或插入操作完成后刷新列表
                    refreshDiaryList();
                }, throwable -> {
                    ToastUtils.showShort("更新本地日记失败: " + throwable.getMessage());
                });
    }

    private Completable updateLocalDiaryFromJson(org.json.JSONObject diaryJson, DiaryDataSource diaryDataSource) {
        try {
            long diaryId = Long.parseLong(diaryJson.getString("id"));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(diaryJson.getString("date"));
            String weather = diaryJson.getString("weather");
            String title = diaryJson.getString("title");
            String content = diaryJson.getString("content");
            String imagePath = diaryJson.optString("imagePath", null);

            Diary diary = new Diary();
            diary.setId(diaryId);
            diary.setDate(date);
            diary.setWeather(weather);
            diary.setTitle(title);
            diary.setContent(content);
            diary.setImagePath(imagePath);
            diary.setUpdateTime(new Date(diaryJson.getLong("lastModified")));

            return diaryDataSource.updateDiary(diary);
        } catch (org.json.JSONException | java.text.ParseException e) {
            return Completable.error(e);
        }
    }

    private Completable insertNewDiaryFromJson(org.json.JSONObject diaryJson, DiaryDataSource diaryDataSource) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(diaryJson.getString("date"));
            String weather = diaryJson.getString("weather");
            String title = diaryJson.getString("title");
            String content = diaryJson.getString("content");
            String imagePath = diaryJson.optString("imagePath", null);

            Diary diary = new Diary(date, weather, title, content);
            diary.setDate(date);
            diary.setWeather(weather);
            diary.setTitle(title);
            diary.setContent(content);
            diary.setImagePath(imagePath);
            diary.setUpdateTime(new Date(diaryJson.getLong("lastModified")));

            return diaryDataSource.insertDiary(diary);
        } catch (org.json.JSONException | java.text.ParseException e) {
            return Completable.error(e);
        }
    }

    //刷新列表

    private void refreshDiaryList() {
        if (diaryListViewModel != null) {
            diaryListViewModel.loadData(false);
        }
    }



    private void uploadDiariesToServer(List<Diary> diaryList) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // 遍历所有日记，添加到同一个请求中
        for (Diary diary : diaryList) {
            // 添加开始新日记标记
            builder.addFormDataPart("startNewDiary", "true");
            // 新增：上传id字段
            builder.addFormDataPart("id", String.valueOf(diary.getId()));
            // 添加日记字段
            builder.addFormDataPart("date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(diary.getDate()));
            builder.addFormDataPart("weather", diary.getWeather());
            builder.addFormDataPart("title", diary.getTitle());
            builder.addFormDataPart("content", diary.getContent());

            // 添加图片（如果有）
            String imagePath = diary.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                    builder.addFormDataPart("image", file.getName(), fileBody);
                }
            }
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showShort("同步失败: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);

                            if (jsonResponse.getBoolean("success")) {
                                int count = jsonResponse.getInt("message");
                                String batchId = jsonResponse.getString("batchId");
                                ToastUtils.showShort("成功同步 " + count + " 篇日记");

                                // 可以在这里处理批次ID，例如保存到本地数据库
                            } else {
                                String error = jsonResponse.getString("error");
                                ToastUtils.showShort("同步失败: " + error);
                            }
                        } else {
                            ToastUtils.showShort("同步失败，服务器返回码: " + response.code());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("解析服务器响应失败");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
}