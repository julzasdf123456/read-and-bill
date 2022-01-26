package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TrackNames {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "TrackName")
    private String TrackName;

    @ColumnInfo(name = "Uploaded")
    private String Uploaded;

    public TrackNames() {
    }

    public TrackNames(String id, String trackName) {
        this.id = id;
        TrackName = trackName;
    }

    public TrackNames(String id, String trackName, String uploaded) {
        this.id = id;
        TrackName = trackName;
        Uploaded = uploaded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrackName() {
        return TrackName;
    }

    public void setTrackName(String trackName) {
        TrackName = trackName;
    }

    public String getUploaded() {
        return Uploaded;
    }

    public void setUploaded(String uploaded) {
        Uploaded = uploaded;
    }
}
