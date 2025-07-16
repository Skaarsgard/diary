package com.app.diary.data.impl;

import androidx.annotation.NonNull;

import com.app.diary.bean.Diary;
import com.app.diary.data.DiaryDataSource;
import com.app.diary.room.database.AppDatabase;

import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * 日志数据源的实现类
 */
public class DiaryDataSourceImpl implements DiaryDataSource {

    @NonNull
    private AppDatabase appDatabase;

    public DiaryDataSourceImpl(@NonNull AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    @Override
    public Completable insertDiary(@NonNull Diary diary) {
        Date now = new Date();
        diary.setCreateTime(now);
        diary.setUpdateTime(now);
        return appDatabase.diaryDao().insert(diary);
    }

    @Override
    public Completable deleteDiary(long diaryId) {
        Diary diary = new Diary();
        diary.setId(diaryId);
        return appDatabase.diaryDao().delete(diary);
    }

    @Override
    public Completable updateDiary(@NonNull Diary diary) {
        diary.setUpdateTime(new Date());
        return appDatabase.diaryDao().update(diary);
    }

    @Override
    public Single<Diary> selectOne(long diaryId) {
        return appDatabase.diaryDao().getOne(diaryId);
    }

    @Override
    public Single<List<Diary>> selectList() {
        return appDatabase.diaryDao().getList();
    }

}