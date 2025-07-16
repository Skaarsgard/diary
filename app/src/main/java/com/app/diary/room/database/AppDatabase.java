package com.app.diary.room.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.app.diary.bean.Diary;
import com.app.diary.room.converter.DateConverter;
import com.app.diary.room.dao.DiaryDao;

@Database(entities = {Diary.class}, version = 3) // 版本号升级到 3
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract DiaryDao diaryDao();

    // 数据库迁移
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE diary ADD COLUMN imagePath TEXT");
        }
    };
}