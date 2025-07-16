package com.app.diary.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.diary.Mapp;
import com.app.diary.bean.Diary;
import com.app.diary.data.DiaryDataSource;
import com.app.diary.utils.ToastUtils;
import com.app.diary.utils.rxjava.CompletableObserverUtils;
import com.app.diary.utils.rxjava.SingleObserverUtils;

import java.util.Date;

import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class DiaryEditViewModel extends BaseViewModel {

    private MutableLiveData<Diary> diaryLiveData = new MutableLiveData<>();//日记的数据容器

    private DiaryDataSource diaryDataSource;//日记数据来源

    public DiaryEditViewModel(@NonNull Application application) {
        super(application);
        diaryDataSource = ((Mapp) application).getDiaryDataSource();
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
            public void onSuccess(Diary diary) {
                diaryLiveData.setValue(diary);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ToastUtils.showShort("获取失败, 原因:" + e.getMessage());
                diaryLiveData.setValue(null);
            }

        });
    }

    /**
     * 新增日记
     */
    public LiveData<Boolean> insertDiary(Date date, String weather, String title, String content, String imagePath) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        Single.create(new SingleOnSubscribe<Diary>() {

            @Override
            public void subscribe(@NonNull SingleEmitter<Diary> emitter) throws Throwable {
                Diary diary = new Diary();
                diary.setDate(date);
                diary.setWeather(weather);
                diary.setTitle(title);
                diary.setContent(content);
                diary.setImagePath(imagePath); // 设置图片路径
                emitter.onSuccess(diary);
            }

        }).flatMapCompletable(new Function<Diary, CompletableSource>() {

            @Override
            public CompletableSource apply(Diary diary) throws Throwable {
                return diaryDataSource.insertDiary(diary);
            }

        }).compose(CompletableObserverUtils.applyUIScheduler(this)).subscribe(new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
                ToastUtils.showShort("新增成功");
                liveData.setValue(true);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ToastUtils.showShort("新增失败, 原因:" + e.getMessage());
                liveData.setValue(false);
            }

        });
        return liveData;
    }

    /**
     * 修改日记
     */
    public LiveData<Boolean> updateDiary(long diaryId, Date date, String weather, String title, String content, String imagePath) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        diaryDataSource.selectOne(diaryId).flatMapCompletable(new Function<Diary, CompletableSource>() {

            @Override
            public CompletableSource apply(Diary diary) throws Throwable {
                diary.setDate(date);
                diary.setWeather(weather);
                diary.setTitle(title);
                diary.setContent(content);
                diary.setImagePath(imagePath); // 设置图片路径
                return diaryDataSource.updateDiary(diary);
            }

        }).compose(CompletableObserverUtils.applyUIScheduler(this)).subscribe(new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
                ToastUtils.showShort("修改成功");
                liveData.setValue(true);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ToastUtils.showShort("修改失败, 原因:" + e.getMessage());
                liveData.setValue(false);
            }

        });
        return liveData;
    }
}
