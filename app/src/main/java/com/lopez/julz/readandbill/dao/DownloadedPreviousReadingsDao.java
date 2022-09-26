package com.lopez.julz.readandbill.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DownloadedPreviousReadingsDao {
    @Query("SELECT * FROM DownloadedPreviousReadings")
    List<DownloadedPreviousReadings> getAll();

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE id = :id")
    DownloadedPreviousReadings getOne(String id);

    @Insert
    void insertAll(DownloadedPreviousReadings... downloadedPreviousReadings);

    @Update
    void updateAll(DownloadedPreviousReadings... downloadedPreviousReadings);

    @Query("DELETE FROM DownloadedPreviousReadings WHERE ServicePeriod = :servicePeriod")
    void deleteAllByServicePeriod(String servicePeriod);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE ServicePeriod = :servicePeriod AND Town = :areaCode AND GroupCode = :groupCode ORDER BY CAST(SequenceCode AS INT)")
    List<DownloadedPreviousReadings> getAllFromSchedule(String servicePeriod, String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE CAST(SequenceCode AS INT) > :sequenceCode AND Town = :areaCode AND GroupCode = :groupCode ORDER BY CAST(SequenceCode AS INT) LIMIT 1")
    DownloadedPreviousReadings getNext(int sequenceCode, String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE CAST(SequenceCode AS INT) < :sequenceCode AND Town = :areaCode AND GroupCode = :groupCode ORDER BY CAST(SequenceCode AS INT) DESC LIMIT 1")
    DownloadedPreviousReadings getPrevious(int sequenceCode, String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE Town = :areaCode AND GroupCode = :groupCode ORDER BY SequenceCode LIMIT 1")
    DownloadedPreviousReadings getFirst(String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE Town = :areaCode AND GroupCode = :groupCode ORDER BY SequenceCode DESC LIMIT 1")
    DownloadedPreviousReadings getLast(String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE ServicePeriod = :servicePeriod AND Town = :areaCode AND GroupCode = :groupCode AND Status IS NULL ORDER BY CAST(SequenceCode AS INT)")
    List<DownloadedPreviousReadings> getAllUnread(String servicePeriod, String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE ServicePeriod = :servicePeriod AND Town = :areaCode AND GroupCode = :groupCode AND Status='READ' ORDER BY CAST(SequenceCode AS INT)")
    List<DownloadedPreviousReadings> getAllRead(String servicePeriod, String areaCode, String groupCode);

    @Query("SELECT * FROM DownloadedPreviousReadings WHERE (ServiceAccountName LIKE :regex OR MeterSerial LIKE :regex OR OldAccountNo LIKE :regex) AND ServicePeriod = :servicePeriod AND Town = :areaCode AND GroupCode = :groupCode ORDER BY ServiceAccountName")
    List<DownloadedPreviousReadings> getSearch(String servicePeriod, String areaCode, String groupCode, String regex);
}
