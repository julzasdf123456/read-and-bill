package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TrackNamesDao {
    @Query("SELECT * FROM TrackNames")
    List<TrackNames> getAll();

    @Query("SELECT COUNT(id) FROM TrackNames WHERE Uploaded='No'")
    int getCount();

    @Insert
    void insertAll(TrackNames... trackNames);

    @Update
    void updateAll(TrackNames... trackNames);

    @Query("SELECT * FROM TrackNames WHERE TrackName = :trackName")
    TrackNames getOneByTrackName(String trackName);

    @Query("SELECT * FROM TrackNames WHERE Uploaded = 'No'")
    TrackNames getOneByUnuploaded();

    @Query("DELETE FROM TrackNames WHERE id = :id")
    void deleteOne(String id);
}
