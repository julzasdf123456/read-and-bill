package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RatesDao {
    @Query("SELECT * FROM Rates")
    List<Rates> getAll();

    @Insert
    void insertAll(Rates... rates);

    @Update
    void updateAll(Rates... rates);

    @Query("SELECT * FROM Rates WHERE ConsumerType = :consumerType AND AreaCode = :areaCode AND ServicePeriod = :servicePeriod")
    Rates getOne(String consumerType, String areaCode, String servicePeriod);

    @Query("SELECT * FROM Rates WHERE id = :id")
    Rates getOneById(String id);

    @Query("SELECT * FROM Rates WHERE ServicePeriod = :servicePeriod")
    List<Rates> getRatesByServicePeriod(String servicePeriod);

    @Query("SELECT * FROM Rates GROUP BY ServicePeriod")
    List<Rates> getRatesGroupByServicePeriod();
}
