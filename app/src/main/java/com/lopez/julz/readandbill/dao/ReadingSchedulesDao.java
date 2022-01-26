package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReadingSchedulesDao {
    @Query("SELECT * FROM ReadingSchedules")
    List<ReadingSchedules> getAll();

    @Query("SELECT * FROM ReadingSchedules WHERE id = :id")
    ReadingSchedules getOne(String id);

    @Insert
    void insertAll(ReadingSchedules... readingSchedules);

    @Update
    void updateAll(ReadingSchedules... readingSchedules);

    @Query("DELETE FROM ReadingSchedules WHERE id = :id")
    void deleteOne(String id);

    @Query("SELECT * FROM ReadingSchedules WHERE Status IS NULL")
    List<ReadingSchedules> getActiveSchedules();
}
