package com.app.diary.ui;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.diary.Mapp;
import com.app.diary.bean.Diary;
import com.app.diary.data.DiaryDataSource;
import com.app.diary.utils.ToastUtils;
import com.app.diary.utils.rxjava.CompletableObserverUtils;
import com.app.diary.utils.rxjava.SingleObserverUtils;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class DiaryBrowseViewModel extends BaseViewModel {

    private MutableLiveData<Diary> diaryLiveData = new MutableLiveData<>();//日记的数据容器

    private DiaryDataSource diaryDataSource;//日记数据来源

    public DiaryBrowseViewModel(@NonNull Application application) {
        super(application);
        diaryDataSource = ((Mapp) application).getDiaryDataSource();
    }

    /**
     * 删除日记
     */
    public LiveData<Boolean> deleteDiary(long diaryId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        diaryDataSource.deleteDiary(diaryId).compose(CompletableObserverUtils.applyUIScheduler(this)).subscribe(new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
                ToastUtils.showShort("删除成功");
                liveData.setValue(true);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ToastUtils.showShort("删除失败, 原因:" + e.getMessage());
                liveData.setValue(false);
            }

        });
        return liveData;
    }

    /**
     * 获取日记的数据容器
     */
    public LiveData<Diary> getDiaryLiveData() {
        return diaryLiveData;
    }

    /**
     * 加载数据
     */
    public void loadData(long diaryId, boolean lazy) {
        if (lazy && loaded) {
            return;
        }
        loaded = true;

        diaryDataSource.selectOne(diaryId).compose(SingleObserverUtils.applyUIScheduler(this)).subscribe(new DisposableSingleObserver<Diary>() {

            @Override
            public void onSuccess(@NonNull Diary diary) {
                diaryLiveData.setValue(diary);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ToastUtils.showShort("获取失败, 原因:" + e.getMessage());
                diaryLiveData.setValue(null);
            }

        });
    }

}