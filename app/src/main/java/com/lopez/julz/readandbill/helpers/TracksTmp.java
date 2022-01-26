package com.lopez.julz.readandbill.helpers;

import androidx.room.ColumnInfo;

public class TracksTmp {

    private String id;

    private String TrackNameId;

    private String Latitude;

    private String Longitude;

    private String Uploaded, created_at, updated_at;

    private String Captured;

    public TracksTmp() {
    }

    public TracksTmp(String id, String created_at, String updated_at, String trackNameId, String latitude, String longitude, String uploaded, String captured) {
        this.id = id;
        TrackNameId = trackNameId;
        this.created_at = created_at;
        this.updated_at = updated_at;
        Latitude = latitude;
        Longitude = longitude;
        Uploaded = uploaded;
        Captured = captured;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getUploaded() {
        return Uploaded;
    }

    public void setUploaded(String uploaded) {
        Uploaded = uploaded;
    }

    public String getCaptured() {
        return Captured;
    }

    public void setCaptured(String captured) {
        Captured = captured;
    }
}
