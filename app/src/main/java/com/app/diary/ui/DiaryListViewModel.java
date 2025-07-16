package com.app.diary.ui;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.diary.Mapp;
import com.app.diary.bean.Diary;
import com.app.diary.data.DiaryDataSource;
import com.app.diary.utils.ToastUtils;
import com.app.diary.utils.rxjava.SingleObserverUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class DiaryListViewModel extends BaseViewModel {

    private MutableLiveData<List<Diary>> diaryListLiveData = new MutableLiveData<>();//日记列表的数据容器
    private DiaryDataSource diaryDataSource;//日记数据来源

    public DiaryListViewModel(@NonNull Application application) {
        super(application);
        diaryDataSource = ((Mapp) application).getDiaryDataSource();
    }

    /**
     * 获取日记列表的数据容器
     */
    public LiveData<List<Diary>> getDiaryListLiveData() {
        return diaryListLiveData;
    }

    /**
     * 加载数据
     */
    public void loadData(boolean lazy) {
        diaryDataSource.selectList()
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.rxjava3.observers.DisposableSingleObserver<List<Diary>>() {
                    @Override
                    public void onSuccess(List<Diary> list) {
                        diaryListLiveData.setValue(list);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ToastUtils.showShort("获取日记列表失败: " + e.getMessage());
                        diaryListLiveData.setValue(new ArrayList<>());
                    }
                });
    }
}