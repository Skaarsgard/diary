package com.app.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.app.diary.data.DiaryDataSource;
import com.app.diary.data.impl.DiaryDataSourceImpl;
import com.app.diary.room.database.AppDatabase;

public class Mapp extends Application {

    private static Mapp instance;//单例

    private AppDatabase appDatabase;//数据库
    private DiaryDataSource diaryDataSource;//日记数据源

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /**
     * 获取实例
     */
    public static Mapp getInstance() {
        return instance;
    }

    /**
     * 懒加载获取数据库
     */
    public AppDatabase getAppDatabase() {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "diary.db")
                    .addMigrations(AppDatabase.MIGRATION_2_3) // 添加迁移
                    .build();
        }
        return appDatabase;
    }

    /**
     * 懒加载获取日记数据源
     */
    public DiaryDataSource getDiaryDataSource() {
        if (diaryDataSource == null) {
            diaryDataSource = new DiaryDataSourceImpl(getAppDatabase());
        }
        return diaryDataSource;
    }
}