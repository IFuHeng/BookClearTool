package com.example.bookcleartool.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FilterDao {
    @Insert
    void insert(FilterBean... beans);

    @Update
    void update(FilterBean... beans);

    @Delete
    void delete(FilterBean... beans);

    @Query("DELETE FROM FilterBean")
    void clean();

    @Query("SELECT * FROM FilterBean ORDER BY ID DESC")
    List<FilterBean> getAll();
}
