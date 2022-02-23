package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lopez.julz.readandbill.objects.DisconnectionGroupList;

import java.util.List;

@Dao
public interface DisconnectionListDao {
    @Query("SELECT * FROM DisconnectionList")
    List<DisconnectionList> getAll();

    @Insert
    void insertAll(DisconnectionList... disconnectionLists);

    @Update
    void updateAll(DisconnectionList... disconnectionLists);

    @Query("SELECT * FROM DisconnectionList WHERE AccountNumber = :accountNumber AND ServicePeriod = :period")
    DisconnectionList getOne(String accountNumber, String period);

    @Query("SELECT * FROM DisconnectionList WHERE BillId = :billId")
    DisconnectionList getOneByBillId(String billId);

    @Query("SELECT ServicePeriod, AreaCode FROM DisconnectionList WHERE IsUploaded IS NULL GROUP BY ServicePeriod, AreaCode")
    List<DisconnectionGroupList> getGroupDownloaded();

    @Query("SELECT * FROM DisconnectionList WHERE ServicePeriod=:period AND (IsUploaded IS NULL OR IsUploaded='UPLOADABLE') AND AreaCode=:areaCode ORDER BY SequenceCode")
    List<DisconnectionList> getListBySchedule(String period, String areaCode);

    @Query("SELECT * FROM DisconnectionList WHERE IsUploaded='UPLOADABLE'")
    List<DisconnectionList> getUploadable();
}
