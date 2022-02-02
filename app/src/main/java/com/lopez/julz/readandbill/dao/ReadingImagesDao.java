package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReadingImagesDao {
    @Query("SELECT * FROM ReadingImages WHERE ServicePeriod = :servicePeriod AND AccountNumber = :accountNumber")
    List<ReadingImages> getAll(String servicePeriod, String accountNumber);

    @Insert
    void insertAll(ReadingImages... readingImages);

    @Update
    void updateAll(ReadingImages... readingImages);

    @Query("SELECT * FROM ReadingImages WHERE UploadStatus = 'UPLOADABLE'")
    List<ReadingImages> getUploadable();
}
