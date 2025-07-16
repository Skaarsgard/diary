package com.app.diary.data;

import androidx.annotation.NonNull;

import com.app.diary.bean.Diary;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * 日记本数据源
 */
public interface DiaryDataSource {

    /**
     * 新增一条日记本
     */
    Completable insertDiary(@NonNull Diary diary);

    /**
     * 删除一条日记本
     */
    Completable deleteDiary(long diaryId);

    /**
     * 修改一条日记本
     */
    Completable updateDiary(@NonNull Diary diary);

    /**
     * 根据主键查询日记本
     */
    Single<Diary> selectOne(long diaryId);

    /**
     * 查找日记本列表
     */
    Single<List<Diary>> selectList();

}