package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReadingsDao {
    @Query("SELECT * FROM Readings")
    List<Readings> getAll();

    @Insert
    void insertAll(Readings... readings);

    @Update
    void updateAll(Readings... readings);

    @Query("SELECT * FROM Readings WHERE AccountNumber = :accountNumber AND ServicePeriod = :servicePeriod")
    Readings getOne(String accountNumber, String servicePeriod);
}
