package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TracksDao {
    @Query("SELECT * FROM Tracks ORDER BY id DESC")
    List<Tracks> getAll();

    @Query("SELECT COUNT(id) FROM Tracks WHERE Uploaded='No'")
    int getCount();

    @Query("SELECT * FROM Tracks WHERE TrackNameId = :trackNameId ORDER BY id DESC")
    List<Tracks> getAllByTrackNameId(String trackNameId);

    @Query("SELECT * FROM Tracks WHERE Uploaded = 'No'")
    Tracks getOneByUnuploaded();

    @Insert
    void insertAll(Tracks... tracks);

    @Update
    void updateAll(Tracks... tracks);

    @Query("DELETE FROM Tracks WHERE TrackNameId = :trackNameId")
    void deleteByTrackNameId(String trackNameId);
}
