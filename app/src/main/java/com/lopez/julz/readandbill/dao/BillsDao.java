package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BillsDao {
    @Query("SELECT * FROM Bills")
    List<Bills> getAll();

    @Insert
    void insertAll(Bills... bills);

    @Update
    void updateAll(Bills... bills);

    @Query("SELECT * FROM Bills WHERE AccountNumber = :accountNumber AND ServicePeriod = :servicePeriod")
    Bills getOneByAccountNumberAndServicePeriod(String accountNumber, String servicePeriod);

    @Query("SELECT * FROM Bills WHERE UploadStatus = 'UPLOADABLE'")
    List<Bills> getUploadables();
}
