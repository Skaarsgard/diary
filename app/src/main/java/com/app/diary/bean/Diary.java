package com.app.diary.bean;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

/**
 * 日记
 */
@Entity(tableName = "diary")
public class Diary extends BaseBean {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private Long id;//主键

    @NonNull
    private Date date;//日期

    @NonNull
    private String weather;//天气

    @NonNull
    private String title;//标题

    @NonNull
    private String content;//内容

    @NonNull
    @ColumnInfo(name = "create_time")
    private Date createTime;//创建时间

    @NonNull
    @ColumnInfo(name = "update_time")
    private Date updateTime;//修改时间
    private String imagePath;//图片路径
    // Getters and Setters
    public String getImagePath() {
        return imagePath;
    }
    // 构造函数中自动生成ID
    public Diary() {

    }

    @Ignore
    // 带参构造函数（用于创建新日记）
    public Diary(Date date, String weather, String title, String content) {
        this.id = generateUniqueId(); // 仅在创建新日记时生成ID
        this.date = date;
        this.weather = weather;
        this.title = title;
        this.content = content;
        this.updateTime = new Date();
    }
    // 生成唯一ID的方法（使用UUID转换为长整型）
    private long generateUniqueId() {
        return UUID.randomUUID().getMostSignificantBits() & 0x7fffffffffffffffL;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}