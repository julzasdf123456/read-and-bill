package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Tracks {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @ColumnInfo(name = "TrackNameId")
    private String TrackNameId;

    @ColumnInfo(name = "Latitude")
    private String Latitude;

    @ColumnInfo(name = "Longitude")
    private String Longitude;

    @ColumnInfo(name = "Uploaded")
    private String Uploaded;

    @ColumnInfo(name = "created_at")
    private String created_at;

    public Tracks() {
    }

    public Tracks(String trackNameId, String latitude, String longitude, String uploaded, String created_at) {
        TrackNameId = trackNameId;
        Latitude = latitude;
        Longitude = longitude;
        Uploaded = uploaded;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrackNameId() {
        return TrackNameId;
    }

    public void setTrackNameId(String trackNameId) {
        TrackNameId = trackNameId;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUploaded() {
        return Uploaded;
    }

    public void setUploaded(String uploaded) {
        Uploaded = uploaded;
    }
}
