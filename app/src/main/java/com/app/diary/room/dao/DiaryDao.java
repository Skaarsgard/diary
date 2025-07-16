package com.app.diary.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.diary.bean.Diary;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DiaryDao {

    /**
     * 新增日记
     */
    @Insert
    Completable insert(Diary diary);

    /**
     * 删除日记
     */
    @Delete
    Completable delete(Diary diary);

    /**
     * 修改日记
     */
    @Update
    Completable update(Diary diary);

    /**
     * 获取日记列表
     */
    @Query("SELECT * FROM diary ORDER BY date DESC")
    Single<List<Diary>> getList();

    /**
     * 获取日记详情
     */
    @Query("SELECT * FROM diary WHERE id = (:diaryId)")
    Single<Diary> getOne(long diaryId);

}