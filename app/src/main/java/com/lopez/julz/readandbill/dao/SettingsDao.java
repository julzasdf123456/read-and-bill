package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SettingsDao {
    @Query("SELECT * FROM Settings ORDER BY id DESC LIMIT 1")
    Settings getSettings();

    @Insert
    void insertAll(Settings... settings);

    @Update
    void updateAll(Settings... settings);
}
