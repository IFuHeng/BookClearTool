package com.example.bookcleartool.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class FilterBean {
    @Ignore
    public static final int DEFAULT_ID = -1;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    public int ID = DEFAULT_ID;
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    public String content;
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    public boolean isPattern;
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    public boolean isChecked;

    public FilterBean() {
    }

    @Ignore
    public FilterBean(String content, boolean isPattern, boolean isChecked) {
        this.content = content;
        this.isPattern = isPattern;
        this.isChecked = isChecked;
    }

    public int getID() {
        return ID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPattern() {
        return isPattern;
    }

    public void setPattern(boolean pattern) {
        isPattern = pattern;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
